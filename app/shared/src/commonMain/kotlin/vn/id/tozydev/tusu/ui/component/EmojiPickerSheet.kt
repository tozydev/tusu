package vn.id.tozydev.tusu.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.action_close
import vn.id.tozydev.tusu.generated.resources.emoji_picker_title
import vn.id.tozydev.tusu.generated.resources.ic_close_24px
import vn.id.tozydev.tusu.ui.theme.AppTheme

private val CellSize = 48.dp
private val GridMaxHeight = 360.dp

/**
 * Bottom sheet with a grid of emoji. Tapping an emoji calls [onSelect] with it; tapping the one
 * that is already [selected] calls [onSelect] with null to clear it. The caller closes the sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiPickerSheet(
    selected: String?,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        dragHandle = null,
    ) {
        EmojiPickerContent(selected = selected, onSelect = onSelect, onDismiss = onDismiss)
    }
}

/** Sheet body without the sheet window, so it can be previewed on its own. */
@Composable
internal fun EmojiPickerContent(
    selected: String?,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
        EmojiPickerHeader(onDismiss = onDismiss)
        EmojiPickerGrid(selected = selected, onSelect = onSelect)
    }
}

@Composable
private fun EmojiPickerHeader(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(Res.string.emoji_picker_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDismiss) {
            Icon(
                painter = painterResource(Res.drawable.ic_close_24px),
                contentDescription = stringResource(Res.string.action_close),
            )
        }
    }
}

@Composable
private fun EmojiPickerGrid(
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(CellSize),
        modifier = modifier.fillMaxWidth().heightIn(max = GridMaxHeight),
    ) {
        items(EmojiCatalog) { emoji ->
            val isSelected = emoji == selected
            EmojiPickerCell(
                emoji = emoji,
                isSelected = isSelected,
                onClick = { onSelect(if (isSelected) null else emoji) },
            )
        }
    }
}

@Composable
private fun EmojiPickerCell(
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background: Color =
        if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
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

@Preview
@Composable
private fun EmojiPickerContentPreview() {
    AppTheme { Surface { EmojiPickerContent(selected = null, onSelect = {}, onDismiss = {}) } }
}

@Preview
@Composable
private fun EmojiPickerContentSelectedPreview() {
    AppTheme { Surface { EmojiPickerContent(selected = "😊", onSelect = {}, onDismiss = {}) } }
}
