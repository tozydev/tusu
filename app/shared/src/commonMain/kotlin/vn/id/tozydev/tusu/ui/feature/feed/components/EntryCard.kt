package vn.id.tozydev.tusu.ui.feature.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.domain.model.Tag
import vn.id.tozydev.tusu.ui.feature.feed.util.parseContentPreview
import vn.id.tozydev.tusu.ui.formatter.DateTimeFormatter

@Composable
context(dateTimeFormatter: DateTimeFormatter)
fun EntryCard(
    entry: Entry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formattedTime =
        remember(entry.recordedAt) {
            dateTimeFormatter.formatTime(entry.recordedAt)
        }

    val contentPreview =
        remember(entry.content) {
            parseContentPreview(entry.content)
        }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            EntryCardHeader(formattedTime)

            Spacer(modifier = Modifier.height(12.dp))

            // Content Preview
            EntryCardBody(contentPreview, entry)

            if (entry.emoji != null || entry.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                EntryCardFooter(entry.emoji, entry.tags)
            }
        }
    }
}

@Composable
private fun EntryCardBody(contentPreview: AnnotatedString, entry: Entry) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = contentPreview,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    lineHeightStyle =
                        LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.Both,
                        )
                ),
            color = MaterialTheme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis,
            maxLines = 5,
        )
        AdaptiveMediaGrid(entry.media)
    }
}

@Composable
private fun EntryCardFooter(
    emoji: String?,
    tags: List<Tag>,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        if (emoji != null) {
            Box(
                modifier =
                    Modifier.background(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(8.dp),
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        LimitedTagRow(tags = tags, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun EntryCardHeader(time: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
    }
}
