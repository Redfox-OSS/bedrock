package dev.redfox.bedrock.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/**
 * Bedrock's motion vocabulary. One rule above all: **nothing overshoots.** Every spring is
 * critically damped (damping ratio 1), so things arrive and rest instead of bouncing, and every
 * curve decelerates into place. Springs are still used for anything a finger can interrupt,
 * because they pick up from the current velocity without a jolt.
 *
 * | Spec | Stiffness | For |
 * |---|---|---|
 * | [quick] | 700 | press feedback, toggles, colour changes |
 * | [settle] | 320 | indicators, pills, layout changes |
 * | [glide] | 170 | sheets, pages, anything large crossing the screen |
 */
public object BrMotion {
    /** Small, direct state changes: a press, a switch thumb. */
    public fun <T> quick(visibilityThreshold: T? = null): SpringSpec<T> =
        spring(dampingRatio = 1f, stiffness = 700f, visibilityThreshold = visibilityThreshold)

    /** Indicators and layout: a selection pill gliding to its new home. */
    public fun <T> settle(visibilityThreshold: T? = null): SpringSpec<T> =
        spring(dampingRatio = 1f, stiffness = 320f, visibilityThreshold = visibilityThreshold)

    /** Large surfaces: sheets, pushed pages. Unhurried, never sluggish. */
    public fun <T> glide(visibilityThreshold: T? = null): SpringSpec<T> =
        spring(dampingRatio = 1f, stiffness = 170f, visibilityThreshold = visibilityThreshold)

    /**
     * The house curve for timed motion: a long, soft deceleration (an ease-out quint). Things
     * arrive quickly and spend most of their time settling, which reads as calm.
     */
    public val Calm: Easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

    /** A symmetric ease for things that both start and stop on screen (a looping drift). */
    public val Soft: Easing = CubicBezierEasing(0.45f, 0f, 0.25f, 1f)

    /** Delay between staggered siblings. */
    public const val StaggerMillis: Int = 38

    /** Siblings after this index enter together, so long lists never wait on their tail. */
    public const val MaxStaggerIndex: Int = 8
}

/** Durations for timed motion: fades and choreographed entrances. Anything that moves is a spring. */
public object BrDurations {
    /** Something leaving. Short, so it's gone before the newcomer settles. */
    public const val Fade: Int = 150
    /** Swapping a label, an icon, a number. */
    public const val Swap: Int = 230
    /** Something arriving: a screen, a revealed row. */
    public const val Enter: Int = 360
    /** Quiet background fades that shouldn't draw the eye. */
    public const val Linger: Int = 520
    /** One lap of an ambient loop (aura drift). Slow enough to be felt rather than seen. */
    public const val Ambient: Int = 16000
}
