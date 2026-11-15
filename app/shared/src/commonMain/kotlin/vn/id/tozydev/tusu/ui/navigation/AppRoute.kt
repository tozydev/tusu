package vn.id.tozydev.tusu.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

/** Every destination of the app; features reach each other only by pushing one of these. */
@Serializable
sealed interface AppRoute : NavKey {
    /** A destination reachable from the floating navigation bar. */
    sealed interface TopLevel : AppRoute

    @Serializable data object Timeline : TopLevel

    @Serializable data object Calendar : TopLevel

    @Serializable data object Media : TopLevel

    /** Writes a new entry, or edits the one with [entryId]. */
    @Serializable data class Composer(val entryId: Uuid? = null) : AppRoute
}
