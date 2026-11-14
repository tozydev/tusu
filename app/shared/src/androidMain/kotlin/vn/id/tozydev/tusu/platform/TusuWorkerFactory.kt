package vn.id.tozydev.tusu.platform

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import vn.id.tozydev.tusu.data.media.MediaJobRunner

/**
 * Builds workers that need app dependencies. WorkManager would otherwise construct them with a
 * no-argument constructor, which cannot receive the graph.
 */
class TusuWorkerFactory(private val mediaJobRunner: MediaJobRunner) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? =
        if (workerClassName == CompressImageWorker::class.java.name) {
            CompressImageWorker(appContext, workerParameters, mediaJobRunner)
        } else {
            // Null lets WorkManager fall back to its default reflection for other workers.
            null
        }
}
