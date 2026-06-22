package vn.io.tozyworks.tusu.ui.feature.entryeditor

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedrejeb.richeditor.model.RichTextState
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import vn.io.tozyworks.tusu.domain.model.Media
import vn.io.tozyworks.tusu.domain.model.Tag
import vn.io.tozyworks.tusu.domain.repository.EntryRepository
import vn.io.tozyworks.tusu.domain.repository.MediaRepository
import vn.io.tozyworks.tusu.generated.resources.Res
import vn.io.tozyworks.tusu.generated.resources.error_entry_not_found
import vn.io.tozyworks.tusu.ui.model.UiText

@AssistedInject
class EntryEditorViewModel(
    @Assisted val initialEntryId: Uuid?,
    private val entryRepository: EntryRepository,
    private val mediaRepository: MediaRepository,
    private val appScope: CoroutineScope,
    private val timeZone: TimeZone,
) : ViewModel() {
    private val _uiState = MutableStateFlow<EntryEditorUiState>(EntryEditorUiState.Loading)
    val uiState = _uiState.asStateFlow()

    val contentState = RichTextState()

    init {
        if (initialEntryId != null) {
            loadEntry(initialEntryId)
        } else {
            initializeNewEntry()
        }
    }

    private fun loadEntry(id: Uuid) {
        viewModelScope.launch {
            entryRepository.getEntryFlow(id).collect { entry ->
                if (entry != null) {
                    contentState.setMarkdown(entry.content)
                    val localDateTime = entry.recordedAt.toLocalDateTime(timeZone)
                    _uiState.update {
                        EntryEditorUiState.Loaded(
                            entryId = entry.id,
                            mode = EntryEditorMode.ReadOnly,
                            recordedAt = entry.recordedAt,
                            recordedDate = localDateTime.date,
                            recordedTime = localDateTime.time,
                            emoji = entry.emoji,
                            tags = entry.tags,
                            media = entry.media,
                        )
                    }
                } else {
                    _uiState.update {
                        EntryEditorUiState.Error(UiText(Res.string.error_entry_not_found))
                    }
                }
            }
        }
    }

    private fun initializeNewEntry() {
        viewModelScope.launch {
            val newEntry = entryRepository.createEmptyEntry()
            _uiState.update {
                val localDateTime = newEntry.recordedAt.toLocalDateTime(timeZone)
                EntryEditorUiState.Loaded(
                    entryId = newEntry.id,
                    mode = EntryEditorMode.Edit,
                    recordedAt = newEntry.recordedAt,
                    recordedDate = localDateTime.date,
                    recordedTime = localDateTime.time,
                )
            }
        }
    }

    @OptIn(FlowPreview::class)
    suspend fun observeContentChanges() {
        snapshotFlow { contentState.toMarkdown() }
            .debounce(SAVE_DEBOUNCE)
            .distinctUntilChanged()
            .collectLatest { markdown ->
                updateContent(markdown)
            }
    }

    private suspend fun updateContent(markdown: String) {
        _uiState.value.ifLoaded {
            entryRepository.updateContent(entryId, markdown)
        }
    }

    fun setEditorMode(mode: EntryEditorMode) {
        _uiState.updateLoaded {
            it.copy(mode = mode)
        }
        if (mode == EntryEditorMode.ReadOnly) {
            saveContent()
        }
    }

    fun saveContent() {
        viewModelScope.launch {
            updateContent(contentState.toMarkdown())
        }
    }

    fun onRemoveMedia(mediaId: Uuid) {
        viewModelScope.launch {
            mediaRepository.deleteMedia(mediaId)
        }
        _uiState.updateLoaded {
            it.copy(media = it.media.filterNot { media -> media.id == mediaId })
        }
    }

    fun onExitEditor() {
        if (initialEntryId == null) {
            discardNewEntryIfContentIsBlank()
        }
    }

    private fun discardNewEntryIfContentIsBlank() {
        _uiState.value.ifLoaded {
            contentState.toMarkdown().ifBlank {
                appScope.launch {
                    withContext(Dispatchers.IO) {
                        entryRepository.deleteEntry(entryId)
                    }
                }
            }
        }
    }

    fun setEmoji(emoji: String?) {
        _uiState.updateLoaded {
            viewModelScope.launch {
                entryRepository.updateEmoji(it.entryId, emoji)
            }
            it.copy(emoji = emoji)
        }
    }

    fun setRecordedTime(recordedTime: LocalTime) {
        _uiState.updateLoaded {
            val newRecordedAt = recordedTime.atDate(it.recordedDate).toInstant(timeZone)
            viewModelScope.launch {
                entryRepository.updateRecordedAt(it.entryId, newRecordedAt)
            }
            it.copy(recordedAt = newRecordedAt, recordedTime = recordedTime)
        }
    }

    fun setRecordedDate(epochMillis: Long) {
        _uiState.updateLoaded {
            val newRecordedDate = LocalDate.fromEpochDays(epochMillis.toEpochDays())
            val newRecordedAt = newRecordedDate.atTime(it.recordedTime).toInstant(timeZone)
            viewModelScope.launch {
                entryRepository.updateRecordedAt(it.entryId, newRecordedAt)
            }
            it.copy(recordedDate = newRecordedDate, recordedAt = newRecordedAt)
        }
    }

    private companion object {
        val SAVE_DEBOUNCE = 500.milliseconds
        val DAY_IN_MILLIS = 1.days.inWholeMilliseconds

        private fun Long.toEpochDays() = this / DAY_IN_MILLIS
    }

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(initialEntryId: Uuid?): EntryEditorViewModel
    }
}

sealed interface EntryEditorUiState {
    object Loading : EntryEditorUiState

    data class Loaded(
        val entryId: Uuid,
        val mode: EntryEditorMode,
        val recordedAt: Instant,
        val recordedTime: LocalTime,
        val recordedDate: LocalDate,
        val emoji: String? = null,
        val tags: List<Tag> = emptyList(),
        val media: List<Media> = emptyList(),
    ) : EntryEditorUiState

    data class Error(val message: UiText) : EntryEditorUiState
}

inline fun EntryEditorUiState.ifLoaded(block: EntryEditorUiState.Loaded.() -> Unit) {
    if (this is EntryEditorUiState.Loaded) {
        block()
    }
}

inline fun MutableStateFlow<EntryEditorUiState>.updateLoaded(
    block: (EntryEditorUiState.Loaded) -> EntryEditorUiState
) {
    update {
        if (it is EntryEditorUiState.Loaded) {
            block(it)
        } else {
            it
        }
    }
}

enum class EntryEditorMode {
    ReadOnly,
    Edit;

    val isReadOnly
        get() = this == ReadOnly
}
