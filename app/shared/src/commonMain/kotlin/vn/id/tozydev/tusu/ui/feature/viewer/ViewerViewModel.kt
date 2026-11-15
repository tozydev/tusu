package vn.id.tozydev.tusu.ui.feature.viewer

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
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.domain.repository.MediaFileStore
import vn.id.tozydev.tusu.domain.repository.MediaRepository
import vn.id.tozydev.tusu.ui.component.toUi

@ContributesIntoMap(AppScope::class)
@ViewModelKey(ViewerViewModel::class)
@Inject
class ViewerViewModel(
    private val mediaRepository: MediaRepository,
    private val files: MediaFileStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ViewerUiState())
    val uiState: StateFlow<ViewerUiState> = _uiState.asStateFlow()

    private var boundEntryId: Uuid? = null
    private var observation: Job? = null

    /** Starts observing the photos of [entryId]; repeated calls with the same id do nothing. */
    fun bind(entryId: Uuid) {
        if (boundEntryId == entryId) return
        boundEntryId = entryId
        observation?.cancel()
        observation = viewModelScope.launch {
            mediaRepository
                .observeEntryMedia(entryId)
                .map { media ->
                    ViewerUiState(
                        isLoading = false,
                        photos =
                            media
                                .filter { it.type == MediaType.PHOTO && it.deletedAt == null }
                                .map { it.toUi(files) },
                    )
                }
                .collect { _uiState.value = it }
        }
    }
}
