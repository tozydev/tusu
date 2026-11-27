package vn.id.tozydev.tusu.util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.floats.shouldBeBetween
import io.kotest.matchers.shouldBe

class AudioLevelsTest :
    FunSpec(
        {
            test("silence is 0 and full scale is 1") {
                normalizedLevel(0) shouldBe 0f
                normalizedLevel(MAX_AMPLITUDE) shouldBe 1f
            }

            test("levels follow a decibel scale, so quiet speech is still visible") {
                normalizedLevel(MAX_AMPLITUDE / 100).shouldBeBetween(0.3f, 0.4f, 0.001f)
            }

            test("amplitudes outside the 16-bit range are clamped") {
                normalizedLevel(-5) shouldBe 0f
                normalizedLevel(MAX_AMPLITUDE * 2) shouldBe 1f
            }

            test("downsampling keeps the loudest level of each bucket") {
                listOf(0.1f, 0.5f, 0.2f, 0.9f, 0.3f, 0.4f).downsampleLevels(3) shouldContainExactly
                    listOf(0.5f, 0.9f, 0.4f)
            }

            test("a recording shorter than the bar count keeps every level") {
                listOf(0.2f, 0.7f).downsampleLevels(48) shouldContainExactly listOf(0.2f, 0.7f)
            }

            test("uneven buckets still cover every level") {
                val levels = List(10) { it / 10f }
                val bars = levels.downsampleLevels(4)
                bars.size shouldBe 4
                bars.last() shouldBe 0.9f
            }

            test("no levels give no bars") {
                emptyList<Float>().downsampleLevels(48).shouldBeEmpty()
            }
        },
    )
