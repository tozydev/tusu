package vn.id.tozydev.tusu.ui.feature.composer.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.action_close
import vn.id.tozydev.tusu.generated.resources.action_redo
import vn.id.tozydev.tusu.generated.resources.action_undo
import vn.id.tozydev.tusu.generated.resources.composer_change_time
import vn.id.tozydev.tusu.generated.resources.ic_close_24px
import vn.id.tozydev.tusu.generated.resources.ic_redo_24px
import vn.id.tozydev.tusu.generated.resources.ic_schedule_24px
import vn.id.tozydev.tusu.generated.resources.ic_undo_24px
import vn.id.tozydev.tusu.ui.feature.composer.SaveIndicator
import vn.id.tozydev.tusu.ui.format.LocalAppDateTimeFormatter
import vn.id.tozydev.tusu.ui.theme.AppTheme

/** The bar above the editor; tapping the date-time text calls [onTimeClick] when [timeEnabled]. */
@Composable
fun ComposerTopBar(
    occurredAt: Instant?,
    timeZone: TimeZone?,
    saveIndicator: SaveIndicator,
    onClose: () -> Unit,
    onRetrySave: () -> Unit,
    onTimeClick: () -> Unit,
    timeEnabled: Boolean,
    modifier: Modifier = Modifier,
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
) {
    val formatter = LocalAppDateTimeFormatter.current
    val dateTime =
        if (occurredAt != null && timeZone != null) {
            context(timeZone) { formatter.fullDateTime(occurredAt) }
        } else {
            ""
        }
    Row(
        modifier = modifier.padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose) {
            Icon(
                painter = painterResource(Res.drawable.ic_close_24px),
                contentDescription = stringResource(Res.string.action_close),
            )
        }
        ComposerTimeButton(
            text = dateTime,
            enabled = timeEnabled,
            onClick = onTimeClick,
            modifier = Modifier.weight(1f),
        )
        // Shown from the first change on, so an untouched entry keeps a quiet bar.
        if (canUndo || canRedo) {
            HistoryButton(Res.drawable.ic_undo_24px, Res.string.action_undo, canUndo, onUndo)
            HistoryButton(Res.drawable.ic_redo_24px, Res.string.action_redo, canRedo, onRedo)
        }
        SaveIndicatorIcon(indicator = saveIndicator, onRetry = onRetrySave)
    }
}

@Composable
private fun HistoryButton(
    icon: DrawableResource,
    label: StringResource,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Taking focus would close the keyboard in the middle of typing.
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.focusProperties { canFocus = false },
    ) {
        Icon(painter = painterResource(icon), contentDescription = stringResource(label))
    }
}

@Composable
private fun ComposerTimeButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(Res.string.composer_change_time)
    TextButton(onClick = onClick, enabled = enabled, modifier = modifier) {
        Icon(
            painter = painterResource(Res.drawable.ic_schedule_24px),
            contentDescription = description,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview
@Composable
private fun ComposerTopBarPreview() {
    AppTheme {
        Surface {
            ComposerTopBar(
                occurredAt = Instant.parse("2026-11-11T03:00:00Z"),
                timeZone = TimeZone.of("Asia/Ho_Chi_Minh"),
                saveIndicator = SaveIndicator.Saved,
                onClose = {},
                onRetrySave = {},
                onTimeClick = {},
                timeEnabled = true,
            )
        }
    }
}

@Preview
@Composable
private fun ComposerTopBarReadOnlyPreview() {
    AppTheme {
        Surface {
            ComposerTopBar(
                occurredAt = Instant.parse("2026-11-11T03:00:00Z"),
                timeZone = TimeZone.of("Asia/Ho_Chi_Minh"),
                saveIndicator = SaveIndicator.Hidden,
                onClose = {},
                onRetrySave = {},
                onTimeClick = {},
                timeEnabled = false,
            )
        }
    }
}
