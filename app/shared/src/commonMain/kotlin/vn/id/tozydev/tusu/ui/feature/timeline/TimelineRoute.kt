package vn.id.tozydev.tusu.ui.feature.timeline

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** [contentPadding] is the space the floating navigation bar covers; scroll content under it. */
@Composable
fun TimelineRoute(contentPadding: PaddingValues, modifier: Modifier = Modifier) {
    // TODO(TOZY-64): replace the placeholder with the entry feed.
    TimelineScreen(contentPadding = contentPadding, modifier = modifier)
}
