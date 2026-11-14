package vn.id.tozydev.tusu.platform

import kotlinx.io.files.Path
import vn.id.tozydev.tusu.domain.error.MediaError
import vn.id.tozydev.tusu.domain.error.Outcome

interface ImageCompressor {
    /** Writes a JPEG of [source] to [destination] whose long edge is at most [maxLongEdgePx]. */
    suspend fun compress(
        source: Path,
        destination: Path,
        maxLongEdgePx: Int = 2048,
    ): Outcome<ImageSize, MediaError>
}
