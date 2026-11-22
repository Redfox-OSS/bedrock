package dev.redfox.bedrock.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * One accent colour family. Bedrock accents are mid-bright: lively enough to carry a screen, never
 * neon. Each family has five tones so nothing is ever derived by guesswork at the call site.
 *
 * - [main]: solid fills (primary button, switch track, slider fill, selected radio).
 * - [soft]: a pale tint, for text and icons on a 16–24% [main] container.
 * - [deep]: a dark shade, the floor of an aura gradient.
 * - [onMain]: text and icons on a solid [main] fill.
 * - [partner]: a neighbouring hue that an aura blends toward, so colour fields feel lit, not flat.
 */
@Immutable
public data class BedrockAccent(
    val name: String,
    val main: Color,
    val soft: Color,
    val deep: Color,
    val onMain: Color,
    val partner: Color,
) {
    public companion object {
        /** Warm orange. The default: friendly, unmistakably not another blue app. */
        public val Ember: BedrockAccent = BedrockAccent(
            name = "Ember",
            main = Color(0xFFF2884B),
            soft = Color(0xFFFFCBA8),
            deep = Color(0xFF7A3212),
            onMain = Color(0xFF2B1205),
            partner = Color(0xFFE8607A),
        )

        /** Clear sea blue. */
        public val Tide: BedrockAccent = BedrockAccent(
            name = "Tide",
            main = Color(0xFF4FA6E6),
            soft = Color(0xFFB6DCF7),
            deep = Color(0xFF123F66),
            onMain = Color(0xFF03203A),
            partner = Color(0xFF52C9BA),
        )

        /** Fresh green. */
        public val Moss: BedrockAccent = BedrockAccent(
            name = "Moss",
            main = Color(0xFF5CC28A),
            soft = Color(0xFFBDEBD0),
            deep = Color(0xFF184D31),
            onMain = Color(0xFF06281A),
            partner = Color(0xFFC6D25C),
        )

        /** Warm pink. */
        public val Rose: BedrockAccent = BedrockAccent(
            name = "Rose",
            main = Color(0xFFEC6F8E),
            soft = Color(0xFFFFC4D2),
            deep = Color(0xFF6E1F38),
            onMain = Color(0xFF33081A),
            partner = Color(0xFFF2A04B),
        )

        /** Periwinkle. */
        public val Iris: BedrockAccent = BedrockAccent(
            name = "Iris",
            main = Color(0xFF8E8BF0),
            soft = Color(0xFFD4D3FF),
            deep = Color(0xFF302D80),
            onMain = Color(0xFF14123F),
            partner = Color(0xFF4FA6E6),
        )

        /** Golden sand. */
        public val Sand: BedrockAccent = BedrockAccent(
            name = "Sand",
            main = Color(0xFFE3B04F),
            soft = Color(0xFFF7DEA8),
            deep = Color(0xFF684B14),
            onMain = Color(0xFF2E2005),
            partner = Color(0xFFF2884B),
        )

        /** Every preset, in picker order. */
        public val Presets: List<BedrockAccent> = listOf(Ember, Tide, Moss, Rose, Iris, Sand)
    }
}

/**
 * The dark neutral ramp a theme sits on. Four surface steps, each a little lighter, so depth reads
 * from tone alone and no surface ever needs a shadow to separate from its parent.
 */
@Immutable
public data class BedrockNeutrals(
    val name: String,
    /** The page. */
    val background: Color,
    /** Cards and grouped lists on the page. */
    val surface: Color,
    /** Something inside a card: a field, a tile, a nested group. */
    val surfaceRaised: Color,
    /** The highest step: tracks, pressed states, unselected chips. */
    val surfaceHigh: Color,
) {
    public companion object {
        /** Warm graphite. The default. */
        public val Basalt: BedrockNeutrals = BedrockNeutrals(
            name = "Basalt",
            background = Color(0xFF111112),
            surface = Color(0xFF1A1A1C),
            surfaceRaised = Color(0xFF232326),
            surfaceHigh = Color(0xFF2D2D31),
        )

        /** A cooler, blue-leaning graphite. */
        public val Slate: BedrockNeutrals = BedrockNeutrals(
            name = "Slate",
            background = Color(0xFF0E1013),
            surface = Color(0xFF171A1E),
            surfaceRaised = Color(0xFF1F2328),
            surfaceHigh = Color(0xFF292E34),
        )

        /** True black page for OLED screens; surfaces keep their steps. */
        public val Pitch: BedrockNeutrals = BedrockNeutrals(
            name = "Pitch",
            background = Color(0xFF000000),
            surface = Color(0xFF121213),
            surfaceRaised = Color(0xFF1B1B1D),
            surfaceHigh = Color(0xFF262628),
        )

        /** Every preset, in picker order. */
        public val Presets: List<BedrockNeutrals> = listOf(Basalt, Slate, Pitch)
    }
}

/**
 * Every colour a Bedrock screen uses. Build one with [bedrockColors]; read it through
 * `BedrockTheme.colors`.
 */
@Immutable
public data class BedrockColors(
    val accent: BedrockAccent,
    val neutrals: BedrockNeutrals,

    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val surfaceHigh: Color,

    /** Warm off-white: titles, values, icons that matter. */
    val textPrimary: Color,
    /** Subtitles, labels, resting icons. */
    val textSecondary: Color,
    /** Meta text, placeholders, trailing values. */
    val textTertiary: Color,
    val textDisabled: Color,

    /** Solid accent fill. */
    val accentMain: Color,
    /** Pale accent, for text and icons on [accentContainer]. */
    val accentSoft: Color,
    /** Text and icons on [accentMain]. */
    val onAccent: Color,
    /** Quiet accent fill: selected chips, the nav pill, tags. */
    val accentContainer: Color,
    /** A firmer accent fill for selected states that sit on a [surface]. */
    val accentContainerStrong: Color,

    /** The light "ink" pill: a high-contrast call to action that isn't the accent. */
    val ink: Color,
    /** Text on [ink]. */
    val onInk: Color,

    /** Hairline around cards and floating surfaces. */
    val line: Color,
    /** A firmer hairline: focused fields, floating bars. */
    val lineStrong: Color,
    /** 1dp separators between rows. */
    val divider: Color,

    /** Tint laid over frosted glass. */
    val glass: Color,
    /** A heavier glass tint, for sheets and dialogs that hold text. */
    val glassStrong: Color,
    /** The lit top edge of a glass surface. */
    val rimLight: Color,
    /** The shaded bottom edge of a glass surface. */
    val rimShade: Color,
    /** Dims the page behind sheets and dialogs. */
    val scrim: Color,

    val positive: Color,
    val caution: Color,
    val critical: Color,
)

/** The warm off-white all text is drawn from. Never pure white: it glares on dark glass. */
private val Ink = Color(0xFFF3F2EF)

/** Builds the full palette from one [accent] and one neutral ramp. */
public fun bedrockColors(
    accent: BedrockAccent = BedrockAccent.Ember,
    neutrals: BedrockNeutrals = BedrockNeutrals.Basalt,
): BedrockColors = BedrockColors(
    accent = accent,
    neutrals = neutrals,
    background = neutrals.background,
    surface = neutrals.surface,
    surfaceRaised = neutrals.surfaceRaised,
    surfaceHigh = neutrals.surfaceHigh,
    textPrimary = Ink,
    textSecondary = Ink.copy(alpha = 0.64f),
    textTertiary = Ink.copy(alpha = 0.42f),
    textDisabled = Ink.copy(alpha = 0.26f),
    accentMain = accent.main,
    accentSoft = accent.soft,
    onAccent = accent.onMain,
    accentContainer = accent.main.copy(alpha = 0.16f),
    accentContainerStrong = accent.main.copy(alpha = 0.24f),
    ink = Ink,
    onInk = Color(0xFF151517),
    line = Color.White.copy(alpha = 0.06f),
    lineStrong = Color.White.copy(alpha = 0.11f),
    divider = Color.White.copy(alpha = 0.06f),
    glass = neutrals.surface.copy(alpha = 0.58f),
    glassStrong = neutrals.surface.copy(alpha = 0.78f),
    rimLight = Color.White.copy(alpha = 0.16f),
    rimShade = Color.White.copy(alpha = 0.03f),
    scrim = Color.Black.copy(alpha = 0.5f),
    positive = Color(0xFF5CC28A),
    caution = Color(0xFFE3B04F),
    critical = Color(0xFFEE6A5E),
)

internal val LocalBedrockColors: ProvidableCompositionLocal<BedrockColors> =
    staticCompositionLocalOf { bedrockColors() }
