package vn.id.tozydev.tusu.ui.feature.entry

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.document.RichTextBlock
import com.mohamedrejeb.richeditor.document.RichTextDocument
import com.mohamedrejeb.richeditor.json.toJson
import com.mohamedrejeb.richeditor.model.RichTextState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds
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
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.io.files.Path
import okio.FileSystem
import vn.id.tozydev.tusu.data.db.label.DefaultLabelNames
import vn.id.tozydev.tusu.data.db.label.DefaultLabelNamesProvider
import vn.id.tozydev.tusu.di.createJvmAppGraph
import vn.id.tozydev.tusu.domain.model.EntryDraft
import vn.id.tozydev.tusu.domain.model.Media
import vn.id.tozydev.tusu.domain.model.MediaStatus
import vn.id.tozydev.tusu.domain.model.MediaType

private val detailLabelNames =
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
private fun detailBody(text: String): String =
    RichTextState()
        .setRichTextDocument(RichTextDocument(listOf(RichTextBlock(text = text))))
        .toJson()

@OptIn(ExperimentalCoroutinesApi::class)
class EntryDetailViewModelTest :
    FunSpec(
        {
            test("bind fills the entry and lists its live photos in album order") {
                // viewModelScope runs on Dispatchers.Main, which a JVM test does not have.
                Dispatchers.setMain(UnconfinedTestDispatcher())
                val tempDir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "tusu-detail-${Uuid.random()}"
                FileSystem.SYSTEM.createDirectories(tempDir)
                val names =
                    object : DefaultLabelNamesProvider {
                        override suspend fun names(): DefaultLabelNames = detailLabelNames
                    }
                val graph = createJvmAppGraph(Path(tempDir.toString()), labelNames = names)
                val store = ViewModelStore()
                try {
                    val entryId = Uuid.random()
                    val occurredAt = Instant.parse("2026-11-10T03:00:00Z")
                    graph.entryRepository.save(
                        EntryDraft(
                            id = entryId,
                            body = detailBody("hello detail"),
                            emoji = "🌱",
                            occurredAt = occurredAt,
                            timeZone = TimeZone.of("Asia/Ho_Chi_Minh"),
                        ),
                    )
                    fun media(name: String, type: MediaType, position: Int) =
                        Media(
                            id = Uuid.random(),
                            entryId = entryId,
                            type = type,
                            localPath = "ab/cd/$name",
                            position = position,
                            status = MediaStatus.READY,
                            updatedAt = occurredAt,
                        )
                    // Added out of order: the carousel must follow the album position.
                    graph.mediaRepository.add(media("second.jpg", MediaType.PHOTO, position = 1))
                    graph.mediaRepository.add(media("first.jpg", MediaType.PHOTO, position = 0))
                    graph.mediaRepository.add(media("voice.m4a", MediaType.AUDIO, position = 2))

                    val viewModel =
                        ViewModelProvider.create(store, graph.metroViewModelFactory)
                            .get(EntryDetailViewModel::class)
                    viewModel.bind(entryId)
                    viewModel.bind(entryId)

                    val state = withTimeout(5.seconds) { viewModel.uiState.first { !it.isLoading } }
                    state.isGone shouldBe false
                    state.emoji shouldBe "🌱"
                    state.occurredAt shouldBe occurredAt
                    state.timeZone shouldBe TimeZone.of("Asia/Ho_Chi_Minh")
                    state.textPlain shouldBe "hello detail"
                    val mediaDir = Path(tempDir.toString(), "media")
                    state.photos.map { it.path } shouldContainExactly
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

            test("bind marks an unknown entry as gone") {
                Dispatchers.setMain(UnconfinedTestDispatcher())
                val tempDir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "tusu-detail-${Uuid.random()}"
                FileSystem.SYSTEM.createDirectories(tempDir)
                val names =
                    object : DefaultLabelNamesProvider {
                        override suspend fun names(): DefaultLabelNames = detailLabelNames
                    }
                val graph = createJvmAppGraph(Path(tempDir.toString()), labelNames = names)
                val store = ViewModelStore()
                try {
                    val viewModel =
                        ViewModelProvider.create(store, graph.metroViewModelFactory)
                            .get(EntryDetailViewModel::class)
                    viewModel.bind(Uuid.random())

                    val state = withTimeout(5.seconds) { viewModel.uiState.first { !it.isLoading } }
                    state.isGone shouldBe true
                } finally {
                    store.clear()
                    graph.database.close()
                    FileSystem.SYSTEM.deleteRecursively(tempDir)
                    Dispatchers.resetMain()
                }
            }

            test("moving the entry to the trash while bound marks it gone") {
                Dispatchers.setMain(UnconfinedTestDispatcher())
                val tempDir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "tusu-detail-${Uuid.random()}"
                FileSystem.SYSTEM.createDirectories(tempDir)
                val names =
                    object : DefaultLabelNamesProvider {
                        override suspend fun names(): DefaultLabelNames = detailLabelNames
                    }
                val graph = createJvmAppGraph(Path(tempDir.toString()), labelNames = names)
                val store = ViewModelStore()
                try {
                    val entryId = Uuid.random()
                    graph.entryRepository.save(
                        EntryDraft(
                            id = entryId,
                            body = detailBody("to trash"),
                            occurredAt = Instant.parse("2026-11-10T03:00:00Z"),
                            timeZone = TimeZone.of("Asia/Ho_Chi_Minh"),
                        ),
                    )
                    val viewModel =
                        ViewModelProvider.create(store, graph.metroViewModelFactory)
                            .get(EntryDetailViewModel::class)
                    viewModel.bind(entryId)
                    withTimeout(5.seconds) { viewModel.uiState.first { !it.isLoading } }

                    graph.entryRepository.moveToTrash(entryId)

                    val state = withTimeout(5.seconds) { viewModel.uiState.first { it.isGone } }
                    state.isGone shouldBe true
                } finally {
                    store.clear()
                    graph.database.close()
                    FileSystem.SYSTEM.deleteRecursively(tempDir)
                    Dispatchers.resetMain()
                }
            }

            test("changing the time saves the new instant in the current zone") {
                Dispatchers.setMain(UnconfinedTestDispatcher())
                val tempDir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "tusu-detail-${Uuid.random()}"
                FileSystem.SYSTEM.createDirectories(tempDir)
                val names =
                    object : DefaultLabelNamesProvider {
                        override suspend fun names(): DefaultLabelNames = detailLabelNames
                    }
                val graph = createJvmAppGraph(Path(tempDir.toString()), labelNames = names)
                val store = ViewModelStore()
                try {
                    val entryId = Uuid.random()
                    graph.entryRepository.save(
                        EntryDraft(
                            id = entryId,
                            body = detailBody("move me"),
                            occurredAt = Instant.parse("2026-11-10T03:00:00Z"),
                            timeZone = TimeZone.of("Asia/Ho_Chi_Minh"),
                        ),
                    )
                    val viewModel =
                        ViewModelProvider.create(store, graph.metroViewModelFactory)
                            .get(EntryDetailViewModel::class)
                    viewModel.bind(entryId)
                    withTimeout(5.seconds) { viewModel.uiState.first { !it.isLoading } }

                    val zone = TimeZone.currentSystemDefault()
                    val expected = LocalDateTime(2026, 11, 12, 8, 30).toInstant(zone)
                    viewModel.onOccurredAtChange(LocalDateTime(2026, 11, 12, 8, 30))

                    val state =
                        withTimeout(5.seconds) {
                            viewModel.uiState.first { it.occurredAt == expected }
                        }
                    state.timeZone shouldBe zone
                    state.currentTimeZone shouldBe zone
                    val saved = graph.entryRepository.observeEntry(entryId).first()
                    saved?.occurredAt shouldBe expected
                    saved?.timeZone shouldBe zone
                } finally {
                    store.clear()
                    graph.database.close()
                    FileSystem.SYSTEM.deleteRecursively(tempDir)
                    Dispatchers.resetMain()
                }
            }

            test("delete moves the entry to the trash and Undo restores it") {
                Dispatchers.setMain(UnconfinedTestDispatcher())
                val tempDir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "tusu-detail-${Uuid.random()}"
                FileSystem.SYSTEM.createDirectories(tempDir)
                val names =
                    object : DefaultLabelNamesProvider {
                        override suspend fun names(): DefaultLabelNames = detailLabelNames
                    }
                val graph = createJvmAppGraph(Path(tempDir.toString()), labelNames = names)
                val store = ViewModelStore()
                try {
                    val entryId = Uuid.random()
                    graph.entryRepository.save(
                        EntryDraft(
                            id = entryId,
                            body = detailBody("delete me"),
                            occurredAt = Instant.parse("2026-11-10T03:00:00Z"),
                            timeZone = TimeZone.of("Asia/Ho_Chi_Minh"),
                        ),
                    )
                    val viewModel =
                        ViewModelProvider.create(store, graph.metroViewModelFactory)
                            .get(EntryDetailViewModel::class)
                    viewModel.bind(entryId)
                    withTimeout(5.seconds) { viewModel.uiState.first { !it.isLoading } }

                    viewModel.onDelete()

                    withTimeout(5.seconds) { viewModel.uiState.first { it.isGone } }
                    val message = withTimeout(5.seconds) { graph.appMessages.messages.first() }
                    message.timeout shouldBe 5.seconds
                    val undo = requireNotNull(message.action)
                    withTimeoutOrNull(100.milliseconds) {
                        graph.appMessages.messages.first()
                    } shouldBe null

                    undo.onAction()

                    val restored = withTimeout(5.seconds) { viewModel.uiState.first { !it.isGone } }
                    restored.isGone shouldBe false
                    graph.entryRepository.observeEntry(entryId).first()?.isDeleted shouldBe false
                } finally {
                    store.clear()
                    graph.database.close()
                    FileSystem.SYSTEM.deleteRecursively(tempDir)
                    Dispatchers.resetMain()
                }
            }
        },
    )
