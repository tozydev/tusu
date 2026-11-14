package vn.id.tozydev.tusu.text

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DiacriticFoldingTest :
    FunSpec(
        {
            // Each family is a run of precomposed Vietnamese letters that all fold to one base.
            val families =
                listOf(
                    "àáảãạ" to 'a',
                    "ăằắẳẵặ" to 'a',
                    "âầấẩẫậ" to 'a',
                    "èéẻẽẹ" to 'e',
                    "êềếểễệ" to 'e',
                    "ìíỉĩị" to 'i',
                    "òóỏõọ" to 'o',
                    "ôồốổỗộ" to 'o',
                    "ơờớởỡợ" to 'o',
                    "ùúủũụ" to 'u',
                    "ưừứửữự" to 'u',
                    "ỳýỷỹỵ" to 'y',
                    "đ" to 'd',
                )

            test("every Vietnamese vowel family folds to its base letter") {
                for ((family, base) in families) {
                    family.foldDiacritics() shouldBe base.toString().repeat(family.length)
                }
            }

            test("upper case folds the same way as lower case") {
                for ((family, base) in families) {
                    family.uppercase().foldDiacritics() shouldBe
                        base.toString().repeat(family.length)
                }
            }

            test("đ and Đ map to d") {
                "Đ".foldDiacritics() shouldBe "d"
                "đ".foldDiacritics() shouldBe "d"
            }

            test("a phrase is lower-cased and stripped") {
                "Bài học Đà Nẵng".foldDiacritics() shouldBe "bai hoc da nang"
                "HÔM NAY".foldDiacritics() shouldBe "hom nay"
            }

            test("decomposed input folds like precomposed input") {
                // NFD of ằ is a + U+0306 + U+0300; the combining marks are dropped.
                "ằ".foldDiacritics() shouldBe "a"
                "ế".foldDiacritics() shouldBe "e"
                "Hòà bành".foldDiacritics() shouldBe "hoa banh"
            }

            test("general Latin diacritics are stripped too") {
                "café naïve façade Ångström".foldDiacritics() shouldBe "cafe naive facade angstrom"
            }

            test("digits, punctuation and emoji are kept") {
                "Hà Nội 2026, ok! 😊".foldDiacritics() shouldBe "ha noi 2026, ok! 😊"
            }

            test("non-Latin text is only lower-cased") {
                "Привет Мир".foldDiacritics() shouldBe "привет мир"
                "你好".foldDiacritics() shouldBe "你好"
            }
        },
    )
