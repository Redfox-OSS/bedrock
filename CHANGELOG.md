# Changelog

## 0.1.0 (unreleased)

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
- Catalog app with Today, Foundations, Components, Motion and Principles tabs.
