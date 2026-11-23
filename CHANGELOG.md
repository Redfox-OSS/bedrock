# Changelog

## 0.2.0

- The pink accent is now red: `BedrockAccent.Red`, main `#EF3355` (soft `#FFC0C9`, deep `#6E1222`,
  on-main `#34040E`), with a pink partner so its auras run red to pink. `BedrockAccent.Rose` is
  deprecated and returns `Red`.

## 0.1.1

- `BrSheet`, `BrDialog` and `BrDialogFrame` are solid surfaces. They open in their own window, which
  can't blur the page, and at 97% opacity bright text behind them ghosted through.
- Their window now draws edge to edge: the sheet reaches the bottom of the screen instead of
  stopping above a black strip, and the dim covers the status bar too.

## 0.1.0

First version.

- Theme: `BedrockTheme`, six `BedrockAccent` families (Ember, Tide, Moss, Rose, Iris, Sand) with five
  tones each, three `BedrockNeutrals` ramps (Basalt, Slate, Pitch), `BedrockTypography`, `BrShapes`
  with `concentric()`, `BrSpacing`.
- Effects: `glass` with a lit rim, `rim`, `GlassBackdrop`, `aura` with `Aura.of` / `Aura.blend` and
  throttled drift, `floatShadow`, `pressable`, `breathe`, `Placeholder`.
- Motion: `BrMotion` (quick, settle, glide; all critically damped), `BrMotion.Calm`, `BrDurations`,
  `RevealScope` / `revealOnEnter`, `BrTransitions`, `SwapText`, `RollingNumber`.
- Components: `BrButton`, `BrIconButton`, `BrChip`, `BrSwitch`, `BrCheckbox`, `BrRadio`,
  `SegmentedControl`, `BrCard`, `GlassCard`, `AuraCard`, `IconTile`, `BrDivider`, `SectionHeader`,
  `ListGroup`, `ListRow`, `BrTextField`, `BrSearchField`, `BrSlider` (thin and thick),
  `BrProgressBar`, `BrProgressRing`, `BrSpinner`, `BrToastState` / `BrToastHost`, `BrBadge`, `BrTag`,
  `BrAvatar`, `StatTile`, `EmptyState`, `BrSheet` / `SheetAction`, `BrDialog`, `BrPage` / `BrTopBar`.
- Navigation: `BrNavBar`, icon-only with a pill selection you can hold and scrub, and a compact form
  it eases into while content scrolls (`compact`, `rememberCompactNavState`); `brNavClearance()`.
- `BrMenu` / `BrMenuItem`, `BrDayChip`, `BrFab`, `BrDialogFrame`; `BrSheet` and `BrDialog` open in
  their own window so they cover a floating nav bar.
- `BrTopBar` side slots size to their content; `BrLargeTitle`, `rememberTopBarCollapse`,
  `topBarClearance` for hand-built pages.
- `RevealColumn`; `BrTransitions.expandIn / collapseOut / revealStart / concealStart / swap`.
- `BrButton` / `BrIconButton` take a `haptic` override; `ListGroup` takes a `shape` (for nesting
  concentrically in dialogs) and `color`; `BrSheet` rides above the keyboard.
- Glass without blur (no provider, or inside a dialog window) falls back to its tint alone.
- The compact nav bar only reacts to scrolling the content actually did, so a drag on a list too
  short to scroll no longer shrinks it.
- Motion about a fifth quicker, still never overshooting: springs `quick` 700, `settle` 320,
  `glide` 170 (were 520 / 220 / 110); `BrDurations` Fade 150, Swap 230, Enter 360, Linger 520
  (were 180 / 280 / 440 / 640); stagger 38ms (was 45).
- Catalog app with Today, Foundations, Components, Motion and Principles tabs.
