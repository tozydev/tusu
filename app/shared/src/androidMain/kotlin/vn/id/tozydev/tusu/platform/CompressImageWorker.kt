package vn.id.tozydev.tusu.platform

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.uuid.Uuid
import vn.id.tozydev.tusu.data.media.MediaJobRunner
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome

private val logger = KotlinLogging.logger {}

/** Compresses one photo; the runner records the result in `media.status`. */
class CompressImageWorker(
    appContext: Context,
    params: WorkerParameters,
    private val runner: MediaJobRunner,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val mediaId = inputData.getString(KEY_MEDIA_ID)?.let { Uuid.parseOrNull(it) }
        if (mediaId == null) {
            // Only a bug can produce this; a retry would fail the same way.
            logger.error { "Compression job has no valid media id" }
            return Result.failure()
        }
        return when (val outcome = runner.compress(mediaId)) {
            is Outcome.Success -> Result.success()
            is Outcome.Failure ->
                when (outcome.error) {
                    // Storage can recover, e.g. after the user frees space, so WorkManager tries
                    // again with back-off. After MAX_ATTEMPTS the photo stays PROCESSING and the
                    // next app start queues it again.
                    MediaError.StorageFailed,
                    MediaError.InsufficientStorage ->
                        if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.success()
                    // The runner already stored FAILED, which the user sees and can retry. A
                    // broken file would fail again, so WorkManager must not loop on it.
                    else -> Result.success()
                }
        }
    }

    companion object {
        const val KEY_MEDIA_ID = "media_id"
        private const val MAX_ATTEMPTS = 3
    }
}
