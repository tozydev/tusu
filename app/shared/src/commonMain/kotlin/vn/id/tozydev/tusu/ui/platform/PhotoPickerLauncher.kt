package vn.id.tozydev.tusu.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.PickerResultLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher

/** Opens the system gallery to choose any number of photos. */
@Stable
interface PhotoPickerLauncher {
    fun launch()
}

/**
 * [onResult] gets the chosen photos; an empty list when the user cancels. [onError] is called when
 * the picker cannot open or hand over the photos.
 */
@Composable
fun rememberPhotoPickerLauncher(
    onResult: (List<PickedPhoto>) -> Unit,
    onError: () -> Unit = {},
): PhotoPickerLauncher {
    val picker =
        rememberFilePickerLauncher(
            type = FileKitType.Image,
            mode = FileKitMode.Multiple(),
            onError = { onError() },
            onResult = { files: List<PlatformFile>? ->
                onResult(files.orEmpty().map { it.toPickedPhoto() })
            },
        )
    return remember(picker) { picker.asPhotoPickerLauncher() }
}

private fun PickerResultLauncher.asPhotoPickerLauncher(): PhotoPickerLauncher =
    object : PhotoPickerLauncher {
        override fun launch() = this@asPhotoPickerLauncher.launch()
    }
