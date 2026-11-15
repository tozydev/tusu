package vn.id.tozydev.tusu.ui.feature.timeline.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.id.tozydev.tusu.ui.theme.AppTheme

private val RailWidth = 52.dp
private val TimeHeight = 28.dp
private val LineWidth = 1.dp

/**
 * The time of an entry, with a thin line below it down to the bottom of the row. Fills the row's
 * height, so the row must be laid out with an intrinsic height.
 */
@Composable
fun TimelineRail(time: String, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    val density = LocalDensity.current
    val lineTop = with(density) { TimeHeight.toPx() }
    val lineWidth = with(density) { LineWidth.toPx() }
    Column(
        modifier =
            modifier.width(RailWidth).fillMaxHeight().drawBehind {
                val x = size.width / 2
                drawLine(
                    color = lineColor,
                    start = Offset(x, lineTop),
                    end = Offset(x, size.height),
                    strokeWidth = lineWidth,
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.height(TimeHeight), contentAlignment = Alignment.Center) {
            Text(
                text = time,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun TimelineRailPreview() {
    AppTheme { Row(modifier = Modifier.height(96.dp)) { TimelineRail(time = "08:30") } }
}
