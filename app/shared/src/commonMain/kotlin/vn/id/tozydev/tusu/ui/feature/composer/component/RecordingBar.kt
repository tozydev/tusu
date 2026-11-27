package vn.id.tozydev.tusu.ui.feature.composer.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.composer_recording
import vn.id.tozydev.tusu.generated.resources.composer_recording_cancel
import vn.id.tozydev.tusu.generated.resources.composer_recording_stop
import vn.id.tozydev.tusu.generated.resources.ic_delete_24px
import vn.id.tozydev.tusu.generated.resources.ic_stop_24px
import vn.id.tozydev.tusu.ui.component.Waveform
import vn.id.tozydev.tusu.ui.feature.composer.RecordingUi
import vn.id.tozydev.tusu.ui.format.LocalAppDateTimeFormatter
import vn.id.tozydev.tusu.ui.format.ProvideAppDateTimeFormatter
import vn.id.tozydev.tusu.ui.theme.AppTheme

private val TICK = 200.milliseconds

/** Bars the live level graph always has, so it fills from the right as the voice comes in. */
private const val LIVE_BARS = 40

/** Takes the toolbar's place while recording: discard, the clock, live levels and stop. */
@Composable
fun RecordingBar(
    recording: RecordingUi,
    onCancel: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var elapsed by
        remember(recording.startedAt) { mutableStateOf(recording.startedAt.elapsedNow()) }
    LaunchedEffect(recording.startedAt) {
        while (true) {
            elapsed = recording.startedAt.elapsedNow()
            delay(TICK)
        }
    }
    RecordingBarContent(
        elapsed = elapsed,
        levels = recording.levels,
        onCancel = onCancel,
        onStop = onStop,
        modifier = modifier,
    )
}

@Composable
private fun RecordingBarContent(
    elapsed: Duration,
    levels: List<Float>,
    onCancel: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(
                onClick = onCancel,
                modifier = Modifier.focusProperties { canFocus = false },
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_delete_24px),
                    contentDescription = stringResource(Res.string.composer_recording_cancel),
                )
            }
            RecordingClock(elapsed)
            val padded = List((LIVE_BARS - levels.size).coerceAtLeast(0)) { 0f } + levels
            Waveform(
                levels = padded.takeLast(LIVE_BARS),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f).height(28.dp),
            )
            FilledIconButton(
                onClick = onStop,
                modifier = Modifier.focusProperties { canFocus = false },
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_stop_24px),
                    contentDescription = stringResource(Res.string.composer_recording_stop),
                )
            }
        }
    }
}

@Composable
private fun RecordingClock(elapsed: Duration, modifier: Modifier = Modifier) {
    val label = stringResource(Res.string.composer_recording)
    val text = LocalAppDateTimeFormatter.current.duration(elapsed)
    Row(
        modifier = modifier.semantics { contentDescription = "$label $text" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.error, CircleShape))
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.widthIn(min = 40.dp),
        )
    }
}

@Preview
@Composable
private fun RecordingBarPreview() {
    AppTheme {
        ProvideAppDateTimeFormatter {
            RecordingBar(
                recording =
                    RecordingUi(
                        startedAt = TimeSource.Monotonic.markNow(),
                        levels = List(24) { ((it * 41) % 100) / 100f },
                    ),
                onCancel = {},
                onStop = {},
            )
        }
    }
}
