package dev.redfox.bedrock.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.redfox.bedrock.effects.floatShadow
import dev.redfox.bedrock.effects.glass
import dev.redfox.bedrock.effects.pressable
import dev.redfox.bedrock.motion.BrDurations
import dev.redfox.bedrock.motion.BrMotion
import dev.redfox.bedrock.theme.BedrockTheme
import dev.redfox.bedrock.theme.BrShapes
import dev.redfox.bedrock.theme.BrSpacing
import kotlinx.coroutines.launch
import androidx.compose.animation.core.VisibilityThreshold

/**
 * A frosted bottom sheet. It glides up over a dimmed page and can be pulled down to dismiss:
 * past a quarter of its height, or with a quick flick. Let go short of that and it settles back.
 *
 * Place it last in the screen's root `Box` so it draws above everything, as a sibling of the
 * scrolling content (so the glass can blur it). Handle the system back gesture in the app.
 */
@Composable
public fun BrSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = BedrockTheme.colors
    val type = BedrockTheme.typography
    val scope = rememberCoroutineScope()
    val drag = remember { Animatable(0f) }
    val height = remember { floatArrayOf(1f) }
    LaunchedEffect(visible) { if (visible) drag.snapTo(0f) }
    val density = LocalDensity.current
    val shape = RoundedCornerShape(topStart = BrShapes.XLargeRadius, topEnd = BrShapes.XLargeRadius)

    Box(modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(BrDurations.Enter, easing = BrMotion.Calm)),
            exit = fadeOut(tween(BrDurations.Swap)),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(colors.scrim)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)
            )
        }
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(BrMotion.glide(IntOffset.VisibilityThreshold)) { it },
            exit = slideOutVertically(BrMotion.settle(IntOffset.VisibilityThreshold)) { it },
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .onSizeChanged { height[0] = it.height.toFloat().coerceAtLeast(1f) }
                    .graphicsLayer { translationY = drag.value }
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            // Free downward; a firm resistance upward, so it can't be pulled off its edge.
                            val next = if (drag.value + delta < 0f) drag.value + delta * 0.15f else drag.value + delta
                            scope.launch { drag.snapTo(next.coerceAtLeast(with(density) { -16.dp.toPx() })) }
                        },
                        onDragStopped = { velocity ->
                            if (drag.value > height[0] * 0.25f || velocity > 1400f) onDismiss()
                            else drag.animateTo(0f, BrMotion.settle())
                        },
                    )
                    .glass(shape, tint = colors.glassStrong, blurRadius = 36.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = BrSpacing.Gutter)
                    .padding(bottom = BrSpacing.L),
            ) {
                Box(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(36.dp, 5.dp).clip(CircleShape).background(colors.textTertiary.copy(alpha = 0.5f)))
                }
                if (title != null) {
                    Text(title, style = type.headline, color = colors.textPrimary, modifier = Modifier.padding(top = BrSpacing.S, bottom = BrSpacing.L))
                }
                content()
            }
        }
    }
}

/** A tappable action row inside a [BrSheet]: an icon and a label. [destructive] turns it critical. */
@Composable
public fun SheetAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    destructive: Boolean = false,
) {
    val colors = BedrockTheme.colors
    val tint = if (destructive) colors.critical else colors.textPrimary
    Row(
        modifier
            .fillMaxWidth()
            .height(54.dp)
            .pressable(pressedScale = 0.98f, onClick = onClick)
            .clip(BrShapes.Medium)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BrSpacing.L),
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Text(label, style = BedrockTheme.typography.bodyLarge, color = tint)
    }
}

/**
 * A centred dialog on frosted glass. It fades in while easing up from 96% scale: present, not
 * popped. [destructive] makes the confirm button critical. Place it last in the root `Box`.
 */
@Composable
public fun BrDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String? = "Cancel",
    destructive: Boolean = false,
) {
    val colors = BedrockTheme.colors
    val type = BedrockTheme.typography
    val state = remember { MutableTransitionState(false) }
    state.targetState = visible
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visibleState = state,
            enter = fadeIn(tween(BrDurations.Enter, easing = BrMotion.Calm)),
            exit = fadeOut(tween(BrDurations.Swap)),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(colors.scrim)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)
            )
        }
        AnimatedVisibility(
            visibleState = state,
            enter = fadeIn(tween(BrDurations.Enter, easing = BrMotion.Calm)) +
                scaleIn(tween(BrDurations.Enter, easing = BrMotion.Calm), initialScale = 0.96f),
            exit = fadeOut(tween(BrDurations.Fade)) + scaleOut(tween(BrDurations.Swap, easing = BrMotion.Calm), targetScale = 0.98f),
        ) {
            Column(
                Modifier
                    .padding(horizontal = BrSpacing.XXL)
                    .widthIn(max = 400.dp)
                    .floatShadow(BrShapes.XLarge, radius = 40.dp, offsetY = 16.dp)
                    .glass(BrShapes.XLarge, tint = colors.glassStrong, blurRadius = 36.dp)
                    .padding(BrSpacing.XL),
            ) {
                Text(title, style = type.headline, color = colors.textPrimary)
                Spacer(Modifier.height(BrSpacing.S))
                Text(message, style = type.body, color = colors.textSecondary, textAlign = TextAlign.Start)
                Spacer(Modifier.height(BrSpacing.XL))
                Row(horizontalArrangement = Arrangement.spacedBy(BrSpacing.S)) {
                    if (dismissLabel != null) {
                        BrButton(dismissLabel, onDismiss, Modifier.weight(1f), variant = ButtonVariant.Tonal, size = ButtonSize.Medium)
                    }
                    BrButton(
                        confirmLabel,
                        onConfirm,
                        Modifier.weight(1f),
                        variant = if (destructive) ButtonVariant.Critical else ButtonVariant.Primary,
                        size = ButtonSize.Medium,
                    )
                }
            }
        }
    }
}
