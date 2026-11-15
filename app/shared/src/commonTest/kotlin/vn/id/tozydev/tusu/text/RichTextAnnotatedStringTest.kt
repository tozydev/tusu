package vn.id.tozydev.tusu.text

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.document.RichTextBlock
import com.mohamedrejeb.richeditor.document.RichTextDocument
import com.mohamedrejeb.richeditor.document.RichTextSpanMark
import com.mohamedrejeb.richeditor.json.toJson
import com.mohamedrejeb.richeditor.model.RichTextState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.error.RichTextError

@OptIn(ExperimentalRichTextApi::class)
private fun bodyJson(vararg blocks: RichTextBlock): String =
    RichTextState().setRichTextDocument(RichTextDocument(blocks.toList())).toJson()

private fun annotatedOk(json: String): AnnotatedString {
    val outcome = richTextToAnnotatedString(json)
    outcome.shouldBeInstanceOf<Outcome.Success<AnnotatedString>>()
    return outcome.value
}

class RichTextAnnotatedStringTest :
    FunSpec(
        {
            test("a blank body gives empty text") {
                annotatedOk("") shouldBe AnnotatedString("")
                annotatedOk("   ") shouldBe AnnotatedString("")
            }

            test("a plain paragraph keeps its text") {
                annotatedOk(bodyJson(RichTextBlock(text = "Bài học Đà Nẵng"))).text shouldBe
                    "Bài học Đà Nẵng"
            }

            test("a bold span is kept as a bold span style") {
                val json =
                    bodyJson(
                        RichTextBlock(
                            text = "Xin chào",
                            spans = listOf(RichTextSpanMark.Bold(0..2)),
                        ),
                    )

                val styled = annotatedOk(json)

                styled.spanStyles.any { it.item.fontWeight == FontWeight.Bold } shouldBe true
            }

            test("malformed JSON maps to MalformedJson") {
                richTextToAnnotatedString("{not json") shouldBe
                    Outcome.Failure(RichTextError.MalformedJson)
            }
        },
    )
