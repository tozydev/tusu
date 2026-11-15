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
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.io.files.Path
import okio.FileSystem
import vn.id.tozydev.tusu.data.db.label.DefaultLabelNames
import vn.id.tozydev.tusu.data.db.label.DefaultLabelNamesProvider
import vn.id.tozydev.tusu.di.createJvmAppGraph
import vn.id.tozydev.tusu.domain.model.EntryDraft

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

@OptIn(ExperimentalCoroutinesApi::class)
/** Saves each (text, instant) pair in Asia/Ho_Chi_Minh, then runs [block] on the ViewModel. */
private suspend fun withTimelineViewModel(
    entries: List<Pair<String, String>>,
    block: suspend (TimelineViewModel) -> Unit,
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
        for ((text, at) in entries) {
            graph.entryRepository.save(
                EntryDraft(
                    id = Uuid.random(),
                    body = body(text),
                    occurredAt = Instant.parse(at),
                    timeZone = zone,
                ),
            )
        }
        val viewModel =
            androidx.lifecycle.ViewModelProvider.create(store, graph.metroViewModelFactory)
                .get(TimelineViewModel::class)
        viewModel.shouldBeInstanceOf<TimelineViewModel>()
        block(viewModel)
    } finally {
        store.clear()
        graph.database.close()
        FileSystem.SYSTEM.deleteRecursively(tempDir)
        Dispatchers.resetMain()
    }
}

class TimelineViewModelTest :
    FunSpec(
        {
            test("the timeline puts a day header before the entries of each day, newest first") {
                withTimelineViewModel(
                    listOf(
                        "older" to "2026-11-10T03:00:00Z",
                        "newer" to "2026-11-11T03:00:00Z",
                    ),
                ) { viewModel ->
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
                ) { viewModel ->
                    val day = LocalDate(2026, 11, 11)
                    viewModel.items.asSnapshot().map { it.describe() } shouldContainExactly
                        listOf("day:$day", "evening", "morning")
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
