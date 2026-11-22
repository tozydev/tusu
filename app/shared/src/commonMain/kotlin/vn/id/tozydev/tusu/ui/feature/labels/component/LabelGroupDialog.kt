package vn.id.tozydev.tusu.ui.feature.labels.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.domain.model.LabelSelection
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.action_cancel
import vn.id.tozydev.tusu.generated.resources.action_delete
import vn.id.tozydev.tusu.generated.resources.action_save
import vn.id.tozydev.tusu.generated.resources.label_group_color
import vn.id.tozydev.tusu.generated.resources.label_group_edit
import vn.id.tozydev.tusu.generated.resources.label_group_name
import vn.id.tozydev.tusu.generated.resources.label_group_show_in_composer
import vn.id.tozydev.tusu.generated.resources.label_selection_multi
import vn.id.tozydev.tusu.generated.resources.label_selection_single
import vn.id.tozydev.tusu.generated.resources.labels_add_group
import vn.id.tozydev.tusu.ui.theme.AppTheme

/** Colours offered for a group, as ARGB. */
internal val GroupColors: List<Int> =
    listOf(
            0xFF68A500,
            0xFF1E88E5,
            0xFFE53935,
            0xFFFB8C00,
            0xFF8E24AA,
            0xFF00897B,
            0xFF6D4C41,
            0xFF546E7A,
        )
        .map { it.toInt() }

/** What the group dialog edits; the screen turns it into a create or an update. */
data class GroupForm(
    val name: String,
    val color: Int,
    val selection: LabelSelection,
    val showInComposer: Boolean,
)

/**
 * Creates a group when [onDelete] is null, otherwise edits one: name, colour, single or multiple
 * choice and whether the composer shows it.
 */
@Composable
fun LabelGroupDialog(
    initial: GroupForm,
    onConfirm: (GroupForm) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var color by rememberSaveable { mutableStateOf(initial.color) }
    var single by rememberSaveable { mutableStateOf(initial.selection == LabelSelection.SINGLE) }
    var shown by rememberSaveable { mutableStateOf(initial.showInComposer) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (onDelete == null) Res.string.labels_add_group
                    else Res.string.label_group_edit,
                ),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(Res.string.label_group_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    stringResource(Res.string.label_group_color),
                    style = MaterialTheme.typography.labelLarge,
                )
                ColorRow(selected = color, onSelect = { color = it })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !single,
                        onClick = { single = false },
                        label = { Text(stringResource(Res.string.label_selection_multi)) },
                    )
                    FilterChip(
                        selected = single,
                        onClick = { single = true },
                        label = { Text(stringResource(Res.string.label_selection_single)) },
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(Res.string.label_group_show_in_composer),
                        modifier = Modifier.weight(1f),
                    )
                    Switch(checked = shown, onCheckedChange = { shown = it })
                }
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text(
                            stringResource(Res.string.action_delete),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        GroupForm(
                            name = name,
                            color = color,
                            selection = if (single) LabelSelection.SINGLE else LabelSelection.MULTI,
                            showInComposer = shown,
                        ),
                    )
                },
                enabled = name.isNotBlank(),
            ) {
                Text(stringResource(Res.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) }
        },
    )
}

@Composable
private fun ColorRow(selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    FlowRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        GroupColors.forEach { argb ->
            val isSelected = argb == selected
            Box(
                Modifier.size(32.dp)
                    .clip(CircleShape)
                    .background(Color(argb))
                    .then(
                        if (isSelected) {
                            Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        } else Modifier,
                    )
                    .clickable(role = Role.RadioButton) { onSelect(argb) }
                    .semantics { this.selected = isSelected },
            )
        }
    }
}

@Preview
@Composable
private fun LabelGroupDialogPreview() {
    AppTheme {
        LabelGroupDialog(
            initial = GroupForm("Loại", GroupColors.first(), LabelSelection.MULTI, true),
            onConfirm = {},
            onDismiss = {},
            onDelete = {},
        )
    }
}
