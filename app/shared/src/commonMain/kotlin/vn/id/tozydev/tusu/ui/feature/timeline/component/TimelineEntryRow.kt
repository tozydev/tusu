package vn.id.tozydev.tusu.ui.feature.timeline.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.ui.component.EmojiBadge
import vn.id.tozydev.tusu.ui.format.LocalAppDateTimeFormatter
import vn.id.tozydev.tusu.ui.theme.AppTheme

private const val MaxTextLines = 4

/** A flat, read-only row; not clickable until the entry detail screen exists. */
// TODO(TOZY-65): replace with the full entry card (rich text, photo grid, label chips).
@Composable
fun TimelineEntryRow(entry: Entry, modifier: Modifier = Modifier) {
    val formatter = LocalAppDateTimeFormatter.current
    // Each entry keeps the zone it was written in, so travelling does not shift its day.
    val dateTime = context(entry.timeZone) { formatter.fullDateTime(entry.occurredAt) }
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        if (entry.emoji != null) {
            EmojiBadge(emoji = entry.emoji, size = 40.dp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = dateTime,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (entry.textPlain.isNotBlank()) {
                Text(
                    text = entry.textPlain,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = MaxTextLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview
@Composable
private fun TimelineEntryRowPreview() {
    AppTheme {
        Surface { Column { sampleTimelineEntries().forEach { TimelineEntryRow(entry = it) } } }
    }
}
