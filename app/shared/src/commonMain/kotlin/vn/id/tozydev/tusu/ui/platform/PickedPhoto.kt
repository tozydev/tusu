package vn.id.tozydev.tusu.ui.platform

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.source
import kotlinx.io.RawSource

/** A photo chosen by the user that has not been stored yet. [open] may be called once. */
class PickedPhoto(val extension: String, val open: () -> RawSource)

internal fun PlatformFile.toPickedPhoto(): PickedPhoto {
    val file = this
    return PickedPhoto(extension = photoExtension(file.extension)) { file.source() }
}

/** Lower-cases the extension; a photo without one is stored as jpg. */
internal fun photoExtension(raw: String?): String =
    raw?.trim()?.lowercase()?.takeIf { it.isNotEmpty() } ?: DEFAULT_PHOTO_EXTENSION

private const val DEFAULT_PHOTO_EXTENSION = "jpg"
