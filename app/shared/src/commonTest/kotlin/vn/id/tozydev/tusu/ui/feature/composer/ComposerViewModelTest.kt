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
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import vn.id.tozydev.tusu.domain.error.EntryError
import vn.id.tozydev.tusu.domain.model.Entry
import vn.id.tozydev.tusu.domain.model.toDraft
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.composer_empty_trashed
import vn.id.tozydev.tusu.generated.resources.composer_saved_message
import vn.id.tozydev.tusu.ui.component.AppMessages
import vn.id.tozydev.tusu.ui.component.UiMessage
import vn.id.tozydev.tusu.ui.format.UiText
import vn.id.tozydev.tusu.ui.format.toUiText

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

private class ComposerFixture(scope: TestScope, appScope: CoroutineScope) {
    val entries = FakeEntryRepository()
    val media = FakeMediaRepository()
    val messages = AppMessages()
    val posted = mutableListOf<UiMessage>()
    val viewModel =
        ComposerViewModel(
            entryRepository = entries,
            mediaRepository = media,
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
        },
    )
