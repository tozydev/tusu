package vn.id.tozydev.tusu.ui.feature.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import vn.id.tozydev.tusu.domain.model.Media

@Composable
fun AdaptiveMediaGrid(
    media: List<Media>,
    modifier: Modifier = Modifier,
) {
    if (media.isEmpty()) return

    Box(modifier = modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(8.dp))) {
        when (media.size) {
            1 -> {
                SingleImage(media[0])
            }
            2 -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).padding(end = 1.dp)) {
                        SingleImage(media[0])
                    }
                    Box(modifier = Modifier.weight(1f).padding(start = 1.dp)) {
                        SingleImage(media[1])
                    }
                }
            }
            else -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(2f).padding(end = 2.dp)) {
                        SingleImage(media[0])
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.weight(1f).padding(bottom = 1.dp)) {
                            SingleImage(media[1])
                        }
                        Box(modifier = Modifier.weight(1f).padding(top = 1.dp)) {
                            SingleImage(media[2])
                            if (media.size > 3) {
                                Box(
                                    modifier =
                                        Modifier.fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "+${media.size - 2}",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontSize = 18.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SingleImage(media: Media) {
    AsyncImage(
        model = media.path,
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
    )
}
