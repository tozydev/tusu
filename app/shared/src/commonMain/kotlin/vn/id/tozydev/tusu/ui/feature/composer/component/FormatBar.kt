package vn.id.tozydev.tusu.ui.feature.composer.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.model.HeadingStyle
import com.mohamedrejeb.richeditor.model.RichTextState
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.format_bold
import vn.id.tozydev.tusu.generated.resources.format_bullet_list
import vn.id.tozydev.tusu.generated.resources.format_heading
import vn.id.tozydev.tusu.generated.resources.format_italic
import vn.id.tozydev.tusu.generated.resources.format_link
import vn.id.tozydev.tusu.generated.resources.format_numbered_list
import vn.id.tozydev.tusu.generated.resources.format_strikethrough
import vn.id.tozydev.tusu.generated.resources.ic_format_bold_24px
import vn.id.tozydev.tusu.generated.resources.ic_format_h3_24px
import vn.id.tozydev.tusu.generated.resources.ic_format_italic_24px
import vn.id.tozydev.tusu.generated.resources.ic_format_list_bulleted_24px
import vn.id.tozydev.tusu.generated.resources.ic_format_list_numbered_rtl_24px
import vn.id.tozydev.tusu.generated.resources.ic_format_strikethrough_24px
import vn.id.tozydev.tusu.generated.resources.ic_link_24px
import vn.id.tozydev.tusu.ui.feature.composer.MarkdownShortcuts
import vn.id.tozydev.tusu.ui.theme.AppTheme

private val BoldStyle = SpanStyle(fontWeight = FontWeight.Bold)
private val ItalicStyle = SpanStyle(fontStyle = FontStyle.Italic)
private val StrikethroughStyle = SpanStyle(textDecoration = TextDecoration.LineThrough)

/**
 * The formatting buttons, in bar order. Quotes are missing on purpose: the rich text JSON cannot
 * store them yet (TOZY-123).
 */
enum class FormatAction(val icon: DrawableResource, val label: StringResource) {
    Bold(Res.drawable.ic_format_bold_24px, Res.string.format_bold),
    Italic(Res.drawable.ic_format_italic_24px, Res.string.format_italic),
    Strikethrough(Res.drawable.ic_format_strikethrough_24px, Res.string.format_strikethrough),
    Heading(Res.drawable.ic_format_h3_24px, Res.string.format_heading),
    BulletList(Res.drawable.ic_format_list_bulleted_24px, Res.string.format_bullet_list),
    NumberedList(Res.drawable.ic_format_list_numbered_rtl_24px, Res.string.format_numbered_list),
    Link(Res.drawable.ic_link_24px, Res.string.format_link),
}

/** The formatting that applies at the caret. */
@Immutable data class FormatState(val active: Set<FormatAction> = emptySet())

/**
 * Formatting buttons for the editor. Each one shows whether its format applies at the caret; they
 * do not take focus, so the keyboard stays open while the user formats.
 */
@Composable
fun FormatBar(
    state: FormatState,
    onAction: (FormatAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FormatAction.entries.forEach { action ->
            FilledTonalIconToggleButton(
                checked = action in state.active,
                onCheckedChange = { onAction(action) },
                modifier = Modifier.focusProperties { canFocus = false },
            ) {
                Icon(
                    painter = painterResource(action.icon),
                    contentDescription = stringResource(action.label),
                )
            }
        }
    }
}

/** Reads the formatting at the caret from [richTextState] and applies the taps to it. */
@OptIn(ExperimentalRichTextApi::class)
@Composable
fun RichTextFormatBar(richTextState: RichTextState, modifier: Modifier = Modifier) {
    var linkDialogOpen by remember { mutableStateOf(false) }
    FormatBar(
        state = richTextState.formatState(),
        onAction = { action ->
            when (action) {
                FormatAction.Bold -> richTextState.toggleSpanStyle(BoldStyle)
                FormatAction.Italic -> richTextState.toggleSpanStyle(ItalicStyle)
                FormatAction.Strikethrough -> richTextState.toggleSpanStyle(StrikethroughStyle)
                FormatAction.Heading ->
                    richTextState.setHeadingStyle(
                        if (richTextState.currentHeadingStyle == HeadingStyle.Normal) {
                            MarkdownShortcuts.HEADING
                        } else {
                            HeadingStyle.Normal
                        },
                    )
                FormatAction.BulletList -> richTextState.toggleUnorderedList()
                FormatAction.NumberedList -> richTextState.toggleOrderedList()
                FormatAction.Link -> linkDialogOpen = true
            }
        },
        modifier = modifier,
    )
    if (linkDialogOpen) {
        LinkDialog(
            initialUrl = richTextState.selectedLinkUrl.orEmpty(),
            isEditing = richTextState.isLink,
            onConfirm = { url ->
                richTextState.applyLink(url)
                linkDialogOpen = false
            },
            onRemove = {
                richTextState.removeLink()
                linkDialogOpen = false
            },
            onDismiss = { linkDialogOpen = false },
        )
    }
}

@OptIn(ExperimentalRichTextApi::class)
private fun RichTextState.formatState(): FormatState {
    val style = currentSpanStyle
    val active = buildSet {
        if (style.fontWeight == FontWeight.Bold) add(FormatAction.Bold)
        if (style.fontStyle == FontStyle.Italic) add(FormatAction.Italic)
        if (style.textDecoration?.contains(TextDecoration.LineThrough) == true) {
            add(FormatAction.Strikethrough)
        }
        if (currentHeadingStyle != HeadingStyle.Normal) add(FormatAction.Heading)
        if (isUnorderedList) add(FormatAction.BulletList)
        if (isOrderedList) add(FormatAction.NumberedList)
        if (isLink) add(FormatAction.Link)
    }
    return FormatState(active)
}

/** Links the selected text, or inserts the URL itself as a link when nothing is selected. */
@OptIn(ExperimentalRichTextApi::class)
private fun RichTextState.applyLink(url: String) {
    when {
        isLink -> updateLink(url)
        !selection.collapsed -> addLinkToSelection(url)
        else -> addLink(text = url, url = url)
    }
}

@Preview
@Composable
private fun FormatBarNoneActivePreview() {
    AppTheme { FormatBar(state = FormatState(), onAction = {}) }
}

@Preview
@Composable
private fun FormatBarSeveralActivePreview() {
    AppTheme {
        FormatBar(
            state =
                FormatState(
                    setOf(FormatAction.Bold, FormatAction.Heading, FormatAction.BulletList),
                ),
            onAction = {},
        )
    }
}
