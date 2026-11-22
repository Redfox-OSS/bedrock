package dev.redfox.bedrock.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import dev.redfox.bedrock.effects.floatShadow
import dev.redfox.bedrock.effects.pressable
import dev.redfox.bedrock.effects.rim
import dev.redfox.bedrock.motion.BrMotion
import dev.redfox.bedrock.theme.BedrockTheme
import kotlin.math.roundToInt

/**
 * An on/off switch. The thumb glides across on a settling spring and widens a little while your
 * finger is on it, so it feels like something you're holding. The track fills with the accent.
 */
@Composable
public fun BrSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = BedrockTheme.colors
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val position = animateFloatAsState(if (checked) 1f else 0f, BrMotion.settle(), label = "switchPos")
    val stretch = animateFloatAsState(if (pressed && enabled) 1f else 0f, BrMotion.quick(), label = "switchHold")
    val track by animateColorAsState(if (checked) colors.accentMain else colors.surfaceHigh, BrMotion.settle(), label = "switchTrack")

    Box(
        modifier
            .size(TrackWidth, TrackHeight)
            .graphicsLayer { alpha = if (enabled) 1f else 0.4f }
            .toggleable(
                value = checked,
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = {
                    haptics.performHapticFeedback(if (it) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
                    onCheckedChange(it)
                },
            )
            .clip(CircleShape)
            .background(track)
            .padding(ThumbInset),
    ) {
        Box(
            Modifier
                .layout { measurable, constraints ->
                    // Width and position in layout, so the glide never recomposes.
                    val base = ThumbSize.roundToPx()
                    val width = (base + (ThumbStretch.toPx() * stretch.value)).roundToInt()
                    val placeable = measurable.measure(Constraints.fixed(width, base))
                    layout(constraints.maxWidth, base) {
                        val travel = constraints.maxWidth - width
                        placeable.place((travel * position.value).roundToInt(), 0)
                    }
                }
                .floatShadow(CircleShape, radius = 4.dp, offsetY = 1.dp, alpha = 0.3f)
                .clip(CircleShape)
                .background(colors.ink)
        )
    }
}

private val TrackWidth = 50.dp
private val TrackHeight = 30.dp
private val ThumbInset = 2.dp
private val ThumbSize = 26.dp
private val ThumbStretch = 6.dp

/**
 * A checkbox (32dp, so it sits in a list row without stretching it; make the whole row
 * toggle it for a full-width target). When checked, the box fills with the accent and the tick draws itself in one
 * smooth stroke rather than popping in.
 */
@Composable
public fun BrCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = BedrockTheme.colors
    val haptics = LocalHapticFeedback.current
    val fill = animateFloatAsState(if (checked) 1f else 0f, BrMotion.quick(), label = "checkFill")
    val tick = remember { Animatable(if (checked) 1f else 0f) }
    LaunchedEffect(checked) {
        if (checked) tick.animateTo(1f, BrMotion.settle()) else tick.snapTo(0f)
    }
    val accent = colors.accentMain
    val outline = colors.textTertiary
    val mark = colors.onAccent
    val path = remember { Path() }
    val segment = remember { Path() }
    val measure = remember { PathMeasure() }
    Canvas(
        modifier
            .size(32.dp)
            .graphicsLayer { alpha = if (enabled) 1f else 0.4f }
            .toggleable(
                value = checked,
                interactionSource = null,
                indication = null,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = {
                    haptics.performHapticFeedback(if (it) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
                    onCheckedChange(it)
                },
            )
            .padding(5.dp)
    ) {
        val r = 7.dp.toPx()
        val f = fill.value
        // Outline fades out as the fill fades in, so there's never a double edge.
        drawRoundRect(
            color = outline.copy(alpha = outline.alpha * (1f - f)),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(r),
            style = Stroke(1.5.dp.toPx()),
        )
        drawRoundRect(color = accent.copy(alpha = f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(r))
        if (tick.value > 0f) {
            val s = size.width / 22f
            path.reset()
            path.moveTo(6f * s, 11.5f * s)
            path.lineTo(9.6f * s, 15f * s)
            path.lineTo(16f * s, 7.5f * s)
            measure.setPath(path, false)
            segment.reset()
            measure.getSegment(0f, measure.length * tick.value, segment, true)
            drawPath(segment, mark, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

/** A radio button: the ring takes the accent and a dot grows smoothly into its centre. */
@Composable
public fun BrRadio(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = BedrockTheme.colors
    val haptics = LocalHapticFeedback.current
    val dot = animateFloatAsState(if (selected) 1f else 0f, BrMotion.settle(), label = "radioDot")
    val ring by animateColorAsState(if (selected) colors.accentMain else colors.textTertiary, BrMotion.quick(), label = "radioRing")
    val accent = colors.accentMain
    Canvas(
        modifier
            .size(32.dp)
            .graphicsLayer { alpha = if (enabled) 1f else 0.4f }
            .selectable(
                selected = selected,
                interactionSource = null,
                indication = null,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = {
                    if (!selected) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    onClick()
                },
            )
            .padding(5.dp)
    ) {
        val radius = size.minDimension / 2f
        drawCircle(ring, radius - 0.75.dp.toPx(), style = Stroke(1.5.dp.toPx()))
        if (dot.value > 0f) drawCircle(accent, radius * 0.46f * dot.value, center = Offset(size.width / 2, size.height / 2))
    }
}

/**
 * A segmented control: options side by side on a recessed track, with a lighter pill that glides
 * under the chosen one. The pill never stretches or bounces; it simply moves and settles.
 */
@Composable
public fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BedrockTheme.colors
    val type = BedrockTheme.typography
    val position = animateFloatAsState(selectedIndex.toFloat(), BrMotion.settle(), label = "segment")
    val shape = RoundedCornerShape(50)
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(shape)
            .background(colors.surfaceRaised)
            .border(1.dp, colors.line, shape)
            .padding(3.dp)
    ) {
        val count = options.size.coerceAtLeast(1)
        val segment = maxWidth / count
        Box(
            Modifier
                .width(segment)
                .fillMaxHeight()
                .graphicsLayer { translationX = segment.toPx() * position.value }
                .floatShadow(shape, radius = 6.dp, offsetY = 2.dp, alpha = 0.25f)
                .clip(shape)
                .background(colors.surfaceHigh)
                .rim(shape)
        )
        Row(Modifier.fillMaxWidth().fillMaxHeight()) {
            options.forEachIndexed { index, option ->
                val selected = index == selectedIndex
                val tint by animateColorAsState(
                    if (selected) colors.textPrimary else colors.textSecondary,
                    BrMotion.quick(),
                    label = "segmentText",
                )
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pressable(
                            role = Role.Tab,
                            pressedScale = 0.96f,
                            haptic = if (selected) null else HapticFeedbackType.SegmentTick,
                        ) { onSelect(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        option,
                        style = type.labelSmall,
                        color = tint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.graphicsLayer {
                            // A hair of emphasis as the pill arrives under this label.
                            val near = 1f - kotlin.math.abs(position.value - index).coerceAtMost(1f)
                            val s = lerp(1f, 1.02f, near)
                            scaleX = s
                            scaleY = s
                        },
                    )
                }
            }
        }
    }
}
