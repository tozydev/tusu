package vn.id.tozydev.tusu.data.sample

import androidx.sqlite.SQLiteException
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.TimeZone
import vn.id.tozydev.tusu.data.db.TusuDatabase
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.error.Raise
import vn.id.tozydev.tusu.domain.error.SampleDataError
import vn.id.tozydev.tusu.domain.error.bind
import vn.id.tozydev.tusu.domain.error.ensureNotNull
import vn.id.tozydev.tusu.domain.error.mapError
import vn.id.tozydev.tusu.domain.error.onFailure
import vn.id.tozydev.tusu.domain.error.outcome
import vn.id.tozydev.tusu.domain.error.raise
import vn.id.tozydev.tusu.domain.model.Label
import vn.id.tozydev.tusu.domain.model.Media
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.domain.repository.EntryRepository
import vn.id.tozydev.tusu.domain.repository.LabelRepository
import vn.id.tozydev.tusu.domain.repository.MediaFileStore
import vn.id.tozydev.tusu.domain.repository.MediaRepository

private val logger = KotlinLogging.logger {}

/** What a call to [SampleDataSeeder.seedIfEmpty] wrote; all zeros when it found data. */
data class SeedResult(val entries: Int, val photos: Int, val labelsCreated: Int)

/**
 * Fills an empty database with about three hundred journal entries for trying out the timeline.
 * Everything goes through the repositories, so derived text, the search index and local dates are
 * exactly what the app produces. Only debug builds call it.
 */
@SingleIn(AppScope::class)
@Inject
class SampleDataSeeder(
    private val database: TusuDatabase,
    private val entries: EntryRepository,
    private val labels: LabelRepository,
    private val media: MediaRepository,
    private val files: MediaFileStore,
    private val images: SampleImageSource,
    private val clock: Clock,
    private val timeZone: TimeZone,
) {
    private val running = Mutex()

    /**
     * Does nothing when any entry exists, the trash included. A run that is cancelled or fails
     * halfway leaves its entries behind, and the next call then sees a non-empty journal.
     */
    suspend fun seedIfEmpty(): Outcome<SeedResult, SampleDataError> = running.withLock {
        outcome {
            val existing =
                try {
                    database.entryDao().countAll()
                } catch (e: SQLiteException) {
                    logger.warn(e) { "Could not count entries before seeding" }
                    raise(SampleDataError.StorageFailed)
                }
            if (existing > 0) return@outcome SeedResult(0, 0, 0)

            val (pool, created) = ensureLabelPool()
            val plan = context(clock, timeZone) { planSampleEntries(pool.size, images.images) }
            var photos = 0
            for (planned in plan) {
                entries.save(planned.draft).mapError(SampleDataError::Entry).bind()
                if (planned.labelSlots.isNotEmpty()) {
                    val labelIds = planned.labelSlots.map { pool[it].id }.toSet()
                    entries
                        .setLabels(planned.draft.id, labelIds)
                        .mapError(SampleDataError::Entry)
                        .bind()
                }
                for (photo in planned.photos) {
                    attachPhoto(planned.draft.id, photo)
                    photos++
                }
            }
            logger.info { "Seeded ${plan.size} sample entries with $photos photos" }
            SeedResult(entries = plan.size, photos = photos, labelsCreated = created)
        }
    }

    /** Visible labels, with the sample people and trip labels added where they are missing. */
    context(_: Raise<SampleDataError>)
    private suspend fun ensureLabelPool(): Pair<List<Label>, Int> {
        val groups =
            labels.observeGroups().first().filter { it.isDefault }.sortedBy { it.sortOrder }
        // The first run seeds the groups as kind, people, trips; a missing one falls back to
        // "Khác".
        val peopleGroupId = groups.getOrNull(1)?.id
        val tripsGroupId = groups.getOrNull(2)?.id

        val existing = labels.observeLabels().first().filter { !it.isHidden }
        val pool = existing.toMutableList()
        var created = 0
        val samples =
            SampleContent.peopleLabels.map { it to peopleGroupId } +
                SampleContent.tripLabels.map { it to tripsGroupId }
        for ((name, groupId) in samples) {
            if (existing.any { it.name == name }) continue
            pool += labels.createLabel(name, groupId).mapError(SampleDataError::Label).bind()
            created++
        }
        return pool to created
    }

    context(_: Raise<SampleDataError>)
    private suspend fun attachPhoto(entryId: Uuid, photo: PlannedPhoto) {
        val source =
            ensureNotNull(images.open(photo.image)) {
                SampleDataError.Media(MediaError.FileUnreadable)
            }
        val relativePath = files.newRelativePath(photo.id, photo.image.name.substringAfterLast('.'))
        files.write(relativePath, source).mapError(SampleDataError::Media).bind()
        val stored =
            media.add(
                Media(
                    id = photo.id,
                    entryId = entryId,
                    type = MediaType.PHOTO,
                    localPath = relativePath,
                    width = photo.image.width,
                    height = photo.image.height,
                    position = photo.position,
                    takenAt = photo.takenAt,
                    status = MediaStatus.READY,
                    updatedAt = clock.now(),
                ),
            )
        if (stored is Outcome.Failure) {
            // The row never existed, so the copied file would stay behind unreachable.
            files.delete(relativePath).onFailure {
                logger.warn { "Could not remove the file of a sample photo that failed to save" }
            }
            raise(SampleDataError.Media(stored.error))
        }
    }
}
