package vn.id.tozydev.tusu.platform

import kotlinx.coroutines.CompletableDeferred
import kotlinx.io.files.Path
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome

/**
 * Compressor whose result a test chooses. A [gate], when set, holds each call until the test
 * completes it, so a test can observe a job that is still in flight.
 */
internal class ScriptedImageCompressor(
    var result: Outcome<ImageSize, MediaError> = Outcome.Success(ImageSize(1600, 1200)),
) : ImageCompressor {
    val calls = mutableListOf<Path>()
    var gate: CompletableDeferred<Unit>? = null

    override suspend fun compress(
        source: Path,
        destination: Path,
        maxLongEdgePx: Int,
    ): Outcome<ImageSize, MediaError> {
        calls += source
        gate?.await()
        return result
    }
}
