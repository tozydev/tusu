package vn.io.tozyworks.tusu

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class SharedLogicAndroidHostTest : ShouldSpec() {
    init {
        should("add 1 and 2 to equal 3") {
            (1 + 2) shouldBe 3
        }
    }
}