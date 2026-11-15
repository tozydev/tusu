package vn.id.tozydev.tusu.ui.feature.composer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import com.mohamedrejeb.richeditor.model.RichTextState
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import vn.id.tozydev.tusu.ui.component.DateTimePickerDialog
import vn.id.tozydev.tusu.ui.component.EmojiPickerSheet
import vn.id.tozydev.tusu.ui.feature.composer.component.ComposerBody
import vn.id.tozydev.tusu.ui.feature.composer.component.ComposerEmojiRow
import vn.id.tozydev.tusu.ui.feature.composer.component.ComposerToolbar
import vn.id.tozydev.tusu.ui.feature.composer.component.ComposerTopBar
import vn.id.tozydev.tusu.ui.feature.composer.component.RichTextFormatBar
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
    onEmojiChange: (String?) -> Unit,
    onOccurredAtChange: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    var formatOpen by rememberSaveable { mutableStateOf(false) }
    var emojiSheetOpen by rememberSaveable { mutableStateOf(false) }
    var timeDialogOpen by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val editable = !state.isLoading && state.loadError == null && !state.isReadOnly
    ReturnFocusWhenClosed(sheetOpen = emojiSheetOpen, focusRequester = focusRequester)
    Surface(modifier = modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            ComposerTopBar(
                occurredAt = state.occurredAt,
                timeZone = state.timeZone,
                saveIndicator = state.saveIndicator,
                onClose = onClose,
                onRetrySave = onRetrySave,
                onTimeClick = { timeDialogOpen = true },
                timeEnabled = editable,
                modifier = Modifier.fillMaxWidth(),
            )
            if (editable) {
                ComposerEmojiRow(emoji = state.emoji, onClick = { emojiSheetOpen = true })
            }
            ComposerBody(
                isLoading = state.isLoading,
                loadError = state.loadError,
                isReadOnly = state.isReadOnly,
                readOnlyText = state.readOnlyText,
                richTextState = richTextState,
                focusRequester = focusRequester,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
            if (editable) {
                ComposerToolbar(
                    formatOpen = formatOpen,
                    onFormatToggle = { formatOpen = !formatOpen },
                    formatBar = { RichTextFormatBar(richTextState) },
                )
            }
        }
    }
    if (emojiSheetOpen) {
        EmojiPickerSheet(
            selected = state.emoji,
            onSelect = {
                onEmojiChange(it)
                emojiSheetOpen = false
            },
            onDismiss = { emojiSheetOpen = false },
        )
    }
    val occurredAt = state.occurredAt
    val currentTimeZone = state.currentTimeZone
    if (timeDialogOpen && occurredAt != null && currentTimeZone != null) {
        // The picker works in the current zone, the same zone the ViewModel converts back with.
        DateTimePickerDialog(
            initial = occurredAt.toLocalDateTime(currentTimeZone),
            onConfirm = {
                onOccurredAtChange(it)
                timeDialogOpen = false
            },
            onDismiss = { timeDialogOpen = false },
        )
    }
}

/** Gives the editor focus, and the keyboard with it, once the emoji sheet has closed. */
@Composable
private fun ReturnFocusWhenClosed(sheetOpen: Boolean, focusRequester: FocusRequester) {
    val keyboard = LocalSoftwareKeyboardController.current
    // Saved so a rotation while the sheet is open still returns focus when it closes.
    var wasOpen by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(sheetOpen) {
        if (sheetOpen) {
            wasOpen = true
        } else if (wasOpen) {
            wasOpen = false
            focusRequester.requestFocus()
            keyboard?.show()
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
                    currentTimeZone = TimeZone.of("Asia/Ho_Chi_Minh"),
                ),
            richTextState = richTextState,
            onClose = {},
            onRetrySave = {},
            onEmojiChange = {},
            onOccurredAtChange = {},
        )
    }
}

@Preview
@Composable
private fun ComposerScreenWithEmojiPreview() {
    AppTheme {
        val richTextState = remember { RichTextState() }
        ComposerScreen(
            state =
                ComposerUiState(
                    isLoading = false,
                    occurredAt = Instant.parse("2026-11-11T03:00:00Z"),
                    timeZone = TimeZone.of("Asia/Ho_Chi_Minh"),
                    emoji = "🌱",
                ),
            richTextState = richTextState,
            onClose = {},
            onRetrySave = {},
            onEmojiChange = {},
            onOccurredAtChange = {},
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
            onEmojiChange = {},
            onOccurredAtChange = {},
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
            onEmojiChange = {},
            onOccurredAtChange = {},
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
            onEmojiChange = {},
            onOccurredAtChange = {},
        )
    }
}
