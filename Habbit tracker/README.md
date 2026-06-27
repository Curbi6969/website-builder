# Rise — Android app

Native Android (Kotlin + Jetpack Compose) implementation of the **Rise** design — a
Dutch sobriety/recovery companion: streak tracking with a growing plant, meditation
player, mood logging, stats, a panic "Hulp" flow (breathing, cold-water reset,
mini-games, your reasons), a "bored" quick-action sheet, and settings.

## Run it

1. Open this folder (`Habbit tracker/`) in **Android Studio** (it bundles the JDK 21 + Gradle this project needs).
2. Let Gradle sync. The SDK path is read from `local.properties` (`sdk.dir`) — Android Studio creates/fixes this automatically.
3. Pick an emulator or device and press **Run** (`▶`). App id: `com.rise.app`, minSdk 24.

Command-line build (uses the bundled JBR):

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :app:assembleDebug
```

## Project layout

```
app/src/main/java/com/rise/app/
  MainActivity.kt            entry point → RiseTheme { RiseApp() }
  RiseApp.kt                 shell: status bar, tab host, bottom nav, panic FAB, overlay host
  data/                      RiseUiState, RiseDefaults (seeded content), RisePreferences (DataStore)
  vm/RiseViewModel.kt        single state holder + all intents + coroutine timers
  ui/theme/                  Color, Type (Fredoka/Nunito), Shape, Theme
  ui/plant/PlantCanvas.kt    L-system plant (Canvas) + pot
  ui/screens/                HomeScreen, RustScreen, MoodScreen, StatsScreen
  ui/overlays/               PanicOverlay, PanicGames, PlayerOverlay, BoredSheet, SettingsOverlay
  ui/common/                 cardSurface, StatusBar, RiseToast
```

## Design contract

The original design is the source of truth, kept under [`design-reference/`](design-reference/):
`Rise.dc.html` (markup + state logic), `lsys-plant.js` (plant algorithm), `support.js`
(the web runtime — reference only, unused by the native app). All Dutch copy and colors
are reproduced from it verbatim. The one deliberate deviation: the app is fluid (fills the
device, `dp` units) instead of locked to the mockup's fixed 390×844 frame.

## Architecture notes

- Single `RiseViewModel` exposes an immutable `RiseUiState` via `StateFlow`; composables
  render from it and call intents. Timers (meditation countdown, tap-green) run as
  `viewModelScope` coroutines.
- Persistence (DataStore) is intentionally minimal — name, reasons, reminder toggles, and
  the tap-game record — mirroring the design's only real persistence. Streak/stats are
  seeded constants, as in the design.
- Fonts are bundled variable TTFs (no Play Services / runtime download needed).

## Status

All planned tasks are complete and `:app:assembleDebug` builds green. Progress and the full
task breakdown live in [`IMPLEMENTATION-PLAN.md`](IMPLEMENTATION-PLAN.md). A few optional
micro-animations from the mockup (per-task pop, bar-grow, bubble pop) are noted there as a
future polish pass.
