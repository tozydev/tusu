package vn.id.tozydev.tusu.data.db

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import vn.id.tozydev.tusu.data.db.entry.EntryEntity
import vn.id.tozydev.tusu.data.db.entry.EntryFtsEntity
import vn.id.tozydev.tusu.data.db.entry.toDomain
import vn.id.tozydev.tusu.data.db.entry.toEntity
import vn.id.tozydev.tusu.data.db.label.EntryLabelEntity
import vn.id.tozydev.tusu.data.db.label.LabelEntity
import vn.id.tozydev.tusu.data.db.label.LabelGroupEntity
import vn.id.tozydev.tusu.data.db.media.MediaEntity
import vn.id.tozydev.tusu.data.db.place.PlaceEntity
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.domain.model.EntryLocation
import vn.id.tozydev.tusu.domain.model.GeoPoint
import vn.id.tozydev.tusu.domain.model.LabelSelection
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType

private val occurredAt = Instant.parse("2026-11-14T01:30:00Z")

private fun entryEntity(id: Uuid) =
    EntryEntity(
        id = id,
        body = """{"blocks":[]}""",
        textPlain = "Bình minh ở hồ",
        textSearch = "binh minh o ho",
        emoji = null,
        occurredAt = occurredAt,
        timezone = "Asia/Ho_Chi_Minh",
        localDate = LocalDate(2026, 11, 14),
        placeName = "Hồ Gươm",
        placeAddress = null,
        latitude = 21.0285,
        longitude = 105.8542,
        mapSnapshotPath = null,
        createdAt = occurredAt,
        updatedAt = occurredAt,
        deletedAt = null,
    )

private fun mediaEntity(id: Uuid, entryId: Uuid) =
    MediaEntity(
        id = id,
        entryId = entryId,
        type = MediaType.AUDIO,
        localPath = "audio/voice.m4a",
        remoteKey = null,
        width = null,
        height = null,
        durationMs = 61_500L,
        position = 0,
        takenAt = occurredAt,
        status = MediaStatus.READY,
        waveform = listOf(0f, 0.25f, 0.5f, 1f),
        updatedAt = occurredAt,
        deletedAt = null,
    )

class DaoSmokeTest :
    FunSpec(
        {
            test("entry round-trips ids, instants, local date and location") {
                withTestDatabase { db ->
                    val id = Uuid.random()
                    val entry = entryEntity(id)

                    db.entryDao().upsert(entry)

                    db.entryDao().getById(id) shouldBe entry
                }
            }

            test("media round-trips enums, duration and waveform") {
                withTestDatabase { db ->
                    val entryId = Uuid.random()
                    val mediaId = Uuid.random()
                    db.entryDao().upsert(entryEntity(entryId))

                    db.mediaDao().upsert(mediaEntity(mediaId, entryId))

                    db.mediaDao().getById(mediaId) shouldBe mediaEntity(mediaId, entryId)
                }
            }

            test("deleting an entry cascades to its media") {
                withTestDatabase { db ->
                    val entryId = Uuid.random()
                    val mediaId = Uuid.random()
                    db.entryDao().upsert(entryEntity(entryId))
                    db.mediaDao().upsert(mediaEntity(mediaId, entryId))

                    db.entryDao().deleteById(entryId)

                    db.mediaDao().getById(mediaId) shouldBe null
                }
            }

            test("label group, label and entry label round-trip") {
                withTestDatabase { db ->
                    val entryId = Uuid.random()
                    val groupId = Uuid.random()
                    val labelId = Uuid.random()
                    db.entryDao().upsert(entryEntity(entryId))
                    val group =
                        LabelGroupEntity(
                            id = groupId,
                            name = "Tâm trạng",
                            color = 0xFF00AA00.toInt(),
                            selection = LabelSelection.SINGLE,
                            showInComposer = true,
                            isDefault = false,
                            sortOrder = 0,
                            updatedAt = occurredAt,
                            deletedAt = null,
                        )
                    val label =
                        LabelEntity(
                            id = labelId,
                            groupId = groupId,
                            name = "Vui",
                            isHidden = false,
                            isDefault = false,
                            sortOrder = 0,
                            updatedAt = occurredAt,
                            deletedAt = null,
                        )
                    val link =
                        EntryLabelEntity(
                            entryId = entryId,
                            labelId = labelId,
                            updatedAt = occurredAt,
                            deletedAt = null,
                        )

                    db.labelDao().upsertGroup(group)
                    db.labelDao().upsertLabel(label)
                    db.labelDao().upsertEntryLabel(link)

                    db.labelDao().observeGroups().first() shouldBe listOf(group)
                    db.labelDao().observeLabels().first() shouldBe listOf(label)
                    db.labelDao().observeEntryLabels(entryId).first() shouldBe listOf(link)
                }
            }

            test("place round-trips coordinates") {
                withTestDatabase { db ->
                    val id = Uuid.random()
                    val place =
                        PlaceEntity(
                            id = id,
                            name = "Nhà",
                            address = "Hà Nội",
                            latitude = 21.03,
                            longitude = 105.83,
                            sortOrder = 0,
                            updatedAt = occurredAt,
                            deletedAt = null,
                        )

                    db.placeDao().upsert(place)

                    db.placeDao().observeAll().first() shouldBe listOf(place)
                }
            }

            test("FTS row inserted in an entry is found by MATCH and removed on delete") {
                withTestDatabase { db ->
                    val entryId = Uuid.random()
                    db.entryDao().upsert(entryEntity(entryId))
                    val row =
                        EntryFtsEntity(
                            rowId = null,
                            entryId = entryId,
                            textSearch = "binh minh o ho",
                            labelNames = "Vui",
                            placeName = "Ho Guom",
                        )

                    db.entryDao().insertFts(row)

                    db.entryDao().searchEntryIds("minh").first() shouldBe listOf(entryId)

                    db.entryDao().deleteFtsByEntryId(entryId)

                    db.entryDao().searchEntryIds("minh").first() shouldBe emptyList()
                }
            }

            test("transaction rolls back every write when the block throws") {
                withTestDatabase { db ->
                    val id = Uuid.random()

                    shouldThrow<IllegalStateException> {
                        db.transaction {
                            db.entryDao().upsert(entryEntity(id))
                            error("boom")
                        }
                    }

                    db.entryDao().getById(id) shouldBe null
                }
            }

            test("domain entry maps to entity and back with media and labels") {
                val id = Uuid.random()
                val entry =
                    Entry(
                        id = id,
                        body = """{"blocks":[]}""",
                        textPlain = "Bình minh ở hồ",
                        occurredAt = occurredAt,
                        timeZone = TimeZone.of("Asia/Ho_Chi_Minh"),
                        location =
                            EntryLocation(
                                name = "Hồ Gươm",
                                coordinates = GeoPoint(21.0285, 105.8542),
                            ),
                        createdAt = occurredAt,
                        updatedAt = occurredAt,
                    )

                entry
                    .toEntity(textSearch = "binh minh o ho")
                    .toDomain(media = emptyList(), labels = emptyList()) shouldBe entry
            }
        },
    )
