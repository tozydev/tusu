package vn.id.tozydev.tusu

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class CoreCommonTest : ShouldSpec() {
    init {
        should("say hello to Kotest") {
            sayHello("Kotest") shouldBe "Hello, Kotest!"
        }
    }
}
