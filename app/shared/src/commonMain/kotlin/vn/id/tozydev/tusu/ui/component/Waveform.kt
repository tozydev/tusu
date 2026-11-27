package vn.id.tozydev.tusu.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.id.tozydev.tusu.ui.theme.AppTheme

private const val MIN_BAR_FRACTION = 0.12f

/**
 * Vertical bars for audio [levels] in 0..1, spread over the full width. Bars before [progress]
 * (0..1) use [playedColor], so the same drawing serves as a playback position.
 */
@Composable
fun Waveform(
    levels: List<Float>,
    modifier: Modifier = Modifier,
    progress: Float = 0f,
    playedColor: Color = MaterialTheme.colorScheme.primary,
    color: Color = MaterialTheme.colorScheme.outline,
) {
    Canvas(modifier = modifier) {
        if (levels.isEmpty()) return@Canvas
        val slot = size.width / levels.size
        val barWidth = (slot * 0.6f).coerceAtMost(4.dp.toPx())
        levels.forEachIndexed { index, level ->
            val x = slot * index + slot / 2
            val barHeight = size.height * level.coerceIn(MIN_BAR_FRACTION, 1f)
            val top = (size.height - barHeight) / 2
            drawLine(
                color = if ((index + 0.5f) / levels.size <= progress) playedColor else color,
                start = Offset(x, top),
                end = Offset(x, top + barHeight),
                strokeWidth = barWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Preview
@Composable
private fun WaveformPreview() {
    AppTheme {
        Waveform(
            levels = List(48) { ((it * 37) % 100) / 100f },
            progress = 0.4f,
            modifier = Modifier.fillMaxWidth().height(32.dp),
        )
    }
}
