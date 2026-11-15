package vn.id.tozydev.tusu.ui.feature.entry.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.media_photo
import vn.id.tozydev.tusu.generated.resources.viewer_page_indicator
import vn.id.tozydev.tusu.ui.component.MediaUi
import vn.id.tozydev.tusu.ui.feature.entry.sampleEntryPhotos
import vn.id.tozydev.tusu.ui.theme.AppTheme

private val CarouselHeight = 320.dp
private val PageCorner = RoundedCornerShape(28.dp)
private val SideInset = 32.dp
private val LoneSideInset = 16.dp

/**
 * Photos in a pager; the neighbouring photos peek in at the edges. [onPhotoClick] gets the index of
 * the tapped photo. The page counter only shows with more than one photo.
 */
@Composable
fun EntryPhotoCarousel(
    photos: List<MediaUi>,
    onPhotoClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { photos.size })
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(CarouselHeight),
            contentPadding =
                PaddingValues(horizontal = if (photos.size > 1) SideInset else LoneSideInset),
            pageSpacing = 8.dp,
            key = { photos[it].id.toString() },
        ) { page ->
            CarouselPhoto(
                photo = photos[page],
                onClick = { onPhotoClick(page) },
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (photos.size > 1) {
            Text(
                text =
                    stringResource(
                        Res.string.viewer_page_indicator,
                        pagerState.currentPage + 1,
                        photos.size,
                    ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CarouselPhoto(photo: MediaUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .clip(PageCorner)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = photo.path,
            contentDescription = stringResource(Res.string.media_photo),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview
@Composable
private fun EntryPhotoCarouselPreview() {
    AppTheme { EntryPhotoCarousel(photos = sampleEntryPhotos(3), onPhotoClick = {}) }
}

@Preview
@Composable
private fun EntryPhotoCarouselSinglePreview() {
    AppTheme { EntryPhotoCarousel(photos = sampleEntryPhotos(1), onPhotoClick = {}) }
}
