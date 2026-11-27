package vn.id.tozydev.tusu.ui.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kdroidfilter.composemediaplayer.InitialPlayerState
import io.github.kdroidfilter.composemediaplayer.VideoPlayerState
import io.github.kdroidfilter.composemediaplayer.createVideoPlayerState
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.ic_pause_24px
import vn.id.tozydev.tusu.generated.resources.ic_play_arrow_24px
import vn.id.tozydev.tusu.generated.resources.voice_pause
import vn.id.tozydev.tusu.generated.resources.voice_play
import vn.id.tozydev.tusu.generated.resources.voice_playback_failed
import vn.id.tozydev.tusu.ui.format.LocalAppDateTimeFormatter
import vn.id.tozydev.tusu.ui.theme.AppTheme

// The player reports its position on a 0..1000 slider scale.
private const val SLIDER_MAX = 1000f
private const val END_FRACTION = 0.995f

/**
 * Plays one voice note: play/pause, its waveform as the position (tap to seek) and its time. The
 * player is created on the first play, so a feed of many voice notes holds no idle players, and
 * only one note plays at a time through [LocalVoicePlayback].
 */
@Composable
fun VoicePlayer(
    voice: MediaUi,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    val playback = LocalVoicePlayback.current
    var player by remember(voice.path) { mutableStateOf<VideoPlayerState?>(null) }
    DisposableEffect(player) {
        val current = player
        onDispose { current?.dispose() }
    }
    val current = player
    val playing = current?.isPlaying == true
    LaunchedEffect(playback.playingId, playing) {
        if (playing && playback.playingId != voice.id) current?.pause()
    }
    val progress = if (current?.hasMedia == true) current.sliderPos / SLIDER_MAX else 0f
    val failed = current?.error != null
    fun toggle() {
        val active = current?.takeUnless { failed }
        if (active == null) {
            playback.playingId = voice.id
            player =
                createVideoPlayerState().also {
                    it.openUri(fileUri(voice.path), InitialPlayerState.PLAY)
                }
            return
        }
        if (active.isPlaying) {
            active.pause()
        } else {
            playback.playingId = voice.id
            if (progress >= END_FRACTION) active.seekTo(0f)
            active.play()
        }
    }
    VoicePlayerRow(
        waveform = voice.waveform,
        progress = progress,
        playing = playing,
        time =
            if (current != null && progress > 0f) current.currentTime.seconds else voice.duration,
        failed = failed,
        onToggle = ::toggle,
        onSeek = { fraction -> current?.takeIf { it.hasMedia }?.seekTo(fraction * SLIDER_MAX) },
        trailing = trailing,
        modifier = modifier,
    )
}

@Composable
private fun VoicePlayerRow(
    waveform: List<Float>,
    progress: Float,
    playing: Boolean,
    time: Duration?,
    failed: Boolean,
    onToggle: () -> Unit,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilledTonalIconButton(onClick = onToggle) {
                Icon(
                    painter =
                        painterResource(
                            if (playing) Res.drawable.ic_pause_24px
                            else Res.drawable.ic_play_arrow_24px,
                        ),
                    contentDescription =
                        stringResource(
                            if (playing) Res.string.voice_pause else Res.string.voice_play,
                        ),
                )
            }
            Waveform(
                levels = waveform,
                progress = progress,
                modifier =
                    Modifier.weight(1f).height(32.dp).pointerInput(onSeek) {
                        detectTapGestures { offset -> onSeek(offset.x / size.width) }
                    },
            )
            VoiceTime(time = time, failed = failed)
            trailing?.invoke()
        }
    }
}

@Composable
private fun VoiceTime(time: Duration?, failed: Boolean, modifier: Modifier = Modifier) {
    if (failed) {
        Text(
            text = stringResource(Res.string.voice_playback_failed),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
            modifier = modifier.widthIn(max = 120.dp),
        )
        return
    }
    if (time == null) return
    Text(
        text = LocalAppDateTimeFormatter.current.duration(time),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.End,
        modifier = modifier.widthIn(min = 36.dp),
    )
}

private fun fileUri(path: String): String = if (path.startsWith("/")) "file://$path" else path

@Preview
@Composable
private fun VoicePlayerPreview() {
    AppTheme {
        VoicePlayer(
            voice =
                MediaUi(
                    id = Uuid.random(),
                    path = "",
                    type = MediaType.AUDIO,
                    status = MediaStatus.READY,
                    width = null,
                    height = null,
                    duration = 83.seconds,
                    waveform = List(48) { ((it * 37) % 100) / 100f },
                ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun VoicePlayerFailedPreview() {
    AppTheme {
        VoicePlayerRow(
            waveform = List(48) { ((it * 53) % 100) / 100f },
            progress = 0f,
            playing = false,
            time = 12.seconds,
            failed = true,
            onToggle = {},
            onSeek = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
