package vn.id.tozydev.tusu.ui.feature.entry

import androidx.compose.runtime.Immutable
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import vn.id.tozydev.tusu.domain.model.EntryLocation
import vn.id.tozydev.tusu.domain.model.Label
import vn.id.tozydev.tusu.ui.component.MediaUi

@Immutable
data class EntryDetailUiState(
    val isLoading: Boolean = true,
    /** The entry does not exist or is in the trash; the screen closes itself. */
    val isGone: Boolean = false,
    /** The zone the app is in now; the date-time dialog starts from it. */
    val currentTimeZone: TimeZone? = null,
    val emoji: String? = null,
    val occurredAt: Instant? = null,
    val timeZone: TimeZone? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val body: String = "",
    val textPlain: String = "",
    val photos: List<MediaUi> = emptyList(),
    /** Voice notes in album order; several are shown as one group. */
    val voices: List<MediaUi> = emptyList(),
    val labels: List<Label> = emptyList(),
    val location: EntryLocation? = null,
)
