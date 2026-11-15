package vn.id.tozydev.tusu.ui.feature.timeline

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import dev.zacsweers.metrox.viewmodel.metroViewModel

/** [contentPadding] is the space the floating navigation bar covers; scroll content under it. */
@Composable
fun TimelineRoute(contentPadding: PaddingValues, modifier: Modifier = Modifier) {
    val viewModel = metroViewModel<TimelineViewModel>()
    TimelineScreen(
        entries = viewModel.entries.collectAsLazyPagingItems(),
        contentPadding = contentPadding,
        modifier = modifier,
    )
}
