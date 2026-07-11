package vn.id.tozydev.tusu.ui.feature.settings

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import vn.id.tozydev.tusu.domain.repository.SettingsRepository

@ViewModelKey
@ContributesIntoMap(AppScope::class)
class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

}
