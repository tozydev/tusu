package vn.id.tozydev.tusu.ui.platform

import androidx.compose.runtime.Composable

// Desktop has no camera, so the camera button is hidden and onResult never runs.
@Composable
@Suppress("UNUSED_PARAMETER")
actual fun rememberCameraLauncher(
    onResult: (PickedPhoto?) -> Unit,
    onError: () -> Unit,
): CameraLauncher? = null
