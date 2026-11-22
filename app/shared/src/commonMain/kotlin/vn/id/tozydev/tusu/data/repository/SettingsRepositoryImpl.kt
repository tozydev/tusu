package vn.id.tozydev.tusu.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.error.SettingsError
import vn.id.tozydev.tusu.domain.model.AppSettings
import vn.id.tozydev.tusu.domain.repository.SettingsRepository

private val logger = KotlinLogging.logger {}

// Preference keys are the on-disk names. Renaming one loses the stored value, so treat them as
// part of the file format.
private val THEME_MODE = stringPreferencesKey("theme_mode")
private val SEED_COLOR = intPreferencesKey("seed_color")
private val LANGUAGE = stringPreferencesKey("language")
private val LOCATION_MODE = stringPreferencesKey("location_mode")
private val KEEP_ORIGINAL_MEDIA = booleanPreferencesKey("keep_original_media")
private val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
private val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
private val RECENT_EMOJIS = stringPreferencesKey("recent_emojis")

// Emojis never contain a line break, so one string keeps the order a string set would lose.
private const val RECENT_EMOJI_SEPARATOR = "\n"

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
class SettingsRepositoryImpl(private val dataStore: DataStore<Preferences>) : SettingsRepository {
    override val settings: Flow<AppSettings> =
        dataStore.data
            .map { it.toAppSettings() }
            // The interface has no error channel for reads. Writes still report StorageFailed,
            // and the defaults keep the app usable until the file is readable again. The read
            // is retried instead of ending the flow, so collectors get the stored settings back.
            .retryWhen { e, attempt ->
                if (e !is IOException) return@retryWhen false
                logger.warn(e) { "Could not read settings, using defaults" }
                emit(AppSettings())
                delay(readRetryDelay(attempt))
                true
            }
            .distinctUntilChanged()

    override suspend fun update(
        transform: (AppSettings) -> AppSettings,
    ): Outcome<Unit, SettingsError> =
        try {
            dataStore.edit { prefs -> prefs.writeAppSettings(transform(prefs.toAppSettings())) }
            Outcome.Success(Unit)
        } catch (e: IOException) {
            logger.warn(e) { "Could not write settings" }
            Outcome.Failure(SettingsError.StorageFailed)
        }
}

/** Doubles from one second up to [MAX_READ_RETRY_DELAY]. */
private fun readRetryDelay(attempt: Long): Duration =
    minOf(MAX_READ_RETRY_DELAY, 1.seconds * (1L shl attempt.coerceAtMost(5).toInt()).toInt())

private val MAX_READ_RETRY_DELAY = 30.seconds

private fun Preferences.toAppSettings(): AppSettings {
    val defaults = AppSettings()
    return AppSettings(
        themeMode = enumOrDefault(this[THEME_MODE], defaults.themeMode),
        seedColor = this[SEED_COLOR],
        language = enumOrDefault(this[LANGUAGE], defaults.language),
        locationMode = enumOrDefault(this[LOCATION_MODE], defaults.locationMode),
        keepOriginalMedia = this[KEEP_ORIGINAL_MEDIA] ?: defaults.keepOriginalMedia,
        isAppLockEnabled = this[APP_LOCK_ENABLED] ?: defaults.isAppLockEnabled,
        isReminderEnabled = this[REMINDER_ENABLED] ?: defaults.isReminderEnabled,
        isOnboardingCompleted = this[ONBOARDING_COMPLETED] ?: defaults.isOnboardingCompleted,
        recentEmojis =
            this[RECENT_EMOJIS]?.split(RECENT_EMOJI_SEPARATOR)?.filter { it.isNotEmpty() }
                ?: defaults.recentEmojis,
    )
}

// An unknown name, from a newer or an older app version, reads as the default instead of failing.
private inline fun <reified E : Enum<E>> enumOrDefault(name: String?, default: E): E =
    enumValues<E>().firstOrNull { it.name == name } ?: default

private fun MutablePreferences.writeAppSettings(settings: AppSettings) {
    this[THEME_MODE] = settings.themeMode.name
    writeNullable(SEED_COLOR, settings.seedColor)
    this[LANGUAGE] = settings.language.name
    this[LOCATION_MODE] = settings.locationMode.name
    this[KEEP_ORIGINAL_MEDIA] = settings.keepOriginalMedia
    this[APP_LOCK_ENABLED] = settings.isAppLockEnabled
    this[REMINDER_ENABLED] = settings.isReminderEnabled
    this[ONBOARDING_COMPLETED] = settings.isOnboardingCompleted
    this[RECENT_EMOJIS] = settings.recentEmojis.joinToString(RECENT_EMOJI_SEPARATOR)
}

// A null value is stored as an absent key, so "not set" and "never written" read the same.
private fun <T : Any> MutablePreferences.writeNullable(key: Preferences.Key<T>, value: T?) {
    if (value == null) {
        remove(key)
    } else {
        this[key] = value
    }
}
