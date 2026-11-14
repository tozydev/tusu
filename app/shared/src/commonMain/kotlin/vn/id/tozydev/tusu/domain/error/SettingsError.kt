package vn.id.tozydev.tusu.domain.error

sealed interface SettingsError {
    /** The preferences file could not be written. */
    data object StorageFailed : SettingsError
}
