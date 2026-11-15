package vn.id.tozydev.tusu.ui.feature.composer

import vn.id.tozydev.tusu.ui.format.UiText

/** What the save indicator in the top bar shows. */
sealed interface SaveIndicator {
    data object Hidden : SaveIndicator

    /** A change is pending or being written. */
    data object Saving : SaveIndicator

    /** Shown for a moment after a successful save. */
    data object Saved : SaveIndicator

    data class Failed(val message: UiText) : SaveIndicator
}
