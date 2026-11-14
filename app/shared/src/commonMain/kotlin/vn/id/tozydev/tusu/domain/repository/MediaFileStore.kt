package vn.id.tozydev.tusu.domain.repository

import kotlin.uuid.Uuid
import kotlinx.io.RawSource
import kotlinx.io.files.Path
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome

/** Files of the media folder. The database keeps only the path relative to that folder. */
interface MediaFileStore {
    /**
     * Relative path for a new media file, e.g. `ab/cd/<uuid>.jpg`; sharded so no folder grows huge.
     */
    fun newRelativePath(id: Uuid, extension: String): String

    /** Absolute path of [relativePath] inside the media folder. */
    fun resolve(relativePath: String): Path

    /** Stores [source] under [relativePath]; closes [source] whether or not the write succeeds. */
    suspend fun write(relativePath: String, source: RawSource): Outcome<Unit, MediaError>

    /** Deleting a file that is already missing succeeds. */
    suspend fun delete(relativePath: String): Outcome<Unit, MediaError>

    suspend fun exists(relativePath: String): Boolean
}
