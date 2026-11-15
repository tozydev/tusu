package vn.id.tozydev.tusu.ui.feature.timeline

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlin.uuid.Uuid

/** [contentPadding] is the space the floating navigation bar covers; scroll content under it. */
@Composable
fun TimelineRoute(
    contentPadding: PaddingValues,
    onNewEntryClick: () -> Unit,
    onEntryClick: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = metroViewModel<TimelineViewModel>()
    TimelineScreen(
        items = viewModel.items.collectAsLazyPagingItems(),
        dayEmojis = viewModel.dayEmojis.collectAsStateWithLifecycle().value,
        contentPadding = contentPadding,
        onNewEntryClick = onNewEntryClick,
        onEntryClick = onEntryClick,
        modifier = modifier,
    )
}
