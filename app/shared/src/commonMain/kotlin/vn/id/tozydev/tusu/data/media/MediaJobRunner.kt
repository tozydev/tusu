package vn.id.tozydev.tusu.data.media

import androidx.sqlite.SQLiteException
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.time.Clock
import kotlin.uuid.Uuid
import vn.id.tozydev.tusu.data.db.TusuDatabase
import vn.id.tozydev.tusu.data.db.media.MediaDao
import vn.id.tozydev.tusu.data.db.media.MediaEntity
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.error.Raise
import vn.id.tozydev.tusu.domain.error.ensureNotNull
import vn.id.tozydev.tusu.domain.error.onFailure
import vn.id.tozydev.tusu.domain.error.outcome
import vn.id.tozydev.tusu.domain.error.raise
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.domain.repository.MediaFileStore
import vn.id.tozydev.tusu.platform.ImageCompressor
import vn.id.tozydev.tusu.platform.MediaJobQueue

private val logger = KotlinLogging.logger {}

private const val COMPRESSED_EXTENSION = "jpg"

/**
 * Body of the photo compression job, shared by every platform queue. The job is written so it can
 * run again at any time: the database state decides what is left to do.
 */
@Inject
class MediaJobRunner(
    private val database: TusuDatabase,
    private val compressor: ImageCompressor,
    private val files: MediaFileStore,
    private val clock: Clock,
) {
    private val dao: MediaDao
        get() = database.mediaDao()

    suspend fun compress(mediaId: Uuid): Outcome<Unit, MediaError> = outcome {
        val live =
            ensureNotNull(
                storage { dao.getById(mediaId) }?.takeIf { it.deletedAt == null },
            ) {
                MediaError.NotFound
            }
        // A duplicate job or a job for an audio or video item has nothing to do.
        if (live.type == MediaType.PHOTO && live.status != MediaStatus.READY) {
            compressLive(live)
        }
    }

    /**
     * Lists the live photos still PROCESSING and hands each to [queue]. It runs at app start, so
     * jobs that were killed with the process are picked up again.
     */
    suspend fun resumePending(queue: MediaJobQueue): Outcome<Unit, MediaError> = outcome {
        val pending = storage { dao.getLivePhotosByStatus(MediaStatus.PROCESSING) }
        pending.forEach { queue.enqueueCompression(it.id) }
    }

    context(raise: Raise<MediaError>)
    private suspend fun compressLive(live: MediaEntity) {
        val id = live.id
        // A FAILED photo is retried from here, so it is set back to PROCESSING before the work.
        if (live.status != MediaStatus.PROCESSING) {
            storage { dao.updateStatus(id, MediaStatus.PROCESSING, clock.now()) }
        }
        // The compressor always writes a JPEG, so the result is stored under a .jpg name; a
        // .png or .heic name would lie about the bytes. The original stays until the row points
        // at the result, so a failure or a killed job can start again from it.
        val compressedPath = files.newRelativePath(id, COMPRESSED_EXTENSION)
        val result =
            compressor.compress(files.resolve(live.localPath), files.resolve(compressedPath))
        when (result) {
            is Outcome.Success -> {
                storage {
                    dao.markCompressed(
                        id = id,
                        localPath = compressedPath,
                        width = result.value.width,
                        height = result.value.height,
                        updatedAt = clock.now(),
                    )
                }
                if (compressedPath != live.localPath) {
                    // The photo is ready either way; a leftover original is only disk use.
                    files.delete(live.localPath).onFailure {
                        logger.warn { "Could not remove the original of media $id: $it" }
                    }
                }
            }
            is Outcome.Failure -> {
                logger.warn { "Compressing media $id failed: ${result.error}" }
                storage { dao.updateStatus(id, MediaStatus.FAILED, clock.now()) }
                raise(result.error)
            }
        }
        // Cancellation is not caught here on purpose: the row stays PROCESSING, so the next start
        // queues the job again instead of marking a photo failed that was never tried.
    }
}

/**
 * Maps SQLite failures to [MediaError.StorageFailed]; other exceptions, including cancellation,
 * pass through.
 */
context(raise: Raise<MediaError>)
private suspend fun <T> storage(block: suspend () -> T): T =
    try {
        block()
    } catch (e: SQLiteException) {
        logger.warn(e) { "Could not access media rows" }
        raise.raise(MediaError.StorageFailed)
    }
