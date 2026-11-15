package vn.id.tozydev.tusu.ui.feature.composer

import kotlin.uuid.Uuid
import vn.id.tozydev.tusu.platform.MediaJobQueue

/** Remembers every enqueued compression instead of running it. */
class FakeMediaJobQueue : MediaJobQueue {
    val enqueued = mutableListOf<Uuid>()

    override fun enqueueCompression(mediaId: Uuid) {
        enqueued += mediaId
    }
}
