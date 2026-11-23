package dev.redfox.bedrock.effects

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import dev.redfox.bedrock.motion.BrMotion
import dev.redfox.bedrock.theme.BedrockTheme

// ---------------------------------------------------------------------------------------------
// Glass
// ---------------------------------------------------------------------------------------------

/**
 * The [HazeState] the whole activity shares. Provide one (`rememberHazeState()`) at the root;
 * scrolling content is marked with [glassSource] and floating surfaces draw it frosted with
 * [glass]. Without a provider, [glassSource] does nothing and [glass] falls back to a solid tint.
 */
public val LocalHazeState: ProvidableCompositionLocal<HazeState?> = staticCompositionLocalOf<HazeState?> { null }

/** Marks this layout as the backdrop that [glass] surfaces blur. */
@Composable
public fun Modifier.glassSource(): Modifier {
    val state = LocalHazeState.current ?: return this
    return hazeSource(state)
}

/**
 * Frosted glass in [shape]: the content behind is blurred (a wide, soft 28dp), lightly grained so
 * gradients never band, and washed with [tint]. With [rim] (the default) the edge catches light:
 * a hairline that is brighter along the top and fades toward the bottom, the way real glass
 * reads under a ceiling light. That rim is what makes Bedrock glass look made, not filtered.
 *
 * A glass surface must be a sibling of the [glassSource] content, never inside it. Without a
 * [LocalHazeState] (or inside another window, such as a dialog) it falls back to [tint] alone:
 * the same colour, without the blur.
 */
@Composable
public fun Modifier.glass(
    shape: Shape,
    tint: Color = BedrockTheme.colors.glass,
    blurRadius: Dp = 28.dp,
    rim: Boolean = true,
): Modifier {
    val colors = BedrockTheme.colors
    val state = LocalHazeState.current
    val frosted = if (state == null) {
        // No blur available (no provider, or another window): the tint alone. Over soft content
        // (an aura, a dimmed page) that reads almost exactly like the frosted surface.
        clip(shape).background(tint)
    } else {
        clip(shape).hazeEffect(
            state = state,
            style = HazeStyle(
                backgroundColor = colors.background,
                tints = listOf(HazeTint(tint)),
                blurRadius = blurRadius,
                noiseFactor = 0.05f,
            ),
        )
    }
    return if (rim) frosted.rim(shape) else frosted
}

/** The lit hairline edge on its own, for solid surfaces that should match glass ones. */
@Composable
public fun Modifier.rim(shape: Shape, width: Dp = 1.dp): Modifier {
    val colors = BedrockTheme.colors
    val brush = remember(colors) { Brush.verticalGradient(listOf(colors.rimLight, colors.rimShade)) }
    return border(BorderStroke(width, brush), shape)
}

// ---------------------------------------------------------------------------------------------
// Elevation
// ---------------------------------------------------------------------------------------------

/**
 * The only shadow in Bedrock: a wide, soft, neutral shadow under things that float above the
 * page (the nav bar, a toast, a menu). Never coloured: colour belongs inside surfaces. Apply
 * before `clip`.
 */
public fun Modifier.floatShadow(shape: Shape, radius: Dp = 28.dp, offsetY: Dp = 10.dp, alpha: Float = 0.42f): Modifier =
    dropShadow(
        shape = shape,
        shadow = Shadow(radius = radius, color = Color.Black.copy(alpha = alpha), offset = DpOffset(0.dp, offsetY)),
    )

// ---------------------------------------------------------------------------------------------
// Press
// ---------------------------------------------------------------------------------------------

/**
 * Bedrock's touch response, replacing ripples: the element eases down to [pressedScale] and
 * dims slightly while held, then eases back. Critically damped both ways, so it never springs
 * past its resting size. [haptic] ticks on click; a long click always gives the long-press haptic.
 *
 * Typical scales: rows 0.99, cards 0.98, buttons 0.97, chips and icon buttons 0.94.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun Modifier.pressable(
    enabled: Boolean = true,
    role: Role? = Role.Button,
    pressedScale: Float = 0.97f,
    haptic: HapticFeedbackType? = null,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current
    val down = pressed && enabled
    val progress by animateFloatAsState(
        targetValue = if (down) 1f else 0f,
        animationSpec = BrMotion.quick(),
        label = "press",
    )
    return this
        .graphicsLayer {
            val s = 1f - (1f - pressedScale) * progress
            scaleX = s
            scaleY = s
            alpha = 1f - 0.12f * progress
        }
        .combinedClickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            role = role,
            onLongClick = onLongClick?.let { longClick ->
                {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    longClick()
                }
            },
            onClick = {
                haptic?.let(haptics::performHapticFeedback)
                onClick()
            },
        )
}

// ---------------------------------------------------------------------------------------------
// Local backdrop
// ---------------------------------------------------------------------------------------------

/**
 * A self-contained stage for glass over something specific: a photo, an aura, a map. The
 * [backdrop] is blurred by any [glass] surface in [content], independently of the page's own
 * glass. Use it when the glass sits *inside* scrolling content (a frosted card over a photo in
 * a feed), where the page-level [glassSource] can't be used.
 */
@Composable
public fun GlassBackdrop(
    modifier: Modifier = Modifier,
    backdrop: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val state = rememberHazeState()
    Box(modifier) {
        Box(Modifier.matchParentSize().hazeSource(state), content = backdrop)
        CompositionLocalProvider(LocalHazeState provides state) {
            content()
        }
    }
}
