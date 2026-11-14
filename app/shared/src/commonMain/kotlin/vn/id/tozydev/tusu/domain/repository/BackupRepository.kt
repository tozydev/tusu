package vn.id.tozydev.tusu.domain.repository

import kotlinx.io.RawSink
import kotlinx.io.RawSource
import vn.id.tozydev.tusu.domain.error.BackupError
import vn.id.tozydev.tusu.domain.error.Outcome

interface BackupRepository {
    /** Writes every table, a Markdown file per entry and all media as a ZIP archive to [sink]. */
    suspend fun exportTo(sink: RawSink): Outcome<Unit, BackupError>

    /**
     * Merges the archive read from [source] into the local data; for a record present on both sides
     * the later `updatedAt` wins.
     */
    suspend fun importFrom(source: RawSource): Outcome<Unit, BackupError>
}
