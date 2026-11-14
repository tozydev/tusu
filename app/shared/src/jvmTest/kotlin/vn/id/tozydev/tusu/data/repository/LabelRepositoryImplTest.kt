package vn.id.tozydev.tusu.data.repository

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate
import vn.id.tozydev.tusu.data.db.TusuDatabase
import vn.id.tozydev.tusu.data.db.TusuDatabaseConstructor
import vn.id.tozydev.tusu.data.db.entry.EntryEntity
import vn.id.tozydev.tusu.data.db.label.EntryLabelEntity
import vn.id.tozydev.tusu.domain.error.LabelError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.Label
import vn.id.tozydev.tusu.domain.model.LabelGroup
import vn.id.tozydev.tusu.domain.model.LabelSelection

private val start: Instant = Instant.parse("2026-11-14T01:00:00Z")
private const val COLOR = 0x336699

private class TestClock(var instant: Instant) : Clock {
    override fun now(): Instant = instant
}

private class Fixture(val repo: LabelRepositoryImpl, val db: TusuDatabase, val clock: TestClock)

private suspend fun withFixture(block: suspend (Fixture) -> Unit) {
    val clock = TestClock(start)
    val db =
        Room.inMemoryDatabaseBuilder<TusuDatabase>(factory = TusuDatabaseConstructor::initialize)
            .setDriver(BundledSQLiteDriver())
            .build()
    try {
        block(Fixture(LabelRepositoryImpl(db, clock), db, clock))
    } finally {
        db.close()
    }
}

private fun <T> Outcome<T, LabelError>.value(): T =
    (this as? Outcome.Success)?.value ?: error("expected success, got $this")

private suspend fun Fixture.group(name: String): LabelGroup =
    repo.createGroup(name, COLOR, LabelSelection.MULTI).value()

private suspend fun Fixture.label(name: String, groupId: Uuid?): Label =
    repo.createLabel(name, groupId).value()

private suspend fun Fixture.insertEntry(id: Uuid) {
    db.entryDao()
        .upsert(
            EntryEntity(
                id = id,
                body = """{"blocks":[]}""",
                textPlain = "Ngày đẹp",
                textSearch = "ngay dep",
                emoji = null,
                occurredAt = start,
                timezone = "Asia/Ho_Chi_Minh",
                localDate = LocalDate(2026, 11, 14),
                placeName = "Hồ Gươm",
                placeAddress = null,
                latitude = 21.0285,
                longitude = 105.8542,
                mapSnapshotPath = null,
                createdAt = start,
                updatedAt = start,
                deletedAt = null,
            ),
        )
}

private suspend fun Fixture.attach(entryId: Uuid, labelId: Uuid) {
    db.labelDao()
        .upsertEntryLabel(
            EntryLabelEntity(
                entryId = entryId,
                labelId = labelId,
                updatedAt = start,
                deletedAt = null,
            ),
        )
}

/** Entry-label rows of [entryId] that are not soft-deleted. */
private suspend fun Fixture.liveEntryLabels(entryId: Uuid): List<EntryLabelEntity> =
    db.labelDao().observeEntryLabels(entryId).first().filter { it.deletedAt == null }

private suspend fun Fixture.groupNames(): List<String> =
    repo.observeGroups().first().map { it.name }

private suspend fun Fixture.labelNames(): List<String> =
    repo.observeLabels().first().map { it.name }

/** Names of the live labels of [groupId] (null is "Khác"), in display order. */
private suspend fun Fixture.labelsIn(groupId: Uuid?): List<String> =
    repo.observeLabels().first().filter { it.groupId == groupId }.map { it.name }

class LabelRepositoryImplTest :
    FunSpec(
        {
            context("createGroup") {
                test(
                    "appends after the existing groups and starts non-default and composer-visible",
                ) {
                    withFixture { f ->
                        val first = f.group("Loại")
                        val second = f.group("Người")

                        first.sortOrder shouldBe 0
                        second.sortOrder shouldBe 1
                        second.isDefault shouldBe false
                        second.showInComposer shouldBe true
                        second.updatedAt shouldBe start
                        f.groupNames() shouldContainExactly listOf("Loại", "Người")
                    }
                }

                test("trims the name") {
                    withFixture { f -> f.group("  Chuyến đi  ").name shouldBe "Chuyến đi" }
                }

                test("rejects a blank name") {
                    withFixture { f ->
                        f.repo.createGroup("   ", COLOR, LabelSelection.MULTI) shouldBe
                            Outcome.Failure(LabelError.BlankName)
                    }
                }

                test("rejects a duplicate ignoring case and diacritics") {
                    withFixture { f ->
                        f.group("Loại")

                        f.repo.createGroup("LOAI", COLOR, LabelSelection.SINGLE) shouldBe
                            Outcome.Failure(LabelError.DuplicateName)
                    }
                }

                test("a deleted group does not block its name") {
                    withFixture { f ->
                        val old = f.group("Cũ")
                        f.repo.deleteGroup(old.id)

                        f.group("cũ").name shouldBe "cũ"
                    }
                }
            }

            context("updateGroup") {
                test("renaming a group to its own name is not a duplicate") {
                    withFixture { f ->
                        val group = f.group("Loại")

                        f.repo.updateGroup(group.copy(name = "loai")) shouldBe Outcome.Success(Unit)
                    }
                }

                test("rejects a name used by another group") {
                    withFixture { f ->
                        f.group("Loại")
                        val people = f.group("Người")

                        f.repo.updateGroup(people.copy(name = "Loai")) shouldBe
                            Outcome.Failure(LabelError.DuplicateName)
                    }
                }

                test("persists editable fields and keeps isDefault and deletedAt from the store") {
                    withFixture { f ->
                        val group = f.group("Loại")
                        f.clock.instant = start + 5.minutes

                        f.repo.updateGroup(
                            group.copy(
                                name = "Thể loại",
                                color = 0x00AA00,
                                selection = LabelSelection.SINGLE,
                                showInComposer = false,
                                sortOrder = 7,
                                isDefault = true,
                            ),
                        ) shouldBe Outcome.Success(Unit)

                        val stored = f.repo.observeGroups().first().single()
                        stored.name shouldBe "Thể loại"
                        stored.color shouldBe 0x00AA00
                        stored.selection shouldBe LabelSelection.SINGLE
                        stored.showInComposer shouldBe false
                        stored.sortOrder shouldBe 7
                        stored.isDefault shouldBe false
                        stored.updatedAt shouldBe start + 5.minutes
                    }
                }

                test("returns GroupNotFound for a deleted or unknown group") {
                    withFixture { f ->
                        val group = f.group("Loại")
                        f.repo.deleteGroup(group.id)

                        f.repo.updateGroup(group) shouldBe Outcome.Failure(LabelError.GroupNotFound)
                        f.repo.updateGroup(group.copy(id = Uuid.random())) shouldBe
                            Outcome.Failure(LabelError.GroupNotFound)
                    }
                }

                test("switching MULTI to SINGLE leaves existing entry labels untouched") {
                    withFixture { f ->
                        val group = f.group("Loại")
                        val label = f.label("Vui", group.id)
                        val entryId = Uuid.random()
                        f.insertEntry(entryId)
                        f.attach(entryId, label.id)

                        f.repo.updateGroup(group.copy(selection = LabelSelection.SINGLE))

                        f.liveEntryLabels(entryId).map { it.labelId } shouldContainExactly
                            listOf(label.id)
                    }
                }
            }

            context("createLabel") {
                test("appends within its group and within Khác separately") {
                    withFixture { f ->
                        val group = f.group("Loại")
                        val a = f.label("A", group.id)
                        val b = f.label("B", group.id)
                        val other = f.label("C", null)

                        a.sortOrder shouldBe 0
                        b.sortOrder shouldBe 1
                        other.sortOrder shouldBe 0
                        other.groupId shouldBe null
                        other.isDefault shouldBe false
                    }
                }

                test("rejects a blank name") {
                    withFixture { f ->
                        f.repo.createLabel(" ", null) shouldBe Outcome.Failure(LabelError.BlankName)
                    }
                }

                test(
                    "rejects a name used in another group or in Khác, ignoring case and diacritics",
                ) {
                    withFixture { f ->
                        val group = f.group("Loại")
                        f.label("Bài học", group.id)
                        val people = f.group("Người")

                        f.repo.createLabel("bai hoc", people.id) shouldBe
                            Outcome.Failure(LabelError.DuplicateName)
                        f.repo.createLabel("BÀI HỌC", null) shouldBe
                            Outcome.Failure(LabelError.DuplicateName)
                    }
                }

                test("rejects an unknown group") {
                    withFixture { f ->
                        f.repo.createLabel("Vui", Uuid.random()) shouldBe
                            Outcome.Failure(LabelError.GroupNotFound)
                    }
                }

                test("rejects a deleted group") {
                    withFixture { f ->
                        val group = f.group("Loại")
                        f.repo.deleteGroup(group.id)

                        f.repo.createLabel("Vui", group.id) shouldBe
                            Outcome.Failure(LabelError.GroupNotFound)
                    }
                }
            }

            context("updateLabel") {
                test("renaming to its own name is not a duplicate, another label's name is") {
                    withFixture { f ->
                        val a = f.label("Vui", null)
                        f.label("Buồn", null)

                        f.repo.updateLabel(a.copy(name = "vui")) shouldBe Outcome.Success(Unit)
                        f.repo.updateLabel(a.copy(name = "buon")) shouldBe
                            Outcome.Failure(LabelError.DuplicateName)
                    }
                }

                test("moving to another group appends at the end of that group") {
                    withFixture { f ->
                        val loai = f.group("Loại")
                        val nguoi = f.group("Người")
                        f.label("Mẹ", nguoi.id)
                        val vui = f.label("Vui", loai.id)

                        f.repo.updateLabel(vui.copy(groupId = nguoi.id, sortOrder = 0)) shouldBe
                            Outcome.Success(Unit)

                        val moved = f.repo.observeLabels().first().single { it.id == vui.id }
                        moved.groupId shouldBe nguoi.id
                        moved.sortOrder shouldBe 1
                    }
                }

                test("moving to Khác appends after the existing Khác labels") {
                    withFixture { f ->
                        val loai = f.group("Loại")
                        f.label("Có sẵn", null)
                        val vui = f.label("Vui", loai.id)

                        f.repo.updateLabel(vui.copy(groupId = null)) shouldBe Outcome.Success(Unit)

                        val moved = f.repo.observeLabels().first().single { it.id == vui.id }
                        moved.groupId shouldBe null
                        moved.sortOrder shouldBe 1
                    }
                }

                test("a missing target group is GroupNotFound") {
                    withFixture { f ->
                        val vui = f.label("Vui", null)

                        f.repo.updateLabel(vui.copy(groupId = Uuid.random())) shouldBe
                            Outcome.Failure(LabelError.GroupNotFound)
                    }
                }

                test("persists editable fields and keeps isDefault and deletedAt") {
                    withFixture { f ->
                        val vui = f.label("Vui", null)
                        f.clock.instant = start + 3.minutes

                        f.repo.updateLabel(
                            vui.copy(
                                name = "Hạnh phúc",
                                isHidden = true,
                                sortOrder = 4,
                                isDefault = true,
                            ),
                        ) shouldBe Outcome.Success(Unit)

                        val stored = f.repo.observeLabels().first().single()
                        stored.name shouldBe "Hạnh phúc"
                        stored.isHidden shouldBe true
                        stored.sortOrder shouldBe 4
                        stored.isDefault shouldBe false
                        stored.updatedAt shouldBe start + 3.minutes
                    }
                }

                test("returns NotFound for a deleted or unknown label") {
                    withFixture { f ->
                        val vui = f.label("Vui", null)
                        f.repo.deleteLabel(vui.id)

                        f.repo.updateLabel(vui) shouldBe Outcome.Failure(LabelError.NotFound)
                    }
                }

                test("hidden labels stay in observeLabels") {
                    withFixture { f ->
                        val vui = f.label("Vui", null)
                        f.repo.updateLabel(vui.copy(isHidden = true))

                        f.labelNames() shouldContainExactly listOf("Vui")
                    }
                }
            }

            context("deleteGroup") {
                test("moves its labels to Khác after the existing Khác labels, in order") {
                    withFixture { f ->
                        f.label("Có sẵn", null)
                        val loai = f.group("Loại")
                        f.label("B", loai.id)
                        f.label("A", loai.id)

                        f.repo.deleteGroup(loai.id) shouldBe Outcome.Success(Unit)

                        f.groupNames().shouldBeEmpty()
                        f.labelNames() shouldContainExactly listOf("Có sẵn", "B", "A")
                        f.repo.observeLabels().first().forEach { label ->
                            label.groupId shouldBe null
                        }
                    }
                }

                test("keeps entry_labels rows live") {
                    withFixture { f ->
                        val loai = f.group("Loại")
                        val label = f.label("Vui", loai.id)
                        val entryId = Uuid.random()
                        f.insertEntry(entryId)
                        f.attach(entryId, label.id)

                        f.repo.deleteGroup(loai.id)

                        f.liveEntryLabels(entryId).map { it.labelId } shouldContainExactly
                            listOf(label.id)
                    }
                }

                test("returns GroupNotFound for an unknown group") {
                    withFixture { f ->
                        f.repo.deleteGroup(Uuid.random()) shouldBe
                            Outcome.Failure(LabelError.GroupNotFound)
                    }
                }
            }

            context("deleteLabel") {
                test("soft-deletes the label and its live entry_labels rows") {
                    withFixture { f ->
                        val vui = f.label("Vui", null)
                        val buon = f.label("Buồn", null)
                        val entryId = Uuid.random()
                        f.insertEntry(entryId)
                        f.attach(entryId, vui.id)
                        f.attach(entryId, buon.id)
                        f.clock.instant = start + 9.minutes

                        f.repo.deleteLabel(vui.id) shouldBe Outcome.Success(Unit)

                        f.labelNames() shouldContainExactly listOf("Buồn")
                        f.liveEntryLabels(entryId).map { it.labelId } shouldContainExactly
                            listOf(buon.id)
                        val removed =
                            f.db.labelDao().observeEntryLabels(entryId).first().single {
                                it.labelId == vui.id
                            }
                        removed.deletedAt shouldBe start + 9.minutes
                        removed.updatedAt shouldBe start + 9.minutes
                    }
                }

                test("returns NotFound for an unknown label") {
                    withFixture { f ->
                        f.repo.deleteLabel(Uuid.random()) shouldBe
                            Outcome.Failure(LabelError.NotFound)
                    }
                }
            }

            context("reorder") {
                test("reorderGroups assigns sort_order in the given order") {
                    withFixture { f ->
                        val a = f.group("A")
                        val b = f.group("B")
                        val c = f.group("C")

                        f.repo.reorderGroups(listOf(c.id, a.id, b.id)) shouldBe
                            Outcome.Success(Unit)

                        f.groupNames() shouldContainExactly listOf("C", "A", "B")
                    }
                }

                test("reorderGroups with a wrong id set writes nothing") {
                    withFixture { f ->
                        val a = f.group("A")
                        val b = f.group("B")

                        f.repo.reorderGroups(listOf(b.id)) shouldBe
                            Outcome.Failure(LabelError.GroupNotFound)
                        f.repo.reorderGroups(listOf(b.id, b.id)) shouldBe
                            Outcome.Failure(LabelError.GroupNotFound)
                        f.repo.reorderGroups(listOf(b.id, a.id, Uuid.random())) shouldBe
                            Outcome.Failure(LabelError.GroupNotFound)

                        f.groupNames() shouldContainExactly listOf("A", "B")
                    }
                }

                test("reorderLabels within a group and within Khác") {
                    withFixture { f ->
                        val group = f.group("Loại")
                        val a = f.label("A", group.id)
                        val b = f.label("B", group.id)
                        val x = f.label("X", null)
                        val y = f.label("Y", null)

                        f.repo.reorderLabels(group.id, listOf(b.id, a.id)) shouldBe
                            Outcome.Success(Unit)
                        f.repo.reorderLabels(null, listOf(y.id, x.id)) shouldBe
                            Outcome.Success(Unit)

                        f.labelsIn(group.id) shouldContainExactly listOf("B", "A")
                        f.labelsIn(null) shouldContainExactly listOf("Y", "X")
                    }
                }

                test("reorderLabels with a wrong id set is NotFound and writes nothing") {
                    withFixture { f ->
                        val group = f.group("Loại")
                        val a = f.label("A", group.id)
                        val b = f.label("B", group.id)
                        val other = f.label("O", null)

                        f.repo.reorderLabels(group.id, listOf(b.id)) shouldBe
                            Outcome.Failure(LabelError.NotFound)
                        f.repo.reorderLabels(group.id, listOf(b.id, a.id, other.id)) shouldBe
                            Outcome.Failure(LabelError.NotFound)

                        f.labelsIn(group.id) shouldContainExactly listOf("A", "B")
                    }
                }

                test("reorderLabels with an unknown group is GroupNotFound") {
                    withFixture { f ->
                        f.repo.reorderLabels(Uuid.random(), emptyList()) shouldBe
                            Outcome.Failure(LabelError.GroupNotFound)
                    }
                }
            }

            context("observe") {
                test("flows re-emit after a write") {
                    withFixture { f ->
                        f.repo.observeGroups().first().shouldBeEmpty()

                        f.group("Loại")

                        withTimeout(5_000) {
                            f.repo.observeGroups().first { groups ->
                                groups.map { it.name } == listOf("Loại")
                            }
                        }
                    }
                }
            }
        },
    )
