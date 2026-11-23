package dev.redfox.bedrock.motion

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
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

/**
 * A `Column` whose children enter one after another, like [revealOnEnter] without numbering each
 * child: forms and settings pages. Each child rises [offsetY] while fading in on the house curve,
 * a beat behind the one above. Children stack top-down with the arrangement's spacing. Inside a [RevealScope] it only plays while the screen is new.
 * Everything runs in placement, so it never recomposes the children.
 */
@Composable
public fun RevealColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    offsetY: Dp = 14.dp,
    content: @Composable () -> Unit,
) {
    val clock = LocalRevealClock.current
    val play = remember { clock?.fresh() ?: true }
    val total = (BrMotion.StaggerMillis * BrMotion.MaxStaggerIndex + BrDurations.Enter).toFloat()
    val elapsed = remember { Animatable(if (play) 0f else total) }
    LaunchedEffect(Unit) {
        if (play) elapsed.animateTo(total, tween(total.toInt(), easing = LinearEasing))
    }
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val loose = constraints.copy(minHeight = 0)
        val placeables = measurables.map { it.measure(loose) }
        // Stacked top-down with the arrangement's spacing (Top or spacedBy), like a wrapping Column.
        val gap = verticalArrangement.spacing.roundToPx()
        val positions = IntArray(placeables.size)
        var y = 0
        placeables.forEachIndexed { i, p ->
            positions[i] = y
            y += p.height + if (i < placeables.lastIndex) gap else 0
        }
        val contentHeight = y
        val width = (placeables.maxOfOrNull { it.width } ?: 0).coerceIn(constraints.minWidth, constraints.maxWidth)
        val height = contentHeight.coerceIn(constraints.minHeight, constraints.maxHeight)
        val shift = offsetY.toPx()
        layout(width, height) {
            placeables.forEachIndexed { i, p ->
                val x = horizontalAlignment.align(p.width, width, layoutDirection)
                p.placeWithLayer(x, positions[i]) {
                    val start = i.coerceAtMost(BrMotion.MaxStaggerIndex) * BrMotion.StaggerMillis
                    val t = ((elapsed.value - start) / BrDurations.Enter).coerceIn(0f, 1f)
                    val e = BrMotion.Calm.transform(t)
                    alpha = e
                    translationY = (1f - e) * shift
                }
            }
        }
    }
}
