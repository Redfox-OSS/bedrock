package dev.redfox.bedrock.catalog

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.redfox.bedrock.components.AuraCard
import dev.redfox.bedrock.components.BrButton
import dev.redfox.bedrock.components.BrChip
import dev.redfox.bedrock.components.BrIconButton
import dev.redfox.bedrock.components.BrSlider
import dev.redfox.bedrock.components.ButtonSize
import dev.redfox.bedrock.components.ButtonVariant
import dev.redfox.bedrock.components.GlassCard
import dev.redfox.bedrock.components.IconButtonVariant
import dev.redfox.bedrock.components.IconTile
import dev.redfox.bedrock.components.ListGroup
import dev.redfox.bedrock.components.ListRow
import dev.redfox.bedrock.components.SectionHeader
import dev.redfox.bedrock.components.SliderStyle
import dev.redfox.bedrock.components.StatTile
import dev.redfox.bedrock.effects.Aura
import dev.redfox.bedrock.effects.GlassBackdrop
import dev.redfox.bedrock.effects.aura
import dev.redfox.bedrock.motion.BrDurations
import dev.redfox.bedrock.motion.BrMotion
import dev.redfox.bedrock.motion.RollingNumber
import dev.redfox.bedrock.motion.SwapText
import dev.redfox.bedrock.motion.revealOnEnter
import dev.redfox.bedrock.theme.BedrockAccent
import dev.redfox.bedrock.theme.BedrockTheme
import dev.redfox.bedrock.theme.BrSpacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

private enum class Area(val label: String) { All("All"), Work("Work"), Home("Home"), Health("Health") }

private data class Plan(
    val title: String,
    val detail: String,
    val time: String,
    val area: Area,
    @param:DrawableRes val icon: Int,
    val accent: BedrockAccent,
)

private val Plans = listOf(
    Plan("Design review", "Bedrock tokens, with Maya and Arjun", "10:30", Area.Work, R.drawable.ic_layers, BedrockAccent.Iris),
    Plan("Walk by the river", "40 minutes, phone in the bag", "13:00", Area.Health, R.drawable.ic_leaf, BedrockAccent.Moss),
    Plan("Pay the electricity bill", "₹2,340, due Tuesday", "16:00", Area.Home, R.drawable.ic_card, BedrockAccent.Red),
    Plan("Write the release notes", "Bedrock 0.1", "17:30", Area.Work, R.drawable.ic_calendar, BedrockAccent.Tide),
    Plan("Water the balcony plants", "The basil looks thirsty", "19:00", Area.Home, R.drawable.ic_drop, BedrockAccent.Sand),
    Plan("Evening stretch", "15 minutes before bed", "21:30", Area.Health, R.drawable.ic_heart, BedrockAccent.Ember),
)

/** Focus hours per day this week, Monday first; today (Sunday) grows while a session runs. */
private val Week = floatArrayOf(3.2f, 1.8f, 2.6f, 0.9f, 2.4f, 1.1f)

@Composable
internal fun TodayScreen() {
    val colors = BedrockTheme.colors
    val type = BedrockTheme.typography
    val toasts = LocalToasts.current
    val scope = rememberCoroutineScope()
    val accent = LocalThemeChoice.current.accent

    var todayMinutes by rememberSaveable { mutableIntStateOf(42) }
    var running by rememberSaveable { mutableStateOf(false) }
    var area by rememberSaveable { mutableStateOf(Area.All) }
    var waterMl by rememberSaveable { mutableIntStateOf(1500) }
    var steps by rememberSaveable { mutableIntStateOf(8214) }
    var light by rememberSaveable { mutableFloatStateOf(0.62f) }

    LaunchedEffect(running) {
        while (running) {
            delay(1400)
            todayMinutes += 3
        }
    }
    val weekHours = Week.sum() + todayMinutes / 60f

    TabPage(title = "Today", subtitle = "Sunday, 23 November") {
        // Hero: the one warm surface on the screen.
        item(key = "hero") {
            AuraCard(
                aura = remember(accent) { Aura.of(accent) },
                drift = true,
                modifier = Modifier.padding(horizontal = BrSpacing.Gutter).fillMaxWidth().revealOnEnter(1),
            ) {
                Text("Focus this week", style = type.label, color = colors.textPrimary.copy(alpha = 0.8f))
                Spacer(Modifier.height(BrSpacing.S))
                Row(verticalAlignment = Alignment.Bottom) {
                    RollingNumber(String.format(Locale.US, "%.1f", weekHours), type.numeral, colors.textPrimary)
                    Text(" hours", style = type.bodyLarge, color = colors.textPrimary.copy(alpha = 0.72f), modifier = Modifier.padding(bottom = 10.dp))
                }
                SwapText(
                    if (running) "In a session. Notifications are held." else "2.1 hours more than last week",
                    type.body,
                    colors.textPrimary.copy(alpha = 0.72f),
                )
                Spacer(Modifier.height(BrSpacing.XL))
                WeekBars(Week, todayMinutes / 60f)
                Spacer(Modifier.height(BrSpacing.XL))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(BrSpacing.S)) {
                    BrButton(
                        text = if (running) "End session" else "Start a session",
                        onClick = {
                            running = !running
                            if (running) scope.launch {
                                if (toasts.show("Session started. Do not disturb is on.", actionLabel = "Undo")) running = false
                            }
                        },
                        variant = ButtonVariant.Ink,
                        size = ButtonSize.Medium,
                        modifier = Modifier.weight(1f),
                    )
                    BrIconButton(
                        painterResource(R.drawable.ic_share),
                        contentDescription = "Share this week",
                        onClick = { scope.launch { toasts.show("Weekly summary copied") } },
                        variant = IconButtonVariant.Glass,
                        size = 46.dp,
                    )
                }
            }
        }

        item(key = "filters") {
            Column(Modifier.padding(top = BrSpacing.Section).revealOnEnter(2)) {
                SectionHeader("Up next", Modifier.padding(horizontal = BrSpacing.Gutter))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = BrSpacing.Gutter),
                    horizontalArrangement = Arrangement.spacedBy(BrSpacing.S),
                ) {
                    items(Area.entries.size) { i ->
                        val a = Area.entries[i]
                        BrChip(
                            label = a.label,
                            selected = area == a,
                            onClick = { area = a },
                            count = if (a == Area.All) Plans.size else Plans.count { it.area == a },
                        )
                    }
                }
            }
        }

        item(key = "plans") {
            val shown = Plans.filter { area == Area.All || it.area == area }
            ListGroup(Modifier.padding(horizontal = BrSpacing.Gutter).padding(top = BrSpacing.M).revealOnEnter(3)) {
                Plans.forEach { plan ->
                    AnimatedVisibility(
                        visible = plan in shown,
                        enter = fadeIn(tween(BrDurations.Enter, easing = BrMotion.Calm)) + expandVertically(BrMotion.settle()),
                        exit = fadeOut(tween(BrDurations.Fade)) + shrinkVertically(BrMotion.settle()),
                    ) {
                        ListRow(
                            title = plan.title,
                            subtitle = plan.detail,
                            value = plan.time,
                            divider = plan != shown.lastOrNull(),
                            leading = {
                                IconTile(
                                    painterResource(plan.icon),
                                    tint = plan.accent.soft,
                                    container = plan.accent.main.copy(alpha = 0.18f),
                                )
                            },
                            onClick = { scope.launch { toasts.show("${plan.title} at ${plan.time}") } },
                        )
                    }
                }
            }
        }

        item(key = "stats") {
            Column(
                Modifier.padding(horizontal = BrSpacing.Gutter).padding(top = BrSpacing.Section).revealOnEnter(4),
                verticalArrangement = Arrangement.spacedBy(BrSpacing.M),
            ) {
                SectionHeader("Body", actionLabel = "Reset", onAction = { waterMl = 1500; steps = 8214 })
                Row(horizontalArrangement = Arrangement.spacedBy(BrSpacing.M)) {
                    StatTile(
                        label = "Water",
                        value = String.format(Locale.US, "%.2f", waterMl / 1000f),
                        unit = "litres",
                        caption = "Tap to add a glass",
                        icon = painterResource(R.drawable.ic_drop),
                        accent = BedrockAccent.Tide.main,
                        onClick = { waterMl += 250 },
                        modifier = Modifier.weight(1f),
                    )
                    StatTile(
                        label = "Steps",
                        value = String.format(Locale.US, "%,d", steps),
                        caption = "Tap to walk a bit",
                        icon = painterResource(R.drawable.ic_route),
                        accent = BedrockAccent.Moss.main,
                        onClick = { steps += 137 },
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(BrSpacing.M)) {
                    StatTile(
                        label = "Sleep",
                        value = "7:40",
                        unit = "h",
                        caption = "Steady all week",
                        icon = painterResource(R.drawable.ic_moon),
                        accent = BedrockAccent.Iris.main,
                        modifier = Modifier.weight(1f),
                    )
                    StatTile(
                        label = "Quiet time",
                        value = "42",
                        unit = "min",
                        caption = "Best since June",
                        icon = painterResource(R.drawable.ic_leaf),
                        accent = BedrockAccent.Sand.main,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        item(key = "outside") {
            Column(Modifier.padding(horizontal = BrSpacing.Gutter).padding(top = BrSpacing.Section).revealOnEnter(5)) {
                SectionHeader("Outside")
                GlassBackdrop(
                    modifier = Modifier.fillMaxWidth().clip(dev.redfox.bedrock.theme.BrShapes.XLarge),
                    backdrop = {
                        Box(Modifier.matchParentSize().aura(remember { Aura.blend(BedrockAccent.Tide, BedrockAccent.Iris) }))
                        // A few soft shapes for the glass to blur, like lights through a window.
                        Box(Modifier.align(Alignment.TopEnd).padding(top = 18.dp, end = 30.dp).size(64.dp).clip(CircleShape).background(Color(0xFFFFE3B0)))
                        Box(Modifier.align(Alignment.BottomStart).padding(start = 24.dp, bottom = 40.dp).size(90.dp, 26.dp).clip(CircleShape).background(BedrockAccent.Tide.soft.copy(alpha = 0.7f)))
                    },
                ) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth().padding(BrSpacing.M),
                        shape = dev.redfox.bedrock.theme.BrShapes.concentric(dev.redfox.bedrock.theme.BrShapes.XLargeRadius, BrSpacing.M),
                        contentPadding = PaddingValues(BrSpacing.L),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Lisbon", style = type.label, color = colors.textSecondary)
                                Text("18°", style = type.numeral, color = colors.textPrimary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Clear until 22:00", style = type.body, color = colors.textPrimary)
                                Text("Wind 9 km/h, from the sea", style = type.caption, color = colors.textSecondary)
                            }
                        }
                        Spacer(Modifier.height(BrSpacing.L))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Evening light", style = type.labelSmall, color = colors.textSecondary, modifier = Modifier.weight(1f))
                            Text("${(light * 100).roundToInt()}%", style = type.figure, color = colors.textTertiary)
                        }
                        Spacer(Modifier.height(BrSpacing.S))
                        BrSlider(
                            value = light,
                            onValueChange = { light = it },
                            style = SliderStyle.Thick,
                            icon = painterResource(R.drawable.ic_sun),
                        )
                    }
                }
            }
        }

        item(key = "footer") {
            Text(
                "Every surface on this screen is a Bedrock component.",
                style = type.caption,
                color = colors.textTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = BrSpacing.Section, start = BrSpacing.Gutter, end = BrSpacing.Gutter),
            )
        }
    }
}

/** Seven slim bars, Monday to Sunday. Today's bar is solid and grows with the session. */
@Composable
private fun WeekBars(week: FloatArray, today: Float) {
    val colors = BedrockTheme.colors
    val type = BedrockTheme.typography
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    val max = (week.maxOrNull() ?: 1f).coerceAtLeast(today).coerceAtLeast(1f)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        days.forEachIndexed { i, d ->
            val hours = if (i < week.size) week[i] else today
            val height by animateDpAsState((44.dp * (hours / max)).coerceAtLeast(4.dp), BrMotion.glide(), label = "bar")
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.height(44.dp).width(22.dp), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        Modifier
                            .width(22.dp)
                            .height(height)
                            .clip(CircleShape)
                            .background(if (i == days.lastIndex) colors.ink else colors.ink.copy(alpha = 0.3f))
                    )
                }
                Text(d, style = type.caption, color = colors.textPrimary.copy(alpha = if (i == days.lastIndex) 0.9f else 0.55f))
            }
        }
    }
}
