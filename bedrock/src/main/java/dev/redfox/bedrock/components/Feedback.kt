package dev.redfox.bedrock.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.redfox.bedrock.effects.floatShadow
import dev.redfox.bedrock.effects.glass
import dev.redfox.bedrock.motion.BrDurations
import dev.redfox.bedrock.motion.BrMotion
import dev.redfox.bedrock.motion.revealOnEnter
import dev.redfox.bedrock.theme.BedrockTheme
import dev.redfox.bedrock.theme.BrSpacing
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout

/** One message on screen. */
@Stable
public class BrToast internal constructor(
    public val message: String,
    public val actionLabel: String?,
    internal val result: CompletableDeferred<Boolean>,
)

/**
 * Shows short messages one at a time, queueing the rest. Hold one per app (`remember`) and place a
 * [BrToastHost] at the bottom of the root layout.
 */
@Stable
public class BrToastState {
    private val mutex = Mutex()

    /** The message on screen now, if any. */
    public var current: BrToast? by mutableStateOf(null)
        private set

    /**
     * Shows [message] and suspends until it's gone. With an [actionLabel] it stays for 5 seconds
     * and returns true if the action was tapped; without one it's a 2.8-second notice.
     */
    public suspend fun show(message: String, actionLabel: String? = null): Boolean = mutex.withLock {
        val toast = BrToast(message, actionLabel, CompletableDeferred())
        current = toast
        try {
            withTimeout(if (actionLabel != null) 5000L else 2800L) { toast.result.await() }
        } catch (_: TimeoutCancellationException) {
            false
        } finally {
            current = null
        }
    }

    internal fun act(toast: BrToast) {
        toast.result.complete(true)
    }
}

/**
 * Where toasts appear: a frosted pill near the bottom that rises gently into place and sinks
 * away. Pass [bottomPadding] to sit it above a nav bar.
 */
@Composable
public fun BrToastHost(state: BrToastState, modifier: Modifier = Modifier, bottomPadding: Dp = 24.dp) {
    val colors = BedrockTheme.colors
    val type = BedrockTheme.typography
    val rise = BrMotion.glide(IntOffset.VisibilityThreshold)
    AnimatedContent(
        targetState = state.current,
        modifier = modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = bottomPadding),
        contentAlignment = Alignment.BottomCenter,
        transitionSpec = {
            (fadeIn(tween(BrDurations.Enter, easing = BrMotion.Calm)) + slideInVertically(rise) { it / 2 }) togetherWith
                (fadeOut(tween(BrDurations.Swap)) + slideOutVertically(rise) { it / 3 })
        },
        label = "toast",
    ) { toast ->
        if (toast == null) {
            Spacer(Modifier.fillMaxWidth())
            return@AnimatedContent
        }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
            Row(
                Modifier
                    .widthIn(max = 480.dp)
                    .heightIn(min = 52.dp)
                    .floatShadow(CircleShape)
                    .glass(CircleShape, tint = colors.glassStrong)
                    .semantics { liveRegion = LiveRegionMode.Polite }
                    .padding(start = 20.dp, end = if (toast.actionLabel != null) 6.dp else 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(BrSpacing.M),
            ) {
                Text(toast.message, style = type.body, color = colors.textPrimary, modifier = Modifier.weight(1f, fill = false))
                if (toast.actionLabel != null) {
                    BrButton(
                        text = toast.actionLabel,
                        onClick = { state.act(toast) },
                        variant = ButtonVariant.Ink,
                        size = ButtonSize.Small,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }
        }
    }
}

/** A count in a small accent pill (unread, pending). Hidden at zero. */
@Composable
public fun BrBadge(count: Int, modifier: Modifier = Modifier, max: Int = 99) {
    if (count <= 0) return
    val colors = BedrockTheme.colors
    Box(
        modifier
            .heightIn(min = 20.dp)
            .widthIn(min = 20.dp)
            .clip(CircleShape)
            .background(colors.accentMain)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (count > max) "$max+" else count.toString(),
            style = BedrockTheme.typography.caption.copy(fontFeatureSettings = "tnum"),
            color = colors.onAccent,
            textAlign = TextAlign.Center,
        )
    }
}

/** The colour family of a [BrTag]. */
public enum class TagTone { Neutral, Accent, Positive, Caution, Critical }

/** A short status word in a soft tinted pill: "Paid", "Due in 3 days", "Draft". */
@Composable
public fun BrTag(text: String, modifier: Modifier = Modifier, tone: TagTone = TagTone.Neutral, icon: Painter? = null) {
    val colors = BedrockTheme.colors
    val (container, content) = when (tone) {
        TagTone.Neutral -> colors.surfaceHigh to colors.textSecondary
        TagTone.Accent -> colors.accentContainer to colors.accentSoft
        TagTone.Positive -> colors.positive.copy(alpha = 0.16f) to colors.positive
        TagTone.Caution -> colors.caution.copy(alpha = 0.16f) to colors.caution
        TagTone.Critical -> colors.critical.copy(alpha = 0.16f) to colors.critical
    }
    Row(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(container)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(12.dp))
        Text(text, style = BedrockTheme.typography.caption, color = content)
    }
}

/**
 * Initials on a soft tint chosen from the name, so the same person always gets the same colour
 * and a list of people reads as varied without looking random. Pass [image] (any image loader)
 * to show a photo instead.
 */
@Composable
public fun BrAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    image: (@Composable () -> Unit)? = null,
) {
    val family = dev.redfox.bedrock.theme.BedrockAccent.Presets.let { it[(name.hashCode() and 0x7fffffff) % it.size] }
    Box(
        modifier.size(size).clip(CircleShape).background(family.main.copy(alpha = 0.22f)),
        contentAlignment = Alignment.Center,
    ) {
        if (image != null) {
            image()
        } else {
            Text(
                initials(name),
                style = BedrockTheme.typography.labelSmall.copy(fontSize = BedrockTheme.typography.labelSmall.fontSize * (size.value / 40f)),
                color = family.soft,
            )
        }
    }
}

/** The initials Bedrock shows for [name]: first letters of the first and last words. */
public fun initials(name: String): String {
    val words = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        words.isEmpty() -> "?"
        words.size == 1 -> words[0].take(1).uppercase()
        else -> (words.first().take(1) + words.last().take(1)).uppercase()
    }
}

/**
 * A metric on a card: a [label], a large tabular [value] with an optional [unit], and a quiet
 * [caption]. Give it an [accent] to tint the icon tile; the number stays in plain text, because
 * colour should mark *what* it is, not shout *how much*.
 */
@Composable
public fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    caption: String? = null,
    icon: Painter? = null,
    accent: Color = BedrockTheme.colors.accentMain,
    onClick: (() -> Unit)? = null,
) {
    val colors = BedrockTheme.colors
    val type = BedrockTheme.typography
    BrCard(modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(BrSpacing.S)) {
            if (icon != null) IconTile(icon, tint = accent, container = accent.copy(alpha = 0.16f), size = 28.dp, iconSize = 16.dp)
            Text(label, style = type.labelSmall, color = colors.textSecondary)
        }
        Spacer(Modifier.size(BrSpacing.L))
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            dev.redfox.bedrock.motion.RollingNumber(value, type.figureLarge, colors.textPrimary)
            if (unit != null) Text(unit, style = type.caption, color = colors.textTertiary, modifier = Modifier.padding(bottom = 4.dp))
        }
        if (caption != null) {
            Spacer(Modifier.size(2.dp))
            Text(caption, style = type.caption, color = colors.textTertiary)
        }
    }
}

/**
 * What a screen shows when there's nothing to show: an icon, a plain-spoken [title], one line of
 * [message] saying what will appear here, and optionally the action that fills it.
 */
@Composable
public fun EmptyState(
    icon: Painter,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    val colors = BedrockTheme.colors
    val type = BedrockTheme.typography
    Column(
        modifier.fillMaxWidth().padding(horizontal = BrSpacing.XXL, vertical = BrSpacing.Section),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconTile(
            icon,
            modifier = Modifier.revealOnEnter(0),
            tint = colors.accentSoft,
            container = colors.accentContainer,
            size = 56.dp,
            iconSize = 26.dp,
        )
        Spacer(Modifier.size(BrSpacing.XL))
        Text(title, style = type.headline, color = colors.textPrimary, textAlign = TextAlign.Center, modifier = Modifier.revealOnEnter(1))
        Spacer(Modifier.size(BrSpacing.S))
        Text(message, style = type.body, color = colors.textSecondary, textAlign = TextAlign.Center, modifier = Modifier.revealOnEnter(2))
        if (actionLabel != null) {
            Spacer(Modifier.size(BrSpacing.XL))
            BrButton(actionLabel, onAction, variant = ButtonVariant.Tonal, size = ButtonSize.Medium, modifier = Modifier.revealOnEnter(3))
        }
    }
}
