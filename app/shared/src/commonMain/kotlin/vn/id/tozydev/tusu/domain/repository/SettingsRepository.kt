package vn.id.tozydev.tusu.domain.repository

import kotlinx.coroutines.flow.Flow
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.error.SettingsError
import vn.id.tozydev.tusu.domain.model.AppSettings

interface SettingsRepository {
    val settings: Flow<AppSettings>

    /** Applies [transform] to the stored settings atomically; it may run more than once. */
    suspend fun update(transform: (AppSettings) -> AppSettings): Outcome<Unit, SettingsError>
}
