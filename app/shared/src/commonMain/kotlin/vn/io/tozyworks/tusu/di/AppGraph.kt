package vn.io.tozyworks.tusu.di

import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import vn.io.tozyworks.tusu.data.db.TusuDatabase

interface AppGraph : ViewModelGraph {
    val tusuDatabase: TusuDatabase
}
