package vn.id.tozydev.tusu.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.io.files.Path
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome

interface AudioRecorder {
    suspend fun start(destination: Path): Outcome<Unit, MediaError>

    suspend fun stop(): Outcome<AudioRecording, MediaError>

    suspend fun cancel()

    /** Live input level in 0..1 while recording; empty when not recording. */
    val amplitudes: Flow<Float>
}
