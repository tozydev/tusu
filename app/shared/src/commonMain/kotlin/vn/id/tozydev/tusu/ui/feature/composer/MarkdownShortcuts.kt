package vn.id.tozydev.tusu.ui.feature.composer

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.model.HeadingStyle
import com.mohamedrejeb.richeditor.model.RichTextState

private val UrlPattern = Regex("""^(https?://|www\.)\S+$""", RegexOption.IGNORE_CASE)

/** An inline marker that formats the text between a pair of it once the pair is closed. */
private class InlineMarker(val pattern: Regex, val style: SpanStyle)

// Longest markers first, so `**` is not read as two italic stars. A single star must not follow a
// star or a letter, which keeps `2*3` and a half-typed `**bold*` as text.
private val InlineMarkers =
    listOf(
        InlineMarker(Regex("""\*\*([^*\n]+)\*\*$"""), SpanStyle(fontWeight = FontWeight.Bold)),
        InlineMarker(
            Regex("""~~([^~\n]+)~~$"""),
            SpanStyle(textDecoration = TextDecoration.LineThrough),
        ),
        InlineMarker(
            Regex("""(?<![*\p{L}\p{N}])\*([^*\s][^*\n]*)\*$"""),
            SpanStyle(fontStyle = FontStyle.Italic),
        ),
    )

/**
 * Turns Markdown typed into the editor into formatting as soon as it is complete: `# ` at the start
 * of a paragraph, `**bold**`, `*italic*` and `~~strikethrough~~`, and a pasted URL becomes a link.
 * The editor already handles `- `, `* ` and `1. ` lists. Call [onTextChange] after every change of
 * the text, on the thread that owns the editor.
 */
@OptIn(ExperimentalRichTextApi::class)
class MarkdownShortcuts(private val state: RichTextState) {
    private var previous = state.annotatedString.text

    /** Takes the current text as the baseline, e.g. after a stored body was loaded. */
    fun sync() {
        previous = state.annotatedString.text
    }

    fun onTextChange() {
        val old = previous
        val text = state.annotatedString.text
        previous = text
        // Deletions and selections never trigger formatting.
        if (text.length <= old.length || !state.selection.collapsed) return
        val start = old.commonPrefixWith(text).length
        val end = start + (text.length - old.length)
        if (end - start > 1) linkPasted(text, start, end) else formatTyped(text, end)
        previous = state.annotatedString.text
    }

    private fun linkPasted(text: String, start: Int, end: Int) {
        val chunk = text.substring(start, end)
        val url = chunk.trim()
        if (!UrlPattern.matches(url)) return
        val from = start + chunk.indexOf(url)
        val target = if (url.startsWith("www.", ignoreCase = true)) "https://$url" else url
        state.addLinkToTextRange(target, TextRange(from, from + url.length))
    }

    private fun formatTyped(text: String, caret: Int) {
        val paragraphStart = text.lastIndexOf('\n', (caret - 1).coerceAtLeast(0)).let { it + 1 }
        val typed = text.substring(paragraphStart, caret)
        if (typed == "# " && state.currentHeadingStyle == HeadingStyle.Normal) {
            state.removeTextRange(TextRange(paragraphStart, caret))
            state.setHeadingStyle(HEADING)
            return
        }
        for (marker in InlineMarkers) {
            val match = marker.pattern.find(typed) ?: continue
            val content = match.groupValues[1]
            val from = paragraphStart + match.range.first
            state.replaceTextRange(TextRange(from, caret), content)
            state.addSpanStyle(marker.style, TextRange(from, from + content.length))
            // What is typed next continues as plain text, as it would after a closing marker.
            state.selection = TextRange(from + content.length)
            state.removeSpanStyle(marker.style)
            return
        }
    }

    companion object {
        /** The single heading level the composer offers. */
        val HEADING = HeadingStyle.H3
    }
}
