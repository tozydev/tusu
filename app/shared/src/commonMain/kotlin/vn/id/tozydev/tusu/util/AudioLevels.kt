package vn.id.tozydev.tusu.util

import kotlin.math.log10

/** Largest peak a 16-bit recorder reports. */
const val MAX_AMPLITUDE = 32767

/** Bars kept in a stored waveform; enough for a full-width player on a phone. */
const val WAVEFORM_BARS = 48

// Anything quieter than this is drawn as silence. A linear scale would leave normal speech as a
// flat line, because it rarely peaks above a tenth of full scale.
private const val FLOOR_DB = 60.0

/** Maps a recorder peak in 0..[MAX_AMPLITUDE] to a display level in 0..1 on a decibel scale. */
fun normalizedLevel(amplitude: Int): Float {
    if (amplitude <= 0) return 0f
    val db = 20 * log10(amplitude.coerceAtMost(MAX_AMPLITUDE).toDouble() / MAX_AMPLITUDE)
    return ((db + FLOOR_DB) / FLOOR_DB).coerceIn(0.0, 1.0).toFloat()
}

/** At most [count] levels, each the loudest of its slice, so short peaks stay visible. */
fun List<Float>.downsampleLevels(count: Int): List<Float> {
    require(count > 0) { "count must be positive, was $count" }
    if (size <= count) return this
    return List(count) { bar ->
        val from = bar * size / count
        val to = (bar + 1) * size / count
        subList(from, to).max()
    }
}
