package vn.id.tozydev.tusu.domain.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class RecentEmojisTest :
    FunSpec(
        {
            test("a used emoji moves to the front without duplicates") {
                AppSettings(recentEmojis = listOf("😀", "🌙", "☀️"))
                    .withRecentEmoji("☀️")
                    .recentEmojis shouldBe listOf("☀️", "😀", "🌙")
            }

            test("the list keeps only the most recent emojis") {
                val full = (1..MAX_RECENT_EMOJIS).map { "e$it" }
                val updated = AppSettings(recentEmojis = full).withRecentEmoji("new").recentEmojis

                updated.size shouldBe MAX_RECENT_EMOJIS
                updated.first() shouldBe "new"
                updated.last() shouldBe "e${MAX_RECENT_EMOJIS - 1}"
            }
        },
    )
