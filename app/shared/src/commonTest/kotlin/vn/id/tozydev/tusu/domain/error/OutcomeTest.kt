package vn.id.tozydev.tusu.domain.error

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class OutcomeTest :
    FunSpec(
        {
            val success: Outcome<Int, String> = Outcome.Success(2)
            val failure: Outcome<Int, String> = Outcome.Failure("boom")

            test("map transforms only the value") {
                success.map { it * 2 } shouldBe Outcome.Success(4)
                failure.map { it * 2 } shouldBe Outcome.Failure("boom")
            }

            test("mapError transforms only the error") {
                success.mapError { it.length } shouldBe Outcome.Success(2)
                failure.mapError { it.length } shouldBe Outcome.Failure(4)
            }

            test("fold picks the matching branch") {
                success.fold(onSuccess = { "value $it" }, onFailure = { "error $it" }) shouldBe
                    "value 2"
                failure.fold(onSuccess = { "value $it" }, onFailure = { "error $it" }) shouldBe
                    "error boom"
            }

            test("getOrNull and errorOrNull expose one side") {
                success.getOrNull() shouldBe 2
                success.errorOrNull() shouldBe null
                failure.getOrNull() shouldBe null
                failure.errorOrNull() shouldBe "boom"
            }

            test("onSuccess and onFailure run only for their side") {
                val seen = mutableListOf<String>()
                success.onSuccess { seen += "s$it" }.onFailure { seen += "f$it" }
                failure.onSuccess { seen += "s$it" }.onFailure { seen += "f$it" }
                seen shouldBe listOf("s2", "fboom")
            }
        },
    )
