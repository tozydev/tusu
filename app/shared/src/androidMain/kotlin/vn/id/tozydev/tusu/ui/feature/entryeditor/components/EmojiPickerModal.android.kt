package vn.id.tozydev.tusu.ui.feature.entryeditor.components

import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.emoji2.emojipicker.EmojiPickerView
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.remove_current_emoji
import vn.id.tozydev.tusu.generated.resources.select_emoji_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun EmojiPickerModal(
    onEmojiSelected: (String?) -> Unit,
    onDismiss: () -> Unit,
    initialEmoji: String?,
    modifier: Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetGesturesEnabled = false,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.7f).padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.select_emoji_title),
                        style = MaterialTheme.typography.titleMediumEmphasized,
                    )

                    if (!initialEmoji.isNullOrEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.size(36.dp),
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                Text(
                                    text = initialEmoji,
                                    fontSize = 20.sp,
                                )
                            }
                        }
                    }
                }

                if (initialEmoji != null) {
                    TextButton(
                        onClick = {
                            onEmojiSelected(null)
                            onDismiss()
                        }
                    ) {
                        Text(
                            text = stringResource(Res.string.remove_current_emoji),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            AndroidView(
                factory = { ctx ->
                    EmojiPickerView(ctx).apply {
                        emojiGridColumns = 8
                        setOnEmojiPickedListener {
                            onEmojiSelected(it.emoji)
                        }
                        layoutParams =
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            )
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
