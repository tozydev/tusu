package vn.id.tozydev.tusu.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

/** Takes one photo with the system camera. */
@Stable
interface CameraLauncher {
    fun launch()
}

/**
 * Null on platforms without a camera, where the camera button must be hidden. [onResult] gets null
 * when the user cancels.
 */
@Composable expect fun rememberCameraLauncher(onResult: (PickedPhoto?) -> Unit): CameraLauncher?
