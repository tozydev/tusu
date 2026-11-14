package vn.id.tozydev.tusu.text

import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.document.RichTextBlock
import com.mohamedrejeb.richeditor.document.RichTextBlockType
import com.mohamedrejeb.richeditor.document.RichTextDocument
import com.mohamedrejeb.richeditor.document.RichTextSpanMark
import com.mohamedrejeb.richeditor.json.toJson
import com.mohamedrejeb.richeditor.model.RichTextState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.error.RichTextError

/** Builds the stored body JSON with the library itself, so tests follow its real schema. */
@OptIn(ExperimentalRichTextApi::class)
private fun bodyJson(vararg blocks: RichTextBlock): String =
    RichTextState().setRichTextDocument(RichTextDocument(blocks.toList())).toJson()

private fun deriveOk(json: String): DerivedText {
    val outcome = deriveText(json)
    outcome.shouldBeInstanceOf<Outcome.Success<DerivedText>>()
    return outcome.value
}

class RichTextTest :
    FunSpec(
        {
            test("a blank body derives empty text") {
                deriveText("") shouldBe Outcome.Success(DerivedText("", ""))
                deriveText("   ") shouldBe Outcome.Success(DerivedText("", ""))
            }

            test("a plain paragraph becomes plain text and folded search text") {
                val json = bodyJson(RichTextBlock(text = "Bài học Đà Nẵng"))

                deriveOk(json) shouldBe
                    DerivedText(plain = "Bài học Đà Nẵng", search = "bai hoc da nang")
            }

            test("marks are dropped and only the text is kept") {
                val json =
                    bodyJson(
                        RichTextBlock(
                            text = "Xin chào thế giới",
                            spans =
                                listOf(
                                    RichTextSpanMark.Bold(0..2),
                                    RichTextSpanMark.Italic(4..8),
                                    RichTextSpanMark.Link(10..16, url = "https://example.com"),
                                ),
                        ),
                    )

                deriveOk(json).plain shouldBe "Xin chào thế giới"
            }

            test("headings and list items become separate lines") {
                val json =
                    bodyJson(
                        RichTextBlock(text = "Tiêu đề", headingLevel = 1),
                        RichTextBlock(
                            text = "Mục một",
                            type = RichTextBlockType.ListItem(ordered = false),
                        ),
                        RichTextBlock(
                            text = "Mục hai",
                            type = RichTextBlockType.ListItem(ordered = false),
                        ),
                        RichTextBlock(
                            text = "Bước một",
                            type = RichTextBlockType.ListItem(ordered = true),
                        ),
                        RichTextBlock(text = "Đoạn cuối"),
                    )

                deriveOk(json) shouldBe
                    DerivedText(
                        plain = "Tiêu đề\nMục một\nMục hai\nBước một\nĐoạn cuối",
                        search = "tieu de muc mot muc hai buoc mot doan cuoi",
                    )
            }

            test("empty paragraphs add no blank lines and each line is trimmed") {
                val json =
                    bodyJson(
                        RichTextBlock(text = "  a  "),
                        RichTextBlock(text = ""),
                        RichTextBlock(text = "   "),
                        RichTextBlock(text = "b"),
                    )

                deriveOk(json).plain shouldBe "a\nb"
            }

            test("search text collapses whitespace runs and keeps digits and emoji") {
                val json = bodyJson(RichTextBlock(text = "Hà   Nội 2026 😊"))

                deriveOk(json) shouldBe
                    DerivedText(plain = "Hà   Nội 2026 😊", search = "ha noi 2026 😊")
            }

            test("malformed JSON maps to MalformedJson") {
                val inputs =
                    listOf("{", "not json", "[]", """{"v":1}""", """{"v":1,"blocks":"x"}""")
                for (input in inputs) {
                    deriveText(input) shouldBe Outcome.Failure(RichTextError.MalformedJson)
                }
            }

            test("a newer schema version maps to UnsupportedVersion") {
                val valid = bodyJson(RichTextBlock(text = "x"))
                val newer = valid.replaceFirst("\"v\":1", "\"v\":2")
                check(newer != valid)

                deriveText(newer) shouldBe
                    Outcome.Failure(RichTextError.UnsupportedVersion(found = 2, supported = 1))
            }
        },
    )
