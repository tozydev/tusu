package vn.id.tozydev.tusu.ui.feature.timeline.component

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PhotoGridLayoutTest :
    FunSpec(
        {
            test("one photo is one cell in one row") { photoGridRows(1) shouldBe listOf(1) }

            test("two and three photos are a single row of that many cells") {
                photoGridRows(2) shouldBe listOf(2)
                photoGridRows(3) shouldBe listOf(3)
            }

            test("four photos are a 2 by 2 grid") { photoGridRows(4) shouldBe listOf(2, 2) }

            test(
                "from five photos the grid still shows four cells, the last one counting the rest",
            ) {
                photoGridRows(5) shouldBe listOf(2, 2)
                photoGridRows(7) shouldBe listOf(2, 2)
            }

            test("no photo shows no grid") { photoGridRows(0) shouldBe emptyList() }
        },
    )
