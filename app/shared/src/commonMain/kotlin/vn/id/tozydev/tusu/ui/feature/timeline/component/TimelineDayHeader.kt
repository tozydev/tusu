package vn.id.tozydev.tusu.ui.feature.timeline.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.timeline_day_more_emojis
import vn.id.tozydev.tusu.ui.theme.AppTheme

private const val MaxHeaderEmojis = 6

/**
 * Sticky header of one day. It is opaque so the entries scroll under it, and it shows at most
 * [MaxHeaderEmojis] emojis of the day followed by a count of the rest.
 */
@Composable
fun TimelineDayHeader(title: String, emojis: List<String>, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            if (emojis.isNotEmpty()) {
                DayEmojis(emojis = emojis)
            }
        }
    }
}

@Composable
private fun DayEmojis(emojis: List<String>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        emojis.take(MaxHeaderEmojis).forEach { emoji ->
            Text(text = emoji, style = MaterialTheme.typography.titleMedium)
        }
        val hidden = emojis.size - MaxHeaderEmojis
        if (hidden > 0) {
            Text(
                text = stringResource(Res.string.timeline_day_more_emojis, hidden),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun TimelineDayHeaderFewEmojisPreview() {
    AppTheme { TimelineDayHeader(title = "Today", emojis = listOf("☕", "🌙")) }
}

@Preview
@Composable
private fun TimelineDayHeaderManyEmojisPreview() {
    AppTheme {
        TimelineDayHeader(
            title = "Yesterday",
            emojis = listOf("☕", "🌙", "🌱", "📚", "🎉", "🍜", "🌧️", "🚲", "🎵"),
        )
    }
}

@Preview
@Composable
private fun TimelineDayHeaderNoEmojisPreview() {
    AppTheme { TimelineDayHeader(title = "Monday, 9 November", emojis = emptyList()) }
}
