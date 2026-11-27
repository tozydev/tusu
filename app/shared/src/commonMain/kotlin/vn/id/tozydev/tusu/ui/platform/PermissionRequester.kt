package vn.id.tozydev.tusu.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import vn.id.tozydev.tusu.domain.error.PermissionError
import vn.id.tozydev.tusu.domain.model.AppPermission

/** Asks the system for one runtime permission. */
@Stable
interface PermissionRequester {
    /** Prompts only when the permission is missing; [onResult] gets null once it is granted. */
    fun request(onResult: (PermissionError?) -> Unit)

    /**
     * Opens the app's page in system settings, where a permanently refused permission is granted.
     */
    fun openSettings()
}

@Composable expect fun rememberPermissionRequester(permission: AppPermission): PermissionRequester
