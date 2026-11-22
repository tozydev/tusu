package vn.id.tozydev.tusu.ui.feature.labels.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.time.Instant
import kotlin.uuid.Uuid
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.domain.model.Label
import vn.id.tozydev.tusu.domain.model.LabelGroup
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.action_cancel
import vn.id.tozydev.tusu.generated.resources.action_delete
import vn.id.tozydev.tusu.generated.resources.action_save
import vn.id.tozydev.tusu.generated.resources.label_edit
import vn.id.tozydev.tusu.generated.resources.label_group_field
import vn.id.tozydev.tusu.generated.resources.label_group_other
import vn.id.tozydev.tusu.generated.resources.label_hidden
import vn.id.tozydev.tusu.generated.resources.label_new_name
import vn.id.tozydev.tusu.ui.theme.AppTheme

/** Renames [label], moves it to another group ("Khác" is null), hides it or deletes it. */
@Composable
fun LabelEditDialog(
    label: Label,
    groups: List<LabelGroup>,
    onConfirm: (Label) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(label.name) }
    var groupKey by rememberSaveable { mutableStateOf(label.groupId?.toString()) }
    var hidden by rememberSaveable { mutableStateOf(label.isHidden) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.label_edit)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(Res.string.label_new_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    stringResource(Res.string.label_group_field),
                    style = MaterialTheme.typography.labelLarge,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    groups.forEach { group ->
                        FilterChip(
                            selected = groupKey == group.id.toString(),
                            onClick = { groupKey = group.id.toString() },
                            label = { Text(group.name) },
                        )
                    }
                    FilterChip(
                        selected = groupKey == null,
                        onClick = { groupKey = null },
                        label = { Text(stringResource(Res.string.label_group_other)) },
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(Res.string.label_hidden), modifier = Modifier.weight(1f))
                    Switch(checked = hidden, onCheckedChange = { hidden = it })
                }
                TextButton(onClick = onDelete) {
                    Text(
                        stringResource(Res.string.action_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        label.copy(
                            name = name,
                            groupId = groupKey?.let(Uuid::parse),
                            isHidden = hidden,
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

@Preview
@Composable
private fun LabelEditDialogPreview() {
    val at = Instant.parse("2026-11-11T03:00:00Z")
    val group =
        LabelGroup(id = Uuid.random(), name = "Loại", color = 0, sortOrder = 0, updatedAt = at)
    AppTheme {
        LabelEditDialog(
            label =
                Label(
                    id = Uuid.random(),
                    groupId = group.id,
                    name = "Kỷ niệm",
                    sortOrder = 0,
                    updatedAt = at,
                ),
            groups = listOf(group),
            onConfirm = {},
            onDelete = {},
            onDismiss = {},
        )
    }
}
