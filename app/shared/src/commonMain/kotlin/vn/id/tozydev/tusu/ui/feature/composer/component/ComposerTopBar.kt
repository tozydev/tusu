package vn.id.tozydev.tusu.ui.feature.composer.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.action_close
import vn.id.tozydev.tusu.generated.resources.ic_close_24px
import vn.id.tozydev.tusu.ui.feature.composer.SaveIndicator
import vn.id.tozydev.tusu.ui.format.LocalAppDateTimeFormatter
import vn.id.tozydev.tusu.ui.theme.AppTheme

@Composable
fun ComposerTopBar(
    occurredAt: Instant?,
    timeZone: TimeZone?,
    saveIndicator: SaveIndicator,
    onClose: () -> Unit,
    onRetrySave: () -> Unit,
    modifier: Modifier = Modifier,
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
        // TODO(TOZY-63): tap to change the date and time.
        Text(
            text = dateTime,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
        )
        SaveIndicatorIcon(indicator = saveIndicator, onRetry = onRetrySave)
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
            )
        }
    }
}
