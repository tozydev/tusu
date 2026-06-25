package vn.id.tozydev.tusu.ui.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlin.uuid.Uuid
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.domain.repository.EntryRepository
import vn.id.tozydev.tusu.domain.repository.TagRepository
import vn.id.tozydev.tusu.ui.formatter.DateTimeFormatter
import vn.id.tozydev.tusu.ui.model.UiText
import vn.id.tozydev.tusu.util.toLocalDate

@ViewModelKey
@ContributesIntoMap(AppScope::class)
class FeedViewModel(
    entryRepository: EntryRepository,
    tagRepository: TagRepository,
    timeZone: TimeZone,
    dateTimeFormatter: DateTimeFormatter,
) : ViewModel() {
    private val _tagIdFilter = MutableStateFlow<Uuid?>(null)
    val tagIdFilter: StateFlow<Uuid?> = _tagIdFilter.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val feedItemsPaging =
        _tagIdFilter
            .flatMapLatest { tagIdFilter ->
                entryRepository.getEntriesPagingFlow(tagIdFilter = tagIdFilter)
            }
            .map { pagingData ->
                pagingData.map { FeedItemUi.EntryItem(it) }
            }
            .map { pagingData ->
                pagingData.insertSeparators<FeedItemUi.EntryItem, FeedItemUi> { before, after ->
                    val beforeDate = before?.entry?.recordedAt?.toLocalDate(timeZone)
                    val afterDate = after?.entry?.recordedAt?.toLocalDate(timeZone)
                    when {
                        beforeDate == null && afterDate != null -> {
                            FeedItemUi.DateHeader(
                                afterDate,
                                dateTimeFormatter.formatRelativeDate(afterDate),
                            )
                        }

                        beforeDate != null && afterDate != null && beforeDate != afterDate -> {
                            FeedItemUi.DateHeader(
                                afterDate,
                                dateTimeFormatter.formatRelativeDate(afterDate),
                            )
                        }

                        else -> null
                    }
                }
            }
            .cachedIn(viewModelScope)

    val tags =
        tagRepository
            .getTagsFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    fun setTagIdFilter(tagId: Uuid?) {
        _tagIdFilter.update { tagId }
    }
}

sealed interface FeedItemUi {
    data class DateHeader(val date: LocalDate, val text: UiText) : FeedItemUi

    @JvmInline value class EntryItem(val entry: Entry) : FeedItemUi
}
