package vn.id.tozydev.tusu.ui.feature.entry.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.entry_action_change_time
import vn.id.tozydev.tusu.generated.resources.entry_action_delete
import vn.id.tozydev.tusu.generated.resources.entry_action_edit
import vn.id.tozydev.tusu.generated.resources.entry_created_at
import vn.id.tozydev.tusu.generated.resources.entry_updated_at
import vn.id.tozydev.tusu.generated.resources.ic_delete_24px
import vn.id.tozydev.tusu.generated.resources.ic_filled_edit_24px
import vn.id.tozydev.tusu.generated.resources.ic_schedule_24px
import vn.id.tozydev.tusu.ui.theme.AppTheme

/**
 * The "more" actions of an entry, in a bottom sheet with the default drag handle. [createdAt] and
 * [updatedAt] are already formatted; [updatedAt] is null when the entry was never edited.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryMoreSheet(
    createdAt: String?,
    updatedAt: String?,
    onEdit: () -> Unit,
    onChangeTime: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        EntryMoreSheetContent(
            createdAt = createdAt,
            updatedAt = updatedAt,
            onEdit = onEdit,
            onChangeTime = onChangeTime,
            onDelete = onDelete,
        )
    }
}

/** The rows of [EntryMoreSheet]; separate so they can be previewed without the sheet window. */
@Composable
internal fun EntryMoreSheetContent(
    createdAt: String?,
    updatedAt: String?,
    onEdit: () -> Unit,
    onChangeTime: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 16.dp)) {
        EntryTimestamps(
            createdAt = createdAt,
            updatedAt = updatedAt,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
        )
        EntryMoreRow(
            icon = Res.drawable.ic_filled_edit_24px,
            label = Res.string.entry_action_edit,
            onClick = onEdit,
        )
        EntryMoreRow(
            icon = Res.drawable.ic_schedule_24px,
            label = Res.string.entry_action_change_time,
            onClick = onChangeTime,
        )
        EntryMoreRow(
            icon = Res.drawable.ic_delete_24px,
            label = Res.string.entry_action_delete,
            onClick = onDelete,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun EntryTimestamps(createdAt: String?, updatedAt: String?, modifier: Modifier = Modifier) {
    if (createdAt == null && updatedAt == null) return
    Column(modifier = modifier.fillMaxWidth()) {
        val style = MaterialTheme.typography.bodySmall
        val color = MaterialTheme.colorScheme.onSurfaceVariant
        if (createdAt != null) {
            Text(
                text = stringResource(Res.string.entry_created_at, createdAt),
                style = style,
                color = color,
            )
        }
        if (updatedAt != null) {
            Text(
                text = stringResource(Res.string.entry_updated_at, updatedAt),
                style = style,
                color = color,
            )
        }
    }
}

@Composable
private fun EntryMoreRow(
    icon: DrawableResource,
    label: StringResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painter = painterResource(icon), contentDescription = null, tint = color)
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

@Preview
@Composable
private fun EntryMoreSheetContentPreview() {
    AppTheme {
        EntryMoreSheetContent(
            createdAt = "Thứ Ba, 10/11/2026 10:00",
            updatedAt = "Thứ Tư, 11/11/2026 08:30",
            onEdit = {},
            onChangeTime = {},
            onDelete = {},
        )
    }
}
