package vn.id.tozydev.tusu.ui.feature.timeline

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.action_retry
import vn.id.tozydev.tusu.generated.resources.timeline_empty_description
import vn.id.tozydev.tusu.generated.resources.timeline_empty_title
import vn.id.tozydev.tusu.generated.resources.timeline_load_error_description
import vn.id.tozydev.tusu.generated.resources.timeline_load_error_title
import vn.id.tozydev.tusu.ui.component.EmojiBadge
import vn.id.tozydev.tusu.ui.component.EmptyState
import vn.id.tozydev.tusu.ui.feature.timeline.component.TimelineAppendFooter
import vn.id.tozydev.tusu.ui.feature.timeline.component.TimelineEntryRow
import vn.id.tozydev.tusu.ui.feature.timeline.component.sampleTimelineEntries
import vn.id.tozydev.tusu.ui.theme.AppTheme

@Composable
fun TimelineScreen(
    entries: LazyPagingItems<Entry>,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        val refresh = entries.loadState.refresh
        when {
            refresh is LoadState.Error ->
                TimelineLoadError(onRetry = entries::retry, contentPadding = contentPadding)
            refresh is LoadState.Loading && entries.itemCount == 0 ->
                TimelineLoading(contentPadding = contentPadding)
            entries.itemCount == 0 -> TimelineEmpty(contentPadding = contentPadding)
            else -> TimelineList(entries = entries, contentPadding = contentPadding)
        }
    }
}

@Composable
private fun TimelineList(
    entries: LazyPagingItems<Entry>,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    // TODO(TOZY-64): group by day with sticky headers instead of this flat list.
    LazyColumn(
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = contentPadding,
    ) {
        items(
            count = entries.itemCount,
            key = entries.itemKey { it.id.toString() },
            contentType = { "entry" },
        ) { index ->
            entries[index]?.let { entry -> TimelineEntryRow(entry = entry) }
        }
        item(key = "append-footer", contentType = "footer") {
            TimelineAppendFooter(state = entries.loadState.append, onRetry = entries::retry)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TimelineLoading(contentPadding: PaddingValues, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().statusBarsPadding().padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        LoadingIndicator()
    }
}

@Composable
private fun TimelineEmpty(contentPadding: PaddingValues, modifier: Modifier = Modifier) {
    EmptyState(
        title = stringResource(Res.string.timeline_empty_title),
        description = stringResource(Res.string.timeline_empty_description),
        illustration = { EmojiBadge(emoji = "📔", size = 72.dp) },
        modifier = modifier.fillMaxSize().statusBarsPadding().padding(contentPadding),
    )
}

@Composable
private fun TimelineLoadError(
    onRetry: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        title = stringResource(Res.string.timeline_load_error_title),
        description = stringResource(Res.string.timeline_load_error_description),
        illustration = { EmojiBadge(emoji = "😕", size = 72.dp) },
        actionLabel = stringResource(Res.string.action_retry),
        onActionClick = onRetry,
        modifier = modifier.fillMaxSize().statusBarsPadding().padding(contentPadding),
    )
}

@Preview
@Composable
private fun TimelineScreenPreview() {
    AppTheme {
        val entries = flowOf(PagingData.from(sampleTimelineEntries())).collectAsLazyPagingItems()
        TimelineScreen(entries = entries, contentPadding = PaddingValues(bottom = 104.dp))
    }
}

@Preview
@Composable
private fun TimelineScreenEmptyPreview() {
    AppTheme {
        val entries = flowOf(PagingData.empty<Entry>()).collectAsLazyPagingItems()
        TimelineScreen(entries = entries, contentPadding = PaddingValues(bottom = 104.dp))
    }
}
