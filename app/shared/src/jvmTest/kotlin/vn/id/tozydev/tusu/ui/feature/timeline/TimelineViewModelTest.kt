package vn.id.tozydev.tusu.ui.feature.timeline

import androidx.lifecycle.ViewModelStore
import androidx.paging.testing.asSnapshot
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.document.RichTextBlock
import com.mohamedrejeb.richeditor.document.RichTextDocument
import com.mohamedrejeb.richeditor.json.toJson
import com.mohamedrejeb.richeditor.model.RichTextState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.io.files.Path
import okio.FileSystem
import vn.id.tozydev.tusu.data.db.label.DefaultLabelNames
import vn.id.tozydev.tusu.data.db.label.DefaultLabelNamesProvider
import vn.id.tozydev.tusu.di.JvmAppGraph
import vn.id.tozydev.tusu.di.createJvmAppGraph
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.EntryDraft
import vn.id.tozydev.tusu.domain.model.Media
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType

private val testLabelNames =
    DefaultLabelNames(
        kindGroup = "Kind",
        peopleGroup = "People",
        tripsGroup = "Trips",
        achievement = "Achievement",
        lesson = "Lesson",
        reflection = "Reflection",
        memory = "Memory",
        gratitude = "Gratitude",
    )

@OptIn(ExperimentalRichTextApi::class)
private fun body(text: String): String =
    RichTextState()
        .setRichTextDocument(RichTextDocument(listOf(RichTextBlock(text = text))))
        .toJson()

/**
 * Saves each (text, instant) pair in Asia/Ho_Chi_Minh, lets [seedMedia] add media to those entries
 * (ids in the same order), then runs [block] on the ViewModel.
 */
@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun withTimelineViewModel(
    entries: List<Pair<String, String>>,
    seedMedia: suspend (JvmAppGraph, List<Uuid>) -> Unit = { _, _ -> },
    block: suspend (TimelineViewModel, JvmAppGraph, List<Uuid>) -> Unit,
) {
    // viewModelScope runs on Dispatchers.Main, which a JVM test does not have.
    Dispatchers.setMain(UnconfinedTestDispatcher())
    val tempDir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "tusu-timeline-${Uuid.random()}"
    FileSystem.SYSTEM.createDirectories(tempDir)
    val names =
        object : DefaultLabelNamesProvider {
            override suspend fun names(): DefaultLabelNames = testLabelNames
        }
    val graph = createJvmAppGraph(Path(tempDir.toString()), labelNames = names)
    val store = ViewModelStore()
    try {
        val zone = TimeZone.of("Asia/Ho_Chi_Minh")
        val ids = entries.map { Uuid.random() }
        for ((index, entry) in entries.withIndex()) {
            val (text, at) = entry
            graph.entryRepository.save(
                EntryDraft(
                    id = ids[index],
                    body = body(text),
                    occurredAt = Instant.parse(at),
                    timeZone = zone,
                ),
            )
        }
        seedMedia(graph, ids)
        val viewModel =
            androidx.lifecycle.ViewModelProvider.create(store, graph.metroViewModelFactory)
                .get(TimelineViewModel::class)
        viewModel.shouldBeInstanceOf<TimelineViewModel>()
        block(viewModel, graph, ids)
    } finally {
        store.clear()
        graph.database.close()
        FileSystem.SYSTEM.deleteRecursively(tempDir)
        Dispatchers.resetMain()
    }
}

/** A READY media item of [entryId] whose file name is [fileName], placed at [position]. */
private fun media(
    entryId: Uuid,
    position: Int,
    fileName: String,
    type: MediaType = MediaType.PHOTO,
) =
    Media(
        id = Uuid.random(),
        entryId = entryId,
        type = type,
        localPath = "ab/cd/$fileName",
        width = 1200,
        height = 900,
        position = position,
        status = MediaStatus.READY,
        updatedAt = Instant.parse("2026-11-11T03:00:00Z"),
    )

class TimelineViewModelTest :
    FunSpec(
        {
            test("the timeline puts a day header before the entries of each day, newest first") {
                withTimelineViewModel(
                    listOf(
                        "older" to "2026-11-10T03:00:00Z",
                        "newer" to "2026-11-11T03:00:00Z",
                    ),
                ) { viewModel, _, _ ->
                    val d1 = LocalDate(2026, 11, 10)
                    val d2 = LocalDate(2026, 11, 11)
                    viewModel.items.asSnapshot().map { it.describe() } shouldContainExactly
                        listOf("day:$d2", "newer", "day:$d1", "older")
                }
            }

            test("entries of the same day share one day header") {
                withTimelineViewModel(
                    listOf(
                        "morning" to "2026-11-11T01:00:00Z",
                        "evening" to "2026-11-11T12:00:00Z",
                    ),
                ) { viewModel, _, _ ->
                    val day = LocalDate(2026, 11, 11)
                    viewModel.items.asSnapshot().map { it.describe() } shouldContainExactly
                        listOf("day:$day", "evening", "morning")
                }
            }

            test("an entry item carries its photos in position order, without audio") {
                withTimelineViewModel(
                    listOf("with media" to "2026-11-11T03:00:00Z"),
                    seedMedia = { graph, ids ->
                        val entryId = ids.single()
                        listOf(
                                media(entryId, position = 1, fileName = "second.jpg"),
                                media(
                                    entryId,
                                    position = 2,
                                    fileName = "voice.m4a",
                                    type = MediaType.AUDIO,
                                ),
                                media(entryId, position = 0, fileName = "first.jpg"),
                            )
                            .forEach {
                                graph.mediaRepository
                                    .add(it)
                                    .shouldBeInstanceOf<Outcome.Success<Media>>()
                            }
                    },
                ) { viewModel, _, _ ->
                    val item =
                        viewModel.items
                            .asSnapshot()
                            .filterIsInstance<TimelineItem.EntryItem>()
                            .single()
                    item.photos.map { Path(it.path).name } shouldBe
                        listOf("first.jpg", "second.jpg")
                    item.photos.forEach { Path(it.path).isAbsolute shouldBe true }
                }
            }

            test("changing the emoji from the feed saves it and keeps the rest of the entry") {
                withTimelineViewModel(listOf("quick emoji" to "2026-11-11T03:00:00Z")) {
                    viewModel,
                    graph,
                    ids ->
                    val entryId = ids.single()
                    val before = graph.entryRepository.observeEntry(entryId).first()

                    viewModel.onEmojiChange(entryId, "🎉")
                    val changed =
                        withTimeout(5.seconds) {
                            graph.entryRepository.observeEntry(entryId).first { it?.emoji == "🎉" }
                        }
                    changed?.textPlain shouldBe "quick emoji"
                    changed?.occurredAt shouldBe before?.occurredAt

                    viewModel.onEmojiChange(entryId, null)
                    withTimeout(5.seconds) {
                        graph.entryRepository.observeEntry(entryId).first { it?.emoji == null }
                    }
                }
            }
        },
    )

/** Compact label of a timeline item, so snapshots read as a list of strings. */
private fun TimelineItem.describe(): String =
    when (this) {
        is TimelineItem.Day -> "day:$date"
        is TimelineItem.EntryItem -> entry.textPlain
    }
