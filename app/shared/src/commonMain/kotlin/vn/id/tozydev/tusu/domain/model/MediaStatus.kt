package vn.id.tozydev.tusu.domain.model

/** Progress of the background job that compresses a media item; jobs resume from this value. */
enum class MediaStatus {
    PROCESSING,
    READY,
    FAILED,
}
