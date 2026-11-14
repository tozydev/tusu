package vn.id.tozydev.tusu.platform

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import vn.id.tozydev.tusu.data.media.MediaJobRunner

/**
 * Runs compression jobs in the app scope. Desktop has no OS scheduler, so this queue only lives as
 * long as the process; the next start picks up what it left in `PROCESSING`.
 */
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
class CoroutineMediaJobQueue(
    private val scope: CoroutineScope,
    private val runner: MediaJobRunner,
) : MediaJobQueue {
    // A concurrent set makes the check-and-add atomic, so two threads cannot start one job twice.
    private val inFlight: MutableSet<Uuid> = ConcurrentHashMap.newKeySet()

    override fun enqueueCompression(mediaId: Uuid) {
        if (!inFlight.add(mediaId)) return
        scope.launch {
            try {
                // The runner persists failures and logs them, so the result needs no handling here.
                runner.compress(mediaId)
            } finally {
                inFlight.remove(mediaId)
            }
        }
    }
}
