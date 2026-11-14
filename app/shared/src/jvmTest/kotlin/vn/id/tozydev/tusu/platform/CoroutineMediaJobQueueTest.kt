package vn.id.tozydev.tusu.platform

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import vn.id.tozydev.tusu.data.db.TusuDatabase
import vn.id.tozydev.tusu.data.db.media.toDomain
import vn.id.tozydev.tusu.data.db.withTestDatabase
import vn.id.tozydev.tusu.data.media.MediaJobRunner
import vn.id.tozydev.tusu.data.repository.FakeMediaFileStore
import vn.id.tozydev.tusu.data.repository.TestClock
import vn.id.tozydev.tusu.data.repository.entryRow
import vn.id.tozydev.tusu.data.repository.instantAt
import vn.id.tozydev.tusu.data.repository.mediaRow
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType

private suspend fun seedProcessingPhoto(db: TusuDatabase): Uuid {
    val entryId = Uuid.random()
    val id = Uuid.random()
    db.entryDao().upsert(entryRow(entryId))
    db.mediaDao()
        .upsert(
            mediaRow(
                id = id,
                entryId = entryId,
                type = MediaType.PHOTO,
                status = MediaStatus.PROCESSING,
                updatedAt = instantAt(1),
            ),
        )
    return id
}

/** The queue runs jobs on real threads, so the tests poll the database until a job finishes. */
private suspend fun awaitStatus(db: TusuDatabase, id: Uuid, status: MediaStatus) {
    withTimeout(5.seconds) {
        while (db.mediaDao().getById(id)?.toDomain()?.status != status) {
            delay(10.milliseconds)
        }
    }
}

class CoroutineMediaJobQueueTest :
    FunSpec(
        {
            test("an enqueued photo is compressed in the background") {
                withTestDatabase { db ->
                    val id = seedProcessingPhoto(db)
                    val runner =
                        MediaJobRunner(
                            db,
                            ScriptedImageCompressor(),
                            FakeMediaFileStore(),
                            TestClock(instantAt(9)),
                        )
                    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
                    try {
                        CoroutineMediaJobQueue(scope, runner).enqueueCompression(id)

                        awaitStatus(db, id, MediaStatus.READY)
                    } finally {
                        scope.cancel()
                    }
                }
            }

            test("a duplicate enqueue while the job is in flight runs it only once") {
                withTestDatabase { db ->
                    val id = seedProcessingPhoto(db)
                    val compressor = ScriptedImageCompressor()
                    val gate = CompletableDeferred<Unit>()
                    compressor.gate = gate
                    val runner =
                        MediaJobRunner(
                            db,
                            compressor,
                            FakeMediaFileStore(),
                            TestClock(instantAt(9)),
                        )
                    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
                    try {
                        val queue = CoroutineMediaJobQueue(scope, runner)
                        queue.enqueueCompression(id)
                        withTimeout(5.seconds) {
                            while (compressor.calls.isEmpty()) delay(10.milliseconds)
                        }

                        queue.enqueueCompression(id)
                        delay(100.milliseconds)
                        compressor.calls.size shouldBe 1

                        gate.complete(Unit)
                        awaitStatus(db, id, MediaStatus.READY)
                        compressor.calls.size shouldBe 1
                    } finally {
                        gate.complete(Unit)
                        scope.cancel()
                    }
                }
            }

            test("the same id can be enqueued again once its job has finished") {
                withTestDatabase { db ->
                    val id = seedProcessingPhoto(db)
                    val compressor =
                        ScriptedImageCompressor(Outcome.Failure(MediaError.ProcessingFailed))
                    val runner =
                        MediaJobRunner(
                            db,
                            compressor,
                            FakeMediaFileStore(),
                            TestClock(instantAt(9)),
                        )
                    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
                    try {
                        val queue = CoroutineMediaJobQueue(scope, runner)
                        queue.enqueueCompression(id)
                        awaitStatus(db, id, MediaStatus.FAILED)

                        compressor.result = Outcome.Success(ImageSize(10, 10))
                        queue.enqueueCompression(id)

                        awaitStatus(db, id, MediaStatus.READY)
                        compressor.calls.size shouldBe 2
                    } finally {
                        scope.cancel()
                    }
                }
            }
        },
    )
