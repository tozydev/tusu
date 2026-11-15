package vn.id.tozydev.tusu.ui.feature.viewer

import androidx.lifecycle.ViewModelStore
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.document.RichTextBlock
import com.mohamedrejeb.richeditor.document.RichTextDocument
import com.mohamedrejeb.richeditor.json.toJson
import com.mohamedrejeb.richeditor.model.RichTextState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
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
import vn.id.tozydev.tusu.domain.model.EntryDraft
import vn.id.tozydev.tusu.domain.model.Media
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType

private val viewerLabelNames =
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
private fun viewerBody(text: String): String =
    RichTextState()
        .setRichTextDocument(RichTextDocument(listOf(RichTextBlock(text = text))))
        .toJson()

@OptIn(ExperimentalCoroutinesApi::class)
class ViewerViewModelTest :
    FunSpec(
        {
            test("bind lists only the live photos of the entry in album order") {
                // viewModelScope runs on Dispatchers.Main, which a JVM test does not have.
                Dispatchers.setMain(UnconfinedTestDispatcher())
                val tempDir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "tusu-viewer-${Uuid.random()}"
                FileSystem.SYSTEM.createDirectories(tempDir)
                val names =
                    object : DefaultLabelNamesProvider {
                        override suspend fun names(): DefaultLabelNames = viewerLabelNames
                    }
                val graph = createJvmAppGraph(Path(tempDir.toString()), labelNames = names)
                val store = ViewModelStore()
                try {
                    val entryId = Uuid.random()
                    graph.entryRepository.save(
                        EntryDraft(
                            id = entryId,
                            body = viewerBody("with photos"),
                            occurredAt = Instant.parse("2026-11-10T03:00:00Z"),
                            timeZone = TimeZone.of("Asia/Ho_Chi_Minh"),
                        ),
                    )
                    val updatedAt = Instant.parse("2026-11-10T03:00:00Z")
                    fun media(name: String, type: MediaType, position: Int) =
                        Media(
                            id = Uuid.random(),
                            entryId = entryId,
                            type = type,
                            localPath = "ab/cd/$name",
                            position = position,
                            status = MediaStatus.READY,
                            updatedAt = updatedAt,
                        )
                    val second = media("second.jpg", MediaType.PHOTO, position = 1)
                    val first = media("first.jpg", MediaType.PHOTO, position = 0)
                    val audio = media("voice.m4a", MediaType.AUDIO, position = 2)
                    val trashed = media("trashed.jpg", MediaType.PHOTO, position = 3)
                    for (item in listOf(second, first, audio, trashed)) {
                        graph.mediaRepository.add(item)
                    }
                    graph.mediaRepository.remove(trashed.id)

                    val viewModel =
                        androidx.lifecycle.ViewModelProvider.create(
                                store,
                                graph.metroViewModelFactory,
                            )
                            .get(ViewerViewModel::class)
                    viewModel.bind(entryId)
                    viewModel.bind(entryId)

                    val photos =
                        withTimeout(5.seconds) { viewModel.uiState.first { !it.isLoading } }.photos
                    val mediaDir = Path(tempDir.toString(), "media")
                    photos.map { it.path } shouldContainExactly
                        listOf(
                            Path(mediaDir, "ab/cd/first.jpg").toString(),
                            Path(mediaDir, "ab/cd/second.jpg").toString(),
                        )
                } finally {
                    store.clear()
                    graph.database.close()
                    FileSystem.SYSTEM.deleteRecursively(tempDir)
                    Dispatchers.resetMain()
                }
            }
        },
    )
