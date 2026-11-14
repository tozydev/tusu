package vn.id.tozydev.tusu.di

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.room3.RoomDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.io.files.Path
import okio.Path.Companion.toPath
import vn.id.tozydev.tusu.data.db.TusuDatabase
import vn.id.tozydev.tusu.data.db.buildTusuDatabase
import vn.id.tozydev.tusu.data.db.label.DefaultLabelNamesProvider

@BindingContainer
@ContributesTo(AppScope::class)
object DataBindings {
    @Provides
    @SingleIn(AppScope::class)
    @MediaDir
    fun provideMediaDir(@AppDir appDir: Path): Path = Path(appDir, "media")

    @Provides
    @SingleIn(AppScope::class)
    fun provideTusuDatabase(
        builder: RoomDatabase.Builder<TusuDatabase>,
        clock: Clock,
        labelNames: DefaultLabelNamesProvider,
    ): TusuDatabase = buildTusuDatabase(builder, clock, labelNames)

    // Only one DataStore may exist per file, so the singleton scope is what keeps writes ordered.
    @Provides
    @SingleIn(AppScope::class)
    fun provideSettingsDataStore(@AppDir appDir: Path): DataStore<Preferences> =
        PreferenceDataStoreFactory.createWithPath(
            // A corrupt file resets to defaults instead of crashing the app at startup.
            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
        ) {
            Path(appDir, SETTINGS_FILE_NAME).toString().toPath()
        }

    private const val SETTINGS_FILE_NAME = "settings.preferences_pb"
}
