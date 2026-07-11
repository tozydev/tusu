package vn.id.tozydev.tusu.data.util

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.write
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun writeBackupZip(
    destFile: PlatformFile,
    jsonContent: String,
    mediaFiles: List<Pair<String, PlatformFile>>,
) {
    withContext(Dispatchers.IO) {
        val tempFile = File.createTempFile("tusu_export_", ".zip")
        try {
            ZipOutputStream(FileOutputStream(tempFile)).use { zipOut ->
                // Write data.json
                val jsonEntry = ZipEntry("data.json")
                zipOut.putNextEntry(jsonEntry)
                zipOut.write(jsonContent.toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()

                // Write media files with a buffer to avoid loading whole files into memory
                val buffer = ByteArray(8192)
                for ((path, file) in mediaFiles) {
                    val cleanPath = path.removePrefix("./")
                    val mediaEntry = ZipEntry(cleanPath)
                    zipOut.putNextEntry(mediaEntry)

                    val localFile = File(file.path)
                    if (localFile.exists()) {
                        FileInputStream(localFile).use { input ->
                            var len: Int
                            while (input.read(buffer).also { len = it } > 0) {
                                zipOut.write(buffer, 0, len)
                            }
                        }
                    }
                    zipOut.closeEntry()
                }
            }
            destFile.write(tempFile.readBytes())
        } finally {
            tempFile.delete()
        }
    }
}

actual suspend fun readBackupZip(
    srcFile: PlatformFile,
    destMediaDir: PlatformFile,
): String {
    return withContext(Dispatchers.IO) {
        val tempFile = File.createTempFile("tusu_import_", ".zip")
        try {
            tempFile.writeBytes(srcFile.readBytes())
            var dataJson = ""
            val localMediaDir = File(destMediaDir.path)
            val buffer = ByteArray(8192)

            ZipInputStream(FileInputStream(tempFile)).use { zipIn ->
                var entry: ZipEntry? = zipIn.nextEntry
                while (entry != null) {
                    val entryName = entry.name.removePrefix("./")
                    if (entryName == "data.json") {
                        val out = ByteArrayOutputStream()
                        var len: Int
                        while (zipIn.read(buffer).also { len = it } > 0) {
                            out.write(buffer, 0, len)
                        }
                        dataJson = out.toString("UTF-8")
                    } else if (entryName.startsWith("media/")) {
                        val outputFile = File(localMediaDir, entryName)
                        outputFile.parentFile?.mkdirs()
                        FileOutputStream(outputFile).use { out ->
                            var len: Int
                            while (zipIn.read(buffer).also { len = it } > 0) {
                                out.write(buffer, 0, len)
                            }
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }

            dataJson
        } finally {
            tempFile.delete()
        }
    }
}
