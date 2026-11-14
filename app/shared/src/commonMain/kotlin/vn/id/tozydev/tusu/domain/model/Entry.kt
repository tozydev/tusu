package vn.id.tozydev.tusu.domain.model

import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** One moment in the journal: text and/or media at a point in time. */
data class Entry(
    val id: Uuid,
    /** Compose Rich Editor JSON, kept exactly as stored so unknown marks survive. */
    val body: String,
    val textPlain: String,
    val emoji: String? = null,
    val occurredAt: Instant,
    /** Zone the moment happened in, so it stays on the same calendar day after travelling. */
    val timeZone: TimeZone,
    val location: EntryLocation? = null,
    val media: List<Media> = emptyList(),
    val labels: List<Label> = emptyList(),
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant? = null,
) {
    val localDate: LocalDate
        get() = occurredAt.toLocalDateTime(timeZone).date

    val isDeleted: Boolean
        get() = deletedAt != null
}
