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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlin.time.Clock
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.action_retry
import vn.id.tozydev.tusu.generated.resources.timeline_empty_action
import vn.id.tozydev.tusu.generated.resources.timeline_empty_description
import vn.id.tozydev.tusu.generated.resources.timeline_empty_title
import vn.id.tozydev.tusu.generated.resources.timeline_load_error_description
import vn.id.tozydev.tusu.generated.resources.timeline_load_error_title
import vn.id.tozydev.tusu.ui.component.EmojiBadge
import vn.id.tozydev.tusu.ui.component.EmptyState
import vn.id.tozydev.tusu.ui.component.LocalMessageHostState
import vn.id.tozydev.tusu.ui.component.MessageHostState
import vn.id.tozydev.tusu.ui.component.UiMessage
import vn.id.tozydev.tusu.ui.feature.timeline.component.TimelineAppendFooter
import vn.id.tozydev.tusu.ui.feature.timeline.component.TimelineDayHeader
import vn.id.tozydev.tusu.ui.feature.timeline.component.TimelineEntryRow
import vn.id.tozydev.tusu.ui.feature.timeline.component.sampleTimelineEntries
import vn.id.tozydev.tusu.ui.format.LocalAppDateTimeFormatter
import vn.id.tozydev.tusu.ui.format.UiText
import vn.id.tozydev.tusu.ui.theme.AppTheme

@Composable
fun TimelineScreen(
    items: LazyPagingItems<TimelineItem>,
    dayEmojis: Map<LocalDate, List<String>>,
    contentPadding: PaddingValues,
    onNewEntryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        val refresh = items.loadState.refresh
        // A refresh error with entries already on screen keeps the list; it is reported as a
        // message.
        val refreshErrorWithItems = (refresh as? LoadState.Error)?.takeIf { items.itemCount > 0 }
        RefreshErrorMessage(error = refreshErrorWithItems, onRetry = items::retry)
        when {
            refresh is LoadState.Error && items.itemCount == 0 ->
                TimelineLoadError(onRetry = items::retry, contentPadding = contentPadding)
            refresh is LoadState.Loading && items.itemCount == 0 ->
                TimelineLoading(contentPadding = contentPadding)
            items.itemCount == 0 ->
                TimelineEmpty(onNewEntryClick = onNewEntryClick, contentPadding = contentPadding)
            else ->
                TimelineList(items = items, dayEmojis = dayEmojis, contentPadding = contentPadding)
        }
    }
}

/** Posts one retryable message per [error]; a new error object posts again. */
@Composable
private fun RefreshErrorMessage(error: LoadState.Error?, onRetry: () -> Unit) {
    val messageHost = LocalMessageHostState.current
    LaunchedEffect(error) {
        if (error != null) {
            messageHost.show(
                UiMessage.retryable(UiText.of(Res.string.timeline_load_error_title), onRetry),
            )
        }
    }
}

@Composable
private fun TimelineList(
    items: LazyPagingItems<TimelineItem>,
    dayEmojis: Map<LocalDate, List<String>>,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val clock = remember { Clock.System }
    val timeZone = remember { TimeZone.currentSystemDefault() }
    val formatter = LocalAppDateTimeFormatter.current
    LazyColumn(
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = contentPadding,
    ) {
        // A plain index loop instead of items(): stickyHeader is only available per item, and
        // peek() reads a row without loading it, so each row is registered with items[index].
        for (index in 0 until items.itemCount) {
            when (val row = items.peek(index)) {
                is TimelineItem.Day ->
                    stickyHeader(key = "day-${row.date}", contentType = "day") {
                        items[index]
                        val title = context(clock, timeZone) { formatter.dayHeader(row.date) }
                        TimelineDayHeader(title = title, emojis = dayEmojis[row.date].orEmpty())
                    }
                is TimelineItem.EntryItem ->
                    item(key = row.entry.id.toString(), contentType = "entry") {
                        items[index]
                        TimelineEntryRow(entry = row.entry)
                    }
                null -> Unit // Placeholders are disabled in the repository, so this is rare.
            }
        }
        item(key = "append-footer", contentType = "footer") {
            TimelineAppendFooter(state = items.loadState.append, onRetry = items::retry)
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
private fun TimelineEmpty(
    onNewEntryClick: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        title = stringResource(Res.string.timeline_empty_title),
        description = stringResource(Res.string.timeline_empty_description),
        illustration = { EmojiBadge(emoji = "📔", size = 72.dp) },
        actionLabel = stringResource(Res.string.timeline_empty_action),
        onActionClick = onNewEntryClick,
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
        CompositionLocalProvider(LocalMessageHostState provides remember { MessageHostState() }) {
            val items = flowOf(PagingData.from(previewTimelineItems())).collectAsLazyPagingItems()
            TimelineScreen(
                items = items,
                dayEmojis = previewDayEmojis(),
                contentPadding = PaddingValues(bottom = 104.dp),
                onNewEntryClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun TimelineScreenEmptyPreview() {
    AppTheme {
        CompositionLocalProvider(LocalMessageHostState provides remember { MessageHostState() }) {
            val items = flowOf(PagingData.empty<TimelineItem>()).collectAsLazyPagingItems()
            TimelineScreen(
                items = items,
                dayEmojis = emptyMap(),
                contentPadding = PaddingValues(bottom = 104.dp),
                onNewEntryClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun TimelineEmptyPreview() {
    AppTheme { TimelineEmpty(onNewEntryClick = {}, contentPadding = PaddingValues()) }
}

@Preview
@Composable
private fun TimelineLoadErrorPreview() {
    AppTheme { TimelineLoadError(onRetry = {}, contentPadding = PaddingValues()) }
}

/** One day header followed by the sample entries, which all fall on the same day. */
private fun previewTimelineItems(): List<TimelineItem> {
    val entries = sampleTimelineEntries()
    val day = TimelineItem.Day(entries.first().localDate)
    return listOf(day) + entries.map { TimelineItem.EntryItem(it) }
}

private fun previewDayEmojis(): Map<LocalDate, List<String>> =
    sampleTimelineEntries()
        .groupBy { it.localDate }
        .mapValues { (_, entries) -> entries.mapNotNull { it.emoji } }
