package vn.id.tozydev.tusu.ui.feature.composer.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.composer_emoji_description
import vn.id.tozydev.tusu.generated.resources.composer_labels
import vn.id.tozydev.tusu.generated.resources.ic_sell_24px
import vn.id.tozydev.tusu.ui.component.EmojiBadge
import vn.id.tozydev.tusu.ui.component.LabelChip
import vn.id.tozydev.tusu.ui.component.LabelChipData
import vn.id.tozydev.tusu.ui.theme.AppTheme

/**
 * The top of the writing area: the emoji slot, the chips of the attached labels in their group
 * colours and the "Labels" chip. [onClick] opens the emoji picker, [onLabelsClick] the label sheet.
 */
@Composable
fun ComposerEmojiRow(
    emoji: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    labels: List<LabelChipData> = emptyList(),
    onLabelsClick: (() -> Unit)? = null,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        EmojiBadge(
            emoji = emoji,
            size = 44.dp,
            onClick = onClick,
            contentDescription = stringResource(Res.string.composer_emoji_description),
        )
        labels.forEach { LabelChip(it) }
        if (onLabelsClick != null) {
            AssistChip(
                onClick = onLabelsClick,
                label = { Text(stringResource(Res.string.composer_labels)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_sell_24px),
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Preview
@Composable
private fun ComposerEmojiRowEmptyPreview() {
    AppTheme { ComposerEmojiRow(emoji = null, onClick = {}, onLabelsClick = {}) }
}

@Preview
@Composable
private fun ComposerEmojiRowChosenPreview() {
    AppTheme {
        ComposerEmojiRow(
            emoji = "🌱",
            onClick = {},
            labels = listOf(LabelChipData("Kỷ niệm", Color(0xFF68A500))),
            onLabelsClick = {},
        )
    }
}
