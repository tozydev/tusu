package vn.id.tozydev.tusu.data.media

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.time.Instant
import kotlin.uuid.Uuid
import vn.id.tozydev.tusu.data.db.TusuDatabase
import vn.id.tozydev.tusu.data.db.media.toDomain
import vn.id.tozydev.tusu.data.db.withTestDatabase
import vn.id.tozydev.tusu.data.repository.FakeMediaFileStore
import vn.id.tozydev.tusu.data.repository.TestClock
import vn.id.tozydev.tusu.data.repository.entryRow
import vn.id.tozydev.tusu.data.repository.instantAt
import vn.id.tozydev.tusu.data.repository.mediaRow
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.Media
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.platform.RecordingMediaJobQueue
import vn.id.tozydev.tusu.platform.ScriptedImageCompressor

private suspend fun mediaOf(db: TusuDatabase, id: Uuid): Media =
    db.mediaDao().getById(id).shouldNotBeNull().toDomain()

/** Inserts an entry with one media row and returns the row's id. */
private suspend fun seed(
    db: TusuDatabase,
    type: MediaType = MediaType.PHOTO,
    status: MediaStatus = MediaStatus.PROCESSING,
    deletedAt: Instant? = null,
): Uuid {
    val entryId = Uuid.random()
    val id = Uuid.random()
    db.entryDao().upsert(entryRow(entryId))
    db.mediaDao()
        .upsert(
            mediaRow(
                id = id,
                entryId = entryId,
                type = type,
                status = status,
                updatedAt = instantAt(1),
                deletedAt = deletedAt,
            ),
        )
    return id
}

class MediaJobRunnerTest :
    FunSpec(
        {
            test("a processing photo is compressed and becomes READY with its size") {
                withTestDatabase { db ->
                    val id = seed(db)
                    val compressor = ScriptedImageCompressor()
                    val runner =
                        MediaJobRunner(
                            db,
                            compressor,
                            FakeMediaFileStore(),
                            TestClock(instantAt(9)),
                        )

                    runner.compress(id) shouldBe Outcome.Success(Unit)

                    compressor.calls.size shouldBe 1
                    val row = mediaOf(db, id)
                    row.status shouldBe MediaStatus.READY
                    row.width shouldBe 1600
                    row.height shouldBe 1200
                    row.updatedAt shouldBe instantAt(9)
                }
            }

            test("a compressor failure marks the photo FAILED and returns its error") {
                withTestDatabase { db ->
                    val id = seed(db)
                    val compressor =
                        ScriptedImageCompressor(Outcome.Failure(MediaError.ProcessingFailed))
                    val runner =
                        MediaJobRunner(
                            db,
                            compressor,
                            FakeMediaFileStore(),
                            TestClock(instantAt(9)),
                        )

                    runner.compress(id) shouldBe Outcome.Failure(MediaError.ProcessingFailed)

                    val row = mediaOf(db, id)
                    row.status shouldBe MediaStatus.FAILED
                    row.width shouldBe null
                    row.updatedAt shouldBe instantAt(9)
                }
            }

            test("a FAILED photo is compressed again and becomes READY") {
                withTestDatabase { db ->
                    val id = seed(db, status = MediaStatus.FAILED)
                    val compressor = ScriptedImageCompressor()
                    val runner =
                        MediaJobRunner(
                            db,
                            compressor,
                            FakeMediaFileStore(),
                            TestClock(instantAt(9)),
                        )

                    runner.compress(id) shouldBe Outcome.Success(Unit)

                    compressor.calls.size shouldBe 1
                    mediaOf(db, id).status shouldBe MediaStatus.READY
                }
            }

            test("a READY photo is left alone without compressing it again") {
                withTestDatabase { db ->
                    val id = seed(db, status = MediaStatus.READY)
                    val compressor = ScriptedImageCompressor()
                    val runner =
                        MediaJobRunner(
                            db,
                            compressor,
                            FakeMediaFileStore(),
                            TestClock(instantAt(9)),
                        )

                    runner.compress(id) shouldBe Outcome.Success(Unit)

                    compressor.calls.shouldBeEmpty()
                    mediaOf(db, id).updatedAt shouldBe instantAt(1)
                }
            }

            test("audio has no compression job, so it succeeds without work") {
                withTestDatabase { db ->
                    val id = seed(db, type = MediaType.AUDIO)
                    val compressor = ScriptedImageCompressor()
                    val runner =
                        MediaJobRunner(
                            db,
                            compressor,
                            FakeMediaFileStore(),
                            TestClock(instantAt(9)),
                        )

                    runner.compress(id) shouldBe Outcome.Success(Unit)

                    compressor.calls.shouldBeEmpty()
                    mediaOf(db, id).status shouldBe MediaStatus.PROCESSING
                }
            }

            test("a missing or trashed photo fails with NotFound and writes nothing") {
                withTestDatabase { db ->
                    val trashed = seed(db, deletedAt = instantAt(2))
                    val compressor = ScriptedImageCompressor()
                    val runner =
                        MediaJobRunner(
                            db,
                            compressor,
                            FakeMediaFileStore(),
                            TestClock(instantAt(9)),
                        )

                    runner.compress(Uuid.random()) shouldBe Outcome.Failure(MediaError.NotFound)
                    runner.compress(trashed) shouldBe Outcome.Failure(MediaError.NotFound)

                    compressor.calls.shouldBeEmpty()
                    mediaOf(db, trashed).status shouldBe MediaStatus.PROCESSING
                }
            }

            test("resumePending enqueues only live photos that are still PROCESSING") {
                withTestDatabase { db ->
                    val live = seed(db)
                    val otherLive = seed(db)
                    seed(db, deletedAt = instantAt(2))
                    seed(db, type = MediaType.AUDIO)
                    seed(db, status = MediaStatus.READY)
                    val runner =
                        MediaJobRunner(
                            db,
                            ScriptedImageCompressor(),
                            FakeMediaFileStore(),
                            TestClock(instantAt(9)),
                        )
                    val queue = RecordingMediaJobQueue()

                    runner.resumePending(queue) shouldBe Outcome.Success(Unit)

                    queue.enqueued shouldContainExactlyInAnyOrder listOf(live, otherLive)
                }
            }
        },
    )
