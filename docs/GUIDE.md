# Bedrock guide

The full reference: every token, effect, motion spec and component, and the rules for using them.
Bedrock is a Jetpack Compose design system for Android: dark, frosted, mid-bright colour that lives
*inside* surfaces, and motion that arrives and rests instead of bouncing.

- **Artifact:** `dev.redfox:bedrock` · **package:** `dev.redfox.bedrock` · **min SDK** 31
- **Depends on (exposed as `api`):** Compose BOM 2026.02.01, Material 3, [Haze](https://github.com/chrisbanes/haze) 1.7.2
- **Font:** Inter (variable TTF, bundled; OFL licence in `assets/licenses/Inter-OFL.txt`)
- **Catalog app:** `catalog/`, every token, component and motion on a phone

```
bedrock/src/main/java/dev/redfox/bedrock/
├── theme/       BedrockTheme, BedrockColors (+ BedrockAccent / BedrockNeutrals presets), BedrockTypography, BrShapes, BrSpacing
├── effects/     glass, glassSource, rim, GlassBackdrop, aura + Aura presets, floatShadow, pressable, breathe, Placeholder
├── motion/      BrMotion springs and curves, BrDurations, RevealScope / revealOnEnter, BrTransitions, SwapText, RollingNumber
├── components/  buttons, chips, switch, checkbox, radio, segmented control, cards, lists, fields, slider,
│                progress, spinner, toast, badge, tag, avatar, stat tile, empty state, sheet, dialog, page + top bar
└── navigation/  BrNavBar (icon-only, pill selection, hold and scrub), brNavClearance()
```

---

## 1. Principles

1. **Colour lives inside surfaces.** Auras fill a card like light on a wall. Nothing glows around its edges.
2. **Nothing overshoots.** Every spring is critically damped (damping ratio 1). Things arrive and rest.
3. **One loud thing per region.** One Primary or Ink button, one aura card, one drifting surface.
4. **Hierarchy from weight and tone.** Sentence case everywhere; no tracked-out uppercase labels.
5. **Numbers are tabular.** Figures share a width, so changing values never shuffle their neighbours.
6. **Corners are concentric.** Inner radius = outer radius − padding (`BrShapes.concentric`).
7. **Glass is earned.** Only things floating over content are frosted: bars, sheets, controls over imagery.
8. **Write like a person.** "Nothing planned for Saturday", not "No data".
9. **Touch is felt lightly.** Ticks on choices, confirms on commitments, never a buzz for a scroll.

## 2. Setup

```kotlin
setContent {
    BedrockTheme(accent = BedrockAccent.Ember, neutrals = BedrockNeutrals.Basalt) {
        val hazeState = rememberHazeState()                       // dev.chrisbanes.haze
        CompositionLocalProvider(LocalHazeState provides hazeState) {
            Box(Modifier.fillMaxSize().background(BedrockTheme.colors.background)) {
                AppContent()                                       // BrPage already marks its list as glassSource()
                BrNavBar(items, selectedIndex, onSelect, Modifier.align(Alignment.BottomCenter))
            }
        }
    }
}
```

Read tokens from `BedrockTheme.colors` and `BedrockTheme.typography`, never `MaterialTheme`. A matching
Material 3 scheme is installed underneath so stock components left in an app sit in the same palette.

---

## 3. Colour

A theme is **one accent** on **one neutral ramp**; `bedrockColors()` derives everything else.

### Accents (`BedrockAccent.Presets`)

Mid-bright: lively enough to carry a screen, never neon. Each family has five tones.

| Accent | `main` | `soft` | `deep` | `onMain` | `partner` |
|---|---|---|---|---|---|
| Ember (default) | `#F2884B` | `#FFCBA8` | `#7A3212` | `#2B1205` | `#E8607A` |
| Tide | `#4FA6E6` | `#B6DCF7` | `#123F66` | `#03203A` | `#52C9BA` |
| Moss | `#5CC28A` | `#BDEBD0` | `#184D31` | `#06281A` | `#C6D25C` |
| Rose | `#EC6F8E` | `#FFC4D2` | `#6E1F38` | `#33081A` | `#F2A04B` |
| Iris | `#8E8BF0` | `#D4D3FF` | `#302D80` | `#14123F` | `#4FA6E6` |
| Sand | `#E3B04F` | `#F7DEA8` | `#684B14` | `#2E2005` | `#F2884B` |

`main` for solid fills · `soft` for text and icons on an accent container · `deep` is an aura's floor ·
`onMain` for text on `main` · `partner` is the neighbouring hue an aura blends toward.

### Neutrals (`BedrockNeutrals.Presets`)

| Ramp | background | surface | surfaceRaised | surfaceHigh |
|---|---|---|---|---|
| Basalt (warm graphite, default) | `#111112` | `#1A1A1C` | `#232326` | `#2D2D31` |
| Slate (cool) | `#0E1013` | `#171A1E` | `#1F2328` | `#292E34` |
| Pitch (OLED) | `#000000` | `#121213` | `#1B1B1D` | `#262628` |

### Derived tokens

| Token | Value | Use |
|---|---|---|
| `textPrimary / Secondary / Tertiary / Disabled` | `#F3F2EF` at 100 / 64 / 42 / 26% | one warm white, four strengths. Never pure white |
| `accentContainer` / `…Strong` | main @ 16% / 24% | selected chips, the nav pill, tags |
| `ink` / `onInk` | `#F3F2EF` / `#151517` | the light high-contrast pill (Construct-style CTA) |
| `line` / `lineStrong` / `divider` | white @ 6 / 11 / 6% | hairlines |
| `glass` / `glassStrong` | surface @ 58% / 78% | tints over frosted glass; strong for sheets and dialogs |
| `rimLight` / `rimShade` | white @ 16% / 3% | the lit top and shaded bottom of a glass edge |
| `scrim` | black @ 50% | behind sheets and dialogs |
| `positive / caution / critical` | `#5CC28A / #E3B04F / #EE6A5E` | status |

---

## 4. Typography

Inter with three character variants on (`cv05` tailed l, `cv08` serifed I, `cv11` single-storey a), so
"Il1" never blur and long reads soften. Numbers use tabular figures. **Sentence case only.**

| Style | Weight | Size / line | Tracking | Use |
|---|---|---|---|---|
| `numeral` | Light | 56 / 60 | −0.04em, tnum | hero numbers |
| `display` | Light | 40 / 44 | −0.03em | editorial statements |
| `title` | Medium | 28 / 34 | −0.02em | page titles |
| `headline` | Medium | 19 / 24 | −0.01em | card and sheet titles |
| `bodyLarge` | Regular | 17 / 24 | — | comfortable reading, fields |
| `body` | Regular | 15 / 21 | — | default body, subtitles |
| `label` | Medium | 15 / 20 | — | row titles, buttons |
| `labelSmall` | Medium | 13 / 18 | — | chips, tags, small buttons |
| `caption` | Regular | 13 / 18 | — | meta, section notes |
| `figure` | Regular | 15 / 20 | tnum | numbers in rows |
| `figureLarge` | Light | 32 / 36 | −0.03em, tnum | numbers in tiles |

Swap the face with `bedrockTypography(fontFamily = …)`; sizes, weights and tracking stay Bedrock's.

## 5. Shape and spacing

| `BrShapes` | Radius | | `BrSpacing` | dp |
|---|---|---|---|---|
| `XSmall` | 8 | | `XXS / XS / S` | 2 / 4 / 8 |
| `Small` | 12 | | `M / L` | 12 / 16 |
| `Medium` (tiles, fields) | 16 | | `Gutter` | 20, every page's edge |
| `Large` (cards) | 22 | | `XL / XXL` | 24 / 32 |
| `XLarge` (hero cards, sheets, dialogs) | 30 | | `Section` | 40, between sections |
| `Pill` | full | | | |

`BrShapes.concentric(outerRadius, padding)` gives the inner shape for anything inset in a rounded parent.
Tap targets ≥ 44dp.

---

## 6. Glass

`Modifier.glass(shape, tint, blurRadius = 28.dp, rim = true)`: a wide, soft blur with a fine grain (so
gradients never band) and a **rim**, a 1dp hairline brighter along the top that fades toward the bottom.
The rim is what makes Bedrock glass look made rather than filtered.

- Mark scrolling content with `Modifier.glassSource()`; `BrPage` does it for you.
- A glass surface is a **sibling** of its source, never inside it.
- For glass *inside* scrolling content (a frosted card over a photo in a feed), stage it on
  `GlassBackdrop(backdrop = { … }) { GlassCard(…) }`, which blurs its own backdrop independently.
- Without a `LocalHazeState`, `glass` falls back to a near-opaque surface.

## 7. Aura

`Modifier.aura(Aura, drift = false)` paints a colour field: a `base` tone with soft radial lights over
it, plus a `shade` toward the bottom so text stays readable.

- `Aura.of(accent)`: the accent's deep tone lit by its main hue and partner.
- `Aura.blend(a, b)`: two families meeting.
- `drift = true` wanders the lights a few percent over 16s, at ~20fps (it moves under a pixel per
  frame, so it looks the same as 60fps at a third of the redraws). **At most one drifting surface per
  screen**: under glass, every drift frame re-blurs the glass above it.
- No glows. The only shadow in Bedrock is `floatShadow`, neutral black, for floating surfaces.

---

## 8. Motion

### Springs: `BrMotion`

All critically damped. Springs are used for anything a finger can interrupt, because they pick up
from the current velocity without a jolt.

| Spec | Damping | Stiffness | For |
|---|---|---|---|
| `quick()` | 1.0 | 520 | press, toggles, colour |
| `settle()` | 1.0 | 220 | pills, indicators, layout |
| `glide()` | 1.0 | 110 | sheets, pages, large surfaces |

### Curves and durations

`BrMotion.Calm` = cubic (0.22, 1, 0.36, 1): fast to arrive, long to settle. `BrMotion.Soft` for loops.

| `BrDurations` | ms | For |
|---|---|---|
| `Fade` | 180 | something leaving |
| `Swap` | 280 | labels, icons, numbers |
| `Enter` | 440 | screens, revealed content |
| `Linger` | 640 | quiet background fades |
| `Ambient` | 16000 | one lap of aura drift |

Stagger: 45ms per sibling, capped at index 8.

### Building blocks

- `RevealScope(enabled)` + `Modifier.revealOnEnter(index)`: rise 14dp while fading in, no scale. Only
  plays in a screen's first 700ms, so scrolling never replays it; pass `enabled = false` on pop.
- `BrTransitions.push() / pop() / predictivePop() / tab(forward) / sharedBounds`: the new screen glides
  1/14 of the width while the old drifts 1/40 the other way.
- `RevealColumn { }`: a `Column` whose children enter in turn, without numbering them.
- `BrTransitions.expandIn / collapseOut` (vertical disclosure), `revealStart / concealStart` (inline
  content from the start edge), `swap` (one set of controls replacing another).
- `SwapText`: old label lifts away as the new one rises.
- `RollingNumber`: a slow odometer; only changed digits roll, up when growing.
- `Modifier.pressable { }`: eases to `pressedScale` and dims 12% while held, no overshoot on release.
  Rows 0.99, cards 0.98, buttons 0.97, chips and icon buttons 0.94.

---

## 9. Components

| Component | Notes |
|---|---|
| `BrButton(text, onClick, variant, size, icon, enabled, loading)` | pill; `Primary`, `Ink`, `Tonal`, `Glass`, `Plain`, `Critical`; `Large 54 / Medium 46 / Small 36`; `loading` crossfades to a spinner without resizing |
| `BrIconButton(icon, contentDescription, onClick, variant)` | 44dp round; `Glass`, `Tonal`, `Plain`, `Ink`, `Primary` |
| `BrChip(label, selected, onClick, count, icon)` | selected turns `ink`; count trails quietly |
| `BrDayChip(label, selected, onClick, contentDescription)` | 40dp round day toggle for repeat pickers; selected fills with the accent |
| `BrFab(icon, contentDescription, onClick)` | 56dp rounded square in the accent on a float shadow; eases in once, no spin |
| `BrSwitch(checked, onCheckedChange)` | 50×30; thumb glides on `settle`, widens 6dp while held |
| `BrCheckbox(checked, onCheckedChange)` | 32dp; the tick draws itself in one stroke |
| `BrRadio(selected, onClick)` | 32dp; dot grows into the accent ring |
| `SegmentedControl(options, selectedIndex, onSelect)` | lighter pill with a rim glides under the choice |
| `BrCard`, `GlassCard`, `AuraCard` | solid with hairline · frosted with rim · colour field with rim |
| `IconTile`, `BrDivider`, `SectionHeader` | building blocks; section headers are sentence case |
| `ListGroup { ListRow(…) }` | grouped rows; dividers start exactly where the text starts |
| `BrTextField(…)`, `BrSearchField(…)` | raised field whose edge warms to the accent on focus; frosted search pill |
| `BrSlider(value, onValueChange, valueRange, steps, style)` | `Thin` (hairline + thumb) or `Thick` (filling pill with icon); tap glides there |
| `BrProgressBar`, `BrProgressRing`, `BrSpinner` | values glide; the spinner turns slowly with a fading tail and never pulses |
| `Placeholder`, `Modifier.breathe()` | loading content breathes instead of shimmering |
| `BrToastState.show(message, actionLabel)` + `BrToastHost` | one at a time, frosted pill rising gently |
| `BrBadge`, `BrTag(text, tone)`, `BrAvatar(name)` | the avatar tint is chosen from the name, so a person always keeps their colour |
| `StatTile(label, value, unit, caption, icon, accent)` | metric card; value is a `RollingNumber` |
| `EmptyState(icon, title, message, actionLabel)` | staggered, plainly worded |
| `BrSheet(visible, onDismiss, title) { }` + `SheetAction` | in its own window (covers a nav bar); glides up; pull down past 25% or flick to dismiss; back dismisses |
| `BrDialog(visible, …, destructive)` / `BrDialogFrame(visible, onDismiss) { }` | in its own window; eases in from 96%; destructive confirms in the critical tone. Keep them composed and drive `visible`, so exits play |
| `BrMenu(expanded, onDismissRequest) { BrMenuItem(text, onClick, icon, selected, destructive) }` | anchored floating menu on a raised surface with a hairline; the selected item carries a check |
| `BrPage(title, subtitle, onBack, actions) { lazy items }` | large title in the list; a glass `BrTopBar` fades in with a small centred title as you scroll |
| `BrTopBar(title, collapse, onBack, actions)` + `BrLargeTitle(title, collapse)` + `rememberTopBarCollapse(listState \| scrollState)` + `topBarClearance()` | the same page built by hand, for screens with their own scrolling; the bar's side slots size to their content and the small title stays centred |
| `BrNavBar(items, selectedIndex, onSelect, accessory, compact)` | see below; `rememberCompactNavState` drives `compact` from scrolling |

### The nav bar

`BrNavBar` is a frosted capsule of icons, **no labels**; `BrNavItem.label` is for screen readers only.
The selected tab sits in a soft accent **pill** that glides between tabs.

**Hold and scrub**, like iOS: press anywhere on the bar and either hold for 260ms or start sliding. The
pill lifts into a larger, lighter **lens** that follows your finger, each icon lighting as it passes
under, with a light tick at every tab. Release and it settles on the nearest tab and selects it. A
plain tap selects directly. The lens position and lift are read only in layout and draw, so a scrub
never recomposes the bar. Pad scrolling content by `brNavClearance()`.

**Compact form.** Pass `compact = rememberCompactNavState(resetKey = currentScreen).compact` and put
`Modifier.nestedScroll(state.connection)` on the screen container. Once content has scrolled 24dp
toward its end, the capsule eases (a `glide` spring) into a slimmer, lower form: 64 → 52dp tall, tabs
68 → 52dp wide, icons at 90%, the accessory shrinking with it. It eases back after 24dp of scrolling
up, at the top of the list, or when the destination changes. Requiring a short run in one direction
means slow drags never make it flicker. The bar keeps its full-height slot, so `brNavClearance()`
stays exact and nothing on the page shifts. Tap and scrub work the same in both forms.

## 10. Haptics

| Interaction | `HapticFeedbackType` |
|---|---|
| Switch, checkbox | `ToggleOn` / `ToggleOff` |
| Chip, segment, radio, tab tap, slider step | `SegmentTick` |
| Nav bar scrub starts / crosses a tab | `GestureThresholdActivate` / `SegmentFrequentTick` |
| Primary, Ink, Critical buttons | `Confirm` |
| Long press | `LongPress` (automatic in `pressable`) |

## 11. Icons

A hand-drawn line set on a 24dp grid: 1.75 stroke, round caps and joins, tinted at use. The library
ships only what its components need (`br_ic_back`, `br_ic_check`, `br_ic_chevron_right`, `br_ic_close`,
`br_ic_search`); the catalog's set in `catalog/src/main/res/drawable` follows the same rules.
