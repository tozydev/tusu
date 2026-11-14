package vn.id.tozydev.tusu.domain.error

import vn.id.tozydev.tusu.domain.model.AppPermission

sealed interface PermissionError {
    val permission: AppPermission

    /** The user declined this time; asking again is allowed. */
    data class Denied(override val permission: AppPermission) : PermissionError

    /** The system no longer shows the prompt; the user has to grant it in system settings. */
    data class DeniedPermanently(override val permission: AppPermission) : PermissionError
}
