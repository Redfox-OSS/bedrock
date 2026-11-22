package dev.redfox.bedrock.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.dp
import dev.redfox.bedrock.motion.BrMotion
import dev.redfox.bedrock.theme.BedrockTheme
import kotlin.math.roundToInt

/** Slider looks. */
public enum class SliderStyle {
    /** A hairline track with a round thumb, for precise values inside forms. */
    Thin,
    /** A full-height pill that fills with colour, for levels you set by feel (volume, warmth). */
    Thick,
}

/**
 * A slider you can tap or drag. The fill follows the finger on a quick, critically damped spring,
 * so a tap far along the track glides there instead of jumping. With [steps] the value snaps and
 * each step gives a light tick.
 */
@Composable
public fun BrSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    style: SliderStyle = SliderStyle.Thin,
    icon: Painter? = null,
    onValueChangeFinished: () -> Unit = {},
) {
    val colors = BedrockTheme.colors
    val haptics = LocalHapticFeedback.current
    val onChange by rememberUpdatedState(onValueChange)
    val onFinished by rememberUpdatedState(onValueChangeFinished)
    val range by rememberUpdatedState(valueRange)
    val stepCount by rememberUpdatedState(steps)
    var dragging by remember { mutableStateOf(false) }

    val span = (valueRange.endInclusive - valueRange.start).takeIf { it > 0f } ?: 1f
    val fraction = ((value - valueRange.start) / span).coerceIn(0f, 1f)
    val shown = animateFloatAsState(fraction, BrMotion.quick(), label = "sliderFill")
    val hold = animateFloatAsState(if (dragging) 1f else 0f, BrMotion.quick(), label = "sliderHold")

    val track = colors.surfaceHigh
    val fill = colors.accentMain
    val thumb = colors.ink

    fun valueAt(x: Float, width: Float, inset: Float): Float {
        val usable = (width - inset * 2).coerceAtLeast(1f)
        var f = ((x - inset) / usable).coerceIn(0f, 1f)
        if (stepCount > 0) {
            val segments = stepCount + 1
            f = (f * segments).roundToInt() / segments.toFloat()
        }
        return range.start + f * (range.endInclusive - range.start)
    }

    val height = if (style == SliderStyle.Thick) 44.dp else 32.dp
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .semantics {
                progressBarRangeInfo = ProgressBarRangeInfo(value, valueRange, steps)
                setProgress { target -> onChange(target.coerceIn(range.start, range.endInclusive)); true }
            }
            .pointerInput(Unit) {
                val inset = if (style == SliderStyle.Thin) 12.dp.toPx() else 0f
                awaitEachGesture {
                    val down = awaitFirstDown()
                    dragging = true
                    var last = valueAt(down.position.x, size.width.toFloat(), inset)
                    onChange(last)
                    drag(down.id) { change ->
                        val next = valueAt(change.position.x, size.width.toFloat(), inset)
                        if (next != last) {
                            if (stepCount > 0) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            last = next
                            onChange(next)
                        }
                        change.consume()
                    }
                    dragging = false
                    onFinished()
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        if (style == SliderStyle.Thick) {
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .graphicsLayer {
                        val s = 1f + 0.03f * hold.value
                        scaleY = s
                        scaleX = 1f + 0.01f * hold.value
                    }
            ) {
                val r = CornerRadius(size.height / 2)
                drawRoundRect(track, cornerRadius = r)
                val w = (size.width * shown.value).coerceAtLeast(size.height)
                drawRoundRect(fill, size = Size(w, size.height), cornerRadius = r)
            }
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (fraction > 0.12f) colors.onAccent else colors.textSecondary,
                    modifier = Modifier.padding(start = 14.dp).size(18.dp),
                )
            }
        } else {
            Canvas(Modifier.fillMaxWidth().height(32.dp)) {
                val inset = 12.dp.toPx()
                val y = size.height / 2
                val trackH = 4.dp.toPx()
                val usable = size.width - inset * 2
                val x = inset + usable * shown.value
                drawRoundRect(track, Offset(inset, y - trackH / 2), Size(usable, trackH), CornerRadius(trackH / 2))
                drawRoundRect(fill, Offset(inset, y - trackH / 2), Size(x - inset, trackH), CornerRadius(trackH / 2))
                val radius = (11.dp.toPx() + 2.dp.toPx() * hold.value)
                // A soft, neutral shadow under the thumb; never a glow.
                drawCircle(Color.Black.copy(alpha = 0.28f), radius + 1.5.dp.toPx(), Offset(x, y + 1.5.dp.toPx()))
                drawCircle(thumb, radius, Offset(x, y))
            }
        }
    }
}
