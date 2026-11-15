package vn.id.tozydev.tusu.ui.feature.composer

import androidx.compose.runtime.Immutable
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDateTime

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
    val onRemovePhoto: (Uuid) -> Unit,
    val onRetryPhoto: (Uuid) -> Unit,
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
                onRemovePhoto = {},
                onRetryPhoto = {},
            )
    }
}
