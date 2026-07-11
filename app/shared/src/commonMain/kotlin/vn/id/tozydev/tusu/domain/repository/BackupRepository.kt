package vn.id.tozydev.tusu.domain.repository

import io.github.vinceglb.filekit.PlatformFile

interface BackupRepository {
    suspend fun exportBackup(destFile: PlatformFile)
    suspend fun importBackup(srcFile: PlatformFile)
}
