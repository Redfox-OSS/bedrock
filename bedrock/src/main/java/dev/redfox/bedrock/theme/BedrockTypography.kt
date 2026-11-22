package dev.redfox.bedrock.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.redfox.bedrock.R

@OptIn(ExperimentalTextApi::class)
private fun inter(weight: FontWeight) = Font(
    resId = R.font.br_inter_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

/** Inter, from one variable TTF, instantiated at the weights Bedrock uses. */
public val BedrockFontFamily: FontFamily = FontFamily(
    inter(FontWeight.Light),
    inter(FontWeight.Normal),
    inter(FontWeight.Medium),
    inter(FontWeight.SemiBold),
)

/**
 * Inter's character variants that give Bedrock its voice: a tailed `l` and a serifed `I` (so
 * "Il1" never blur together) and the single-storey `a`, which softens long reads.
 */
private const val Voice = "cv05, cv08, cv11"

/** [Voice] plus tabular figures, so changing numbers never shuffle their neighbours. */
private const val Figures = "$Voice, tnum"

/**
 * The Bedrock type scale. Sentence case everywhere (no tracked-out uppercase labels): hierarchy
 * comes from size, weight and the three text colours, never from shouting.
 */
@Immutable
public data class BedrockTypography(
    /** Hero numerals: a balance, a temperature, a time. Light, tight, tabular. */
    val numeral: TextStyle,
    /** Big editorial statements, page heroes. */
    val display: TextStyle,
    /** Page titles. */
    val title: TextStyle,
    /** Card and sheet titles. */
    val headline: TextStyle,
    /** Comfortable reading text. */
    val bodyLarge: TextStyle,
    /** Default body text, subtitles. */
    val body: TextStyle,
    /** Row titles and button labels. */
    val label: TextStyle,
    /** Chips, tags, small buttons. */
    val labelSmall: TextStyle,
    /** Meta text and section headers. */
    val caption: TextStyle,
    /** Numbers in rows and stats: tabular, so columns line up. */
    val figure: TextStyle,
    /** Mid-size numbers inside tiles. */
    val figureLarge: TextStyle,
) {
    public companion object {
        /** The scale on Inter. */
        public val Default: BedrockTypography = bedrockTypography()
    }
}

/** Builds the scale on any [fontFamily]; the sizes, weights and tracking stay Bedrock's. */
public fun bedrockTypography(fontFamily: FontFamily = BedrockFontFamily): BedrockTypography {
    val trim = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
    fun style(weight: FontWeight, size: Int, line: Int, tracking: Double, features: String = Voice) = TextStyle(
        fontFamily = fontFamily,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = line.sp,
        letterSpacing = tracking.em,
        fontFeatureSettings = features,
        lineHeightStyle = trim,
    )
    return BedrockTypography(
        numeral = style(FontWeight.Light, 56, 60, -0.04, Figures),
        display = style(FontWeight.Light, 40, 44, -0.03),
        title = style(FontWeight.Medium, 28, 34, -0.02),
        headline = style(FontWeight.Medium, 19, 24, -0.01),
        bodyLarge = style(FontWeight.Normal, 17, 24, -0.005),
        body = style(FontWeight.Normal, 15, 21, 0.0),
        label = style(FontWeight.Medium, 15, 20, -0.005),
        labelSmall = style(FontWeight.Medium, 13, 18, 0.0),
        caption = style(FontWeight.Normal, 13, 18, 0.0),
        figure = style(FontWeight.Normal, 15, 20, 0.0, Figures),
        figureLarge = style(FontWeight.Light, 32, 36, -0.03, Figures),
    )
}

internal fun materialTypography(t: BedrockTypography): Typography = Typography(
    displayLarge = t.numeral,
    displayMedium = t.display,
    displaySmall = t.display,
    headlineLarge = t.title,
    headlineMedium = t.title,
    headlineSmall = t.headline,
    titleLarge = t.headline,
    titleMedium = t.label,
    titleSmall = t.labelSmall,
    bodyLarge = t.bodyLarge,
    bodyMedium = t.body,
    bodySmall = t.caption,
    labelLarge = t.label,
    labelMedium = t.labelSmall,
    labelSmall = t.caption,
)

internal val LocalBedrockTypography: ProvidableCompositionLocal<BedrockTypography> =
    staticCompositionLocalOf { BedrockTypography.Default }
