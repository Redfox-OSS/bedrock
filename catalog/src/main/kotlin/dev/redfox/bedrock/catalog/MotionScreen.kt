package dev.redfox.bedrock.catalog

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import dev.redfox.bedrock.components.BrButton
import dev.redfox.bedrock.components.BrCard
import dev.redfox.bedrock.components.BrIconButton
import dev.redfox.bedrock.components.ButtonSize
import dev.redfox.bedrock.components.ButtonVariant
import dev.redfox.bedrock.components.IconButtonVariant
import dev.redfox.bedrock.components.IconTile
import dev.redfox.bedrock.components.ListGroup
import dev.redfox.bedrock.components.ListRow
import dev.redfox.bedrock.motion.BrMotion
import dev.redfox.bedrock.motion.BrTransitions
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.mutableStateOf
import dev.redfox.bedrock.motion.RevealScope
import dev.redfox.bedrock.motion.RollingNumber
import dev.redfox.bedrock.motion.SwapText
import dev.redfox.bedrock.motion.revealOnEnter
import dev.redfox.bedrock.theme.BedrockAccent
import dev.redfox.bedrock.theme.BedrockTheme
import dev.redfox.bedrock.theme.BrSpacing
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.compose.ui.res.painterResource

private class SpringDemo(val name: String, val detail: String, val spec: AnimationSpec<Float>, val color: Color)

@Composable
internal fun MotionScreen() {
    val colors = BedrockTheme.colors
    val type = BedrockTheme.typography
    val scope = rememberCoroutineScope()
    val accent = LocalThemeChoice.current.accent

    val springs = listOf(
        SpringDemo("quick", "1.0 damping, 520 stiffness: presses, toggles", BrMotion.quick(), accent.main),
        SpringDemo("settle", "1.0 damping, 220: pills, indicators, layout", BrMotion.settle(), accent.main),
        SpringDemo("glide", "1.0 damping, 110: sheets and pages", BrMotion.glide(), accent.main),
        SpringDemo("Obsidian bouncy", "0.55 damping, 380, for comparison", spring(dampingRatio = 0.55f, stiffness = 380f), colors.textTertiary),
    )
    val dots = remember { springs.map { Animatable(0f) } }
    var side by remember { mutableIntStateOf(0) }
    var replay by remember { mutableIntStateOf(0) }
    var amount by remember { mutableIntStateOf(1284) }
    var status by remember { mutableIntStateOf(0) }
    val statuses = listOf("Draft saved", "Syncing", "Up to date", "Shared with 3 people")

    TabPage(title = "Motion", subtitle = "Nothing overshoots. Everything arrives and rests.") {
        demo("Springs", 1, note = "Every Bedrock spring is critically damped. Run them against Obsidian's bounce.") {
            BrCard(Modifier.fillMaxWidth()) {
                springs.forEachIndexed { i, demo ->
                    Text(demo.name, style = type.labelSmall, color = colors.textPrimary)
                    Text(demo.detail, style = type.caption, color = colors.textTertiary)
                    Spacer(Modifier.height(BrSpacing.S))
                    Track(dots[i], demo.color)
                    if (i != springs.lastIndex) Spacer(Modifier.height(BrSpacing.L))
                }
                Spacer(Modifier.height(BrSpacing.L))
                BrButton(
                    "Run",
                    {
                        side = 1 - side
                        springs.forEachIndexed { i, demo -> scope.launch { dots[i].animateTo(side.toFloat(), demo.spec) } }
                    },
                    Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Tonal,
                    size = ButtonSize.Medium,
                )
            }
        }

        demo("The curve", 2, note = "Timed motion uses one curve: fast to arrive, long to settle (cubic 0.22, 1, 0.36, 1).") {
            BrCard(Modifier.fillMaxWidth()) {
                CurvePlot(Modifier.fillMaxWidth().height(140.dp), accent.main)
            }
        }

        demo("Disclosure", 3, note = "Opening content: the space settles open, then the content fades in. Closing reverses it.") {
            var open by remember { mutableStateOf(false) }
            BrCard(Modifier.fillMaxWidth(), onClick = { open = !open }) {
                Text(if (open) "Hide details" else "Show details", style = type.label, color = colors.textPrimary)
                AnimatedVisibility(open, enter = BrTransitions.expandIn, exit = BrTransitions.collapseOut) {
                    Text(
                        "Uses BrTransitions.expandIn and collapseOut. For inline content growing from the start edge, revealStart and concealStart.",
                        style = type.body,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(top = BrSpacing.S),
                    )
                }
            }
        }

        demo("Entrances", 3, note = "Content rises 14dp while it fades in, a beat after its neighbour. No scale, no pop.") {
            key(replay) {
                RevealScope {
                    ListGroup {
                        listOf(
                            Triple("Morning pages", R.drawable.ic_sun, BedrockAccent.Sand),
                            Triple("Inbox zero", R.drawable.ic_bell, BedrockAccent.Tide),
                            Triple("Long walk", R.drawable.ic_leaf, BedrockAccent.Moss),
                            Triple("Early night", R.drawable.ic_moon, BedrockAccent.Iris),
                        ).forEachIndexed { i, (t, icon, a) ->
                            ListRow(
                                t,
                                Modifier.revealOnEnter(i),
                                leading = { IconTile(painterResource(icon), tint = a.soft, container = a.main.copy(alpha = 0.18f)) },
                                divider = i != 3,
                            )
                        }
                    }
                }
            }
            BrButton("Replay", { replay++ }, Modifier.fillMaxWidth(), variant = ButtonVariant.Tonal, size = ButtonSize.Medium)
        }

        demo("Numbers and words", 4, note = "Only the digits that change roll. Labels lift away as the next one rises.") {
            BrCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
                        Text("€", style = type.figureLarge, color = colors.textTertiary)
                        RollingNumber(String.format(Locale.US, "%,d", amount), type.numeral, colors.textPrimary)
                    }
                    BrIconButton(painterResource(R.drawable.ic_arrow_down_left), "Less", { amount = (amount - 37).coerceAtLeast(0) }, variant = IconButtonVariant.Tonal)
                    Spacer(Modifier.size(BrSpacing.S))
                    BrIconButton(painterResource(R.drawable.ic_plus), "More", { amount += 128 }, variant = IconButtonVariant.Tonal)
                }
                Spacer(Modifier.height(BrSpacing.L))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(colors.positive))
                    Spacer(Modifier.size(BrSpacing.S))
                    SwapText(statuses[status], type.label, colors.textPrimary, Modifier.weight(1f))
                    BrButton("Next", { status = (status + 1) % statuses.size }, variant = ButtonVariant.Tonal, size = ButtonSize.Small)
                }
            }
        }

        demo("Press", 5, note = "Hold each one. They ease down and dim a touch; on release they ease back without springing past.") {
            Row(horizontalArrangement = Arrangement.spacedBy(BrSpacing.M)) {
                BrCard(Modifier.weight(1f), onClick = {}) {
                    Text("Card", style = type.label, color = colors.textPrimary)
                    Text("0.98", style = type.figure, color = colors.textTertiary)
                }
                BrCard(Modifier.weight(1f), onClick = {}) {
                    Text("Row", style = type.label, color = colors.textPrimary)
                    Text("0.99", style = type.figure, color = colors.textTertiary)
                }
            }
            BrButton("Button, 0.97", {}, Modifier.fillMaxWidth())
        }

        demo("Transitions", 6, note = "Push a page to feel the glide and the soft parallax behind it.") {
            val navigator = LocalNavigator.current
            BrButton("Push a page", { navigator.push(Destination.PushedDemo(1)) }, Modifier.fillMaxWidth(), variant = ButtonVariant.Tonal, size = ButtonSize.Medium)
        }
    }
}

@Composable
private fun Track(dot: Animatable<Float, *>, color: Color) {
    val colors = BedrockTheme.colors
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(CircleShape)
            .background(colors.surfaceRaised)
            .padding(4.dp)
    ) {
        val travel = maxWidth - 20.dp
        Box(
            Modifier
                .size(20.dp)
                .graphicsLayer { translationX = travel.toPx() * dot.value }
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
private fun CurvePlot(modifier: Modifier, color: Color) {
    val colors = BedrockTheme.colors
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, androidx.compose.animation.core.tween(1400, easing = androidx.compose.animation.core.LinearEasing))
    }
    val grid = colors.divider
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        for (i in 0..4) {
            val y = h * i / 4f
            drawLine(grid, Offset(0f, y), Offset(w, y), 1.dp.toPx())
        }
        val path = Path()
        val steps = 80
        val limit = (steps * progress.value).toInt()
        for (s in 0..limit) {
            val t = s / steps.toFloat()
            val v = BrMotion.Calm.transform(t)
            val x = t * w
            val y = h - v * h
            if (s == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
    }
}
