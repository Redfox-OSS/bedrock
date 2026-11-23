package dev.redfox.bedrock.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.redfox.bedrock.effects.floatShadow
import dev.redfox.bedrock.effects.glass
import dev.redfox.bedrock.effects.pressable
import dev.redfox.bedrock.motion.BrDurations
import dev.redfox.bedrock.motion.BrMotion
import dev.redfox.bedrock.theme.BedrockTheme
import dev.redfox.bedrock.theme.BrSpacing

/** How loudly a button asks to be pressed. One [Primary] or [Ink] per screen region. */
public enum class ButtonVariant {
    /** Solid accent. The main action. */
    Primary,
    /** Solid warm white. A high-contrast action that isn't the accent (over aura and photos). */
    Ink,
    /** A raised neutral fill. Secondary actions. */
    Tonal,
    /** Frosted. Actions floating over imagery or colour. */
    Glass,
    /** Text only, in the soft accent. Tertiary actions. */
    Plain,
    /** Solid critical tone. Confirms something that can't be undone. */
    Critical,
}

/** Button heights. */
public enum class ButtonSize(internal val height: Dp, internal val padding: Dp, internal val icon: Dp) {
    Large(54.dp, 24.dp, 20.dp),
    Medium(46.dp, 20.dp, 18.dp),
    Small(36.dp, 14.dp, 16.dp),
}

/**
 * A pill button. Set [loading] to swap the label for a small spinner without the button changing
 * size; the swap is a slow crossfade. Disabled buttons fade to 40% and stop responding.
 */
@Composable
public fun BrButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Large,
    icon: Painter? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    /** Overrides the default haptic (Confirm for Primary, Ink and Critical; none otherwise). */
    haptic: HapticFeedbackType? = null,
) {
    val colors = BedrockTheme.colors
    val type = BedrockTheme.typography
    val (container, content) = when (variant) {
        ButtonVariant.Primary -> colors.accentMain to colors.onAccent
        ButtonVariant.Ink -> colors.ink to colors.onInk
        ButtonVariant.Tonal -> colors.surfaceHigh to colors.textPrimary
        ButtonVariant.Glass -> Color.Transparent to colors.textPrimary
        ButtonVariant.Plain -> Color.Transparent to colors.accentSoft
        ButtonVariant.Critical -> colors.critical to colors.onInk
    }
    val style: TextStyle = if (size == ButtonSize.Small) type.labelSmall else type.label
    Box(
        modifier
            .height(size.height)
            .graphicsLayer { alpha = if (enabled) 1f else 0.4f }
            .pressable(
                enabled = enabled && !loading,
                pressedScale = 0.97f,
                haptic = haptic ?: if (variant == ButtonVariant.Primary || variant == ButtonVariant.Ink || variant == ButtonVariant.Critical) HapticFeedbackType.Confirm else null,
                onClick = onClick,
            )
            .then(
                when (variant) {
                    ButtonVariant.Glass -> Modifier.glass(CircleShape)
                    ButtonVariant.Plain -> Modifier.clip(CircleShape)
                    else -> Modifier.clip(CircleShape).background(container)
                }
            )
            .padding(horizontal = size.padding),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = loading,
            transitionSpec = { fadeIn(tween(BrDurations.Swap, easing = BrMotion.Calm)) togetherWith fadeOut(tween(BrDurations.Fade)) },
            contentAlignment = Alignment.Center,
            label = "buttonLoading",
        ) { busy ->
            if (busy) {
                Box(Modifier.semantics { contentDescription = "$text, loading" }) {
                    BrSpinner(size = size.icon, strokeWidth = 2.dp, color = content)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(BrSpacing.S)) {
                    if (icon != null) Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(size.icon))
                    Text(text, style = style, color = content, maxLines = 1)
                }
            }
        }
    }
}

/** Round icon button styles. */
public enum class IconButtonVariant { Glass, Tonal, Plain, Ink, Primary }

/**
 * A round, icon-only button with a 44dp target. [Glass][IconButtonVariant.Glass] for headers over
 * scrolling content, [Tonal][IconButtonVariant.Tonal] inside cards, [Plain][IconButtonVariant.Plain]
 * where the icon alone is enough.
 */
@Composable
public fun BrIconButton(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: IconButtonVariant = IconButtonVariant.Glass,
    size: Dp = 44.dp,
    enabled: Boolean = true,
    tint: Color? = null,
    /** A haptic on click (e.g. ContextClick for an action that opens something); none by default. */
    haptic: HapticFeedbackType? = null,
) {
    val colors = BedrockTheme.colors
    val iconTint by animateColorAsState(
        tint ?: when (variant) {
            IconButtonVariant.Ink -> colors.onInk
            IconButtonVariant.Primary -> colors.onAccent
            else -> colors.textPrimary
        },
        BrMotion.quick(),
        label = "iconTint",
    )
    Box(
        modifier
            .size(size)
            .graphicsLayer { alpha = if (enabled) 1f else 0.4f }
            .pressable(enabled = enabled, pressedScale = 0.94f, haptic = haptic, onClick = onClick)
            .then(
                when (variant) {
                    IconButtonVariant.Glass -> Modifier.glass(CircleShape)
                    IconButtonVariant.Tonal -> Modifier.clip(CircleShape).background(colors.surfaceHigh)
                    IconButtonVariant.Ink -> Modifier.clip(CircleShape).background(colors.ink)
                    IconButtonVariant.Primary -> Modifier.clip(CircleShape).background(colors.accentMain)
                    IconButtonVariant.Plain -> Modifier
                }
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(size * 0.45f))
    }
}

/**
 * A chip for filtering, Construct-style: the selected chip turns warm white with dark text, the
 * rest sit on a raised neutral. An optional [count] trails the label in a quieter tone.
 */
@Composable
public fun BrChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null,
    icon: Painter? = null,
) {
    val colors = BedrockTheme.colors
    val type = BedrockTheme.typography
    val container by animateColorAsState(if (selected) colors.ink else colors.surfaceRaised, BrMotion.quick(), label = "chipFill")
    val content by animateColorAsState(if (selected) colors.onInk else colors.textPrimary, BrMotion.quick(), label = "chipText")
    val meta by animateColorAsState(
        if (selected) colors.onInk.copy(alpha = 0.55f) else colors.textTertiary,
        BrMotion.quick(),
        label = "chipCount",
    )
    Row(
        modifier
            .height(36.dp)
            .pressable(pressedScale = 0.94f, haptic = HapticFeedbackType.SegmentTick, onClick = onClick)
            .clip(CircleShape)
            .background(container)
            .border(1.dp, if (selected) Color.Transparent else colors.line, CircleShape)
            .semantics { this.selected = selected }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(16.dp))
        Text(label, style = type.labelSmall, color = content)
        if (count != null) Text(count.toString(), style = type.caption.copy(fontFeatureSettings = "tnum"), color = meta)
    }
}

/**
 * The floating "add" button: a 56dp rounded square in the solid accent, resting on a soft neutral
 * shadow. It eases in (a short rise and fade) when it first appears; no spin, no pop.
 */
@Composable
public fun BrFab(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = BedrockTheme.colors
    val shape = RoundedCornerShape(20.dp)
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) { enter.animateTo(1f, tween(BrDurations.Enter, easing = BrMotion.Calm)) }
    Box(
        modifier
            .size(56.dp)
            .graphicsLayer {
                alpha = enter.value
                translationY = (1f - enter.value) * 10.dp.toPx()
            }
            .pressable(pressedScale = 0.94f, haptic = HapticFeedbackType.ContextClick, onClick = onClick)
            .floatShadow(shape, radius = 18.dp, offsetY = 6.dp, alpha = 0.45f)
            .clip(shape)
            .background(colors.accentMain)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = colors.onAccent, modifier = Modifier.size(26.dp))
    }
}
