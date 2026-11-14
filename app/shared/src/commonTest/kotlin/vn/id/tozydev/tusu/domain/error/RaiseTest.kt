package vn.id.tozydev.tusu.domain.error

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

class RaiseTest :
    FunSpec(
        {
            test("outcome wraps the block result") {
                outcome<Int, String> { 1 + 1 } shouldBe Outcome.Success(2)
            }

            test("raise short-circuits with the error") {
                var reached = false
                val result =
                    outcome<Int, String> {
                        if (!reached) raise("boom")
                        reached = true
                        1
                    }
                result shouldBe Outcome.Failure("boom")
                reached shouldBe false
            }

            test("bind unwraps a success and raises a failure") {
                val ok: Outcome<Int, String> = Outcome.Success(2)
                val bad: Outcome<Int, String> = Outcome.Failure("boom")

                outcome<Int, String> { ok.bind() + 1 } shouldBe Outcome.Success(3)
                outcome<Int, String> { ok.bind() + bad.bind() } shouldBe Outcome.Failure("boom")
            }

            test("ensure raises when the condition is false") {
                outcome<Int, String> {
                    ensure(true) { "unused" }
                    ensure(false) { "invalid" }
                    1
                } shouldBe Outcome.Failure("invalid")
            }

            test("ensureNotNull returns the value or raises") {
                val present: Int? = 3
                val missing: Int? = null

                outcome<Int, String> { ensureNotNull(present) { "missing" } } shouldBe
                    Outcome.Success(3)
                outcome<Int, String> { ensureNotNull(missing) { "missing" } } shouldBe
                    Outcome.Failure("missing")
            }

            test("a raise is caught by the scope that owns it, not by a nested one") {
                val result =
                    outcome<Outcome<Int, Int>, String> {
                        val outer = contextOf<Raise<String>>()
                        outcome<Int, Int> { outer.raise("outer") }
                    }
                result shouldBe Outcome.Failure("outer")
            }

            test("raise works across suspension points") {
                outcome<Int, String> {
                    delay(1)
                    raise("late")
                } shouldBe Outcome.Failure("late")
            }

            test("unrelated exceptions pass through") {
                shouldThrow<IllegalStateException> { outcome<Int, String> { error("bug") } }
                shouldThrow<CancellationException> {
                    outcome<Int, String> { throw CancellationException("cancelled") }
                }
            }
        },
    )
