package vn.id.tozydev.tusu.ui.platform

import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.vinceglb.filekit.dialogs.compose.PhotoResultLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher

@Composable
actual fun rememberCameraLauncher(onResult: (PickedPhoto?) -> Unit): CameraLauncher? {
    val context = LocalContext.current
    // Devices without any camera get no button; the camera app is only opened through an intent,
    // so the CAMERA permission is not needed.
    val hasCamera = remember(context) { context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) }
    val camera = rememberCameraPickerLauncher(onResult = { file -> onResult(file?.toPickedPhoto()) })
    val launcher = remember(camera) { camera.asCameraLauncher() }
    return if (hasCamera) launcher else null
}

private fun PhotoResultLauncher.asCameraLauncher(): CameraLauncher =
    object : CameraLauncher {
        override fun launch() = this@asCameraLauncher.launch()
    }
