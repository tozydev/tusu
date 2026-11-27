package vn.id.tozydev.tusu.ui.feature.composer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
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
import kotlin.uuid.Uuid
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.ui.component.DateTimePickerDialog
import vn.id.tozydev.tusu.ui.component.EmojiPickerRoute
import vn.id.tozydev.tusu.ui.component.MediaUi
import vn.id.tozydev.tusu.ui.component.labelChipsOf
import vn.id.tozydev.tusu.ui.feature.composer.component.ComposerBody
import vn.id.tozydev.tusu.ui.feature.composer.component.ComposerEmojiRow
import vn.id.tozydev.tusu.ui.feature.composer.component.ComposerPhotoStrip
import vn.id.tozydev.tusu.ui.feature.composer.component.ComposerToolbar
import vn.id.tozydev.tusu.ui.feature.composer.component.ComposerTopBar
import vn.id.tozydev.tusu.ui.feature.composer.component.ComposerVoiceList
import vn.id.tozydev.tusu.ui.feature.composer.component.LabelPickerSheet
import vn.id.tozydev.tusu.ui.feature.composer.component.RecordingBar
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
    actions: ComposerActions,
    modifier: Modifier = Modifier,
) {
    var formatOpen by rememberSaveable { mutableStateOf(false) }
    var emojiSheetOpen by rememberSaveable { mutableStateOf(false) }
    var labelSheetOpen by rememberSaveable { mutableStateOf(false) }
    var timeDialogOpen by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val editable = !state.isLoading && state.loadError == null && !state.isReadOnly
    ReturnFocusWhenClosed(
        sheetOpen = emojiSheetOpen || labelSheetOpen,
        focusRequester = focusRequester,
    )
    Surface(modifier = modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            ComposerTopBar(
                occurredAt = state.occurredAt,
                timeZone = state.timeZone,
                saveIndicator = state.saveIndicator,
                onClose = actions.onClose,
                onRetrySave = actions.onRetrySave,
                onTimeClick = { timeDialogOpen = true },
                timeEnabled = editable,
                modifier = Modifier.fillMaxWidth(),
            )
            if (editable) {
                val otherColor = MaterialTheme.colorScheme.outline
                val chips =
                    remember(state.selectedLabelIds, state.labels, state.labelGroups, otherColor) {
                        labelChipsOf(
                            state.labels.filter { it.id in state.selectedLabelIds },
                            state.labelGroups,
                            otherColor,
                        )
                    }
                ComposerEmojiRow(
                    emoji = state.emoji,
                    onClick = { emojiSheetOpen = true },
                    labels = chips,
                    onLabelsClick = { labelSheetOpen = true },
                )
            }
            ComposerBody(
                isLoading = state.isLoading,
                loadError = state.loadError,
                isReadOnly = state.isReadOnly,
                readOnlyText = state.readOnlyText,
                richTextState = richTextState,
                focusRequester = focusRequester,
                header = {
                    PhotoStrip(state.photos, actions)
                    VoiceList(state.voices, actions)
                },
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
            val recording = state.recording
            when {
                !editable -> Unit
                recording != null ->
                    RecordingBar(
                        recording = recording,
                        onCancel = actions.onCancelRecording,
                        onStop = actions.onStopRecording,
                    )
                else ->
                    ComposerToolbar(
                        formatOpen = formatOpen,
                        onFormatToggle = { formatOpen = !formatOpen },
                        onPickPhotosClick = actions.onPickPhotos,
                        onTakePhotoClick = actions.onTakePhoto,
                        onRecordClick = actions.onRecord,
                        formatBar = { RichTextFormatBar(richTextState) },
                    )
            }
        }
    }
    if (labelSheetOpen) {
        LabelPickerSheet(
            groups = state.labelGroups,
            labels = state.labels,
            selectedIds = state.selectedLabelIds,
            onToggle = actions.onLabelToggle,
            onCreate = actions.onCreateLabel,
            onManage = {
                labelSheetOpen = false
                actions.onManageLabels()
            },
            onDismiss = { labelSheetOpen = false },
        )
    }
    if (emojiSheetOpen) {
        EmojiPickerRoute(
            selected = state.emoji,
            onSelect = {
                actions.onEmojiChange(it)
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
                actions.onOccurredAtChange(it)
                timeDialogOpen = false
            },
            onDismiss = { timeDialogOpen = false },
        )
    }
}

/** The photo strip above the text; empty when there are no photos, so nothing is shown. */
@Composable
private fun PhotoStrip(photos: List<MediaUi>, actions: ComposerActions) {
    if (photos.isEmpty()) return
    ComposerPhotoStrip(
        photos = photos,
        onRemove = actions.onRemoveMedia,
        onRetry = actions.onRetryPhoto,
    )
}

/** The voice notes under the photos; nothing when the entry has none. */
@Composable
private fun VoiceList(voices: List<MediaUi>, actions: ComposerActions) {
    if (voices.isEmpty()) return
    ComposerVoiceList(voices = voices, onRemove = actions.onRemoveMedia)
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

private val previewInstant = Instant.parse("2026-11-11T03:00:00Z")
private val previewZone = TimeZone.of("Asia/Ho_Chi_Minh")

@Preview
@Composable
private fun ComposerScreenNewEntryPreview() {
    AppTheme {
        ComposerScreen(
            state =
                ComposerUiState(
                    isLoading = false,
                    occurredAt = previewInstant,
                    timeZone = previewZone,
                    currentTimeZone = previewZone,
                ),
            richTextState = remember { RichTextState() },
            actions = ComposerActions.NoOp,
        )
    }
}

@Preview
@Composable
private fun ComposerScreenWithEmojiPreview() {
    AppTheme {
        ComposerScreen(
            state =
                ComposerUiState(
                    isLoading = false,
                    occurredAt = previewInstant,
                    timeZone = previewZone,
                    emoji = "🌱",
                ),
            richTextState = remember { RichTextState() },
            actions = ComposerActions.NoOp,
        )
    }
}

@Preview
@Composable
private fun ComposerScreenWithPhotosPreview() {
    AppTheme {
        ComposerScreen(
            state =
                ComposerUiState(
                    isLoading = false,
                    occurredAt = previewInstant,
                    timeZone = previewZone,
                    photos =
                        listOf(
                            previewPhoto(MediaStatus.READY),
                            previewPhoto(MediaStatus.PROCESSING),
                            previewPhoto(MediaStatus.FAILED),
                        ),
                ),
            richTextState = remember { RichTextState() },
            actions = ComposerActions.NoOp,
        )
    }
}

@Preview
@Composable
private fun ComposerScreenSavingPreview() {
    AppTheme {
        ComposerScreen(
            state =
                ComposerUiState(
                    isLoading = false,
                    occurredAt = previewInstant,
                    timeZone = previewZone,
                    saveIndicator = SaveIndicator.Saving,
                ),
            richTextState = remember { RichTextState() },
            actions = ComposerActions.NoOp,
        )
    }
}

@Preview
@Composable
private fun ComposerScreenFailedPreview() {
    AppTheme {
        ComposerScreen(
            state =
                ComposerUiState(
                    isLoading = false,
                    occurredAt = previewInstant,
                    timeZone = previewZone,
                    saveIndicator = SaveIndicator.Failed(UiText.of("Save failed")),
                ),
            richTextState = remember { RichTextState() },
            actions = ComposerActions.NoOp,
        )
    }
}

@Preview
@Composable
private fun ComposerScreenReadOnlyPreview() {
    AppTheme {
        ComposerScreen(
            state =
                ComposerUiState(
                    isLoading = false,
                    occurredAt = previewInstant,
                    timeZone = previewZone,
                    isReadOnly = true,
                    readOnlyText = "A note written by a newer version of Tusu.",
                ),
            richTextState = remember { RichTextState() },
            actions = ComposerActions.NoOp,
        )
    }
}

private fun previewPhoto(status: MediaStatus): MediaUi =
    MediaUi(
        id = Uuid.random(),
        path = "",
        type = MediaType.PHOTO,
        status = status,
        width = null,
        height = null,
    )
