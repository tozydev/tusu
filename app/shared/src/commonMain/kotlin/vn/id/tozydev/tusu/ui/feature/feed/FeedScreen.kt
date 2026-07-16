package vn.id.tozydev.tusu.ui.feature.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlin.uuid.Uuid
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.domain.model.Tag
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.app_name
import vn.id.tozydev.tusu.generated.resources.fab_compose
import vn.id.tozydev.tusu.generated.resources.ic_filled_edit_24px
import vn.id.tozydev.tusu.generated.resources.ic_settings_24px
import vn.id.tozydev.tusu.generated.resources.settings_desc
import vn.id.tozydev.tusu.generated.resources.tag_filter_all
import vn.id.tozydev.tusu.ui.feature.feed.components.EntryCard
import vn.id.tozydev.tusu.ui.feature.feed.components.TagFilterChip
import vn.id.tozydev.tusu.ui.formatter.DateTimeFormatter

context(dateTimeFormatter: DateTimeFormatter)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    onNavigateToEditor: (Uuid?) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyFeedItems = viewModel.feedItemsPaging.collectAsLazyPagingItems()

    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val tagIdFilter by viewModel.tagIdFilter.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val listState = rememberLazyListState()
    val isScrollingUp by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { FeedTopAppBar(scrollBehavior, onNavigateToSettings) },
        floatingActionButton = { ComposeEntryFab(onNavigateToEditor, isScrollingUp) },
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding())
        ) {
            TagFiltersRow(tags, tagIdFilter, viewModel::setTagIdFilter)

            Box(modifier = Modifier.weight(1f)) {
                FeedItemList(
                    listState = listState,
                    lazyFeedItems = lazyFeedItems,
                    onNavigateToEditor = onNavigateToEditor,
                    bottomPadding = innerPadding.calculateBottomPadding(),
                )
            }
        }
    }
}

context(dateTimeFormatter: DateTimeFormatter)
@Composable
private fun FeedItemList(
    listState: LazyListState,
    lazyFeedItems: LazyPagingItems<FeedItemUi>,
    onNavigateToEditor: (Uuid?) -> Unit,
    bottomPadding: Dp,
) {
    LazyColumn(
        state = listState,
        contentPadding =
            PaddingValues(
                start = 16.dp,
                top = 8.dp,
                end = 16.dp,
                bottom = 8.dp + bottomPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        val itemCount = lazyFeedItems.itemCount
        for (i in 0 until itemCount) {
            when (val item = lazyFeedItems[i]) {
                is FeedItemUi.DateHeader -> {
                    stickyHeader(key = "header_${item.date}") {
                        FeedItemDateHeader(item)
                    }
                }

                is FeedItemUi.EntryItem -> {
                    item(key = item.entry.id) {
                        EntryCard(
                            entry = item.entry,
                            onClick = { onNavigateToEditor(item.entry.id) },
                        )
                    }
                }

                null -> {}
            }
        }

        if (lazyFeedItems.loadState.append is LoadState.Loading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator()
                }
            }
        }
    }
}

@Composable
private fun FeedItemDateHeader(item: FeedItemUi.DateHeader) {
    Box(
        modifier =
            Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = item.text.asString(),
            style = MaterialTheme.typography.titleLargeEmphasized,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun TagFiltersRow(tags: List<Tag>, tagIdFilter: Uuid?, setTagIdFilter: (Uuid?) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        item {
            TagFilterChip(
                text = stringResource(Res.string.tag_filter_all),
                isSelected = tagIdFilter == null,
                onClick = { setTagIdFilter(null) },
            )
        }

        items(tags, { it.id }) { tag ->
            TagFilterChip(
                text = tag.name,
                isSelected = tag.id == tagIdFilter,
                onClick = { setTagIdFilter(tag.id) },
            )
        }
    }
}

@Composable
private fun ComposeEntryFab(onNavigateToEditor: (Uuid?) -> Unit, isScrollingUp: Boolean) {
    ExtendedFloatingActionButton(
        onClick = { onNavigateToEditor(null) },
        expanded = isScrollingUp,
        icon = { Icon(painterResource(Res.drawable.ic_filled_edit_24px), null) },
        text = { Text(stringResource(Res.string.fab_compose)) },
    )
}

@Composable
private fun FeedTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onNavigateToSettings: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.displaySmallEmphasized,
                fontWeight = FontWeight.Bold,
            )
        },
        scrollBehavior = scrollBehavior,
        actions = {
            FilledIconButton(
                onClick = onNavigateToSettings,
                colors =
                    IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_settings_24px),
                    contentDescription = stringResource(Res.string.settings_desc),
                )
            }
        },
    )
}
