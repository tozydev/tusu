package vn.id.tozydev.tusu.ui.feature.entryeditor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlin.uuid.Uuid
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.domain.model.Media
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.add_media
import vn.id.tozydev.tusu.generated.resources.add_media_desc
import vn.id.tozydev.tusu.generated.resources.ic_add_photo_alternate_24px
import vn.id.tozydev.tusu.generated.resources.ic_close_24px
import vn.id.tozydev.tusu.generated.resources.remove_media_desc

@Composable
fun EntryMediaEditor(
    mediaList: List<Media>,
    onAddMedia: () -> Unit,
    onRemoveMedia: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(mediaList, { it.id }) { media ->
            Box(
                modifier =
                    Modifier.size(width = 186.dp, height = 220.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
            ) {
                AsyncImage(
                    model = media.path,
                    contentDescription = null,
                    modifier = Modifier.size(width = 186.dp, height = 220.dp),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier =
                        Modifier.align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
                            .clickable { onRemoveMedia(media.id) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_close_24px),
                        contentDescription =
                            stringResource(Res.string.remove_media_desc, media.id.toString()),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        item {
            Box(
                modifier =
                    Modifier.size(width = 186.dp, height = 220.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .clickable { onAddMedia() },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_add_photo_alternate_24px),
                        contentDescription = stringResource(Res.string.add_media_desc),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp),
                    )
                    Text(
                        text = stringResource(Res.string.add_media),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
