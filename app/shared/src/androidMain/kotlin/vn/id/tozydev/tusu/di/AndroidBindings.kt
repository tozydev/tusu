package vn.id.tozydev.tusu.di

import android.app.Application
import android.content.Context
import androidx.room3.RoomDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.io.files.Path
import vn.id.tozydev.tusu.data.db.TusuDatabase
import vn.id.tozydev.tusu.data.db.label.ComposeDefaultLabelNamesProvider
import vn.id.tozydev.tusu.data.db.label.DefaultLabelNamesProvider
import vn.id.tozydev.tusu.data.db.tusuDatabaseBuilder

@BindingContainer
@ContributesTo(AppScope::class)
object AndroidBindings {
    @Provides
    @SingleIn(AppScope::class)
    fun provideApplicationContext(application: Application): Context = application

    @Provides
    @SingleIn(AppScope::class)
    @AppDir
    fun provideAppDir(context: Context): Path = Path(context.filesDir.absolutePath)

    @Provides
    @SingleIn(AppScope::class)
    @CacheDir
    fun provideCacheDir(context: Context): Path = Path(context.cacheDir.absolutePath)

    @Provides
    @SingleIn(AppScope::class)
    fun provideDatabaseBuilder(context: Context): RoomDatabase.Builder<TusuDatabase> =
        tusuDatabaseBuilder(context)

    @Provides
    @SingleIn(AppScope::class)
    fun provideDefaultLabelNames(): DefaultLabelNamesProvider = ComposeDefaultLabelNamesProvider()
}
