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
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.domain.model.toDraft
import vn.id.tozydev.tusu.domain.repository.EntryRepository
import vn.id.tozydev.tusu.domain.repository.MediaFileStore
import vn.id.tozydev.tusu.ui.component.AppMessages
import vn.id.tozydev.tusu.ui.component.UiMessage
import vn.id.tozydev.tusu.ui.component.toUi
import vn.id.tozydev.tusu.ui.format.toUiText

@ContributesIntoMap(AppScope::class)
@ViewModelKey(TimelineViewModel::class)
@Inject
class TimelineViewModel(
    private val entryRepository: EntryRepository,
    private val files: MediaFileStore,
    private val appMessages: AppMessages,
) : ViewModel() {
    /** Paging data is its own stream, so it is not folded into a `UiState`. */
    val items: Flow<PagingData<TimelineItem>> =
        entryRepository
            .observeTimeline()
            .map { page -> page.map { it.toEntryItem(files) } }
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

    /**
     * Sets the emoji of [entryId] straight from the feed; null removes it. The entry is read again
     * so a paged copy that is a little behind cannot overwrite newer text.
     */
    fun onEmojiChange(entryId: Uuid, emoji: String?) {
        viewModelScope.launch {
            val entry = entryRepository.observeEntry(entryId).first()
            if (entry == null || entry.isDeleted || entry.emoji == emoji) return@launch
            val outcome = entryRepository.save(entry.toDraft().copy(emoji = emoji))
            if (outcome is Outcome.Failure) appMessages.post(UiMessage(outcome.error.toUiText()))
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

/** Only photos are shown in the feed; the repository already drops soft-deleted media. */
internal fun Entry.toEntryItem(files: MediaFileStore): TimelineItem.EntryItem =
    TimelineItem.EntryItem(
        entry = this,
        photos =
            media
                .filter { it.type == MediaType.PHOTO && it.deletedAt == null }
                .sortedBy { it.position }
                .map { it.toUi(files) },
    )

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
