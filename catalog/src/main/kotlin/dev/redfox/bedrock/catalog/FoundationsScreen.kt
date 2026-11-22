package dev.redfox.bedrock.catalog

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.redfox.bedrock.components.BrCard
import dev.redfox.bedrock.components.GlassCard
import dev.redfox.bedrock.components.SegmentedControl
import dev.redfox.bedrock.effects.Aura
import dev.redfox.bedrock.effects.GlassBackdrop
import dev.redfox.bedrock.effects.aura
import dev.redfox.bedrock.effects.pressable
import dev.redfox.bedrock.effects.rim
import dev.redfox.bedrock.motion.BrMotion
import dev.redfox.bedrock.theme.BedrockAccent
import dev.redfox.bedrock.theme.BedrockNeutrals
import dev.redfox.bedrock.theme.BedrockTheme
import dev.redfox.bedrock.theme.BrShapes
import dev.redfox.bedrock.theme.BrSpacing

@Composable
internal fun FoundationsScreen() {
    val colors = BedrockTheme.colors
    val type = BedrockTheme.typography
    val choice = LocalThemeChoice.current

    TabPage(title = "Foundations", subtitle = "Tokens every screen is built from. Change the theme here; the whole catalog follows.") {
        demo("Accent", 1, note = "Six mid-bright families. Each has five tones, so no screen ever mixes its own.") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                BedrockAccent.Presets.forEach { a ->
                    Swatch(a, selected = a == choice.accent) { choice.onAccent(a) }
                }
            }
            Text(choice.accent.name, style = type.headline, color = colors.textPrimary, modifier = Modifier.padding(top = BrSpacing.S))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(BrSpacing.S)) {
                Tone("main", choice.accent.main, Modifier.weight(1f))
                Tone("soft", choice.accent.soft, Modifier.weight(1f))
                Tone("deep", choice.accent.deep, Modifier.weight(1f))
                Tone("on", choice.accent.onMain, Modifier.weight(1f))
                Tone("partner", choice.accent.partner, Modifier.weight(1f))
            }
        }

        demo("Neutrals", 2, note = "Four surface steps. Depth comes from tone, so nothing needs a shadow to sit on the page.") {
            SegmentedControl(
                options = BedrockNeutrals.Presets.map { it.name },
                selectedIndex = BedrockNeutrals.Presets.indexOf(choice.neutrals),
                onSelect = { choice.onNeutrals(BedrockNeutrals.Presets[it]) },
            )
            // Nested steps, each concentric with the one around it.
            Box(
                Modifier.fillMaxWidth().clip(BrShapes.XLarge).background(colors.background).border(1.dp, colors.line, BrShapes.XLarge).padding(10.dp)
            ) {
                Column(
                    Modifier.fillMaxWidth().clip(BrShapes.concentric(30.dp, 10.dp)).background(colors.surface).padding(10.dp)
                ) {
                    Column(
                        Modifier.fillMaxWidth().clip(BrShapes.concentric(20.dp, 10.dp)).background(colors.surfaceRaised).padding(10.dp)
                    ) {
                        Box(
                            Modifier.fillMaxWidth().height(44.dp).clip(BrShapes.concentric(10.dp, 4.dp)).background(colors.surfaceHigh),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Text("high", style = type.caption, color = colors.textTertiary, modifier = Modifier.padding(start = 12.dp))
                        }
                        Text("raised", style = type.caption, color = colors.textTertiary, modifier = Modifier.padding(top = 8.dp))
                    }
                    Text("surface", style = type.caption, color = colors.textTertiary, modifier = Modifier.padding(top = 8.dp))
                }
            }
            Text("background", style = type.caption, color = colors.textTertiary)
        }

        demo("Text", 3, note = "One warm white at four strengths. Never pure white: it glares on dark glass.") {
            BrCard(Modifier.fillMaxWidth()) {
                Text("Primary: titles and values", style = type.bodyLarge, color = colors.textPrimary)
                Text("Secondary: subtitles and labels", style = type.bodyLarge, color = colors.textSecondary)
                Text("Tertiary: meta and placeholders", style = type.bodyLarge, color = colors.textTertiary)
                Text("Disabled", style = type.bodyLarge, color = colors.textDisabled)
            }
        }

        demo("Type", 4, note = "Inter with a tailed l, serifed I and single-storey a. Sentence case only; numbers are tabular.") {
            BrCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = BrSpacing.L, vertical = BrSpacing.S)) {
                TypeRow("numeral", "1,284.60", type.numeral)
                TypeRow("display", "Calm, by default", type.display)
                TypeRow("title", "Page title", type.title)
                TypeRow("headline", "Card headline", type.headline)
                TypeRow("bodyLarge", "Illegible 1Il0O becomes legible.", type.bodyLarge)
                TypeRow("body", "Body text for comfortable reading.", type.body)
                TypeRow("label", "Row title, button label", type.label)
                TypeRow("labelSmall", "Chip, tag, small button", type.labelSmall)
                TypeRow("caption", "Meta text, timestamps", type.caption)
                TypeRow("figure", "10:30  16:00  21:30", type.figure)
                TypeRow("figureLarge", "8,214", type.figureLarge, last = true)
            }
        }

        demo("Shape", 5, note = "Inner corners are the outer radius minus the padding, so curves run parallel and never pinch.") {
            Row(horizontalArrangement = Arrangement.spacedBy(BrSpacing.M), verticalAlignment = Alignment.Bottom) {
                RadiusChip("8", 8.dp)
                RadiusChip("12", 12.dp)
                RadiusChip("16", 16.dp)
                RadiusChip("22", 22.dp)
                RadiusChip("30", 30.dp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(BrSpacing.M)) {
                Concentric("Concentric", 30.dp, 10.dp, good = true, modifier = Modifier.weight(1f))
                Concentric("Pinched", 30.dp, 10.dp, good = false, modifier = Modifier.weight(1f))
            }
        }

        demo("Spacing", 6, note = "A 4dp scale. 20 is the page gutter; 40 separates sections.") {
            listOf("4" to 4.dp, "8" to 8.dp, "12" to 12.dp, "16" to 16.dp, "20" to 20.dp, "24" to 24.dp, "32" to 32.dp, "40" to 40.dp).forEach { (label, size) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(label, style = type.figure, color = colors.textTertiary, modifier = Modifier.width(36.dp))
                    Box(Modifier.width(size * 4).height(10.dp).clip(CircleShape).background(colors.accentContainerStrong))
                }
            }
        }

        demo("Aura", 7, note = "Bedrock's colour lives inside surfaces, as light falling on a wall. No glows around anything.") {
            BedrockAccent.Presets.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(BrSpacing.M)) {
                    row.forEach { a ->
                        Box(
                            Modifier
                                .weight(1f)
                                .aspectRatio(0.8f)
                                .clip(BrShapes.Large)
                                .aura(remember(a) { Aura.of(a) })
                                .rim(BrShapes.Large)
                                .pressable(pressedScale = 0.97f, haptic = HapticFeedbackType.SegmentTick) { choice.onAccent(a) },
                            contentAlignment = Alignment.BottomStart,
                        ) {
                            Text(a.name, style = type.labelSmall, color = colors.textPrimary, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
            }
        }

        demo("Glass", 8, note = "A wide 28dp blur, a fine grain so gradients never band, and a rim that catches light along the top.") {
            GlassBackdrop(
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(BrShapes.XLarge),
                backdrop = {
                    Box(Modifier.matchParentSize().aura(remember(choice.accent) { Aura.of(choice.accent) }))
                    Box(Modifier.align(Alignment.CenterStart).padding(start = 30.dp).size(70.dp).clip(CircleShape).background(choice.accent.soft))
                    Box(Modifier.align(Alignment.TopEnd).padding(top = 24.dp, end = 40.dp).size(110.dp, 34.dp).clip(CircleShape).background(choice.accent.partner))
                },
            ) {
                Row(
                    Modifier.matchParentSize().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    GlassCard(Modifier.weight(1f), contentPadding = PaddingValues(14.dp)) {
                        Text("glass", style = type.labelSmall, color = colors.textPrimary)
                        Text("58% surface", style = type.caption, color = colors.textSecondary)
                    }
                    GlassCard(Modifier.weight(1f), tint = colors.glassStrong, contentPadding = PaddingValues(14.dp)) {
                        Text("glassStrong", style = type.labelSmall, color = colors.textPrimary)
                        Text("78%, for text", style = type.caption, color = colors.textSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun Swatch(accent: BedrockAccent, selected: Boolean, onClick: () -> Unit) {
    val colors = BedrockTheme.colors
    val ring by animateFloatAsState(if (selected) 1f else 0f, BrMotion.settle(), label = "swatchRing")
    Box(
        Modifier
            .size(48.dp)
            .pressable(pressedScale = 0.92f, haptic = HapticFeedbackType.SegmentTick, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(48.dp)
                .graphicsLayer { alpha = ring }
                .border(2.dp, colors.textPrimary.copy(alpha = 0.85f), CircleShape)
        )
        Box(
            Modifier
                .graphicsLayer {
                    val s = 1f - 0.16f * ring
                    scaleX = s
                    scaleY = s
                }
                .size(44.dp)
                .clip(CircleShape)
                .background(accent.main)
        )
    }
}

@Composable
private fun Tone(name: String, color: Color, modifier: Modifier) {
    val type = BedrockTheme.typography
    val colors = BedrockTheme.colors
    Column(modifier) {
        Box(Modifier.fillMaxWidth().height(44.dp).clip(BrShapes.Small).background(color).border(1.dp, colors.line, BrShapes.Small))
        Spacer(Modifier.height(6.dp))
        Text(name, style = type.caption, color = colors.textSecondary)
        Text(hex(color), style = type.caption.copy(fontFeatureSettings = "tnum"), color = colors.textTertiary)
    }
}

private fun hex(color: Color): String = "#" + Integer.toHexString(color.toArgb()).uppercase().takeLast(6)

@Composable
private fun TypeRow(name: String, sample: String, style: TextStyle, last: Boolean = false) {
    val colors = BedrockTheme.colors
    Column(Modifier.fillMaxWidth().padding(vertical = BrSpacing.S)) {
        Text(name, style = BedrockTheme.typography.caption, color = colors.textTertiary)
        Text(sample, style = style, color = colors.textPrimary, maxLines = 1)
    }
    if (!last) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.divider))
}

@Composable
private fun RadiusChip(label: String, radius: Dp) {
    val colors = BedrockTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(radius * 1.2f + 32.dp)
                .clip(RoundedCornerShape(radius))
                .background(colors.surfaceRaised)
                .border(1.dp, colors.lineStrong, RoundedCornerShape(radius))
        )
        Spacer(Modifier.height(6.dp))
        Text(label, style = BedrockTheme.typography.figure, color = colors.textTertiary)
    }
}

@Composable
private fun Concentric(label: String, outer: Dp, padding: Dp, good: Boolean, modifier: Modifier) {
    val colors = BedrockTheme.colors
    Column(modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(outer))
                .background(colors.surface)
                .border(1.dp, colors.line, RoundedCornerShape(outer))
                .padding(padding)
        ) {
            val inner = if (good) BrShapes.concentric(outer, padding) else RoundedCornerShape(outer)
            Box(Modifier.fillMaxWidth().height(110.dp).clip(inner).background(if (good) colors.accentContainerStrong else colors.critical.copy(alpha = 0.2f)))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            if (good) "$label: 30 − 10 = 20" else "$label: 30 inside 30",
            style = BedrockTheme.typography.caption,
            color = colors.textTertiary,
        )
    }
}
