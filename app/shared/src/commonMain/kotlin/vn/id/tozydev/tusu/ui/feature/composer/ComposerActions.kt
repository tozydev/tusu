package vn.id.tozydev.tusu.ui.feature.composer

import androidx.compose.runtime.Immutable
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDateTime
import vn.id.tozydev.tusu.domain.model.EntryLocation

/**
 * Everything the composer screen can ask for; grouped so the screen keeps a short parameter list.
 */
@Immutable
class ComposerActions(
    val onClose: () -> Unit,
    val onRetrySave: () -> Unit,
    val onEmojiChange: (String?) -> Unit,
    val onOccurredAtChange: (LocalDateTime) -> Unit,
    val onPickPhotos: () -> Unit,
    /** Null on platforms without a camera, which hides the camera button. */
    val onTakePhoto: (() -> Unit)?,
    /** Removes a photo or a voice note. */
    val onRemoveMedia: (Uuid) -> Unit,
    val onRetryPhoto: (Uuid) -> Unit,
    /** Asks for the microphone when needed, then starts recording. */
    val onRecord: () -> Unit = {},
    val onStopRecording: () -> Unit = {},
    val onCancelRecording: () -> Unit = {},
    /** Sets or (with null) removes the place. */
    val onLocationChange: (EntryLocation?) -> Unit = {},
    /** Asks for the location permission when needed, then looks up where the user is. */
    val onUseCurrentLocation: () -> Unit = {},
    val onUndo: () -> Unit = {},
    val onRedo: () -> Unit = {},
    val onLabelToggle: (Uuid) -> Unit = {},
    /** Creates a label in the group (null for "Khác") and attaches it. */
    val onCreateLabel: (name: String, groupId: Uuid?) -> Unit = { _, _ -> },
    /** Opens the label screen; the entry to it lives here until Settings exists. */
    val onManageLabels: () -> Unit = {},
) {
    companion object {
        /** Does nothing; for previews. */
        val NoOp =
            ComposerActions(
                onClose = {},
                onRetrySave = {},
                onEmojiChange = {},
                onOccurredAtChange = {},
                onPickPhotos = {},
                onTakePhoto = {},
                onRemoveMedia = {},
                onRetryPhoto = {},
            )
    }
}
