package vn.id.tozydev.tusu.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import vn.id.tozydev.tusu.domain.repository.SettingsRepository

const val SETTINGS_DATASTORE_FILENAME = "tusu.preferences_pb"

@Inject
@ContributesBinding(AppScope::class, binding = binding<SettingsRepository>())
class SettingsRepositoryImpl(private val dataStore: DataStore<Preferences>) : SettingsRepository
