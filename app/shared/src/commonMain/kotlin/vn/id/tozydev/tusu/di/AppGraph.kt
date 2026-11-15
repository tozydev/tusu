package vn.id.tozydev.tusu.di

import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import kotlinx.coroutines.CoroutineScope
import kotlinx.io.files.Path
import vn.id.tozydev.tusu.data.media.MediaJobRunner
import vn.id.tozydev.tusu.data.sample.SampleDataSeeder
import vn.id.tozydev.tusu.domain.repository.EntryRepository
import vn.id.tozydev.tusu.domain.repository.LabelRepository
import vn.id.tozydev.tusu.domain.repository.MediaRepository
import vn.id.tozydev.tusu.domain.repository.SettingsRepository
import vn.id.tozydev.tusu.domain.usecase.AttachMediaUseCase
import vn.id.tozydev.tusu.platform.ImageCompressor
import vn.id.tozydev.tusu.platform.MediaJobQueue

/**
 * The accessors keep every platform graph validating the whole data layer, because Metro only
 * checks bindings that are reachable from a graph.
 */
interface AppGraph : ViewModelGraph {
    @AppDir val appDir: Path

    @CacheDir val cacheDir: Path

    val entryRepository: EntryRepository
    val mediaRepository: MediaRepository
    val labelRepository: LabelRepository
    val settingsRepository: SettingsRepository
    val attachMedia: AttachMediaUseCase
    val imageCompressor: ImageCompressor
    val mediaJobQueue: MediaJobQueue
    val mediaJobRunner: MediaJobRunner

    /** Debug builds fill an empty journal with it; release builds never call it. */
    val sampleDataSeeder: SampleDataSeeder

    /** The app-wide scope; the platform entry point starts background work on it. */
    val appScope: CoroutineScope
}
