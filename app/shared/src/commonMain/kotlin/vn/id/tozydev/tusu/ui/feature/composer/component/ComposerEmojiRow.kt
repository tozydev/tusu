package vn.id.tozydev.tusu.ui.feature.composer.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.composer_emoji_description
import vn.id.tozydev.tusu.ui.component.EmojiBadge
import vn.id.tozydev.tusu.ui.theme.AppTheme

/** The emoji slot at the top of the writing area; [onClick] opens the picker. */
@Composable
fun ComposerEmojiRow(emoji: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EmojiBadge(
            emoji = emoji,
            size = 44.dp,
            onClick = onClick,
            contentDescription = stringResource(Res.string.composer_emoji_description),
        )
    }
}

@Preview
@Composable
private fun ComposerEmojiRowEmptyPreview() {
    AppTheme { ComposerEmojiRow(emoji = null, onClick = {}) }
}

@Preview
@Composable
private fun ComposerEmojiRowChosenPreview() {
    AppTheme { ComposerEmojiRow(emoji = "🌱", onClick = {}) }
}
