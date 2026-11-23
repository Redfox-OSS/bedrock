package dev.redfox.bedrock.catalog

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.redfox.bedrock.components.AuraCard
import dev.redfox.bedrock.components.BrAvatar
import dev.redfox.bedrock.components.BrBadge
import dev.redfox.bedrock.components.BrButton
import dev.redfox.bedrock.components.BrCard
import dev.redfox.bedrock.components.BrCheckbox
import dev.redfox.bedrock.components.BrChip
import dev.redfox.bedrock.components.BrDayChip
import dev.redfox.bedrock.components.BrDialog
import dev.redfox.bedrock.components.BrFab
import dev.redfox.bedrock.components.BrMenu
import dev.redfox.bedrock.components.BrMenuItem
import dev.redfox.bedrock.components.BrIconButton
import dev.redfox.bedrock.components.BrProgressBar
import dev.redfox.bedrock.components.BrProgressRing
import dev.redfox.bedrock.components.BrRadio
import dev.redfox.bedrock.components.BrSearchField
import dev.redfox.bedrock.components.BrSheet
import dev.redfox.bedrock.components.BrSlider
import dev.redfox.bedrock.components.BrSpinner
import dev.redfox.bedrock.components.BrSwitch
import dev.redfox.bedrock.components.BrTag
import dev.redfox.bedrock.components.BrTextField
import dev.redfox.bedrock.components.ButtonSize
import dev.redfox.bedrock.components.ButtonVariant
import dev.redfox.bedrock.components.EmptyState
import dev.redfox.bedrock.components.GlassCard
import dev.redfox.bedrock.components.IconButtonVariant
import dev.redfox.bedrock.components.IconTile
import dev.redfox.bedrock.components.ListGroup
import dev.redfox.bedrock.components.ListRow
import dev.redfox.bedrock.components.SegmentedControl
import dev.redfox.bedrock.components.SheetAction
import dev.redfox.bedrock.components.SliderStyle
import dev.redfox.bedrock.components.TagTone
import dev.redfox.bedrock.effects.Aura
import dev.redfox.bedrock.effects.GlassBackdrop
import dev.redfox.bedrock.effects.Placeholder
import dev.redfox.bedrock.effects.aura
import dev.redfox.bedrock.motion.revealOnEnter
import dev.redfox.bedrock.navigation.BrNavBar
import dev.redfox.bedrock.navigation.BrNavItem
import dev.redfox.bedrock.theme.BedrockAccent
import dev.redfox.bedrock.theme.BedrockTheme
import dev.redfox.bedrock.theme.BrShapes
import dev.redfox.bedrock.theme.BrSpacing
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

/** The component pages, in list order. */
internal enum class Category(val title: String, val summary: String, @param:DrawableRes val icon: Int, val accent: BedrockAccent) {
    Buttons("Buttons", "Pill buttons, icon buttons, chips", R.drawable.ic_plus, BedrockAccent.Ember),
    Selection("Selection", "Switch, checkbox, radio, segments", R.drawable.ic_check, BedrockAccent.Moss),
    Inputs("Inputs", "Text fields, search, sliders", R.drawable.ic_search, BedrockAccent.Tide),
    Surfaces("Surfaces", "Cards, glass, aura", R.drawable.ic_layers, BedrockAccent.Iris),
    Lists("Lists", "Grouped rows, avatars, tags", R.drawable.ic_user, BedrockAccent.Red),
    Feedback("Feedback", "Progress, loading, toasts, empty states", R.drawable.ic_bell, BedrockAccent.Sand),
    Overlays("Overlays", "Sheets and dialogs", R.drawable.ic_archive, BedrockAccent.Tide),
    Navigation("Navigation", "A tab bar you can scrub, and its compact form", R.drawable.ic_compass, BedrockAccent.Ember),
}

@Composable
internal fun ComponentsScreen() {
    val navigator = LocalNavigator.current
    TabPage(title = "Components", subtitle = "${Category.entries.size} groups, each on its own page.") {
        item(key = "list") {
            ListGroup(Modifier.padding(horizontal = BrSpacing.Gutter).revealOnEnter(1)) {
                Category.entries.forEachIndexed { i, c ->
                    ListRow(
                        title = c.title,
                        subtitle = c.summary,
                        leading = {
                            IconTile(painterResource(c.icon), tint = c.accent.soft, container = c.accent.main.copy(alpha = 0.18f))
                        },
                        chevron = true,
                        divider = i != Category.entries.lastIndex,
                        onClick = { navigator.push(Destination.Components(c)) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun CategoryScreen(category: Category) {
    when (category) {
        Category.Buttons -> ButtonsPage()
        Category.Selection -> SelectionPage()
        Category.Inputs -> InputsPage()
        Category.Surfaces -> SurfacesPage()
        Category.Lists -> ListsPage()
        Category.Feedback -> FeedbackPage()
        Category.Overlays -> OverlaysPage()
        Category.Navigation -> NavigationPage()
    }
}

@Composable
private fun ButtonsPage() {
    val toasts = LocalToasts.current
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var chip by remember { mutableIntStateOf(0) }
    DetailPage("Buttons", "One loud button per region. The rest step back.") {
        demo("Variants", 1) {
            BrButton("Continue", {}, Modifier.fillMaxWidth())
            BrButton("Add to calendar", {}, Modifier.fillMaxWidth(), variant = ButtonVariant.Ink)
            BrButton("Not now", {}, Modifier.fillMaxWidth(), variant = ButtonVariant.Tonal)
            BrButton("Learn how it works", {}, Modifier.fillMaxWidth(), variant = ButtonVariant.Plain)
            BrButton("Delete account", {}, Modifier.fillMaxWidth(), variant = ButtonVariant.Critical)
        }
        demo("Sizes and icons", 2) {
            Row(horizontalArrangement = Arrangement.spacedBy(BrSpacing.S), verticalAlignment = Alignment.CenterVertically) {
                BrButton("Large", {}, size = ButtonSize.Large, icon = painterResource(R.drawable.ic_plus))
                BrButton("Medium", {}, size = ButtonSize.Medium, variant = ButtonVariant.Tonal)
                BrButton("Small", {}, size = ButtonSize.Small, variant = ButtonVariant.Tonal)
            }
        }
        demo("Loading", 3, note = "The label crossfades to a spinner without the button changing size.") {
            BrButton(
                text = "Send ₹2,340",
                onClick = {
                    loading = true
                    scope.launch {
                        kotlinx.coroutines.delay(2200)
                        loading = false
                        toasts.show("Payment sent to Tata Power")
                    }
                },
                loading = loading,
                modifier = Modifier.fillMaxWidth(),
            )
            BrButton("Disabled", {}, Modifier.fillMaxWidth(), enabled = false)
        }
        demo("Icon buttons", 4) {
            Row(horizontalArrangement = Arrangement.spacedBy(BrSpacing.M)) {
                BrIconButton(painterResource(R.drawable.ic_heart), "Like", {}, variant = IconButtonVariant.Glass)
                BrIconButton(painterResource(R.drawable.ic_share), "Share", {}, variant = IconButtonVariant.Tonal)
                BrIconButton(painterResource(R.drawable.ic_plus), "Add", {}, variant = IconButtonVariant.Primary)
                BrIconButton(painterResource(R.drawable.ic_arrow_up_right), "Open", {}, variant = IconButtonVariant.Ink)
                BrIconButton(painterResource(R.drawable.ic_settings), "Settings", {}, variant = IconButtonVariant.Plain)
            }
        }
        demo("Floating button", 6, note = "The one add action on a list screen. It eases in once, with no spin.") {
            Box(Modifier.fillMaxWidth().height(96.dp), contentAlignment = Alignment.CenterEnd) {
                BrFab(painterResource(R.drawable.ic_plus), "Add", {})
            }
        }
        demo("Chips", 5, note = "The selected chip turns warm white; counts sit quietly beside the label.") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(BrSpacing.S), verticalArrangement = Arrangement.spacedBy(BrSpacing.S)) {
                listOf("All" to 232, "Websites" to 45, "Apps" to 37, "Components" to 150).forEachIndexed { i, (label, count) ->
                    BrChip(label, selected = chip == i, onClick = { chip = i }, count = count)
                }
            }
        }
    }
}

@Composable
private fun SelectionPage() {
    var wifi by remember { mutableStateOf(true) }
    var quiet by remember { mutableStateOf(false) }
    var checks by remember { mutableStateOf(setOf(0)) }
    var radio by remember { mutableIntStateOf(1) }
    var segment by remember { mutableIntStateOf(0) }
    var days by remember { mutableStateOf(setOf(0, 1, 2, 3, 4)) }
    DetailPage("Selection", "Every control glides into place and rests there.") {
        demo("Switch", 1, note = "Hold the thumb: it widens under your finger.") {
            ListGroup {
                ListRow("Wi-Fi calling", trailing = { BrSwitch(wifi, { wifi = it }) }, divider = true)
                ListRow("Quiet hours", subtitle = "22:00 to 07:00", trailing = { BrSwitch(quiet, { quiet = it }) }, divider = true)
                ListRow("Location history", trailing = { BrSwitch(false, {}, enabled = false) })
            }
        }
        demo("Checkbox", 2, note = "The tick draws itself in one stroke.") {
            ListGroup {
                listOf("Milk, oat", "Sourdough", "Lemons", "Basil").forEachIndexed { i, item ->
                    ListRow(
                        item,
                        leading = {
                            BrCheckbox(i in checks, { checks = if (it) checks + i else checks - i })
                        },
                        divider = i != 3,
                        onClick = { checks = if (i in checks) checks - i else checks + i },
                    )
                }
            }
        }
        demo("Radio", 3) {
            ListGroup {
                listOf("Standard, 3 to 5 days", "Express, tomorrow", "Pick up in store").forEachIndexed { i, item ->
                    ListRow(item, trailing = { BrRadio(radio == i, { radio = i }) }, divider = i != 2, onClick = { radio = i })
                }
            }
        }
        demo("Segmented control", 4) {
            SegmentedControl(listOf("Day", "Week", "Month", "Year"), segment, { segment = it })
            SegmentedControl(listOf("List", "Grid"), segment.coerceAtMost(1), { segment = it })
        }
        demo("Day chips", 5, note = "For repeat days. Selected days fill with the accent.") {
            val names = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                names.forEachIndexed { i, name ->
                    BrDayChip(
                        label = name.take(1),
                        selected = i in days,
                        onClick = { days = if (i in days) days - i else days + i },
                        contentDescription = name,
                    )
                }
            }
        }
    }
}

@Composable
private fun InputsPage() {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("12O") }
    var note by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var thin by remember { mutableFloatStateOf(0.4f) }
    var stepped by remember { mutableFloatStateOf(3f) }
    var volume by remember { mutableFloatStateOf(0.55f) }
    val amountError = amount.any { !it.isDigit() }
    DetailPage("Inputs", "Fields warm to the accent while you type in them.") {
        demo("Search", 1) {
            BrSearchField(query, { query = it }, placeholder = "Search designs")
        }
        demo("Text fields", 2) {
            BrTextField(name, { name = it }, label = "Name on the card", placeholder = "As printed")
            BrTextField(
                amount,
                { amount = it },
                label = "Amount",
                placeholder = "0",
                isError = amountError,
                supportingText = if (amountError) "Only digits. Is that a letter O?" else "Up to ₹50,000 a day",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            BrTextField(note, { note = it }, label = "Note", placeholder = "Anything they should know", singleLine = false)
        }
        demo("Sliders", 3, note = "Tap anywhere: the fill glides there instead of jumping.") {
            BrCard(Modifier.fillMaxWidth()) {
                Text("Thin, ${(thin * 100).roundToInt()}%", style = BedrockTheme.typography.labelSmall, color = BedrockTheme.colors.textSecondary)
                BrSlider(thin, { thin = it })
                Spacer(Modifier.height(BrSpacing.M))
                Text("Stepped, ${stepped.roundToInt()} of 5", style = BedrockTheme.typography.labelSmall, color = BedrockTheme.colors.textSecondary)
                BrSlider(stepped, { stepped = it }, valueRange = 0f..5f, steps = 4)
                Spacer(Modifier.height(BrSpacing.M))
                Text("Thick", style = BedrockTheme.typography.labelSmall, color = BedrockTheme.colors.textSecondary)
                Spacer(Modifier.height(BrSpacing.S))
                BrSlider(volume, { volume = it }, style = SliderStyle.Thick, icon = painterResource(R.drawable.ic_volume))
            }
        }
    }
}

@Composable
private fun SurfacesPage() {
    val colors = BedrockTheme.colors
    val type = BedrockTheme.typography
    val accent = LocalThemeChoice.current.accent
    DetailPage("Surfaces", "Tone and a hairline separate surfaces. Colour lives inside them.") {
        demo("Card", 1) {
            BrCard(Modifier.fillMaxWidth()) {
                Text("Card", style = type.headline, color = colors.textPrimary)
                Spacer(Modifier.height(4.dp))
                Text("The everyday surface. A step lighter than the page, with a hairline edge.", style = type.body, color = colors.textSecondary)
            }
            BrCard(onClick = {}) {
                Text("Tappable card", style = type.headline, color = colors.textPrimary)
                Spacer(Modifier.height(4.dp))
                Text("Press it: it eases down and dims slightly, then eases back. No ripple.", style = type.body, color = colors.textSecondary)
            }
        }
        demo("Aura card", 2, note = "The hero surface. At most one per screen, and only one that drifts.") {
            AuraCard(remember(accent) { Aura.of(accent) }, Modifier.fillMaxWidth()) {
                Text("Available", style = type.label, color = colors.textPrimary.copy(alpha = 0.8f))
                Text("€8,214.60", style = type.numeral, color = colors.textPrimary)
                Text("Spend what you earn the day it lands.", style = type.body, color = colors.textPrimary.copy(alpha = 0.72f))
                Spacer(Modifier.height(BrSpacing.XL))
                Row(horizontalArrangement = Arrangement.spacedBy(BrSpacing.S)) {
                    BrButton("Request", {}, Modifier.weight(1f), variant = ButtonVariant.Glass, size = ButtonSize.Medium, icon = painterResource(R.drawable.ic_arrow_down_left))
                    BrButton("Send", {}, Modifier.weight(1f), variant = ButtonVariant.Ink, size = ButtonSize.Medium, icon = painterResource(R.drawable.ic_arrow_up_right))
                }
            }
            AuraCard(remember { Aura.blend(BedrockAccent.Moss, BedrockAccent.Tide) }, Modifier.fillMaxWidth(), shape = BrShapes.Large, contentPadding = PaddingValues(BrSpacing.L)) {
                Text("Blended aura", style = type.headline, color = colors.textPrimary)
                Text("Two families meeting, for things that belong to both.", style = type.body, color = colors.textPrimary.copy(alpha = 0.75f))
            }
        }
        demo("Glass card", 3, note = "Over imagery or colour. Inside a list, stage it on a GlassBackdrop.") {
            GlassBackdrop(
                Modifier.fillMaxWidth().height(180.dp).clip(BrShapes.XLarge),
                backdrop = {
                    Box(Modifier.matchParentSize().aura(remember { Aura.blend(BedrockAccent.Red, BedrockAccent.Sand) }))
                    Box(Modifier.align(Alignment.Center).size(96.dp).clip(BrShapes.Pill).background(BedrockAccent.Sand.soft))
                },
            ) {
                GlassCard(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(12.dp), shape = BrShapes.concentric(BrShapes.XLargeRadius, 12.dp)) {
                    Text("Long table supper", style = type.headline, color = colors.textPrimary)
                    Text("Thursday 19:30, twelve seats", style = type.caption, color = colors.textSecondary)
                }
            }
        }
        demo("Loading placeholders", 4, note = "Placeholders breathe slowly instead of a bright sweep racing across.") {
            BrCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(BrSpacing.M)) {
                    Placeholder(Modifier.size(40.dp), shape = BrShapes.Pill)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Placeholder(Modifier.fillMaxWidth(0.6f).height(12.dp))
                        Placeholder(Modifier.fillMaxWidth(0.35f).height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ListsPage() {
    DetailPage("Lists", "Hairlines start where the text starts, not under the icons.") {
        demo("Grouped list", 1) {
            ListGroup {
                ListRow("Convert currency", leading = { IconTile(painterResource(R.drawable.ic_route)) }, value = "0.4%", divider = true, chevron = true, onClick = {})
                ListRow("Pay another account", leading = { IconTile(painterResource(R.drawable.ic_arrow_up_right)) }, value = "Free", divider = true, chevron = true, onClick = {})
                ListRow("Send an invoice", leading = { IconTile(painterResource(R.drawable.ic_card)) }, trailing = { BrBadge(2) }, chevron = true, onClick = {})
            }
        }
        demo("People", 2, note = "Each name always gets the same tint, so a list of people reads as varied, never random.") {
            ListGroup {
                listOf(
                    Triple("Maya Fernandes", "Shared 3 files", "09:41"),
                    Triple("Arjun Rao", "Can we move to 11?", "Yesterday"),
                    Triple("Halima Ncube", "Photographer, Cape Town", "Mon"),
                    Triple("Leo", "Sent a voice note", "Sun"),
                ).forEachIndexed { i, (n, s, t) ->
                    ListRow(n, subtitle = s, value = t, leading = { BrAvatar(n, size = 34.dp) }, divider = i != 3, onClick = {})
                }
            }
        }
        demo("Tags", 3) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(BrSpacing.S), verticalArrangement = Arrangement.spacedBy(BrSpacing.S)) {
                BrTag("Draft")
                BrTag("New", tone = TagTone.Accent)
                BrTag("Paid", tone = TagTone.Positive)
                BrTag("Due in 3 days", tone = TagTone.Caution)
                BrTag("Overdue", tone = TagTone.Critical)
            }
        }
    }
}

@Composable
private fun FeedbackPage() {
    val toasts = LocalToasts.current
    val scope = rememberCoroutineScope()
    var progress by remember { mutableFloatStateOf(0.35f) }
    val colors = BedrockTheme.colors
    val type = BedrockTheme.typography
    DetailPage("Feedback", "Waiting should feel calm, not urgent.") {
        demo("Progress", 1, note = "Tap a step; the bar and ring glide to it.") {
            BrCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(BrSpacing.L)) {
                    BrProgressRing(progress) {
                        Text("${(progress * 100).roundToInt()}", style = type.figure, color = colors.textPrimary)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(BrSpacing.S)) {
                        Text("Uploading 24 photos", style = type.label, color = colors.textPrimary)
                        BrProgressBar(progress)
                        Text(String.format(Locale.US, "%.1f of 48 MB", 48 * progress), style = type.caption.copy(fontFeatureSettings = "tnum"), color = colors.textTertiary)
                    }
                }
                Spacer(Modifier.height(BrSpacing.L))
                Row(horizontalArrangement = Arrangement.spacedBy(BrSpacing.S)) {
                    listOf(0f, 0.35f, 0.7f, 1f).forEach { p ->
                        BrChip("${(p * 100).roundToInt()}%", selected = progress == p, onClick = { progress = p })
                    }
                }
            }
        }
        demo("Spinner", 2, note = "One slow turn every 1.1 seconds. It never pulses.") {
            Row(horizontalArrangement = Arrangement.spacedBy(BrSpacing.XL), verticalAlignment = Alignment.CenterVertically) {
                BrSpinner(size = 20.dp, strokeWidth = 2.dp)
                BrSpinner()
                BrSpinner(size = 40.dp, strokeWidth = 3.dp, color = colors.textPrimary)
            }
        }
        demo("Toasts", 3, note = "One at a time, rising gently above the content.") {
            Row(horizontalArrangement = Arrangement.spacedBy(BrSpacing.S)) {
                BrButton("Show a notice", { scope.launch { toasts.show("Saved to your library") } }, variant = ButtonVariant.Tonal, size = ButtonSize.Medium)
                BrButton("With undo", {
                    scope.launch {
                        if (toasts.show("Moved 3 notes to the archive", "Undo")) toasts.show("Notes restored")
                    }
                }, variant = ButtonVariant.Tonal, size = ButtonSize.Medium)
            }
        }
        demo("Empty state", 4) {
            BrCard(Modifier.fillMaxWidth()) {
                EmptyState(
                    icon = painterResource(R.drawable.ic_calendar),
                    title = "Nothing planned for Saturday",
                    message = "Plans you add for the weekend will show up here.",
                    actionLabel = "Add a plan",
                )
            }
        }
    }
}

@Composable
private fun OverlaysPage() {
    var sheet by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf(false) }
    val toasts = LocalToasts.current
    val scope = rememberCoroutineScope()
    var menu by remember { mutableStateOf(false) }
    var sort by remember { mutableIntStateOf(0) }
    Box(Modifier.fillMaxSize()) {
        DetailPage("Overlays", "Sheets and dialogs open in their own window, above everything, including a nav bar.") {
            demo("Sheet", 1, note = "Pull it down to dismiss, or tap outside.") {
                BrButton("Open a sheet", { sheet = true }, Modifier.fillMaxWidth(), variant = ButtonVariant.Tonal)
            }
            demo("Dialog", 2) {
                BrButton("Delete a note", { dialog = true }, Modifier.fillMaxWidth(), variant = ButtonVariant.Tonal)
            }
            demo("Menu", 3, note = "Actions behind a more button. The selected choice carries a check.") {
                Box {
                    BrButton("Sort: ${listOf("Time", "Name", "Group")[sort]}", { menu = true }, variant = ButtonVariant.Tonal, size = ButtonSize.Medium)
                    BrMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        listOf("Time", "Name", "Group").forEachIndexed { i, label ->
                            BrMenuItem(label, { sort = i; menu = false }, selected = sort == i)
                        }
                        BrMenuItem("Delete all", { menu = false }, icon = painterResource(R.drawable.ic_trash), destructive = true)
                    }
                }
            }
        }
        BrSheet(visible = sheet, onDismiss = { sheet = false }, title = "Weekly summary") {
            SheetAction("Share", { sheet = false }, icon = painterResource(R.drawable.ic_share))
            SheetAction("Add to calendar", { sheet = false }, icon = painterResource(R.drawable.ic_calendar))
            SheetAction("Archive", { sheet = false }, icon = painterResource(R.drawable.ic_archive))
            SheetAction("Delete", { sheet = false; dialog = true }, icon = painterResource(R.drawable.ic_trash), destructive = true)
        }
        BrDialog(
            visible = dialog,
            onDismiss = { dialog = false },
            title = "Delete this note?",
            message = "“Ideas for the Lisbon trip” will be removed from all your devices. This can’t be undone.",
            confirmLabel = "Delete",
            onConfirm = {
                dialog = false
                scope.launch { toasts.show("Note deleted") }
            },
            destructive = true,
        )
    }
}

@Composable
private fun NavigationPage() {
    var selected by remember { mutableIntStateOf(0) }
    val colors = BedrockTheme.colors
    val type = BedrockTheme.typography
    val tabs = listOf(
        BrNavItem(R.drawable.ic_today, "Home"),
        BrNavItem(R.drawable.ic_calendar, "Calendar"),
        BrNavItem(R.drawable.ic_bell, "Inbox", badge = 3),
        BrNavItem(R.drawable.ic_user, "Profile"),
    )
    DetailPage("Navigation", "Icons only. The pill is the label.") {
        demo("Tab bar", 1, note = "Tap a tab, or press and hold anywhere on the bar and slide: the pill lifts into a lens that follows your finger. Let go to choose.") {
            GlassBackdrop(
                Modifier.fillMaxWidth().clip(BrShapes.XLarge).consumeWindowInsets(WindowInsets.navigationBars),
                backdrop = {
                    Box(Modifier.matchParentSize().aura(remember { Aura.blend(BedrockAccent.Iris, BedrockAccent.Red) }))
                },
            ) {
                Column(Modifier.fillMaxWidth().padding(top = BrSpacing.XL)) {
                    Text(
                        tabs[selected].label,
                        style = type.display,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(horizontal = BrSpacing.XL),
                    )
                    Spacer(Modifier.height(BrSpacing.XXL))
                    BrNavBar(tabs, selected, { selected = it }, Modifier.padding(bottom = 4.dp))
                }
            }
        }
        demo("With an accessory", 2, note = "A separate round button for the one action that isn't a place: search, or compose.") {
            GlassBackdrop(
                Modifier.fillMaxWidth().clip(BrShapes.XLarge).consumeWindowInsets(WindowInsets.navigationBars),
                backdrop = {
                    Box(Modifier.matchParentSize().aura(remember { Aura.blend(BedrockAccent.Tide, BedrockAccent.Moss) }))
                },
            ) {
                BrNavBar(
                    tabs.take(3),
                    selected.coerceAtMost(2),
                    { selected = it },
                    Modifier.padding(top = BrSpacing.XL, bottom = 4.dp),
                    accessory = BrNavItem(R.drawable.ic_search, "Search"),
                )
            }
        }
        demo("Compact form", 3, note = "Scroll any tab toward its end: the bar eases into a slimmer, lower form while you read, and eases back when you scroll up. Here it's toggled by hand.") {
            var compact by remember { mutableStateOf(false) }
            val compactState = remember { mutableStateOf(false) }
            compactState.value = compact
            GlassBackdrop(
                Modifier.fillMaxWidth().clip(BrShapes.XLarge).consumeWindowInsets(WindowInsets.navigationBars),
                backdrop = {
                    Box(Modifier.matchParentSize().aura(remember { Aura.blend(BedrockAccent.Sand, BedrockAccent.Red) }))
                },
            ) {
                BrNavBar(tabs, selected, { selected = it }, Modifier.padding(top = BrSpacing.XL, bottom = 4.dp), compact = compactState)
            }
            SegmentedControl(listOf("Full", "Compact"), if (compact) 1 else 0, { compact = it == 1 })
        }
        demo("Push and pop", 4, note = "Pages glide in a short way while the old one drifts back: a soft parallax, never a slide.") {
            val navigator = LocalNavigator.current
            BrButton("Push a page", { navigator.push(Destination.PushedDemo(1)) }, Modifier.fillMaxWidth(), variant = ButtonVariant.Tonal)
        }
    }
}

@Composable
internal fun PushedDemoScreen(depth: Int) {
    val navigator = LocalNavigator.current
    val colors = BedrockTheme.colors
    val type = BedrockTheme.typography
    DetailPage("Page $depth", "Swipe from the edge to go back; the page follows your finger.") {
        demo("Deeper", 1) {
            BrCard(Modifier.fillMaxWidth()) {
                Text(
                    "Each push travels 1/14 of the screen on a slow, critically damped spring while the page beneath drifts 1/40 the other way.",
                    style = type.body,
                    color = colors.textSecondary,
                )
            }
            BrButton("Go deeper", { navigator.push(Destination.PushedDemo(depth + 1)) }, Modifier.fillMaxWidth())
            BrButton("Back", { navigator.back() }, Modifier.fillMaxWidth(), variant = ButtonVariant.Tonal)
        }
    }
}
