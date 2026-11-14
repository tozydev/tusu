package vn.id.tozydev.tusu.data.repository

import androidx.paging.testing.asSnapshot
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.document.RichTextBlock
import com.mohamedrejeb.richeditor.document.RichTextDocument
import com.mohamedrejeb.richeditor.json.toJson
import com.mohamedrejeb.richeditor.model.RichTextState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toInstant
import vn.id.tozydev.tusu.data.db.TusuDatabase
import vn.id.tozydev.tusu.data.db.entry.reindexEntriesWithLabel
import vn.id.tozydev.tusu.data.db.label.EntryLabelEntity
import vn.id.tozydev.tusu.data.db.label.LabelEntity
import vn.id.tozydev.tusu.data.db.label.LabelGroupEntity
import vn.id.tozydev.tusu.data.db.media.MediaEntity
import vn.id.tozydev.tusu.data.db.transaction
import vn.id.tozydev.tusu.data.db.withTestDatabase
import vn.id.tozydev.tusu.domain.error.EntryError
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.domain.model.EntryDraft
import vn.id.tozydev.tusu.domain.model.EntryFilter
import vn.id.tozydev.tusu.domain.model.EntryLocation
import vn.id.tozydev.tusu.domain.model.GeoPoint
import vn.id.tozydev.tusu.domain.model.LabelSelection
import vn.id.tozydev.tusu.domain.model.Media
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType

private val HCM = TimeZone.of("Asia/Ho_Chi_Minh")
private val T0: Instant = Instant.parse("2026-11-01T00:00:00Z")

/** Wall-clock time in [zone], so tests read as the local day the user sees. */
private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int = 0, zone: TimeZone = HCM) =
    LocalDateTime(year, month, day, hour, minute).toInstant(zone)

@OptIn(ExperimentalRichTextApi::class)
private fun body(text: String): String =
    RichTextState()
        .setRichTextDocument(RichTextDocument(listOf(RichTextBlock(text = text))))
        .toJson()

private fun draft(
    id: Uuid = Uuid.random(),
    text: String = "Hello",
    at: Instant = T0,
    zone: TimeZone = HCM,
    emoji: String? = null,
    location: EntryLocation? = null,
) =
    EntryDraft(
        id = id,
        body = body(text),
        emoji = emoji,
        occurredAt = at,
        timeZone = zone,
        location = location,
    )

private suspend fun withRepo(
    block: suspend (db: TusuDatabase, clock: TestClock, repo: EntryRepositoryImpl) -> Unit,
) = withTestDatabase { db ->
    val clock = TestClock(T0)
    block(db, clock, EntryRepositoryImpl(db, FakeMediaFileStore(), clock))
}

private suspend fun EntryRepositoryImpl.entryOrFail(id: Uuid): Entry =
    observeEntry(id).first() ?: error("entry $id is missing")

private suspend fun TusuDatabase.addGroup(
    id: Uuid,
    name: String,
    selection: LabelSelection = LabelSelection.MULTI,
) =
    labelDao()
        .upsertGroup(
            LabelGroupEntity(
                id = id,
                name = name,
                color = 0xFF00AA00.toInt(),
                selection = selection,
                showInComposer = true,
                isDefault = false,
                sortOrder = 0,
                updatedAt = T0,
                deletedAt = null,
            ),
        )

private suspend fun TusuDatabase.addLabel(
    id: Uuid,
    name: String,
    groupId: Uuid? = null,
    sortOrder: Int = 0,
    deletedAt: Instant? = null,
) =
    labelDao()
        .upsertLabel(
            LabelEntity(
                id = id,
                groupId = groupId,
                name = name,
                isHidden = false,
                isDefault = false,
                sortOrder = sortOrder,
                updatedAt = T0,
                deletedAt = deletedAt,
            ),
        )

private suspend fun TusuDatabase.addMedia(
    entryId: Uuid,
    type: MediaType,
    status: MediaStatus = MediaStatus.READY,
    deletedAt: Instant? = null,
    position: Int = 0,
): Uuid {
    val id = Uuid.random()
    mediaDao()
        .upsert(
            MediaEntity(
                id = id,
                entryId = entryId,
                type = type,
                localPath = "media/$id.jpg",
                remoteKey = null,
                width = null,
                height = null,
                durationMs = null,
                position = position,
                takenAt = null,
                status = status,
                waveform = emptyList(),
                updatedAt = T0,
                deletedAt = deletedAt,
            ),
        )
    return id
}

/** Waits for the next value of a channel that satisfies [predicate]. */
private suspend fun <T> Channel<T>.receiveUntil(predicate: (T) -> Boolean): T {
    while (true) {
        val value = receive()
        if (predicate(value)) return value
    }
}

class EntryRepositoryImplTest :
    FunSpec(
        {
            test("saving an existing entry keeps created_at and moves updated_at") {
                withRepo { _, clock, repo ->
                    val id = Uuid.random()
                    repo.save(draft(id, "Bản nháp")) shouldBe Outcome.Success(Unit)

                    clock.instant = T0 + 1.hours
                    repo.save(draft(id, "Bản cuối")) shouldBe Outcome.Success(Unit)

                    val entry = repo.entryOrFail(id)
                    entry.createdAt shouldBe T0
                    entry.updatedAt shouldBe T0 + 1.hours
                    entry.textPlain shouldBe "Bản cuối"
                }
            }

            test("plain and search text are derived and the local date uses the entry zone") {
                withRepo { _, _, repo ->
                    val id = Uuid.random()
                    val occurred = at(2026, 11, 14, 1, 30) // 2026-11-13 18:30 UTC

                    repo.save(draft(id, "Cà phê sữa", at = occurred, zone = HCM))

                    val entry = repo.entryOrFail(id)
                    entry.textPlain shouldBe "Cà phê sữa"
                    entry.localDate shouldBe LocalDate(2026, 11, 14)
                    entry.timeZone shouldBe HCM
                    entry.occurredAt shouldBe occurred
                }
            }

            test("a malformed body is rejected and nothing is written") {
                withRepo { db, _, repo ->
                    val id = Uuid.random()
                    val broken =
                        EntryDraft(
                            id = id,
                            body = "{not json",
                            occurredAt = T0,
                            timeZone = HCM,
                        )

                    repo.save(broken) shouldBe Outcome.Failure(EntryError.UnreadableBody)

                    db.entryDao().getById(id) shouldBe null
                }
            }

            test("timeline lists live entries newest first and leaves out the trash") {
                withRepo { _, _, repo ->
                    val older = Uuid.random()
                    val trashed = Uuid.random()
                    val newest = Uuid.random()
                    repo.save(draft(older, "Cũ", at = T0))
                    repo.save(draft(trashed, "Đã xóa", at = T0 + 1.hours))
                    repo.save(draft(newest, "Mới", at = T0 + 2.hours))
                    repo.moveToTrash(trashed) shouldBe Outcome.Success(Unit)

                    repo.observeTimeline().asSnapshot().map { it.id } shouldContainExactly
                        listOf(newest, older)
                }
            }

            test("day query returns one day in chronological order") {
                withRepo { _, _, repo ->
                    val evening = Uuid.random()
                    val morning = Uuid.random()
                    val otherDay = Uuid.random()
                    repo.save(draft(evening, at = at(2026, 11, 14, 20)))
                    repo.save(draft(morning, at = at(2026, 11, 14, 8)))
                    repo.save(draft(otherDay, at = at(2026, 11, 15, 8)))

                    repo.observeDay(LocalDate(2026, 11, 14)).first().map { it.id } shouldBe
                        listOf(morning, evening)
                }
            }

            test("calendar counts entries per day, takes the latest emoji and respects months") {
                withRepo { _, _, repo ->
                    repo.save(draft(at = at(2026, 11, 14, 8), emoji = "😀"))
                    repo.save(draft(at = at(2026, 11, 14, 20), emoji = "🌧"))
                    repo.save(draft(at = at(2026, 11, 14, 21), emoji = null))
                    repo.save(draft(at = at(2026, 11, 30, 10)))
                    // Edges of the month: local 31 Oct and 1 Dec must not be counted.
                    repo.save(draft(at = at(2026, 10, 31, 23)))
                    repo.save(draft(at = at(2026, 12, 1, 0, 30)))

                    repo.observeCalendar(YearMonth(2026, Month.NOVEMBER)).first() shouldBe
                        listOf(
                            vn.id.tozydev.tusu.domain.model.CalendarDay(
                                date = LocalDate(2026, 11, 14),
                                emoji = "🌧",
                                entryCount = 3,
                            ),
                            vn.id.tozydev.tusu.domain.model.CalendarDay(
                                date = LocalDate(2026, 11, 30),
                                emoji = null,
                                entryCount = 1,
                            ),
                        )
                }
            }

            test("trash, restore and repeated calls are idempotent") {
                withRepo { _, clock, repo ->
                    val id = Uuid.random()
                    repo.save(draft(id))

                    clock.instant = T0 + 1.hours
                    repo.moveToTrash(id) shouldBe Outcome.Success(Unit)
                    clock.instant = T0 + 2.hours
                    repo.moveToTrash(id) shouldBe Outcome.Success(Unit)

                    val trashed = repo.entryOrFail(id)
                    trashed.deletedAt shouldBe T0 + 1.hours
                    repo.observeTrash().first().map { it.id } shouldBe listOf(id)

                    repo.restoreFromTrash(id) shouldBe Outcome.Success(Unit)
                    repo.restoreFromTrash(id) shouldBe Outcome.Success(Unit)
                    repo.entryOrFail(id).deletedAt shouldBe null
                    repo.observeTrash().first() shouldBe emptyList()
                }
            }

            test("trash and restore report NotFound for a missing entry") {
                withRepo { _, _, repo ->
                    val missing = Uuid.random()

                    repo.moveToTrash(missing) shouldBe Outcome.Failure(EntryError.NotFound)
                    repo.restoreFromTrash(missing) shouldBe Outcome.Failure(EntryError.NotFound)
                }
            }

            test("trash lists the most recently deleted first") {
                withRepo { _, clock, repo ->
                    val first = Uuid.random()
                    val second = Uuid.random()
                    repo.save(draft(first))
                    repo.save(draft(second))

                    clock.instant = T0 + 1.hours
                    repo.moveToTrash(first)
                    clock.instant = T0 + 2.hours
                    repo.moveToTrash(second)

                    repo.observeTrash().first().map { it.id } shouldBe listOf(second, first)
                }
            }

            test("deleting permanently removes media, labels and the search row") {
                withRepo { db, _, repo ->
                    val id = Uuid.random()
                    val groupId = Uuid.random()
                    val labelId = Uuid.random()
                    repo.save(draft(id, "Xóa hẳn"))
                    db.addGroup(groupId, "Tâm trạng")
                    db.addLabel(labelId, "Vui", groupId = groupId)
                    repo.setLabels(id, setOf(labelId)) shouldBe Outcome.Success(Unit)
                    val mediaId = db.addMedia(id, MediaType.PHOTO)

                    repo.deletePermanently(id) shouldBe Outcome.Success(Unit)

                    repo.observeEntry(id).first() shouldBe null
                    db.mediaDao().getById(mediaId) shouldBe null
                    db.entryDao().searchEntryIds("hello").first() shouldBe emptyList()
                    repo.search(EntryFilter(query = "xoa")).asSnapshot() shouldBe emptyList()
                }
            }

            test("deleting permanently deletes the files of live and trashed media") {
                withTestDatabase { db ->
                    val files = FakeMediaFileStore()
                    val repo = EntryRepositoryImpl(db, files, TestClock(T0))
                    val id = Uuid.random()
                    repo.save(draft(id))
                    val live = db.addMedia(id, MediaType.PHOTO)
                    val trashed = db.addMedia(id, MediaType.AUDIO, deletedAt = T0, position = 1)

                    repo.deletePermanently(id) shouldBe Outcome.Success(Unit)

                    files.deleted shouldContainExactlyInAnyOrder
                        listOf("media/$live.jpg", "media/$trashed.jpg")
                }
            }

            test("a media file that cannot be deleted is reported after the entry is gone") {
                withTestDatabase { db ->
                    val files = FakeMediaFileStore()
                    files.deleteError = MediaError.StorageFailed
                    val repo = EntryRepositoryImpl(db, files, TestClock(T0))
                    val id = Uuid.random()
                    repo.save(draft(id))
                    db.addMedia(id, MediaType.PHOTO)

                    repo.deletePermanently(id) shouldBe Outcome.Failure(EntryError.StorageFailed)

                    repo.observeEntry(id).first() shouldBe null
                }
            }

            test("deleting permanently a missing entry reports NotFound") {
                withRepo { _, _, repo ->
                    repo.deletePermanently(Uuid.random()) shouldBe
                        Outcome.Failure(EntryError.NotFound)
                }
            }

            test("setLabels adds, replaces and clears labels") {
                withRepo { db, _, repo ->
                    val id = Uuid.random()
                    val groupId = Uuid.random()
                    val x = Uuid.random()
                    val y = Uuid.random()
                    val z = Uuid.random()
                    repo.save(draft(id))
                    db.addGroup(groupId, "Tâm trạng")
                    db.addLabel(x, "Xa", groupId = groupId, sortOrder = 0)
                    db.addLabel(y, "Yêu", groupId = groupId, sortOrder = 1)
                    db.addLabel(z, "Zen", groupId = null, sortOrder = 2)

                    repo.setLabels(id, setOf(x)) shouldBe Outcome.Success(Unit)
                    repo.entryOrFail(id).labels.map { it.id } shouldBe listOf(x)

                    repo.setLabels(id, setOf(y, z)) shouldBe Outcome.Success(Unit)
                    repo.entryOrFail(id).labels.map { it.id } shouldBe listOf(y, z)

                    repo.setLabels(id, emptySet()) shouldBe Outcome.Success(Unit)
                    repo.entryOrFail(id).labels shouldBe emptyList()
                }
            }

            test("setLabels rejects unknown labels and writes nothing") {
                withRepo { db, _, repo ->
                    val id = Uuid.random()
                    val known = Uuid.random()
                    repo.save(draft(id))
                    db.addLabel(known, "Vui")

                    repo.setLabels(id, setOf(known, Uuid.random())) shouldBe
                        Outcome.Failure(EntryError.NotFound)

                    repo.entryOrFail(id).labels shouldBe emptyList()
                }
            }

            test("setLabels reports NotFound for a missing entry") {
                withRepo { db, _, repo ->
                    val label = Uuid.random()
                    db.addLabel(label, "Vui")

                    repo.setLabels(Uuid.random(), setOf(label)) shouldBe
                        Outcome.Failure(EntryError.NotFound)
                }
            }

            test("soft-deleted media and labels are not emitted with the entry") {
                withRepo { db, _, repo ->
                    val id = Uuid.random()
                    val live = Uuid.random()
                    val gone = Uuid.random()
                    repo.save(draft(id))
                    val livePhoto = db.addMedia(id, MediaType.PHOTO, position = 0)
                    db.addMedia(id, MediaType.AUDIO, deletedAt = T0, position = 1)
                    db.addLabel(live, "Vui")
                    db.addLabel(gone, "Đã xóa", deletedAt = T0)
                    db.labelDao().upsertEntryLabel(EntryLabelEntity(id, live, T0, null))
                    db.labelDao().upsertEntryLabel(EntryLabelEntity(id, gone, T0, null))

                    val entry = repo.entryOrFail(id)

                    entry.media.map { it.id } shouldBe listOf(livePhoto)
                    entry.labels.map { it.id } shouldBe listOf(live)
                }
            }

            test("observeEntry re-emits when media or labels change") {
                withRepo { db, _, repo ->
                    val id = Uuid.random()
                    val label = Uuid.random()
                    repo.save(draft(id))
                    db.addLabel(label, "Vui")
                    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
                    val seen = Channel<Entry?>(Channel.UNLIMITED)
                    val job = scope.launch { repo.observeEntry(id).collect { seen.send(it) } }
                    try {
                        withTimeout(5_000) { seen.receive() }?.media shouldBe emptyList<Media>()

                        db.addMedia(id, MediaType.PHOTO)
                        withTimeout(5_000) { seen.receiveUntil { it?.media?.size == 1 } }

                        repo.setLabels(id, setOf(label))
                        withTimeout(5_000) { seen.receiveUntil { it?.labels?.size == 1 } }
                    } finally {
                        job.cancel()
                        scope.cancel()
                    }
                }
            }

            test("search matches text without diacritics and by prefix") {
                withRepo { _, _, repo ->
                    val coffee = Uuid.random()
                    val road = Uuid.random()
                    val lesson = Uuid.random()
                    repo.save(draft(coffee, "Cà phê sữa đá", at = at(2026, 11, 10, 8)))
                    repo.save(draft(road, "Đường lên núi", at = at(2026, 11, 11, 9)))
                    repo.save(draft(lesson, "Bài học Đà Nẵng", at = at(2026, 11, 12, 10)))

                    repo.search(EntryFilter(query = "ca phe")).asSnapshot().map { it.id } shouldBe
                        listOf(coffee)
                    repo.search(EntryFilter(query = "duong")).asSnapshot().map { it.id } shouldBe
                        listOf(road)
                    repo.search(EntryFilter(query = "Đường")).asSnapshot().map { it.id } shouldBe
                        listOf(road)
                    repo.search(EntryFilter(query = "bai ho")).asSnapshot().map { it.id } shouldBe
                        listOf(lesson)
                    repo.search(EntryFilter(query = "ca ph")).asSnapshot().map { it.id } shouldBe
                        listOf(coffee)
                }
            }

            test("search ignores trashed entries") {
                withRepo { _, _, repo ->
                    val id = Uuid.random()
                    repo.save(draft(id, "Cà phê cũ"))
                    repo.moveToTrash(id)

                    repo.search(EntryFilter(query = "ca phe")).asSnapshot() shouldBe emptyList()
                }
            }

            test("search matches label names and place names") {
                withRepo { db, _, repo ->
                    val tagged = Uuid.random()
                    val placed = Uuid.random()
                    val groupId = Uuid.random()
                    val label = Uuid.random()
                    repo.save(draft(tagged, "Một ngày nắng"))
                    repo.save(
                        draft(
                            placed,
                            "Chiều nay",
                            location = EntryLocation(name = "Hồ Gươm", coordinates = null),
                        ),
                    )
                    db.addGroup(groupId, "Tâm trạng")
                    db.addLabel(label, "Buồn", groupId = groupId)
                    repo.setLabels(tagged, setOf(label))

                    repo.search(EntryFilter(query = "buon")).asSnapshot().map { it.id } shouldBe
                        listOf(tagged)
                    repo.search(EntryFilter(query = "guom")).asSnapshot().map { it.id } shouldBe
                        listOf(placed)
                }
            }

            test("a query without a letter or digit matches nothing") {
                withRepo { _, _, repo ->
                    repo.save(draft(text = "Sinh nhật 🎉"))

                    repo.search(EntryFilter(query = "🎉")).asSnapshot() shouldBe emptyList()
                    repo.search(EntryFilter(query = "???")).asSnapshot() shouldBe emptyList()
                    repo.search(EntryFilter(query = "  ")).asSnapshot().size shouldBe 1
                }
            }

            test("FTS syntax characters in the query do not throw") {
                withRepo { _, _, repo ->
                    repo.save(draft(text = "Ghi chú"))

                    repo.search(EntryFilter(query = "\"")).asSnapshot()
                    repo.search(EntryFilter(query = "*")).asSnapshot()
                    repo.search(EntryFilter(query = "-")).asSnapshot()
                    repo.search(EntryFilter(query = "NEAR( OR AND")).asSnapshot()
                    repo.search(EntryFilter(query = "x:\"*-")).asSnapshot() shouldBe emptyList()
                }
            }

            test("each filter alone selects the matching entries") {
                withRepo { db, _, repo ->
                    val photo = Uuid.random()
                    val audio = Uuid.random()
                    val placed = Uuid.random()
                    val emojiEntry = Uuid.random()
                    repo.save(draft(photo, "Ảnh", at = at(2026, 11, 10, 8)))
                    repo.save(draft(audio, "Giọng nói", at = at(2026, 11, 11, 9)))
                    repo.save(
                        draft(
                            placed,
                            "Quán cà phê",
                            at = at(2026, 11, 12, 10),
                            location = EntryLocation(name = "Quán", coordinates = null),
                        ),
                    )
                    repo.save(draft(emojiEntry, "Mưa", at = at(2026, 11, 13, 11), emoji = "🌧"))
                    db.addMedia(photo, MediaType.PHOTO)
                    db.addMedia(audio, MediaType.AUDIO)

                    repo.search(EntryFilter(mediaTypes = setOf(MediaType.AUDIO))).asSnapshot().map {
                        it.id
                    } shouldBe listOf(audio)
                    repo.search(EntryFilter(hasLocation = true)).asSnapshot().map { it.id } shouldBe
                        listOf(placed)
                    repo.search(EntryFilter(emojis = setOf("🌧"))).asSnapshot().map {
                        it.id
                    } shouldBe listOf(emojiEntry)
                    repo
                        .search(
                            EntryFilter(
                                dateRange = LocalDate(2026, 11, 11)..LocalDate(2026, 11, 12),
                            ),
                        )
                        .asSnapshot()
                        .map { it.id } shouldBe listOf(placed, audio)
                }
            }

            test("label groups combine with AND and labels inside a group with OR") {
                withRepo { db, _, repo ->
                    val mood = Uuid.random()
                    val topic = Uuid.random()
                    val happy = Uuid.random()
                    val sad = Uuid.random()
                    val study = Uuid.random()
                    val a = Uuid.random()
                    val b = Uuid.random()
                    val c = Uuid.random()
                    db.addGroup(mood, "Tâm trạng")
                    db.addGroup(topic, "Chủ đề")
                    db.addLabel(happy, "Vui", groupId = mood)
                    db.addLabel(sad, "Buồn", groupId = mood)
                    db.addLabel(study, "Học", groupId = topic)
                    repo.save(draft(a, "A", at = at(2026, 11, 10, 8)))
                    repo.save(draft(b, "B", at = at(2026, 11, 11, 8)))
                    repo.save(draft(c, "C", at = at(2026, 11, 12, 8)))
                    repo.setLabels(a, setOf(happy))
                    repo.setLabels(b, setOf(sad, study))
                    repo.setLabels(c, setOf(happy, study))

                    repo
                        .search(EntryFilter(labelIdsByGroup = mapOf(mood to setOf(happy, sad))))
                        .asSnapshot()
                        .map { it.id } shouldBe listOf(c, b, a)
                    repo
                        .search(
                            EntryFilter(
                                labelIdsByGroup =
                                    mapOf(mood to setOf(happy), topic to setOf(study)),
                            ),
                        )
                        .asSnapshot()
                        .map { it.id } shouldBe listOf(c)
                    repo
                        .search(
                            EntryFilter(
                                labelIdsByGroup = mapOf(mood to setOf(sad), topic to setOf(happy)),
                            ),
                        )
                        .asSnapshot() shouldBe emptyList()
                }
            }

            test("filters combine with the text query") {
                withRepo { db, _, repo ->
                    val match = Uuid.random()
                    val other = Uuid.random()
                    val topic = Uuid.random()
                    val study = Uuid.random()
                    db.addGroup(topic, "Chủ đề")
                    db.addLabel(study, "Học", groupId = topic)
                    repo.save(draft(match, "Bài học hôm nay", at = at(2026, 11, 10, 8)))
                    repo.save(draft(other, "Bài học khác", at = at(2026, 11, 11, 8)))
                    repo.setLabels(match, setOf(study))

                    repo
                        .search(
                            EntryFilter(
                                query = "bai hoc",
                                labelIdsByGroup = mapOf(topic to setOf(study)),
                            ),
                        )
                        .asSnapshot()
                        .map { it.id } shouldBe listOf(match)
                    repo
                        .search(EntryFilter(query = "bai hoc", hasLocation = true))
                        .asSnapshot() shouldBe emptyList()
                }
            }

            test("recent locations are de-duplicated and newest first") {
                withRepo { _, _, repo ->
                    val hoGuom =
                        EntryLocation(
                            name = "Hồ Gươm",
                            address = "Hoàn Kiếm",
                            coordinates = GeoPoint(21.0285, 105.8542),
                        )
                    repo.save(draft(at = at(2026, 11, 10, 8), location = hoGuom))
                    repo.save(
                        draft(
                            at = at(2026, 11, 11, 8),
                            location = EntryLocation(name = "Nhà ngoại"),
                        ),
                    )
                    repo.save(draft(at = at(2026, 11, 12, 8), location = hoGuom))
                    repo.save(draft(at = at(2026, 11, 13, 8)))

                    repo.observeRecentLocations(10).first() shouldBe
                        listOf(hoGuom, EntryLocation(name = "Nhà ngoại"))
                    repo.observeRecentLocations(1).first() shouldBe listOf(hoGuom)
                }
            }

            test("renaming a label is reflected in search after reindexing its entries") {
                withRepo { db, _, repo ->
                    val id = Uuid.random()
                    val label = Uuid.random()
                    db.addLabel(label, "Vui")
                    repo.save(draft(id, "Ngày đẹp"))
                    repo.setLabels(id, setOf(label))
                    repo.search(EntryFilter(query = "vui")).asSnapshot().map { it.id } shouldBe
                        listOf(id)

                    db.labelDao()
                        .upsertLabel(
                            LabelEntity(
                                id = label,
                                groupId = null,
                                name = "Hạnh phúc",
                                isHidden = false,
                                isDefault = false,
                                sortOrder = 0,
                                updatedAt = T0 + 1.hours,
                                deletedAt = null,
                            ),
                        )
                    db.transaction { db.reindexEntriesWithLabel(label) }

                    repo.search(EntryFilter(query = "vui")).asSnapshot() shouldBe emptyList()
                    repo.search(EntryFilter(query = "hanh phuc")).asSnapshot().map {
                        it.id
                    } shouldBe listOf(id)
                }
            }
        },
    )
