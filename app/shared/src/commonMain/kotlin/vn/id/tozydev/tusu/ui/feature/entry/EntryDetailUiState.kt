package vn.id.tozydev.tusu.ui.feature.entry

import androidx.compose.runtime.Immutable
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import vn.id.tozydev.tusu.ui.component.MediaUi

@Immutable
data class EntryDetailUiState(
    val isLoading: Boolean = true,
    /** The entry does not exist or is in the trash; the screen closes itself. */
    val isGone: Boolean = false,
    val emoji: String? = null,
    val occurredAt: Instant? = null,
    val timeZone: TimeZone? = null,
    val body: String = "",
    val textPlain: String = "",
    val photos: List<MediaUi> = emptyList(),
)
