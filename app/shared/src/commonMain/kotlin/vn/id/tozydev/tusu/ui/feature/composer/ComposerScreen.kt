package vn.id.tozydev.tusu.ui.feature.composer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mohamedrejeb.richeditor.model.RichTextState
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import vn.id.tozydev.tusu.ui.feature.composer.component.ComposerBody
import vn.id.tozydev.tusu.ui.feature.composer.component.ComposerTopBar
import vn.id.tozydev.tusu.ui.format.UiText
import vn.id.tozydev.tusu.ui.theme.AppTheme

/**
 * The full-screen composer. [safeDrawingPadding] already includes the keyboard inset, so the editor
 * stays above the keyboard without a separate `imePadding()`.
 */
@Composable
fun ComposerScreen(
    state: ComposerUiState,
    richTextState: RichTextState,
    onClose: () -> Unit,
    onRetrySave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            ComposerTopBar(
                occurredAt = state.occurredAt,
                timeZone = state.timeZone,
                saveIndicator = state.saveIndicator,
                onClose = onClose,
                onRetrySave = onRetrySave,
                modifier = Modifier.fillMaxWidth(),
            )
            ComposerBody(
                isLoading = state.isLoading,
                loadError = state.loadError,
                isReadOnly = state.isReadOnly,
                readOnlyText = state.readOnlyText,
                richTextState = richTextState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun ComposerScreenNewEntryPreview() {
    AppTheme {
        val richTextState = remember { RichTextState() }
        ComposerScreen(
            state =
                ComposerUiState(
                    isLoading = false,
                    occurredAt = Instant.parse("2026-11-11T03:00:00Z"),
                    timeZone = TimeZone.of("Asia/Ho_Chi_Minh"),
                ),
            richTextState = richTextState,
            onClose = {},
            onRetrySave = {},
        )
    }
}

@Preview
@Composable
private fun ComposerScreenSavingPreview() {
    AppTheme {
        val richTextState = remember { RichTextState() }
        ComposerScreen(
            state =
                ComposerUiState(
                    isLoading = false,
                    occurredAt = Instant.parse("2026-11-11T03:00:00Z"),
                    timeZone = TimeZone.of("Asia/Ho_Chi_Minh"),
                    saveIndicator = SaveIndicator.Saving,
                ),
            richTextState = richTextState,
            onClose = {},
            onRetrySave = {},
        )
    }
}

@Preview
@Composable
private fun ComposerScreenFailedPreview() {
    AppTheme {
        val richTextState = remember { RichTextState() }
        ComposerScreen(
            state =
                ComposerUiState(
                    isLoading = false,
                    occurredAt = Instant.parse("2026-11-11T03:00:00Z"),
                    timeZone = TimeZone.of("Asia/Ho_Chi_Minh"),
                    saveIndicator = SaveIndicator.Failed(UiText.of("Save failed")),
                ),
            richTextState = richTextState,
            onClose = {},
            onRetrySave = {},
        )
    }
}

@Preview
@Composable
private fun ComposerScreenReadOnlyPreview() {
    AppTheme {
        val richTextState = remember { RichTextState() }
        ComposerScreen(
            state =
                ComposerUiState(
                    isLoading = false,
                    occurredAt = Instant.parse("2026-11-11T03:00:00Z"),
                    timeZone = TimeZone.of("Asia/Ho_Chi_Minh"),
                    isReadOnly = true,
                    readOnlyText = "A note written by a newer version of Tusu.",
                ),
            richTextState = richTextState,
            onClose = {},
            onRetrySave = {},
        )
    }
}
