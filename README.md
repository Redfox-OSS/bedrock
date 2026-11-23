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

## Install

Bedrock is published from this repository; no account or token is needed to use it. Add the
Maven repository in `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://redfox-oss.github.io/bedrock/maven") {
            content { includeGroup("dev.redfox") }
        }
    }
}
```

then the dependency, for example through the version catalog:

```toml
[versions]
bedrock = "0.1.0"

[libraries]
bedrock = { group = "dev.redfox", name = "bedrock", version.ref = "bedrock" }
```

```kotlin
// app/build.gradle.kts
dependencies { implementation(libs.bedrock) }
```

Compose, Material 3 and Haze come with it. Each [release](https://github.com/Redfox-OSS/bedrock/releases)
also has the AAR, its sources and the catalog APK attached.

**From source, while changing Bedrock and an app together:** clone Bedrock next to the app and add
`includeBuild("../Bedrock")` to the app's `settings.gradle.kts`. Gradle uses the local source in
place of the published `dev.redfox:bedrock`, so changes show up on the next build.

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

## Releasing

1. Set `version` in `bedrock/build.gradle.kts`, and turn the changelog's "unreleased" heading into
   that version.
2. Commit, then tag and push: `git tag v0.1.0 && git push origin v0.1.0`.

The release workflow checks the tag matches the version, builds and tests, adds the version to the
Maven repository on GitHub Pages (never overwriting a released version), and creates a GitHub
Release with the AAR, its sources and the catalog APK.

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
