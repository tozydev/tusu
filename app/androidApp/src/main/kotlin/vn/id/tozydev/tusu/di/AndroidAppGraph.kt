package vn.id.tozydev.tusu.di

import android.app.Application
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides

@DependencyGraph(AppScope::class)
interface AndroidAppGraph : BaseAndroidAppGraph {
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides application: Application): AndroidAppGraph
    }
}
