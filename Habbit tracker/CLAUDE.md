# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

This is **Rise** — a native Android (Kotlin + Jetpack Compose) port of a Dutch sobriety/recovery
companion design. The parent repo's `../CLAUDE.md` governs the wider "website builder" workspace;
this file covers only the `Habbit tracker/` Android project.

## Build & run

No test suite exists (pure Compose UI). Verification = the build compiles green and the result
matches the design region. The system `java` is JDK 8 and will fail; always point `JAVA_HOME` at
Android Studio's bundled JBR (JDK 21) when building from the shell:

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
export ANDROID_HOME="/c/Users/<user>/AppData/Local/Android/Sdk"   # also read from local.properties (sdk.dir)
./gradlew :app:assembleDebug --no-daemon      # full build → app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:compileDebugKotlin --no-daemon # faster compile-only check while iterating
```

In Android Studio: open this folder, let Gradle sync (it fixes `local.properties` automatically), Run ▶.
App id `com.rise.app`, `minSdk 24` / `compileSdk 35`, JVM target 17.

Toolchain is pinned in `gradle/libs.versions.toml`: AGP 8.7.3, Kotlin 2.0.21, Compose BOM
2024.09.03, Gradle 8.11.1 (wrapper). Fonts (Fredoka/Nunito) are bundled variable TTFs in
`app/src/main/res/font/` — no Play Services or runtime font download.

## Architecture

Single-Activity Compose app. `MainActivity` → `RiseTheme { RiseApp() }`.

- **One source of state.** `vm/RiseViewModel` (an `AndroidViewModel`) holds the entire app state as an
  immutable `data/RiseUiState` exposed via `StateFlow`. Every composable takes `(state, vm)`, renders
  from the state, and mutates only by calling VM intents — never local mutable app state. When adding a
  feature, add a field to `RiseUiState` + an intent on the ViewModel; don't introduce a second state owner.
- **Derived values are computed in composables, not stored** (nav colors, `m:ss` timer formatting,
  calendar cells, chart bar heights). `RiseUiState` exposes a few computed getters (`doneCount`, `streak`,
  `plantGenerations`).
- **`RiseApp.kt` is the shell**: background, status bar, the tab host that switches on `state.tab`
  (HOME/RUST/MOOD/STATS), bottom nav, the pulsing panic FAB, and the **overlay host** that z-stacks
  Bored → Settings → Panic → Player → Toast on top of the active tab. Overlays are driven by enum/bool
  fields in the state (`panic: Panic`, `game: Game`, `bored`, `showSettings`, `player`), not by a nav graph.
- **Timers** (meditation countdown, 20s tap-green game, random tap target) run as `viewModelScope`
  coroutines inside the ViewModel — not `java.util.Timer` and not composable side-effects.
- **Persistence is deliberately minimal.** `data/RisePreferences` (DataStore) stores only name, reasons,
  reminder toggles, and the tap-game record — mirroring the design's only real persistence. Streak and
  stats are **seeded constants** (see `data/RiseDefaults`), exactly as in the design. Do not add
  persistence for streak/stats unless explicitly asked.
- **The plant** (`ui/plant/PlantCanvas.kt`) is a hand-ported L-system: seeded `xorshift32` RNG +
  stochastic rewrite rules, drawn turtle-style on a `Canvas` via a `withFrameNanos` growth/idle-sway
  loop. It is a faithful port of `design-reference/lsys-plant.js` — preserve the algorithm if touched.

## Design contract — read before changing UI

`design-reference/` is the **source of truth**, read-only: `Rise.dc.html` (markup, copy, state logic)
and `support.js` (the original web runtime, reference only — unused by the native app). When a pixel
value, color, copy string, or behavior is unclear, read `Rise.dc.html` rather than guessing.

- **Dutch UI copy is content, not translatable strings.** Reproduce it **verbatim**, emoji included.
  Never translate, paraphrase, or "improve" it. The seeded data table in `IMPLEMENTATION-PLAN.md`
  lists the exact strings, colors, and numbers; `data/RiseDefaults.kt` is the in-code copy of it.
- **Colors** are fixed design tokens in `ui/theme/Color.kt` — use those `val`s, don't invent hex values.
- **Deliberate deviation:** the design is a fixed 390×844 mockup; the app is **fluid** (translate `px`→`dp`
  1:1 but fill device width and let the content column scroll). Do not hardcode 390/844.

## Project status & conventions

`IMPLEMENTATION-PLAN.md` is the resumable task log (Tasks 0–14, all complete). It documents every
design region → file mapping and the full seeded-data spec. A few optional mockup micro-animations
(per-task pop, stat bar-grow, bubble pop scale-in) remain as a noted future polish pass.

Commits: no Claude/Anthropic/AI attribution in messages, PRs, or comments (repo rule). Messages
describe the change only.
