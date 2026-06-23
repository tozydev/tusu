package vn.io.tozydev.tusu.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoutes : NavKey {
    @Serializable data object Feed : AppRoutes

    @Serializable data object Settings : AppRoutes

    @Serializable data class EntryEditor(val entryId: Uuid? = null) : AppRoutes
}
