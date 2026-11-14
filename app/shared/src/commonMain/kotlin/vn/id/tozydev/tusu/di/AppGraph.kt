package vn.id.tozydev.tusu.di

import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import kotlinx.io.files.Path
import vn.id.tozydev.tusu.domain.repository.EntryRepository
import vn.id.tozydev.tusu.domain.repository.LabelRepository
import vn.id.tozydev.tusu.domain.repository.MediaRepository
import vn.id.tozydev.tusu.domain.repository.SettingsRepository
import vn.id.tozydev.tusu.domain.usecase.AttachMediaUseCase
import vn.id.tozydev.tusu.platform.ImageCompressor

/**
 * The accessors keep every platform graph validating the whole data layer, because Metro only
 * checks bindings that are reachable from a graph.
 */
interface AppGraph : ViewModelGraph {
    @AppDir val appDir: Path

    val entryRepository: EntryRepository
    val mediaRepository: MediaRepository
    val labelRepository: LabelRepository
    val settingsRepository: SettingsRepository
    val attachMedia: AttachMediaUseCase
    val imageCompressor: ImageCompressor
}
