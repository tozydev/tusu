package vn.id.tozydev.tusu.ui.feature.entryeditor.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun EmojiPickerModal(
    onEmojiSelected: (String?) -> Unit,
    onDismiss: () -> Unit,
    initialEmoji: String? = null,
    modifier: Modifier = Modifier,
)
