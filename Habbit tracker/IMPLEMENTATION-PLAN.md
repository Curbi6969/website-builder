# Rise — Native Android Implementation Plan

> **For agentic workers:** This plan is the resumable source of progress. Each task ends with a buildable deliverable. Check boxes (`- [ ]` → `- [x]`) as you finish. The **design contract / source of truth** is [design-reference/Rise.dc.html](design-reference/Rise.dc.html) — when a pixel value, color, copy string, or behavior is unclear, read that file. Dutch UI copy must be reproduced **verbatim** (it is the design's content; do not translate or rewrite it).

**Goal:** A faithful native Android (Kotlin + Jetpack Compose) port of the "Rise" sobriety/recovery app design, runnable in Android Studio.

**Architecture:** Single-Activity Compose app. One `RiseViewModel` holds all UI state (mirrors the design's `state` object); Compose screens/overlays render from an immutable `RiseUiState` and call ViewModel intents. Persistence (name, reasons, reminder toggles, tap-game record) via DataStore Preferences. The growing plant is a hand-drawn `Canvas` port of the design's L-system (`design-reference/lsys-plant.js`).

**Tech Stack:** Kotlin 2.0.21, Jetpack Compose (BOM 2024.09.03), Material3, `lifecycle-viewmodel-compose`, `datastore-preferences`, `ui-text-google-fonts` (Fredoka + Nunito), AGP 8.7.3, Gradle 8.11.1 (wrapper), JDK 21 (Android Studio bundled JBR).

## Global Constraints

- **Package:** `com.beaunolten.rise`  ·  **App module:** `app`
- **SDK:** `minSdk 24`, `compileSdk 35`, `targetSdk 35`
- **SDK location (local):** `C:\Users\beaun\AppData\Local\Android\Sdk` — referenced via `local.properties` (`sdk.dir=...`). System Java is 8, BUT Android Studio's JBR (JDK 21) at `C:\Program Files\Android\Android Studio\jbr` works from the shell. **To build/verify from this agent shell:**
  ```bash
  export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
  export ANDROID_HOME="/c/Users/beaun/AppData/Local/Android/Sdk"
  bash "c:/Code/website-builder/Habbit tracker/gradlew" -p "c:/Code/website-builder/Habbit tracker" :app:assembleDebug --no-daemon
  ```
  Task 0 produced a green `:app:assembleDebug` (BUILD SUCCESSFUL). Re-run this after each task to verify it still compiles.
- **Language/UI copy:** Dutch, verbatim from the design. Keep all emoji.
- **Phone frame:** the design is fixed at 390×844; the Android app is **fluid** — translate fixed `px` to `dp` (1:1) but let layouts fill the device width and the content column scroll. Do not hardcode 390/844.
- **No AI attribution** in any commit (repo rule in [../CLAUDE.md](../CLAUDE.md)).
- **Verification model:** Each task's "verify" = *`:app:assembleDebug` is green via the JBR command above* + *matches the design region* (read `design-reference/Rise.dc.html`). No unit tests (pure Compose UI). Commit after each task.

## Design Tokens (verbatim — see `design-reference/Rise.dc.html`)

**Colors** (define in `ui/theme/Color.kt`):

| Token | Hex | Use |
|---|---|---|
| `Bg` | `#EAF6EF` | app background / mint |
| `Ink` | `#1B3A2E` | primary text |
| `InkSoft` | `#4A6A5A` | secondary text |
| `InkFaint` | `#9BB0A6` | tertiary text |
| `InkGhost` | `#BCCCC3` | faintest text / labels |
| `NavIdle` | `#AAB9B1` | inactive nav icon |
| `Green` | `#2EA873` | primary accent |
| `GreenDark` | `#1E8A5E` | gradient end / panic top |
| `GreenDeep` | `#14613F` | panic gradient bottom |
| `Stem` | `#3FA071` | plant stem |
| `StemTip` | `#7DC79B` | plant new growth |
| `BerryA` | `#F2A9C4` | plant berry A |
| `BerryB` | `#FFB07C` | plant berry B |
| `Teal` | `#4FB8CE` | meditation accent |
| `TealDark` | `#3C8FAE` | teal gradient end / player top |
| `TealDeep` | `#2B5E7A` | player gradient bottom / ring core |
| `Orange` | `#FF7A59` | urge / panic accent |
| `OrangeA` | `#FF8A65` | gradient start |
| `OrangeB` | `#FF6B43` | gradient end / FAB |
| `PotRim` | `#E78A4A` | plant pot rim |
| `PotA` `PotB` | `#D9763B` `#C9692F` | pot body gradient |
| `Purple` | `#6C5CE0` | journal accent |
| `PurpleSoft` | `#8B7AE8` | journal gradient |
| `JournalA` `JournalB` | `#E9E2FB` `#F3EFFD` | journal card bg |
| `PurpleInk` `PurpleInk2` | `#5B4B9E` `#3A3160` | journal text |
| `YellowA` `YellowB` | `#FFD98A` `#FBC55F` | bored/quick card |
| `YellowInk` `YellowInk2` | `#7A5A14` `#9A7826` | yellow card text |
| `MoodGreat`…`MoodRough` | `#9AE6B4` `#C8EFA0` `#FBE08A` `#FFC48A` `#FFAA9E` | 5 mood colors |
| `CardMint` | `#D6ECE0` | mood button bg on home |
| `HeroPlantA` `HeroPlantB` | `#D6F0E4` `#EAF6EF` | plant hero bg |
| `Card` | `#FFFFFF` | cards |
| Shadow base | `rgba(27,58,46,*)` = `Ink` w/ alpha | soft shadows |

**Fonts** (`ui/theme/Type.kt`, downloadable Google Fonts):
- `Fredoka` (weights 400/500/600/700) — display & headings. The design uses 600 for most headings, 700 for big numbers.
- `Nunito` (weights 400/600/700/800/900) — body. Default body weight 700–800; "900" for emphasis.

**Shapes / elevation:** cards use radius 20–30dp (`RoundedCornerShape`), soft shadows (`Modifier.shadow` with low alpha). Pills/buttons radius 14–18dp. Plant/hero radius 30dp.

**Key seeded data** (from the design's `renderVals()` — reproduce exactly):
- `streak = 14`, `urgePct = 92`, reclaimed `36u`, focus `+18%`.
- Tasks: `08:00 Ochtend check-in ☀️ (done)`, `12:30 15 min wandelen 🚶 (done)`, `17:00 10-min meditatie 🧘 (todo)`, `21:30 Avond check-in 🌙 (todo)`.
- Courses (title/sub/min/icon/bg): Ademruimte 5 🌬️ `#E6F2F6`; Slaap & loslaten 12 🌙 `#E9E2FB`; Ochtendkracht 7 ☀️ `#FFF1D6`; Zelfcompassie 10 💚 `#E3F2E9`; Focus reset 6 🎯 `#E6F2F6`.
- Moods: Top 😄 `#9AE6B4` "Mooi, geniet ervan!"; Goed 🙂 `#C8EFA0` "Fijn dat het lekker gaat."; Oké 😐 `#FBE08A` "Oké is helemaal prima."; Matig 😟 `#FFC48A` "Je voelt het — dat is moedig."; Zwaar 😣 `#FFAA9E` "Zware dag mag. Ik ben er."
- Calendar: June 2026, 2 leading blanks, days 1–25 seeded faces via `seed = [2,1,0,2,1,3,0,1,1,0,2,4,1,0,0,1,2,1,0,3,1,0,0,1,2]` (`seed[(day-1)%len]`), days 26–30 are plain numbers on `#F0F6F2`/`#BCCCC3`.
- Streak bars (14): `h = 40 + round((i/13)*58)`, last bar (`i==13`) color `Orange`, rest `Green`.
- Triggers: Verveling 😪 42% `#FF7A59`; ’s Avonds laat 🌙 28% `#6C5CE0`; ’s Ochtends ☀️ 18% `#4FB8CE`; Stress / down 💭 12% `#2EA873`.
- Weeks beaten: wk1 60, wk2 72, wk3 85, wk4 95 (gradient `#FF8A65→#FF6B43`).
- Affirmations: `["Goed bezig, man! 💪","Trots op je. Echt waar.","Dat is een win. Pak de volgende.","Jij koos jezelf. Sterk.","Zo bouw je je streak 🌱","Yes! Weer een stap vooruit."]`
- Default name `Beau` (falls back to `maat` when blank). Greeting line: `Kom op {name}, jij kan dit 💪`; date label `Dinsdag 25 juni`; streak subtitle `Sterk & vrij. Record: 21 dagen.`
- Reasons (default 3): `"Ik wil ’s ochtends wakker worden zonder schuldgevoel."`, `"Ik wil echte connectie met mensen, niet met een scherm."`, `"Ik wil mijn tijd en energie terug voor wat ik echt wil."`
- Reminders: morning ☀️ "Ochtend check-in"/"Sterke start" (on); evening 🌙 "Avond check-in"/"Reflecteer & sluit af" (on); daytime 💬 "Steun-pings overdag"/"Willekeurige positieve berichten" (on); boredom 🎯 "Bij verveling"/"Seintje als je lang stil bent" (on).
- Grounding steps: 5 dingen die je ZIET 👀; 4 dingen die je HOORT 👂; 3 dingen die je VOELT ✋; 2 dingen die je RUIKT 👃; 1 goede eigenschap van jezelf ❤️.
- Bored activities: 💪 "10 push-ups nu"; 🚶 "Loop naar buiten"; 📞 "App een vriend"; 🎧 "Zet je favo nummer"; 🚿 "Koude douche"; 🧹 "Ruim 5 min op". Mini-goal: ⭐ "Drink een groot glas water".
- `plantGenerations = max(2, min(6, 2 + floor(streak/3)))` → for streak 14 = 6.

---

## File Structure

```
Habbit tracker/
├─ IMPLEMENTATION-PLAN.md          (this file)
├─ design-reference/               (decoded design — read-only contract)
│   ├─ Rise.dc.html  support.js  lsys-plant.js
├─ settings.gradle.kts  build.gradle.kts  gradle.properties
├─ gradle/libs.versions.toml  gradle/wrapper/…  gradlew  gradlew.bat
├─ local.properties               (sdk.dir; gitignored)
└─ app/
    ├─ build.gradle.kts
    ├─ proguard-rules.pro
    └─ src/main/
        ├─ AndroidManifest.xml
        ├─ res/values/(themes.xml, strings.xml), res/font/(google fonts certs), res/xml, mipmap/…
        └─ java/com/beaunolten/rise/
            ├─ MainActivity.kt              # setContent { RiseTheme { RiseApp() } }
            ├─ RiseApp.kt                   # scaffold: bg, statusbar, content host, bottom nav, FAB, overlays
            ├─ data/
            │   ├─ RiseUiState.kt           # immutable state + sub-models (Task, Course, Mood, Reminder, …)
            │   ├─ RiseDefaults.kt          # the seeded data above
            │   └─ RisePreferences.kt       # DataStore (name, reasons, reminders, tapRecord)
            ├─ vm/RiseViewModel.kt          # state holder + all intents
            ├─ ui/theme/(Color.kt, Type.kt, Theme.kt, Shape.kt)
            ├─ ui/common/(Cards.kt, Toast.kt, Pills.kt)   # reusable bits
            ├─ ui/plant/PlantCanvas.kt      # L-system port
            ├─ ui/screens/(HomeScreen.kt, RustScreen.kt, MoodScreen.kt, StatsScreen.kt)
            └─ ui/overlays/(PanicOverlay.kt, PlayerOverlay.kt, BoredSheet.kt, SettingsOverlay.kt)
```

---

## State Model (target shape — `data/RiseUiState.kt`)

```kotlin
enum class Tab { HOME, RUST, MOOD, STATS }
enum class Panic { NONE, MENU, BREATHING, WATER, GAME, REASONS, DONE }
enum class Game { NONE, BUBBLES, GROUND, TAP }
enum class HeroStyle { PLANT, NUMBER }   // default PLANT

data class TaskItem(val id: Int, val time: String, val label: String, val icon: String, val done: Boolean)
data class Course(val title: String, val sub: String, val len: String, val min: Int, val icon: String, val bg: Long)
data class MoodDef(val key: String, val face: String, val label: String, val bg: Long, val note: String)
data class ReminderDef(val key: String, val icon: String, val label: String, val sub: String, val on: Boolean)
data class Bubble(val id: Int, val size: Int, val x: Int, val y: Int, val color: Long)

data class RiseUiState(
    val tab: Tab = Tab.HOME,
    val heroStyle: HeroStyle = HeroStyle.PLANT,
    val userName: String = "Beau",
    val tasks: List<TaskItem> = RiseDefaults.tasks,
    val moodPicked: String? = null,
    val journalText: String = "",
    val reasons: List<String> = RiseDefaults.reasons,
    val reminders: Map<String, Boolean> = RiseDefaults.reminderState,
    val panic: Panic = Panic.NONE,
    val game: Game = Game.NONE,
    val bubbles: List<Bubble> = emptyList(),
    val gameScore: Int = 0,
    val groundStep: Int = 0, val groundFilled: Int = 0, val groundDone: Boolean = false,
    val tapScore: Int = 0, val tapTarget: Int = 0, val tapTime: Int = 20,
    val tapRunning: Boolean = false, val tapOver: Boolean = false, val tapRecord: Int = 0,
    val player: Course? = null, val playerTotal: Int = 1, val playerLeft: Int = 0,
    val playerPlaying: Boolean = false, val playerDone: Boolean = false,
    val bored: Boolean = false,
    val showSettings: Boolean = false,
    val toast: String? = null,
) {
    val doneCount get() = tasks.count { it.done }
    val streak get() = 14
    val urgePct get() = 92
    val plantGenerations get() = maxOf(2, minOf(6, 2 + streak / 3))
}
```

Derived/UI-only values (nav colors, formatted timer `m:ss`, calendar cells, bars) are computed in the composables, not stored.

---

## L-System Plant Algorithm (port of `design-reference/lsys-plant.js`)

Port faithfully into `ui/plant/PlantCanvas.kt`:
- Rules (`X` and `F` are stochastic; `(` `)` strip on next gen):
  - `X →` `(F[+X][-X]FX)`·0.5, `(F[-X]FX)`·0.05, `(F[+X]FX)`·0.05, `(F[++X][-X]FX)`·0.1, `(F[+X][--X]FX)`·0.1, `(F[+X][-X]FXA)`·0.1, `(F[+X][-X]FXB)`·0.1
  - `F →` `F(F)`·0.85, `F(FF)`·0.05, `F`·0.10
- Seeded `xorshift32` RNG (`seed >>> 0`; `x ^= x<<13; x ^= x>>>17; x ^= x<<5`) for stable plants. Use Kotlin `Int`/`UInt` and `ushr`.
- Params: `width=210, height=196, generations=plantGenerations, angle=24°, seg=5, stem #3FA071, stemTip #7DC79B, berryA #F2A9C4, berryB #FFB07C`.
- Growth: per-gen `growthPercent` lerps newest `( )` growth in; `growthRate=0.05/(gen+pct)`; idle sway `sin(t*0.9)*0.025` once finished. Drive via `withFrameNanos` loop in a `LaunchedEffect`; draw with `drawWithCache`/`Canvas`. Turtle: `F` draws a tapered segment (width `1.4 + age*2.6`, `age=1 - i/total`), `+`/`-` rotate by `angle*lt`, `[` `]` push/pop matrix (use an explicit stack of translate+rotate), `A`/`B` draw a berry circle radius `seg*lt`.
- Below the plant: the pot — rim bar 96×14 `#E78A4A` r8, body 78×42 gradient `#D9763B→#C9692F` r0/0/18/18.

---

## Tasks

### Task 0: Gradle/Compose project scaffold
**Files:** Create all root Gradle files, `gradle/libs.versions.toml`, wrapper, `app/build.gradle.kts`, `AndroidManifest.xml`, `res/values/themes.xml` + `strings.xml`, `MainActivity.kt` rendering a placeholder `Text("Rise")` inside an empty `MaterialTheme`. Add `.gitignore` for `/build`, `local.properties`, `.idea`, `.gradle`.
- [x] Root `settings.gradle.kts` (pluginManagement + dependencyResolutionManagement, `rootProject.name="Rise"`, `include(":app")`).
- [x] `gradle/libs.versions.toml` with AGP 8.7, Kotlin 2.0.21, compose-bom 2024.09.03, core-ktx, activity-compose, lifecycle-viewmodel-compose, datastore-preferences, ui-text-google-fonts.
- [x] Root + app `build.gradle.kts`; `gradle.properties` (AndroidX, jvmargs); `proguard-rules.pro`.
- [x] Gradle wrapper (`gradle/wrapper/gradle-wrapper.properties` → 8.11.1, `gradlew`/`gradlew.bat`, `gradle-wrapper.jar`).
- [x] `AndroidManifest.xml` (single Activity), `themes.xml` (NoActionBar), `colors.xml`, launcher icon (`drawable/ic_launcher.xml` vector sprout).
- [x] `MainActivity.kt` placeholder.
- [x] **Verify:** `:app:assembleDebug` → BUILD SUCCESSFUL (verified via JBR 21). Committed.

### Task 1: Theme (colors, fonts, shapes) — DONE
**Files:** `ui/theme/Color.kt`, `Type.kt`, `Shape.kt`, `Theme.kt`; bundled variable fonts `res/font/fredoka.ttf` + `nunito.ttf` (from google/fonts; weights via `FontVariation`, no Play Services/certs needed).
- [x] `Color.kt` — every token as `val X = Color(0xFF……)`.
- [x] `Type.kt` — `Fredoka`/`Nunito` `FontFamily` from variable TTFs (`@OptIn(ExperimentalTextApi)`), `RiseTypography` Material3 mapping.
- [x] `Theme.kt` — `RiseTheme { }` (lightColorScheme from tokens, `Bg` bg, light status bar). `Shape.kt` corner sizes 12–30dp.
- [x] **Verify:** `:app:assembleDebug` green; MainActivity probe renders "Rise" in Fredoka. Committed.

### Task 2: State, defaults, persistence — DONE
**Files:** `data/RiseUiState.kt`, `data/RiseDefaults.kt`, `data/RisePreferences.kt`, `vm/RiseViewModel.kt`.
- [x] `RiseUiState.kt` — enums + sub-models + `RiseUiState` (computed `doneCount`/`streak`/`displayName`/`plantGenerations`). Added `PlayerContent` (urge-surf has no Course) and `bubbleSeed`.
- [x] `RiseDefaults.kt` — all seeded lists/maps incl. mood-calendar seed and `urgeSurf`.
- [x] `RisePreferences.kt` — DataStore (`user_name`, `reasons` joined by ``, `rem_<key>` bools, `tap_record`).
- [x] `RiseViewModel.kt` (`AndroidViewModel`) — `StateFlow<RiseUiState>` + all intents (nav, tasks, mood/journal, settings/name/reasons/reminders, panic nav, games, grounding, tap-green timer, bubbles, bored, player timer, `affirm`/`rndAffirm`). Coroutine timers (no java Timer). Persists name/reasons/reminders/tapRecord.
- [x] **Verify:** `:app:compileDebugKotlin` green. Committed.

### Task 3: App shell (scaffold, nav, FAB, overlay host) — DONE
**Files:** `RiseApp.kt`, `ui/common/Common.kt` (cardSurface + StatusBar), `ui/common/Toast.kt`, stub screens (`ui/screens/*`) + stub overlays (`ui/overlays/*`), `MainActivity.kt`.
- [x] `RiseApp()` — `Bg` box w/ status+nav bar insets; `StatusBar`; scrollable content switching on `state.tab`; bottom nav (⌂/🧘/spacer/☺/▥, active `Green`); centered pulsing Panic FAB. Overlay host stacks Bored→Settings→Panic→Player→Toast by z.
- [x] All composables take `(state, vm)`. Stub screens show the tab name; stub overlays have working close buttons. Each gets filled in its own task.
- [x] **Verify:** `:app:assembleDebug` green. Committed.

### Task 4: Plant Canvas — DONE
**Files:** `ui/plant/PlantCanvas.kt` (`PlantCanvas` + `PlantWithPot`).
- [x] Ported the L-system faithfully (xorshift32, stochastic X/F rules, growth lerp, idle sway), turtle drawing on `Canvas` via `withFrameNanos`. `PlantWithPot` adds the terracotta pot (Spacers).
- [x] **Verify:** `:app:compileDebugKotlin` green. Visual confirmation happens once Home renders it (Task 5) / on the user's first emulator run.

### Task 5: Home tab — DONE
**Files:** `ui/screens/HomeScreen.kt`.
- [x] Greeting header (date, `Kom op {name}…`, ⚙️ + 🙂 buttons), hero (PlantHero default via `PlantWithPot`; NumberHero variant), 3 mini-stat cards, bored CTA, "Plan van vandaag" toggleable task list (`{doneCount}/{taskTotal}`, affirmation toast on complete via vm).
- [x] **Verify:** `:app:assembleDebug` green. Committed.

### Task 6: Rust tab + Player overlay — DONE
**Files:** `ui/screens/RustScreen.kt`, `ui/overlays/PlayerOverlay.kt`.
- [x] Rust: "Urge surfen" featured card → `openUrgeSurf`; "Routines" list of 5 courses → `openPlayer`.
- [x] Player overlay: gradient bg, icon/title/sub, progress ring drawn with two `drawArc`s (white sweep = pct, translucent remainder) + inner circle `m:ss` "resterend", play/pause toggle, done state. Countdown driven by the VM's coroutine timer.
- [x] **Verify:** `:app:assembleDebug` green. Committed.

### Task 7: Mood tab — DONE
**Files:** `ui/screens/MoodScreen.kt`.
- [x] Mood picker (5, selected `scale(1.18)` + toast via vm); journal "boomhut" card (BasicTextField + Bewaren → clears + toast); mood calendar grid (weekday row + 35 seeded cells via `buildCalendar()`).
- [x] **Verify:** `:app:assembleDebug` green. Committed.

### Task 8: Stats tab — DONE
**Files:** `ui/screens/StatsScreen.kt`.
- [x] Two reclaimed cards (36u / +18%), streak bar chart (14 bars, `h=40+round(i/13*58)`), triggers (4 labeled progress bars via `fillMaxWidth(pct)`), urges-beaten weekly (4 gradient bars + "92% deze week 🔥").
- [x] **Verify:** `:app:assembleDebug` green. Committed.

### Task 9: Panic overlay — menu, breathing, cold water, reasons, done
**Files:** `ui/overlays/PanicOverlay.kt`.
- [ ] Green gradient overlay, header "Je bent veilig. Adem." + close. Sub-states: MENU (4 action rows + "Ik heb 'm verslagen ✓"), BREATHING (two breathe-scaling circles 8s + box-breathing copy), WATER (4 steps), REASONS (user reasons), DONE (🎉 celebration → close). Game sub-state handled in Task 10.
- [ ] **Verify:** builds; sub-navigation + breathing animation. Commit `feat: panic overlay (non-game states)`.

### Task 10: Panic mini-games (bubbles, grounding, tap-green)
**Files:** extend `ui/overlays/PanicOverlay.kt` (or `ui/overlays/PanicGames.kt`).
- [ ] Game menu (3 choices). Bubbles: 7 randomly placed bubbles, tap pops + respawns + score. Grounding 5-4-3-2-1: dots fill per step, next when filled, done screen. Tap-green: 3×3 grid, random green target every 900ms, 20s timer tick, score, record persisted to DataStore (`tapRecord`), game-over restart.
- [ ] **Verify:** builds; all three games playable; record persists across launches. Commit `feat: panic mini-games`.

### Task 11: Bored sheet
**Files:** `ui/overlays/BoredSheet.kt`.
- [ ] Bottom-sheet (scrim + slide-up): grabber, title "Vul de leegte 🎯", mini-goal card (Doe 't! → toast + close), 6 quick-action grid (→ affirm + close), journal CTA (→ close + go Mood tab).
- [ ] **Verify:** builds; sheet slides up, actions work. Commit `feat: bored sheet`.

### Task 12: Settings overlay
**Files:** `ui/overlays/SettingsOverlay.kt`.
- [ ] Header + back; name field (persists); reminders list with toggle switches (track/knob animate); reasons editor (textarea rows, remove ✕, "+ Reden toevoegen"). Persist name, reminders, reasons.
- [ ] **Verify:** builds; edits persist across relaunch. Commit `feat: settings overlay`.

### Task 13: Toast + affirmations + animation polish
**Files:** `ui/common/Toast.kt` (finalize), small passes across screens.
- [ ] Toast pop animation (slide+fade, auto-dismiss 2.4s). Confirm affirmations fire on: task complete, mood log, bored actions, goal, journal save. Add the remaining design animations where cheap: hero `fadein`, task `pop`, FAB `pulseRing`, bar `barGrow`, bubble `pop`, sheet `sheetup`.
- [ ] **Verify:** builds; toasts + key animations present. Commit `feat: toasts and animation polish`.

### Task 14: Final fidelity pass
- [ ] Side-by-side against `design-reference/Rise.dc.html` (open it in a browser via the Design runtime, or read values): spacing, radii, shadows, font weights, gradients. Fix drift.
- [ ] Update this plan's checkboxes; write a short `README.md` (how to open/build/run in Android Studio, where the design contract lives).
- [ ] **Verify:** full walk-through builds and matches. Commit `chore: fidelity pass + readme`.

---

## Self-Review notes
- Every design region maps to a task: Home→T5, Rust+Player→T6, Mood→T7, Stats→T8, Panic menu/breath/water/reasons/done→T9, games→T10, Bored→T11, Settings→T12, Plant→T4, nav/FAB/shell→T3, toast→T13, theme→T1, state/persistence→T2, build→T0.
- Persistence scope intentionally minimal (name, reasons, reminders, tapRecord) — matches the design's only real persistence (`localStorage rise_tap_record`) plus the obviously-user-owned settings fields. Streak/stats stay seeded constants as in the design.
- The app is made fluid (dp, fill width) rather than locked to 390×844 — the one deliberate, necessary deviation from the fixed-frame mockup.
```
