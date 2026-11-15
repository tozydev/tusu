package vn.id.tozydev.tusu.ui.feature.composer.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.model.RichTextState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.format_bold
import vn.id.tozydev.tusu.generated.resources.format_bullet_list
import vn.id.tozydev.tusu.generated.resources.format_italic
import vn.id.tozydev.tusu.generated.resources.format_numbered_list
import vn.id.tozydev.tusu.generated.resources.format_strikethrough
import vn.id.tozydev.tusu.generated.resources.ic_format_bold_24px
import vn.id.tozydev.tusu.generated.resources.ic_format_italic_24px
import vn.id.tozydev.tusu.generated.resources.ic_format_list_bulleted_24px
import vn.id.tozydev.tusu.generated.resources.ic_format_list_numbered_rtl_24px
import vn.id.tozydev.tusu.generated.resources.ic_format_strikethrough_24px
import vn.id.tozydev.tusu.ui.theme.AppTheme

private val BoldStyle = SpanStyle(fontWeight = FontWeight.Bold)
private val ItalicStyle = SpanStyle(fontStyle = FontStyle.Italic)
private val StrikethroughStyle = SpanStyle(textDecoration = TextDecoration.LineThrough)

/**
 * Formatting buttons for the editor. Each one shows whether its style applies at the caret; they do
 * not take focus, so the keyboard stays open while the user formats.
 */
@Composable
fun FormatBar(
    bold: Boolean,
    italic: Boolean,
    strikethrough: Boolean,
    bulletList: Boolean,
    numberedList: Boolean,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onStrikethroughClick: () -> Unit,
    onBulletListClick: () -> Unit,
    onNumberedListClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FormatButton(
            icon = painterResource(Res.drawable.ic_format_bold_24px),
            description = stringResource(Res.string.format_bold),
            checked = bold,
            onClick = onBoldClick,
        )
        FormatButton(
            icon = painterResource(Res.drawable.ic_format_italic_24px),
            description = stringResource(Res.string.format_italic),
            checked = italic,
            onClick = onItalicClick,
        )
        FormatButton(
            icon = painterResource(Res.drawable.ic_format_strikethrough_24px),
            description = stringResource(Res.string.format_strikethrough),
            checked = strikethrough,
            onClick = onStrikethroughClick,
        )
        FormatButton(
            icon = painterResource(Res.drawable.ic_format_list_bulleted_24px),
            description = stringResource(Res.string.format_bullet_list),
            checked = bulletList,
            onClick = onBulletListClick,
        )
        FormatButton(
            icon = painterResource(Res.drawable.ic_format_list_numbered_rtl_24px),
            description = stringResource(Res.string.format_numbered_list),
            checked = numberedList,
            onClick = onNumberedListClick,
        )
    }
}

@Composable
private fun FormatButton(
    icon: Painter,
    description: String,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalIconToggleButton(
        checked = checked,
        onCheckedChange = { onClick() },
        modifier = modifier.focusProperties { canFocus = false },
    ) {
        Icon(painter = icon, contentDescription = description)
    }
}

/** Reads the formatting at the caret from [richTextState] and applies the taps to it. */
@OptIn(ExperimentalRichTextApi::class)
@Composable
fun RichTextFormatBar(richTextState: RichTextState, modifier: Modifier = Modifier) {
    val style = richTextState.currentSpanStyle
    FormatBar(
        bold = style.fontWeight == FontWeight.Bold,
        italic = style.fontStyle == FontStyle.Italic,
        strikethrough = style.textDecoration?.contains(TextDecoration.LineThrough) == true,
        bulletList = richTextState.isUnorderedList,
        numberedList = richTextState.isOrderedList,
        onBoldClick = { richTextState.toggleSpanStyle(BoldStyle) },
        onItalicClick = { richTextState.toggleSpanStyle(ItalicStyle) },
        onStrikethroughClick = { richTextState.toggleSpanStyle(StrikethroughStyle) },
        onBulletListClick = richTextState::toggleUnorderedList,
        onNumberedListClick = richTextState::toggleOrderedList,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun FormatBarNoneActivePreview() {
    AppTheme {
        FormatBar(
            bold = false,
            italic = false,
            strikethrough = false,
            bulletList = false,
            numberedList = false,
            onBoldClick = {},
            onItalicClick = {},
            onStrikethroughClick = {},
            onBulletListClick = {},
            onNumberedListClick = {},
        )
    }
}

@Preview
@Composable
private fun FormatBarSeveralActivePreview() {
    AppTheme {
        FormatBar(
            bold = true,
            italic = true,
            strikethrough = false,
            bulletList = true,
            numberedList = false,
            onBoldClick = {},
            onItalicClick = {},
            onStrikethroughClick = {},
            onBulletListClick = {},
            onNumberedListClick = {},
        )
    }
}
