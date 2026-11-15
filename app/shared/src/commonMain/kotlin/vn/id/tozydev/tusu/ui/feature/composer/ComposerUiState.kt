package vn.id.tozydev.tusu.ui.feature.composer

import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import vn.id.tozydev.tusu.ui.format.UiText

data class ComposerUiState(
    val isLoading: Boolean = true,
    val occurredAt: Instant? = null,
    val timeZone: TimeZone? = null,
    /** The entry's emoji; null when none is chosen. */
    val emoji: String? = null,
    val saveIndicator: SaveIndicator = SaveIndicator.Hidden,
    /** Set when the entry to edit cannot be opened at all (e.g. it no longer exists). */
    val loadError: UiText? = null,
    /** The stored body cannot be edited by this app version; [readOnlyText] is shown instead. */
    val isReadOnly: Boolean = false,
    val readOnlyText: String = "",
)
