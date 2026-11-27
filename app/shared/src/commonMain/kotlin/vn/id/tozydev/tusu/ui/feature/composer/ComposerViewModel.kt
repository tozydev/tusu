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
import kotlin.concurrent.Volatile
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.time.TimeSource
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import vn.id.tozydev.tusu.domain.error.EntryError
import vn.id.tozydev.tusu.domain.error.LocationError
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.error.PermissionError
import vn.id.tozydev.tusu.domain.error.getOrNull
import vn.id.tozydev.tusu.domain.model.EntryDraft
import vn.id.tozydev.tusu.domain.model.EntryLocation
import vn.id.tozydev.tusu.domain.model.LocationMode
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.domain.model.toDraft
import vn.id.tozydev.tusu.domain.repository.EntryRepository
import vn.id.tozydev.tusu.domain.repository.LabelRepository
import vn.id.tozydev.tusu.domain.repository.MediaFileStore
import vn.id.tozydev.tusu.domain.repository.MediaRepository
import vn.id.tozydev.tusu.domain.repository.SettingsRepository
import vn.id.tozydev.tusu.domain.usecase.AttachMediaUseCase
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.action_open_settings
import vn.id.tozydev.tusu.generated.resources.action_undo
import vn.id.tozydev.tusu.generated.resources.composer_empty_trashed
import vn.id.tozydev.tusu.generated.resources.composer_saved_message
import vn.id.tozydev.tusu.platform.AudioRecorder
import vn.id.tozydev.tusu.platform.AudioRecording
import vn.id.tozydev.tusu.platform.LocationProvider
import vn.id.tozydev.tusu.platform.MediaJobQueue
import vn.id.tozydev.tusu.platform.label
import vn.id.tozydev.tusu.text.matchLabelHashtags
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
    private val labelRepository: LabelRepository,
    private val mediaRepository: MediaRepository,
    private val attachMedia: AttachMediaUseCase,
    private val mediaFiles: MediaFileStore,
    private val mediaJobQueue: MediaJobQueue,
    private val audioRecorder: AudioRecorder,
    private val locationProvider: LocationProvider,
    private val settingsRepository: SettingsRepository,
    private val appMessages: AppMessages,
    private val clock: Clock,
    private val timeZone: TimeZone,
    private val appScope: CoroutineScope,
) : ViewModel() {
    /** Owned here so the text survives configuration changes; the screen edits it directly. */
    // The editor keeps no history of its own: [history] covers text together with the rest, so
    // one Undo goes back one step whatever it changed.
    val richTextState = RichTextState(historyLimit = 0)

    /** Applies Markdown typed into [richTextState]; runs with each text change. */
    private val markdownShortcuts = MarkdownShortcuts(richTextState)

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

    /** Set once the user picks or removes a place, so a late automatic location does not win. */
    @Volatile private var locationTouched = false

    /** Labels a #hashtag has already attached in this session. */
    private val hashtagLabelIds = mutableSetOf<Uuid>()

    /** Photo work of this session. Closing joins it before it decides what the session kept. */
    private val mediaJobs = mutableListOf<Job>()
    /** Written by the photo jobs on [appScope], which can run on several threads at once. */
    private val mediaLock = Mutex()
    private val addedMedia = mutableSetOf<Uuid>()
    private val removedMedia = mutableSetOf<Uuid>()

    /** The recording in progress. A state flow for its compare-and-set: its jobs run off main. */
    private val activeRecording = MutableStateFlow<ActiveRecording?>(null)

    private val history = ComposerHistory()

    /** The body as of the last text change, the "before" state of the next typing step. */
    private var lastBody = ""

    /** Running while the user keeps typing; changes until it ends join the same step. */
    private var typingStep: Job? = null

    /** True while Undo or Redo puts a state back, so that is not recorded as a new step. */
    private var restoring = false

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
     * Starts the session for [entryId], or for a new entry when it is null. A new entry written for
     * an earlier [date] keeps the current time of day on that date. Only the first call counts.
     */
    fun bind(entryId: Uuid?, date: LocalDate? = null) {
        if (bound) return
        bound = true
        _uiState.update { it.copy(currentTimeZone = timeZone) }
        observeLabels()
        observeRecentLocations()
        if (entryId == null) startNewEntry(date) else sessionScope.launch { openEntry(entryId) }
    }

    /** Called whenever the rich text changes. Saves only when the content really changed. */
    fun onBodyChanged() {
        markdownShortcuts.onTextChange()
        if (editable && !restoring) recordTyping()
        syncDraft()
    }

    /** Goes back one step: text, emoji, labels, place or media, whichever changed. */
    fun onUndo() {
        val current = snapshot() ?: return
        history.undo(current)?.let(::restore)
    }

    fun onRedo() {
        val current = snapshot() ?: return
        history.redo(current)?.let(::restore)
    }

    /**
     * Opens a typing step on the first change after a pause and keeps it open while changes keep
     * coming, so a burst of keystrokes is undone at once.
     */
    private fun recordTyping() {
        val body = richTextState.toJson()
        if (body == lastBody) return
        if (typingStep == null) snapshot(body = lastBody)?.let(::recordSnapshot)
        lastBody = body
        typingStep?.cancel()
        typingStep = sessionScope.launch {
            delay(TYPING_PAUSE)
            typingStep = null
        }
    }

    /** Records the state before a change that is not typing; it also ends a typing step. */
    private fun recordStep() {
        if (restoring) return
        endTypingStep()
        snapshot()?.let(::recordSnapshot)
    }

    private fun recordSnapshot(before: ComposerSnapshot) {
        history.record(before)
        publishHistory()
    }

    private fun endTypingStep() {
        typingStep?.cancel()
        typingStep = null
    }

    private fun publishHistory() {
        _uiState.update { it.copy(canUndo = history.canUndo, canRedo = history.canRedo) }
    }

    private fun snapshot(body: String = richTextState.toJson()): ComposerSnapshot? {
        val target = target ?: return null
        if (!editable) return null
        val state = _uiState.value
        return ComposerSnapshot(
            body = body,
            emoji = target.emoji,
            labelIds = target.labelIds,
            location = target.location,
            mediaIds = (state.photos + state.voices).map { it.id }.toSet(),
        )
    }

    /** Puts [snapshot] back and saves it like any other edit. */
    private fun restore(snapshot: ComposerSnapshot) {
        val target = target ?: return
        endTypingStep()
        restoring = true
        try {
            if (snapshot.body != richTextState.toJson()) {
                richTextState.setJson(snapshot.body)
                markdownShortcuts.sync()
            }
            lastBody = snapshot.body
            target.emoji = snapshot.emoji
            target.location = snapshot.location
            val labelsChanged = target.labelIds != snapshot.labelIds
            target.labelIds = snapshot.labelIds
            _uiState.update {
                it.copy(
                    emoji = snapshot.emoji,
                    location = snapshot.location,
                    selectedLabelIds = snapshot.labelIds,
                )
            }
            if (labelsChanged) writeLabels(target, snapshot.labelIds)
            restoreMedia(target.id, snapshot.mediaIds)
            syncDraft()
        } finally {
            restoring = false
        }
        publishHistory()
    }

    /** Removes the media added since [mediaIds] was taken and brings back the media removed. */
    private fun restoreMedia(entryId: Uuid, mediaIds: Set<Uuid>) {
        val shown = _uiState.value.let { (it.photos + it.voices).map { media -> media.id }.toSet() }
        if (shown == mediaIds) return
        submitted = true
        trackMediaWork {
            val live =
                mediaRepository
                    .observeEntryMedia(entryId)
                    .first()
                    .filter { it.deletedAt == null }
                    .map { it.id }
                    .toSet()
            for (id in live - mediaIds) removeMediaNow(id)
            for (id in mediaIds - live) {
                if (mediaSucceeded(mediaRepository.restore(id))) {
                    mediaLock.withLock { if (!removedMedia.remove(id)) addedMedia += id }
                }
            }
        }
    }

    /** Sets or clears the emoji. A persisted entry is auto-saved like text. */
    fun onEmojiChange(emoji: String?) {
        val target = target ?: return
        if (!editable) return
        recordStep()
        target.emoji = emoji
        _uiState.update { it.copy(emoji = emoji) }
        syncDraft()
    }

    /**
     * Adds or removes a label, following the selection kind of its group. A new entry is written
     * first, because labels need its row; the work is tracked so closing waits for it.
     */
    fun onLabelToggle(labelId: Uuid) {
        val target = target ?: return
        if (!editable) return
        val state = _uiState.value
        val label = state.labels.firstOrNull { it.id == labelId } ?: return
        recordStep()
        val next = toggledLabels(target.labelIds, label, state.labelGroups, state.labels)
        target.labelIds = next
        _uiState.update { it.copy(selectedLabelIds = next) }
        writeLabels(target, next)
    }

    /** Creates a label in [groupId] (null for "Khác") and attaches it to this entry. */
    fun onCreateLabel(name: String, groupId: Uuid?) {
        val target = target ?: return
        val trimmed = name.trim()
        if (!editable || trimmed.isEmpty()) return
        recordStep()
        trackMediaWork {
            when (val outcome = labelRepository.createLabel(trimmed, groupId)) {
                is Outcome.Failure -> appMessages.post(UiMessage(outcome.error.toUiText()))
                is Outcome.Success -> {
                    val created = outcome.value
                    _uiState.update { state ->
                        if (state.labels.any { it.id == created.id }) state
                        else state.copy(labels = state.labels + created)
                    }
                    val next =
                        toggledLabels(
                            target.labelIds,
                            created,
                            _uiState.value.labelGroups,
                            _uiState.value.labels,
                        )
                    target.labelIds = next
                    _uiState.update { it.copy(selectedLabelIds = next) }
                    writeLabels(target, next)
                }
            }
        }
    }

    private fun writeLabels(target: EntryTarget, labelIds: Set<Uuid>) {
        if (target.original == null && !submitted) writeDraftNow(target)
        submitted = true
        trackMediaWork {
            // A new entry's row may still be waiting in the debounce; labels need it written.
            if (target.original == null && !awaitEntryWritten()) return@trackMediaWork
            val outcome = entryRepository.setLabels(target.id, labelIds)
            if (outcome is Outcome.Failure) appMessages.post(UiMessage(outcome.error.toUiText()))
        }
    }

    /** Keeps the groups and labels the label sheet offers in step with the database. */
    private fun observeLabels() {
        sessionScope.launch {
            combine(labelRepository.observeGroups(), labelRepository.observeLabels()) {
                    groups,
                    labels ->
                    groups to labels
                }
                .collect { (groups, labels) ->
                    _uiState.update { it.copy(labelGroups = groups, labels = labels) }
                }
        }
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
     * Sets the place of the entry, a free-typed name or a found one; null removes it. Like the
     * emoji, a place alone does not create a new entry.
     */
    fun onLocationChange(location: EntryLocation?) {
        if (!editable) return
        locationTouched = true
        recordStep()
        setLocation(location)
    }

    /** Finds where the user is now. The location permission must already be granted. */
    fun onUseCurrentLocation() {
        if (!editable || _uiState.value.isLocating) return
        locationTouched = true
        sessionScope.launch {
            when (val found = locate()) {
                is Outcome.Success -> {
                    recordStep()
                    setLocation(found.value)
                }
                is Outcome.Failure -> appMessages.post(UiMessage(found.error.toUiText()))
            }
        }
    }

    /** The location permission was refused; a permanent refusal offers [openSettings]. */
    fun onLocationPermissionDenied(
        error: LocationError.PermissionRequired,
        openSettings: () -> Unit,
    ) {
        appMessages.post(permissionMessage(error.toUiText(), error.error, openSettings))
    }

    private fun setLocation(location: EntryLocation?) {
        val target = target ?: return
        target.location = location
        _uiState.update { it.copy(location = location, suggestedLocation = null) }
        syncDraft()
    }

    /**
     * Places a new entry where the user is, as the location setting asks. Nothing is reported when
     * this fails: the user did not ask for it, and "Add location" stays available.
     */
    private fun autoLocate() {
        sessionScope.launch {
            val mode = settingsRepository.settings.first().locationMode
            if (mode == LocationMode.OFF) return@launch
            val found = locate().getOrNull() ?: return@launch
            // A place the user chose meanwhile wins over the one found for them.
            if (locationTouched) return@launch
            if (mode == LocationMode.ALWAYS) setLocation(found)
            else _uiState.update { it.copy(suggestedLocation = found) }
        }
    }

    private suspend fun locate(): Outcome<EntryLocation, LocationError> {
        _uiState.update { it.copy(isLocating = true) }
        try {
            val point =
                when (val fix = locationProvider.currentLocation()) {
                    is Outcome.Success -> fix.value
                    is Outcome.Failure -> return fix
                }
            return when (val named = locationProvider.reverseGeocode(point)) {
                is Outcome.Success -> named
                // The geocoder needs the network; the coordinates still place the entry.
                is Outcome.Failure ->
                    Outcome.Success(EntryLocation(name = point.label(), coordinates = point))
            }
        } finally {
            _uiState.update { it.copy(isLocating = false) }
        }
    }

    private fun observeRecentLocations() {
        sessionScope.launch {
            entryRepository.observeRecentLocations(RECENT_LOCATIONS).collect { recent ->
                _uiState.update { it.copy(recentLocations = recent) }
            }
        }
    }

    /**
     * Stores the photos in the order given, on [appScope]: the session may close while files are
     * still being copied. A new entry is written first, because a photo needs its entry row.
     */
    fun onPhotosPicked(photos: List<PickedPhoto>) {
        val target = target ?: return
        if (photos.isEmpty() || !editable) return
        recordStep()
        val isNewEntry = target.original == null && !submitted
        if (isNewEntry) writeDraftNow(target)
        submitted = true
        trackMediaWork {
            if (isNewEntry && !awaitEntryWritten()) return@trackMediaWork
            for (photo in photos) attachPhoto(target.id, photo)
        }
    }

    /**
     * Starts recording a voice into the media folder. The microphone permission must already be
     * granted; a new entry is written first, because the recording needs its row.
     */
    fun onStartRecording() {
        val target = target ?: return
        if (!editable) return
        val id = Uuid.random()
        val active = ActiveRecording(id, mediaFiles.newRelativePath(id, AUDIO_EXTENSION))
        if (!activeRecording.compareAndSet(null, active)) return
        val isNewEntry = target.original == null && !submitted
        if (isNewEntry) writeDraftNow(target)
        submitted = true
        _uiState.update { it.copy(recording = RecordingUi(TimeSource.Monotonic.markNow())) }
        active.levelsJob = sessionScope.launch { showLevels() }
        active.startJob = trackMediaWork {
            if (isNewEntry && !awaitEntryWritten()) {
                abandon(active)
                return@trackMediaWork
            }
            when (val started = audioRecorder.start(mediaFiles.resolve(active.relativePath))) {
                is Outcome.Success -> active.started = true
                is Outcome.Failure -> {
                    appMessages.post(UiMessage(started.error.toUiText()))
                    abandon(active)
                }
            }
        }
    }

    /** Stops the recording and attaches it to the entry. */
    fun onStopRecording() {
        if (activeRecording.value != null) recordStep()
        endRecording(keep = true)
    }

    /** Stops the recording and throws it away. */
    fun onCancelRecording() {
        endRecording(keep = false)
    }

    /**
     * The microphone was refused. A permanent refusal offers [openSettings], since the system no
     * longer shows its prompt.
     */
    fun onRecordingPermissionDenied(
        error: MediaError.PermissionRequired,
        openSettings: () -> Unit,
    ) {
        appMessages.post(permissionMessage(error.toUiText(), error.error, openSettings))
    }

    /** Only system settings can undo a permanent refusal, so that message links there. */
    private fun permissionMessage(
        text: UiText,
        error: PermissionError,
        openSettings: () -> Unit,
    ): UiMessage =
        if (error is PermissionError.DeniedPermanently) {
            UiMessage(
                text,
                UiMessageAction(UiText.of(Res.string.action_open_settings), openSettings),
            )
        } else {
            UiMessage(text)
        }

    private suspend fun showLevels() {
        audioRecorder.amplitudes.collect { level ->
            _uiState.update { state ->
                val recording = state.recording ?: return@update state
                state.copy(
                    recording =
                        recording.copy(levels = (recording.levels + level).takeLast(LIVE_LEVELS)),
                )
            }
        }
    }

    /** Clears a recording that never started. */
    private fun abandon(active: ActiveRecording) {
        if (!activeRecording.compareAndSet(active, null)) return
        active.levelsJob?.cancel()
        _uiState.update { it.copy(recording = null) }
    }

    private fun endRecording(keep: Boolean) {
        val active = activeRecording.getAndUpdate { null } ?: return
        active.levelsJob?.cancel()
        _uiState.update { it.copy(recording = null) }
        val entryId = target?.id ?: return
        trackMediaWork {
            active.startJob?.join()
            if (!active.started) return@trackMediaWork
            if (!keep) {
                audioRecorder.cancel()
                return@trackMediaWork
            }
            when (val stopped = audioRecorder.stop()) {
                is Outcome.Failure -> appMessages.post(UiMessage(stopped.error.toUiText()))
                is Outcome.Success -> attachRecording(entryId, active, stopped.value)
            }
        }
    }

    private suspend fun attachRecording(
        entryId: Uuid,
        active: ActiveRecording,
        recording: AudioRecording,
    ) {
        val outcome =
            attachMedia.register(
                entryId = entryId,
                id = active.id,
                relativePath = active.relativePath,
                type = MediaType.AUDIO,
                duration = recording.duration,
                waveform = recording.waveform,
            )
        when (outcome) {
            is Outcome.Success -> mediaLock.withLock { addedMedia += outcome.value.id }
            is Outcome.Failure -> appMessages.post(UiMessage(outcome.error.toUiText()))
        }
    }

    /** Soft-deletes a photo or voice; Undo of this session brings it back. */
    fun onRemoveMedia(mediaId: Uuid) {
        if (!editable) return
        recordStep()
        removeMedia(mediaId)
    }

    private fun removeMedia(mediaId: Uuid) {
        submitted = true
        trackMediaWork { removeMediaNow(mediaId) }
    }

    private suspend fun removeMediaNow(mediaId: Uuid) {
        when (val outcome = mediaRepository.remove(mediaId)) {
            is Outcome.Success ->
                mediaLock.withLock {
                    // A photo added and removed in the same session is not kept, so there is
                    // nothing for the closing Undo to restore.
                    if (!addedMedia.remove(mediaId)) removedMedia += mediaId
                }
            is Outcome.Failure -> appMessages.post(UiMessage(outcome.error.toUiText()))
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

    /** The system picker or camera could not hand over a photo. */
    fun onPickerFailed() {
        appMessages.post(UiMessage(MediaError.FileUnreadable.toUiText()))
    }

    fun onBackground() {
        // Android stops delivering microphone input to an app in the background, so the
        // recording ends here and is kept rather than going silent.
        endRecording(keep = true)
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
        // A recording still running when the composer closes is kept, like everything else.
        endRecording(keep = true)
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

    private fun startNewEntry(date: LocalDate?) {
        val current = clock.now()
        val now =
            if (date == null) current
            else LocalDateTime(date, current.toLocalDateTime(timeZone).time).toInstant(timeZone)
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
        lastBody = richTextState.toJson()
        editable = true
        _uiState.update { it.copy(isLoading = false, occurredAt = now, timeZone = timeZone) }
        observeMedia(newTarget.id)
        autoLocate()
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
                originalLabelIds = entry.labels.map { it.id }.toSet(),
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
        lastBody = richTextState.toJson()
        editable = true
        _uiState.update {
            it.copy(
                isLoading = false,
                occurredAt = entry.occurredAt,
                timeZone = entry.timeZone,
                emoji = entry.emoji,
                selectedLabelIds = existing.labelIds,
                location = existing.location,
            )
        }
        observeMedia(existing.id)
    }

    /** Keeps the photos and voices of the state in step with the entry's album, in order. */
    private fun observeMedia(entryId: Uuid) {
        sessionScope.launch {
            mediaRepository.observeEntryMedia(entryId).collect { items ->
                val live = items.filter { it.deletedAt == null }.sortedBy { it.position }
                val photos = live.filter { it.type == MediaType.PHOTO }.map { it.toUi(mediaFiles) }
                val voices = live.filter { it.type == MediaType.AUDIO }.map { it.toUi(mediaFiles) }
                _uiState.update { it.copy(photos = photos, voices = voices) }
            }
        }
    }

    /** Starts [block] on [appScope] and remembers it, so closing the composer can wait for it. */
    private fun trackMediaWork(block: suspend () -> Unit): Job =
        appScope.launch { block() }.also { mediaJobs += it }

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
            // The loaded text is not something typed, so it must not trigger a shortcut.
            markdownShortcuts.sync()
            true
        } catch (e: MalformedRichTextJsonException) {
            false
        } catch (e: UnsupportedRichTextJsonVersionException) {
            false
        }
    }

    /** The draft the session currently describes. */
    private fun draftOf(target: EntryTarget): EntryDraft =
        EntryDraft(
            id = target.id,
            body = richTextState.toJson(),
            emoji = target.emoji,
            occurredAt = target.occurredAt,
            timeZone = target.timeZone,
            location = target.location,
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
        applyHashtags(target)
    }

    /**
     * Attaches the labels that the text mentions as `#name`, in any group. Each label is attached
     * by a hashtag only once per session, so removing its chip is not undone by the next keystroke.
     */
    private fun applyHashtags(target: EntryTarget) {
        val state = _uiState.value
        val live = state.labels.filter { it.deletedAt == null }
        if (live.isEmpty()) return
        val names = matchLabelHashtags(richTextState.annotatedString.text, live.map { it.name })
        val fresh = live.filter { it.name in names && it.id !in hashtagLabelIds }
        if (fresh.isEmpty()) return
        var next = target.labelIds
        for (label in fresh) {
            hashtagLabelIds += label.id
            if (label.id !in next) next = toggledLabels(next, label, state.labelGroups, live)
        }
        if (next == target.labelIds) return
        target.labelIds = next
        _uiState.update { it.copy(selectedLabelIds = next) }
        writeLabels(target, next)
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
            target
                ?.takeIf { it.labelIds != it.originalLabelIds }
                ?.let { succeeded(entryRepository.setLabels(id, it.originalLabelIds)) }
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
        const val AUDIO_EXTENSION = "m4a"

        /** Live levels the recording bar draws. */
        const val LIVE_LEVELS = 40

        /** Places offered in the location sheet. */
        const val RECENT_LOCATIONS = 5

        /** A pause this long ends a typing step for Undo. */
        val TYPING_PAUSE = 1.seconds
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
    /** Labels when the session started; Undo writes them back. */
    val originalLabelIds: Set<Uuid> = emptySet(),
    var labelIds: Set<Uuid> = originalLabelIds,
    /** Where the entry happened in this session; Undo writes back the one in [original]. */
    var location: EntryLocation? = original?.location,
)

/** A recording this session started; [started] is set once the recorder runs. */
private class ActiveRecording(val id: Uuid, val relativePath: String) {
    var startJob: Job? = null
    var levelsJob: Job? = null
    /** Written by [startJob] and read only after joining it. */
    var started = false
}
