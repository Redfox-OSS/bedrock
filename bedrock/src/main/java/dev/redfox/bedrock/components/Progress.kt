package dev.redfox.bedrock.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.redfox.bedrock.motion.BrMotion
import dev.redfox.bedrock.theme.BedrockTheme

/** A slim progress bar. Changes in [progress] glide rather than step. */
@Composable
public fun BrProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = BedrockTheme.colors.accentMain,
    trackColor: Color = BedrockTheme.colors.surfaceHigh,
    height: Dp = 6.dp,
) {
    val shown = animateFloatAsState(progress.coerceIn(0f, 1f), BrMotion.glide(), label = "progress")
    Canvas(
        modifier
            .fillMaxWidth()
            .height(height)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f) }
    ) {
        val r = CornerRadius(size.height / 2)
        drawRoundRect(trackColor, cornerRadius = r)
        val w = size.width * shown.value
        if (w > 0f) drawRoundRect(color, size = Size(w.coerceAtLeast(size.height), size.height), cornerRadius = r)
    }
}

/**
 * A progress ring with room for a label in the middle (a percentage, a count). The arc glides to
 * each new value and has rounded ends.
 */
@Composable
public fun BrProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    strokeWidth: Dp = 6.dp,
    color: Color = BedrockTheme.colors.accentMain,
    trackColor: Color = BedrockTheme.colors.surfaceHigh,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val shown = animateFloatAsState(progress.coerceIn(0f, 1f), BrMotion.glide(), label = "ring")
    Box(
        modifier
            .size(size)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f) },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            drawArc(trackColor, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            drawArc(color, -90f, 360f * shown.value, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        content()
    }
}

/**
 * For waits of unknown length: an arc with a fading tail, turning slowly (one turn every 1.1s).
 * It never pulses or changes length, so it settles the eye instead of agitating it.
 */
@Composable
public fun BrSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    strokeWidth: Dp = 2.5.dp,
    color: Color = BedrockTheme.colors.accentMain,
) {
    val turn = rememberInfiniteTransition(label = "spinner").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
        label = "spin",
    )
    Canvas(modifier.size(size)) {
        val stroke = strokeWidth.toPx()
        val inset = stroke / 2
        val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
        val tail = Brush.sweepGradient(
            0f to color.copy(alpha = 0f),
            0.75f to color,
            1f to color,
            center = center,
        )
        rotate(turn.value) {
            drawArc(tail, 0f, 270f, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        }
    }
}
