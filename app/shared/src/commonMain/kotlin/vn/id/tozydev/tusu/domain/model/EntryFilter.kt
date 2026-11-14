package vn.id.tozydev.tusu.domain.model

import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate

/** Search criteria; an entry must satisfy every criterion that is set. */
data class EntryFilter(
    val query: String = "",
    /** Matches entries with media of any of these types. */
    val mediaTypes: Set<MediaType> = emptySet(),
    val hasLocation: Boolean = false,
    /** Matches entries carrying any of these emoji. */
    val emojis: Set<String> = emptySet(),
    /**
     * Selected label ids keyed by group id (null is the "Khác" group). An entry needs one of the
     * labels of each group that has a selection.
     */
    val labelIdsByGroup: Map<Uuid?, Set<Uuid>> = emptyMap(),
    val dateRange: ClosedRange<LocalDate>? = null,
) {
    val isEmpty: Boolean
        get() =
            query.isBlank() &&
                mediaTypes.isEmpty() &&
                !hasLocation &&
                emojis.isEmpty() &&
                labelIdsByGroup.values.all { it.isEmpty() } &&
                dateRange == null
}
