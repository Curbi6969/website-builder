# Inspiratie-tab + Routines Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an Inspiratie tab (evidence-based routine cards + non-diagnostic self-checks) and routine-chips on Home, and move Cijfers from the nav to a Home button.

**Architecture:** Single-Activity Compose app, one state owner (`RiseViewModel` → `RiseUiState` StateFlow). New feature = new fields on `RiseUiState` + intents on the VM + new composables driven by that state. Content lives in a static `RoutineCatalog` (like `RiseDefaults`). Persistence via Supabase (`RiseRepository`).

**Tech Stack:** Kotlin 2.0.21, Jetpack Compose (BOM 2024.09.03), Supabase-kt, OpenRouter (image gen). AGP 8.7.3, Gradle 8.11.1.

**Spec:** `Habbit tracker/docs/superpowers/specs/2026-06-27-inspiratie-tab-design.md`

## Global Constraints

- App id `com.rise.app`, `minSdk 24`, `compileSdk 35`, JVM target 17.
- **Build/verify** (run from `Habbit tracker/`): `export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"` then `./gradlew :app:compileDebugKotlin --no-daemon`. System JDK 8 will fail — JBR is required.
- **Single state owner:** add fields to `RiseUiState` + intents on `RiseViewModel`; never a second state owner. Derived values = computed getters/in-composable, not stored.
- **Dutch copy is content** — reproduce the strings in this plan verbatim (emoji included); do not translate/paraphrase.
- **Colors** only from `ui/theme/Color.kt` tokens; **fonts** Fredoka (display) / Nunito (text). Card radius family 20–30dp, matching existing screens.
- **Self-checks are non-diagnostic** — no scores/labels; always show the disclaimer + 113 crisis line on heavy outcomes.
- **Commits:** describe the change only — NO Claude/Anthropic/AI attribution.
- **Do NOT stage the in-progress package rename** (`com.beaunolten.rise` → `com.rise.app`) already in the working tree. Each task commits only its own new/modified feature files by explicit path.
- **Persistence migration** runs via the Supabase MCP (`apply_migration`), not raw SQL files.

---

## File structure

| File | Responsibility | Task |
|---|---|---|
| `app/src/main/java/com/rise/app/data/RoutineCatalog.kt` | Models + static catalogue (routines + self-checks, Dutch copy) | 1 |
| `app/src/main/java/com/rise/app/data/RiseUiState.kt` (modify) | New state fields + computed chips/tasks | 2 |
| `app/src/main/java/com/rise/app/vm/RiseViewModel.kt` (modify) | New intents | 2 |
| `app/src/main/java/com/rise/app/RiseApp.kt` (modify) | Tab enum host + nav swap + overlay host | 3 |
| `app/src/main/java/com/rise/app/ui/screens/HomeScreen.kt` (modify) | Cijfers button + routine chip row | 3,4 |
| `app/src/main/java/com/rise/app/ui/common/InspoCard.kt` | Reusable inspiration card | 5 |
| `app/src/main/java/com/rise/app/ui/screens/InspiratieScreen.kt` | The tab: category chips + grid | 5 |
| `app/src/main/java/com/rise/app/ui/overlays/RoutineDetailOverlay.kt` | Routine detail + add action | 6 |
| `app/src/main/java/com/rise/app/ui/overlays/SelfCheckOverlay.kt` | Self-check flow + feedback + safety | 7 |
| `app/src/main/res/drawable-nodpi/inspo_*.png` | Final illustrations | 8 |
| `data/RiseRepository.kt` (modify) + Supabase migration | Persistence | 9 |

---

### Task 1: Routine data models + catalogue

**Files:**
- Create: `app/src/main/java/com/rise/app/data/RoutineCatalog.kt`

**Interfaces — Produces:**
- `enum class RoutineCategory { OCHTEND, DRANG, ZELFZORG, SLAAP }`
- `enum class InspoAccent { GREEN, LAVENDER, TEAL, YELLOW, CORAL, BERRY }`
- `data class RoutineStep(val time: String, val label: String, val icon: String)`
- `data class Routine(val id: String, val name: String, val category: RoutineCategory, val accent: InspoAccent, val illustration: Int, val why: String, val steps: List<RoutineStep>)`
- `enum class CheckKind { LIKERT_1_5, YES_NO, SCALE_0_10 }`
- `data class SelfCheckQuestion(val text: String, val kind: CheckKind)`
- `data class SelfCheck(val id: String, val name: String, val category: RoutineCategory, val accent: InspoAccent, val illustration: Int, val intro: String, val questions: List<SelfCheckQuestion>, val heavy: (List<Int>) -> Boolean, val feedback: (answers: List<Int>, mood: String?) -> String)`
- `object RoutineCatalog { val routines: List<Routine>; val selfChecks: List<SelfCheck> }`

- [ ] **Step 1: Create the file with enums + data classes + catalogue.**
  Use `illustration = 0` as a temporary placeholder (real drawables wired in Task 8). Content from spec §7 verbatim. Example shape:

```kotlin
package com.rise.app.data

enum class RoutineCategory(val label: String) {
    OCHTEND("Ochtend"), DRANG("Drang"), ZELFZORG("Zelfzorg"), SLAAP("Slaap")
}
enum class InspoAccent { GREEN, LAVENDER, TEAL, YELLOW, CORAL, BERRY }

data class RoutineStep(val time: String, val label: String, val icon: String)
data class Routine(
    val id: String, val name: String, val category: RoutineCategory,
    val accent: InspoAccent, val illustration: Int, val why: String,
    val steps: List<RoutineStep>,
)
enum class CheckKind { LIKERT_1_5, YES_NO, SCALE_0_10 }
data class SelfCheckQuestion(val text: String, val kind: CheckKind)
data class SelfCheck(
    val id: String, val name: String, val category: RoutineCategory,
    val accent: InspoAccent, val illustration: Int, val intro: String,
    val questions: List<SelfCheckQuestion>,
    val heavy: (List<Int>) -> Boolean,
    val feedback: (List<Int>, String?) -> String,
)

object RoutineCatalog {
    val routines = listOf(
        Routine("ochtend", "Ochtendroutine — Sterke start", RoutineCategory.OCHTEND, InspoAccent.GREEN, 0,
            "Koppel kleine stappen aan wakker worden (implementatie-intenties + habit stacking).",
            listOf(
                RoutineStep("07:30", "Glas water bij opstaan", "💧"),
                RoutineStep("07:35", "1 minuut ademhaling", "🌬️"),
                RoutineStep("07:40", "Noem 1 ding waar je dankbaar voor bent", "🙏"),
                RoutineStep("07:45", "Zet je intentie voor vandaag", "✨"),
            )),
        // ... drang, zelfcompassie, avond, dankbaarheid, 1procent — exactly per spec §7.1
    )
    val selfChecks = listOf(
        SelfCheck("halt", "HALT-check", RoutineCategory.DRANG, InspoAccent.TEAL, 0,
            "Vier korte vragen. Geen score — gewoon checken wat je nu nodig hebt.",
            listOf(
                SelfCheckQuestion("Heb je honger?", CheckKind.YES_NO),
                SelfCheckQuestion("Ben je boos of geïrriteerd?", CheckKind.YES_NO),
                SelfCheckQuestion("Voel je je eenzaam?", CheckKind.YES_NO),
                SelfCheckQuestion("Ben je moe?", CheckKind.YES_NO),
            ),
            heavy = { it.count { a -> a == 1 } >= 3 },
            feedback = { ans, _ -> /* build supportive Dutch message per answers */ "..." }),
        // ... zelfcompassie (LIKERT_1_5 x5), drang (SCALE_0_10), stemming — per spec §7.2
    )
}
```

- [ ] **Step 2: Compile.** From `Habbit tracker/`: `./gradlew :app:compileDebugKotlin --no-daemon` → Expected: BUILD SUCCESSFUL.
- [ ] **Step 3: Commit.**

```bash
git add "Habbit tracker/app/src/main/java/com/rise/app/data/RoutineCatalog.kt"
git commit -m "Rise: routine + self-check catalogue (data layer)"
```

---

### Task 2: State fields + VM intents (in-memory)

**Files:**
- Modify: `app/src/main/java/com/rise/app/data/RiseUiState.kt`
- Modify: `app/src/main/java/com/rise/app/vm/RiseViewModel.kt`

**Interfaces:**
- Consumes: `RoutineCatalog`, `TaskItem` (Task 1 / existing).
- Produces (on `RiseUiState`):
  - `activeRoutineId: String = "personal"`, `addedRoutineIds: List<String> = emptyList()`, `inspoCategory: RoutineCategory? = null`, `openRoutine: String? = null`, `openSelfCheck: String? = null`
  - computed `val activeRoutineTasks: List<TaskItem>` — `"personal"` → existing `tasks`; else map the catalogue routine's `steps` to `TaskItem(id = namespaced, time, label, icon, done=false)` with `id = (routineId.hashCode() * 100 + index)`.
  - computed `data class RoutineChip(val id: String, val name: String)` + `val homeChips: List<RoutineChip>` = `[RoutineChip("personal","Persoonlijk")] + addedRoutineIds.mapNotNull { id -> RoutineCatalog.routines.find{it.id==id}?.let{ RoutineChip(it.id,it.name) } }`
- Produces (VM intents): `setActiveRoutine(id: String)`, `addRoutine(catalogId: String)`, `removeRoutine(id: String)`, `setInspoCategory(cat: RoutineCategory?)`, `openRoutine(id: String)`, `closeRoutine()`, `openSelfCheck(id: String)`, `closeSelfCheck()`.

- [ ] **Step 1:** Add the 5 fields + 2 computed (`activeRoutineTasks`, `homeChips`) + `RoutineChip` to `RiseUiState`.
- [ ] **Step 2:** Add the 8 intents to `RiseViewModel`, each `_state.update { it.copy(...) }`. `addRoutine` appends if absent; `removeRoutine` drops it and resets `activeRoutineId` to `"personal"` if it was active. (No persistence yet — Task 9.)
- [ ] **Step 3: Compile.** `./gradlew :app:compileDebugKotlin --no-daemon` → BUILD SUCCESSFUL.
- [ ] **Step 4: Commit.**

```bash
git add "Habbit tracker/app/src/main/java/com/rise/app/data/RiseUiState.kt" "Habbit tracker/app/src/main/java/com/rise/app/vm/RiseViewModel.kt"
git commit -m "Rise: routine state fields + intents"
```

---

### Task 3: Nav restructure + Cijfers button + empty Inspiratie tab

**Files:**
- Modify: `app/src/main/java/com/rise/app/data/RiseUiState.kt` (Tab enum)
- Modify: `app/src/main/java/com/rise/app/RiseApp.kt`
- Create (stub): `app/src/main/java/com/rise/app/ui/screens/InspiratieScreen.kt`
- Modify: `app/src/main/java/com/rise/app/ui/screens/HomeScreen.kt`

**Interfaces:**
- Produces: `Tab.INSPIRATIE`; `@Composable fun InspiratieScreen(state: RiseUiState, vm: RiseViewModel)`.

- [ ] **Step 1:** `enum class Tab { HOME, RUST, MOOD, STATS, INSPIRATIE }`.
- [ ] **Step 2:** In `RiseApp.kt` `when(tab)` add `Tab.INSPIRATIE -> InspiratieScreen(state, vm)`. In `BottomNav`, replace the `Cijfers`/`Tab.STATS` `ExpandableTab` with `ExpandableTab(Icons.Rounded.AutoAwesome, "Inspiratie", active == Tab.INSPIRATIE) { onTab(Tab.INSPIRATIE) }` (import `androidx.compose.material.icons.rounded.AutoAwesome`). Keep `Stemming` + the others.
- [ ] **Step 3:** Stub `InspiratieScreen` = a `Column` with a `Text("Inspiratie", Fredoka …)` title (grid added Task 5).
- [ ] **Step 4:** In `HomeScreen` header `Row`, add a third `CircleButton("📊", Card, elevated = true, label = "Cijfers")` → `vm.goTab(Tab.STATS)`.
- [ ] **Step 5: Compile + visual.** Build, run app: nav shows `Inspiratie` not `Cijfers`; 📊 on Home opens the stats screen; back returns Home.
- [ ] **Step 6: Commit.**

```bash
git add "Habbit tracker/app/src/main/java/com/rise/app/data/RiseUiState.kt" "Habbit tracker/app/src/main/java/com/rise/app/RiseApp.kt" "Habbit tracker/app/src/main/java/com/rise/app/ui/screens/InspiratieScreen.kt" "Habbit tracker/app/src/main/java/com/rise/app/ui/screens/HomeScreen.kt"
git commit -m "Rise: nav swap Cijfers->Inspiratie, Cijfers button on Home"
```

---

### Task 4: Home routine chip row

**Files:**
- Modify: `app/src/main/java/com/rise/app/ui/screens/HomeScreen.kt`

**Interfaces:**
- Consumes: `state.homeChips`, `state.activeRoutineId`, `state.activeRoutineTasks`, `vm.setActiveRoutine`.

- [ ] **Step 1:** Add a horizontally-scrollable chip `Row` (`Modifier.horizontalScroll(rememberScrollState())`) directly above "Plan van vandaag", one chip per `state.homeChips`. Chip styling mirrors `ExpandableTab`: active = `Green.copy(alpha=0.15f)` bg + `GreenDark` text, inactive = `Card` bg + `border(1.dp, CheckBorder)` + `InkSoft` text; `RoundedCornerShape(16.dp)`; Nunito ExtraBold 12.5sp; `pressable { vm.setActiveRoutine(chip.id) }`.
- [ ] **Step 2:** Change the task list to render `state.activeRoutineTasks` instead of `state.tasks`; keep `${state.doneCount}/${state.taskTotal}` but compute from `activeRoutineTasks` (add `activeDoneCount`/`activeTaskTotal` computed getters on state, or inline `.count{it.done}`/`.size`).
- [ ] **Step 3: Compile + visual.** `Persoonlijk` chip present and active; tasks unchanged for it. (Added routines appear after Task 6.)
- [ ] **Step 4: Commit.**

```bash
git add "Habbit tracker/app/src/main/java/com/rise/app/ui/screens/HomeScreen.kt" "Habbit tracker/app/src/main/java/com/rise/app/data/RiseUiState.kt"
git commit -m "Rise: routine chips on Home"
```

---

### Task 5: Inspiratie grid + InspoCard

**Files:**
- Create: `app/src/main/java/com/rise/app/ui/common/InspoCard.kt`
- Modify: `app/src/main/java/com/rise/app/ui/screens/InspiratieScreen.kt`

**Interfaces:**
- Produces: `@Composable fun InspoCard(title: String, illustration: Int, accent: InspoAccent, onClick: () -> Unit)`; helper `fun InspoAccent.color(): Color` mapping to tokens (GREEN→Green, LAVENDER→PurpleSoft, TEAL→Teal, YELLOW→YellowB, CORAL→Orange, BERRY→BerryA).

- [ ] **Step 1:** `InspoCard` = portrait `Column`, `clip(RoundedCornerShape(24.dp))`, `cardSurface`; top = `Image(painterResource(illustration))` (or accent-colored `Box` placeholder while `illustration==0`) at ~3:4; bottom = accent-colored footer strip with the title (Fredoka SemiBold 15sp, white). `pressable { onClick() }`.
- [ ] **Step 2:** `InspiratieScreen`: title + subtitle; category chip row (`Alles` + `RoutineCategory.values()`), selecting calls `vm.setInspoCategory`; then a 2-column grid (two `Column`s in a `Row`, or `FlowRow`) of cards from `RoutineCatalog.routines` + `RoutineCatalog.selfChecks` filtered by `state.inspoCategory`. Routine card → `vm.openRoutine(id)`; self-check card → `vm.openSelfCheck(id)`.
- [ ] **Step 3: Compile + visual.** Cards render (color placeholders ok); category filter works.
- [ ] **Step 4: Commit.**

```bash
git add "Habbit tracker/app/src/main/java/com/rise/app/ui/common/InspoCard.kt" "Habbit tracker/app/src/main/java/com/rise/app/ui/screens/InspiratieScreen.kt"
git commit -m "Rise: Inspiratie grid + cards"
```

---

### Task 6: Routine detail overlay + add to routines

**Files:**
- Create: `app/src/main/java/com/rise/app/ui/overlays/RoutineDetailOverlay.kt`
- Modify: `app/src/main/java/com/rise/app/RiseApp.kt` (overlay host + BackHandler)

**Interfaces:**
- Consumes: `state.openRoutine`, `state.addedRoutineIds`, `vm.addRoutine`, `vm.closeRoutine`, `vm.setActiveRoutine`.

- [ ] **Step 1:** Overlay (full-screen scrim `Box` like existing overlays): illustration, name, `why` text, the `steps` list (reuse `TaskRow` visuals or a simple labelled row), and a primary button. If `id !in addedRoutineIds`: "Voeg toe aan mijn routines" → `vm.addRoutine(id)` + `vm.setActiveRoutine(id)` + toast "Toegevoegd ✓" + `vm.closeRoutine()`. Else: disabled "Toegevoegd ✓".
- [ ] **Step 2:** In `RiseApp.kt` add `if (state.openRoutine != null) RoutineDetailOverlay(state, vm)` to the overlay host; add `state.openRoutine != null` to BackHandler enable + `vm.closeRoutine()` branch (z-order: above tab, below panic/player).
- [ ] **Step 3: Compile + visual.** Open a routine → add → new chip appears on Home and becomes active showing its steps.
- [ ] **Step 4: Commit.**

```bash
git add "Habbit tracker/app/src/main/java/com/rise/app/ui/overlays/RoutineDetailOverlay.kt" "Habbit tracker/app/src/main/java/com/rise/app/RiseApp.kt"
git commit -m "Rise: routine detail overlay + add to routines"
```

---

### Task 7: Self-check overlay + feedback + safety

**Files:**
- Create: `app/src/main/java/com/rise/app/ui/overlays/SelfCheckOverlay.kt`
- Modify: `app/src/main/java/com/rise/app/RiseApp.kt` (overlay host + BackHandler)

**Interfaces:**
- Consumes: `state.openSelfCheck`, `state.moodPicked`, `vm.closeSelfCheck`, `vm.openRoutine`.

- [ ] **Step 1:** Overlay with local `remember` answer state: render `intro`, then questions by `CheckKind` (YES_NO = two pill buttons; LIKERT_1_5 = 1–5 row; SCALE_0_10 = slider/0–10 row). A "Klaar" button computes `feedback(answers, state.moodPicked)` and shows the feedback screen.
- [ ] **Step 2:** Feedback screen: the supportive message, a suggestion button where relevant (e.g. HALT → "Start Drang-routine" via `vm.openRoutine("drang")`), the disclaimer "Dit is geen medisch advies of diagnose.", and — if `selfCheck.heavy(answers)` — a crisis card: "Heb je nu iemand nodig? Bel 113 (gratis, 24/7) of gebruik de Hulp-knop." Close via `vm.closeSelfCheck()`.
- [ ] **Step 3:** Wire into `RiseApp.kt` overlay host + BackHandler (`state.openSelfCheck != null` → `vm.closeSelfCheck()`).
- [ ] **Step 4: Compile + visual.** Each self-check runs end to end; heavy path shows the 113 card.
- [ ] **Step 5: Commit.**

```bash
git add "Habbit tracker/app/src/main/java/com/rise/app/ui/overlays/SelfCheckOverlay.kt" "Habbit tracker/app/src/main/java/com/rise/app/RiseApp.kt"
git commit -m "Rise: self-check overlay with supportive feedback + safety"
```

---

### Task 8: Final illustrations

**Files:**
- Create: `app/src/main/res/drawable-nodpi/inspo_ochtend.png` (+ `inspo_drang`, `inspo_zelfcompassie`, `inspo_avond`, `inspo_dankbaarheid`, `inspo_1procent`, `inspo_halt`, `inspo_zelfcompassie_check`, `inspo_drang_check`, `inspo_stemming`)
- Modify: `app/src/main/java/com/rise/app/data/RoutineCatalog.kt` (`illustration = R.drawable.inspo_*`)

- [ ] **Step 1:** Update `scratchpad/gen_set.py` prompts: add "**vertical portrait 3:4, full-bleed, no border, no card frame**"; keep the approved calm Rise palette (greens/cream/terracotta + per-card accent). Generate all card images via OpenRouter (`google/gemini-2.5-flash-image`, `OPENROUTER_API_KEY`).
- [ ] **Step 2:** Resize/crop to portrait ~768px wide with Pillow; save into `res/drawable-nodpi/` with the `inspo_*` names above.
- [ ] **Step 3:** Set each catalogue `illustration` to its `R.drawable.inspo_*`; remove the color-placeholder branch in `InspoCard`.
- [ ] **Step 4: Compile + visual.** `./gradlew :app:compileDebugKotlin` green; cards show real art, full-bleed.
- [ ] **Step 5: Commit.**

```bash
git add "Habbit tracker/app/src/main/res/drawable-nodpi/inspo_"*.png "Habbit tracker/app/src/main/java/com/rise/app/data/RoutineCatalog.kt" "Habbit tracker/app/src/main/java/com/rise/app/ui/common/InspoCard.kt"
git commit -m "Rise: Inspiratie illustrations"
```

---

### Task 9: Persistence (Supabase)

**Files:**
- Supabase migration (via MCP `apply_migration`)
- Modify: `app/src/main/java/com/rise/app/data/RiseRepository.kt`
- Modify: `app/src/main/java/com/rise/app/vm/RiseViewModel.kt` (call repo in intents)

- [ ] **Step 1: Migration** `inspiratie_routines` — create `user_routines (user_id uuid references auth.users, routine_id text, position int, primary key(user_id, routine_id))` with RLS `auth.uid() = user_id` (select/insert/delete); `alter table profiles add column active_routine text default 'personal'`; `alter table task_completions add column routine_id text default 'personal'` and update the unique/conflict key to `(user_id, date, routine_id, task_id)`.
- [ ] **Step 2:** `RiseRepository`: add `UserRoutineRow`; methods `addRoutine(id)` (upsert with next position), `removeRoutine(id)` (delete), `setActiveRoutine(id)` (update profiles), and include `routine_id` in `setTaskDone`. Extend `load()` + `RemoteState` with `addedRoutineIds` + `activeRoutineId`; load today's completions per active routine.
- [ ] **Step 3:** VM intents `addRoutine`/`removeRoutine`/`setActiveRoutine`/`toggleTask` call the repo in `viewModelScope` (fire-and-forget like existing intents). Apply `RemoteState` additions at startup load.
- [ ] **Step 4: Compile + visual.** Add a routine, switch chip, toggle a task, kill & relaunch → selection + completions persist.
- [ ] **Step 5: Commit.**

```bash
git add "Habbit tracker/app/src/main/java/com/rise/app/data/RiseRepository.kt" "Habbit tracker/app/src/main/java/com/rise/app/vm/RiseViewModel.kt"
git commit -m "Rise: persist routines + per-routine task completions"
```

---

### Task 10 (optional polish)

- [ ] Card press-scale + grid fade-in; chip switch crossfade on the task list; `contentDescription` on cards/chips; empty-state copy if a category has no cards. Compile + visual + commit `"Rise: Inspiratie polish"`.

---

## Self-review

- **Spec coverage:** §4.1 nav→T3; §4.2 chips+Cijfers→T3/T4; §4.3 grid/overlays→T5/T6/T7; §4.4 mood feedback→T7; §5 components→all; §6 model/state/intents→T1/T2; §6.4 persistence→T9; §7 content→T1; §8 safety→T7; §9 illustrations→T8. ✓
- **Placeholders:** `illustration=0` is an explicit, tracked temporary resolved in T8 (not a vague TODO). Catalogue/feedback bodies reference spec §7 verbatim copy. ✓
- **Type consistency:** `activeRoutineTasks`, `homeChips`, `RoutineChip`, intent names, and `InspoAccent.color()` mapping are used consistently across T2/T4/T5/T6. ✓
