package vn.id.tozydev.tusu.ui.feature.labels.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.time.Instant
import kotlin.uuid.Uuid
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.domain.model.Label
import vn.id.tozydev.tusu.domain.model.LabelGroup
import vn.id.tozydev.tusu.domain.model.LabelSelection
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.label_group_other
import vn.id.tozydev.tusu.generated.resources.label_hidden_badge
import vn.id.tozydev.tusu.generated.resources.label_new
import vn.id.tozydev.tusu.generated.resources.label_selection_single
import vn.id.tozydev.tusu.ui.theme.AppTheme

/**
 * One group of the label screen ("Khác" when [group] is null): its header, which opens the group
 * editor when [onGroupClick] is set, its labels and a chip that adds one.
 */
@Composable
fun LabelGroupSection(
    group: LabelGroup?,
    labels: List<Label>,
    onLabelClick: (Label) -> Unit,
    onNewLabel: () -> Unit,
    modifier: Modifier = Modifier,
    onGroupClick: (() -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .then(
                        if (onGroupClick != null) Modifier.clickable(onClick = onGroupClick)
                        else Modifier,
                    )
                    .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val color = group?.let { Color(it.color) } ?: MaterialTheme.colorScheme.outline
            Box(Modifier.size(12.dp).clip(CircleShape).background(color))
            Text(
                text = group?.name ?: stringResource(Res.string.label_group_other),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.alpha(if (group?.showInComposer == false) 0.6f else 1f),
            )
            if (group?.selection == LabelSelection.SINGLE) {
                Text(
                    text = stringResource(Res.string.label_selection_single),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            labels.forEach { label ->
                val hidden = stringResource(Res.string.label_hidden_badge)
                AssistChip(
                    onClick = { onLabelClick(label) },
                    label = { Text(if (label.isHidden) "${label.name} · $hidden" else label.name) },
                    modifier = Modifier.alpha(if (label.isHidden) 0.6f else 1f),
                )
            }
            AssistChip(
                onClick = onNewLabel,
                label = { Text("+ " + stringResource(Res.string.label_new)) },
            )
        }
    }
}

@Preview
@Composable
private fun LabelGroupSectionPreview() {
    val at = Instant.parse("2026-11-11T03:00:00Z")
    val group =
        LabelGroup(
            id = Uuid.random(),
            name = "Loại",
            color = 0xFF68A500.toInt(),
            selection = LabelSelection.SINGLE,
            sortOrder = 0,
            updatedAt = at,
        )
    AppTheme {
        Surface {
            LabelGroupSection(
                group = group,
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
                            groupId = group.id,
                            name = "Bài học",
                            isHidden = true,
                            sortOrder = 1,
                            updatedAt = at,
                        ),
                    ),
                onLabelClick = {},
                onNewLabel = {},
                onGroupClick = {},
            )
        }
    }
}
