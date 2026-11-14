package vn.id.tozydev.tusu.platform

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.io.files.Path
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome

/** JVM is a build and test target only, so recording is always reported as failed. */
@ContributesBinding(AppScope::class)
@Inject
class NoOpAudioRecorder : AudioRecorder {
    override suspend fun start(destination: Path): Outcome<Unit, MediaError> =
        Outcome.Failure(MediaError.RecordingFailed)

    override suspend fun stop(): Outcome<AudioRecording, MediaError> =
        Outcome.Failure(MediaError.RecordingFailed)

    override suspend fun cancel() = Unit

    override val amplitudes: Flow<Float> = emptyFlow()
}
