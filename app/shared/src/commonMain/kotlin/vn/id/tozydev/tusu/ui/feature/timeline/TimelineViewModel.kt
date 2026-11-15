package vn.id.tozydev.tusu.ui.feature.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import vn.id.tozydev.tusu.domain.repository.EntryRepository

@ContributesIntoMap(AppScope::class)
@ViewModelKey(TimelineViewModel::class)
@Inject
class TimelineViewModel(private val entryRepository: EntryRepository) : ViewModel() {
    /** Paging data is its own stream, so it is not folded into a `UiState`. */
    val items: Flow<PagingData<TimelineItem>> =
        entryRepository
            .observeTimeline()
            .map { page -> page.map { TimelineItem.EntryItem(it) } }
            .map { page ->
                page.insertSeparators<TimelineItem.EntryItem, TimelineItem> { before, after ->
                    dayHeaderBefore(before, after)
                }
            }
            .cachedIn(viewModelScope)

    /** The emojis of every day, read by the sticky day headers. */
    val dayEmojis: StateFlow<Map<LocalDate, List<String>>> =
        entryRepository
            .observeDayEmojis()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                emptyMap(),
            )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

/**
 * A day header goes right before the first entry of a day in the feed. The feed is newest first, so
 * the entry after the boundary is the first one of its day.
 */
private fun dayHeaderBefore(
    before: TimelineItem.EntryItem?,
    after: TimelineItem.EntryItem?,
): TimelineItem.Day? {
    val day = after?.entry?.localDate ?: return null
    return if (before == null || before.entry.localDate != day) TimelineItem.Day(day) else null
}
