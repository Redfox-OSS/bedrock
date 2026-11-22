package dev.redfox.bedrock.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.redfox.bedrock.components.BrCard
import dev.redfox.bedrock.components.BrDivider
import dev.redfox.bedrock.components.ListGroup
import dev.redfox.bedrock.theme.BedrockTheme
import dev.redfox.bedrock.theme.BrSpacing

private val Rules = listOf(
    "Colour lives inside surfaces" to "Auras fill a card like light on a wall. Nothing glows around its edges, so the screen stays quiet even when it's colourful.",
    "Nothing overshoots" to "Every spring is critically damped. Things arrive and rest. If you notice the animation, it's too much.",
    "One loud thing per region" to "One primary or ink button, one aura card, one drifting surface. Everything else steps back.",
    "Hierarchy from weight and tone" to "Sentence case everywhere. No tracked-out uppercase labels; size, weight and three text tones do the work.",
    "Numbers are tabular" to "Figures share one width, so changing values never shuffle their neighbours, and columns line up.",
    "Corners are concentric" to "An inner radius is the outer radius minus the padding. Curves run parallel instead of pinching.",
    "Glass is earned" to "Only things that float over content are frosted: bars, sheets, controls on a photo. Cards on the page are solid.",
    "Write like a person" to "“Nothing planned for Saturday” beats “No data”. Say what will appear, and how to make it appear.",
    "Touch is felt lightly" to "Haptics tick on choices and confirm on commitments. Never a buzz for a scroll.",
)

private val FromObsidian = listOf(
    "Glow halos" to "Aura fills and a lit glass rim",
    "Bouncy, jelly springs" to "Critically damped springs and one calm curve",
    "Labelled tab bar" to "Icons only, a pill you can scrub, a calm compact form",
    "12dp blur, no grain" to "28dp blur, fine grain, lit rim",
    "Uppercase eyebrows" to "Sentence case, weight and tone",
    "Neon-bright accents" to "Mid-bright families with five tones",
    "Shimmer sweep" to "Placeholders that breathe slowly",
)

@Composable
internal fun PrinciplesScreen() {
    val colors = BedrockTheme.colors
    val type = BedrockTheme.typography
    TabPage(title = "Principles", subtitle = "What Bedrock believes, so every app built on it feels calm in the same way.") {
        demo("Nine rules", 1) {
            Rules.forEachIndexed { i, (title, body) ->
                BrCard(Modifier.fillMaxWidth()) {
                    Row {
                        Text(
                            "${i + 1}",
                            style = type.figure,
                            color = colors.accentSoft,
                            modifier = Modifier.width(28.dp),
                        )
                        Column(Modifier.weight(1f)) {
                            Text(title, style = type.label, color = colors.textPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text(body, style = type.body, color = colors.textSecondary)
                        }
                    }
                }
            }
        }
        demo("Coming from Obsidian", 2, note = "Same structure and toolchain, so an app can move across one screen at a time.") {
            ListGroup {
                FromObsidian.forEachIndexed { i, (before, after) ->
                    Column(Modifier.fillMaxWidth().padding(horizontal = BrSpacing.L, vertical = BrSpacing.M)) {
                        Text(before, style = type.caption, color = colors.textTertiary)
                        Text(after, style = type.label, color = colors.textPrimary)
                    }
                    if (i != FromObsidian.lastIndex) BrDivider(inset = BrSpacing.L)
                }
            }
        }
        demo("About", 3) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Bedrock 0.1.0", style = type.label, color = colors.textPrimary)
                Text("Jetpack Compose, Haze glass, Inter. GPL-3.0.", style = type.caption, color = colors.textTertiary)
            }
        }
    }
}
