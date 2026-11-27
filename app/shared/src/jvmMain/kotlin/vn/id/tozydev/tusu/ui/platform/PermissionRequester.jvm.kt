package vn.id.tozydev.tusu.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import vn.id.tozydev.tusu.domain.error.PermissionError
import vn.id.tozydev.tusu.domain.model.AppPermission

/** Desktop has no runtime permissions; access problems surface from the feature itself. */
@Composable
actual fun rememberPermissionRequester(permission: AppPermission): PermissionRequester = remember {
    object : PermissionRequester {
        override fun request(onResult: (PermissionError?) -> Unit) = onResult(null)

        override fun openSettings() = Unit
    }
}
