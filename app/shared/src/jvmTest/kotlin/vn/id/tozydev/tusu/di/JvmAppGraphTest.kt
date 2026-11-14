package vn.id.tozydev.tusu.di

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.first
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import okio.FileSystem
import vn.id.tozydev.tusu.data.db.label.DefaultLabelNames
import vn.id.tozydev.tusu.data.db.label.DefaultLabelNamesProvider
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.AppSettings
import vn.id.tozydev.tusu.domain.model.ThemeMode
import vn.id.tozydev.tusu.domain.usecase.AttachMediaUseCase
import vn.id.tozydev.tusu.platform.FakeImageCompressor

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

class JvmAppGraphTest :
    FunSpec(
        {
            test("the JVM graph wires the data layer end to end") {
                val tempDir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "tusu-di-${Uuid.random()}"
                FileSystem.SYSTEM.createDirectories(tempDir)
                val appDir = Path(tempDir.toString())
                // Compose Resources needs a native Skiko library that the test classpath lacks.
                val names =
                    object : DefaultLabelNamesProvider {
                        override suspend fun names(): DefaultLabelNames = testLabelNames
                    }
                val graph = createJvmAppGraph(appDir, labelNames = names)
                try {
                    graph.settingsRepository.settings.first() shouldBe AppSettings()

                    val updated = AppSettings(themeMode = ThemeMode.DARK)
                    graph.settingsRepository.update { updated } shouldBe Outcome.Success(Unit)
                    graph.settingsRepository.settings.first() shouldBe updated

                    graph.labelRepository.observeGroups().first() shouldHaveSize 3

                    SystemFileSystem.exists(Path(appDir, "tusu.db")) shouldBe true
                    SystemFileSystem.exists(Path(appDir, "settings.preferences_pb")) shouldBe true

                    graph.attachMedia.shouldBeInstanceOf<AttachMediaUseCase>()
                    graph.imageCompressor.shouldBeInstanceOf<FakeImageCompressor>()
                } finally {
                    graph.database.close()
                    FileSystem.SYSTEM.deleteRecursively(tempDir)
                }
            }
        },
    )
