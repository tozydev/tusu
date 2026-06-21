package vn.io.tozyworks.tusu.ui.feature.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import vn.io.tozyworks.tusu.domain.model.Tag

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LimitedTagRow(
    tags: List<Tag>,
    modifier: Modifier = Modifier,
    maxTags: Int = 2,
) {
    if (tags.isEmpty()) return

    val itemsToRender = tags.take(maxTags)
    val totalRemaining = tags.size - itemsToRender.size

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        maxLines = 1,
    ) {
        itemsToRender.forEach { tag ->
            TagLabel(text = "#${tag.name}", modifier = Modifier.weight(1f, fill = false))
        }

        if (totalRemaining > 0) {
            TagLabel("+$totalRemaining", isBold = true)
        }
    }
}

@Composable
private fun TagLabel(
    text: String,
    modifier: Modifier = Modifier,
    isBold: Boolean = false,
) {
    Box(
        modifier =
            modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isBold) FontWeight.SemiBold else null,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
