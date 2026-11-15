package vn.id.tozydev.tusu.ui.feature.timeline.component

import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.datetime.TimeZone
import vn.id.tozydev.tusu.domain.model.Entry

private val PreviewStart = Instant.parse("2026-11-11T03:00:00Z")

/** Fixed entries for `@Preview`s only. */
internal fun sampleTimelineEntries(): List<Entry> =
    listOf(
        previewEntry("Morning coffee by the window.", "☕", minutes = 40),
        previewEntry("Finished the first chapter.", null, minutes = 20),
        previewEntry("Long walk after dinner, the air finally cooled down.", "🌙", minutes = 0),
    )

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
