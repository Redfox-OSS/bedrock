package dev.redfox.bedrock.motion

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

/**
 * Text that changes calmly: the old value lifts away as the new one rises into place, a short
 * distance, on the house curve. For labels and status words that change in place.
 */
@Composable
public fun SwapText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = text,
        modifier = modifier,
        transitionSpec = {
            (fadeIn(tween(BrDurations.Swap, easing = BrMotion.Calm)) +
                slideInVertically(tween(BrDurations.Swap, easing = BrMotion.Calm)) { it / 3 }) togetherWith
                (fadeOut(tween(BrDurations.Fade)) + slideOutVertically(tween(BrDurations.Swap, easing = BrMotion.Calm)) { -it / 3 }) using
                SizeTransform(clip = false) { _, _ -> tween(BrDurations.Swap, easing = BrMotion.Calm) }
        },
        label = "swapText",
    ) { value ->
        Text(value, style = style, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/**
 * A number that rolls like a slow odometer: only the digits that changed move, upward when the
 * value grows and downward when it shrinks. Use with a tabular style (`figure`, `numeral`) so
 * the unchanged digits stay perfectly still.
 */
@Composable
public fun RollingNumber(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    var previous by remember { mutableStateOf(text) }
    val up = remember(text) {
        val a = previous.filter(Char::isDigit).toBigIntegerOrNull()
        val b = text.filter(Char::isDigit).toBigIntegerOrNull()
        val result = if (a != null && b != null) b >= a else true
        previous = text
        result
    }
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        // Keyed from the right so units, tens and hundreds stay in their own columns.
        val padded = text.reversed()
        for (i in padded.indices.reversed()) {
            AnimatedContent(
                targetState = padded[i],
                transitionSpec = {
                    val dir = if (up) 1 else -1
                    (fadeIn(tween(BrDurations.Swap, easing = BrMotion.Calm)) +
                        slideInVertically(tween(BrDurations.Enter, easing = BrMotion.Calm)) { dir * it / 2 }) togetherWith
                        (fadeOut(tween(BrDurations.Fade)) +
                            slideOutVertically(tween(BrDurations.Enter, easing = BrMotion.Calm)) { -dir * it / 2 }) using
                        SizeTransform(clip = false)
                },
                label = "digit",
            ) { ch ->
                Text(ch.toString(), style = style, color = color)
            }
        }
    }
}
