package vn.id.tozydev.tusu.data.sample

import androidx.paging.testing.asSnapshot
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.ints.shouldBeBetween
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import vn.id.tozydev.tusu.data.db.TusuDatabase
import vn.id.tozydev.tusu.data.db.label.LabelEntity
import vn.id.tozydev.tusu.data.db.label.LabelGroupEntity
import vn.id.tozydev.tusu.data.db.withTestDatabase
import vn.id.tozydev.tusu.data.repository.EntryRepositoryImpl
import vn.id.tozydev.tusu.data.repository.FakeMediaFileStore
import vn.id.tozydev.tusu.data.repository.LabelRepositoryImpl
import vn.id.tozydev.tusu.data.repository.MediaRepositoryImpl
import vn.id.tozydev.tusu.data.repository.TestClock
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.error.SampleDataError
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.domain.model.EntryDraft
import vn.id.tozydev.tusu.domain.model.EntryFilter
import vn.id.tozydev.tusu.domain.model.LabelSelection
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.text.foldDiacritics

private val HCM = TimeZone.of("Asia/Ho_Chi_Minh")

// 10:30 in Ho Chi Minh City, so the morning entries of "today" are already in the past.
private val NOW: Instant = Instant.parse("2026-11-15T03:30:00Z")

private class FakeImageSource(val available: Boolean = true) : SampleImageSource {
    override val images = List(4) { SampleImage("image-$it.jpg", 4000 + it, 3000 + it) }

    override fun open(image: SampleImage): RawSource? =
        if (available) Buffer().also { it.write(byteArrayOf(1, 2, 3, image.width.toByte())) }
        else null
}

private class Fixture(
    val db: TusuDatabase,
    val files: FakeMediaFileStore,
    val entries: EntryRepositoryImpl,
    val labels: LabelRepositoryImpl,
    val media: MediaRepositoryImpl,
    val seeder: SampleDataSeeder,
) {
    val today = NOW.toLocalDateTime(HCM).date

    /** Every live entry of the sampled year, found through the day query. */
    suspend fun allEntries(): List<Entry> =
        (0 until SAMPLE_DAYS + 5).flatMap {
            entries.observeDay(today.minus(it, DateTimeUnit.DAY)).first()
        }
}

private suspend fun withSeeder(
    images: SampleImageSource = FakeImageSource(),
    block: suspend (Fixture) -> Unit,
) = withTestDatabase { db ->
    // The default groups and labels are seeded by a database callback the in-memory test database
    // does not run, so they are written here: kind, people and trips, in that order.
    val groupIds = List(3) { Uuid.random() }
    listOf("Kind", "People", "Trips").forEachIndexed { index, name ->
        db.labelDao()
            .upsertGroup(
                LabelGroupEntity(
                    id = groupIds[index],
                    name = name,
                    color = 0xFF00AA00.toInt(),
                    selection = LabelSelection.MULTI,
                    showInComposer = true,
                    isDefault = true,
                    sortOrder = index,
                    updatedAt = NOW,
                    deletedAt = null,
                ),
            )
    }
    listOf("Achievement", "Lesson", "Memory", "Gratitude", "Reflection").forEachIndexed { i, name ->
        db.labelDao()
            .upsertLabel(
                LabelEntity(
                    id = Uuid.random(),
                    groupId = groupIds[0],
                    name = name,
                    isHidden = false,
                    isDefault = true,
                    sortOrder = i,
                    updatedAt = NOW,
                    deletedAt = null,
                ),
            )
    }

    val clock = TestClock(NOW)
    val files = FakeMediaFileStore()
    val entries = EntryRepositoryImpl(db, files, clock)
    val labels = LabelRepositoryImpl(db, clock)
    val media = MediaRepositoryImpl(db, files, clock)
    block(
        Fixture(
            db,
            files,
            entries,
            labels,
            media,
            SampleDataSeeder(db, entries, labels, media, files, images, clock, HCM),
        ),
    )
}

/** Everything about an entry that a second run must reproduce. */
private fun Entry.fingerprint() =
    listOf(
        id,
        body,
        textPlain,
        emoji,
        occurredAt,
        location,
        labels.map { it.name }.sorted(),
        media.map { listOf(it.id, it.localPath, it.width, it.height, it.position, it.takenAt) },
    )

private fun Int.percentOf(total: Int): Int = this * 100 / total

class SampleDataSeederTest :
    FunSpec(
        {
            test("seeds about three hundred entries into an empty journal") {
                withSeeder { f ->
                    val result = f.seeder.seedIfEmpty()

                    val seeded = result.shouldBeInstanceOf<Outcome.Success<SeedResult>>().value
                    seeded.entries.shouldBeBetween(250, 400)
                    f.allEntries().size shouldBe seeded.entries
                    f.db.entryDao().countAll() shouldBe seeded.entries
                }
            }

            test("the same clock and zone produce the same journal on two databases") {
                var first = emptyList<List<Any?>>()
                withSeeder { f ->
                    f.seeder.seedIfEmpty()
                    first = f.allEntries().map { it.fingerprint() }
                }
                withSeeder { f ->
                    f.seeder.seedIfEmpty()
                    val second = f.allEntries().map { it.fingerprint() }
                    first.shouldNotBeEmpty()
                    second shouldBe first
                }
            }

            test("entries start today and yesterday and reach back a year without the future") {
                withSeeder { f ->
                    f.seeder.seedIfEmpty()
                    val entries = f.allEntries()

                    entries.count { it.localDate == f.today } shouldNotBe 0
                    entries.count { it.localDate == f.today.minus(1, DateTimeUnit.DAY) } shouldNotBe
                        0
                    entries.map { it.occurredAt }.max() shouldBeLessThanOrEqualTo NOW
                    (entries.map { it.occurredAt }.min() <= NOW - 330.days) shouldBe true
                    entries.groupBy { it.localDate }.values.maxOf { it.size }.shouldBeBetween(1, 6)
                    // Some days stay empty.
                    entries.map { it.localDate }.toSet().size.shouldBeBetween(100, SAMPLE_DAYS - 20)
                    entries.all { it.timeZone == HCM } shouldBe true
                }
            }

            test("every entry has text or photos, and two have only photos") {
                withSeeder { f ->
                    f.seeder.seedIfEmpty()
                    val entries = f.allEntries()

                    entries.all { it.textPlain.isNotBlank() || it.media.isNotEmpty() } shouldBe true
                    entries.count { it.textPlain.isBlank() } shouldBe 2
                    entries.filter { it.textPlain.isBlank() }.all { it.media.isNotEmpty() } shouldBe
                        true
                }
            }

            test("emoji, labels, locations and photos appear in the intended proportions") {
                withSeeder { f ->
                    f.seeder.seedIfEmpty()
                    val entries = f.allEntries()
                    val total = entries.size

                    entries.count { it.emoji != null }.percentOf(total).shouldBeBetween(30, 50)
                    entries
                        .count { it.labels.isNotEmpty() }
                        .percentOf(total)
                        .shouldBeBetween(40, 60)
                    entries.all { it.labels.size <= 3 } shouldBe true
                    entries.count { it.location != null }.percentOf(total).shouldBeBetween(18, 33)
                    entries.count { it.media.isNotEmpty() }.percentOf(total).shouldBeBetween(24, 40)

                    val locations = entries.mapNotNull { it.location }
                    locations.any { it.coordinates != null && it.address != null } shouldBe true
                    locations.any { it.coordinates == null && it.address == null } shouldBe true
                    entries.map { it.textPlain }.any { '#' in it } shouldBe true
                }
            }

            test("photos are ready, sized, ordered and stored in their own files") {
                withSeeder { f ->
                    val seeded =
                        f.seeder
                            .seedIfEmpty()
                            .shouldBeInstanceOf<Outcome.Success<SeedResult>>()
                            .value
                    val photos = f.allEntries().flatMap { it.media }

                    photos.size shouldBe seeded.photos
                    photos.all {
                        it.type == MediaType.PHOTO && it.status == MediaStatus.READY
                    } shouldBe true
                    photos.all {
                        it.width != null && it.height != null && it.takenAt != null
                    } shouldBe true
                    photos.map { it.localPath }.toSet().size shouldBe photos.size
                    f.files.files.keys shouldBe photos.map { it.localPath }.toSet()
                    f.allEntries()
                        .filter { it.media.isNotEmpty() }
                        .forEach { entry ->
                            entry.media.map { it.position } shouldBe entry.media.indices.toList()
                            entry.media.size.shouldBeBetween(1, 6)
                        }
                }
            }

            test("sample people and trip labels join the default ones in their groups") {
                withSeeder { f ->
                    val seeded =
                        f.seeder
                            .seedIfEmpty()
                            .shouldBeInstanceOf<Outcome.Success<SeedResult>>()
                            .value
                    val groups = f.labels.observeGroups().first().associateBy { it.name }
                    val labels = f.labels.observeLabels().first().associateBy { it.name }

                    seeded.labelsCreated shouldBe 6
                    labels.getValue("Mẹ").groupId shouldBe groups.getValue("People").id
                    labels.getValue("Đà Lạt").groupId shouldBe groups.getValue("Trips").id
                    labels.keys shouldContain "Achievement"
                }
            }

            test("the timeline, a day and the calendar return the seeded entries") {
                withSeeder { f ->
                    f.seeder.seedIfEmpty()
                    val newest =
                        f.allEntries().maxWith(compareBy<Entry>({ it.occurredAt }, { it.id }))

                    f.entries.observeTimeline().asSnapshot().first().id shouldBe newest.id
                    f.entries.observeDay(f.today).first().shouldNotBeEmpty()
                    val month = YearMonth(f.today.year, f.today.month)
                    f.entries.observeCalendar(month).first().shouldNotBeEmpty()
                }
            }

            test("search finds a word typed without diacritics") {
                withSeeder { f ->
                    f.seeder.seedIfEmpty()

                    val hits = f.entries.search(EntryFilter(query = "ca phe")).asSnapshot()

                    hits.shouldNotBeEmpty()
                    // The index also holds the place name, so "Cà phê Cộng" is a hit without body
                    // text.
                    hits.all {
                        "ca phe" in
                            "${it.textPlain} ${it.location?.name.orEmpty()}"
                                .foldDiacritics()
                                .lowercase()
                    } shouldBe true
                    hits.any { "ca phe" in it.textPlain.foldDiacritics().lowercase() } shouldBe true
                }
            }

            test("a second call, or a journal with only trashed entries, is left alone") {
                withSeeder { f ->
                    val first = f.seeder.seedIfEmpty()
                    first
                        .shouldBeInstanceOf<Outcome.Success<SeedResult>>()
                        .value
                        .entries shouldNotBe 0
                    val count = f.db.entryDao().countAll()

                    f.seeder.seedIfEmpty() shouldBe Outcome.Success(SeedResult(0, 0, 0))
                    f.db.entryDao().countAll() shouldBe count
                }
                withSeeder { f ->
                    val id = Uuid.random()
                    f.entries.save(EntryDraft(id, "", occurredAt = NOW, timeZone = HCM))
                    f.entries.moveToTrash(id)

                    f.seeder.seedIfEmpty() shouldBe Outcome.Success(SeedResult(0, 0, 0))
                    f.db.entryDao().countAll() shouldBe 1
                }
            }

            test("a photo that cannot be read stops the run with a typed error") {
                withSeeder(images = FakeImageSource(available = false)) { f ->
                    f.seeder.seedIfEmpty() shouldBe
                        Outcome.Failure(SampleDataError.Media(MediaError.FileUnreadable))
                }
            }
        },
    )
