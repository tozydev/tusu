package vn.id.tozydev.tusu.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith

private const val BackgroundScale = 0.94f

/** Material fade-through: the old tab fades out, then the new one fades and scales in. */
internal fun <S> fadeThroughTransition(): AnimatedContentTransitionScope<S>.() -> ContentTransform =
    {
        (fadeIn(tween(210, delayMillis = 90, easing = LinearOutSlowInEasing)) +
                scaleIn(
                    tween(300, delayMillis = 90, easing = LinearOutSlowInEasing),
                    initialScale = 0.96f,
                ))
            .togetherWith(fadeOut(tween(90, easing = FastOutLinearInEasing)))
    }

/** The composer slides up over the screen below, which recedes. */
internal fun <S> composerEnterTransition():
    AnimatedContentTransitionScope<S>.() -> ContentTransform = {
    (slideInVertically(tween(380, easing = LinearOutSlowInEasing)) { it / 5 } +
            fadeIn(tween(240, easing = LinearOutSlowInEasing)))
        .togetherWith(
            scaleOut(tween(300), targetScale = BackgroundScale) + fadeOut(tween(240)),
        )
}

/** Closing the composer slides it down and brings the screen below back to full size. */
internal fun <S> composerExitTransition():
    AnimatedContentTransitionScope<S>.() -> ContentTransform = {
    (scaleIn(tween(300), initialScale = BackgroundScale) + fadeIn(tween(240))).togetherWith(
        slideOutVertically(tween(300, easing = FastOutLinearInEasing)) { it / 5 } +
            fadeOut(tween(220)),
    )
}
