package vn.id.tozydev.tusu.di

import android.app.Application
import android.content.Context
import androidx.core.net.toFile
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.FileStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesFileSerializer
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.toAndroidUri
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.resolve
import vn.id.tozydev.tusu.data.db.TusuDatabase
import vn.id.tozydev.tusu.data.db.getDatabaseBuilder
import vn.id.tozydev.tusu.data.repository.SETTINGS_DATASTORE_FILENAME

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

    @Provides
    @SingleIn(AppScope::class)
    fun provideDataStore(@AppDir appDir: PlatformFile): DataStore<Preferences> =
        DataStoreFactory.create(
            storage =
                FileStorage(
                    serializer = PreferencesFileSerializer,
                    produceFile = {
                        appDir.resolve(SETTINGS_DATASTORE_FILENAME).toAndroidUri().toFile()
                    },
                )
        )
}
