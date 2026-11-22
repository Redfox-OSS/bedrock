package dev.redfox.bedrock.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.redfox.bedrock.effects.Aura
import dev.redfox.bedrock.effects.aura
import dev.redfox.bedrock.effects.glass
import dev.redfox.bedrock.effects.pressable
import dev.redfox.bedrock.effects.rim
import dev.redfox.bedrock.theme.BedrockTheme
import dev.redfox.bedrock.theme.BrShapes
import dev.redfox.bedrock.theme.BrSpacing

/**
 * The everyday surface: a [BedrockTheme.colors] `surface` card with a hairline edge. Separation
 * from the page comes from tone and that hairline, never a shadow. Give it [onClick] to make the
 * whole card a target.
 */
@Composable
public fun BrCard(
    modifier: Modifier = Modifier,
    shape: Shape = BrShapes.Large,
    color: Color = BedrockTheme.colors.surface,
    contentPadding: PaddingValues = PaddingValues(BrSpacing.L),
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = BedrockTheme.colors
    Column(
        modifier
            .then(if (onClick != null) Modifier.pressable(pressedScale = 0.98f, onLongClick = onLongClick, onClick = onClick) else Modifier)
            .clip(shape)
            .background(color)
            .border(1.dp, colors.line, shape)
            .padding(contentPadding),
        content = content,
    )
}

/**
 * A frosted card: the content scrolling behind it shows through, blurred, with a lit rim. Use it
 * for things that float over imagery or colour (a control over a photo, a card over an aura).
 */
@Composable
public fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = BrShapes.Large,
    tint: Color = BedrockTheme.colors.glass,
    contentPadding: PaddingValues = PaddingValues(BrSpacing.L),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .then(if (onClick != null) Modifier.pressable(pressedScale = 0.98f, onClick = onClick) else Modifier)
            .glass(shape, tint)
            .padding(contentPadding),
        content = content,
    )
}

/**
 * A card made of colour: an [Aura] fills it edge to edge, with a lit rim. Bedrock's hero surface,
 * for the one thing on a screen that should feel warm (a balance, today's plan, a featured item).
 * Text on it uses `textPrimary`; [Aura.shade] keeps the bottom readable. With [drift] the light
 * wanders slowly; use that on at most one card per screen.
 */
@Composable
public fun AuraCard(
    aura: Aura,
    modifier: Modifier = Modifier,
    shape: Shape = BrShapes.XLarge,
    drift: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(BrSpacing.XL),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .then(if (onClick != null) Modifier.pressable(pressedScale = 0.98f, onClick = onClick) else Modifier)
            .clip(shape)
            .aura(aura, drift)
            .rim(shape)
            .padding(contentPadding),
        content = content,
    )
}

/**
 * A small rounded square holding an icon, at the start of a row or in a stat tile. [container]
 * defaults to the raised surface; pass an accent container for rows that should carry colour.
 */
@Composable
public fun IconTile(
    icon: Painter,
    modifier: Modifier = Modifier,
    tint: Color = BedrockTheme.colors.textPrimary,
    container: Color = BedrockTheme.colors.surfaceHigh,
    size: Dp = 34.dp,
    iconSize: Dp = 18.dp,
) {
    Box(
        modifier.size(size).clip(BrShapes.concentric(size / 2 + 2.dp, 6.dp)).background(container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
    }
}

/** A 1dp separator. [inset] indents it from the start, to line up with row text. */
@Composable
public fun BrDivider(modifier: Modifier = Modifier, inset: Dp = 0.dp) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(start = inset)
            .height(1.dp)
            .background(BedrockTheme.colors.divider)
    )
}

/**
 * The title above a group of content, in sentence case. An optional [actionLabel] sits at the
 * end ("See all"). Pad it by the page gutter like the content below it.
 */
@Composable
public fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    val colors = BedrockTheme.colors
    val type = BedrockTheme.typography
    Row(
        modifier.fillMaxWidth().padding(top = BrSpacing.S, bottom = BrSpacing.M),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(title, style = type.label, color = colors.textSecondary)
        if (actionLabel != null) {
            Text(
                actionLabel,
                style = type.label,
                color = colors.accentSoft,
                modifier = Modifier.pressable(pressedScale = 0.95f, onClick = onAction),
            )
        }
    }
}
