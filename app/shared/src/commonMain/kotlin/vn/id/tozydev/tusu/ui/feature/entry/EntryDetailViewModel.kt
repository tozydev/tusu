package vn.id.tozydev.tusu.ui.feature.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlin.uuid.Uuid
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.domain.repository.EntryRepository
import vn.id.tozydev.tusu.domain.repository.MediaFileStore
import vn.id.tozydev.tusu.ui.component.toUi

@ContributesIntoMap(AppScope::class)
@ViewModelKey(EntryDetailViewModel::class)
@Inject
class EntryDetailViewModel(
    private val entryRepository: EntryRepository,
    private val files: MediaFileStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EntryDetailUiState())
    val uiState: StateFlow<EntryDetailUiState> = _uiState.asStateFlow()

    private var boundEntryId: Uuid? = null
    private var observation: Job? = null

    /**
     * Starts observing [entryId]; the screen keeps observing, so later edits show up on their own.
     * Repeated calls with the same id do nothing.
     */
    fun bind(entryId: Uuid) {
        if (boundEntryId == entryId) return
        boundEntryId = entryId
        observation?.cancel()
        observation = viewModelScope.launch {
            entryRepository
                .observeEntry(entryId)
                .map { entry -> entry.toUiState(files) }
                .collect { _uiState.value = it }
        }
    }
}

private fun Entry?.toUiState(files: MediaFileStore): EntryDetailUiState {
    if (this == null || isDeleted) return EntryDetailUiState(isLoading = false, isGone = true)
    return EntryDetailUiState(
        isLoading = false,
        emoji = emoji,
        occurredAt = occurredAt,
        timeZone = timeZone,
        body = body,
        textPlain = textPlain,
        photos =
            media
                .filter { it.type == MediaType.PHOTO && it.deletedAt == null }
                .sortedBy { it.position }
                .map { it.toUi(files) },
    )
}
