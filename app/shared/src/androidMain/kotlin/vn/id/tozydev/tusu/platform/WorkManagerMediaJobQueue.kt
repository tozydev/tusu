package vn.id.tozydev.tusu.platform

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.uuid.Uuid

/**
 * Schedules [CompressImageWorker] through WorkManager, so compression survives the app being
 * closed or killed. One unique job per photo: KEEP drops a request while that photo's job is still
 * pending, which makes a repeated enqueue harmless.
 */
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
class WorkManagerMediaJobQueue(private val context: Context) : MediaJobQueue {
    override fun enqueueCompression(mediaId: Uuid) {
        val request =
            OneTimeWorkRequestBuilder<CompressImageWorker>()
                .setInputData(workDataOf(CompressImageWorker.KEY_MEDIA_ID to mediaId.toString()))
                .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(uniqueWorkName(mediaId), ExistingWorkPolicy.KEEP, request)
    }

    companion object {
        fun uniqueWorkName(mediaId: Uuid): String = "compress-$mediaId"
    }
}
