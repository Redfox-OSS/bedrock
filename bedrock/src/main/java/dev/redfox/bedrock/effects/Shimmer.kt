package dev.redfox.bedrock.effects

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Shape
import dev.redfox.bedrock.motion.BrMotion
import dev.redfox.bedrock.theme.BedrockTheme
import dev.redfox.bedrock.theme.BrShapes

/**
 * Loading content breathes: its opacity rises and falls slowly (1.6s each way) instead of a
 * bright sweep racing across it. Calmer to wait on, and it reads the same over glass.
 */
@Composable
public fun Modifier.breathe(): Modifier {
    val alpha = rememberInfiniteTransition(label = "breathe").animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = BrMotion.Soft), RepeatMode.Reverse),
        label = "breatheAlpha",
    )
    return graphicsLayer { this.alpha = alpha.value }
}

/** A breathing stand-in for content that's loading. Size it like what it replaces. */
@Composable
public fun Placeholder(modifier: Modifier = Modifier, shape: Shape = BrShapes.XSmall) {
    Box(modifier.breathe().clip(shape).background(BedrockTheme.colors.surfaceHigh))
}
