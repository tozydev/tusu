package vn.id.tozydev.tusu.ui.feature.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import vn.id.tozydev.tusu.domain.error.EntryError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.domain.model.LabelGroup
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.domain.model.toDraft
import vn.id.tozydev.tusu.domain.repository.EntryRepository
import vn.id.tozydev.tusu.domain.repository.LabelRepository
import vn.id.tozydev.tusu.domain.repository.MediaFileStore
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.action_undo
import vn.id.tozydev.tusu.generated.resources.entry_deleted_message
import vn.id.tozydev.tusu.ui.component.AppMessages
import vn.id.tozydev.tusu.ui.component.UiMessage
import vn.id.tozydev.tusu.ui.component.UiMessageAction
import vn.id.tozydev.tusu.ui.component.toUi
import vn.id.tozydev.tusu.ui.format.UiText
import vn.id.tozydev.tusu.ui.format.toUiText

@ContributesIntoMap(AppScope::class)
@ViewModelKey(EntryDetailViewModel::class)
@Inject
class EntryDetailViewModel(
    private val entryRepository: EntryRepository,
    private val files: MediaFileStore,
    labelRepository: LabelRepository,
    private val appMessages: AppMessages,
    private val timeZone: TimeZone,
    private val appScope: CoroutineScope,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EntryDetailUiState(currentTimeZone = timeZone))
    val uiState: StateFlow<EntryDetailUiState> = _uiState.asStateFlow()

    /** Label groups give the chips their order and colour. */
    val labelGroups: StateFlow<List<LabelGroup>> =
        labelRepository
            .observeGroups()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var boundEntryId: Uuid? = null
    private var observation: Job? = null

    /** The last entry read from the database, trashed or not; null until it is loaded. */
    private var latestEntry: Entry? = null

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
                .map { entry ->
                    latestEntry = entry
                    entry.toUiState(files, timeZone)
                }
                .collect { _uiState.value = it }
        }
    }

    /**
     * Moves the entry to [local] in the current zone, and the entry takes that zone. The screen
     * follows the database, so nothing is set here.
     */
    fun onOccurredAtChange(local: LocalDateTime) {
        val entry = latestEntry?.takeUnless { it.isDeleted } ?: return
        val occurredAt = local.toInstant(timeZone)
        viewModelScope.launch {
            postFailure(
                entryRepository.save(
                    entry.toDraft().copy(occurredAt = occurredAt, timeZone = timeZone),
                ),
            )
        }
    }

    /**
     * Moves the entry to the trash and offers Undo. Runs on [appScope] because the screen closes as
     * soon as the entry is trashed, which would cancel [viewModelScope].
     */
    fun onDelete() {
        val entryId = boundEntryId ?: return
        appScope.launch {
            when (val outcome = entryRepository.moveToTrash(entryId)) {
                is Outcome.Success -> appMessages.post(deletedMessage(entryId))
                is Outcome.Failure -> appMessages.post(UiMessage(outcome.error.toUiText()))
            }
        }
    }

    private fun deletedMessage(entryId: Uuid): UiMessage =
        UiMessage(
            text = UiText.of(Res.string.entry_deleted_message),
            action =
                UiMessageAction(label = UiText.of(Res.string.action_undo)) {
                    appScope.launch { postFailure(entryRepository.restoreFromTrash(entryId)) }
                },
            timeout = UNDO_DURATION,
        )

    private fun postFailure(outcome: Outcome<Unit, EntryError>) {
        if (outcome is Outcome.Failure) appMessages.post(UiMessage(outcome.error.toUiText()))
    }

    private companion object {
        val UNDO_DURATION = 5.seconds
    }
}

private fun Entry?.toUiState(files: MediaFileStore, currentTimeZone: TimeZone): EntryDetailUiState {
    if (this == null || isDeleted) {
        return EntryDetailUiState(
            isLoading = false,
            isGone = true,
            currentTimeZone = currentTimeZone,
        )
    }
    return EntryDetailUiState(
        isLoading = false,
        currentTimeZone = currentTimeZone,
        emoji = emoji,
        occurredAt = occurredAt,
        timeZone = timeZone,
        createdAt = createdAt,
        updatedAt = updatedAt,
        labels = labels,
        location = location,
        body = body,
        textPlain = textPlain,
        photos =
            media
                .filter { it.type == MediaType.PHOTO && it.deletedAt == null }
                .sortedBy { it.position }
                .map { it.toUi(files) },
    )
}
