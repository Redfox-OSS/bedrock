package dev.redfox.bedrock.navigation

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import dev.redfox.bedrock.components.BrBadge
import dev.redfox.bedrock.effects.floatShadow
import dev.redfox.bedrock.effects.glass
import dev.redfox.bedrock.effects.pressable
import dev.redfox.bedrock.effects.rim
import dev.redfox.bedrock.motion.BrMotion
import dev.redfox.bedrock.theme.BedrockTheme
import dev.redfox.bedrock.theme.BrSpacing
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * One destination in a [BrNavBar]. The bar shows only the [icon]; [label] is what a screen reader
 * says. [badge] adds a count over the icon.
 */
@Immutable
public data class BrNavItem(
    @param:DrawableRes val icon: Int,
    val label: String,
    val badge: Int = 0,
)

/**
 * Space a tab screen must leave at the bottom of its scrolling content so the last item can
 * scroll clear of the [BrNavBar].
 */
@Composable
public fun brNavClearance(): Dp =
    BarHeight + BottomGap + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + BrSpacing.L

/**
 * Bedrock's floating tab bar: a frosted capsule of icons with no labels. The selected tab sits in
 * a soft accent pill that glides between tabs.
 *
 * **Hold and scrub.** Press anywhere on the bar and either hold for a moment or start sliding:
 * the pill lifts into a larger, lighter lens that follows your finger across the icons, each icon
 * lighting as the lens passes over it, with a light tick at every tab. Let go and the lens
 * settles onto the nearest tab and selects it. A plain tap selects directly. Every movement is
 * critically damped: the pill glides and rests; it never stretches, wobbles or overshoots.
 *
 * [accessory] is an optional separate round button beside the bar (search, compose).
 *
 * **Compact form.** While [compact] is true the capsule eases into a slimmer, icons-tight form
 * that sits lower on the screen, giving the content more room while it's being read; it eases back
 * when [compact] turns false. [rememberCompactNavState] turns it on as content scrolls toward its
 * end. The compact bar stays fully usable: tap and scrub work the same. The bar keeps its full-height
 * slot, so content padded by [brNavClearance] never shifts.
 *
 * Performance: the pill's position and lift, and the compact morph, are read only in layout and
 * draw, so a glide, a scrub or the morph never recomposes the bar.
 */
@Composable
public fun BrNavBar(
    items: List<BrNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    accessory: BrNavItem? = null,
    onAccessory: () -> Unit = {},
    /** A State, not a Boolean, so only the bar (not its caller) recomposes when it flips. */
    compact: State<Boolean> = NeverCompact,
) {
    val colors = BedrockTheme.colors
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val count = items.size.coerceAtLeast(1)
    val select by rememberUpdatedState(onSelect)
    /** 0 = full bar, 1 = compact. A slow, critically damped spring: an ease, interruptible mid-way. */
    val shrink = animateFloatAsState(if (compact.value) 1f else 0f, BrMotion.glide(), label = "navCompact")

    /** The pill's position, in tabs (0 = first tab). */
    val position = remember { Animatable(selectedIndex.coerceAtLeast(0).toFloat()) }
    /** 0 at rest, 1 while scrubbing: the pill becomes a lens. */
    val lift = remember { Animatable(0f) }
    var scrubbing by remember { mutableStateOf(false) }
    var releases by remember { mutableIntStateOf(0) }

    // Follow the selection from outside, and settle back onto it after every release (even if
    // the caller declined the change).
    LaunchedEffect(selectedIndex, releases) {
        if (!scrubbing && selectedIndex >= 0) position.animateTo(selectedIndex.toFloat(), BrMotion.settle())
    }

    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = BrSpacing.Gutter, end = BrSpacing.Gutter, bottom = BottomGap),
        contentAlignment = Alignment.BottomCenter,
    ) {
        val accessoryRoom = if (accessory != null) BarHeight + AccessoryGap else 0.dp
        val barWidth = minOf(maxWidth - accessoryRoom, SlotWidth * count + Inner * 2)
        val compactWidth = minOf(barWidth, CompactSlotWidth * count + CompactInner * 2)
        Row(horizontalArrangement = Arrangement.spacedBy(AccessoryGap), verticalAlignment = Alignment.Bottom) {
            Box(
                Modifier
                    .layout { measurable, _ ->
                        // The capsule morphs inside a fixed full-height slot, resting on its bottom
                        // edge, so it lowers as it slims and nothing around it re-measures.
                        val t = shrink.value
                        val slotHeight = BarHeight.roundToPx()
                        val width = lerp(barWidth.toPx(), compactWidth.toPx(), t).roundToInt()
                        val height = lerp(BarHeight.toPx(), CompactHeight.toPx(), t).roundToInt()
                        val placeable = measurable.measure(Constraints.fixed(width, height))
                        layout(width, slotHeight) { placeable.place(0, slotHeight - height) }
                    }
                    .floatShadow(CircleShape)
                    .glass(CircleShape)
                    .layout { measurable, constraints ->
                        // Inner padding eases from 6dp to 4dp with the morph.
                        val pad = lerp(Inner.toPx(), CompactInner.toPx(), shrink.value).roundToInt()
                        val placeable = measurable.measure(constraints.offset(-pad * 2, -pad * 2))
                        layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(pad, pad) }
                    }
                    .pointerInput(count) {
                        // Read the width live: it changes as the bar morphs.
                        fun slot() = size.width.toFloat() / count
                        fun slotAt(x: Float) = (x / slot()).toInt().coerceIn(0, count - 1)
                        fun along(x: Float) = (x / slot() - 0.5f).coerceIn(0f, (count - 1).toFloat())

                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var held = false
                            var released = false
                            // Decide: a tap, a hold, or a slide.
                            val decided = withTimeoutOrNull(HoldMillis) {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: return@withTimeoutOrNull
                                    if (!change.pressed) {
                                        released = true
                                        return@withTimeoutOrNull
                                    }
                                    if (abs(change.position.x - down.position.x) > viewConfiguration.touchSlop) {
                                        held = true
                                        return@withTimeoutOrNull
                                    }
                                }
                            }
                            if (decided == null) held = true
                            if (released) {
                                val target = slotAt(down.position.x)
                                if (target != selectedIndexOrNull(position)) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                select(target)
                                releases++
                                return@awaitEachGesture
                            }
                            if (!held) return@awaitEachGesture

                            // Scrubbing: the lens follows the finger.
                            scrubbing = true
                            haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                            scope.launch { lift.animateTo(1f, BrMotion.settle()) }
                            var hover = slotAt(down.position.x)
                            var x = down.position.x
                            scope.launch { position.animateTo(along(x), BrMotion.quick()) }
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) break
                                x = change.position.x
                                change.consume()
                                scope.launch { position.snapTo(along(x).let { lerp(position.value, it, 0.55f) }) }
                                val now = slotAt(x)
                                if (now != hover) {
                                    hover = now
                                    haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                                }
                            }
                            scrubbing = false
                            scope.launch { lift.animateTo(0f, BrMotion.settle()) }
                            select(hover)
                            releases++
                        }
                    }
            ) {
                Lens(position, lift, count)
                Row(Modifier.fillMaxSize()) {
                    items.forEachIndexed { index, item ->
                        NavIcon(
                            item = item,
                            index = index,
                            selected = index == selectedIndex,
                            position = position,
                            lift = lift,
                            shrink = shrink,
                            onClick = { select(index) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            if (accessory != null) {
                Box(
                    Modifier
                        .layout { measurable, _ ->
                            val slotHeight = BarHeight.roundToPx()
                            val size = lerp(BarHeight.toPx(), CompactHeight.toPx(), shrink.value).roundToInt()
                            val placeable = measurable.measure(Constraints.fixed(size, size))
                            layout(size, slotHeight) { placeable.place(0, slotHeight - size) }
                        }
                        .pressable(pressedScale = 0.94f, haptic = HapticFeedbackType.ContextClick, onClick = onAccessory)
                        .floatShadow(CircleShape)
                        .glass(CircleShape)
                        .semantics { contentDescription = accessory.label },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(accessory.icon), contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(IconSize))
                }
            }
        }
    }
}

/** The pill: a soft accent capsule at rest, a larger, lighter lens while scrubbing. */
@Composable
private fun BoxScope.Lens(position: Animatable<Float, *>, lift: Animatable<Float, *>, count: Int) {
    val colors = BedrockTheme.colors
    Box(
        Modifier
            .matchParentSize()
            .layout { measurable, constraints ->
                val slot = constraints.maxWidth.toFloat() / count
                val l = lift.value
                // The lens grows a little past its slot and up to the bar's height while held.
                val width = (slot + LensGrow.toPx() * l).roundToInt()
                val height = (constraints.maxHeight + LensRise.toPx() * l).roundToInt()
                val placeable = measurable.measure(Constraints.fixed(width, height))
                layout(constraints.maxWidth, constraints.maxHeight) {
                    val x = (slot * position.value + (slot - width) / 2f).roundToInt()
                    placeable.place(x, (constraints.maxHeight - height) / 2)
                }
            }
    ) {
        // Resting pill.
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 1f - lift.value }
                .clip(CircleShape)
                .background(colors.accentContainerStrong)
        )
        // Lens: lighter, with a lit rim, fading in as it lifts.
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = lift.value }
                .clip(CircleShape)
                .background(colors.ink.copy(alpha = 0.14f))
                .rim(CircleShape)
        )
    }
}

@Composable
private fun NavIcon(
    item: BrNavItem,
    index: Int,
    selected: Boolean,
    position: Animatable<Float, *>,
    lift: Animatable<Float, *>,
    shrink: State<Float>,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val colors = BedrockTheme.colors
    val painter = painterResource(item.icon)
    Box(
        modifier
            .fillMaxHeight()
            .semantics {
                this.selected = selected
                role = Role.Tab
                contentDescription = item.label
                onClick(label = item.label) { onClick(); true }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.graphicsLayer {
                // The icon under the lens swells slightly while scrubbing.
                val near = (1f - abs(position.value - index)).coerceIn(0f, 1f)
                // Icons ease to 90% in the compact bar.
                val s = (1f + 0.1f * near * lift.value) * (1f - 0.1f * shrink.value)
                scaleX = s
                scaleY = s
            }
        ) {
            // The resting icon, and an accent copy that brightens as the pill passes over it:
            // the highlight follows the pill continuously instead of switching at the end.
            Icon(painter, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(IconSize))
            Icon(
                painter,
                contentDescription = null,
                tint = colors.accentSoft,
                modifier = Modifier
                    .size(IconSize)
                    .graphicsLayer { alpha = (1f - abs(position.value - index)).coerceIn(0f, 1f) },
            )
            if (item.badge > 0) {
                BrBadge(item.badge, Modifier.align(Alignment.TopEnd).offset(x = 12.dp, y = (-8).dp))
            }
        }
    }
}

/** The selected tab the pill rests on, if it's resting on one. */
private fun selectedIndexOrNull(position: Animatable<Float, *>): Int? {
    val v = position.value
    return if (abs(v - v.roundToInt()) < 0.01f) v.roundToInt() else null
}

private val BarHeight = 64.dp
private val BottomGap = 12.dp
private val AccessoryGap = 10.dp
private val Inner = 6.dp
private val SlotWidth = 68.dp
private val IconSize = 24.dp
private val LensGrow = 10.dp
private val LensRise = 4.dp
private val CompactHeight = 52.dp
private val CompactInner = 4.dp
/** Per-tab width when compact: still wide enough that the selection reads as a pill. */
private val CompactSlotWidth = 52.dp

private object NeverCompact : State<Boolean> {
    override val value: Boolean = false
}
private const val HoldMillis = 260L
