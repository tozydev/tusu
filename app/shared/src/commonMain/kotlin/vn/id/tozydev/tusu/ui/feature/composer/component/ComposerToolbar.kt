package vn.id.tozydev.tusu.ui.feature.composer.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.composer_format_description
import vn.id.tozydev.tusu.generated.resources.composer_format_toggle
import vn.id.tozydev.tusu.generated.resources.composer_hide_keyboard
import vn.id.tozydev.tusu.generated.resources.ic_keyboard_hide_24px
import vn.id.tozydev.tusu.ui.theme.AppTheme

/**
 * The bar that sits on the keyboard while the user types: the attach buttons, then Aa and a button
 * that hides the keyboard. Aa swaps the attach buttons for [formatBar]. No button takes focus, so
 * the keyboard stays open.
 */
@Composable
fun ComposerToolbar(
    actions: List<AttachAction>,
    formatOpen: Boolean,
    onFormatToggle: () -> Unit,
    onHideKeyboard: () -> Unit,
    modifier: Modifier = Modifier,
    formatBar: @Composable () -> Unit = {},
) {
    Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedContent(targetState = formatOpen, modifier = Modifier.weight(1f)) { open ->
                if (open) {
                    formatBar()
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        actions.forEach { ToolbarButton(it) }
                    }
                }
            }
            FormatToggleButton(checked = formatOpen, onClick = onFormatToggle)
            IconButton(
                onClick = onHideKeyboard,
                modifier = Modifier.focusProperties { canFocus = false },
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_keyboard_hide_24px),
                    contentDescription = stringResource(Res.string.composer_hide_keyboard),
                )
            }
        }
    }
}

@Composable
private fun ToolbarButton(action: AttachAction, modifier: Modifier = Modifier) {
    IconButton(
        onClick = action.onClick,
        modifier = modifier.focusProperties { canFocus = false },
    ) {
        Icon(
            painter = painterResource(action.icon),
            contentDescription = stringResource(action.label),
        )
    }
}

@Composable
private fun FormatToggleButton(
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(Res.string.composer_format_description)
    TextButton(
        onClick = onClick,
        modifier =
            modifier
                .focusProperties { canFocus = false }
                .semantics {
                    contentDescription = description
                    selected = checked
                },
        colors =
            ButtonDefaults.textButtonColors(
                contentColor =
                    if (checked) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            ),
    ) {
        Text(text = stringResource(Res.string.composer_format_toggle))
    }
}

private val previewActions =
    attachActions(onTakePhoto = {}, onPickPhotos = {}, onRecord = {}, onAddLocation = {})

@Preview
@Composable
private fun ComposerToolbarPreview() {
    AppTheme {
        ComposerToolbar(
            actions = previewActions,
            formatOpen = false,
            onFormatToggle = {},
            onHideKeyboard = {},
        )
    }
}

@Preview
@Composable
private fun ComposerToolbarFormatOpenPreview() {
    AppTheme {
        ComposerToolbar(
            actions = previewActions,
            formatOpen = true,
            onFormatToggle = {},
            onHideKeyboard = {},
            formatBar = { FormatBar(state = FormatState(setOf(FormatAction.Bold)), onAction = {}) },
        )
    }
}
