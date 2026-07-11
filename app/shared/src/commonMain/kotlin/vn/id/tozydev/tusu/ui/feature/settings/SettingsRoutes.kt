package vn.id.tozydev.tusu.ui.feature.settings

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface SettingsRoutes : NavKey {
    @Serializable data object Main : SettingsRoutes

    @Serializable data object Appearance : SettingsRoutes

    @Serializable data object Backup : SettingsRoutes

    @Serializable data object About : SettingsRoutes
}
