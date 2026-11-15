package vn.id.tozydev.tusu.ui.feature.viewer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ZoomStateTest :
    FunSpec(
        {
            // Container 200 x 100, so its centre is (100, 50).
            val container = Size(200f, 100f)
            val centre = Offset(100f, 50f)

            test("scale is clamped to MinScale..MaxScale") {
                ZoomState().transformBy(100f, Offset.Zero, centre, container).scale shouldBe
                    ZoomState.MaxScale
                ZoomState(scale = 2f)
                    .transformBy(0.01f, Offset.Zero, centre, container)
                    .scale shouldBe ZoomState.MinScale
            }

            test("offset resets to zero when the scale returns to 1") {
                val zoomed = ZoomState(scale = 2f, offset = Offset(50f, 20f))

                zoomed.transformBy(0.1f, Offset.Zero, centre, container) shouldBe ZoomState()
            }

            test("offset is clamped so no empty band shows") {
                // At 2x the photo may move by width * (scale - 1) / 2 = 100 px horizontally and
                // height * (scale - 1) / 2 = 50 px vertically.
                val result =
                    ZoomState(scale = 2f).transformBy(1f, Offset(500f, -500f), centre, container)

                result shouldBe ZoomState(scale = 2f, offset = Offset(100f, -50f))
            }

            test("pan at scale 1 does nothing") {
                ZoomState().transformBy(1f, Offset(30f, 40f), Offset(10f, 10f), container) shouldBe
                    ZoomState()
            }

            test("zooming around the centre keeps the offset at zero") {
                ZoomState(scale = 2f)
                    .transformBy(1.5f, Offset.Zero, centre, container)
                    .offset shouldBe Offset.Zero
            }

            test("zooming keeps the point under the centroid in place") {
                // Local point under (120, 70) before zoom is (5, 20); after 1.5x it must stay put.
                val before = ZoomState(scale = 2f, offset = Offset(10f, -20f))

                before.transformBy(1.5f, Offset.Zero, Offset(120f, 70f), container) shouldBe
                    ZoomState(scale = 3f, offset = Offset(5f, -40f))
            }

            test("toggle zooms in to DoubleTapScale and back out to 1x") {
                val zoomedIn = ZoomState().toggle(centre, container)

                zoomedIn shouldBe ZoomState(scale = ZoomState.DoubleTapScale)
                zoomedIn.toggle(centre, container) shouldBe ZoomState()
            }

            test("toggle at a corner moves the tapped corner to the screen corner") {
                // The tapped corner (0, 0) stays at the corner: offset = (1 - r) * (corner -
                // centre).
                ZoomState().toggle(Offset.Zero, container) shouldBe
                    ZoomState(scale = 2.5f, offset = Offset(150f, 75f))
            }

            test("isZoomed is true only above 1x") {
                ZoomState().isZoomed shouldBe false
                ZoomState(scale = 1.5f).isZoomed shouldBe true
            }
        },
    )
