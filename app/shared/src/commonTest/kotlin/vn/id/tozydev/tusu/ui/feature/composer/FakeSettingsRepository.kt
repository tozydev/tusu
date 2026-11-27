package vn.id.tozydev.tusu.ui.feature.composer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.error.SettingsError
import vn.id.tozydev.tusu.domain.model.AppSettings
import vn.id.tozydev.tusu.domain.repository.SettingsRepository

/** Settings kept in memory; a test changes [state] directly. */
class FakeSettingsRepository : SettingsRepository {
    val state = MutableStateFlow(AppSettings())
    override val settings: Flow<AppSettings> = state

    override suspend fun update(
        transform: (AppSettings) -> AppSettings,
    ): Outcome<Unit, SettingsError> {
        state.value = transform(state.value)
        return Outcome.Success(Unit)
    }
}
