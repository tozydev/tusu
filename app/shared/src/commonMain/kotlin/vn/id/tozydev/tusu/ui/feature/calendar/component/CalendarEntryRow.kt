package vn.id.tozydev.tusu.ui.feature.calendar.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.id.tozydev.tusu.ui.component.EmojiBadge
import vn.id.tozydev.tusu.ui.theme.AppTheme

private const val MaxPreviewLines = 2

/** One entry of the selected day: its time, emoji and the start of its text. Opens the entry. */
@Composable
fun CalendarEntryRow(
    time: String,
    emoji: String?,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(44.dp).padding(top = 2.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (emoji != null) {
                EmojiBadge(emoji = emoji, size = 28.dp)
            }
            if (text.isNotBlank()) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = MaxPreviewLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview
@Composable
private fun CalendarEntryRowPreview() {
    AppTheme {
        CalendarEntryRow(
            time = "08:15",
            emoji = "☀️",
            text = "Buổi sáng đi bộ quanh hồ, trời trong và mát.",
            onClick = {},
        )
    }
}
