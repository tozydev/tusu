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

    private const val DATABASE_FILE_NAME = "tusu.db"
}
