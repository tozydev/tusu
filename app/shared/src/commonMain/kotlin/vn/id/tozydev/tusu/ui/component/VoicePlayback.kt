package vn.id.tozydev.tusu.ui.component

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.uuid.Uuid

/** Which voice note is playing, so starting one pauses the others. */
@Stable
class VoicePlayback {
    var playingId: Uuid? by mutableStateOf(null)
}

/** One coordinator for the whole app unless a screen provides its own. */
val LocalVoicePlayback = staticCompositionLocalOf { VoicePlayback() }
