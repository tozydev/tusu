package vn.id.tozydev.tusu.ui.feature.timeline.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.timeline_more_photos
import vn.id.tozydev.tusu.ui.component.MediaThumbnail
import vn.id.tozydev.tusu.ui.component.MediaUi
import vn.id.tozydev.tusu.ui.theme.AppTheme

private val GridGap = 4.dp
private val CellCorner = 16.dp
private const val MaxGridCells = 4
private const val SquareRatio = 1f
private const val DefaultRatio = 4f / 3f
private const val MinRatio = 0.75f
private const val MaxRatio = 1.6f

/**
 * Cells per row for the cells the feed shows: one photo is one large cell, two and three photos a
 * single row, and from four photos a 2 by 2 grid whose last cell may stand for the rest.
 */
internal fun photoGridRows(count: Int): List<Int> =
    when {
        count <= 0 -> emptyList()
        count == 1 -> listOf(1)
        count <= 3 -> listOf(count)
        else -> listOf(2, 2)
    }

/**
 * Aspect ratio of a lone photo from its size, clamped so a panorama or a tall shot stays usable.
 */
internal fun singlePhotoAspectRatio(width: Int?, height: Int?): Float =
    if (width != null && height != null && width > 0 && height > 0) {
        (width.toFloat() / height).coerceIn(MinRatio, MaxRatio)
    } else {
        DefaultRatio
    }

/** The photos of one entry; the feed never shows more than four cells. */
@Composable
fun TimelinePhotoGrid(photos: List<MediaUi>, modifier: Modifier = Modifier) {
    if (photos.size == 1) {
        val photo = photos.first()
        PhotoTile(
            photo = photo,
            modifier = modifier.fillMaxWidth(),
            aspectRatio = singlePhotoAspectRatio(photo.width, photo.height),
        )
        return
    }
    val rows = photoGridRows(photos.size)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(GridGap),
    ) {
        var index = 0
        rows.forEach { cells ->
            Row(horizontalArrangement = Arrangement.spacedBy(GridGap)) {
                repeat(cells) {
                    val shown = index++
                    val rest = photos.size - MaxGridCells
                    val overflow = if (shown == MaxGridCells - 1 && rest > 0) rest else 0
                    GridCell(
                        photo = photos[shown],
                        overflow = overflow,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/** A square cell; when [overflow] is positive the cell carries a "+N" scrim. */
@Composable
private fun GridCell(photo: MediaUi, overflow: Int, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        PhotoTile(photo = photo, modifier = Modifier.fillMaxWidth(), aspectRatio = SquareRatio)
        if (overflow > 0) {
            OverflowScrim(count = overflow, modifier = Modifier.matchParentSize())
        }
    }
}

@Composable
private fun PhotoTile(photo: MediaUi, aspectRatio: Float, modifier: Modifier = Modifier) {
    MediaThumbnail(
        model = photo.path,
        type = MediaType.PHOTO,
        status = photo.status,
        modifier = modifier,
        aspectRatio = aspectRatio,
    )
}

@Composable
private fun OverflowScrim(count: Int, modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(CellCorner))
                .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(Res.string.timeline_more_photos, count),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            modifier = Modifier.padding(4.dp),
        )
    }
}

@Preview
@Composable
private fun TimelinePhotoGridOnePreview() {
    AppTheme { TimelinePhotoGrid(photos = samplePhotos(1)) }
}

@Preview
@Composable
private fun TimelinePhotoGridThreePreview() {
    AppTheme { TimelinePhotoGrid(photos = samplePhotos(3)) }
}

@Preview
@Composable
private fun TimelinePhotoGridFourPreview() {
    AppTheme { TimelinePhotoGrid(photos = samplePhotos(4)) }
}

@Preview
@Composable
private fun TimelinePhotoGridSevenPreview() {
    AppTheme { TimelinePhotoGrid(photos = samplePhotos(7)) }
}
