package vn.id.tozydev.tusu.ui.feature.timeline

import kotlinx.datetime.LocalDate
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.ui.component.MediaUi

/** One row of the timeline feed: a day header, or an entry. */
sealed interface TimelineItem {
    data class Day(val date: LocalDate) : TimelineItem

    /** The entry's live photos and voice notes, each in album order; video is not shown yet. */
    data class EntryItem(
        val entry: Entry,
        val photos: List<MediaUi>,
        val voices: List<MediaUi> = emptyList(),
    ) : TimelineItem
}
