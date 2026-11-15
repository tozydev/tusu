package vn.id.tozydev.tusu.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.pluralStringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.label_chip_more_description
import vn.id.tozydev.tusu.ui.theme.AppTheme

/** Shows the first [maxVisible] labels as chips and the rest as a single "+N" chip. */
@Composable
fun LabelChipRow(
    labels: List<LabelChipData>,
    modifier: Modifier = Modifier,
    maxVisible: Int = 2,
) {
    require(maxVisible >= 0) { "maxVisible must not be negative" }
    val hidden = labels.size - maxVisible
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        labels.take(maxVisible).forEach { LabelChip(it) }
        if (hidden > 0) {
            val description =
                pluralStringResource(Res.plurals.label_chip_more_description, hidden, hidden)
            ChipSurface(
                container = MaterialTheme.colorScheme.surfaceContainerHighest,
                content = MaterialTheme.colorScheme.onSurfaceVariant,
                text = "+$hidden",
                modifier = Modifier.semantics { contentDescription = description },
            )
        }
    }
}

private val PreviewLabels =
    listOf(
        LabelChipData("Gratitude", Color(0xFF68A500)),
        LabelChipData("Trip", Color(0xFF1E88E5)),
        LabelChipData("Family", Color(0xFFE53935)),
        LabelChipData("Work", Color(0xFF8E24AA)),
    )

@Preview
@Composable
private fun LabelChipRowPreview() {
    AppTheme {
        Surface {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LabelChipRow(PreviewLabels.take(1))
                LabelChipRow(PreviewLabels.take(2))
                LabelChipRow(PreviewLabels)
                LabelChipRow(PreviewLabels, maxVisible = 3)
            }
        }
    }
}
