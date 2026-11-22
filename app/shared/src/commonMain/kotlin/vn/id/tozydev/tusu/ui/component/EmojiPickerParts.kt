package vn.id.tozydev.tusu.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.action_cancel
import vn.id.tozydev.tusu.generated.resources.action_done
import vn.id.tozydev.tusu.generated.resources.emoji_picker_title
import vn.id.tozydev.tusu.generated.resources.emoji_remove
import vn.id.tozydev.tusu.generated.resources.emoji_search_empty
import vn.id.tozydev.tusu.generated.resources.emoji_search_hint

private val CellSize = 48.dp
private val GridMaxHeight = 320.dp

/** A titled run of emojis in the picker grid. */
internal data class EmojiSection(val title: String, val emojis: List<String>)

/** [leading] (the Recent group) followed by the catalog groups. */
@Composable
internal fun emojiSections(leading: List<EmojiSection>): List<EmojiSection> =
    leading +
        EmojiGroups.map { group ->
            EmojiSection(stringResource(group.title), group.emojis.map { it.emoji })
        }

/**
 * Grid index of the header of section [index]: every earlier section adds a header and its emojis.
 */
internal fun List<EmojiSection>.headerIndex(index: Int): Int =
    take(index).sumOf { it.emojis.size + 1 }

@Composable
internal fun EmojiPickerTopBar(
    onCancel: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onCancel) { Text(stringResource(Res.string.action_cancel)) }
        Text(
            text = stringResource(Res.string.emoji_picker_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onDone) { Text(stringResource(Res.string.action_done)) }
    }
}

@Composable
internal fun EmojiSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(Res.string.emoji_search_hint)) },
        singleLine = true,
        shape = CircleShape,
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
    )
}

@Composable
internal fun EmojiGroupChips(
    titles: List<String>,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        titles.forEachIndexed { index, title ->
            AssistChip(onClick = { onClick(index) }, label = { Text(title) })
        }
    }
}

@Composable
internal fun EmojiSectionGrid(
    sections: List<EmojiSection>,
    marked: String?,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(CellSize),
        state = state,
        modifier = modifier.fillMaxWidth().heightIn(max = GridMaxHeight),
    ) {
        sections.forEachIndexed { sectionIndex, section ->
            item(key = "header-$sectionIndex", span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                )
            }
            // The same emoji can be in Recent and in its group, so keys carry the section.
            items(section.emojis, key = { "$sectionIndex-$it" }) { emoji ->
                EmojiPickerCell(
                    emoji = emoji,
                    isSelected = emoji == marked,
                    onClick = { onToggle(emoji) },
                )
            }
        }
    }
}

@Composable
internal fun EmojiSearchResults(
    results: List<String>,
    marked: String?,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (results.isEmpty()) {
        Text(
            text = stringResource(Res.string.emoji_search_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.fillMaxWidth().padding(vertical = 24.dp),
            textAlign = TextAlign.Center,
        )
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(CellSize),
        modifier = modifier.fillMaxWidth().heightIn(max = GridMaxHeight),
    ) {
        items(results, key = { it }) { emoji ->
            EmojiPickerCell(
                emoji = emoji,
                isSelected = emoji == marked,
                onClick = { onToggle(emoji) },
            )
        }
    }
}

@Composable
internal fun RemoveEmojiButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(onClick = onClick, enabled = enabled, modifier = modifier.fillMaxWidth()) {
        Text(
            stringResource(Res.string.emoji_remove),
            color = if (enabled) MaterialTheme.colorScheme.error else Color.Unspecified,
        )
    }
}

@Composable
private fun EmojiPickerCell(
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background =
        if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            modifier =
                Modifier.size(CellSize)
                    .clip(CircleShape)
                    .background(background)
                    .clickable(role = Role.Button, onClick = onClick)
                    .semantics { this.selected = isSelected },
            contentAlignment = Alignment.Center,
        ) {
            Text(text = emoji, fontSize = 28.sp, maxLines = 1)
        }
    }
}
