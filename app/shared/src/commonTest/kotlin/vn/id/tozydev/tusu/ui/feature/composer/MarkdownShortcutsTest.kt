@file:OptIn(ExperimentalRichTextApi::class)

package vn.id.tozydev.tusu.ui.feature.composer

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.model.HeadingStyle
import com.mohamedrejeb.richeditor.model.RichSpanStyle
import com.mohamedrejeb.richeditor.model.RichTextState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/** Types [text] one character at a time, as a keyboard does. */
private fun RichTextState.type(text: String, shortcuts: MarkdownShortcuts) {
    text.forEach {
        addTextAfterSelection(it.toString())
        shortcuts.onTextChange()
    }
}

/** Inserts [text] in one change, as a paste does. */
private fun RichTextState.paste(text: String, shortcuts: MarkdownShortcuts) {
    addTextAfterSelection(text)
    shortcuts.onTextChange()
}

class MarkdownShortcutsTest :
    FunSpec(
        {
            test("# at the start of a paragraph makes it a heading") {
                val state = RichTextState()
                val shortcuts = MarkdownShortcuts(state)
                state.type("# Hôm nay", shortcuts)

                state.annotatedString.text shouldBe "Hôm nay"
                state.currentHeadingStyle shouldBe MarkdownShortcuts.HEADING
            }

            test("# inside a sentence stays text") {
                val state = RichTextState()
                val shortcuts = MarkdownShortcuts(state)
                state.type("Số # 1", shortcuts)

                state.annotatedString.text shouldBe "Số # 1"
                state.currentHeadingStyle shouldBe HeadingStyle.Normal
            }

            test("**text** becomes bold once it is closed") {
                val state = RichTextState()
                val shortcuts = MarkdownShortcuts(state)
                state.type("Một **ngày** dài", shortcuts)

                state.annotatedString.text shouldBe "Một ngày dài"
                state.getSpanStyle(TextRange(5, 8)).fontWeight shouldBe FontWeight.Bold
                state.getSpanStyle(TextRange(10, 12)).fontWeight shouldBe null
            }

            test("*text* becomes italic") {
                val state = RichTextState()
                val shortcuts = MarkdownShortcuts(state)
                state.type("Rất *vui*", shortcuts)

                state.annotatedString.text shouldBe "Rất vui"
                state.getSpanStyle(TextRange(4, 7)).fontStyle shouldBe FontStyle.Italic
            }

            test("~~text~~ is struck through") {
                val state = RichTextState()
                val shortcuts = MarkdownShortcuts(state)
                state.type("~~bỏ~~", shortcuts)

                state.annotatedString.text shouldBe "bỏ"
                state.getSpanStyle(TextRange(0, 2)).textDecoration shouldBe
                    TextDecoration.LineThrough
            }

            test("a lone asterisk or an empty pair stays text") {
                val state = RichTextState()
                val shortcuts = MarkdownShortcuts(state)
                state.type("2 * 3 = 6 ****", shortcuts)

                state.annotatedString.text shouldBe "2 * 3 = 6 ****"
            }

            test("a pasted URL becomes a link to itself") {
                val state = RichTextState()
                val shortcuts = MarkdownShortcuts(state)
                state.type("Xem ", shortcuts)
                state.paste("https://tozydev.id.vn/tusu", shortcuts)

                state.annotatedString.text shouldBe "Xem https://tozydev.id.vn/tusu"
                state
                    .getRichSpanStyle(TextRange(6, 10))
                    .shouldBeInstanceOf<RichSpanStyle.Link>()
                    .url shouldBe "https://tozydev.id.vn/tusu"
            }

            test("pasted text that is not a URL stays plain") {
                val state = RichTextState()
                val shortcuts = MarkdownShortcuts(state)
                state.paste("không phải link", shortcuts)

                state.getRichSpanStyle(TextRange(1, 4)).shouldBeInstanceOf<RichSpanStyle.Default>()
            }
        },
    )
