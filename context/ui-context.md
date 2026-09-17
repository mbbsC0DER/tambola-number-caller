# Tambola Number Caller — UI Context

## Purpose

This document is the visual source of truth for Tambola. It defines the tokens, typography, shapes, spacing, component treatments, layouts, icons, motion, and accessibility rules used by the Android application. New UI must reuse these decisions instead of introducing raw values or one-off patterns.

Product behavior belongs in `project-overview.md`, system structure belongs in `architecture.md`, and implementation conventions belong in `code-standards.md`.

## Visual Direction

Tambola should feel calm, friendly, and dependable during a live game. The interface uses warm neutral surfaces, forest-green actions, soft botanical highlights, generous touch targets, and large legible numbers. It is minimal rather than decorative: the current number and playback controls receive the strongest emphasis, while saved sessions, readouts, and settings remain visually quiet.

The design must not look like a casino, lottery, or gaming advertisement. Avoid saturated rainbow palettes, gold-on-black styling, flashing effects, confetti, skeuomorphic balls, gradients, glass effects, and ornamental copy.

## Theme Model

The app supports three preferences:

- `System`: follow the device light or dark appearance.
- `Light`: always use the light token set.
- `Dark`: always use the dark token set.

Use the fixed Tambola color schemes below. Do not enable Material dynamic color; a device-generated palette would change number-state meaning and weaken visual consistency.

## Color Tokens

### Core Material Tokens

Every color must be accessed through `MaterialTheme.colorScheme` or a documented Tambola semantic token. Hex values belong only in the centralized theme definition.

| Semantic token | Light | Dark | Role |
|---|---:|---:|---|
| `primary` | `#255B45` | `#ACD4AE` | Primary actions, active status, current-number fill |
| `onPrimary` | `#FFFFFF` | `#113825` | Content placed on `primary` |
| `primaryContainer` | `#D8EDB8` | `#2D5138` | Called-number cells, featured cards, tonal emphasis |
| `onPrimaryContainer` | `#143D2B` | `#D8EDB8` | Content placed on `primaryContainer` |
| `secondary` | `#58634D` | `#BECAB4` | Secondary emphasis and controls |
| `onSecondary` | `#FFFFFF` | `#293326` | Content placed on `secondary` |
| `secondaryContainer` | `#E0E8D5` | `#3F493A` | Secondary tonal controls |
| `onSecondaryContainer` | `#172013` | `#DAE6D0` | Content placed on `secondaryContainer` |
| `background` | `#F7F8F2` | `#131A15` | App window background |
| `onBackground` | `#20271F` | `#E3EAE1` | Primary content on the app background |
| `surface` | `#F7F8F2` | `#131A15` | Default screen and component surface |
| `onSurface` | `#20271F` | `#E3EAE1` | Primary text and icons |
| `surfaceVariant` | `#E1E5DD` | `#414941` | Legacy Material variant surface where required |
| `onSurfaceVariant` | `#596154` | `#C1C9BE` | Secondary text, inactive status, supporting icons |
| `surfaceContainerLowest` | `#FFFFFF` | `#0D120E` | Lowest contrasting surface |
| `surfaceContainerLow` | `#F2F4ED` | `#1A211C` | Subtle grouped background |
| `surfaceContainer` | `#EDF0E7` | `#202A22` | Remaining-number cells and ordinary tonal surfaces |
| `surfaceContainerHigh` | `#E6EADF` | `#2B352D` | Previous-number button and elevated tonal surfaces |
| `surfaceContainerHighest` | `#DDE2D8` | `#364038` | Strongest neutral container |
| `outline` | `#747D70` | `#8B9489` | Focused outlines and essential borders |
| `outlineVariant` | `#D5DBCD` | `#414941` | Dividers and quiet borders |
| `inverseSurface` | `#2D332C` | `#E3EAE1` | Inverse surfaces such as transient messages |
| `inverseOnSurface` | `#EFF1EB` | `#2A312B` | Content on `inverseSurface` |
| `inversePrimary` | `#ACD4AE` | `#255B45` | Brand action on an inverse surface |
| `error` | `#BA1A1A` | `#FFB4AB` | Error actions and critical status |
| `onError` | `#FFFFFF` | `#690005` | Content placed on `error` |
| `errorContainer` | `#FFDAD6` | `#93000A` | Recoverable error message surface |
| `onErrorContainer` | `#410002` | `#FFDAD6` | Content placed on `errorContainer` |
| `scrim` | `#000000` | `#000000` | Modal overlay base |

### Tambola-Specific Tokens

These aliases communicate product meaning. Implement them as centralized semantic properties or map them consistently to Material tokens.

| Product token | Light | Dark | Usage |
|---|---:|---:|---|
| `numberHero` | `primaryContainer` | `primaryContainer` | Large current-number surface |
| `onNumberHero` | `onPrimaryContainer` | `onPrimaryContainer` | Large current-number text |
| `numberCurrent` | `primary` | `primary` | Current number cell on the 1–90 board |
| `onNumberCurrent` | `onPrimary` | `onPrimary` | Current number text |
| `numberCalled` | `primaryContainer` | `primaryContainer` | Previously called board cells |
| `onNumberCalled` | `onPrimaryContainer` | `onPrimaryContainer` | Called number text |
| `numberRemaining` | `surfaceContainer` | `surfaceContainer` | Uncalled board cells |
| `onNumberRemaining` | `onSurfaceVariant` | `onSurfaceVariant` | Uncalled number text |
| `playbackActive` | `primary` | `primary` | Active playback indicator |
| `playbackInactive` | `outline` | `outline` | Paused or idle indicator |
| `historyDrawer` | `#151D18` | `#151D18` | Fixed dark previous-number drawer |
| `onHistoryDrawer` | `#F0F5ED` | `#F0F5ED` | Primary drawer content |
| `historyDrawerMuted` | `#9CA99F` | `#9CA99F` | Call-position labels in the drawer |
| `historyDrawerDivider` | `#FFFFFF` at 8% | `#FFFFFF` at 8% | Drawer row dividers |
| `modalScrim` | `#000000` at 60% | `#000000` at 60% | Drawer and modal masking layer |

### Color Usage Rules

1. Use `primary` for the single highest-priority action or active state in a region.
2. Use `primaryContainer` for tonal emphasis, not as a second competing primary action.
3. Never infer a number's state from color alone. Accessibility semantics and weight or border changes must reinforce it.
4. Reserve error colors for actual failures or destructive confirmation. Do not use red for ordinary pause or quit controls.
5. Use neutral surface containers to create hierarchy before adding outlines or shadows.
6. Do not introduce additional accent hues without updating this file and both theme palettes.
7. There is no AI functionality, so no AI-specific gradient or accent palette exists.

## Typography

Use the Android system sans-serif/Roboto family. Do not add a downloadable or bundled font unless typography is deliberately redesigned for the whole app. Number glyphs must remain clear at a glance; avoid decorative or condensed faces.

### Material Type Scale

| Token | Size / line height | Weight | Letter spacing | Usage |
|---|---:|---:|---:|---|
| `displayLarge` | 57 / 64 sp | 400 | -0.25 sp | Reserved; not used in routine screens |
| `displayMedium` | 45 / 52 sp | 400 | 0 sp | Reserved for exceptional numeric emphasis |
| `displaySmall` | 36 / 44 sp | 400 | 0 sp | Speed value in the pace sheet |
| `headlineLarge` | 32 / 40 sp | 400 | 0 sp | Rare full-screen heading |
| `headlineMedium` | 28 / 36 sp | 400 | 0 sp | Screen heading and drawer number |
| `headlineSmall` | 24 / 32 sp | 400 | 0 sp | Sheet heading and saved-session current number |
| `titleLarge` | 22 / 28 sp | 400 | 0 sp | Section heading |
| `titleMedium` | 16 / 24 sp | 500 | 0.15 sp | Card and drawer title |
| `titleSmall` | 14 / 20 sp | 500 | 0.1 sp | Compact component title |
| `bodyLarge` | 16 / 24 sp | 400 | 0.5 sp | Explanatory text |
| `bodyMedium` | 14 / 20 sp | 400 | 0.25 sp | Playback status and standard body copy |
| `bodySmall` | 12 / 16 sp | 400 | 0.4 sp | Session metadata |
| `labelLarge` | 14 / 20 sp | 500 | 0.1 sp | Buttons and prominent labels |
| `labelMedium` | 12 / 16 sp | 500 | 0.5 sp | Compact labels and drawer call index |
| `labelSmall` | 11 / 16 sp | 500 | 0.5 sp | Counts, expiry, and low-priority metadata |

### Numeric and Brand Styles

| Token | Specification | Usage |
|---|---|---|
| `numberHero` | 68 sp, 72 sp line height, weight 500, -3 sp letter spacing | Current number on the caller screen |
| `numberPrevious` | 24 sp, 32 sp line height, weight 400 | Previous-number circular control |
| `numberDrawer` | 28 sp, 36 sp line height, weight 500 | Chronological history row number |
| `numberBoard` | 12 sp, 16 sp line height; weight 700 when called/current, otherwise 400 | 1–90 board cells |
| `brandLabel` | 14 sp, 20 sp line height, weight 500, 3 sp letter spacing, uppercase | `TAMBOLA` home label only |

Typography rules:

- Display one- and two-digit numbers with at least two-character visual width where layout stability matters; the hero and history may display `01`, while spoken text never includes padding.
- Use sentence case for all actions and headings except the `TAMBOLA` brand label.
- Do not use italics, underlines, all-caps section headings, or more than three font weights on one screen.
- Avoid decorative captions and subtitles. Supporting text must add necessary state, retention, timing, recovery, or accessibility information.
- Text must reflow at large system font sizes; never scale text down to force it into a fixed box.

## Shape and Border Radius Scale

| Token | Radius | Usage |
|---|---:|---|
| `radiusNone` | 0 dp | Edge-to-edge regions only |
| `radiusXs` | 4 dp | Small internal indicators |
| `radiusSm` | 6 dp | Number-board cells |
| `radiusMd` | 12 dp | Material small shape, compact surfaces |
| `radiusLg` | 16 dp | Saved-session number tile and preference rows |
| `radiusXl` | 20 dp | Material medium shape, cards and sheets where appropriate |
| `radius2xl` | 24 dp | History drawer exposed corners |
| `radius3xl` | 28 dp | Material large shape and current-number hero |
| `radiusFull` | 50% | Circular previous, play/pause, and icon controls |

Use the closest token. Do not add arbitrary radii such as 10, 14, 18, or 22 dp. Nested surfaces should normally use a smaller radius than their parent.

Borders use `outlineVariant` at 1 dp by default. The current board number uses a 2 dp emphasis border in `onPrimaryContainer`. Avoid borders when a container-color change already establishes hierarchy.

## Spacing and Size Scale

| Token | Value | Typical usage |
|---|---:|---|
| `space0` | 0 dp | No separation |
| `space1` | 4 dp | Board gutters and tight metadata |
| `space2` | 8 dp | Icon-to-label gap and compact rows |
| `space3` | 12 dp | Related controls |
| `space4` | 16 dp | Standard component padding and list gap |
| `space5` | 20 dp | Caller horizontal inset |
| `space6` | 24 dp | Screen inset, major card padding, section gap |
| `space7` | 28 dp | Caller board separation |
| `space8` | 32 dp | Bottom-sheet bottom inset and major separation |
| `space10` | 40 dp | Rare large section separation |
| `space12` | 48 dp | Minimum primary touch target |
| `space16` | 64 dp | Previous and playback control diameter |

Size rules:

- The minimum interactive target is 48 × 48 dp.
- Primary start actions have a minimum height of 56 dp.
- Standard icons are 24 dp; compact chip icons are 18 dp; primary playback icons are 32 dp.
- Prefer the spacing scale over raw `dp` values. A component-specific measurement may be used only when documented in the component section below.

## Elevation

Tambola uses tonal surface hierarchy rather than prominent shadows.

| Level | Elevation | Usage |
|---|---:|---|
| `level0` | 0 dp | Screens, number board, ordinary cards |
| `level1` | 1 dp | Interactive card only when tonal separation is insufficient |
| `level2` | 3 dp | Menus and floating overlays supplied by Material |
| `level3` | 6 dp | Modal bottom sheet or drawer supplied by Material |

Do not add custom drop shadows to number cells, hero numbers, or playback controls. Do not use elevation as the only indication that a surface is interactive.

## Layout System

### Global Layout

- Design for portrait phones first, including a 360 dp-wide viewport.
- Center primary content and cap it at 640 dp on wide windows.
- Respect system bars through the app scaffold; do not apply duplicate inset padding inside screens.
- Use 24 dp horizontal screen padding on Home and Settings and 20 dp on Caller.
- Use vertical scrolling when content can exceed the viewport. Controls must not become unreachable with large fonts or short screens.
- Keep one primary vertical reading order and avoid multi-column layouts except the caller control row and fixed number board.

### Home Screen

- Top row: brand label on the start edge, Settings icon on the end edge.
- Start-session action: full-width featured `primaryContainer` card with one primary button.
- Saved sessions: chronological list of full-width outlined cards separated by 16 dp.
- Each session card shows the current number tile, date/time, called/remaining counts, expiry, and a trailing chevron.
- Empty state uses one quiet outlined card. Do not add an illustration or motivational subtitle.

### Caller Screen

- Top row contains Quit/Back, pace chip, and readout loop action.
- The central control row uses approximate weights `1 : 1.8 : 1` for previous number, hero number, and play/pause.
- Previous and playback controls are 64 dp circles.
- The current-number hero has a minimum height of 148 dp and `radius3xl` corners.
- Playback status appears immediately below the controls with a 7 dp status dot.
- Called/remaining count sits above the board, aligned to the end.
- Avoid labels beneath controls when the icon, semantics, and placement already make their purpose clear.

### Number Board

- Always render 90 cells as 9 rows × 10 columns in numeric order, 1 through 90.
- Use a 4 dp gap horizontally and vertically.
- Cells share equal width and have a minimum height of 34 dp.
- Cell radius is 6 dp.
- The current number uses `numberCurrent`, bold text, and a 2 dp border.
- Earlier called numbers use `numberCalled` and bold text.
- Remaining numbers use `numberRemaining` and normal-weight text.
- Do not animate or reorder the board when state changes.

### Previous-Numbers Drawer

- Open from the start/left edge when the previous-number circle is pressed.
- Width is 232 dp and height fills the window.
- Use the fixed `historyDrawer` color, with 24 dp rounding only on exposed end corners.
- Apply a 60% black scrim to the masked remainder of the screen.
- List numbers from earliest to latest, top to bottom.
- Each row shows the call position in muted text and the two-digit number in `numberDrawer` style.
- Use 20 dp horizontal content padding and 16 dp vertical row padding.
- The list scrolls independently and never pauses, resumes, repeats, or otherwise mutates the session.

### Settings Screen and Bottom Sheets

- Group settings under short `titleLarge` section headings.
- Theme choices use full-width tonal rows with radio buttons.
- Separate unrelated groups with Material dividers.
- The pace selector belongs in a modal bottom sheet, not permanently on the caller screen.
- A sheet uses 24 dp horizontal padding, 16 dp internal spacing, and 32 dp bottom padding.
- Show the selected pace prominently, the 1–6 second endpoints, one necessary timing explanation, and a full-width confirmation button.

## Component Conventions

| Component | Required treatment |
|---|---|
| Primary button | Filled Material button using `primary`; 56 dp minimum height for the session-start action |
| Secondary button | Outlined or tonal button; never compete visually with the primary action |
| Icon button | 48 dp minimum target; filled tonal for top-level caller utilities, filled primary for play/pause |
| Pace control | Material `AssistChip` with speed icon and compact value such as `2s` |
| Session card | Full-width `OutlinedCard`; entire card is one resume target |
| Featured start card | `primaryContainer` surface with 24 dp padding and one clear action |
| Number hero | `primaryContainer`, 28 dp radius, centered `numberHero` text |
| Number cell | Semantic number-state colors, 6 dp radius, centered label, accessibility state description |
| Status indicator | 7 dp circle plus `bodyMedium` status text; color and text both change |
| Error card | `errorContainer` with concise failure text and one recovery action |
| Dropdown menu | Material menu anchored to the loop/readout action; remaining first, called second |
| Snackbar | Material snackbar for transient operation feedback; never for durable session state |
| Empty state | Quiet outlined card with one icon and one factual line |

Disabled components use Material's built-in disabled treatment. Do not manually reduce opacity unless the Material component does not expose an appropriate disabled state.

## Iconography

Use the repository's `AppIcons` native vector set. Do not add an icon-font dependency for a small set of controls.

Icon construction:

- 24 × 24 dp viewport.
- 1.8 px outline stroke.
- Round stroke caps and joins.
- Filled geometry only for immediate playback controls: Play, Pause, and Stop.
- Outline geometry for Back, Chevron, Repeat, Volume, History, Speed, and Settings.
- Back and directional audio icons must auto-mirror in right-to-left layouts where applicable.

Usage rules:

- Use the loop/repeat icon for called and remaining number readouts. Do not use the speaker icon for this action.
- Use the speaker/volume icon only for speech setup, voice testing, or voice availability.
- Never use emoji or Unicode symbols as production icons.
- Icon color inherits the component content color; do not hard-code black or white at the call site.
- Every icon-only interactive control requires a concise content description.
- Decorative icons use a null content description.
- Do not mix outlined and filled versions of the same non-playback action on one screen.

## Motion and Feedback

Motion is functional and restrained.

- Animate the hero-number replacement with a short Material content transition between 150 and 250 ms.
- Use Material's standard drawer, menu, bottom-sheet, and snackbar transitions.
- Playback state changes immediately; do not delay state feedback until speech begins.
- Do not animate every called board cell, pulse the current number continuously, flash the screen, or use celebration effects after number 90.
- Haptic feedback is not required. If introduced later, it must be optional and tied only to deliberate user actions, never every automatic number.
- UI animation timing must never control number-calling or readout timing.

## Accessibility

1. Maintain WCAG AA-equivalent contrast for text and essential icons in both themes.
2. Preserve at least 48 dp touch targets even when the visible icon is smaller.
3. Provide semantics for every board cell in the form “number, current/called/remaining.”
4. Provide both call position and number for each previous-number drawer row.
5. Give play, pause, stop readout, quit, settings, pace, and readout controls explicit action labels.
6. Do not use color, position, or animation as the only carrier of meaning.
7. Support large font sizes without clipped controls or inaccessible content; allow the screen to scroll.
8. Keep the reading and focus order aligned with the visual top-to-bottom order.
9. Do not automatically move accessibility focus on every automatic call; expose the updated current-number state without repeatedly interrupting exploration.
10. Avoid rapid flashes and continuous animation.

## Copy and Number Formatting

- Prefer direct labels: `Start new session`, `Pause`, `Play`, `Read remaining numbers`, and `Repeat called numbers`.
- Use `called` and `remaining` consistently. Do not alternate with `completed`, `done`, `picked`, or `used` for the same state.
- Use `Quit session` for leaving the caller; completion is determined by the 90th successful announcement, not by the button label.
- Display pace compactly as `1s`, `1.5s`, through `6s` on the caller chip.
- Use an em dash when no current or previous number exists.
- Do not add slogans or filler subtitles such as “Let the numbers roll” or “The number board.”
- Visible number padding is a layout choice only. TTS follows the speech rules in the product specification.

## Implementation Rules

1. Define complete light and dark `ColorScheme` values in `Theme.kt`; never rely on Material's default purple fallback tokens.
2. Centralize reusable Tambola dimensions, colors, and type extensions once repetition justifies a token object. Do not scatter duplicated raw values.
3. Use `MaterialTheme` values in screens and components.
4. Raw fixed colors are permitted only for the documented history drawer and its overlay treatment.
5. Preview critical screens at 390 × 844 dp in both themes.
6. Also verify layouts at 360 dp width, large font scale, and a wide 640 dp content area.
7. A new reusable component must define its enabled, pressed, focused, disabled, light, dark, and accessibility behavior.
8. Any deliberate change to color, type, radius, component treatment, or layout pattern must update this document in the same change.

## Visual Review Checklist

Before approving a UI change, confirm:

- All colors come from semantic theme tokens.
- The light and dark appearances both preserve hierarchy and contrast.
- Typography uses an existing role or a documented numeric/brand style.
- Spacing and radius values use the defined scales.
- Only one action has dominant visual emphasis in each region.
- The current, called, and remaining number states remain immediately distinguishable without color alone.
- Icon style and meaning match the iconography rules.
- Icon-only controls have content descriptions and 48 dp targets.
- The screen works at 360 dp width and with large text.
- Scrolling content reaches its first and last meaningful item.
- No decorative caption, redundant subtitle, raw hex value, arbitrary gradient, or one-off shadow was introduced.
