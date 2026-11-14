package vn.id.tozydev.tusu.domain.usecase

import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.first
import kotlinx.io.RawSource
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.error.bind
import vn.id.tozydev.tusu.domain.error.onFailure
import vn.id.tozydev.tusu.domain.error.outcome
import vn.id.tozydev.tusu.domain.error.raise
import vn.id.tozydev.tusu.domain.model.Media
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.domain.repository.MediaFileStore
import vn.id.tozydev.tusu.domain.repository.MediaRepository
import vn.id.tozydev.tusu.platform.MediaJobQueue

private val logger = KotlinLogging.logger {}

/** Stores a new attachment and appends it to the entry's album. */
@Inject
class AttachMediaUseCase(
    private val files: MediaFileStore,
    private val media: MediaRepository,
    private val clock: Clock,
    private val jobs: MediaJobQueue,
) {
    /**
     * Photos start as PROCESSING because a background job still compresses them; audio and video
     * are usable as they are.
     */
    suspend operator fun invoke(
        entryId: Uuid,
        source: RawSource,
        type: MediaType,
        extension: String,
        takenAt: Instant? = null,
    ): Outcome<Media, MediaError> = outcome {
        val id = Uuid.random()
        val relativePath = files.newRelativePath(id, extension)
        files.write(relativePath, source).bind()
        val position =
            (media.observeEntryMedia(entryId).first().maxOfOrNull { it.position } ?: -1) + 1
        val created =
            Media(
                id = id,
                entryId = entryId,
                type = type,
                localPath = relativePath,
                position = position,
                takenAt = takenAt,
                status = if (type == MediaType.PHOTO) MediaStatus.PROCESSING else MediaStatus.READY,
                updatedAt = clock.now(),
            )
        when (val added = media.add(created)) {
            is Outcome.Success -> {
                if (type == MediaType.PHOTO) jobs.enqueueCompression(id)
                added.value
            }
            is Outcome.Failure -> {
                // Without a row the file cannot be reached, so it must not stay behind. The add
                // error is what the caller needs; a failed cleanup only leaves a harmless orphan.
                files.delete(relativePath).onFailure {
                    logger.warn { "Could not remove the file of a failed attachment" }
                }
                raise(added.error)
            }
        }
    }
}
