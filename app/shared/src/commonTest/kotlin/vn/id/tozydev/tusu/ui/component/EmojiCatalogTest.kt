package vn.id.tozydev.tusu.ui.component

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe

class EmojiCatalogTest :
    FunSpec(
        {
            test("search ignores case and Vietnamese diacritics") {
                searchEmoji("CƯỜI") shouldContain "😀"
                searchEmoji("cuoi") shouldContain "😀"
                searchEmoji("mưa") shouldContain "🌧️"
            }

            test("search also matches English keywords") {
                searchEmoji("rain") shouldContain "🌧️"
                searchEmoji("rain") shouldNotContain "😀"
            }

            test("a blank query finds nothing, so the sheet shows the groups") {
                searchEmoji("   ").shouldBeEmpty()
            }

            test("every catalog emoji belongs to exactly one group and has keywords") {
                val all = EmojiGroups.flatMap { group -> group.emojis.map { it.emoji } }
                all.size shouldBe all.toSet().size
                EmojiGroups.flatMap { it.emojis }.forEach { it.keywords.isNotBlank() shouldBe true }
            }
        },
    )
