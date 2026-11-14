package vn.id.tozydev.tusu.text

import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.json.CURRENT_JSON_SCHEMA_VERSION
import com.mohamedrejeb.richeditor.json.MalformedRichTextJsonException
import com.mohamedrejeb.richeditor.json.UnsupportedRichTextJsonVersionException
import com.mohamedrejeb.richeditor.json.setJson
import com.mohamedrejeb.richeditor.model.RichTextState
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.error.RichTextError

/** Preview text and search text derived from an entry body. */
data class DerivedText(val plain: String, val search: String)

private val OBJECT_REPLACEMENT: String = Char(0xFFFC).toString()
private val WHITESPACE_RUN = Regex("\\s+")

/**
 * Derives `text_plain` and `text_search` from a body stored as Compose Rich Editor JSON.
 *
 * Decodes through [RichTextState.setJson] because the library's document codec is internal. The
 * state is a plain object, so this needs no composition and runs in unit tests.
 */
@OptIn(ExperimentalRichTextApi::class)
fun deriveText(bodyJson: String): Outcome<DerivedText, RichTextError> {
    if (bodyJson.isBlank()) return Outcome.Success(DerivedText(plain = "", search = ""))

    val blocks =
        try {
            RichTextState().setJson(bodyJson).toRichTextDocument().blocks
        } catch (e: MalformedRichTextJsonException) {
            return Outcome.Failure(RichTextError.MalformedJson)
        } catch (e: UnsupportedRichTextJsonVersionException) {
            return Outcome.Failure(
                RichTextError.UnsupportedVersion(
                    found = e.documentVersion,
                    supported = CURRENT_JSON_SCHEMA_VERSION,
                ),
            )
        }

    // Block text excludes list prefixes, which is what a reader expects in the preview. Empty
    // blocks are dropped so spacing paragraphs do not add blank lines to the preview.
    val plain =
        blocks
            .map { block -> block.text.replace(OBJECT_REPLACEMENT, "").trim() }
            .filter { it.isNotEmpty() }
            .joinToString(separator = "\n")
    val search = plain.foldDiacritics().replace(WHITESPACE_RUN, " ").trim()
    return Outcome.Success(DerivedText(plain = plain, search = search))
}
