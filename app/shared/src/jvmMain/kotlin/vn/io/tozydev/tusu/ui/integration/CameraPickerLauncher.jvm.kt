package vn.io.tozydev.tusu.ui.integration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.PlatformFile

@Composable
internal actual fun rememberCameraPickerLauncher(
    onResult: (PlatformFile?) -> Unit
): CameraPickerLauncher = remember {
    object : CameraPickerLauncher {
        override val isSupported = false

        override fun launch(cameraFacing: CameraFacingOption) {
            onResult(null)
        }
    }
}
