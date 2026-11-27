package vn.id.tozydev.tusu.ui.feature.composer.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.composer_remove_voice
import vn.id.tozydev.tusu.generated.resources.ic_close_24px
import vn.id.tozydev.tusu.ui.component.MediaUi
import vn.id.tozydev.tusu.ui.component.VoicePlayer
import vn.id.tozydev.tusu.ui.theme.AppTheme

/** The recorded voice notes of the entry, each playable and removable. */
@Composable
fun ComposerVoiceList(
    voices: List<MediaUi>,
    onRemove: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        voices.forEach { voice ->
            VoicePlayer(
                voice = voice,
                modifier = Modifier.fillMaxWidth(),
                trailing = {
                    IconButton(onClick = { onRemove(voice.id) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_close_24px),
                            contentDescription = stringResource(Res.string.composer_remove_voice),
                        )
                    }
                },
            )
        }
    }
}

@Preview
@Composable
private fun ComposerVoiceListPreview() {
    AppTheme {
        ComposerVoiceList(
            voices =
                List(2) { index ->
                    MediaUi(
                        id = Uuid.random(),
                        path = "",
                        type = MediaType.AUDIO,
                        status = MediaStatus.READY,
                        width = null,
                        height = null,
                        duration = (12 + index * 50).seconds,
                        waveform = List(48) { ((it * (31 + index * 6)) % 100) / 100f },
                    )
                },
            onRemove = {},
        )
    }
}
