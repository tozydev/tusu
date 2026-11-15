package vn.id.tozydev.tusu.ui.feature.timeline

import kotlinx.datetime.LocalDate
import vn.id.tozydev.tusu.domain.model.Entry

/** One row of the timeline feed: a day header, or an entry. */
sealed interface TimelineItem {
    data class Day(val date: LocalDate) : TimelineItem

    data class EntryItem(val entry: Entry) : TimelineItem
}
