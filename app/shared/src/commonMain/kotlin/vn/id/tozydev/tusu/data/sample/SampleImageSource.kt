package vn.id.tozydev.tusu.data.sample

import kotlinx.io.RawSource

/** A photo the seeder can attach to entries; [width] and [height] are its pixel size. */
data class SampleImage(val name: String, val width: Int, val height: Int)

/** Where the seeder gets photo bytes from. */
interface SampleImageSource {
    val images: List<SampleImage>

    /** A fresh source over the photo's bytes; the caller closes it. Null if it cannot be found. */
    fun open(image: SampleImage): RawSource?
}
