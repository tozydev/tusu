package vn.id.tozydev.tusu.ui.feature.composer.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.time.Instant
import kotlin.uuid.Uuid
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.domain.model.Label
import vn.id.tozydev.tusu.domain.model.LabelGroup
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.action_cancel
import vn.id.tozydev.tusu.generated.resources.action_ok
import vn.id.tozydev.tusu.generated.resources.label_group_other
import vn.id.tozydev.tusu.generated.resources.label_new
import vn.id.tozydev.tusu.generated.resources.label_new_name
import vn.id.tozydev.tusu.generated.resources.label_picker_title
import vn.id.tozydev.tusu.generated.resources.labels_manage
import vn.id.tozydev.tusu.ui.theme.AppTheme

/** One part of the sheet: a group, or "Khác" when [group] is null. */
private data class LabelSection(val group: LabelGroup?, val labels: List<Label>)

/**
 * Labels by group, groups hidden from the composer left out. Each part ends with a chip that
 * creates a label in it. [onToggle] gets the tapped label's id.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelPickerSheet(
    groups: List<LabelGroup>,
    labels: List<Label>,
    selectedIds: Set<Uuid>,
    onToggle: (Uuid) -> Unit,
    onCreate: (name: String, groupId: Uuid?) -> Unit,
    onDismiss: () -> Unit,
    onManage: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        LabelPickerContent(groups, labels, selectedIds, onToggle, onCreate, onManage)
    }
}

@Composable
private fun LabelPickerContent(
    groups: List<LabelGroup>,
    labels: List<Label>,
    selectedIds: Set<Uuid>,
    onToggle: (Uuid) -> Unit,
    onCreate: (name: String, groupId: Uuid?) -> Unit,
    onManage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Saved as a string: null means no dialog, "" means a new label in "Khác".
    var creatingIn by rememberSaveable { mutableStateOf<String?>(null) }
    val sections = labelSections(groups, labels)
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(max = 480.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.label_picker_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onManage) { Text(stringResource(Res.string.labels_manage)) }
        }
        sections.forEach { section ->
            LabelSectionView(
                section = section,
                selectedIds = selectedIds,
                onToggle = onToggle,
                onNewLabel = { creatingIn = section.group?.id?.toString() ?: "" },
            )
        }
    }
    creatingIn?.let { groupKey ->
        NewLabelDialog(
            onConfirm = { name ->
                creatingIn = null
                onCreate(name, groupKey.takeIf { it.isNotEmpty() }?.let(Uuid::parse))
            },
            onDismiss = { creatingIn = null },
        )
    }
}

private fun labelSections(groups: List<LabelGroup>, labels: List<Label>): List<LabelSection> {
    val shown = labels.filter { !it.isHidden && it.deletedAt == null }
    val visibleGroups =
        groups.filter { it.showInComposer && it.deletedAt == null }.sortedBy { it.sortOrder }
    val liveGroupIds = groups.filter { it.deletedAt == null }.map { it.id }.toSet()
    return visibleGroups.map { group ->
        LabelSection(group, shown.filter { it.groupId == group.id }.sortedBy { it.sortOrder })
    } +
        LabelSection(
            null,
            shown
                .filter { it.groupId == null || it.groupId !in liveGroupIds }
                .sortedBy { it.sortOrder },
        )
}

@Composable
private fun LabelSectionView(
    section: LabelSection,
    selectedIds: Set<Uuid>,
    onToggle: (Uuid) -> Unit,
    onNewLabel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val color = section.group?.let { Color(it.color) } ?: MaterialTheme.colorScheme.outline
            Box(Modifier.size(10.dp).clip(CircleShape).background(color))
            Text(
                text = section.group?.name ?: stringResource(Res.string.label_group_other),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            section.labels.forEach { label ->
                FilterChip(
                    selected = label.id in selectedIds,
                    onClick = { onToggle(label.id) },
                    label = { Text(label.name) },
                )
            }
            AssistChip(
                onClick = onNewLabel,
                label = { Text("+ " + stringResource(Res.string.label_new)) },
            )
        }
    }
}

@Composable
private fun NewLabelDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.label_new)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(Res.string.label_new_name)) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) {
                Text(stringResource(Res.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_cancel)) }
        },
    )
}

@Preview
@Composable
private fun LabelPickerContentPreview() {
    val at = Instant.parse("2026-11-11T03:00:00Z")
    val kind =
        LabelGroup(
            id = Uuid.random(),
            name = "Loại",
            color = 0xFF68A500.toInt(),
            sortOrder = 0,
            updatedAt = at,
        )
    val memory =
        Label(
            id = Uuid.random(),
            groupId = kind.id,
            name = "Kỷ niệm",
            sortOrder = 0,
            updatedAt = at,
        )
    val lesson =
        Label(
            id = Uuid.random(),
            groupId = kind.id,
            name = "Bài học",
            sortOrder = 1,
            updatedAt = at,
        )
    val loose = Label(id = Uuid.random(), name = "Cà phê", sortOrder = 0, updatedAt = at)
    AppTheme {
        Surface {
            LabelPickerContent(
                groups = listOf(kind),
                labels = listOf(memory, lesson, loose),
                selectedIds = setOf(memory.id),
                onToggle = {},
                onCreate = { _, _ -> },
                onManage = {},
            )
        }
    }
}
