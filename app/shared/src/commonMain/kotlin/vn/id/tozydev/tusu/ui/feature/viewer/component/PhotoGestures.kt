package vn.id.tozydev.tusu.ui.feature.viewer.component

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import kotlin.math.abs

private enum class Direction {
    /** Two or more fingers: pinch to zoom, pan with the centroid. */
    Pinch,

    /** One finger on a zoomed photo: pan. */
    Pan,

    /** One finger moving mostly up or down on a photo at 1x: drag to dismiss. */
    Dismiss,
}

/**
 * Pinch, pan and drag-to-dismiss for one photo. The direction is decided once the finger moves past
 * [touchSlop]. A mostly horizontal drag at 1x is not consumed, so the pager can scroll.
 *
 * [onTransform] gets the zoom factor, the pan and the centroid, all in the photo's own coordinates.
 */
internal suspend fun PointerInputScope.detectPhotoGestures(
    touchSlop: Float,
    isZoomed: () -> Boolean,
    onTransform: (zoom: Float, pan: Offset, centroid: Offset) -> Unit,
    onDismissDrag: (dragY: Float) -> Unit,
    onDismissDragEnd: () -> Unit,
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        var direction: Direction? = null
        // A second finger can turn a dismiss drag into a pinch; the drag still has to end.
        var dismissing = false
        while (true) {
            val event = awaitPointerEvent()
            val pressed = event.changes.filter { it.pressed }
            if (pressed.isEmpty()) break
            if (pressed.size > 1) direction = Direction.Pinch

            var move = event.calculatePan()
            if (direction == null) {
                val total = pressed.single().position - down.position
                if (total.getDistance() < touchSlop) continue
                direction =
                    when {
                        isZoomed() -> Direction.Pan
                        abs(total.y) > abs(total.x) -> Direction.Dismiss
                        else -> return@awaitEachGesture
                    }
                // The movement before the decision still belongs to this gesture.
                move = total
            }

            if (direction == Direction.Dismiss) {
                dismissing = true
                onDismissDrag(move.y)
            } else {
                onTransform(event.calculateZoom(), move, event.calculateCentroid())
            }
            event.changes.forEach { it.consume() }
        }
        if (dismissing) onDismissDragEnd()
    }
}
