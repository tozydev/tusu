package vn.id.tozydev.tusu.domain.error

sealed interface MediaError {
    data object NotFound : MediaError

    /** The source file, or the stored file of an existing media item, cannot be read. */
    data object FileUnreadable : MediaError

    data object UnsupportedFormat : MediaError

    data object InsufficientStorage : MediaError

    /** Compressing or transcoding failed; the media item is kept with status `FAILED`. */
    data object ProcessingFailed : MediaError

    data object RecordingFailed : MediaError

    data class PermissionRequired(val error: PermissionError) : MediaError

    /** The database or the media folder could not be written. */
    data object StorageFailed : MediaError
}
