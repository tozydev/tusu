package vn.id.tozydev.tusu.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.error.SettingsError
import vn.id.tozydev.tusu.domain.model.AppLanguage
import vn.id.tozydev.tusu.domain.model.AppSettings
import vn.id.tozydev.tusu.domain.model.LocationMode
import vn.id.tozydev.tusu.domain.model.ThemeMode

class SettingsRepositoryImplTest :
    FunSpec(
        {
            val storeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            val tempDirectories = mutableListOf<Path>()

            // A real file-backed store, so the tests exercise the on-disk format.
            fun newStore(): DataStore<Preferences> {
                val dir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "tusu-settings-${Uuid.random()}"
                FileSystem.SYSTEM.createDirectories(dir)
                tempDirectories += dir
                return PreferenceDataStoreFactory.createWithPath(scope = storeScope) {
                    dir / "settings.preferences_pb"
                }
            }

            afterSpec {
                tempDirectories.forEach { FileSystem.SYSTEM.deleteRecursively(it) }
                storeScope.cancel()
            }

            test("an empty store yields the default settings") {
                SettingsRepositoryImpl(newStore()).settings.first() shouldBe AppSettings()
            }

            test("every field round-trips through the store") {
                val repository = SettingsRepositoryImpl(newStore())
                val expected =
                    AppSettings(
                        themeMode = ThemeMode.DARK,
                        seedColor = 0xFF112233.toInt(),
                        locationMode = LocationMode.OFF,
                        keepOriginalMedia = true,
                        isAppLockEnabled = true,
                        isReminderEnabled = true,
                        isOnboardingCompleted = true,
                        language = AppLanguage.ENGLISH,
                    )

                repository.update { expected } shouldBe Outcome.Success(Unit)

                repository.settings.first() shouldBe expected
            }

            test("a nullable seed colour can be set and then cleared") {
                val repository = SettingsRepositoryImpl(newStore())

                repository.update { it.copy(seedColor = 0xFFAABBCC.toInt()) }
                repository.settings.first().seedColor shouldBe 0xFFAABBCC.toInt()

                repository.update { it.copy(seedColor = null) }
                repository.settings.first().seedColor shouldBe null
            }

            test("successive updates compose and see the current values") {
                val repository = SettingsRepositoryImpl(newStore())

                repository.update { it.copy(themeMode = ThemeMode.LIGHT) }
                repository.update { it.copy(language = AppLanguage.VIETNAMESE) }
                repository.update { it.copy(isReminderEnabled = !it.isReminderEnabled) }
                repository.update { it.copy(isReminderEnabled = !it.isReminderEnabled) }

                repository.settings.first() shouldBe
                    AppSettings(themeMode = ThemeMode.LIGHT, language = AppLanguage.VIETNAMESE)
            }

            test("unknown enum names in the file fall back to the defaults") {
                val store = newStore()
                store.edit {
                    it[stringPreferencesKey("theme_mode")] = "AMOLED_FUTURE"
                    it[stringPreferencesKey("location_mode")] = "WHEREVER"
                    it[stringPreferencesKey("language")] = "KLINGON"
                }

                SettingsRepositoryImpl(store).settings.first() shouldBe AppSettings()
            }

            test("a write failure returns StorageFailed") {
                val repository =
                    SettingsRepositoryImpl(FailingDataStore(flowOf(emptyPreferences())))

                repository.update { it.copy(themeMode = ThemeMode.DARK) } shouldBe
                    Outcome.Failure(SettingsError.StorageFailed)
            }

            test("a read failure is retried and the stored settings come back") {
                runTest {
                    var reads = 0
                    val stored =
                        preferencesOf(stringPreferencesKey("theme_mode") to ThemeMode.DARK.name)
                    val data = flow {
                        if (reads++ == 0) throw IOException("unreadable", null)
                        emit(stored)
                    }

                    val repository = SettingsRepositoryImpl(FailingDataStore(data))

                    repository.settings.take(2).toList() shouldBe
                        listOf(AppSettings(), AppSettings(themeMode = ThemeMode.DARK))
                }
            }

            test("a read failure emits the default settings") {
                val repository =
                    SettingsRepositoryImpl(
                        FailingDataStore(flow { throw IOException("unreadable", null) }),
                    )

                repository.settings.first() shouldBe AppSettings()
            }
        },
    )

/** A store whose reads are [data] and whose writes always fail with an [IOException]. */
private class FailingDataStore(override val data: Flow<Preferences>) : DataStore<Preferences> {
    override suspend fun updateData(
        transform: suspend (t: Preferences) -> Preferences,
    ): Preferences = throw IOException("disk full", null)
}
