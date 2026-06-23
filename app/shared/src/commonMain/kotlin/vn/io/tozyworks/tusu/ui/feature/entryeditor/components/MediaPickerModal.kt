package vn.io.tozyworks.tusu.ui.feature.entryeditor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.io.tozyworks.tusu.generated.resources.Res
import vn.io.tozyworks.tusu.generated.resources.add_media
import vn.io.tozyworks.tusu.generated.resources.choose_gallery_btn
import vn.io.tozyworks.tusu.generated.resources.ic_add_a_photo_24px
import vn.io.tozyworks.tusu.generated.resources.ic_add_photo_alternate_24px
import vn.io.tozyworks.tusu.generated.resources.take_photo_btn
import vn.io.tozyworks.tusu.ui.integration.CameraFacingOption
import vn.io.tozyworks.tusu.ui.integration.rememberCameraPickerLauncher

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MediaPickerModal(
    onDismiss: () -> Unit,
    onSelectMedia: (List<PlatformFile>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cameraPickerLauncher = rememberCameraPickerLauncher { file ->
        file?.let { onSelectMedia(listOf(it)) }
    }
    val imagesPickerLauncher =
        rememberFilePickerLauncher(
            type = FileKitType.Image,
            mode = FileKitMode.Multiple(),
        ) { files ->
            files?.let { onSelectMedia(it) }
        }

    val count = 2
    val colors =
        ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    val contentPadding =
        PaddingValues(
            horizontal = 24.dp,
            vertical = 16.dp,
        )

    ModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        Text(
            text = stringResource(Res.string.add_media),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Column(
            modifier =
                Modifier.padding(top = 20.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
                    .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SegmentedListItem(
                onClick = {
                    cameraPickerLauncher.launch(cameraFacing = CameraFacingOption.System)
                },
                shapes = ListItemDefaults.segmentedShapes(0, count),
                colors = colors,
                contentPadding = contentPadding,
                leadingContent = {
                    Icon(
                        painterResource(Res.drawable.ic_add_a_photo_24px),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                },
            ) {
                Text(
                    text = stringResource(Res.string.take_photo_btn),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            SegmentedListItem(
                onClick = {
                    imagesPickerLauncher.launch()
                },
                shapes = ListItemDefaults.segmentedShapes(1, count),
                colors = colors,
                contentPadding = contentPadding,
                leadingContent = {
                    Icon(
                        painterResource(Res.drawable.ic_add_photo_alternate_24px),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                },
            ) {
                Text(
                    text = stringResource(Res.string.choose_gallery_btn),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}
