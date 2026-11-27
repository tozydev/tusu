package vn.id.tozydev.tusu.ui.feature.entry.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.ui.component.MediaUi
import vn.id.tozydev.tusu.ui.component.VoicePlayer
import vn.id.tozydev.tusu.ui.format.ProvideAppDateTimeFormatter
import vn.id.tozydev.tusu.ui.theme.AppTheme

/** The voice notes of an entry, stacked as one group in album order. */
@Composable
fun EntryVoiceGroup(voices: List<MediaUi>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        voices.forEach { voice -> VoicePlayer(voice = voice, modifier = Modifier.fillMaxWidth()) }
    }
}

@Preview
@Composable
private fun EntryVoiceGroupPreview() {
    AppTheme {
        ProvideAppDateTimeFormatter {
            EntryVoiceGroup(
                voices =
                    List(3) { index ->
                        MediaUi(
                            id = Uuid.random(),
                            path = "",
                            type = MediaType.AUDIO,
                            status = MediaStatus.READY,
                            width = null,
                            height = null,
                            duration = (8 + index * 40).seconds,
                            waveform = List(48) { ((it * (23 + index * 7)) % 100) / 100f },
                        )
                    },
            )
        }
    }
}
