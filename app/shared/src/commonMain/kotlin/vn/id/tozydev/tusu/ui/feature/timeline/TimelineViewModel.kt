package vn.id.tozydev.tusu.ui.feature.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.Flow
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.domain.repository.EntryRepository

@ContributesIntoMap(AppScope::class)
@ViewModelKey(TimelineViewModel::class)
@Inject
class TimelineViewModel(entryRepository: EntryRepository) : ViewModel() {
    /** Paging data is its own stream, so it is not folded into a `UiState`. */
    val entries: Flow<PagingData<Entry>> =
        entryRepository.observeTimeline().cachedIn(viewModelScope)
}
