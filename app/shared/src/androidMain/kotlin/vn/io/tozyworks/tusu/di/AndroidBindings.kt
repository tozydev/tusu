package vn.io.tozyworks.tusu.di

import android.app.Application
import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.filesDir
import vn.io.tozyworks.tusu.data.db.TusuDatabase
import vn.io.tozyworks.tusu.data.db.getDatabaseBuilder

@BindingContainer
@ContributesTo(AppScope::class)
object AndroidBindings {
    @Provides
    @SingleIn(AppScope::class)
    fun provideApplicationContext(application: Application): Context = application

    @Provides
    @SingleIn(AppScope::class)
    fun provideTusuDatabase(application: Application): TusuDatabase =
        TusuDatabase.create(getDatabaseBuilder(application))

    @AppDir
    @Provides
    @SingleIn(AppScope::class)
    fun provideAppDir(): PlatformFile = FileKit.filesDir
}
