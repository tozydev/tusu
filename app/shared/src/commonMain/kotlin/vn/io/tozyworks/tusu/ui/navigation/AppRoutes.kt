package vn.io.tozyworks.tusu.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoutes : NavKey {

    @Serializable data object DemoA : AppRoutes

    @Serializable data class DemoB(val message: String) : AppRoutes
}
