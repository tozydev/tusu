package vn.id.tozydev.tusu.ui.platform

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PickedPhotoTest :
    FunSpec(
        {
            test("keeps a lower-case extension") { photoExtension("jpg") shouldBe "jpg" }

            test("lower-cases the extension") { photoExtension("HEIC") shouldBe "heic" }

            test("trims surrounding spaces") { photoExtension("  png ") shouldBe "png" }

            test("falls back to jpg when the file has no extension") {
                photoExtension(null) shouldBe "jpg"
                photoExtension("") shouldBe "jpg"
                photoExtension("   ") shouldBe "jpg"
            }
        },
    )
