package vn.id.tozydev.tusu.ui.feature.entry.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.id.tozydev.tusu.ui.component.EmojiBadge
import vn.id.tozydev.tusu.ui.theme.AppTheme

/** The entry's emoji (when it has one) and its full date and time. */
@Composable
fun EntryDetailHeader(dateTime: String, modifier: Modifier = Modifier, emoji: String? = null) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (emoji != null) {
            EmojiBadge(emoji = emoji, size = 40.dp)
        }
        Text(text = dateTime, style = MaterialTheme.typography.titleMedium)
    }
}

@Preview
@Composable
private fun EntryDetailHeaderPreview() {
    AppTheme { EntryDetailHeader(emoji = "🌱", dateTime = "Thứ Ba, 10/11/2026 10:00") }
}

@Preview
@Composable
private fun EntryDetailHeaderWithoutEmojiPreview() {
    AppTheme { EntryDetailHeader(dateTime = "Tuesday, 10 Nov 2026 10:00") }
}
