package vn.id.tozydev.tusu.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import androidx.sqlite.SQLiteException
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toLocalDateTime
import vn.id.tozydev.tusu.data.db.TusuDatabase
import vn.id.tozydev.tusu.data.db.entry.EntryEntity
import vn.id.tozydev.tusu.data.db.entry.EntryWithRelations
import vn.id.tozydev.tusu.data.db.entry.reindexEntry
import vn.id.tozydev.tusu.data.db.entry.toCalendarDay
import vn.id.tozydev.tusu.data.db.entry.toDomain
import vn.id.tozydev.tusu.data.db.entry.toEntryLocation
import vn.id.tozydev.tusu.data.db.entry.toRoomRawQuery
import vn.id.tozydev.tusu.data.db.label.EntryLabelEntity
import vn.id.tozydev.tusu.data.db.transaction
import vn.id.tozydev.tusu.domain.error.EntryError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.error.getOrNull
import vn.id.tozydev.tusu.domain.error.onFailure
import vn.id.tozydev.tusu.domain.model.CalendarDay
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.domain.model.EntryDraft
import vn.id.tozydev.tusu.domain.model.EntryFilter
import vn.id.tozydev.tusu.domain.model.EntryLocation
import vn.id.tozydev.tusu.domain.repository.EntryRepository
import vn.id.tozydev.tusu.domain.repository.MediaFileStore
import vn.id.tozydev.tusu.text.deriveText

private val logger = KotlinLogging.logger {}

private const val PAGE_SIZE = 30

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
class EntryRepositoryImpl(
    private val database: TusuDatabase,
    private val files: MediaFileStore,
    private val clock: Clock,
) : EntryRepository {

    override fun observeEntry(id: Uuid): Flow<Entry?> =
        database.entryDao().observeWithRelations(id).map { it?.toDomain() }

    override fun observeTimeline(): Flow<PagingData<Entry>> = pagedEntries {
        database.entryDao().timelinePagingSource()
    }

    override fun observeCalendar(month: YearMonth): Flow<List<CalendarDay>> =
        database.entryDao().observeCalendarRows(from = month.firstDay, to = month.lastDay).map {
            rows ->
            rows.map { it.toCalendarDay() }
        }

    override fun observeDay(date: LocalDate): Flow<List<Entry>> =
        database.entryDao().observeDay(date).map { rows -> rows.map { it.toDomain() } }

    override fun search(filter: EntryFilter): Flow<PagingData<Entry>> {
        val query = filter.toRoomRawQuery()
        return pagedEntries { database.entryDao().searchPagingSource(query) }
    }

    override fun observeTrash(): Flow<List<Entry>> =
        database.entryDao().observeTrash().map { rows -> rows.map { it.toDomain() } }

    override fun observeRecentLocations(limit: Int): Flow<List<EntryLocation>> =
        database.entryDao().observeRecentLocationRows(limit).map { rows ->
            rows.map { it.toEntryLocation() }
        }

    override suspend fun save(draft: EntryDraft): Outcome<Unit, EntryError> {
        // A body that cannot be read must not be overwritten, so nothing is written for it.
        val text =
            deriveText(draft.body).getOrNull() ?: return Outcome.Failure(EntryError.UnreadableBody)
        return guardedWrite {
            database.transaction<Outcome<Unit, EntryError>> {
                val dao = database.entryDao()
                val now = clock.now()
                val existing = dao.getById(draft.id)
                dao.upsert(
                    draft.toEntity(
                        textPlain = text.plain,
                        textSearch = text.search,
                        createdAt = existing?.createdAt ?: now,
                        updatedAt = now,
                        deletedAt = existing?.deletedAt,
                    ),
                )
                database.reindexEntry(draft.id)
                Outcome.Success(Unit)
            }
        }
    }

    override suspend fun setLabels(entryId: Uuid, labelIds: Set<Uuid>): Outcome<Unit, EntryError> =
        guardedWrite {
            database.transaction<Outcome<Unit, EntryError>> {
                val dao = database.entryDao()
                val labelDao = database.labelDao()
                val entry = dao.getById(entryId) ?: return@transaction notFound()
                // Checked before any write, so an unknown or trashed label leaves no partial
                // change.
                val allLive = labelIds.all { id ->
                    val label = labelDao.getLabelById(id)
                    label != null && label.deletedAt == null
                }
                if (!allLive) return@transaction notFound()

                val now = clock.now()
                val rows = dao.getEntryLabelRows(entryId)
                val liveIds = rows.filter { it.deletedAt == null }.map { it.labelId }.toSet()
                rows
                    .filter { it.deletedAt == null && it.labelId !in labelIds }
                    .forEach {
                        labelDao.upsertEntryLabel(it.copy(updatedAt = now, deletedAt = now))
                    }
                labelIds
                    .filter { it !in liveIds }
                    .forEach { labelId ->
                        labelDao.upsertEntryLabel(
                            EntryLabelEntity(
                                entryId = entryId,
                                labelId = labelId,
                                updatedAt = now,
                                deletedAt = null,
                            ),
                        )
                    }
                dao.upsert(entry.copy(updatedAt = now))
                database.reindexEntry(entryId)
                Outcome.Success(Unit)
            }
        }

    override suspend fun moveToTrash(id: Uuid): Outcome<Unit, EntryError> =
        updateEntry(id) { entry, now ->
            // Trashing twice keeps the first deletion time.
            if (entry.deletedAt != null) null else entry.copy(deletedAt = now, updatedAt = now)
        }

    override suspend fun restoreFromTrash(id: Uuid): Outcome<Unit, EntryError> =
        updateEntry(id) { entry, now ->
            if (entry.deletedAt == null) null else entry.copy(deletedAt = null, updatedAt = now)
        }

    override suspend fun deletePermanently(id: Uuid): Outcome<Unit, EntryError> {
        var mediaPaths = emptyList<String>()
        val deleted = guardedWrite {
            database.transaction<Outcome<Unit, EntryError>> {
                val dao = database.entryDao()
                if (dao.getById(id) == null) return@transaction notFound()
                mediaPaths = database.mediaDao().localPathsOf(id)
                // Media and label links go with the row through foreign-key cascades.
                dao.deleteFtsByEntryId(id)
                dao.deleteById(id)
                Outcome.Success(Unit)
            }
        }
        if (deleted is Outcome.Failure) return deleted
        // The rows go first: a file without a row is only disk use, while a row without its file
        // breaks the album. Every file is tried, so one failure does not keep the others.
        var fileLeftBehind = false
        for (path in mediaPaths) {
            files.delete(path).onFailure { fileLeftBehind = true }
        }
        return if (fileLeftBehind) Outcome.Failure(EntryError.StorageFailed)
        else Outcome.Success(Unit)
    }

    /**
     * Applies [change] to the entry in one transaction. A null result means nothing changes and the
     * call still succeeds, which keeps repeated trash and restore calls idempotent.
     */
    private suspend fun updateEntry(
        id: Uuid,
        change: (EntryEntity, Instant) -> EntryEntity?,
    ): Outcome<Unit, EntryError> = guardedWrite {
        database.transaction<Outcome<Unit, EntryError>> {
            val dao = database.entryDao()
            val entry = dao.getById(id) ?: return@transaction notFound()
            change(entry, clock.now())?.let { dao.upsert(it) }
            Outcome.Success(Unit)
        }
    }

    /**
     * Converts a SQLite failure into [EntryError.StorageFailed]. Other exceptions are bugs and
     * propagate, and cancellation is never caught.
     */
    private suspend fun guardedWrite(
        block: suspend () -> Outcome<Unit, EntryError>,
    ): Outcome<Unit, EntryError> =
        try {
            block()
        } catch (e: SQLiteException) {
            logger.warn(e) { "Entry write failed" }
            Outcome.Failure(EntryError.StorageFailed)
        }

    private fun pagedEntries(
        source: () -> PagingSource<Int, EntryWithRelations>,
    ): Flow<PagingData<Entry>> =
        Pager(config = PagingConfig(pageSize = PAGE_SIZE), pagingSourceFactory = source).flow.map {
            page ->
            page.map { it.toDomain() }
        }

    private fun notFound(): Outcome<Unit, EntryError> = Outcome.Failure(EntryError.NotFound)
}

private fun EntryDraft.toEntity(
    textPlain: String,
    textSearch: String,
    createdAt: Instant,
    updatedAt: Instant,
    deletedAt: Instant?,
): EntryEntity =
    EntryEntity(
        id = id,
        body = body,
        textPlain = textPlain,
        textSearch = textSearch,
        emoji = emoji,
        occurredAt = occurredAt,
        timezone = timeZone.id,
        localDate = occurredAt.toLocalDateTime(timeZone).date,
        placeName = location?.name,
        placeAddress = location?.address,
        latitude = location?.coordinates?.latitude,
        longitude = location?.coordinates?.longitude,
        mapSnapshotPath = location?.mapSnapshotPath,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )
