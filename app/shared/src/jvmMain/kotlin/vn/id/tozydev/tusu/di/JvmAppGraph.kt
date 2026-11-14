package vn.id.tozydev.tusu.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.toKotlinxIoPath
import kotlinx.io.files.Path
import vn.id.tozydev.tusu.data.db.TusuDatabase
import vn.id.tozydev.tusu.data.db.label.ComposeDefaultLabelNamesProvider
import vn.id.tozydev.tusu.data.db.label.DefaultLabelNamesProvider

@DependencyGraph(AppScope::class)
interface JvmAppGraph : AppGraph {
    /** Exposed so tests can close the database before deleting their temporary folder. */
    val database: TusuDatabase

    @DependencyGraph.Factory
    fun interface Factory {
        // Passed in rather than bound, so a test can swap the label names without a second graph.
        fun create(
            @Provides @AppDir appDir: Path,
            @Provides labelNames: DefaultLabelNamesProvider,
        ): JvmAppGraph
    }
}

/**
 * Builds the desktop graph over [appDir]; tests pass a temporary folder and fixed label names
 * instead of the defaults.
 */
fun createJvmAppGraph(
    appDir: Path = defaultJvmAppDir(),
    labelNames: DefaultLabelNamesProvider = ComposeDefaultLabelNamesProvider(),
): JvmAppGraph = createGraphFactory<JvmAppGraph.Factory>().create(appDir, labelNames)

/**
 * FileKit's per-user folder: `%APPDATA%\Tusu` on Windows, and the equivalent on macOS and Linux.
 * FileKit has to know the app id before it can resolve that folder.
 */
fun defaultJvmAppDir(): Path {
    FileKit.init(appId = "Tusu")
    return FileKit.filesDir.toKotlinxIoPath()
}
