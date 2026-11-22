package vn.id.tozydev.tusu.ui.feature.labels

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import kotlinx.io.files.Path
import okio.FileSystem
import vn.id.tozydev.tusu.data.db.label.DefaultLabelNames
import vn.id.tozydev.tusu.data.db.label.DefaultLabelNamesProvider
import vn.id.tozydev.tusu.di.createJvmAppGraph
import vn.id.tozydev.tusu.domain.model.LabelSelection

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
        },
    )
