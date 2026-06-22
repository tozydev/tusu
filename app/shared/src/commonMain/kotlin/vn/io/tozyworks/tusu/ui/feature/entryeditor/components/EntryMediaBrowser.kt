package vn.io.tozyworks.tusu.ui.feature.entryeditor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import vn.io.tozyworks.tusu.domain.model.Media

@Composable
fun EntryMediaBrowser(media: List<Media>, modifier: Modifier = Modifier) {
    when (media.size) {
        0 -> {}
        1 -> {
            SingleImage(
                media = media.first(),
                modifier =
                    Modifier.fillMaxSize().padding(16.dp).clip(MaterialTheme.shapes.extraLarge),
            )
        }
        2 -> {
            Row(
                modifier =
                    Modifier.fillMaxSize().padding(16.dp).clip(MaterialTheme.shapes.extraLarge),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier =
                        Modifier.weight(1f).padding(end = 1.dp).clip(MaterialTheme.shapes.small)
                ) {
                    SingleImage(media[0])
                }
                Box(
                    modifier =
                        Modifier.weight(1f).padding(start = 1.dp).clip(MaterialTheme.shapes.small)
                ) {
                    SingleImage(media[1])
                }
            }
        }
        else -> {
            HorizontalMultiBrowseCarousel(
                state = rememberCarouselState { media.size },
                modifier = modifier.fillMaxWidth().wrapContentHeight(),
                preferredItemWidth = 186.dp,
                itemSpacing = 8.dp,
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) { page ->
                SingleImage(media[page], Modifier.maskClip(MaterialTheme.shapes.extraLarge))
            }
        }
    }
}

@Composable
private fun SingleImage(media: Media, modifier: Modifier = Modifier) {
    AsyncImage(
        model = media.path,
        contentDescription = null,
        modifier = modifier.height(220.dp),
        contentScale = ContentScale.Crop,
    )
}
