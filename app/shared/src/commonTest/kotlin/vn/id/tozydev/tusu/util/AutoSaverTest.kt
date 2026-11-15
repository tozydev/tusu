package vn.id.tozydev.tusu.util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import vn.id.tozydev.tusu.domain.error.Outcome

private fun ok(): Outcome<Unit, String> = Outcome.Success(Unit)

private fun TestScope.saverRecording(saved: MutableList<String>) =
    AutoSaver<String, String>(
        backgroundScope,
        save = {
            saved += it
            ok()
        },
    )

class AutoSaverTest :
    FunSpec(
        {
            test("nothing is saved before the debounce, then exactly once at 500 ms") {
                runTest {
                    val saved = mutableListOf<String>()
                    val saver = saverRecording(saved)

                    saver.submit("a")
                    runCurrent()
                    advanceTimeBy(499)
                    runCurrent()
                    saved shouldBe emptyList()

                    advanceTimeBy(1)
                    runCurrent()
                    saved shouldBe listOf("a")

                    advanceTimeBy(1_000)
                    runCurrent()
                    saved shouldBe listOf("a")
                }
            }

            test("submits inside the window collapse into one save of the last value") {
                runTest {
                    val saved = mutableListOf<String>()
                    val saver = saverRecording(saved)

                    saver.submit("a")
                    runCurrent()
                    advanceTimeBy(300)
                    runCurrent()
                    saver.submit("b")
                    runCurrent()
                    advanceTimeBy(300)
                    runCurrent()
                    saver.submit("c")
                    runCurrent()

                    advanceTimeBy(499)
                    runCurrent()
                    saved shouldBe emptyList()

                    advanceTimeBy(1)
                    runCurrent()
                    saved shouldBe listOf("c")
                }
            }

            test("flush saves the pending value now and the timer does not save it again") {
                runTest {
                    val saved = mutableListOf<String>()
                    val saver = saverRecording(saved)

                    saver.submit("a")
                    runCurrent()
                    saver.flush()
                    runCurrent()
                    saved shouldBe listOf("a")

                    advanceTimeBy(1_000)
                    runCurrent()
                    saved shouldBe listOf("a")
                    saver.state.value shouldBe SaveState.Saved
                }
            }

            test("flush with nothing pending does not call save") {
                runTest {
                    val saved = mutableListOf<String>()
                    val saver = saverRecording(saved)

                    saver.flush()
                    runCurrent()
                    saved shouldBe emptyList()

                    saver.submit("a")
                    runCurrent()
                    saver.flush()
                    runCurrent()
                    saver.flush()
                    runCurrent()
                    saved shouldBe listOf("a")
                }
            }

            test("a save never overlaps the previous one and the newest value wins") {
                runTest {
                    val gate = CompletableDeferred<Unit>()
                    val started = mutableListOf<String>()
                    val saver =
                        AutoSaver<String, String>(
                            backgroundScope,
                            save = {
                                started += it
                                if (it == "a") gate.await()
                                ok()
                            },
                        )

                    saver.submit("a")
                    runCurrent()
                    advanceTimeBy(500)
                    runCurrent()
                    started shouldBe listOf("a")
                    saver.state.value shouldBe SaveState.Saving

                    saver.submit("b")
                    runCurrent()
                    advanceTimeBy(500)
                    runCurrent()
                    started shouldBe listOf("a")

                    gate.complete(Unit)
                    runCurrent()
                    advanceTimeBy(500)
                    runCurrent()
                    started shouldBe listOf("a", "b")
                    saver.state.value shouldBe SaveState.Saved
                }
            }

            test("state goes Idle, Pending, Saving, Saved for a successful save") {
                runTest {
                    val gate = CompletableDeferred<Unit>()
                    val saver =
                        AutoSaver<String, String>(
                            backgroundScope,
                            save = {
                                gate.await()
                                ok()
                            },
                        )
                    saver.state.value shouldBe SaveState.Idle

                    saver.submit("a")
                    runCurrent()
                    saver.state.value shouldBe SaveState.Pending

                    advanceTimeBy(500)
                    runCurrent()
                    saver.state.value shouldBe SaveState.Saving

                    gate.complete(Unit)
                    runCurrent()
                    saver.state.value shouldBe SaveState.Saved
                }
            }

            test(
                "a failed save ends in Failed, retry saves the kept value, a new submit saves again",
            ) {
                runTest {
                    val saved = mutableListOf<String>()
                    val results =
                        ArrayDeque(listOf<Outcome<Unit, String>>(Outcome.Failure("boom"), ok()))
                    val saver =
                        AutoSaver<String, String>(
                            backgroundScope,
                            save = {
                                saved += it
                                results.removeFirstOrNull() ?: ok()
                            },
                        )

                    saver.submit("x")
                    runCurrent()
                    advanceTimeBy(500)
                    runCurrent()
                    saver.state.value shouldBe SaveState.Failed("boom")

                    saver.retry()
                    runCurrent()
                    saved shouldBe listOf("x", "x")
                    saver.state.value shouldBe SaveState.Saved

                    saver.retry()
                    runCurrent()
                    saved shouldBe listOf("x", "x")

                    saver.submit("y")
                    runCurrent()
                    saver.state.value shouldBe SaveState.Pending
                    advanceTimeBy(500)
                    runCurrent()
                    saved shouldBe listOf("x", "x", "y")
                    saver.state.value shouldBe SaveState.Saved
                }
            }

            test("awaitIdle flushes a pending value instead of waiting out the debounce") {
                runTest {
                    val saved = mutableListOf<String>()
                    val saver = saverRecording(saved)

                    saver.submit("a")
                    saver.awaitIdle()

                    saved shouldBe listOf("a")
                    saver.state.value shouldBe SaveState.Saved
                    testScheduler.currentTime shouldBe 0L
                }
            }

            test("awaitIdle returns at once when nothing is pending") {
                runTest {
                    val saver = saverRecording(mutableListOf())

                    saver.awaitIdle()

                    saver.state.value shouldBe SaveState.Idle
                }
            }

            test("cancelling the scope stops the saver without throwing") {
                runTest {
                    val saved = mutableListOf<String>()
                    val scope = CoroutineScope(Job() + StandardTestDispatcher(testScheduler))
                    val saver =
                        AutoSaver<String, String>(
                            scope,
                            save = {
                                saved += it
                                ok()
                            },
                        )

                    saver.submit("a")
                    runCurrent()
                    advanceTimeBy(100)
                    scope.cancel()
                    advanceUntilIdle()

                    saver.submit("b")
                    advanceUntilIdle()
                    saver.awaitIdle()

                    saved shouldBe emptyList()
                }
            }

            test("a CancellationException from save is rethrown and not turned into Failed") {
                runTest {
                    val saver =
                        AutoSaver<String, String>(
                            backgroundScope,
                            save = { throw CancellationException("save cancelled") },
                        )

                    saver.submit("a")
                    runCurrent()
                    advanceTimeBy(500)
                    runCurrent()

                    saver.state.value shouldBe SaveState.Pending
                }
            }
        },
    )
