package dev.redfox.bedrock.components

import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import dev.redfox.bedrock.effects.LocalHazeState
import dev.redfox.bedrock.effects.floatShadow
import dev.redfox.bedrock.effects.glass
import dev.redfox.bedrock.effects.pressable
import dev.redfox.bedrock.motion.BrDurations
import dev.redfox.bedrock.motion.BrMotion
import dev.redfox.bedrock.theme.BedrockTheme
import dev.redfox.bedrock.theme.BrShapes
import dev.redfox.bedrock.theme.BrSpacing
import kotlinx.coroutines.launch

/**
 * A full-screen window for an overlay, kept open while [state] is visible *or still animating
 * out*, so exits play in full before the window goes. The platform's own dim and window
 * animations are switched off: Bedrock draws its scrim and moves the content itself.
 *
 * Glass inside falls back to a near-solid surface: blur can't reach across windows, and over a
 * dimmed page a solid sheet reads better anyway.
 */
@Composable
private fun OverlayWindow(
    state: MutableTransitionState<Boolean>,
    onDismiss: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    if (!state.currentState && !state.targetState && state.isIdle) return
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            window?.apply {
                setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
                // Edge to edge, like the page behind: without these a dialog window leaves the
                // system bars to Android, which paints them black (a dark strip under a sheet and
                // an undimmed band under the status bar). The sheet pads its own content.
                attributes = attributes.apply { fitInsetsTypes = 0 }
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                @Suppress("DEPRECATION")
                statusBarColor = android.graphics.Color.TRANSPARENT
                @Suppress("DEPRECATION")
                navigationBarColor = android.graphics.Color.TRANSPARENT
                isNavigationBarContrastEnforced = false
                isStatusBarContrastEnforced = false
                setDimAmount(0f)
                setWindowAnimations(0)
            }
        }
        CompositionLocalProvider(LocalHazeState provides null) {
            Box(Modifier.fillMaxSize(), content = content)
        }
    }
}

/** The dimmed page behind an overlay; tapping it dismisses. */
@Composable
private fun Scrim(state: MutableTransitionState<Boolean>, onDismiss: () -> Unit) {
    AnimatedVisibility(
        visibleState = state,
        enter = fadeIn(tween(BrDurations.Enter, easing = BrMotion.Calm)),
        exit = fadeOut(tween(BrDurations.Swap)),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(BedrockTheme.colors.scrim)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)
        )
    }
}

/**
 * A bottom sheet in its own window, so it covers everything on screen, including a floating nav
 * bar. It glides up over a dimmed page and can be pulled down to dismiss: past a quarter of its
 * height, or with a quick flick. Let go short of that and it settles back. Back dismisses it, and
 * it rides above the keyboard when a field inside it has focus.
 */
@Composable
public fun BrSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val state = remember { MutableTransitionState(false) }
    state.targetState = visible
    OverlayWindow(state, onDismiss) {
        val colors = BedrockTheme.colors
        val type = BedrockTheme.typography
        val scope = rememberCoroutineScope()
        val drag = remember { Animatable(0f) }
        val height = remember { floatArrayOf(1f) }
        val density = LocalDensity.current
        LaunchedEffect(visible) { if (visible) drag.snapTo(0f) }
        val shape = RoundedCornerShape(topStart = BrShapes.XLargeRadius, topEnd = BrShapes.XLargeRadius)

        Scrim(state, onDismiss)
        AnimatedVisibility(
            visibleState = state,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(BrMotion.glide(IntOffset.VisibilityThreshold)) { it },
            exit = slideOutVertically(BrMotion.settle(IntOffset.VisibilityThreshold)) { it },
        ) {
            Column(
                modifier
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
                    .floatShadow(shape, radius = 32.dp, offsetY = 0.dp, alpha = 0.35f)
                    // Its own window can't blur the page, so the sheet is a solid surface (anything
                    // less lets bright text behind it ghost through), still with the lit rim.
                    .glass(shape, tint = colors.surface)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    // Rides above the keyboard when a field inside has focus.
                    .imePadding()
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
 * A centred dialog card with your own content, in its own window above everything. It fades in
 * while easing up from 96% scale: present, not popped. Back and a tap outside call [onDismiss].
 * For the common title, message and two buttons, use [BrDialog].
 */
@Composable
public fun BrDialogFrame(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val state = remember { MutableTransitionState(false) }
    state.targetState = visible
    OverlayWindow(state, onDismiss) {
        Scrim(state, onDismiss)
        AnimatedVisibility(
            visibleState = state,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(tween(BrDurations.Enter, easing = BrMotion.Calm)) +
                scaleIn(tween(BrDurations.Enter, easing = BrMotion.Calm), initialScale = 0.96f),
            exit = fadeOut(tween(BrDurations.Fade)) + scaleOut(tween(BrDurations.Swap, easing = BrMotion.Calm), targetScale = 0.98f),
        ) {
            Column(
                modifier
                    .padding(horizontal = BrSpacing.XXL)
                    .widthIn(max = 400.dp)
                    .floatShadow(BrShapes.XLarge, radius = 40.dp, offsetY = 16.dp)
                    .glass(BrShapes.XLarge, tint = BedrockTheme.colors.surface)
                    .padding(BrSpacing.XL),
                content = content,
            )
        }
    }
}

/**
 * A confirmation dialog: a title, a plain-spoken message and two buttons. [destructive] makes the
 * confirm button critical. Pass [dismissLabel] = null for a single-button notice.
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
    BrDialogFrame(visible, onDismiss, modifier) {
        Text(title, style = type.headline, color = colors.textPrimary)
        Spacer(Modifier.height(BrSpacing.S))
        Text(message, style = type.body, color = colors.textSecondary)
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
