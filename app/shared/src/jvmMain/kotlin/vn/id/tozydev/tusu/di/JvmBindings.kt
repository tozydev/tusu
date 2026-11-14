package vn.id.tozydev.tusu.di

import androidx.room3.RoomDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.io.files.Path
import vn.id.tozydev.tusu.data.db.TusuDatabase
import vn.id.tozydev.tusu.data.db.tusuDatabaseBuilder

@BindingContainer
@ContributesTo(AppScope::class)
object JvmBindings {
    @Provides
    @SingleIn(AppScope::class)
    fun provideDatabaseBuilder(@AppDir appDir: Path): RoomDatabase.Builder<TusuDatabase> =
        tusuDatabaseBuilder(Path(appDir, DATABASE_FILE_NAME))

    // Kept under the app folder, so a test graph over a temporary folder stays inside it.
    @Provides
    @SingleIn(AppScope::class)
    @CacheDir
    fun provideCacheDir(@AppDir appDir: Path): Path = Path(appDir, CACHE_DIR_NAME)

    private const val DATABASE_FILE_NAME = "tusu.db"
    private const val CACHE_DIR_NAME = "cache"
}
