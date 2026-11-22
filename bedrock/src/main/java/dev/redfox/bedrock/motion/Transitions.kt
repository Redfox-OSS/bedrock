package dev.redfox.bedrock.motion

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntOffset

/**
 * Screen-to-screen motion. The incoming screen glides a short way while it fades in; the outgoing
 * one drifts a little the other way as it fades, a soft parallax that tells you which way you
 * went without making you watch a slide. Movement is a critically damped spring, so predictive
 * back scrubs it and a quick tap-back reverses it smoothly.
 *
 * Each is a [ContentTransform] for Navigation 3's `NavDisplay`, Navigation Compose, or any
 * `AnimatedContent`.
 */
public object BrTransitions {
    /** The incoming screen travels 1/14 of the width. */
    private const val Travel = 14
    /** The outgoing screen drifts 1/40 the other way. */
    private const val Parallax = 40
    /** Tabs are siblings: a smaller drift. */
    private const val TabTravel = 32

    private val move = BrMotion.glide(IntOffset.VisibilityThreshold)

    /** Going deeper. */
    public fun push(): ContentTransform =
        (slideInHorizontally(move) { it / Travel } + fadeIn(tween(BrDurations.Enter, easing = BrMotion.Calm))) togetherWith
            (slideOutHorizontally(move) { -it / Parallax } + fadeOut(tween(BrDurations.Fade)))

    /** Going back. */
    public fun pop(): ContentTransform =
        (slideInHorizontally(move) { -it / Parallax } + fadeIn(tween(BrDurations.Enter, easing = BrMotion.Calm))) togetherWith
            (slideOutHorizontally(move) { it / Travel } + fadeOut(tween(BrDurations.Swap)))

    /** The back gesture, scrubbed by the finger: the same motion as [pop]. */
    public fun predictivePop(): ContentTransform = pop()

    /** Switching tabs; [forward] means the new tab sits to the right of the old one. */
    public fun tab(forward: Boolean): ContentTransform {
        val sign = if (forward) 1 else -1
        return (slideInHorizontally(move) { sign * it / TabTravel } + fadeIn(tween(BrDurations.Enter, easing = BrMotion.Calm))) togetherWith
            fadeOut(tween(BrDurations.Fade))
    }

    /** Shared-element bounds (a card opening into a page): a slow, soft glide with no overshoot. */
    @ExperimentalSharedTransitionApi
    public val sharedBounds: BoundsTransform = BoundsTransform { _, _ ->
        BrMotion.glide(Rect.VisibilityThreshold)
    }
}
