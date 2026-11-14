package vn.id.tozydev.tusu.data.db.label

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import vn.id.tozydev.tusu.data.db.TusuDatabase
import vn.id.tozydev.tusu.data.db.TusuDatabaseConstructor
import vn.id.tozydev.tusu.data.repository.LabelRepositoryImpl
import vn.id.tozydev.tusu.domain.model.LabelSelection

private object FixedClock : Clock {
    override fun now(): Instant = Instant.parse("2026-11-14T01:00:00Z")
}

private val names =
    DefaultLabelNames(
        kindGroup = "Loại",
        peopleGroup = "Người",
        tripsGroup = "Chuyến đi",
        achievement = "Thành tựu",
        lesson = "Bài học",
        reflection = "Reflection",
        memory = "Kỷ niệm",
        gratitude = "Biết ơn",
    )

private fun seeded(builder: androidx.room3.RoomDatabase.Builder<TusuDatabase>): TusuDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .addCallback(DefaultLabelSeedCallback(FixedClock) { names })
        .build()

class DefaultLabelSeedCallbackTest :
    FunSpec(
        {
            test("a fresh database holds the three default groups and five default labels") {
                val db =
                    seeded(
                        Room.inMemoryDatabaseBuilder<TusuDatabase>(
                            factory = TusuDatabaseConstructor::initialize,
                        ),
                    )
                try {
                    val groups = db.labelDao().observeGroups().first()
                    groups.map { it.name } shouldContainExactly listOf("Loại", "Người", "Chuyến đi")
                    groups.map { it.selection } shouldContainExactly
                        listOf(LabelSelection.MULTI, LabelSelection.MULTI, LabelSelection.SINGLE)
                    groups.map { it.sortOrder } shouldContainExactly listOf(0, 1, 2)
                    groups.all {
                        it.isDefault && it.showInComposer && it.deletedAt == null
                    } shouldBe true
                    groups.map { it.color }.distinct().size shouldBe 3

                    val labels = db.labelDao().observeLabels().first()
                    labels.map { it.name } shouldContainExactly
                        listOf("Thành tựu", "Bài học", "Reflection", "Kỷ niệm", "Biết ơn")
                    labels.map { it.sortOrder } shouldContainExactly listOf(0, 1, 2, 3, 4)
                    val kindId = groups.first().id
                    labels.all {
                        it.groupId == kindId && it.isDefault && !it.isHidden && it.deletedAt == null
                    } shouldBe true
                } finally {
                    db.close()
                }
            }

            test("reopening does not seed again, and deleting every group does not re-seed") {
                val dir = Files.createTempDirectory("tusu-label-seed")
                val path = dir.resolve("tusu.db").toString()
                val factory = TusuDatabaseConstructor::initialize
                try {
                    seeded(Room.databaseBuilder<TusuDatabase>(name = path, factory = factory)).let {
                        try {
                            it.labelDao().observeGroups().first().size shouldBe 3
                        } finally {
                            it.close()
                        }
                    }

                    val reopened =
                        seeded(Room.databaseBuilder<TusuDatabase>(name = path, factory = factory))
                    try {
                        reopened.labelDao().observeGroups().first().size shouldBe 3
                        reopened.labelDao().observeLabels().first().size shouldBe 5

                        val repo = LabelRepositoryImpl(reopened, FixedClock)
                        reopened.labelDao().observeGroups().first().forEach {
                            repo.deleteGroup(it.id)
                        }
                    } finally {
                        reopened.close()
                    }

                    val afterDelete =
                        seeded(Room.databaseBuilder<TusuDatabase>(name = path, factory = factory))
                    try {
                        afterDelete.labelDao().observeGroups().first().shouldBeEmpty()
                        afterDelete.labelDao().observeLabels().first().size shouldBe 5
                    } finally {
                        afterDelete.close()
                    }
                } finally {
                    dir.toFile().deleteRecursively()
                }
            }
        },
    )
