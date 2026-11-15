package vn.id.tozydev.tusu.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.media_audio
import vn.id.tozydev.tusu.generated.resources.media_photo
import vn.id.tozydev.tusu.generated.resources.media_video
import vn.id.tozydev.tusu.ui.theme.AppTheme

/**
 * Thumbnail of a media item. [model] is anything Coil can load (a path or a `PlatformFile`);
 * [durationText] is already formatted. While [status] is not READY an overlay covers the tile, and
 * a FAILED tile calls [onRetry] when tapped.
 */
@Composable
fun MediaThumbnail(
    model: Any?,
    type: MediaType,
    status: MediaStatus,
    modifier: Modifier = Modifier,
    durationText: String? = null,
    waveform: List<Float> = emptyList(),
    aspectRatio: Float = 1f,
    onClick: (() -> Unit)? = null,
    onRetry: (() -> Unit)? = null,
    contentDescription: String? = null,
) {
    val description = contentDescription ?: stringResource(type.labelRes())
    val clickable =
        if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier
    Box(
        modifier =
            modifier
                .aspectRatio(aspectRatio)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .then(clickable)
                .semantics { this.contentDescription = description },
    ) {
        MediaThumbnailContent(model, type, waveform, durationText)
        when (status) {
            MediaStatus.READY -> Unit
            MediaStatus.PROCESSING -> ProcessingOverlay(Modifier.fillMaxSize())
            MediaStatus.FAILED -> FailedOverlay(onRetry, Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun MediaThumbnailContent(
    model: Any?,
    type: MediaType,
    waveform: List<Float>,
    durationText: String?,
) {
    Box(Modifier.fillMaxSize()) {
        when (type) {
            MediaType.AUDIO -> AudioTile(waveform, Modifier.fillMaxSize())
            MediaType.PHOTO,
            MediaType.VIDEO ->
                AsyncImage(
                    model = model,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
        }
        if (type == MediaType.VIDEO) PlayGlyph(Modifier.align(Alignment.Center))
        if (durationText != null && type != MediaType.PHOTO) {
            DurationBadge(durationText, Modifier.align(Alignment.BottomEnd).padding(6.dp))
        }
    }
}

private fun MediaType.labelRes() =
    when (this) {
        MediaType.PHOTO -> Res.string.media_photo
        MediaType.VIDEO -> Res.string.media_video
        MediaType.AUDIO -> Res.string.media_audio
    }

@Preview
@Composable
private fun MediaThumbnailTypesPreview() {
    AppTheme {
        Surface {
            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MediaThumbnail(null, MediaType.PHOTO, MediaStatus.READY, Modifier.size(96.dp))
                MediaThumbnail(
                    null,
                    MediaType.VIDEO,
                    MediaStatus.READY,
                    Modifier.size(96.dp),
                    durationText = "0:42",
                )
                MediaThumbnail(
                    null,
                    MediaType.AUDIO,
                    MediaStatus.READY,
                    Modifier.size(96.dp),
                    durationText = "1:05",
                    waveform = List(40) { (it % 7) / 7f },
                )
                MediaThumbnail(
                    null,
                    MediaType.AUDIO,
                    MediaStatus.READY,
                    Modifier.size(96.dp),
                    durationText = "0:08",
                )
            }
        }
    }
}

@Preview
@Composable
private fun MediaThumbnailStatusPreview() {
    AppTheme {
        Surface {
            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MediaThumbnail(null, MediaType.PHOTO, MediaStatus.PROCESSING, Modifier.size(96.dp))
                MediaThumbnail(
                    null,
                    MediaType.VIDEO,
                    MediaStatus.FAILED,
                    Modifier.size(96.dp),
                    durationText = "0:42",
                    onRetry = {},
                )
                MediaThumbnail(
                    null,
                    MediaType.PHOTO,
                    MediaStatus.READY,
                    Modifier.size(width = 160.dp, height = 96.dp),
                    aspectRatio = 16f / 9f,
                    onClick = {},
                )
            }
        }
    }
}
