package dev.redfox.bedrock.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Root of every Bedrock app. Screens read tokens from [BedrockTheme.colors] and
 * [BedrockTheme.typography]; a matching Material 3 theme is installed underneath so any stock
 * component left in an app (a date picker, a dropdown) still sits in the same palette.
 */
@Composable
public fun BedrockTheme(
    colors: BedrockColors = bedrockColors(),
    typography: BedrockTypography = BedrockTypography.Default,
    content: @Composable () -> Unit,
) {
    val scheme = remember(colors) {
        darkColorScheme(
            primary = colors.accentMain,
            onPrimary = colors.onAccent,
            primaryContainer = colors.accentContainer,
            onPrimaryContainer = colors.accentSoft,
            secondary = colors.accentSoft,
            onSecondary = colors.onAccent,
            secondaryContainer = colors.surfaceHigh,
            onSecondaryContainer = colors.textPrimary,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.surfaceRaised,
            onSurfaceVariant = colors.textSecondary,
            surfaceContainerLowest = colors.background,
            surfaceContainerLow = colors.surface,
            surfaceContainer = colors.surface,
            surfaceContainerHigh = colors.surfaceRaised,
            surfaceContainerHighest = colors.surfaceHigh,
            outline = colors.lineStrong,
            outlineVariant = colors.line,
            error = colors.critical,
            scrim = colors.scrim,
        )
    }
    val material = remember(typography) { materialTypography(typography) }
    CompositionLocalProvider(
        LocalBedrockColors provides colors,
        LocalBedrockTypography provides typography,
    ) {
        MaterialTheme(colorScheme = scheme, typography = material, shapes = MaterialShapes, content = content)
    }
}

/** Convenience overload: one accent on one neutral ramp (both have presets on their companions). */
@Composable
public fun BedrockTheme(
    accent: BedrockAccent,
    neutrals: BedrockNeutrals = BedrockNeutrals.Basalt,
    typography: BedrockTypography = BedrockTypography.Default,
    content: @Composable () -> Unit,
) {
    val colors = remember(accent, neutrals) { bedrockColors(accent, neutrals) }
    BedrockTheme(colors = colors, typography = typography, content = content)
}

/** Where screens read the current theme. */
public object BedrockTheme {
    public val colors: BedrockColors
        @Composable @ReadOnlyComposable get() = LocalBedrockColors.current

    public val typography: BedrockTypography
        @Composable @ReadOnlyComposable get() = LocalBedrockTypography.current
}

/**
 * Corner radii. Nested corners are concentric: an inner shape's radius is its parent's radius
 * minus the padding between them ([concentric]), so curves run parallel instead of pinching.
 */
public object BrShapes {
    public val XSmall: RoundedCornerShape = RoundedCornerShape(8.dp)
    public val Small: RoundedCornerShape = RoundedCornerShape(12.dp)
    /** Icon tiles and fields inside cards. */
    public val Medium: RoundedCornerShape = RoundedCornerShape(16.dp)
    /** Cards. */
    public val Large: RoundedCornerShape = RoundedCornerShape(22.dp)
    /** Hero cards, sheets, dialogs. */
    public val XLarge: RoundedCornerShape = RoundedCornerShape(30.dp)
    public val Pill: RoundedCornerShape = CircleShape

    public val SmallRadius: Dp = 12.dp
    public val MediumRadius: Dp = 16.dp
    public val LargeRadius: Dp = 22.dp
    public val XLargeRadius: Dp = 30.dp

    /** The radius for a shape inset by [padding] inside a parent with [outerRadius]. */
    public fun concentric(outerRadius: Dp, padding: Dp): RoundedCornerShape =
        RoundedCornerShape((outerRadius - padding).coerceAtLeast(4.dp))
}

/** A 4dp spacing scale. Every page uses [Gutter] as its horizontal edge. */
public object BrSpacing {
    public val XXS: Dp = 2.dp
    public val XS: Dp = 4.dp
    public val S: Dp = 8.dp
    public val M: Dp = 12.dp
    public val L: Dp = 16.dp
    public val Gutter: Dp = 20.dp
    public val XL: Dp = 24.dp
    public val XXL: Dp = 32.dp
    /** Between page sections. */
    public val Section: Dp = 40.dp
}

private val MaterialShapes = Shapes(
    extraSmall = BrShapes.XSmall,
    small = BrShapes.Small,
    medium = BrShapes.Medium,
    large = BrShapes.Large,
    extraLarge = BrShapes.XLarge,
)
