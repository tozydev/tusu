package vn.id.tozydev.tusu.ui.feature.entry

import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.document.RichTextBlock
import com.mohamedrejeb.richeditor.document.RichTextDocument
import com.mohamedrejeb.richeditor.json.toJson
import com.mohamedrejeb.richeditor.model.RichTextState
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.datetime.TimeZone
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.ui.component.MediaUi

/** Fixed photos for `@Preview`s only; their paths are empty, so no image loads. */
internal fun sampleEntryPhotos(count: Int): List<MediaUi> =
    List(count) {
        MediaUi(
            id = Uuid.random(),
            path = "",
            type = MediaType.PHOTO,
            status = MediaStatus.READY,
            width = null,
            height = null,
        )
    }

/** A loaded entry for `@Preview`s only. */
internal fun sampleEntryState(
    text: String = "",
    emoji: String? = null,
    photoCount: Int = 0,
    voiceCount: Int = 0,
): EntryDetailUiState =
    EntryDetailUiState(
        isLoading = false,
        emoji = emoji,
        occurredAt = Instant.parse("2026-11-10T03:00:00Z"),
        timeZone = TimeZone.UTC,
        body = sampleBody(text),
        textPlain = text,
        photos = sampleEntryPhotos(photoCount),
        voices = sampleEntryVoices(voiceCount),
    )

@OptIn(ExperimentalRichTextApi::class)
private fun sampleBody(text: String): String =
    if (text.isBlank()) ""
    else
        RichTextState()
            .setRichTextDocument(RichTextDocument(listOf(RichTextBlock(text = text))))
            .toJson()

/** Voice notes for `@Preview`s only; nothing plays because their paths are empty. */
internal fun sampleEntryVoices(count: Int): List<MediaUi> =
    List(count) { index ->
        MediaUi(
            id = Uuid.random(),
            path = "",
            type = MediaType.AUDIO,
            status = MediaStatus.READY,
            width = null,
            height = null,
            duration = (15 + index * 45).seconds,
            waveform = List(48) { ((it * (31 + index * 9)) % 100) / 100f },
        )
    }
