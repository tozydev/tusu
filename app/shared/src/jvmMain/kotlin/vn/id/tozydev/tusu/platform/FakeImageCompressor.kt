package vn.id.tozydev.tusu.platform

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome

private const val FAKE_LONG_EDGE_PX = 1600

/** Copies the source unchanged. JVM is a build and test target, so there is no real encoder. */
@ContributesBinding(AppScope::class)
@Inject
class FakeImageCompressor : ImageCompressor {
    override suspend fun compress(
        source: Path,
        destination: Path,
        maxLongEdgePx: Int,
    ): Outcome<ImageSize, MediaError> =
        withContext(Dispatchers.IO) {
            if (SystemFileSystem.metadataOrNull(source)?.isRegularFile != true) {
                return@withContext Outcome.Failure(MediaError.FileUnreadable)
            }
            // Read everything before opening the destination, so source and destination may be
            // the same file.
            val bytes =
                try {
                    SystemFileSystem.source(source).buffered().use { it.readByteArray() }
                } catch (e: IOException) {
                    return@withContext Outcome.Failure(MediaError.FileUnreadable)
                }
            try {
                SystemFileSystem.sink(destination).buffered().use { it.write(bytes) }
            } catch (e: IOException) {
                return@withContext Outcome.Failure(MediaError.StorageFailed)
            }
            val longEdge = minOf(FAKE_LONG_EDGE_PX, maxLongEdgePx)
            Outcome.Success(ImageSize(width = longEdge, height = longEdge * 3 / 4))
        }
}
