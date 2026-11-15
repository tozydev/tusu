package vn.id.tozydev.tusu.domain.model

import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.datetime.TimeZone

/**
 * The fields of an entry the user edits directly. Plain text, search text, the local date and the
 * timestamps are derived when it is saved; media and labels are written through their own
 * repositories.
 */
data class EntryDraft(
    val id: Uuid,
    val body: String,
    val emoji: String? = null,
    val occurredAt: Instant,
    val timeZone: TimeZone,
    val location: EntryLocation? = null,
)

/** The editable fields of this entry, as they are when it is opened for editing. */
fun Entry.toDraft(): EntryDraft =
    EntryDraft(
        id = id,
        body = body,
        emoji = emoji,
        occurredAt = occurredAt,
        timeZone = timeZone,
        location = location,
    )
