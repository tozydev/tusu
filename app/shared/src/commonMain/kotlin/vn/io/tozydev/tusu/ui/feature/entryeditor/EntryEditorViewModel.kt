package vn.io.tozydev.tusu.ui.feature.entryeditor

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
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vinceglb.filekit.PlatformFile
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
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
import vn.io.tozydev.tusu.domain.model.Media
import vn.io.tozydev.tusu.domain.model.Tag
import vn.io.tozydev.tusu.domain.repository.EntryRepository
import vn.io.tozydev.tusu.domain.repository.MediaRepository
import vn.io.tozydev.tusu.domain.repository.TagRepository
import vn.io.tozydev.tusu.generated.resources.Res
import vn.io.tozydev.tusu.generated.resources.error_entry_not_found
import vn.io.tozydev.tusu.ui.model.UiText

@AssistedInject
class EntryEditorViewModel(
    @Assisted val initialEntryId: Uuid?,
    private val entryRepository: EntryRepository,
    private val tagRepository: TagRepository,
    private val mediaRepository: MediaRepository,
    private val appScope: CoroutineScope,
    private val timeZone: TimeZone,
) : ViewModel() {
    private val _uiState = MutableStateFlow<EntryEditorUiState>(EntryEditorUiState.Loading)
    val uiState = _uiState.asStateFlow()

    val allTags =
        tagRepository
            .getTagsFlow()
            .stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

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
            logger.info { "Loading entry $id" }
            val entry = entryRepository.getEntry(id)
            if (entry == null) {
                _uiState.update {
                    EntryEditorUiState.Error(UiText(Res.string.error_entry_not_found))
                }
            } else {
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

    fun addMedia(files: List<PlatformFile>) {
        viewModelScope.launch {
            _uiState.updateLoaded {
                val media = mediaRepository.addMedia(it.entryId, files)
                it.copy(media = it.media + media)
            }
        }
    }

    fun removeMedia(mediaId: Uuid) {
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

    fun selectTag(tagId: Uuid) {
        _uiState.updateLoaded {
            viewModelScope.launch {
                entryRepository.assignTag(it.entryId, tagId)
            }
            it.copy(tags = it.tags + allTags.value.first { tag -> tag.id == tagId })
        }
    }

    fun deselectTag(tagId: Uuid) {
        _uiState.updateLoaded {
            viewModelScope.launch {
                entryRepository.unassignTag(it.entryId, tagId)
            }
            it.copy(tags = it.tags.filterNot { tag -> tag.id == tagId })
        }
    }

    fun createAndSelectTag(name: String) {
        viewModelScope.launch {
            val newTag = tagRepository.createTag(name)

            val currentEntryId = _uiState.value.ifLoaded { entryId } ?: return@launch

            entryRepository.assignTag(currentEntryId, newTag.id)

            _uiState.updateLoaded {
                it.copy(tags = it.tags + newTag)
            }
        }
    }

    fun deleteEntry() {
        _uiState.value.ifLoaded {
            appScope.launch {
                withContext(Dispatchers.IO) {
                    entryRepository.deleteEntry(entryId)
                }
            }
        }
    }

    private companion object {
        val SAVE_DEBOUNCE = 500.milliseconds
        val DAY_IN_MILLIS = 1.days.inWholeMilliseconds

        val logger = KotlinLogging.logger {}

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

inline fun <R> EntryEditorUiState.ifLoaded(block: EntryEditorUiState.Loaded.() -> R): R? =
    if (this is EntryEditorUiState.Loaded) {
        block()
    } else {
        null
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
