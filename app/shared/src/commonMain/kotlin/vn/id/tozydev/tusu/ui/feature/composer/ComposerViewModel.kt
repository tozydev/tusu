package vn.id.tozydev.tusu.ui.feature.composer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.json.MalformedRichTextJsonException
import com.mohamedrejeb.richeditor.json.UnsupportedRichTextJsonVersionException
import com.mohamedrejeb.richeditor.json.setJson
import com.mohamedrejeb.richeditor.json.toJson
import com.mohamedrejeb.richeditor.model.RichTextState
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import vn.id.tozydev.tusu.domain.error.EntryError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.EntryDraft
import vn.id.tozydev.tusu.domain.model.toDraft
import vn.id.tozydev.tusu.domain.repository.EntryRepository
import vn.id.tozydev.tusu.domain.repository.MediaRepository
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.action_undo
import vn.id.tozydev.tusu.generated.resources.composer_empty_trashed
import vn.id.tozydev.tusu.generated.resources.composer_saved_message
import vn.id.tozydev.tusu.ui.component.AppMessages
import vn.id.tozydev.tusu.ui.component.UiMessage
import vn.id.tozydev.tusu.ui.component.UiMessageAction
import vn.id.tozydev.tusu.ui.format.UiText
import vn.id.tozydev.tusu.ui.format.toUiText
import vn.id.tozydev.tusu.util.AutoSaver
import vn.id.tozydev.tusu.util.SaveState

/**
 * One composer session: the rich text being edited, its auto-save, and what closing it does. The
 * session ends in [finishSession], which runs on [appScope] so it completes after the screen is
 * gone.
 */
@OptIn(ExperimentalRichTextApi::class)
@ContributesIntoMap(AppScope::class)
@ViewModelKey(ComposerViewModel::class)
@Inject
class ComposerViewModel(
    private val entryRepository: EntryRepository,
    private val mediaRepository: MediaRepository,
    private val appMessages: AppMessages,
    private val clock: Clock,
    private val timeZone: TimeZone,
    private val appScope: CoroutineScope,
) : ViewModel() {
    /** Owned here so the text survives configuration changes; the screen edits it directly. */
    val richTextState = RichTextState()

    private val _uiState = MutableStateFlow(ComposerUiState())
    val uiState: StateFlow<ComposerUiState> = _uiState.asStateFlow()

    // Not viewModelScope: clearing the screen cancels that scope, and the last keystrokes must
    // still be written after it.
    private val sessionScope =
        CoroutineScope(appScope.coroutineContext + SupervisorJob(appScope.coroutineContext[Job]))

    private val autoSaver =
        AutoSaver<EntryDraft, EntryError>(scope = sessionScope, save = entryRepository::save)

    private var bound = false
    private var target: EntryTarget? = null

    /** False until the body is loaded; a read-only or missing entry never becomes editable. */
    private var editable = false
    private var lastSubmittedJson: String? = null
    private var lastDraft: EntryDraft? = null
    private var submitted = false
    private var finishJob: Job? = null

    init {
        viewModelScope.launch {
            autoSaver.state.collectLatest { state ->
                setSaveIndicator(state.toIndicator())
                if (state == SaveState.Saved) {
                    delay(SAVED_INDICATOR_DURATION)
                    setSaveIndicator(SaveIndicator.Hidden)
                }
            }
        }
    }

    /**
     * Starts the session for [entryId], or for a new entry when it is null. Only the first call
     * counts.
     */
    fun bind(entryId: Uuid?) {
        if (bound) return
        bound = true
        if (entryId == null) startNewEntry() else viewModelScope.launch { openEntry(entryId) }
    }

    /** Called whenever the rich text changes. Saves only when the content really changed. */
    fun onBodyChanged() {
        val target = target ?: return
        if (!editable) return
        val json = richTextState.toJson()
        if (json == lastSubmittedJson) return
        // A new entry is written once it has text; an existing one is always written when edited.
        val persisted = target.original != null || submitted
        if (!persisted && richTextState.annotatedString.text.isBlank()) return
        submit(target, json)
    }

    fun onBackground() {
        autoSaver.flush()
    }

    fun retrySave() {
        autoSaver.retry()
    }

    /**
     * Ends the editing session: waits for pending saves, then applies the empty-entry rule and
     * offers Undo. Later calls return the same job.
     */
    internal fun finishSession(): Job {
        finishJob?.let {
            return it
        }
        // Read on the caller's thread, where the editor state is owned.
        val textIsBlank = richTextState.annotatedString.text.isBlank()
        return appScope
            .launch {
                try {
                    closeSession(textIsBlank)
                } finally {
                    sessionScope.cancel()
                }
            }
            .also { finishJob = it }
    }

    override fun onCleared() {
        finishSession()
    }

    private fun startNewEntry() {
        val now = clock.now()
        target =
            EntryTarget(
                id = Uuid.random(),
                occurredAt = now,
                timeZone = timeZone,
                original = null,
            )
        lastSubmittedJson = richTextState.toJson()
        editable = true
        _uiState.update { it.copy(isLoading = false, occurredAt = now, timeZone = timeZone) }
    }

    private suspend fun openEntry(entryId: Uuid) {
        val entry = entryRepository.observeEntry(entryId).first()
        if (entry == null) {
            _uiState.update {
                it.copy(isLoading = false, loadError = EntryError.NotFound.toUiText())
            }
            return
        }
        target =
            EntryTarget(
                id = entry.id,
                occurredAt = entry.occurredAt,
                timeZone = entry.timeZone,
                original = entry.toDraft(),
            )
        if (!loadBody(entry.body)) {
            _uiState.update {
                it.copy(isLoading = false, isReadOnly = true, readOnlyText = entry.textPlain)
            }
            return
        }
        // The loaded state is the baseline, so opening an entry does not count as an edit.
        lastSubmittedJson = richTextState.toJson()
        editable = true
        _uiState.update {
            it.copy(
                isLoading = false,
                occurredAt = entry.occurredAt,
                timeZone = entry.timeZone,
            )
        }
    }

    /** False when this version cannot read the stored JSON. */
    private fun loadBody(body: String): Boolean {
        if (body.isBlank()) return true
        return try {
            richTextState.setJson(body)
            true
        } catch (e: MalformedRichTextJsonException) {
            false
        } catch (e: UnsupportedRichTextJsonVersionException) {
            false
        }
    }

    private fun submit(target: EntryTarget, json: String) {
        val draft =
            EntryDraft(
                id = target.id,
                body = json,
                emoji = target.original?.emoji,
                occurredAt = target.occurredAt,
                timeZone = target.timeZone,
                location = target.original?.location,
            )
        lastSubmittedJson = json
        lastDraft = draft
        submitted = true
        autoSaver.submit(draft)
    }

    private suspend fun closeSession(textIsBlank: Boolean) {
        autoSaver.awaitIdle()
        val target = target ?: return
        // Nothing typed or changed means nothing to save, delete or report.
        if (!editable || !submitted) return
        if (autoSaver.state.value is SaveState.Failed) {
            // The editor is going away, so retry once before the content would be lost.
            val draft = lastDraft ?: return
            if (!succeeded(entryRepository.save(draft))) return
        }
        val hasLiveMedia =
            mediaRepository.observeEntryMedia(target.id).first().any { it.deletedAt == null }
        val original = target.original
        when {
            !textIsBlank || hasLiveMedia -> postSaved(target.id, original)
            original == null -> succeeded(entryRepository.deletePermanently(target.id))
            else -> {
                if (succeeded(entryRepository.moveToTrash(target.id))) {
                    postEmptyTrashed(target.id, original)
                }
            }
        }
    }

    private fun postSaved(id: Uuid, original: EntryDraft?) {
        appMessages.post(
            UiMessage(
                text = UiText.of(Res.string.composer_saved_message),
                action =
                    UiMessageAction(label = UiText.of(Res.string.action_undo)) {
                        appScope.launch { undoSession(id, original) }
                    },
                timeout = UNDO_DURATION,
            ),
        )
    }

    private fun postEmptyTrashed(id: Uuid, original: EntryDraft) {
        appMessages.post(
            UiMessage(
                text = UiText.of(Res.string.composer_empty_trashed),
                action =
                    UiMessageAction(label = UiText.of(Res.string.action_undo)) {
                        appScope.launch { restoreEmptied(id, original) }
                    },
                timeout = UNDO_DURATION,
            ),
        )
    }

    /**
     * New entry: moved to the trash. Existing entry: the content it had when the composer opened.
     */
    private suspend fun undoSession(id: Uuid, original: EntryDraft?) {
        if (original == null) {
            succeeded(entryRepository.moveToTrash(id))
        } else {
            succeeded(entryRepository.save(original))
        }
    }

    private suspend fun restoreEmptied(id: Uuid, original: EntryDraft) {
        succeeded(entryRepository.save(original))
        succeeded(entryRepository.restoreFromTrash(id))
    }

    /** Posts the error of a failed [outcome] so the user sees it; returns true on success. */
    private fun succeeded(outcome: Outcome<Unit, EntryError>): Boolean =
        when (outcome) {
            is Outcome.Success -> true
            is Outcome.Failure -> {
                appMessages.post(UiMessage(outcome.error.toUiText()))
                false
            }
        }

    private fun setSaveIndicator(indicator: SaveIndicator) {
        _uiState.update { it.copy(saveIndicator = indicator) }
    }

    private fun SaveState<EntryError>.toIndicator(): SaveIndicator =
        when (this) {
            SaveState.Idle -> SaveIndicator.Hidden
            SaveState.Pending,
            SaveState.Saving -> SaveIndicator.Saving
            SaveState.Saved -> SaveIndicator.Saved
            is SaveState.Failed -> SaveIndicator.Failed(error.toUiText())
        }

    private companion object {
        val SAVED_INDICATOR_DURATION = 1500.milliseconds
        val UNDO_DURATION = 5.seconds
    }
}

/** The entry this session writes; [original] is null for a new entry. */
private class EntryTarget(
    val id: Uuid,
    val occurredAt: Instant,
    val timeZone: TimeZone,
    val original: EntryDraft?,
)
