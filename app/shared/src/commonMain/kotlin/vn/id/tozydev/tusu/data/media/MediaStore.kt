package vn.id.tozydev.tusu.data.media

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import vn.id.tozydev.tusu.di.MediaDir
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.repository.MediaFileStore

private val logger = KotlinLogging.logger {}

/**
 * Media files under [root], the app's private media folder. Blocking file I/O runs on
 * [Dispatchers.IO], which is available in common code.
 */
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
class MediaStore(@MediaDir private val root: Path) : MediaFileStore {
    init {
        require(root.isAbsolute) { "Media folder must be an absolute path: $root" }
    }

    override fun newRelativePath(id: Uuid, extension: String): String {
        val safeExtension = extension.lowercase().filter { it in 'a'..'z' || it in '0'..'9' }
        require(safeExtension.isNotEmpty()) { "Media extension has no letters or digits" }
        // Two levels of folders keep each one small even with tens of thousands of photos.
        val name = id.toString()
        return "${name.substring(0, 2)}/${name.substring(2, 4)}/$name.$safeExtension"
    }

    override fun resolve(relativePath: String): Path {
        checkInsideRoot(relativePath)
        return Path(root, relativePath)
    }

    override suspend fun write(relativePath: String, source: RawSource): Outcome<Unit, MediaError> {
        try {
            val target = resolve(relativePath)
            return withContext(Dispatchers.IO) { writeAtomically(target, source) }
        } finally {
            // Closed on every path, including a rejected path, because the caller hands the
            // source over for good.
            source.close()
        }
    }

    override suspend fun delete(relativePath: String): Outcome<Unit, MediaError> {
        val target = resolve(relativePath)
        return withContext(Dispatchers.IO) {
            try {
                SystemFileSystem.delete(target, mustExist = false)
                Outcome.Success(Unit)
            } catch (e: IOException) {
                logger.warn(e) { "Could not delete media file" }
                Outcome.Failure(MediaError.StorageFailed)
            }
        }
    }

    override suspend fun exists(relativePath: String): Boolean {
        val target = resolve(relativePath)
        return withContext(Dispatchers.IO) {
            try {
                SystemFileSystem.exists(target)
            } catch (e: IOException) {
                // The signature has no error channel. "Not there" is the safe answer: callers
                // then re-attach or skip the file instead of trusting a file they cannot read.
                logger.warn(e) { "Could not check media file" }
                false
            }
        }
    }

    /**
     * Writes into a temp sibling and moves it into place, so a crash never leaves a half-written
     * file under the final name.
     */
    private fun writeAtomically(target: Path, source: RawSource): Outcome<Unit, MediaError> {
        val tracked = ReadTrackingSource(source)
        val temp = Path("$target.part-${Uuid.random()}")
        return try {
            target.parent?.let { SystemFileSystem.createDirectories(it) }
            SystemFileSystem.sink(temp).buffered().use { it.transferFrom(tracked) }
            SystemFileSystem.atomicMove(temp, target)
            Outcome.Success(Unit)
        } catch (e: IOException) {
            logger.warn(e) { "Could not store media file" }
            removeTemp(temp)
            Outcome.Failure(
                if (tracked.readFailed) MediaError.FileUnreadable else MediaError.StorageFailed,
            )
        }
    }

    private fun removeTemp(temp: Path) {
        try {
            SystemFileSystem.delete(temp, mustExist = false)
        } catch (e: IOException) {
            // The write error is what the caller needs; a leftover temp file is only disk use.
            logger.warn(e) { "Could not remove temp media file" }
        }
    }

    // Paths come from the database or from newRelativePath, so a bad one is a bug in the caller
    // and fails loudly. Checking the segments keeps every file inside the media folder.
    private fun checkInsideRoot(relativePath: String) {
        require(relativePath.isNotEmpty()) { "Media path is empty" }
        require(!relativePath.startsWith('/') && !relativePath.startsWith('\\')) {
            "Media path must be relative: $relativePath"
        }
        require(!Path(relativePath).isAbsolute) { "Media path must be relative: $relativePath" }
        val segments = relativePath.split('/', '\\')
        require(segments.none { it.isEmpty() || it == "." || it == ".." }) {
            "Media path leaves the media folder: $relativePath"
        }
    }
}

/**
 * Tells a failure while reading the source from a failure while storing, so the error is accurate.
 */
private class ReadTrackingSource(private val source: RawSource) : RawSource {
    var readFailed = false
        private set

    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long =
        try {
            source.readAtMostTo(sink, byteCount)
        } catch (e: IOException) {
            readFailed = true
            throw e
        }

    override fun close() = source.close()
}
