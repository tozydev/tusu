package vn.id.tozydev.tusu.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.error.PermissionError
import vn.id.tozydev.tusu.domain.model.AppPermission
import vn.id.tozydev.tusu.util.WAVEFORM_BARS
import vn.id.tozydev.tusu.util.downsampleLevels
import vn.id.tozydev.tusu.util.normalizedLevel

private val logger = KotlinLogging.logger {}

private val LEVEL_INTERVAL = 100.milliseconds
private const val SAMPLE_RATE = 44_100
private const val BIT_RATE = 64_000

/** Records mono AAC in an MPEG-4 container (`.m4a`) with [MediaRecorder]. */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class AndroidAudioRecorder(private val context: Context, private val appScope: CoroutineScope) :
    AudioRecorder {
    private val lock = Mutex()
    private var session: Session? = null

    private val levels =
        MutableSharedFlow<Float>(
            extraBufferCapacity = 16,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    override val amplitudes: Flow<Float> = levels.asSharedFlow()

    override suspend fun start(destination: Path): Outcome<Unit, MediaError> =
        lock.withLock {
            check(session == null) { "A recording is already running" }
            if (!hasPermission()) {
                return@withLock Outcome.Failure(
                    MediaError.PermissionRequired(PermissionError.Denied(AppPermission.MICROPHONE))
                )
            }
            val file = File(destination.toString())
            when (val opened = withContext(Dispatchers.IO) { open(file) }) {
                is Outcome.Success -> {
                    val started = opened.value
                    started.poller = appScope.launch(Dispatchers.Default) { poll(started) }
                    session = started
                    Outcome.Success(Unit)
                }
                is Outcome.Failure -> opened
            }
        }

    override suspend fun stop(): Outcome<AudioRecording, MediaError> =
        lock.withLock {
            val current = session ?: return@withLock Outcome.Failure(MediaError.RecordingFailed)
            session = null
            current.poller?.cancelAndJoin()
            val duration = current.startedAt.elapsedNow()
            withContext(Dispatchers.IO) {
                try {
                    current.recorder.stop()
                    Outcome.Success(
                        AudioRecording(duration, current.levels.downsampleLevels(WAVEFORM_BARS))
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: RuntimeException) {
                    // MediaRecorder throws when it stopped before any audio was written, e.g. a
                    // tap on stop right after start; the file is unusable.
                    logger.warn(e) { "Recording could not be finished" }
                    current.file.delete()
                    Outcome.Failure(MediaError.RecordingFailed)
                } finally {
                    current.recorder.release()
                }
            }
        }

    override suspend fun cancel() {
        lock.withLock {
            val current = session ?: return@withLock
            session = null
            current.poller?.cancelAndJoin()
            withContext(Dispatchers.IO) {
                // reset() discards the recording without the exception stop() throws when nothing
                // was written yet.
                current.recorder.reset()
                current.recorder.release()
                current.file.delete()
            }
        }
    }

    private fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    private fun open(file: File): Outcome<Session, MediaError> {
        val recorder = newRecorder()
        return try {
            file.parentFile?.mkdirs()
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setAudioChannels(1)
            recorder.setAudioSamplingRate(SAMPLE_RATE)
            recorder.setAudioEncodingBitRate(BIT_RATE)
            recorder.setOutputFile(file.absolutePath)
            recorder.prepare()
            recorder.start()
            Outcome.Success(Session(recorder, file, TimeSource.Monotonic.markNow()))
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            logger.warn(e) { "Recording file could not be opened" }
            recorder.release()
            file.delete()
            Outcome.Failure(MediaError.StorageFailed)
        } catch (e: RuntimeException) {
            // Thrown when the microphone is in use by another app or the device has none.
            logger.warn(e) { "Recording could not be started" }
            recorder.release()
            file.delete()
            Outcome.Failure(MediaError.RecordingFailed)
        }
    }

    private suspend fun poll(session: Session) {
        while (currentCoroutineContext().isActive) {
            val level = normalizedLevel(session.recorder.maxAmplitude)
            session.levels += level
            levels.tryEmit(level)
            delay(LEVEL_INTERVAL)
        }
    }

    private fun newRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION") MediaRecorder()
        }

    /** [levels] is written only by [poller] and read after it has been joined. */
    private class Session(val recorder: MediaRecorder, val file: File, val startedAt: TimeMark) {
        val levels = mutableListOf<Float>()
        var poller: Job? = null
    }
}
