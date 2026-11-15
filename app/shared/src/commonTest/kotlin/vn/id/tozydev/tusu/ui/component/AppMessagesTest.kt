package vn.id.tozydev.tusu.ui.component

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import vn.id.tozydev.tusu.ui.format.UiText

class AppMessagesTest :
    FunSpec(
        {
            test("messages posted before and after collection starts arrive in order") {
                runTest {
                    val appMessages = AppMessages()
                    val first = UiMessage(UiText.of("first"))
                    val second = UiMessage(UiText.of("second"))
                    val third = UiMessage(UiText.of("third"))

                    appMessages.post(first)
                    val collected = async { appMessages.messages.take(3).toList() }
                    appMessages.post(second)
                    appMessages.post(third)

                    collected.await() shouldBe listOf(first, second, third)
                }
            }
        },
    )
