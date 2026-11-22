package vn.id.tozydev.tusu.ui.feature.labels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.time.Instant
import kotlin.uuid.Uuid
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.domain.model.Label
import vn.id.tozydev.tusu.domain.model.LabelGroup
import vn.id.tozydev.tusu.domain.model.LabelSelection
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.action_back
import vn.id.tozydev.tusu.generated.resources.ic_arrow_back_24px
import vn.id.tozydev.tusu.generated.resources.label_delete_confirm
import vn.id.tozydev.tusu.generated.resources.label_group_delete_confirm
import vn.id.tozydev.tusu.generated.resources.labels_add_group
import vn.id.tozydev.tusu.generated.resources.labels_title
import vn.id.tozydev.tusu.ui.feature.labels.component.ConfirmDeleteDialog
import vn.id.tozydev.tusu.ui.feature.labels.component.GroupColors
import vn.id.tozydev.tusu.ui.feature.labels.component.GroupForm
import vn.id.tozydev.tusu.ui.feature.labels.component.LabelEditDialog
import vn.id.tozydev.tusu.ui.feature.labels.component.LabelGroupDialog
import vn.id.tozydev.tusu.ui.feature.labels.component.LabelGroupSection
import vn.id.tozydev.tusu.ui.feature.labels.component.NewLabelDialog
import vn.id.tozydev.tusu.ui.theme.AppTheme

/** Everything the label screen can ask for. */
class LabelsActions(
    val onBack: () -> Unit,
    val onCreateGroup: (name: String, color: Int, selection: LabelSelection) -> Unit,
    val onUpdateGroup: (LabelGroup) -> Unit,
    val onDeleteGroup: (Uuid) -> Unit,
    val onCreateLabel: (name: String, groupId: Uuid?) -> Unit,
    val onUpdateLabel: (Label) -> Unit,
    val onDeleteLabel: (Uuid) -> Unit,
)

/** The dialog open on the label screen, if any. */
private sealed interface LabelsDialog {
    data object NewGroup : LabelsDialog

    data class EditGroup(val group: LabelGroup) : LabelsDialog

    data class DeleteGroup(val id: Uuid) : LabelsDialog

    data class NewLabel(val groupId: Uuid?) : LabelsDialog

    data class EditLabel(val label: Label) : LabelsDialog

    data class DeleteLabel(val id: Uuid) : LabelsDialog
}

/** One part per group, then "Khác" with labels that have no live group. */
@Composable
fun LabelsScreen(state: LabelsUiState, actions: LabelsActions, modifier: Modifier = Modifier) {
    var dialog by remember { mutableStateOf<LabelsDialog?>(null) }
    val liveGroupIds = state.groups.map { it.id }.toSet()
    Surface(modifier = modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            LabelsTopBar(onBack = actions.onBack, onNewGroup = { dialog = LabelsDialog.NewGroup })
            LazyColumn(Modifier.fillMaxSize()) {
                items(state.groups, key = { it.id.toString() }) { group ->
                    LabelGroupSection(
                        group = group,
                        labels =
                            state.labels
                                .filter { it.groupId == group.id }
                                .sortedBy { it.sortOrder },
                        onLabelClick = { dialog = LabelsDialog.EditLabel(it) },
                        onNewLabel = { dialog = LabelsDialog.NewLabel(group.id) },
                        onGroupClick = { dialog = LabelsDialog.EditGroup(group) },
                    )
                }
                item(key = "other") {
                    LabelGroupSection(
                        group = null,
                        labels =
                            state.labels
                                .filter { it.groupId == null || it.groupId !in liveGroupIds }
                                .sortedBy { it.sortOrder },
                        onLabelClick = { dialog = LabelsDialog.EditLabel(it) },
                        onNewLabel = { dialog = LabelsDialog.NewLabel(null) },
                    )
                }
            }
        }
    }
    LabelsDialogHost(
        dialog = dialog,
        groups = state.groups,
        actions = actions,
        onChange = { dialog = it },
    )
}

@Composable
private fun LabelsTopBar(
    onBack: () -> Unit,
    onNewGroup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_back_24px),
                contentDescription = stringResource(Res.string.action_back),
            )
        }
        Text(
            text = stringResource(Res.string.labels_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onNewGroup) { Text(stringResource(Res.string.labels_add_group)) }
    }
}

@Composable
private fun LabelsDialogHost(
    dialog: LabelsDialog?,
    groups: List<LabelGroup>,
    actions: LabelsActions,
    onChange: (LabelsDialog?) -> Unit,
) {
    val close = { onChange(null) }
    when (dialog) {
        null -> Unit
        LabelsDialog.NewGroup ->
            LabelGroupDialog(
                initial = GroupForm("", GroupColors.first(), LabelSelection.MULTI, true),
                onConfirm = {
                    close()
                    actions.onCreateGroup(it.name, it.color, it.selection)
                },
                onDismiss = close,
            )
        is LabelsDialog.EditGroup -> {
            val group = dialog.group
            LabelGroupDialog(
                initial = GroupForm(group.name, group.color, group.selection, group.showInComposer),
                onConfirm = {
                    close()
                    actions.onUpdateGroup(
                        group.copy(
                            name = it.name,
                            color = it.color,
                            selection = it.selection,
                            showInComposer = it.showInComposer,
                        ),
                    )
                },
                onDismiss = close,
                onDelete = { onChange(LabelsDialog.DeleteGroup(group.id)) },
            )
        }
        is LabelsDialog.DeleteGroup ->
            ConfirmDeleteDialog(
                message = stringResource(Res.string.label_group_delete_confirm),
                onConfirm = {
                    close()
                    actions.onDeleteGroup(dialog.id)
                },
                onDismiss = close,
            )
        is LabelsDialog.NewLabel ->
            NewLabelDialog(
                onConfirm = {
                    close()
                    actions.onCreateLabel(it, dialog.groupId)
                },
                onDismiss = close,
            )
        is LabelsDialog.EditLabel ->
            LabelEditDialog(
                label = dialog.label,
                groups = groups,
                onConfirm = {
                    close()
                    actions.onUpdateLabel(it)
                },
                onDelete = { onChange(LabelsDialog.DeleteLabel(dialog.label.id)) },
                onDismiss = close,
            )
        is LabelsDialog.DeleteLabel ->
            ConfirmDeleteDialog(
                message = stringResource(Res.string.label_delete_confirm),
                onConfirm = {
                    close()
                    actions.onDeleteLabel(dialog.id)
                },
                onDismiss = close,
            )
    }
}

@Preview
@Composable
private fun LabelsScreenPreview() {
    val at = Instant.parse("2026-11-11T03:00:00Z")
    val group =
        LabelGroup(
            id = Uuid.random(),
            name = "Loại",
            color = 0xFF68A500.toInt(),
            sortOrder = 0,
            updatedAt = at,
        )
    AppTheme {
        LabelsScreen(
            state =
                LabelsUiState(
                    isLoading = false,
                    groups = listOf(group),
                    labels =
                        listOf(
                            Label(
                                id = Uuid.random(),
                                groupId = group.id,
                                name = "Kỷ niệm",
                                sortOrder = 0,
                                updatedAt = at,
                            ),
                            Label(
                                id = Uuid.random(),
                                name = "Cà phê",
                                sortOrder = 0,
                                updatedAt = at,
                            ),
                        ),
                ),
            actions = LabelsActions({}, { _, _, _ -> }, {}, {}, { _, _ -> }, {}, {}),
        )
    }
}
