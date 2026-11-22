package vn.id.tozydev.tusu.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.emoji_group_recent
import vn.id.tozydev.tusu.ui.theme.AppTheme

/**
 * [EmojiPickerSheet] with the "Recent" group read from and saved to settings. [onSelect] gets the
 * chosen emoji, or null when the emoji is removed; the caller closes the sheet.
 */
@Composable
fun EmojiPickerRoute(
    selected: String?,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = metroViewModel<RecentEmojisViewModel>()
    val recent by viewModel.recent.collectAsStateWithLifecycle()
    EmojiPickerSheet(
        selected = selected,
        recent = recent,
        onSelect = { emoji ->
            emoji?.let(viewModel::onPicked)
            onSelect(emoji)
        },
        onDismiss = onDismiss,
        modifier = modifier,
    )
}

/**
 * Bottom sheet modelled on the Android emoji picker: Cancel and Done, a search field, the groups
 * (Recent first when it has emojis) and a button to remove the emoji. Tapping an emoji marks it;
 * Done calls [onSelect] with the marked one.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiPickerSheet(
    selected: String?,
    recent: List<String>,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier, dragHandle = null) {
        EmojiPickerContent(
            selected = selected,
            recent = recent,
            onSelect = onSelect,
            onDismiss = onDismiss,
        )
    }
}

/** Sheet body without the sheet window, so it can be previewed on its own. */
@Composable
internal fun EmojiPickerContent(
    selected: String?,
    recent: List<String>,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var marked by rememberSaveable { mutableStateOf(selected) }
    var query by rememberSaveable { mutableStateOf("") }
    val recentTitle = stringResource(Res.string.emoji_group_recent)
    val sections =
        remember(recent, recentTitle) {
            buildList { if (recent.isNotEmpty()) add(EmojiSection(recentTitle, recent)) }
        }
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    Column(modifier = modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
        EmojiPickerTopBar(onCancel = onDismiss, onDone = { onSelect(marked) })
        EmojiSearchField(query = query, onQueryChange = { query = it })
        val results = remember(query) { searchEmoji(query) }
        if (query.isBlank()) {
            val groups = emojiSections(leading = sections)
            EmojiGroupChips(
                titles = groups.map { it.title },
                onClick = { index ->
                    scope.launch { gridState.animateScrollToItem(groups.headerIndex(index)) }
                },
            )
            EmojiSectionGrid(
                sections = groups,
                marked = marked,
                onToggle = { marked = if (marked == it) null else it },
                state = gridState,
            )
        } else {
            EmojiSearchResults(
                results = results,
                marked = marked,
                onToggle = { marked = if (marked == it) null else it },
            )
        }
        RemoveEmojiButton(
            enabled = selected != null || marked != null,
            onClick = { onSelect(null) },
        )
    }
}

@Preview
@Composable
private fun EmojiPickerContentPreview() {
    AppTheme {
        Surface {
            EmojiPickerContent(
                selected = "😊",
                recent = listOf("😊", "🌙", "☕"),
                onSelect = {},
                onDismiss = {},
            )
        }
    }
}

@Preview
@Composable
private fun EmojiPickerContentEmptyPreview() {
    AppTheme {
        Surface {
            EmojiPickerContent(selected = null, recent = emptyList(), onSelect = {}, onDismiss = {})
        }
    }
}
