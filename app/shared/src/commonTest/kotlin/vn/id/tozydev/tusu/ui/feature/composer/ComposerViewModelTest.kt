@file:OptIn(ExperimentalRichTextApi::class)

package vn.id.tozydev.tusu.ui.feature.composer

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.document.RichTextBlock
import com.mohamedrejeb.richeditor.document.RichTextDocument
import com.mohamedrejeb.richeditor.json.setJson
import com.mohamedrejeb.richeditor.json.toJson
import com.mohamedrejeb.richeditor.model.RichTextState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.io.Buffer
import vn.id.tozydev.tusu.domain.error.EntryError
import vn.id.tozydev.tusu.domain.error.LocationError
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.error.PermissionError
import vn.id.tozydev.tusu.domain.model.AppPermission
import vn.id.tozydev.tusu.domain.model.AppSettings
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.domain.model.EntryLocation
import vn.id.tozydev.tusu.domain.model.LocationMode
import vn.id.tozydev.tusu.domain.model.Media
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType
import vn.id.tozydev.tusu.domain.model.toDraft
import vn.id.tozydev.tusu.domain.usecase.AttachMediaUseCase
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.composer_empty_trashed
import vn.id.tozydev.tusu.generated.resources.composer_saved_message
import vn.id.tozydev.tusu.ui.component.AppMessages
import vn.id.tozydev.tusu.ui.component.UiMessage
import vn.id.tozydev.tusu.ui.format.UiText
import vn.id.tozydev.tusu.ui.format.toUiText
import vn.id.tozydev.tusu.ui.platform.PickedPhoto

private val zone = TimeZone.of("Asia/Ho_Chi_Minh")
private val paris = TimeZone.of("Europe/Paris")
private val fixedNow: Instant = Instant.parse("2026-11-11T03:00:00Z")
private val fixedClock =
    object : Clock {
        override fun now(): Instant = fixedNow
    }

private fun paragraph(text: String) = RichTextDocument(listOf(RichTextBlock(text = text)))

private fun storedBody(text: String): String =
    RichTextState().setRichTextDocument(paragraph(text)).toJson()

private fun existingEntry(
    id: Uuid,
    body: String,
    textPlain: String,
    emoji: String? = null,
): Entry =
    Entry(
        id = id,
        body = body,
        emoji = emoji,
        textPlain = textPlain,
        occurredAt = fixedNow,
        timeZone = zone,
        createdAt = fixedNow,
        updatedAt = fixedNow,
    )

/** Replaces the editor text the way a keystroke does, then reports the change. */
private fun ComposerViewModel.typeText(text: String) {
    richTextState.setRichTextDocument(paragraph(text))
    onBodyChanged()
}

private fun UiText.shouldBeSavedMessage() {
    shouldBeInstanceOf<UiText.Resource>().resource shouldBe Res.string.composer_saved_message
}

private fun photo(content: String): PickedPhoto =
    PickedPhoto(extension = "jpg") { Buffer().also { it.write(content.encodeToByteArray()) } }

private fun photoMedia(
    id: Uuid,
    entryId: Uuid,
    position: Int,
    status: MediaStatus = MediaStatus.READY,
): Media =
    Media(
        id = id,
        entryId = entryId,
        type = MediaType.PHOTO,
        localPath = "$id.jpg",
        position = position,
        status = status,
        updatedAt = fixedNow,
    )

private class ComposerFixture(scope: TestScope, appScope: CoroutineScope) {
    val entries = FakeEntryRepository()
    val labels = FakeLabelRepository()
    val media = FakeMediaRepository()
    val files = FakeMediaFileStore()
    val jobs = FakeMediaJobQueue()
    val recorder = FakeAudioRecorder()
    val location = FakeLocationProvider()
    // Off unless a test turns it on, so other tests see no place they did not set.
    val settings =
        FakeSettingsRepository().also {
            it.state.value = AppSettings(locationMode = LocationMode.OFF)
        }
    val messages = AppMessages()
    val posted = mutableListOf<UiMessage>()
    val viewModel =
        ComposerViewModel(
            entryRepository = entries,
            labelRepository = labels,
            mediaRepository = media,
            attachMedia =
                AttachMediaUseCase(files = files, media = media, clock = fixedClock, jobs = jobs),
            mediaFiles = files,
            mediaJobQueue = jobs,
            audioRecorder = recorder,
            locationProvider = location,
            settingsRepository = settings,
            appMessages = messages,
            clock = fixedClock,
            timeZone = zone,
            appScope = appScope,
        )

    init {
        scope.backgroundScope.launch { messages.messages.collect { posted += it } }
    }
}

/** Runs [block] with the main dispatcher set to the test scheduler, and restores it afterwards. */
private fun composerTest(block: suspend TestScope.(ComposerFixture) -> Unit) = runTest {
    Dispatchers.setMain(StandardTestDispatcher(testScheduler))
    // A foreground scope, not backgroundScope: advanceUntilIdle does not run background work, and
    // the composer starts its photo work on appScope. It is cancelled before Main is reset.
    val appScope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
    try {
        block(ComposerFixture(this, appScope))
    } finally {
        appScope.cancel()
        Dispatchers.resetMain()
    }
}

class ComposerViewModelTest :
    FunSpec(
        {
            test("a new entry is not written before it has text") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    advanceUntilIdle()
                    f.viewModel.onBodyChanged()
                    advanceTimeBy(2.seconds)

                    f.entries.saves.shouldBeEmpty()
                }
            }

            test("a new entry for a chosen day starts on that day at the current time of day") {
                composerTest { f ->
                    f.viewModel.bind(null, date = LocalDate(2026, 11, 5))
                    advanceUntilIdle()
                    val expected = LocalDateTime(2026, 11, 5, 10, 0).toInstant(zone)
                    f.viewModel.uiState.value.occurredAt shouldBe expected

                    f.viewModel.typeText("Back then")
                    advanceTimeBy(1.seconds)
                    f.entries.saves.single().occurredAt shouldBe expected
                }
            }

            test("text is saved once, 500 ms after the change, as rich text JSON") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    f.viewModel.typeText("Hello")

                    advanceTimeBy(499)
                    f.entries.saves.shouldBeEmpty()

                    advanceTimeBy(100)
                    val saved = f.entries.saves.single()
                    saved.body shouldBe f.viewModel.richTextState.toJson()
                    RichTextState().setJson(saved.body).annotatedString.text shouldBe "Hello"
                }
            }

            test("applying bold to saved text saves again with a different body") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    f.viewModel.typeText("Hello")
                    advanceTimeBy(600)
                    val first = f.entries.saves.single()

                    f.viewModel.richTextState.selection = TextRange(0, 5)
                    f.viewModel.richTextState.toggleSpanStyle(
                        SpanStyle(fontWeight = FontWeight.Bold),
                    )
                    f.viewModel.onBodyChanged()
                    advanceTimeBy(600)

                    val second = f.entries.saves.last()
                    second.body shouldNotBe first.body
                    RichTextState().setJson(second.body).annotatedString.text shouldBe "Hello"
                }
            }

            test("changes within the debounce give one save with the latest body") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    f.viewModel.typeText("a")
                    advanceTimeBy(200)
                    f.viewModel.typeText("ab")
                    advanceTimeBy(200)
                    f.viewModel.typeText("abc")
                    advanceTimeBy(600)

                    val saved = f.entries.saves.single()
                    saved.body shouldBe f.viewModel.richTextState.toJson()
                    RichTextState().setJson(saved.body).annotatedString.text shouldBe "abc"
                }
            }

            test("onBackground saves at once without waiting for the debounce") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    f.viewModel.typeText("Hello")
                    runCurrent()
                    f.entries.saves.shouldBeEmpty()

                    f.viewModel.onBackground()
                    runCurrent()

                    f.entries.saves.size shouldBe 1
                }
            }

            test("the indicator shows saving, then saved, then hides 1.5 s later") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    f.viewModel.typeText("Hello")
                    runCurrent()
                    f.viewModel.uiState.value.saveIndicator shouldBe SaveIndicator.Saving

                    advanceTimeBy(600)
                    f.viewModel.uiState.value.saveIndicator shouldBe SaveIndicator.Saved

                    advanceTimeBy(1000)
                    f.viewModel.uiState.value.saveIndicator shouldBe SaveIndicator.Saved

                    advanceTimeBy(600)
                    f.viewModel.uiState.value.saveIndicator shouldBe SaveIndicator.Hidden
                }
            }

            test("a failed save shows the error, and retry saves the kept content") {
                composerTest { f ->
                    f.entries.failNextSave = true
                    f.viewModel.bind(null)
                    f.viewModel.typeText("Hello")
                    advanceTimeBy(600)

                    f.viewModel.uiState.value.saveIndicator
                        .shouldBeInstanceOf<SaveIndicator.Failed>()
                        .message shouldBe EntryError.StorageFailed.toUiText()

                    f.viewModel.retrySave()
                    advanceTimeBy(10)

                    f.viewModel.uiState.value.saveIndicator shouldBe SaveIndicator.Saved
                    f.entries.saves.size shouldBe 2
                }
            }

            test("finishing with a pending change saves it and offers undo that trashes it") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    f.viewModel.typeText("Hello")

                    f.viewModel.finishSession().join()
                    runCurrent()

                    val id = f.entries.saves.single().id
                    f.entries.trashed.shouldBeEmpty()
                    val message = f.posted.single()
                    message.text.shouldBeSavedMessage()
                    message.timeout shouldBe 5.seconds

                    requireNotNull(message.action).onAction()
                    advanceUntilIdle()

                    f.entries.trashed shouldBe listOf(id)
                }
            }

            test("a new entry that was never typed in is not touched on finish") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    f.viewModel.finishSession().join()
                    runCurrent()

                    f.entries.saves.shouldBeEmpty()
                    f.entries.trashed.shouldBeEmpty()
                    f.entries.restored.shouldBeEmpty()
                    f.entries.deleted.shouldBeEmpty()
                    f.posted.shouldBeEmpty()
                }
            }

            test("a new entry typed and then cleared is deleted on finish without a message") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    f.viewModel.typeText("Hello")
                    advanceTimeBy(600)
                    f.viewModel.typeText("")
                    advanceTimeBy(600)

                    f.viewModel.finishSession().join()
                    runCurrent()

                    f.entries.deleted shouldBe listOf(f.entries.saves.first().id)
                    f.entries.trashed.shouldBeEmpty()
                    f.posted.shouldBeEmpty()
                }
            }

            test("an existing entry opened and closed unchanged is neither saved nor reported") {
                composerTest { f ->
                    val id = Uuid.random()
                    f.entries.entries[id] = existingEntry(id, storedBody("Hello"), "Hello")

                    f.viewModel.bind(id)
                    advanceUntilIdle()
                    f.viewModel.onBodyChanged()
                    advanceTimeBy(2.seconds)
                    f.viewModel.finishSession().join()
                    runCurrent()

                    f.viewModel.uiState.value.isLoading shouldBe false
                    f.entries.saves.shouldBeEmpty()
                    f.entries.trashed.shouldBeEmpty()
                    f.posted.shouldBeEmpty()
                }
            }

            test("an existing entry that was changed is reported, and undo saves the original") {
                composerTest { f ->
                    val id = Uuid.random()
                    val original = existingEntry(id, storedBody("Hello"), "Hello")
                    f.entries.entries[id] = original

                    f.viewModel.bind(id)
                    advanceUntilIdle()
                    f.viewModel.typeText("Changed")
                    advanceTimeBy(600)
                    f.viewModel.finishSession().join()
                    runCurrent()

                    f.entries.saves.size shouldBe 1
                    f.posted.single().text.shouldBeSavedMessage()

                    requireNotNull(f.posted.single().action).onAction()
                    advanceUntilIdle()

                    f.entries.saves.last() shouldBe original.toDraft()
                }
            }

            test("an existing entry that was emptied is trashed, and undo restores it") {
                composerTest { f ->
                    val id = Uuid.random()
                    val original = existingEntry(id, storedBody("Hello"), "Hello")
                    f.entries.entries[id] = original

                    f.viewModel.bind(id)
                    advanceUntilIdle()
                    f.viewModel.typeText("")
                    advanceTimeBy(600)
                    f.viewModel.finishSession().join()
                    runCurrent()

                    f.entries.trashed shouldBe listOf(id)
                    val message = f.posted.single()
                    message.text.shouldBeInstanceOf<UiText.Resource>().resource shouldBe
                        Res.string.composer_empty_trashed
                    message.timeout shouldBe 5.seconds

                    requireNotNull(message.action).onAction()
                    advanceUntilIdle()

                    f.entries.saves.last() shouldBe original.toDraft()
                    f.entries.restored shouldBe listOf(id)
                }
            }

            test("an emoji alone does not create a new entry; the first text saves it") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    f.viewModel.onEmojiChange("😊")
                    advanceTimeBy(2.seconds)
                    f.entries.saves.shouldBeEmpty()
                    f.viewModel.uiState.value.emoji shouldBe "😊"

                    f.viewModel.typeText("Hello")
                    advanceTimeBy(600)

                    f.entries.saves.single().emoji shouldBe "😊"
                }
            }

            test("an emoji change on an existing entry is saved 500 ms later") {
                composerTest { f ->
                    val id = Uuid.random()
                    f.entries.entries[id] = existingEntry(id, storedBody("Hello"), "Hello")
                    f.viewModel.bind(id)
                    advanceUntilIdle()

                    f.viewModel.onEmojiChange("🌱")
                    advanceTimeBy(499)
                    f.entries.saves.shouldBeEmpty()

                    advanceTimeBy(100)
                    f.entries.saves.single().emoji shouldBe "🌱"
                }
            }

            test("choosing null clears the emoji and saves the removal") {
                composerTest { f ->
                    val id = Uuid.random()
                    f.entries.entries[id] = existingEntry(id, storedBody("Hello"), "Hello", "😊")
                    f.viewModel.bind(id)
                    advanceUntilIdle()
                    f.viewModel.uiState.value.emoji shouldBe "😊"

                    f.viewModel.onEmojiChange(null)
                    advanceTimeBy(600)

                    f.viewModel.uiState.value.emoji shouldBe null
                    f.entries.saves.single().emoji shouldBe null
                }
            }

            test("undo restores the original emoji") {
                composerTest { f ->
                    val id = Uuid.random()
                    val original = existingEntry(id, storedBody("Hello"), "Hello", "😊")
                    f.entries.entries[id] = original
                    f.viewModel.bind(id)
                    advanceUntilIdle()

                    f.viewModel.onEmojiChange("🌱")
                    advanceTimeBy(600)
                    f.viewModel.finishSession().join()
                    runCurrent()

                    requireNotNull(f.posted.single().action).onAction()
                    advanceUntilIdle()

                    f.entries.saves.last() shouldBe original.toDraft()
                }
            }

            test("an entry that only has an emoji is deleted on finish, as before") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    f.viewModel.typeText("Hello")
                    advanceTimeBy(600)
                    f.viewModel.onEmojiChange("😊")
                    advanceTimeBy(600)
                    f.viewModel.typeText("")
                    advanceTimeBy(600)

                    f.viewModel.finishSession().join()
                    runCurrent()

                    f.entries.deleted shouldBe listOf(f.entries.saves.first().id)
                    f.posted.shouldBeEmpty()
                }
            }

            test("an unknown entry id sets the load error") {
                composerTest { f ->
                    f.viewModel.bind(Uuid.random())
                    advanceUntilIdle()

                    val state = f.viewModel.uiState.value
                    state.isLoading shouldBe false
                    state.loadError.shouldNotBeNull()
                }
            }

            test("an unreadable body is read-only and never saved") {
                composerTest { f ->
                    val id = Uuid.random()
                    f.entries.entries[id] = existingEntry(id, "{not json", "Raw text")

                    f.viewModel.bind(id)
                    advanceUntilIdle()
                    f.viewModel.typeText("Edited")
                    advanceTimeBy(2.seconds)
                    f.viewModel.finishSession().join()
                    runCurrent()

                    val state = f.viewModel.uiState.value
                    state.isReadOnly shouldBe true
                    state.readOnlyText shouldBe "Raw text"
                    state.loadError.shouldBeNull()
                    f.entries.saves.shouldBeEmpty()
                    f.entries.trashed.shouldBeEmpty()
                    f.posted.shouldBeEmpty()
                }
            }
            test("changing the time of a new entry keeps it in memory until the first text") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    f.viewModel.onOccurredAtChange(LocalDateTime(2026, 11, 10, 8, 0))
                    advanceTimeBy(2.seconds)
                    f.entries.saves.shouldBeEmpty()

                    f.viewModel.typeText("Hello")
                    advanceTimeBy(600)

                    val saved = f.entries.saves.single()
                    saved.occurredAt shouldBe LocalDateTime(2026, 11, 10, 8, 0).toInstant(zone)
                    saved.timeZone shouldBe zone
                }
            }

            test("changing the time of an existing entry saves it in the current zone") {
                composerTest { f ->
                    val id = Uuid.random()
                    f.entries.entries[id] =
                        existingEntry(id, storedBody("Hello"), "Hello").copy(timeZone = paris)
                    f.viewModel.bind(id)
                    advanceUntilIdle()
                    f.viewModel.uiState.value.timeZone shouldBe paris
                    f.viewModel.uiState.value.currentTimeZone shouldBe zone

                    f.viewModel.onOccurredAtChange(LocalDateTime(2026, 11, 11, 10, 0))
                    advanceTimeBy(600)

                    val saved = f.entries.saves.single()
                    saved.occurredAt shouldBe LocalDateTime(2026, 11, 11, 10, 0).toInstant(zone)
                    saved.timeZone shouldBe zone
                    f.viewModel.uiState.value.timeZone shouldBe zone
                }
            }

            test("undo restores the original time and zone of an existing entry") {
                composerTest { f ->
                    val id = Uuid.random()
                    val original =
                        existingEntry(id, storedBody("Hello"), "Hello").copy(timeZone = paris)
                    f.entries.entries[id] = original
                    f.viewModel.bind(id)
                    advanceUntilIdle()

                    f.viewModel.onOccurredAtChange(LocalDateTime(2026, 11, 11, 10, 0))
                    advanceTimeBy(600)
                    f.viewModel.finishSession().join()
                    runCurrent()

                    requireNotNull(f.posted.single().action).onAction()
                    advanceUntilIdle()

                    f.entries.saves.last() shouldBe original.toDraft()
                }
            }

            test("a photo picked on an untouched new entry saves the entry, then adds the photo") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    f.viewModel.onPhotosPicked(listOf(photo("first")))
                    advanceUntilIdle()

                    val entry = f.entries.saves.single()
                    val item = f.media.media.single()
                    item.entryId shouldBe entry.id
                    item.status shouldBe MediaStatus.PROCESSING
                    f.jobs.enqueued shouldBe listOf(item.id)
                    f.viewModel.uiState.value.photos.single().id shouldBe item.id
                }
            }

            test("photos picked together keep the order they were picked in") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    f.viewModel.onPhotosPicked(listOf(photo("first"), photo("second")))
                    advanceUntilIdle()

                    f.media.media
                        .sortedBy { it.position }
                        .map { f.files.contentOf(it.localPath) } shouldBe listOf("first", "second")
                }
            }

            test("a photo that cannot be stored posts an error and adds nothing") {
                composerTest { f ->
                    f.files.failWrites = true
                    f.viewModel.bind(null)
                    f.viewModel.onPhotosPicked(listOf(photo("first")))
                    advanceUntilIdle()

                    f.media.media.shouldBeEmpty()
                    f.jobs.enqueued.shouldBeEmpty()
                    f.posted.single().text shouldBe MediaError.StorageFailed.toUiText()
                }
            }

            test("removing a photo takes it out of the strip") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    f.viewModel.onPhotosPicked(listOf(photo("first"), photo("second")))
                    advanceUntilIdle()
                    val (first, second) = f.viewModel.uiState.value.photos.map { it.id }

                    f.viewModel.onRemoveMedia(first)
                    runCurrent()
                    advanceUntilIdle()

                    f.viewModel.uiState.value.photos.map { it.id } shouldBe listOf(second)
                }
            }

            test("retrying a failed photo sets it to PROCESSING and queues compression again") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    f.viewModel.onPhotosPicked(listOf(photo("first")))
                    advanceUntilIdle()
                    val id = f.media.media.single().id
                    f.media.media = f.media.media.map { it.copy(status = MediaStatus.FAILED) }
                    advanceUntilIdle()
                    f.viewModel.uiState.value.photos.single().status shouldBe MediaStatus.FAILED
                    f.jobs.enqueued.clear()

                    f.viewModel.onRetryPhoto(id)
                    advanceUntilIdle()

                    f.media.media.single().status shouldBe MediaStatus.PROCESSING
                    f.jobs.enqueued shouldBe listOf(id)
                }
            }

            test("a new entry with only a photo is kept on finish and reported as saved") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    f.viewModel.onPhotosPicked(listOf(photo("first")))
                    advanceUntilIdle()

                    f.viewModel.finishSession().join()
                    runCurrent()

                    f.entries.trashed.shouldBeEmpty()
                    f.entries.deleted.shouldBeEmpty()
                    f.posted.single().text.shouldBeSavedMessage()
                }
            }

            test("undo removes the photos added in the session from an existing entry") {
                composerTest { f ->
                    val id = Uuid.random()
                    f.entries.entries[id] = existingEntry(id, storedBody("Hello"), "Hello")
                    f.viewModel.bind(id)
                    advanceUntilIdle()

                    f.viewModel.onPhotosPicked(listOf(photo("first")))
                    advanceUntilIdle()
                    f.viewModel.finishSession().join()
                    runCurrent()

                    requireNotNull(f.posted.single().action).onAction()
                    advanceUntilIdle()

                    f.media.media.single().deletedAt.shouldNotBeNull()
                }
            }

            test("undo restores the photos removed in the session to an existing entry") {
                composerTest { f ->
                    val id = Uuid.random()
                    val photoId = Uuid.random()
                    f.entries.entries[id] = existingEntry(id, storedBody("Hello"), "Hello")
                    f.media.media = listOf(photoMedia(photoId, id, position = 0))
                    f.viewModel.bind(id)
                    advanceUntilIdle()

                    f.viewModel.onRemoveMedia(photoId)
                    advanceUntilIdle()
                    f.viewModel.uiState.value.photos.shouldBeEmpty()
                    f.viewModel.finishSession().join()
                    runCurrent()

                    requireNotNull(f.posted.single().action).onAction()
                    advanceUntilIdle()

                    f.media.media.single().deletedAt shouldBe null
                }
            }

            test("closing the composer waits for a photo that is still being stored") {
                composerTest { f ->
                    val id = Uuid.random()
                    f.entries.entries[id] = existingEntry(id, storedBody("Hello"), "Hello")
                    f.viewModel.bind(id)
                    advanceUntilIdle()
                    f.files.writeGate = CompletableDeferred()

                    f.viewModel.onPhotosPicked(listOf(photo("first")))
                    advanceTimeBy(100)
                    val finish = f.viewModel.finishSession()
                    advanceTimeBy(1000)
                    finish.isCompleted shouldBe false

                    f.files.writeGate?.complete(Unit)
                    advanceUntilIdle()

                    finish.isCompleted shouldBe true
                    f.media.media.single().entryId shouldBe id
                    f.posted.single().text.shouldBeSavedMessage()
                }
            }

            test("a label on a new entry writes the entry first, then its labels") {
                composerTest { f ->
                    val group = f.labels.addGroup()
                    val label = f.labels.addLabel(group)
                    f.viewModel.bind(null)
                    advanceUntilIdle()

                    f.viewModel.onLabelToggle(label.id)
                    advanceUntilIdle()

                    val id = f.entries.saves.single().id
                    f.entries.labelSets[id] shouldBe setOf(label.id)
                    f.viewModel.uiState.value.selectedLabelIds shouldBe setOf(label.id)
                }
            }

            test("a new label created in a group is attached right away") {
                composerTest { f ->
                    val group = f.labels.addGroup()
                    f.viewModel.bind(null)
                    f.viewModel.typeText("Hello")
                    advanceTimeBy(1.seconds)

                    f.viewModel.onCreateLabel("Picnic", group.id)
                    advanceUntilIdle()

                    val created = f.labels.labels.value.single { it.name == "Picnic" }
                    f.entries.labelSets[f.entries.saves.first().id] shouldBe setOf(created.id)
                }
            }

            test("a #hashtag attaches the matching label once, and a removed one stays removed") {
                composerTest { f ->
                    val group = f.labels.addGroup()
                    val picnic = f.labels.addLabel(group, name = "Dã ngoại")
                    f.viewModel.bind(null)
                    advanceUntilIdle()

                    f.viewModel.typeText("Cuối tuần #da_ngoai cùng nhà")
                    advanceUntilIdle()
                    val id = f.entries.saves.first().id
                    f.entries.labelSets[id] shouldBe setOf(picnic.id)

                    f.viewModel.onLabelToggle(picnic.id)
                    f.viewModel.typeText("Cuối tuần #da_ngoai cùng nhà, vui lắm")
                    advanceUntilIdle()
                    f.entries.labelSets[id] shouldBe emptySet()
                }
            }

            test("a voice recorded on a new entry is attached with its length and waveform") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    f.viewModel.onStartRecording()
                    advanceUntilIdle()

                    val entry = f.entries.saves.single()
                    f.recorder.isRecording shouldBe true
                    f.viewModel.uiState.value.recording.shouldNotBeNull()

                    f.viewModel.onStopRecording()
                    advanceUntilIdle()

                    val voice = f.media.media.single()
                    voice.entryId shouldBe entry.id
                    voice.type shouldBe MediaType.AUDIO
                    voice.status shouldBe MediaStatus.READY
                    voice.duration shouldBe 3.seconds
                    voice.waveform shouldBe listOf(0.2f, 0.8f, 0.5f)
                    f.files.resolve(voice.localPath) shouldBe f.recorder.destinations.single()
                    f.jobs.enqueued.shouldBeEmpty()
                    f.viewModel.uiState.value.recording.shouldBeNull()
                    f.viewModel.uiState.value.voices.single().id shouldBe voice.id
                    f.viewModel.uiState.value.photos.shouldBeEmpty()
                }
            }

            test("live levels of the recording are shown while it runs") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    f.viewModel.onStartRecording()
                    advanceUntilIdle()

                    f.recorder.levels.emit(0.4f)
                    f.recorder.levels.emit(0.9f)
                    runCurrent()

                    f.viewModel.uiState.value.recording.shouldNotBeNull().levels shouldBe
                        listOf(0.4f, 0.9f)
                }
            }

            test("a cancelled recording is discarded") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    f.viewModel.onStartRecording()
                    advanceUntilIdle()

                    f.viewModel.onCancelRecording()
                    advanceUntilIdle()

                    f.recorder.cancels shouldBe 1
                    f.media.media.shouldBeEmpty()
                    f.viewModel.uiState.value.recording.shouldBeNull()
                }
            }

            test("a recording that cannot start posts the error and leaves no recording bar") {
                composerTest { f ->
                    f.recorder.startResult = Outcome.Failure(MediaError.RecordingFailed)
                    f.viewModel.bind(null)
                    f.viewModel.onStartRecording()
                    advanceUntilIdle()

                    f.viewModel.uiState.value.recording.shouldBeNull()
                    f.posted.single().text shouldBe MediaError.RecordingFailed.toUiText()
                }
            }

            test("a recording that cannot be finished posts the error and attaches nothing") {
                composerTest { f ->
                    f.recorder.stopResult = Outcome.Failure(MediaError.RecordingFailed)
                    f.viewModel.bind(null)
                    f.viewModel.onStartRecording()
                    advanceUntilIdle()
                    f.viewModel.onStopRecording()
                    advanceUntilIdle()
                    runCurrent()

                    f.media.media.shouldBeEmpty()
                    f.posted.single().text shouldBe MediaError.RecordingFailed.toUiText()
                }
            }

            test("a denied microphone is reported") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    val error =
                        MediaError.PermissionRequired(
                            PermissionError.DeniedPermanently(AppPermission.MICROPHONE),
                        )
                    f.viewModel.onRecordingPermissionDenied(error, openSettings = {})
                    runCurrent()

                    val message = f.posted.single()
                    message.text shouldBe error.toUiText()
                    message.action.shouldNotBeNull()
                }
            }

            test("closing the composer while recording keeps the recording") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    f.viewModel.onStartRecording()
                    advanceUntilIdle()

                    f.viewModel.finishSession().join()
                    runCurrent()

                    f.recorder.stops shouldBe 1
                    f.media.media.single().type shouldBe MediaType.AUDIO
                    f.entries.deleted.shouldBeEmpty()
                    f.posted.single().text.shouldBeSavedMessage()
                }
            }

            test("going to the background stops the recording and keeps it") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    f.viewModel.onStartRecording()
                    advanceUntilIdle()

                    f.viewModel.onBackground()
                    advanceUntilIdle()

                    f.recorder.isRecording shouldBe false
                    f.media.media.single().type shouldBe MediaType.AUDIO
                }
            }

            test("removing a voice takes it out of the list") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    f.viewModel.onStartRecording()
                    advanceUntilIdle()
                    f.viewModel.onStopRecording()
                    advanceUntilIdle()
                    val voice = f.viewModel.uiState.value.voices.single()

                    f.viewModel.onRemoveMedia(voice.id)
                    advanceUntilIdle()

                    f.viewModel.uiState.value.voices.shouldBeEmpty()
                }
            }

            test("a new entry is placed where the user is, and the place is saved with the text") {
                composerTest { f ->
                    f.settings.state.value = AppSettings(locationMode = LocationMode.ALWAYS)
                    f.viewModel.bind(null)
                    advanceUntilIdle()

                    f.viewModel.uiState.value.location shouldBe f.location.place
                    f.entries.saves.shouldBeEmpty()

                    f.viewModel.typeText("Cà phê sáng")
                    advanceUntilIdle()
                    f.entries.saves.single().location shouldBe f.location.place
                }
            }

            test("an existing entry is not placed again when it is opened") {
                composerTest { f ->
                    f.settings.state.value = AppSettings(locationMode = LocationMode.ALWAYS)
                    val id = Uuid.random()
                    f.entries.entries[id] = existingEntry(id, storedBody("Hi"), "Hi")
                    f.viewModel.bind(id)
                    advanceUntilIdle()

                    f.location.fixes shouldBe 0
                    f.viewModel.uiState.value.location.shouldBeNull()
                }
            }

            test("with suggestions only, the place is offered but not attached") {
                composerTest { f ->
                    f.settings.state.value = AppSettings(locationMode = LocationMode.SUGGEST)
                    f.viewModel.bind(null)
                    advanceUntilIdle()

                    val state = f.viewModel.uiState.value
                    state.location.shouldBeNull()
                    state.suggestedLocation shouldBe f.location.place

                    f.viewModel.onLocationChange(state.suggestedLocation)
                    f.viewModel.uiState.value.location shouldBe f.location.place
                    f.viewModel.uiState.value.suggestedLocation.shouldBeNull()
                }
            }

            test("an automatic location that fails is not reported") {
                composerTest { f ->
                    f.settings.state.value = AppSettings(locationMode = LocationMode.ALWAYS)
                    f.location.fixResult = Outcome.Failure(LocationError.ServiceDisabled)
                    f.viewModel.bind(null)
                    advanceUntilIdle()
                    runCurrent()

                    f.viewModel.uiState.value.location.shouldBeNull()
                    f.viewModel.uiState.value.isLocating shouldBe false
                    f.posted.shouldBeEmpty()
                }
            }

            test("asking for the current location reports a failure") {
                composerTest { f ->
                    f.location.fixResult = Outcome.Failure(LocationError.ServiceDisabled)
                    f.viewModel.bind(null)
                    advanceUntilIdle()

                    f.viewModel.onUseCurrentLocation()
                    advanceUntilIdle()
                    runCurrent()

                    f.posted.single().text shouldBe LocationError.ServiceDisabled.toUiText()
                }
            }

            test("without a geocoder the coordinates name the place") {
                composerTest { f ->
                    f.location.geocodeResult = Outcome.Failure(LocationError.Unavailable)
                    f.viewModel.bind(null)
                    advanceUntilIdle()

                    f.viewModel.onUseCurrentLocation()
                    advanceUntilIdle()

                    val location = f.viewModel.uiState.value.location.shouldNotBeNull()
                    location.name shouldBe "10.78756, 106.67891"
                    location.coordinates shouldBe f.location.point
                }
            }

            test("a free-typed place on an existing entry is saved, and removing it saves again") {
                composerTest { f ->
                    val id = Uuid.random()
                    f.entries.entries[id] = existingEntry(id, storedBody("Hi"), "Hi")
                    f.viewModel.bind(id)
                    advanceUntilIdle()

                    f.viewModel.onLocationChange(EntryLocation(name = "Nhà ngoại"))
                    advanceUntilIdle()
                    f.entries.saves.last().location shouldBe EntryLocation(name = "Nhà ngoại")

                    f.viewModel.onLocationChange(null)
                    advanceUntilIdle()
                    f.entries.saves.last().location.shouldBeNull()
                }
            }

            test("a place picked before the automatic one arrives is kept") {
                composerTest { f ->
                    f.settings.state.value = AppSettings(locationMode = LocationMode.ALWAYS)
                    f.viewModel.bind(null)
                    f.viewModel.onLocationChange(EntryLocation(name = "Nhà"))
                    advanceUntilIdle()

                    f.viewModel.uiState.value.location shouldBe EntryLocation(name = "Nhà")
                }
            }

            test("recent places are offered") {
                composerTest { f ->
                    f.entries.recentLocations.value = listOf(EntryLocation(name = "Hồ Gươm"))
                    f.viewModel.bind(null)
                    advanceUntilIdle()

                    f.viewModel.uiState.value.recentLocations shouldBe
                        listOf(EntryLocation(name = "Hồ Gươm"))
                }
            }

            test("undo and redo are unavailable until something changes") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    advanceUntilIdle()
                    f.viewModel.uiState.value.canUndo shouldBe false

                    f.viewModel.onEmojiChange("🌱")
                    f.viewModel.uiState.value.canUndo shouldBe true
                    f.viewModel.uiState.value.canRedo shouldBe false
                }
            }

            test("undo and redo an emoji") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    advanceUntilIdle()
                    f.viewModel.onEmojiChange("🌱")
                    f.viewModel.onEmojiChange("🔥")

                    f.viewModel.onUndo()
                    f.viewModel.uiState.value.emoji shouldBe "🌱"
                    f.viewModel.onUndo()
                    f.viewModel.uiState.value.emoji.shouldBeNull()
                    f.viewModel.uiState.value.canUndo shouldBe false

                    f.viewModel.onRedo()
                    f.viewModel.uiState.value.emoji shouldBe "🌱"
                }
            }

            test("typing without a pause is one step, and undo saves the earlier text") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    advanceUntilIdle()
                    f.viewModel.typeText("H")
                    f.viewModel.typeText("He")
                    f.viewModel.typeText("Hey")
                    advanceTimeBy(2.seconds)
                    f.viewModel.typeText("Hey there")
                    advanceTimeBy(2.seconds)

                    f.viewModel.onUndo()
                    f.viewModel.richTextState.annotatedString.text shouldBe "Hey"
                    advanceUntilIdle()
                    RichTextState()
                        .setJson(f.entries.saves.last().body)
                        .annotatedString
                        .text shouldBe "Hey"

                    f.viewModel.onUndo()
                    f.viewModel.richTextState.annotatedString.text shouldBe ""
                }
            }

            test("another change ends the typing step") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    advanceUntilIdle()
                    f.viewModel.typeText("Hi")
                    f.viewModel.onEmojiChange("🌱")
                    f.viewModel.typeText("Hi you")

                    f.viewModel.onUndo()
                    f.viewModel.richTextState.annotatedString.text shouldBe "Hi"
                    f.viewModel.uiState.value.emoji shouldBe "🌱"
                }
            }

            test("undo a label and a place") {
                composerTest { f ->
                    val group = f.labels.addGroup()
                    val label = f.labels.addLabel(group, name = "Trip")
                    f.viewModel.bind(null)
                    advanceUntilIdle()
                    f.viewModel.typeText("Hi")
                    advanceTimeBy(2.seconds)

                    f.viewModel.onLabelToggle(label.id)
                    f.viewModel.onLocationChange(EntryLocation(name = "Nhà"))
                    advanceUntilIdle()

                    f.viewModel.onUndo()
                    f.viewModel.uiState.value.location.shouldBeNull()
                    f.viewModel.onUndo()
                    advanceUntilIdle()
                    f.viewModel.uiState.value.selectedLabelIds shouldBe emptySet()
                    f.entries.labelSets[f.entries.saves.first().id] shouldBe emptySet()
                }
            }

            test("undo removes an added photo, and redo brings it back") {
                composerTest { f ->
                    f.viewModel.bind(null)
                    f.viewModel.typeText("Hi")
                    advanceTimeBy(2.seconds)
                    f.viewModel.onPhotosPicked(listOf(photo("first")))
                    advanceUntilIdle()
                    f.viewModel.uiState.value.photos.size shouldBe 1

                    f.viewModel.onUndo()
                    advanceUntilIdle()
                    f.viewModel.uiState.value.photos.shouldBeEmpty()

                    f.viewModel.onRedo()
                    advanceUntilIdle()
                    f.viewModel.uiState.value.photos.size shouldBe 1
                }
            }
        },
    )
