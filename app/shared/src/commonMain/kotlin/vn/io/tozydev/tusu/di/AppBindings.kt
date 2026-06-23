package vn.io.tozydev.tusu.di

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import kotlin.reflect.KClass
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.TimeZone
import vn.io.tozydev.tusu.data.db.EntryDao
import vn.io.tozydev.tusu.data.db.MediaDao
import vn.io.tozydev.tusu.data.db.TagDao
import vn.io.tozydev.tusu.data.db.TusuDatabase

@BindingContainer
@ContributesTo(AppScope::class)
object AppBindings {
    @Provides
    @SingleIn(AppScope::class)
    fun provideAppScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @SingleIn(AppScope::class)
    fun provideMetroViewModelFactory(
        viewModelProvides: Map<KClass<out ViewModel>, () -> ViewModel>,
        assistedFactoryProviders: Map<KClass<out ViewModel>, () -> ViewModelAssistedFactory>,
        manualAssistedFactoryProviders:
            Map<KClass<out ManualViewModelAssistedFactory>, () -> ManualViewModelAssistedFactory>,
    ): MetroViewModelFactory =
        object : MetroViewModelFactory() {
            override val viewModelProviders
                get() = viewModelProvides

            override val assistedFactoryProviders
                get() = assistedFactoryProviders

            override val manualAssistedFactoryProviders
                get() = manualAssistedFactoryProviders
        }

    @Provides
    @SingleIn(AppScope::class)
    fun provideTimeZone(): TimeZone = TimeZone.currentSystemDefault()

    @Provides @SingleIn(AppScope::class) fun provideClock(): Clock = Clock.System

    @Provides
    @SingleIn(AppScope::class)
    fun provideEntryDao(database: TusuDatabase): EntryDao = database.entryDao()

    @Provides
    @SingleIn(AppScope::class)
    fun provideTagDao(database: TusuDatabase): TagDao = database.tagDao()

    @Provides
    @SingleIn(AppScope::class)
    fun provideMediaDao(database: TusuDatabase): MediaDao = database.mediaDao()
}
