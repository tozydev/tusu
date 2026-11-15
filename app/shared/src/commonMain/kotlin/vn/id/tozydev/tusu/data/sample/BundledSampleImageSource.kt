package vn.id.tozydev.tusu.data.sample

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.io.RawSource
import vn.id.tozydev.tusu.platform.openBundledResource

/** The ten photos packaged under `test-data/media`; release builds leave them out. */
@ContributesBinding(AppScope::class)
@Inject
class BundledSampleImageSource : SampleImageSource {
    override val images: List<SampleImage> = BUNDLED_IMAGES

    override fun open(image: SampleImage): RawSource? =
        openBundledResource("test-data/media/00/00/${image.name}")

    private companion object {
        // Sizes are those of the files; keep in sync with `test-data/files.txt`.
        val BUNDLED_IMAGES =
            listOf(
                bundled(0x65, 3600, 2400),
                bundled(0x66, 3264, 2448),
                bundled(0x67, 6240, 4160),
                bundled(0x68, 4000, 2667),
                bundled(0x69, 2325, 3100),
                bundled(0x6a, 6282, 4189),
                bundled(0x6b, 4000, 3957),
                bundled(0x6c, 3100, 3100),
                bundled(0x6d, 2000, 3000),
                bundled(0x6e, 6336, 9504),
            )

        fun bundled(index: Int, width: Int, height: Int) =
            SampleImage(
                name = "00000000-0000-0000-0000-${index.toString(16).padStart(12, '0')}.jpg",
                width = width,
                height = height,
            )
    }
}
