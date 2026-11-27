package vn.id.tozydev.tusu.ui.feature.timeline.component

import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.datetime.TimeZone
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.ui.component.MediaUi

private val PreviewStart = Instant.parse("2026-11-11T03:00:00Z")

/** Fixed entries for `@Preview`s only. */
internal fun sampleTimelineEntries(): List<Entry> =
    listOf(
        previewEntry("Morning coffee by the window.", "☕", minutes = 40),
        previewEntry("Finished the first chapter.", null, minutes = 20),
        previewEntry("Long walk after dinner, the air finally cooled down.", "🌙", minutes = 0),
    )

/** An entry whose text runs past the six lines the feed shows. */
internal fun sampleLongTimelineEntry(): Entry =
    previewEntry(
        text =
            "Today the whole family gathered at the old house, and we spent the afternoon " +
                "sorting boxes from the attic. Every box held something unexpected: letters, " +
                "a broken clock, photographs nobody had seen for decades. We laughed a lot and " +
                "argued about a few of them. By evening the living room was full of piles, and " +
                "we promised to finish the rest next Sunday. I want to remember how warm the " +
                "kitchen felt while everyone was talking at once.",
        emoji = "🏡",
        minutes = 0,
    )

/** Photos for `@Preview`s; the empty path renders as a placeholder tile. */
internal fun samplePhotos(count: Int): List<MediaUi> =
    List(count) {
        MediaUi(
            id = Uuid.random(),
            path = "",
            type = MediaType.PHOTO,
            status = MediaStatus.READY,
            width = 1200,
            height = 800,
        )
    }

private fun previewEntry(text: String, emoji: String?, minutes: Int) =
    Entry(
        id = Uuid.random(),
        body = "",
        textPlain = text,
        emoji = emoji,
        occurredAt = PreviewStart + minutes.minutes,
        timeZone = TimeZone.of("Asia/Ho_Chi_Minh"),
        createdAt = PreviewStart,
        updatedAt = PreviewStart,
    )

internal fun sampleVoice(seconds: Int): MediaUi =
    MediaUi(
        id = Uuid.random(),
        path = "",
        type = MediaType.AUDIO,
        status = MediaStatus.READY,
        width = null,
        height = null,
        duration = seconds.seconds,
        waveform = List(48) { ((it * (29 + seconds)) % 100) / 100f },
    )
