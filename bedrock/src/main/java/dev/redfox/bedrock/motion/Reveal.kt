package dev.redfox.bedrock.motion

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** When a screen arrived, so its items know whether they're part of the entrance. */
@Stable
internal class RevealClock(val enabled: Boolean) {
    private val start = SystemClock.uptimeMillis()
    fun fresh(): Boolean = enabled && SystemClock.uptimeMillis() - start < FreshWindowMillis
}

private const val FreshWindowMillis = 700L

private val LocalRevealClock = staticCompositionLocalOf<RevealClock?> { null }

/**
 * Wrap each screen in a RevealScope. Items using [revealOnEnter] animate only while the screen is
 * new (its first 700ms), so scrolling a lazy list never replays an entrance. Pass
 * `enabled = false` when a screen is uncovered by going back: it was already seen.
 */
@Composable
public fun RevealScope(enabled: Boolean = true, content: @Composable () -> Unit) {
    val clock = remember { RevealClock(enabled) }
    CompositionLocalProvider(LocalRevealClock provides clock, content = content)
}

/**
 * The Bedrock entrance: the item rises [offsetY] while it fades in, on the [BrMotion.Calm]
 * curve, a beat after the sibling before it ([index]). No scale, no overshoot: content should
 * look like it was always there and has just come into focus.
 */
@Composable
public fun Modifier.revealOnEnter(index: Int = 0, offsetY: Dp = 14.dp): Modifier {
    val clock = LocalRevealClock.current
    val play = remember { clock?.fresh() ?: false }
    if (!play) return this
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            1f,
            tween(
                durationMillis = BrDurations.Enter,
                delayMillis = index.coerceIn(0, BrMotion.MaxStaggerIndex) * BrMotion.StaggerMillis,
                easing = BrMotion.Calm,
            ),
        )
    }
    return graphicsLayer {
        val p = progress.value
        alpha = p
        translationY = (1f - p) * offsetY.toPx()
    }
}
