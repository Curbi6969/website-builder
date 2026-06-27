# Rise — UI/UX Polish & Professionalization Plan

> Goal: take the functionally-complete Rise app from "faithful port" to "shipped product."
> Every page audited, refined, and made consistent. Each pass ends with a green
> `:app:assembleDebug` and an on-device screenshot diff. Dutch copy stays verbatim.

**Method:** for each page I run a critique/audit pass (find the problems), then targeted
refinement passes drawn from the impeccable + taste skills:
`/audit` · `/critique` · `/typeset` · `/layout` · `/colorize` · `/polish` · `/animate` ·
`/delight` · `/distill` · `/optimize`. Foundations first (they touch every screen), then
page-by-page, then cross-cutting QA.

Verify command (JBR 21):
```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :app:assembleDebug --no-daemon
```

---

## Phase 0 — Foundations (touches every screen) ⭐ do first

These are systemic and must land before per-page polish, or the page work fights them.

- [ ] **0.1 Fonts — make Fredoka/Nunito actually render.** Current bundled variable-font
  `variationSettings` approach is rendering as a system fallback on device. Switch to the
  intended **downloadable Google Fonts** (`ui-text-google-fonts` is already a dependency)
  with `GoogleFont.Provider` + certs, and keep the bundled TTFs as the offline fallback in
  each `FontFamily`. Verify on-device that headings show Fredoka's rounded shapes.
  *Skills: /typeset.* **Highest priority — the whole app's character depends on it.**
- [ ] **0.2 Design tokens — harmonize the scale.** Radii currently sprawl across
  20/22/24/26/28/30dp and elevations across 4/5/6/8/10/12/14dp. Collapse to a small,
  intentional scale (e.g. radius sm/md/lg/xl, elevation rest/raised/floating) in
  `ui/theme/Shape.kt` + a spacing/elevation helper. *Skills: /distill, /polish.*
- [ ] **0.3 Spacing rhythm.** Section gaps vary (14/16/18/20). Define a vertical rhythm
  (e.g. 8/12/16/24) and apply consistently. Add consistent top padding now that the fake
  status bar is gone so content doesn't crowd the system bar. *Skills: /layout.*
- [ ] **0.4 Motion tokens.** One easing + duration set for entrances, taps, and toggles so
  motion feels coherent rather than per-component. *Skills: /animate.*

---

## Phase 1 — Per-page polish

Each page: audit → typeset → layout → color → motion → re-screenshot.

- [ ] **1.1 Home (Start).** Unify the header with the other tabs' title+subtitle pattern (or
  intentionally differentiate it). Re-balance hero ↔ bored CTA ↔ task list spacing after the
  stat-card removal. Task rows: add a satisfying check "pop" + strikethrough transition on
  complete. Verify the plant sits centered with margin in its card across plant sizes.
- [ ] **1.2 Rust + Player overlay.** Featured "Urge surfen" card: tighten the ▶ Start pill,
  emoji watermark bleed, and text width. Course rows: consistent icon tiles, press feedback.
  Player overlay: progress-ring easing, play/pause transition, done state polish.
- [ ] **1.3 Mood (Stemming).** Mood picker: bigger touch targets (≥48dp), selected-state
  transition (scale + ring), haptic-feel. Journal card contrast + focus state on the text
  field. Calendar: legend, today-marker, cell sizing on narrow phones.
- [ ] **1.4 Stats (Cijfers).** Animate bars on entry (barGrow). Reclaimed cards: icon/þnumber
  hierarchy. Trigger bars + weekly bars: consistent track color, labels, and rounding.
- [ ] **1.5 Panic flow (overlay + games).** Highest-stakes screen — must feel calm and
  immediate. Audit menu/breathing/water/reasons/done transitions; breathing animation
  timing; back/close affordances. Games (bubbles/grounding/tap-green): pop feedback, score
  legibility, game-over clarity.
- [ ] **1.6 Bored sheet.** Sheet entrance/exit easing, grabber, scrim opacity, grid spacing,
  CTA hierarchy.
- [ ] **1.7 Settings overlay.** Field focus states, toggle animation, reasons editor add/remove
  affordances, header/back consistency with other overlays.

---

## Phase 2 — Cross-cutting QA

- [ ] **2.1 Accessibility.** `contentDescription` for emoji/icon-only buttons (⚙️, 🙂, nav,
  panic FAB); verify text contrast vs backgrounds; min 48dp touch targets everywhere; respect
  large font scales. *Skills: /audit.*
- [ ] **2.2 Responsive.** Test on small (≤360dp) and large widths; ensure cards, calendar,
  and charts don't clip or overflow. *Skills: /adapt.*
- [ ] **2.3 Consistency sweep.** One final pass for stray radii/shadows/colors/weights that
  drifted from the Phase-0 tokens. *Skills: /polish, /critique.*
- [ ] **2.4 Performance.** Check recomposition hotspots (plant canvas loop, timers), overdraw
  from stacked gradients/shadows. *Skills: /optimize.*

---

## Phase 3 — Sign-off

- [ ] Full screenshot set (every tab + overlay) captured on-device for before/after.
- [ ] `:app:assembleDebug` green; install + smoke-test the whole flow on the phone.
- [ ] Update README/IMPLEMENTATION-PLAN status.

---

## Out of scope (unless you ask)
- New features or screens.
- Changing Dutch copy, the color palette, or seeded data (design contract).
- Real persistence for streak/stats (intentionally seeded).
