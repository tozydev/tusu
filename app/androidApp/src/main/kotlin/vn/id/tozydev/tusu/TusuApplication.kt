package vn.id.tozydev.tusu

import android.app.Application
import androidx.work.Configuration
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.android.MetroAppComponentProviders
import dev.zacsweers.metrox.android.MetroApplication
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import vn.id.tozydev.tusu.di.AndroidAppGraph
import vn.id.tozydev.tusu.domain.error.onFailure
import vn.id.tozydev.tusu.platform.TusuWorkerFactory

private val logger = KotlinLogging.logger {}

class TusuApplication : Application(), MetroApplication, Configuration.Provider {
    init {
        System.setProperty("kotlin-logging-to-android-native", "true")
    }

    private val appGraph: AndroidAppGraph by lazy {
        createGraphFactory<AndroidAppGraph.Factory>().create(this)
    }

    override val appComponentProviders: MetroAppComponentProviders
        get() = appGraph

    // WorkManager reads this on first use. The workers need the graph, so the factory comes from
    // here instead of WorkManager's default initializer, which is removed in the manifest.
    override val workManagerConfiguration: Configuration by lazy {
        Configuration.Builder().setWorkerFactory(TusuWorkerFactory(appGraph.mediaJobRunner)).build()
    }

    override fun onCreate() {
        super.onCreate()
        // Photos left PROCESSING by a killed process are queued again. The app scope keeps this
        // off the main thread.
        appGraph.appScope.launch {
            appGraph.mediaJobRunner.resumePending(appGraph.mediaJobQueue).onFailure {
                logger.warn { "Could not queue unfinished photo compression: $it" }
            }
        }
        DebugTools.onAppCreated(this, appGraph)
    }
}
