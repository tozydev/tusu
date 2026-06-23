package vn.io.tozyworks.tusu.data.util

import io.github.vinceglb.filekit.PlatformFile

data class ImageDimension(
    val width: Int,
    val height: Int,
)

internal expect fun getImageDimension(file: PlatformFile): ImageDimension
