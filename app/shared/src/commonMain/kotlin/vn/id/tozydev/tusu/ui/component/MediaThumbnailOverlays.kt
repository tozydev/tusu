package vn.id.tozydev.tusu.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.ic_mic_24px
import vn.id.tozydev.tusu.generated.resources.ic_play_arrow_24px
import vn.id.tozydev.tusu.generated.resources.ic_refresh_24px
import vn.id.tozydev.tusu.generated.resources.media_failed_retry
import vn.id.tozydev.tusu.generated.resources.media_processing

private const val WaveformBars = 14
private const val ScrimAlpha = 0.45f

@Composable
internal fun DurationBadge(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.6f),
        contentColor = Color.White,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
internal fun PlayGlyph(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(36.dp),
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.5f),
        contentColor = Color.White,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(painterResource(Res.drawable.ic_play_arrow_24px), null, Modifier.size(24.dp))
        }
    }
}

/** Tile for voice recordings: the waveform when known, otherwise a microphone. */
@Composable
internal fun AudioTile(waveform: List<Float>, modifier: Modifier = Modifier) {
    val barColor = MaterialTheme.colorScheme.onSecondaryContainer
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (waveform.isEmpty()) {
            Icon(
                painter = painterResource(Res.drawable.ic_mic_24px),
                contentDescription = null,
                tint = barColor,
                modifier = Modifier.size(32.dp),
            )
        } else {
            val bars = sampleBars(waveform)
            Box(
                Modifier.fillMaxWidth(0.7f).fillMaxHeight(0.4f).drawBehind {
                    val step = size.width / bars.size
                    val barWidth = step * 0.55f
                    bars.forEachIndexed { index, amplitude ->
                        val height = (size.height * amplitude).coerceAtLeast(barWidth)
                        drawRoundRect(
                            color = barColor,
                            topLeft =
                                Offset(
                                    index * step + (step - barWidth) / 2,
                                    (size.height - height) / 2,
                                ),
                            size = Size(barWidth, height),
                            cornerRadius = CornerRadius(barWidth / 2),
                        )
                    }
                },
            )
        }
    }
}

/** Picks [WaveformBars] evenly spaced amplitudes so the tile looks the same at any length. */
internal fun sampleBars(waveform: List<Float>): List<Float> =
    List(WaveformBars) { index ->
        waveform[(index * waveform.size / WaveformBars).coerceAtMost(waveform.lastIndex)].coerceIn(
            0f,
            1f,
        )
    }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ProcessingOverlay(modifier: Modifier = Modifier) {
    val description = stringResource(Res.string.media_processing)
    Box(
        modifier =
            modifier.background(Color.Black.copy(alpha = ScrimAlpha)).semantics {
                contentDescription = description
            },
        contentAlignment = Alignment.Center,
    ) {
        LoadingIndicator(Modifier.size(40.dp), color = Color.White)
    }
}

@Composable
internal fun FailedOverlay(onRetry: (() -> Unit)?, modifier: Modifier = Modifier) {
    val description = stringResource(Res.string.media_failed_retry)
    val clickable =
        if (onRetry != null) Modifier.clickable(role = Role.Button, onClick = onRetry) else Modifier
    Box(
        modifier =
            modifier
                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f))
                .then(clickable)
                .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_refresh_24px),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.size(32.dp),
        )
    }
}
