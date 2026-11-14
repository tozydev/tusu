package vn.id.tozydev.tusu.text

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class HashtagsTest :
    FunSpec(
        {
            test("parses tags in order, without the hash, with diacritics") {
                parseHashtags("Hôm nay #cảm_xúc và #làm_việc, #tag.")
                    .shouldContainExactly("cảm_xúc", "làm_việc", "tag")
            }

            test("duplicate tags are reported once, in first-seen order") {
                parseHashtags("#a #b #A #b").shouldContainExactly("a", "b")
            }

            test("a hash inside a word or a URL fragment is not a tag") {
                parseHashtags("abc#def").shouldBeEmpty()
                parseHashtags("site.com/#top").shouldBeEmpty()
            }

            test("a lone hash, a digit-only token and a double hash are not tags") {
                parseHashtags("# #1 #42 ##x").shouldBeEmpty()
            }

            test("tags may follow punctuation or start the text") {
                parseHashtags("(#học) \"#đi\" #start").shouldContainExactly("học", "đi", "start")
            }

            test("a tag ends at the first character that is not a letter, digit or underscore") {
                parseHashtags("#a-b #c.d #e_f").shouldContainExactly("a", "c", "e_f")
            }

            test("a token mixing letters and digits is a tag") {
                parseHashtags("#2026ghi").shouldContainExactly("2026ghi")
            }

            test("matches a multi-word label, ignoring diacritics and case") {
                matchLabelHashtags("#bài học hôm nay", listOf("Bài học")) shouldBe setOf("Bài học")
                matchLabelHashtags("#bai hoc", listOf("Bài học")) shouldBe setOf("Bài học")
                matchLabelHashtags("#BÀI HỌC hôm nay", listOf("Bài học")) shouldBe setOf("Bài học")
            }

            test("an underscore matches a space in a label") {
                matchLabelHashtags("#bài_học", listOf("Bài học")) shouldBe setOf("Bài học")
            }

            test("a match must end at a word boundary") {
                matchLabelHashtags("#bài họcc", listOf("Bài học")) shouldBe emptySet()
                matchLabelHashtags("#bài học_x", listOf("Bài học")) shouldBe emptySet()
            }

            test("the longest label that follows the hash wins") {
                matchLabelHashtags("#bài học", listOf("Bài", "Bài học")) shouldBe setOf("Bài học")
            }

            test("a label matches when punctuation follows it") {
                matchLabelHashtags("#bài.", listOf("Bài")) shouldBe setOf("Bài")
            }

            test("several labels in one text are all matched") {
                matchLabelHashtags(
                    "Ghi chú #Bài học, và #Công việc.",
                    listOf("Bài học", "Công việc", "Khác"),
                ) shouldBe setOf("Bài học", "Công việc")
            }

            test("a hash that is not at a tag start does not match") {
                matchLabelHashtags("abc#bài học", listOf("Bài học")) shouldBe emptySet()
            }

            test("no matching label yields an empty set") {
                matchLabelHashtags("#khác", listOf("Bài học")) shouldBe emptySet()
                matchLabelHashtags("không có thẻ", listOf("Bài học")) shouldBe emptySet()
            }
        },
    )
