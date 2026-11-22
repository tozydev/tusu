package vn.id.tozydev.tusu.ui.feature.entry.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.id.tozydev.tusu.ui.component.MediaThumbnail
import vn.id.tozydev.tusu.ui.component.MediaUi
import vn.id.tozydev.tusu.ui.feature.entry.sampleEntryPhotos
import vn.id.tozydev.tusu.ui.theme.AppTheme

/** The carousel shrunk to a row of small tiles once the content is scrolled. */
@Composable
fun EntryPhotoStrip(
    photos: List<MediaUi>,
    onPhotoClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        itemsIndexed(photos, key = { _, photo -> photo.id.toString() }) { index, photo ->
            MediaThumbnail(
                model = photo.path,
                type = photo.type,
                status = photo.status,
                onClick = { onPhotoClick(index) },
                modifier = Modifier.size(48.dp),
            )
        }
    }
}

@Preview
@Composable
private fun EntryPhotoStripPreview() {
    AppTheme { EntryPhotoStrip(photos = sampleEntryPhotos(4), onPhotoClick = {}) }
}
