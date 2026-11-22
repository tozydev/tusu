package vn.id.tozydev.tusu.ui.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.model.withRecentEmoji
import vn.id.tozydev.tusu.domain.repository.SettingsRepository
import vn.id.tozydev.tusu.ui.format.toUiText

/** The "Recent" group of the emoji sheet, shared by every screen that opens it. */
@ContributesIntoMap(AppScope::class)
@ViewModelKey(RecentEmojisViewModel::class)
@Inject
class RecentEmojisViewModel(
    private val settingsRepository: SettingsRepository,
    private val appMessages: AppMessages,
) : ViewModel() {
    val recent: StateFlow<List<String>> =
        settingsRepository.settings
            .map { it.recentEmojis }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                emptyList(),
            )

    fun onPicked(emoji: String) {
        viewModelScope.launch {
            val outcome = settingsRepository.update { it.withRecentEmoji(emoji) }
            if (outcome is Outcome.Failure) appMessages.post(UiMessage(outcome.error.toUiText()))
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
