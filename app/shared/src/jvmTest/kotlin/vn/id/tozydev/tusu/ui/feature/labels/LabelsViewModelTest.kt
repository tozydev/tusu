package vn.id.tozydev.tusu.ui.feature.labels

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.mohamedrejeb.richeditor.json.toJson
import com.mohamedrejeb.richeditor.model.RichTextState
import io.kotest.core.spec.style.FunSpec
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
import kotlinx.datetime.TimeZone
import kotlinx.io.files.Path
import okio.FileSystem
import vn.id.tozydev.tusu.data.db.label.DefaultLabelNames
import vn.id.tozydev.tusu.data.db.label.DefaultLabelNamesProvider
import vn.id.tozydev.tusu.di.createJvmAppGraph
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.EntryDraft
import vn.id.tozydev.tusu.domain.model.LabelSelection
import vn.id.tozydev.tusu.generated.resources.Res
import vn.id.tozydev.tusu.generated.resources.label_group_single_notice
import vn.id.tozydev.tusu.ui.format.UiText

private val labelsTestNames =
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

class LabelsViewModelTest :
    FunSpec(
        {
            @OptIn(ExperimentalCoroutinesApi::class)
            test("groups and labels can be created, edited, moved to Khác and deleted") {
                Dispatchers.setMain(UnconfinedTestDispatcher())
                val tempDir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "tusu-labels-${Uuid.random()}"
                FileSystem.SYSTEM.createDirectories(tempDir)
                val names =
                    object : DefaultLabelNamesProvider {
                        override suspend fun names(): DefaultLabelNames = labelsTestNames
                    }
                val graph = createJvmAppGraph(Path(tempDir.toString()), labelNames = names)
                val store = ViewModelStore()
                try {
                    val viewModel =
                        ViewModelProvider.create(store, graph.metroViewModelFactory)
                            .get(LabelsViewModel::class)
                    suspend fun until(check: (LabelsUiState) -> Boolean) =
                        withTimeout(5.seconds) { viewModel.uiState.first(check) }

                    viewModel.onCreateGroup("Sport", 0xFF1E88E5.toInt(), LabelSelection.SINGLE)
                    val group = until { s ->
                        s.groups.any { it.name == "Sport" }
                    }
                        .groups
                        .single { it.name == "Sport" }
                    group.selection shouldBe LabelSelection.SINGLE

                    viewModel.onUpdateGroup(group.copy(name = "Sports", showInComposer = false))
                    until { s -> s.groups.any { it.name == "Sports" && !it.showInComposer } }

                    viewModel.onCreateLabel("Run", group.id)
                    val run = until { s ->
                        s.labels.any { it.name == "Run" }
                    }
                        .labels
                        .single { it.name == "Run" }

                    viewModel.onUpdateLabel(
                        run.copy(name = "Running", isHidden = true, groupId = null),
                    )
                    val moved = until { s ->
                        s.labels.any { it.name == "Running" }
                    }
                        .labels
                        .single { it.name == "Running" }
                    moved.groupId shouldBe null
                    moved.isHidden shouldBe true

                    viewModel.onDeleteGroup(group.id)
                    until { s -> s.groups.none { it.id == group.id } }

                    viewModel.onDeleteLabel(moved.id)
                    until { s -> s.labels.none { it.id == moved.id } }
                } finally {
                    store.clear()
                    graph.database.close()
                    FileSystem.SYSTEM.deleteRecursively(tempDir)
                    Dispatchers.resetMain()
                }
            }

            @OptIn(ExperimentalCoroutinesApi::class)
            test("making a group single-choice tells how many entries keep several labels") {
                Dispatchers.setMain(UnconfinedTestDispatcher())
                val tempDir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "tusu-labels-${Uuid.random()}"
                FileSystem.SYSTEM.createDirectories(tempDir)
                val names =
                    object : DefaultLabelNamesProvider {
                        override suspend fun names(): DefaultLabelNames = labelsTestNames
                    }
                val graph = createJvmAppGraph(Path(tempDir.toString()), labelNames = names)
                val store = ViewModelStore()
                try {
                    val repo = graph.labelRepository
                    val group =
                        (repo.createGroup("Mood", 0, LabelSelection.MULTI) as Outcome.Success).value
                    val a = (repo.createLabel("Calm", group.id) as Outcome.Success).value
                    val b = (repo.createLabel("Glad", group.id) as Outcome.Success).value
                    val entryId = Uuid.random()
                    graph.entryRepository.save(
                        EntryDraft(
                            id = entryId,
                            body = RichTextState().toJson(),
                            occurredAt = Instant.parse("2026-11-11T03:00:00Z"),
                            timeZone = TimeZone.of("Asia/Ho_Chi_Minh"),
                        ),
                    )
                    graph.entryRepository.setLabels(entryId, setOf(a.id, b.id))
                    val viewModel =
                        ViewModelProvider.create(store, graph.metroViewModelFactory)
                            .get(LabelsViewModel::class)
                    val shown =
                        withTimeout(5.seconds) {
                            viewModel.uiState.first { s -> s.groups.any { it.id == group.id } }
                        }

                    viewModel.onUpdateGroup(
                        shown.groups
                            .single { it.id == group.id }
                            .copy(selection = LabelSelection.SINGLE),
                    )

                    val message = withTimeout(5.seconds) { graph.appMessages.messages.first() }
                    message.text shouldBe UiText.plural(Res.plurals.label_group_single_notice, 1, 1)
                    graph.entryRepository.observeEntry(entryId).first()?.labels?.size shouldBe 2
                } finally {
                    store.clear()
                    graph.database.close()
                    FileSystem.SYSTEM.deleteRecursively(tempDir)
                    Dispatchers.resetMain()
                }
            }
        },
    )
