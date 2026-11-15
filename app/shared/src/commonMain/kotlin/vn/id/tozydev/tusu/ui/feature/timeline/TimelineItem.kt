package vn.id.tozydev.tusu.ui.feature.timeline

import kotlinx.datetime.LocalDate
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.ui.component.MediaUi

/** One row of the timeline feed: a day header, or an entry. */
sealed interface TimelineItem {
    data class Day(val date: LocalDate) : TimelineItem

    /** [photos] are the entry's live photos in album order; audio and video are not shown here. */
    data class EntryItem(val entry: Entry, val photos: List<MediaUi>) : TimelineItem
}
