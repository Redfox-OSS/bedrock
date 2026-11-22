package dev.redfox.bedrock.effects

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import dev.redfox.bedrock.motion.BrDurations
import dev.redfox.bedrock.theme.BedrockAccent
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * One soft light inside an aura. [x] and [y] place its centre as fractions of the surface;
 * [radius] is a fraction of the surface's longer side.
 */
@Immutable
public data class AuraLight(
    val color: Color,
    val x: Float,
    val y: Float,
    val radius: Float,
)

/**
 * A field of colour: a [base] tone with a few soft lights blended over it, like light falling on a
 * coloured wall. This is where Bedrock puts its colour: *inside* surfaces, never as a glow
 * around them. [shade] darkens the bottom so text laid there stays readable.
 */
@Immutable
public data class Aura(
    val base: Color,
    val lights: List<AuraLight>,
    val shade: Float = 0.28f,
) {
    public companion object {
        /** The standard aura for an accent: its deep tone, lit by its main hue and its partner. */
        public fun of(accent: BedrockAccent): Aura = Aura(
            base = accent.deep,
            lights = listOf(
                AuraLight(accent.main, x = 0.1f, y = 0.05f, radius = 0.95f),
                AuraLight(accent.partner.copy(alpha = 0.9f), x = 0.95f, y = 0.9f, radius = 0.85f),
                AuraLight(accent.soft.copy(alpha = 0.55f), x = 0.72f, y = 0.18f, radius = 0.38f),
            ),
        )

        /** Two accents meeting: for a surface that belongs to two things at once. */
        public fun blend(a: BedrockAccent, b: BedrockAccent): Aura = Aura(
            base = a.deep,
            lights = listOf(
                AuraLight(a.main, x = 0.05f, y = 0.1f, radius = 0.9f),
                AuraLight(b.main, x = 1f, y = 0.85f, radius = 0.9f),
                AuraLight(b.soft.copy(alpha = 0.45f), x = 0.6f, y = 0.35f, radius = 0.35f),
            ),
        )
    }
}

/**
 * Paints [aura] behind this element's content. Clip to the surface's shape first. With [drift]
 * the lights wander very slowly (one lap every 16 seconds, a few percent of the surface), which
 * keeps a hero surface alive without ever catching the eye. The drift is read in the draw phase,
 * so it never recomposes, and it ticks at about 20fps: it moves too slowly for that to show.
 */
@Composable
public fun Modifier.aura(aura: Aura, drift: Boolean = false): Modifier {
    if (!drift) return drawBehind { drawAura(aura, 0f) }
    // The lights move well under a pixel per frame, so ~20 updates a second look identical to
    // 60 while redrawing (and re-blurring any glass above) a third as often.
    val phase = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = SystemClock.uptimeMillis()
        while (isActive) {
            phase.floatValue = ((SystemClock.uptimeMillis() - start) % BrDurations.Ambient) / BrDurations.Ambient.toFloat()
            delay(DriftFrameMillis)
        }
    }
    return drawAuraWithPhase(aura, phase)
}

private const val DriftFrameMillis = 48L

private fun Modifier.drawAuraWithPhase(aura: Aura, phase: State<Float>): Modifier =
    drawBehind { drawAura(aura, phase.value) }

private fun DrawScope.drawAura(aura: Aura, phase: Float) {
    drawRect(aura.base)
    val span = max(size.width, size.height)
    val angle = phase * 2f * PI.toFloat()
    aura.lights.forEachIndexed { i, light ->
        // Each light circles its home on its own offset, so they never move in step.
        val a = angle + i * 2.1f
        val wobble = 0.06f
        val center = Offset(
            x = (light.x + cos(a) * wobble) * size.width,
            y = (light.y + sin(a) * wobble) * size.height,
        )
        val radius = light.radius * span
        drawRect(
            Brush.radialGradient(
                0f to light.color,
                0.45f to light.color.copy(alpha = light.color.alpha * 0.45f),
                1f to light.color.copy(alpha = 0f),
                center = center,
                radius = radius,
            ),
        )
    }
    if (aura.shade > 0f) {
        drawRect(
            Brush.verticalGradient(
                0.45f to Color.Transparent,
                1f to Color.Black.copy(alpha = aura.shade),
            ),
        )
    }
}

/** An [Aura] built once per accent. */
@Composable
public fun rememberAura(accent: BedrockAccent): Aura = remember(accent) { Aura.of(accent) }
