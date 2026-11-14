package vn.id.tozydev.tusu.domain.error

sealed interface LocationError {
    data class PermissionRequired(val error: PermissionError) : LocationError

    /** System location is switched off, so no fix or geocoding is possible. */
    data object ServiceDisabled : LocationError

    /** No fix was obtained, the geocoder failed, or the platform has no location support. */
    data object Unavailable : LocationError
}
