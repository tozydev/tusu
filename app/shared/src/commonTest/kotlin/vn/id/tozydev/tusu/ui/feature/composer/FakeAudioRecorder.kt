package vn.id.tozydev.tusu.ui.feature.composer

import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.io.files.Path
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.platform.AudioRecorder
import vn.id.tozydev.tusu.platform.AudioRecording

/** Records nothing; a test sets the outcomes and pushes levels through [levels]. */
class FakeAudioRecorder : AudioRecorder {
    var startResult: Outcome<Unit, MediaError> = Outcome.Success(Unit)
    var stopResult: Outcome<AudioRecording, MediaError> =
        Outcome.Success(AudioRecording(3.seconds, listOf(0.2f, 0.8f, 0.5f)))

    val destinations = mutableListOf<Path>()
    var stops = 0
    var cancels = 0
    var isRecording = false
        private set

    val levels = MutableSharedFlow<Float>(extraBufferCapacity = 16)
    override val amplitudes: Flow<Float> = levels

    override suspend fun start(destination: Path): Outcome<Unit, MediaError> {
        destinations += destination
        isRecording = startResult is Outcome.Success
        return startResult
    }

    override suspend fun stop(): Outcome<AudioRecording, MediaError> {
        stops++
        isRecording = false
        return stopResult
    }

    override suspend fun cancel() {
        cancels++
        isRecording = false
    }
}
