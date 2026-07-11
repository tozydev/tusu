package vn.id.tozydev.tusu.data.util

import io.github.vinceglb.filekit.PlatformFile

expect suspend fun writeBackupZip(
    destFile: PlatformFile,
    jsonContent: String,
    mediaFiles: List<Pair<String, PlatformFile>>
)

expect suspend fun readBackupZip(
    srcFile: PlatformFile,
    destMediaDir: PlatformFile
): String
