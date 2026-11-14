package vn.id.tozydev.tusu.domain.error

sealed interface BackupError {
    /** The file is not a Tusu ZIP archive, or its `tusu.json` is missing or malformed. */
    data object InvalidArchive : BackupError

    /** The archive was written by a newer app version. */
    data class UnsupportedSchemaVersion(val found: Int, val supported: Int) : BackupError

    data object InsufficientStorage : BackupError

    data object ReadFailed : BackupError

    data object WriteFailed : BackupError
}
