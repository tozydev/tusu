package vn.id.tozydev.tusu.platform

import kotlin.uuid.Uuid

/** Remembers every enqueued id instead of running anything. */
internal class RecordingMediaJobQueue : MediaJobQueue {
    val enqueued = mutableListOf<Uuid>()

    override fun enqueueCompression(mediaId: Uuid) {
        enqueued += mediaId
    }
}
