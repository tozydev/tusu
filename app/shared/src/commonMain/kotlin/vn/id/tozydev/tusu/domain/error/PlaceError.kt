package vn.id.tozydev.tusu.domain.error

sealed interface PlaceError {
    data object NotFound : PlaceError

    data object BlankName : PlaceError

    /** The database could not be written. */
    data object StorageFailed : PlaceError
}
