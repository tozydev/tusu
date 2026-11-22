package vn.id.tozydev.tusu.ui.feature.calendar

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.document.RichTextBlock
import com.mohamedrejeb.richeditor.document.RichTextDocument
import com.mohamedrejeb.richeditor.json.toJson
import com.mohamedrejeb.richeditor.model.RichTextState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
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
import kotlinx.datetime.YearMonth
import kotlinx.io.files.Path
import okio.FileSystem
import vn.id.tozydev.tusu.data.db.label.DefaultLabelNames
import vn.id.tozydev.tusu.data.db.label.DefaultLabelNamesProvider
import vn.id.tozydev.tusu.di.createJvmAppGraph
import vn.id.tozydev.tusu.domain.model.EntryDraft

private val calendarLabelNames =
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
private fun calendarBody(text: String): String =
    RichTextState()
        .setRichTextDocument(RichTextDocument(listOf(RichTextBlock(text = text))))
        .toJson()

class CalendarViewModelTest :
    FunSpec(
        {
            @OptIn(ExperimentalCoroutinesApi::class)
            test("selecting a day shows its month and its entries in time order") {
                // viewModelScope runs on Dispatchers.Main, which a JVM test does not have.
                Dispatchers.setMain(UnconfinedTestDispatcher())
                val tempDir =
                    FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "tusu-calendar-${Uuid.random()}"
                FileSystem.SYSTEM.createDirectories(tempDir)
                val names =
                    object : DefaultLabelNamesProvider {
                        override suspend fun names(): DefaultLabelNames = calendarLabelNames
                    }
                val graph = createJvmAppGraph(Path(tempDir.toString()), labelNames = names)
                val store = ViewModelStore()
                try {
                    val zone = TimeZone.of("Asia/Ho_Chi_Minh")
                    suspend fun save(text: String, at: String, emoji: String?) =
                        graph.entryRepository.save(
                            EntryDraft(
                                id = Uuid.random(),
                                body = calendarBody(text),
                                emoji = emoji,
                                occurredAt = Instant.parse(at),
                                timeZone = zone,
                            ),
                        )
                    save("evening", "2026-11-05T12:00:00Z", emoji = "🌙")
                    save("morning", "2026-11-05T01:00:00Z", emoji = "☀️")
                    save("other day", "2026-11-11T03:00:00Z", emoji = null)

                    val viewModel =
                        ViewModelProvider.create(store, graph.metroViewModelFactory)
                            .get(CalendarViewModel::class)
                    val day = LocalDate(2026, 11, 5)
                    viewModel.onDaySelect(day)

                    val state =
                        withTimeout(5.seconds) {
                            viewModel.uiState.first {
                                it.selectedDate == day && it.dayEntries.size == 2
                            }
                        }
                    state.month shouldBe YearMonth(2026, 11)
                    state.dayEntries.map { it.textPlain } shouldContainExactly
                        listOf("morning", "evening")
                    state.days.getValue(day).emoji shouldBe "🌙"
                    state.days.getValue(day).entryCount shouldBe 2
                    state.days.getValue(LocalDate(2026, 11, 11)).entryCount shouldBe 1

                    viewModel.onMonthChange(1)
                    val next =
                        withTimeout(5.seconds) {
                            viewModel.uiState.first { it.month == YearMonth(2026, 12) }
                        }
                    next.days.shouldBeEmpty()
                } finally {
                    store.clear()
                    graph.database.close()
                    FileSystem.SYSTEM.deleteRecursively(tempDir)
                    Dispatchers.resetMain()
                }
            }
        },
    )
