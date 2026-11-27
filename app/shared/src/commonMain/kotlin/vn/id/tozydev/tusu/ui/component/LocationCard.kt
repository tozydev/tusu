package vn.id.tozydev.tusu.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import vn.id.tozydev.tusu.ui.theme.AppTheme

/** The large tile of the entry detail; the composer uses a compact one. */
val LargeMapTile: Dp = 72.dp
val CompactMapTile: Dp = 48.dp

/**
 * Where an entry happened: a fixed-size map tile, the name and the address. The tile keeps its size
 * so a real map can replace it later without moving anything. Without a saved map image it shows a
 * pin; a place without coordinates has no tile at all.
 */
@Composable
fun LocationCard(
    name: String,
    modifier: Modifier = Modifier,
    address: String? = null,
    mapSnapshotPath: String? = null,
    hasCoordinates: Boolean = true,
    tileSize: Dp = LargeMapTile,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (hasCoordinates) MapTile(mapSnapshotPath, tileSize)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!address.isNullOrBlank()) {
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            trailing?.invoke()
        }
    }
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier.fillMaxWidth()) { content() }
    } else {
        Card(modifier = modifier.fillMaxWidth()) { content() }
    }
}

@Composable
private fun MapTile(mapSnapshotPath: String?, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        if (mapSnapshotPath != null) {
            AsyncImage(
                model = mapSnapshotPath,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size),
            )
        } else {
            Text(text = "📍", fontSize = (size.value * 0.4f).sp)
        }
    }
}

@Preview
@Composable
private fun LocationCardPreview() {
    AppTheme { LocationCard(name = "Hồ Gươm", address = "Hoàn Kiếm, Hà Nội") }
}

@Preview
@Composable
private fun LocationCardCompactPreview() {
    AppTheme {
        LocationCard(
            name = "Nhà ngoại",
            hasCoordinates = false,
            tileSize = CompactMapTile,
            onClick = {},
        )
    }
}
