package vn.id.tozydev.tusu.domain.error

sealed interface LabelError {
    data object NotFound : LabelError

    data object GroupNotFound : LabelError

    data object BlankName : LabelError

    /** Another label, or another group, already uses this name. */
    data object DuplicateName : LabelError

    /** The database could not be written. */
    data object StorageFailed : LabelError
}
