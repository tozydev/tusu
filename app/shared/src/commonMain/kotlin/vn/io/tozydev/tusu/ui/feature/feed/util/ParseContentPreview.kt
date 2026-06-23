package vn.io.tozydev.tusu.ui.feature.feed.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

private const val H1_MARKER = "# "

fun parseContentPreview(content: String, maxLines: Int = 4): AnnotatedString =
    buildAnnotatedString {
        val previewLines = content.lines().filter { it.isNotBlank() }.take(maxLines)
        previewLines.forEachIndexed { index, line ->
            if (index == 0 && line.startsWith(H1_MARKER)) {
                val headingText = line.substring(H1_MARKER.length)
                withStyle(ParagraphStyle(lineHeight = 28.sp, lineBreak = LineBreak.Heading)) {
                    withStyle(
                        style =
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                letterSpacing = 0.sp,
                            )
                    ) {
                        appendLine(headingText)
                    }
                }
            } else {
                withStyle(ParagraphStyle(lineHeight = 20.sp, lineBreak = LineBreak.Paragraph)) {
                    appendLine(parseMarkdownLine(line))
                }
            }
        }
    }

private val BoldStyle = SpanStyle(fontWeight = FontWeight.SemiBold)
private val ItalicStyle = SpanStyle(fontStyle = FontStyle.Italic)
private val StrikeStyle = SpanStyle(textDecoration = TextDecoration.LineThrough)
private val CodeStyle = SpanStyle(fontFamily = FontFamily.Monospace)

fun parseMarkdownLine(input: String): AnnotatedString {
    val cleanedInput = input.trim().replace(Regex("^([#>]+\\s*|([*+-]|\\d+\\.)\\s+)"), "")

    val markdownRegex =
        Regex(
            "\\[(.*?)]\\((.*?)\\)|" + // Group 1, 2: Link
                "\\*\\*(.*?)\\*\\*|__(.*?)__|" + // Group 3, 4: Bold
                "\\*(.*?)\\*|_(.*?)_|" + // Group 5, 6: Italic
                "~~(.*?)~~|" + // Group 7: Strikethrough
                "`(.*?)`" // Group 8: Inline Code
        )

    return buildAnnotatedString {
        var lastCursor = 0

        markdownRegex.findAll(cleanedInput).forEach { matchResult ->
            if (matchResult.range.first > lastCursor) {
                append(cleanedInput.substring(lastCursor, matchResult.range.first))
            }

            when {
                // CASE 1: LINK [text](url)
                matchResult.groups[1] != null -> {
                    val linkText = matchResult.groupValues[1]
                    val url = matchResult.groupValues[2]

                    withLink(LinkAnnotation.Url(url)) {
                        append(linkText)
                    }
                }

                // CASE 2: BOLD **text** or __text__
                matchResult.groups[3] != null || matchResult.groups[4] != null -> {
                    val boldText = matchResult.groupValues[3].ifEmpty { matchResult.groupValues[4] }
                    withStyle(style = BoldStyle) {
                        append(boldText)
                    }
                }

                // CASE 3: ITALIC *text* or _text_
                matchResult.groups[5] != null || matchResult.groups[6] != null -> {
                    val italicText =
                        matchResult.groupValues[5].ifEmpty { matchResult.groupValues[6] }
                    withStyle(style = ItalicStyle) {
                        append(italicText)
                    }
                }

                // CASE 4: STRIKETHROUGH ~~text~~
                matchResult.groups[7] != null -> {
                    val strikeText = matchResult.groupValues[7]
                    withStyle(style = StrikeStyle) {
                        append(strikeText)
                    }
                }

                // CASE 5: INLINE CODE `text`
                matchResult.groups[8] != null -> {
                    val codeText = matchResult.groupValues[8]
                    withStyle(style = CodeStyle) {
                        append(codeText)
                    }
                }
            }

            lastCursor = matchResult.range.last + 1
        }

        if (lastCursor < cleanedInput.length) {
            append(cleanedInput.substring(lastCursor))
        }
    }
}
