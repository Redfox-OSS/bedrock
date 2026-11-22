# Bedrock

A calm, frosted design system for Android, built with Jetpack Compose. Bedrock is Obsidian's quieter
sibling: the same dark surfaces and frosted glass, but colour lives *inside* surfaces instead of glowing
around them, accents are mid-bright instead of neon, and every animation arrives and rests instead of
bouncing.

- **Tokens:** 6 accent families × 3 neutral ramps, an Inter type scale in sentence case with tabular
  figures, concentric radii, a 4dp spacing scale
- **Effects:** frosted glass with a lit rim (via [Haze](https://github.com/chrisbanes/haze)), aura colour
  fields, a neutral float shadow, a calm press response that replaces ripples
- **Motion:** critically damped springs only, one house curve, quiet entrances, rolling numbers, soft
  parallax page transitions
- **Components:** buttons, icon buttons, chips, switch, checkbox, radio, segmented control, cards (solid,
  glass, aura), grouped lists, fields, search, sliders, progress, spinner, toasts, badges, tags, avatars,
  stat tiles, empty states, sheets, dialogs, a large-title page with a frosted top bar, and an icon-only
  nav bar whose pill you can **hold and scrub** across tabs, and which eases into a compact form while
  you scroll
- **Catalog app:** everything on a phone, in [`catalog/`](catalog)

The full reference (every value, motion spec and usage rule) is in **[docs/GUIDE.md](docs/GUIDE.md)**.

## Quick start

```kotlin
setContent {
    BedrockTheme(accent = BedrockAccent.Ember) {
        val hazeState = rememberHazeState()
        val compactNav = rememberCompactNavState(resetKey = currentScreen)
        CompositionLocalProvider(LocalHazeState provides hazeState) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(BedrockTheme.colors.background)
                    .nestedScroll(compactNav.connection)       // the bar eases compact while you scroll
            ) {
                BrPage(title = "Today", bottomPadding = brNavClearance()) { /* lazy items */ }
                BrNavBar(items, selectedIndex, onSelect, Modifier.align(Alignment.BottomCenter), compact = compactNav.compact)
            }
        }
    }
}
```

## Toolchain

Pinned to Obsidian's, so an app can move across without touching its build: AGP 9.2.1, Kotlin 2.2.10,
Compose BOM 2026.02.01, Haze 1.7.2, compile SDK 36.1, min SDK 31 (the glass blur needs Android 12).

```bash
./gradlew build                      # library + catalog: compile, unit tests, lint
./gradlew :catalog:installDebug      # put the catalog on a connected phone
```

## License

GNU General Public License v3.0 (see [LICENSE](LICENSE)). Bundles the Inter typeface under the
[SIL Open Font License 1.1](bedrock/src/main/assets/licenses/Inter-OFL.txt).
