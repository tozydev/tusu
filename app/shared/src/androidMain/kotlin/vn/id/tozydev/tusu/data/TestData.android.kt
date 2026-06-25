package vn.id.tozydev.tusu.data

import android.util.Log
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.parent
import io.github.vinceglb.filekit.resolve
import io.github.vinceglb.filekit.write

private const val TEST_DATA_DIR = "test-data"
private const val MEDIA_DIR = "media"
private const val TEST_DATA_MEDIA_FILES_INDEX = "${TEST_DATA_DIR}/files.txt"

private const val LOG_TAG = "TestData"

internal actual suspend fun copyTestMediaToAppDir() {
    if (FileKit.filesDir.resolve(MEDIA_DIR).exists()) {
        return
    }

    val classLoader = TestData::class.java.classLoader
    if (classLoader == null) {
        Log.i(LOG_TAG, "Class loader not found")
        return
    }
    classLoader
        .getResourceAsStream(TEST_DATA_MEDIA_FILES_INDEX)
        ?.use { inputStream ->
            inputStream.bufferedReader().use { it.readLines() }.filter { it.isNotBlank() }
        }
        .also {
            if (it == null) {
                Log.i(LOG_TAG, "No $TEST_DATA_MEDIA_FILES_INDEX found")
            }
        }
        ?.forEach { path ->
            val resPath = "$TEST_DATA_DIR/$path"
            classLoader.getResourceAsStream(resPath)?.use { inputStream ->
                val file = FileKit.filesDir.resolve(path)
                file.parent()?.createDirectories()
                inputStream.buffered().use {
                    file.write(it.readBytes())
                }
                Log.d(LOG_TAG, "Copied $resPath to $file")
            } ?: Log.w(LOG_TAG, "No media file found for $resPath")
        }
}
