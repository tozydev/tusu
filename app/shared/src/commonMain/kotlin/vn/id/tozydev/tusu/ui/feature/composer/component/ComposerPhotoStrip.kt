package vn.id.tozydev.tusu.ui.feature.composer.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.uuid.Uuid
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.composer_remove_photo
import vn.id.tozydev.tusu.generated.resources.ic_close_24px
import vn.id.tozydev.tusu.ui.component.MediaThumbnail
import vn.id.tozydev.tusu.ui.component.MediaUi
import vn.id.tozydev.tusu.ui.theme.AppTheme

/**
 * A row of square photo tiles. A failed tile is retried by tapping it, and each tile has a remove
 * button; [onRemove] and [onRetry] get the photo's id.
 */
@Composable
fun ComposerPhotoStrip(
    photos: List<MediaUi>,
    onRemove: (Uuid) -> Unit,
    onRetry: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items = photos, key = { it.id }) { photo ->
            PhotoTile(
                photo = photo,
                onRemove = { onRemove(photo.id) },
                onRetry = { onRetry(photo.id) },
            )
        }
    }
}

@Composable
private fun PhotoTile(
    photo: MediaUi,
    onRemove: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(TileSize)) {
        MediaThumbnail(
            model = photo.path,
            type = photo.type,
            status = photo.status,
            modifier = Modifier.fillMaxSize(),
            onRetry = onRetry,
        )
        RemoveButton(onClick = onRemove, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp))
    }
}

@Composable
private fun RemoveButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val description = stringResource(Res.string.composer_remove_photo)
    Box(
        modifier =
            modifier
                .size(RemoveSize)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(role = Role.Button, onClick = onClick)
                .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_close_24px),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp),
        )
    }
}

private val TileSize = 96.dp
private val RemoveSize = 26.dp

@Preview
@Composable
private fun ComposerPhotoStripPreview() {
    AppTheme {
        ComposerPhotoStrip(
            photos =
                listOf(
                    previewPhoto(MediaStatus.READY),
                    previewPhoto(MediaStatus.PROCESSING),
                    previewPhoto(MediaStatus.FAILED),
                ),
            onRemove = {},
            onRetry = {},
        )
    }
}

private fun previewPhoto(status: MediaStatus): MediaUi =
    MediaUi(
        id = Uuid.random(),
        path = "",
        type = MediaType.PHOTO,
        status = status,
        width = null,
        height = null,
    )
