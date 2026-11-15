package vn.id.tozydev.tusu.ui.feature.composer.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
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
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.composer_format_description
import vn.id.tozydev.tusu.generated.resources.composer_format_toggle
import vn.id.tozydev.tusu.ui.theme.AppTheme

/**
 * The bar under the editor. [formatBar] opens above the row when [formatOpen] is true; [leading]
 * holds buttons at the start of the row for later features.
 */
@Composable
fun ComposerToolbar(
    formatOpen: Boolean,
    onFormatToggle: () -> Unit,
    modifier: Modifier = Modifier,
    formatBar: @Composable () -> Unit = {},
    leading: @Composable RowScope.() -> Unit = {},
) {
    Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
        Column {
            AnimatedVisibility(visible = formatOpen) { formatBar() }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leading()
                Spacer(Modifier.weight(1f))
                FormatToggleButton(checked = formatOpen, onClick = onFormatToggle)
            }
        }
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

@Preview
@Composable
private fun ComposerToolbarPreview() {
    AppTheme { ComposerToolbar(formatOpen = false, onFormatToggle = {}) }
}

@Preview
@Composable
private fun ComposerToolbarFormatOpenPreview() {
    AppTheme {
        ComposerToolbar(
            formatOpen = true,
            onFormatToggle = {},
            formatBar = {
                FormatBar(
                    bold = true,
                    italic = false,
                    strikethrough = true,
                    bulletList = false,
                    numberedList = true,
                    onBoldClick = {},
                    onItalicClick = {},
                    onStrikethroughClick = {},
                    onBulletListClick = {},
                    onNumberedListClick = {},
                )
            },
        )
    }
}
