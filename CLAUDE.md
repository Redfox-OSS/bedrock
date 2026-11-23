# CLAUDE.md

Guidance for Claude Code (and anyone else) working in this repository.

## What this is

Bedrock is a Jetpack Compose design system for Android: dark surfaces, frosted glass with a lit rim,
mid-bright colour that lives inside surfaces (auras, never glows), and calm motion where nothing
overshoots. It is the successor to Obsidian (github.com/Redfox-OSS/obsidian) for apps that want a
quieter look, and keeps Obsidian's structure and toolchain.

| Path | What |
|---|---|
| `bedrock/` | The library, `dev.redfox:bedrock`, package `dev.redfox.bedrock` |
| `catalog/` | The catalog app: every token, component and motion on a phone |
| `build-logic/` | Convention plugins (SDK levels, Compose) |
| `docs/GUIDE.md` | The full reference: every value, motion spec and usage rule |
| `CHANGELOG.md` | What changed in each version |

## Commands

```bash
./gradlew build                          # library + catalog: compile, unit tests, lint
./gradlew :catalog:installDebug          # put the catalog on a connected phone
./gradlew :bedrock:publishToMavenLocal   # try a change in an app that uses mavenLocal()
```

## Toolchain (pinned on purpose)

AGP 9.2.1, Kotlin 2.2.10, Compose BOM 2026.02.01, Haze 1.7.2, compile SDK 36.1, min SDK 31. Same as
Obsidian; don't upgrade casually (Haze 2.x needs Compose 1.12 and Kotlin 2.4).

## Rules for library code

**API**
- Kotlin explicit API mode is on: every public declaration states `public` and its type.
- Every public declaration has KDoc that says what it is for, not only what it does.
- Library resources start with `br_` (enforced by `resourcePrefix`).

**Design**
- Only tokens: `BedrockTheme.colors`, `BedrockTheme.typography`, `BrShapes`, `BrSpacing`, `BrMotion`,
  `BrDurations`. No hard-coded colours, no `MaterialTheme.*` outside `theme/`.
- **No glows.** Colour goes inside surfaces (`aura`, accent containers). The only shadow is
  `floatShadow`, neutral black, for floating surfaces.
- **Nothing overshoots.** Every spring has damping ratio 1 (`BrMotion.quick/settle/glide`); timed
  motion uses `BrMotion.Calm`. Never add a bouncy spec.
- Sentence case for all UI text. Numbers use tabular figures (`figure`, `figureLarge`, `numeral`).
- Nested corners are concentric (`BrShapes.concentric`).
- No `Modifier.clickable` or ripples on visible controls: use `Modifier.pressable { }`.
- Glass: scrolling content is `glassSource()`; a glass surface is a sibling of it, never inside it.
  Inside scrolling content, use `GlassBackdrop`. At most one drifting aura per screen.
- Accessibility: tap targets of at least 32dp inside tappable rows and 44dp elsewhere, content
  descriptions on icon-only controls, semantics (role, selected, progress) on custom controls.

**Performance**
- Per-frame animation is read in layout or draw (`Modifier.layout`, `graphicsLayer {}`, `drawBehind`),
  not in composition.
- Pass fast-changing values as `State<T>` or lambdas, not plain values.

## Adding or changing a component

1. The composable, with KDoc, following the rules above.
2. Unit tests for any logic that isn't drawing.
3. A demo in the catalog app.
4. A row in the components table in `docs/GUIDE.md`.
5. An entry in `CHANGELOG.md`.
6. `./gradlew build`, then look at it on a phone.

## Git and releases

- Commits are the maintainer's: no `Co-Authored-By` or "Generated with" lines in messages.
- Don't push, tag or release without asking.
- Releasing: set `version` in `bedrock/build.gradle.kts`, turn the changelog's "unreleased" heading
  into that version, commit, then push a `v<version>` tag. The release workflow
  (`.github/workflows/release.yml`) publishes to the Maven repository on the `gh-pages` branch
  (https://redfox-oss.github.io/bedrock/maven) and attaches the AAR, sources and catalog APK to a
  GitHub Release. A released version is never overwritten: fix forward with a patch version.
- Never commit signing keys or `local.properties`.
