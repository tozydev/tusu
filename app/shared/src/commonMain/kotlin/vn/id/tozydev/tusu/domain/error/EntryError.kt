package vn.id.tozydev.tusu.domain.error

sealed interface EntryError {
    data object NotFound : EntryError

    /** The stored rich text JSON is malformed or from a newer schema, so it cannot be edited. */
    data object UnreadableBody : EntryError

    /** The database could not be written. */
    data object StorageFailed : EntryError
}
