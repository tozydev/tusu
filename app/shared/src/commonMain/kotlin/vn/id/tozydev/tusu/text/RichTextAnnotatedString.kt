package vn.id.tozydev.tusu.text

import androidx.compose.ui.text.AnnotatedString
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.json.CURRENT_JSON_SCHEMA_VERSION
import com.mohamedrejeb.richeditor.json.MalformedRichTextJsonException
import com.mohamedrejeb.richeditor.json.UnsupportedRichTextJsonVersionException
import com.mohamedrejeb.richeditor.json.setJson
import com.mohamedrejeb.richeditor.model.RichTextState
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.error.RichTextError

/**
 * Converts a stored body (Compose Rich Editor JSON) into styled text for read-only display. A blank
 * body gives empty text.
 */
@OptIn(ExperimentalRichTextApi::class)
fun richTextToAnnotatedString(bodyJson: String): Outcome<AnnotatedString, RichTextError> {
    if (bodyJson.isBlank()) return Outcome.Success(AnnotatedString(""))

    val state = RichTextState()
    try {
        state.setJson(bodyJson)
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
    return Outcome.Success(state.annotatedString)
}
