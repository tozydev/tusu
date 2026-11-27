package vn.id.tozydev.tusu.ui.feature.composer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.composer_show_keyboard
import vn.id.tozydev.tusu.generated.resources.ic_keyboard_24px
import vn.id.tozydev.tusu.ui.component.DateTimePickerDialog
import vn.id.tozydev.tusu.ui.component.EmojiPickerRoute
import vn.id.tozydev.tusu.ui.component.MediaUi
import vn.id.tozydev.tusu.ui.component.labelChipsOf
import vn.id.tozydev.tusu.ui.feature.composer.component.AttachAction
import vn.id.tozydev.tusu.ui.feature.composer.component.ComposerBody
import vn.id.tozydev.tusu.ui.feature.composer.component.ComposerEmojiRow
import vn.id.tozydev.tusu.ui.feature.composer.component.ComposerFabMenu
import vn.id.tozydev.tusu.ui.feature.composer.component.ComposerLocation
import vn.id.tozydev.tusu.ui.feature.composer.component.ComposerPhotoStrip
import vn.id.tozydev.tusu.ui.feature.composer.component.ComposerToolbar
import vn.id.tozydev.tusu.ui.feature.composer.component.ComposerTopBar
import vn.id.tozydev.tusu.ui.feature.composer.component.ComposerVoiceList
import vn.id.tozydev.tusu.ui.feature.composer.component.LabelPickerSheet
import vn.id.tozydev.tusu.ui.feature.composer.component.LocationPickerSheet
import vn.id.tozydev.tusu.ui.feature.composer.component.RecordingBar
import vn.id.tozydev.tusu.ui.feature.composer.component.RichTextFormatBar
import vn.id.tozydev.tusu.ui.feature.composer.component.attachActions
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
    var locationSheetOpen by rememberSaveable { mutableStateOf(false) }
    var timeDialogOpen by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val editable = !state.isLoading && state.loadError == null && !state.isReadOnly
    ReturnFocusWhenClosed(
        sheetOpen = emojiSheetOpen || labelSheetOpen || locationSheetOpen,
        focusRequester = focusRequester,
    )
    // The keyboard inset is the common way to tell; desktop has no on-screen keyboard.
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val attach =
        attachActions(
            onTakePhoto = actions.onTakePhoto,
            onPickPhotos = actions.onPickPhotos,
            onRecord = actions.onRecord,
            onAddLocation = if (state.location == null) ({ locationSheetOpen = true }) else null,
        )
    Surface(modifier = modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            ComposerContent(
                state = state,
                richTextState = richTextState,
                actions = actions,
                editable = editable,
                imeVisible = imeVisible,
                attach = attach,
                formatOpen = formatOpen,
                onFormatToggle = { formatOpen = !formatOpen },
                focusRequester = focusRequester,
                onEmojiClick = { emojiSheetOpen = true },
                onLabelsClick = { labelSheetOpen = true },
                onLocationClick = { locationSheetOpen = true },
                onTimeClick = { timeDialogOpen = true },
            )
            // Over the content, so opening the menu does not push the editor up.
            if (editable && state.recording == null && !imeVisible) {
                ComposerFabMenu(
                    actions = attach,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                )
            }
        }
    }
    ComposerSheets(
        state = state,
        actions = actions,
        emojiSheetOpen = emojiSheetOpen,
        labelSheetOpen = labelSheetOpen,
        locationSheetOpen = locationSheetOpen,
        timeDialogOpen = timeDialogOpen,
        onEmojiSheetClose = { emojiSheetOpen = false },
        onLabelSheetClose = { labelSheetOpen = false },
        onLocationSheetClose = { locationSheetOpen = false },
        onTimeDialogClose = { timeDialogOpen = false },
    )
}

/** The top bar, the pinned emoji and place rows, the editor and the bar under it. */
@Composable
private fun ComposerContent(
    state: ComposerUiState,
    richTextState: RichTextState,
    actions: ComposerActions,
    editable: Boolean,
    imeVisible: Boolean,
    attach: List<AttachAction>,
    formatOpen: Boolean,
    onFormatToggle: () -> Unit,
    focusRequester: FocusRequester,
    onEmojiClick: () -> Unit,
    onLabelsClick: () -> Unit,
    onLocationClick: () -> Unit,
    onTimeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        ComposerTopBar(
            occurredAt = state.occurredAt,
            timeZone = state.timeZone,
            saveIndicator = state.saveIndicator,
            onClose = actions.onClose,
            onRetrySave = actions.onRetrySave,
            onTimeClick = onTimeClick,
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
                onClick = onEmojiClick,
                labels = chips,
                onLabelsClick = onLabelsClick,
            )
            // Pinned with the emoji row: the editor scrolls itself into view when it takes
            // focus, which would push a place at the top of the scrolling part out of sight.
            ComposerLocation(
                location = state.location,
                isLocating = state.isLocating,
                suggestedLocation = state.suggestedLocation,
                onOpen = onLocationClick,
                onRemove = { actions.onLocationChange(null) },
                onAcceptSuggestion = actions.onLocationChange,
                modifier = Modifier.padding(horizontal = 20.dp),
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
        if (editable) {
            ComposerBottomBar(
                recording = state.recording,
                imeVisible = imeVisible,
                attach = attach,
                formatOpen = formatOpen,
                onFormatToggle = onFormatToggle,
                richTextState = richTextState,
                focusRequester = focusRequester,
                actions = actions,
            )
        }
    }
}

/** The sheets and dialogs the composer opens over itself. */
@Composable
private fun ComposerSheets(
    state: ComposerUiState,
    actions: ComposerActions,
    emojiSheetOpen: Boolean,
    labelSheetOpen: Boolean,
    locationSheetOpen: Boolean,
    timeDialogOpen: Boolean,
    onEmojiSheetClose: () -> Unit,
    onLabelSheetClose: () -> Unit,
    onLocationSheetClose: () -> Unit,
    onTimeDialogClose: () -> Unit,
) {
    if (locationSheetOpen) {
        LocationPickerSheet(
            current = state.location,
            recent = state.recentLocations,
            onUseCurrent = actions.onUseCurrentLocation,
            onPick = actions.onLocationChange,
            onDismiss = onLocationSheetClose,
        )
    }
    if (labelSheetOpen) {
        LabelPickerSheet(
            groups = state.labelGroups,
            labels = state.labels,
            selectedIds = state.selectedLabelIds,
            onToggle = actions.onLabelToggle,
            onCreate = actions.onCreateLabel,
            onManage = {
                onLabelSheetClose()
                actions.onManageLabels()
            },
            onDismiss = onLabelSheetClose,
        )
    }
    if (emojiSheetOpen) {
        EmojiPickerRoute(
            selected = state.emoji,
            onSelect = {
                actions.onEmojiChange(it)
                onEmojiSheetClose()
            },
            onDismiss = onEmojiSheetClose,
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
                onTimeDialogClose()
            },
            onDismiss = onTimeDialogClose,
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

/**
 * What sits under the editor: the recording bar while recording, the toolbar on the keyboard while
 * typing, and otherwise a button that brings the keyboard back next to the "+" menu.
 */
@Composable
private fun ComposerBottomBar(
    recording: RecordingUi?,
    imeVisible: Boolean,
    attach: List<AttachAction>,
    formatOpen: Boolean,
    onFormatToggle: () -> Unit,
    richTextState: RichTextState,
    focusRequester: FocusRequester,
    actions: ComposerActions,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    when {
        recording != null ->
            RecordingBar(
                recording = recording,
                onCancel = actions.onCancelRecording,
                onStop = actions.onStopRecording,
                modifier = modifier,
            )
        imeVisible ->
            ComposerToolbar(
                actions = attach,
                formatOpen = formatOpen,
                onFormatToggle = onFormatToggle,
                onHideKeyboard = {
                    keyboard?.hide()
                    focusManager.clearFocus()
                },
                formatBar = { RichTextFormatBar(richTextState) },
                modifier = modifier,
            )
        else ->
            Row(
                modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        focusRequester.requestFocus()
                        keyboard?.show()
                    },
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_keyboard_24px),
                        contentDescription = stringResource(Res.string.composer_show_keyboard),
                    )
                }
            }
    }
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
