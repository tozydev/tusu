package vn.id.tozydev.tusu.ui.feature.composer

import kotlin.uuid.Uuid
import kotlinx.coroutines.CompletableDeferred
import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.readByteArray
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome
import vn.id.tozydev.tusu.domain.repository.MediaFileStore

/** Media files kept in memory; a test can make writes fail or hold them until it releases them. */
class FakeMediaFileStore : MediaFileStore {
    private val files = mutableMapOf<String, String>()

    /** Contents in the order they were written. */
    val writes = mutableListOf<String>()

    var failWrites = false

    /** When set, a write waits for this to complete, so a test can end the session mid-write. */
    var writeGate: CompletableDeferred<Unit>? = null

    fun contentOf(relativePath: String): String? = files[relativePath]

    override fun newRelativePath(id: Uuid, extension: String): String = "$id.$extension"

    override fun resolve(relativePath: String): Path = Path("media", relativePath)

    override suspend fun write(relativePath: String, source: RawSource): Outcome<Unit, MediaError> {
        val content = source.use { it.buffered().readByteArray().decodeToString() }
        writeGate?.await()
        if (failWrites) return Outcome.Failure(MediaError.StorageFailed)
        files[relativePath] = content
        writes += content
        return Outcome.Success(Unit)
    }

    override suspend fun delete(relativePath: String): Outcome<Unit, MediaError> {
        files.remove(relativePath)
        return Outcome.Success(Unit)
    }

    override suspend fun exists(relativePath: String): Boolean = relativePath in files
}
