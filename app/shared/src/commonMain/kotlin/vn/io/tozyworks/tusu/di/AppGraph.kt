package vn.io.tozyworks.tusu.di

import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import io.github.vinceglb.filekit.PlatformFile
import vn.io.tozyworks.tusu.data.db.TusuDatabase

interface AppGraph : ViewModelGraph {
    val tusuDatabase: TusuDatabase

    @AppDir val appDir: PlatformFile
}
