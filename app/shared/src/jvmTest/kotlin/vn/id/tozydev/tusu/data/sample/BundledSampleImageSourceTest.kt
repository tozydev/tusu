package vn.id.tozydev.tusu.data.sample

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.io.buffered
import kotlinx.io.readByteArray

class BundledSampleImageSourceTest :
    FunSpec(
        {
            test("every bundled photo can be opened and is a JPEG") {
                val source = BundledSampleImageSource()

                source.images.size shouldBe 10
                for (image in source.images) {
                    val head =
                        checkNotNull(source.open(image)) { "missing ${image.name}" }
                            .buffered()
                            .use { it.readByteArray(2) }
                    head.toList() shouldBe listOf(0xFF.toByte(), 0xD8.toByte())
                }
            }

            test("an unknown photo is reported as missing") {
                BundledSampleImageSource().open(SampleImage("nope.jpg", 1, 1)) shouldBe null
            }
        },
    )
