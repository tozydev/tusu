package vn.id.tozydev.tusu.ui.platform

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import vn.id.tozydev.tusu.domain.error.PermissionError
import vn.id.tozydev.tusu.domain.model.AppPermission

@Composable
actual fun rememberPermissionRequester(permission: AppPermission): PermissionRequester {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val pending = remember { PendingResult() }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            results ->
            // Any of the names is enough: location works with approximate access alone.
            val error = if (results.values.any { it }) null else denial(activity, permission)
            pending.take()?.invoke(error)
        }
    return remember(context, launcher, permission) {
        AndroidPermissionRequester(context, launcher, permission, pending)
    }
}

/** The callback of the prompt on screen; one prompt runs at a time. */
private class PendingResult {
    private var callback: ((PermissionError?) -> Unit)? = null

    fun put(onResult: (PermissionError?) -> Unit) {
        callback = onResult
    }

    fun take(): ((PermissionError?) -> Unit)? = callback.also { callback = null }
}

private class AndroidPermissionRequester(
    private val context: Context,
    private val launcher: ActivityResultLauncher<Array<String>>,
    private val permission: AppPermission,
    private val pending: PendingResult,
) : PermissionRequester {
    override fun request(onResult: (PermissionError?) -> Unit) {
        val names = permission.manifestNames()
        val granted =
            names.any {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        if (names.isEmpty() || granted) {
            onResult(null)
            return
        }
        pending.put(onResult)
        launcher.launch(names.toTypedArray())
    }

    override fun openSettings() {
        val intent =
            Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null),
                )
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

/**
 * After a refusal the system still explains the permission only while it may ask again; once it
 * stops, the prompt is no longer shown and only system settings can grant it.
 */
private fun denial(activity: Activity?, permission: AppPermission): PermissionError {
    val canAskAgain =
        permission.manifestNames().any { activity?.shouldShowRequestPermissionRationale(it) == true }
    return if (canAskAgain) PermissionError.Denied(permission)
    else PermissionError.DeniedPermanently(permission)
}

/**
 * Empty when this Android version grants the permission without asking. Location asks for both
 * names: Android 12 ignores a request for precise location alone and lets the user pick.
 */
private fun AppPermission.manifestNames(): List<String> =
    when (this) {
        AppPermission.CAMERA -> listOf(Manifest.permission.CAMERA)
        AppPermission.MICROPHONE -> listOf(Manifest.permission.RECORD_AUDIO)
        AppPermission.LOCATION ->
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        AppPermission.NOTIFICATIONS ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listOf(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                emptyList()
            }
    }
