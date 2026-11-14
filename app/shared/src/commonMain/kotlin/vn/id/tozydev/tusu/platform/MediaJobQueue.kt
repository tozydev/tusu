package vn.id.tozydev.tusu.platform

import kotlin.uuid.Uuid

/** Runs media compression in the background; the job state lives in `media.status`, not here. */
interface MediaJobQueue {
    /** Schedules compression of the photo; enqueueing the same id twice keeps one job. */
    fun enqueueCompression(mediaId: Uuid)
}
