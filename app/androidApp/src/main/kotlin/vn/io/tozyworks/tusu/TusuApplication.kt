package vn.io.tozyworks.tusu

import android.app.Application
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.android.MetroAppComponentProviders
import dev.zacsweers.metrox.android.MetroApplication
import vn.io.tozyworks.tusu.di.AndroidAppGraph

class TusuApplication : Application(), MetroApplication {
    override val appComponentProviders: MetroAppComponentProviders by lazy {
        createGraphFactory<AndroidAppGraph.Factory>().create(this)
    }
}
