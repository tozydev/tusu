package vn.id.tozydev.tusu.ui.feature.composer

import androidx.compose.runtime.Immutable
import kotlin.time.TimeMark

/** A voice recording in progress; the bar counts its time from [startedAt]. */
@Immutable
data class RecordingUi(
    val startedAt: TimeMark,
    /** The latest input levels, each in 0..1, oldest first. */
    val levels: List<Float> = emptyList(),
)
