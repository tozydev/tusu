package vn.id.tozydev.tusu.ui.feature.media

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** [contentPadding] is the space the floating navigation bar covers; scroll content under it. */
@Composable
fun MediaRoute(contentPadding: PaddingValues, modifier: Modifier = Modifier) {
    // TODO(TOZY-22): replace the placeholder with the media library.
    MediaScreen(contentPadding = contentPadding, modifier = modifier)
}
