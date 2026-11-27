package vn.id.tozydev.tusu.ui.feature.composer

import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.datetime.TimeZone
import vn.id.tozydev.tusu.domain.model.EntryLocation
import vn.id.tozydev.tusu.domain.model.Label
import vn.id.tozydev.tusu.domain.model.LabelGroup
import vn.id.tozydev.tusu.ui.component.MediaUi
import vn.id.tozydev.tusu.ui.format.UiText

data class ComposerUiState(
    val isLoading: Boolean = true,
    val occurredAt: Instant? = null,
    val timeZone: TimeZone? = null,
    /** The zone the user is in now; the date-time picker reads and writes times in it. */
    val currentTimeZone: TimeZone? = null,
    /** The entry's emoji; null when none is chosen. */
    val emoji: String? = null,
    val saveIndicator: SaveIndicator = SaveIndicator.Hidden,
    /** Set when the entry to edit cannot be opened at all (e.g. it no longer exists). */
    val loadError: UiText? = null,
    /** The stored body cannot be edited by this app version; [readOnlyText] is shown instead. */
    val isReadOnly: Boolean = false,
    val readOnlyText: String = "",
    /** Live photos of the entry in album order; empty until the entry is known. */
    val photos: List<MediaUi> = emptyList(),
    /** Live voice recordings of the entry in album order. */
    val voices: List<MediaUi> = emptyList(),
    /** Set while a voice is being recorded; the toolbar becomes the recording bar. */
    val recording: RecordingUi? = null,
    /** Where the entry happened; null shows the "Add location" button. */
    val location: EntryLocation? = null,
    /** True while the current location is being looked up. */
    val isLocating: Boolean = false,
    /** Found for a new entry when the location setting only suggests; one tap attaches it. */
    val suggestedLocation: EntryLocation? = null,
    /** Places of recent entries, offered for a quick pick. */
    val recentLocations: List<EntryLocation> = emptyList(),
    val selectedLabelIds: Set<Uuid> = emptySet(),
    /** Every live group and label, for the label sheet and the chips. */
    val labelGroups: List<LabelGroup> = emptyList(),
    val labels: List<Label> = emptyList(),
)
