package vn.io.tozydev.tusu.ui.feature.entryeditor.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun EmojiPickerModal(
    onEmojiSelected: (String?) -> Unit,
    onDismiss: () -> Unit,
    initialEmoji: String?,
    modifier: Modifier,
) {
    // noop
}
