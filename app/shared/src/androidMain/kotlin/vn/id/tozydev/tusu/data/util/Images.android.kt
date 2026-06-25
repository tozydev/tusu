package vn.id.tozydev.tusu.data.util

import android.graphics.BitmapFactory
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path

internal actual fun getImageDimension(file: PlatformFile): ImageDimension {
    val options =
        BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
    BitmapFactory.decodeFile(file.path, options)
    return ImageDimension(options.outWidth, options.outHeight)
}
