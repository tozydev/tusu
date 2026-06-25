package vn.id.tozydev.tusu.ui.integration

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

internal enum class CameraFacingOption {
    System,
    Front,
    Back,
}

internal interface CameraPickerLauncher {
    val isSupported: Boolean

    fun launch(cameraFacing: CameraFacingOption)
}

@Composable
internal expect fun rememberCameraPickerLauncher(
    onResult: (PlatformFile?) -> Unit
): CameraPickerLauncher
