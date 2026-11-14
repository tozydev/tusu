package vn.id.tozydev.tusu.platform

import kotlin.time.Duration

/** A finished recording. [waveform] holds amplitudes in 0..1, downsampled for display. */
data class AudioRecording(val duration: Duration, val waveform: List<Float>)
