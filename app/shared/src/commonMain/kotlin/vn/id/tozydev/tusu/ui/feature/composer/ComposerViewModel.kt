package vn.id.tozydev.tusu.ui.feature.composer

import androidx.lifecycle.ViewModel
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
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import vn.id.tozydev.tusu.domain.error.EntryError
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.EntryDraft
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.domain.model.toDraft
import vn.id.tozydev.tusu.domain.repository.EntryRepository
import vn.id.tozydev.tusu.domain.repository.MediaFileStore
import vn.id.tozydev.tusu.domain.repository.MediaRepository
import vn.id.tozydev.tusu.domain.usecase.AttachMediaUseCase
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.action_undo
import vn.id.tozydev.tusu.generated.resources.composer_empty_trashed
import vn.id.tozydev.tusu.generated.resources.composer_saved_message
import vn.id.tozydev.tusu.platform.MediaJobQueue
import vn.id.tozydev.tusu.ui.component.AppMessages
import vn.id.tozydev.tusu.ui.component.UiMessage
import vn.id.tozydev.tusu.ui.component.UiMessageAction
import vn.id.tozydev.tusu.ui.component.toUi
import vn.id.tozydev.tusu.ui.format.UiText
import vn.id.tozydev.tusu.ui.format.toUiText
import vn.id.tozydev.tusu.ui.platform.PickedPhoto
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
    private val attachMedia: AttachMediaUseCase,
    private val mediaFiles: MediaFileStore,
    private val mediaJobQueue: MediaJobQueue,
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
    // still be written after it. Every session coroutine runs here, never on Dispatchers.Main.
    private val sessionScope =
        CoroutineScope(appScope.coroutineContext + SupervisorJob(appScope.coroutineContext[Job]))

    private val autoSaver =
        AutoSaver<EntryDraft, EntryError>(scope = sessionScope, save = entryRepository::save)

    private var bound = false
    private var target: EntryTarget? = null

    /** False until the body is loaded; a read-only or missing entry never becomes editable. */
    private var editable = false
    /** The last draft written, or the baseline the session started from. */
    private var lastDraft: EntryDraft? = null
    private var submitted = false
    private var finishJob: Job? = null

    /** Photo work of this session. Closing joins it before it decides what the session kept. */
    private val mediaJobs = mutableListOf<Job>()
    /** Written by the photo jobs on [appScope], which can run on several threads at once. */
    private val mediaLock = Mutex()
    private val addedMedia = mutableSetOf<Uuid>()
    private val removedMedia = mutableSetOf<Uuid>()

    init {
        sessionScope.launch {
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
        _uiState.update { it.copy(currentTimeZone = timeZone) }
        if (entryId == null) startNewEntry() else sessionScope.launch { openEntry(entryId) }
    }

    /** Called whenever the rich text changes. Saves only when the content really changed. */
    fun onBodyChanged() {
        syncDraft()
    }

    /** Sets or clears the emoji. A persisted entry is auto-saved like text. */
    fun onEmojiChange(emoji: String?) {
        val target = target ?: return
        if (!editable) return
        target.emoji = emoji
        _uiState.update { it.copy(emoji = emoji) }
        syncDraft()
    }

    /**
     * Moves the entry to [local], read in the current zone, and makes that zone the entry's own. A
     * persisted entry is auto-saved like text.
     */
    fun onOccurredAtChange(local: LocalDateTime) {
        val target = target ?: return
        if (!editable) return
        val occurredAt = local.toInstant(timeZone)
        target.occurredAt = occurredAt
        target.timeZone = timeZone
        _uiState.update { it.copy(occurredAt = occurredAt, timeZone = timeZone) }
        syncDraft()
    }

    /**
     * Stores the photos in the order given, on [appScope]: the session may close while files are
     * still being copied. A new entry is written first, because a photo needs its entry row.
     */
    fun onPhotosPicked(photos: List<PickedPhoto>) {
        val target = target ?: return
        if (photos.isEmpty() || !editable) return
        val isNewEntry = target.original == null && !submitted
        if (isNewEntry) writeDraftNow(target)
        submitted = true
        trackMediaWork {
            if (isNewEntry && !awaitEntryWritten()) return@trackMediaWork
            for (photo in photos) attachPhoto(target.id, photo)
        }
    }

    /** Soft-deletes a photo; Undo of this session brings it back. */
    fun onRemovePhoto(mediaId: Uuid) {
        if (!editable) return
        submitted = true
        trackMediaWork {
            when (val outcome = mediaRepository.remove(mediaId)) {
                is Outcome.Success ->
                    mediaLock.withLock {
                        // A photo added and removed in the same session is not kept, so there is
                        // nothing for Undo to restore.
                        if (!addedMedia.remove(mediaId)) removedMedia += mediaId
                    }
                is Outcome.Failure -> appMessages.post(UiMessage(outcome.error.toUiText()))
            }
        }
    }

    /** Queues compression again for a photo whose compression failed. */
    fun onRetryPhoto(mediaId: Uuid) {
        if (!editable) return
        if (_uiState.value.photos.none { it.id == mediaId && it.status == MediaStatus.FAILED }) {
            return
        }
        trackMediaWork {
            when (val outcome = mediaRepository.setStatus(mediaId, MediaStatus.PROCESSING)) {
                is Outcome.Success -> mediaJobQueue.enqueueCompression(mediaId)
                is Outcome.Failure -> appMessages.post(UiMessage(outcome.error.toUiText()))
            }
        }
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
        val newTarget =
            EntryTarget(
                id = Uuid.random(),
                occurredAt = now,
                timeZone = timeZone,
                original = null,
                emoji = null,
            )
        target = newTarget
        lastDraft = draftOf(newTarget)
        editable = true
        _uiState.update { it.copy(isLoading = false, occurredAt = now, timeZone = timeZone) }
        observePhotos(newTarget.id)
    }

    private suspend fun openEntry(entryId: Uuid) {
        val entry = entryRepository.observeEntry(entryId).first()
        if (entry == null) {
            _uiState.update {
                it.copy(isLoading = false, loadError = EntryError.NotFound.toUiText())
            }
            return
        }
        val existing =
            EntryTarget(
                id = entry.id,
                occurredAt = entry.occurredAt,
                timeZone = entry.timeZone,
                original = entry.toDraft(),
                emoji = entry.emoji,
            )
        target = existing
        if (!loadBody(entry.body)) {
            _uiState.update {
                it.copy(isLoading = false, isReadOnly = true, readOnlyText = entry.textPlain)
            }
            return
        }
        // The loaded state is the baseline, so opening an entry does not count as an edit.
        lastDraft = draftOf(existing)
        editable = true
        _uiState.update {
            it.copy(
                isLoading = false,
                occurredAt = entry.occurredAt,
                timeZone = entry.timeZone,
                emoji = entry.emoji,
            )
        }
        observePhotos(existing.id)
    }

    /** Keeps [ComposerUiState.photos] in step with the album of the entry, in position order. */
    private fun observePhotos(entryId: Uuid) {
        sessionScope.launch {
            mediaRepository.observeEntryMedia(entryId).collect { items ->
                val photos =
                    items
                        .filter { it.type == MediaType.PHOTO && it.deletedAt == null }
                        .sortedBy { it.position }
                        .map { it.toUi(mediaFiles) }
                _uiState.update { it.copy(photos = photos) }
            }
        }
    }

    /** Starts [block] on [appScope] and remembers it, so closing the composer can wait for it. */
    private fun trackMediaWork(block: suspend () -> Unit) {
        mediaJobs += appScope.launch { block() }
    }

    /** Submits the draft of a new entry even when its text is blank, since media needs the row. */
    private fun writeDraftNow(target: EntryTarget) {
        val draft = draftOf(target)
        lastDraft = draft
        autoSaver.submit(draft)
    }

    /** Waits until the entry is written; false, with the error posted, when the write failed. */
    private suspend fun awaitEntryWritten(): Boolean {
        autoSaver.flush()
        autoSaver.awaitIdle()
        val state = autoSaver.state.value
        if (state is SaveState.Failed) {
            appMessages.post(UiMessage(state.error.toUiText()))
            return false
        }
        return true
    }

    private suspend fun attachPhoto(entryId: Uuid, photo: PickedPhoto) {
        when (val outcome = attachMedia(entryId, photo.open(), MediaType.PHOTO, photo.extension)) {
            is Outcome.Success -> mediaLock.withLock { addedMedia += outcome.value.id }
            is Outcome.Failure -> appMessages.post(UiMessage(outcome.error.toUiText()))
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

    /** The draft the session currently describes; location is kept from the original entry. */
    private fun draftOf(target: EntryTarget): EntryDraft =
        EntryDraft(
            id = target.id,
            body = richTextState.toJson(),
            emoji = target.emoji,
            occurredAt = target.occurredAt,
            timeZone = target.timeZone,
            location = target.original?.location,
        )

    /** Submits the current draft when it differs from the last one written or the baseline. */
    private fun syncDraft() {
        val target = target ?: return
        if (!editable) return
        val draft = draftOf(target)
        if (draft == lastDraft) return
        // A new entry is written once it has text; an existing one is always written when edited.
        val persisted = target.original != null || submitted
        if (!persisted && richTextState.annotatedString.text.isBlank()) return
        lastDraft = draft
        submitted = true
        autoSaver.submit(draft)
    }

    private suspend fun closeSession(textIsBlank: Boolean) {
        // Photo work ends first, so the photos it adds or removes count as this session's changes.
        mediaJobs.toList().joinAll()
        autoSaver.awaitIdle()
        val target = target ?: return
        // Nothing typed or changed means nothing to save, delete or report.
        if (!editable || !submitted) return
        if (autoSaver.state.value is SaveState.Failed) {
            // The editor is going away, so retry once before the content would be lost.
            val draft = lastDraft ?: return
            if (!succeeded(entryRepository.save(draft))) return
        }
        val changes = mediaLock.withLock { MediaChanges(addedMedia.toSet(), removedMedia.toSet()) }
        val hasLiveMedia =
            mediaRepository.observeEntryMedia(target.id).first().any { it.deletedAt == null }
        val original = target.original
        when {
            !textIsBlank || hasLiveMedia -> postSaved(target.id, original, changes)
            original == null -> succeeded(entryRepository.deletePermanently(target.id))
            else -> {
                if (succeeded(entryRepository.moveToTrash(target.id))) {
                    postEmptyTrashed(target.id, original, changes)
                }
            }
        }
    }

    private fun postSaved(id: Uuid, original: EntryDraft?, changes: MediaChanges) {
        appMessages.post(
            UiMessage(
                text = UiText.of(Res.string.composer_saved_message),
                action =
                    UiMessageAction(label = UiText.of(Res.string.action_undo)) {
                        appScope.launch { undoSession(id, original, changes) }
                    },
                timeout = UNDO_DURATION,
            ),
        )
    }

    private fun postEmptyTrashed(id: Uuid, original: EntryDraft, changes: MediaChanges) {
        appMessages.post(
            UiMessage(
                text = UiText.of(Res.string.composer_empty_trashed),
                action =
                    UiMessageAction(label = UiText.of(Res.string.action_undo)) {
                        appScope.launch { restoreEmptied(id, original, changes) }
                    },
                timeout = UNDO_DURATION,
            ),
        )
    }

    /**
     * New entry: moved to the trash. Existing entry: the content and photos it had when the
     * composer opened.
     */
    private suspend fun undoSession(id: Uuid, original: EntryDraft?, changes: MediaChanges) {
        if (original == null) {
            succeeded(entryRepository.moveToTrash(id))
        } else {
            succeeded(entryRepository.save(original))
            revertMedia(changes)
        }
    }

    private suspend fun restoreEmptied(id: Uuid, original: EntryDraft, changes: MediaChanges) {
        succeeded(entryRepository.save(original))
        succeeded(entryRepository.restoreFromTrash(id))
        revertMedia(changes)
    }

    /** Removes the photos this session added and brings back the ones it removed. */
    private suspend fun revertMedia(changes: MediaChanges) {
        changes.added.forEach { mediaSucceeded(mediaRepository.remove(it)) }
        changes.removed.forEach { mediaSucceeded(mediaRepository.restore(it)) }
    }

    /** Posts the error of a failed photo [outcome] so the user sees it; returns true on success. */
    private fun mediaSucceeded(outcome: Outcome<Unit, MediaError>): Boolean =
        when (outcome) {
            is Outcome.Success -> true
            is Outcome.Failure -> {
                appMessages.post(UiMessage(outcome.error.toUiText()))
                false
            }
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

/** The photos one session added and removed, as they were when the session ended. */
private class MediaChanges(val added: Set<Uuid>, val removed: Set<Uuid>)

/** The entry this session writes; [original] is null for a new entry. */
private class EntryTarget(
    val id: Uuid,
    var occurredAt: Instant,
    var timeZone: TimeZone,
    val original: EntryDraft?,
    /** The emoji of this session; it can differ from [original] after the user changes it. */
    var emoji: String?,
)
