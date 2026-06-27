# Inspiratie-tab + routines op Home — Design Spec

**Date:** 2026-06-27
**Project:** Rise (`Habbit tracker/`) — native Android, Kotlin + Jetpack Compose
**Status:** Draft for review

---

## 1. Doel

Drie samenhangende toevoegingen aan Rise, gebouwd op **bewezen methodes** (zie §3):

1. **Inspiratie-tab** met twee soorten kaarten in een nieuwe illustratiestijl:
   - **Routine-kaarten** — kant-en-klare routines die je met één tik aan je eigen routines toevoegt.
   - **Self-check-kaarten** — korte, **niet-diagnostische** check-ins die ondersteunende, op onderzoek gebaseerde feedback geven over hoe je je voelt.
2. **Routines als chips op Home** — een chiprij (`Persoonlijk` = je standaardroutine, + toegevoegde routines). Tik een chip om te wisselen welke routine je takenlijst op Home toont.
3. **Navigatie-herschikking** — `Cijfers` verdwijnt uit de bottom-nav en wordt een knop op Home; de vrijgekomen navplek wordt `Inspiratie`.

### Niet-doelen (out of scope)
- Geen klinische diagnostiek of screening (bewust — zie §8 Veiligheid).
- Geen zelf-ontworpen routines/editor in v1 (alleen catalogus + `Persoonlijk`). Custom routines = latere fase.
- Geen wijziging aan het plant-hero, panic-flow of bestaande stijl van andere schermen.

---

## 2. Uitgangspunten uit de bestaande app

- **Eén state owner:** `vm/RiseViewModel` → `data/RiseUiState` (`StateFlow`). Nieuwe features = veld op `RiseUiState` + intent op de VM. Geen tweede state-eigenaar. (Vastgelegd in `Habbit tracker/CLAUDE.md`.)
- **Tabs:** `enum Tab { HOME, RUST, MOOD, STATS }`; nav-shell in `RiseApp.kt` switcht op `state.tab`. Bottom-nav = 4 tabs + centrale `Hulp`-knop.
- **Taken nu:** `RiseDefaults.tasks` = vaste lijst van 4 `TaskItem(id, time, label, icon, done)`. Alleen de *done*-status per dag wordt bewaard (`task_completions` in Supabase, RLS per user).
- **Persistentie:** Supabase via `RiseRepository` (geen DataStore meer). Tabellen o.a. `profiles`, `task_completions`, `mood_logs`, `daily_checkins`, `urge_events`.
- **Design-contract:** Dutch copy verbatim, kleur-tokens in `ui/theme/Color.kt`, fonts Fredoka/Nunito. Nieuwe Dutch copy in deze spec volgt dezelfde informele, warme toon (`je`, emoji).

---

## 3. Onderbouwing — bewezen methodes

Alle content is gebaseerd op onderzoek (research uitgevoerd 2026-06-27):

**Gedragsverandering / gewoontes (James Clear, *Atomic Habits* / NL "De 1% methode" / "Elementaire gewoontes"):**
- De 4 wetten: maak het *duidelijk, aantrekkelijk, makkelijk, bevredigend*.
- **Cue → routine → reward**-lus.
- **Implementatie-intenties** (Gollwitzer): "Ik doe X om TIJD op PLEK."
- **Habit stacking** ("Na [bestaande gewoonte], doe ik [nieuwe gewoonte]") — koppelt nieuwe stappen aan bestaande ankers; betrouwbaarder dan tijd-cues. Bouwt op BJ Fogg's *Tiny Habits*.
- 1% beter per dag → ~37× per jaar.

**Herstel / terugvalpreventie (evidence-based):**
- **HALT** (Hungry, Angry, Lonely, Tired) — basisbehoefte-check bij drang.
- **Urge surfing** — drang als golf waarnemen die altijd weer zakt (zit al in Rise's panic-flow als `urgeSurf`).
- **MBRP** (Mindfulness-Based Relapse Prevention) — ~31% lagere terugval.
- Dankbaarheid (3 punten/dag) → betere stemming; 5-zintuigen grounding (al in Rise als `groundSteps`); parasympathische ademhaling (al in Rise).

**Zelfcompassie > zelfwaardering (Kristin Neff):**
- 3 componenten: *vriendelijkheid naar jezelf* (i.p.v. zelfkritiek), *gedeelde menselijkheid*, *mindfulness*.
- Self-Compassion Scale (SCS) als basis voor een respectvolle, niet-diagnostische self-check.
- Kerninzicht (de door de gebruiker genoemde NL-insteek): *zelfcompassie heeft dezelfde voordelen als zelfwaardering, maar zonder de nadelen.*

**Bronnen:** James Clear / Atomic Habits (habi.app, jamesclear.com), Gollwitzer implementatie-intenties, BJ Fogg Tiny Habits, HALT/urge surfing & MBRP (recovery-literatuur), Kristin Neff zelfcompassie (self-compassion.org, zelfcompassie.nl).

---

## 4. Informatie-architectuur & UX

### 4.1 Navigatie-herschikking
- **Bottom-nav nieuw:** `Start · Rust · [Hulp] · Stemming · Inspiratie`.
- `Cijfers` (Stats) blijft bestaan als scherm en `Tab.STATS`, maar is **niet** meer in de nav. Bereikbaar via een **knop op Home** (zie 4.2).
- `Tab` enum krijgt `INSPIRATIE`. `RiseApp.kt` `when(tab)` handelt nu 5 tabs af; back-gedrag ongewijzigd (niet-HOME → HOME).

### 4.2 Home (Start) — wijzigingen
- **Cijfers-knop:** kleine kaart/knop in de header-rij (naast ⚙️ en 🙂) óf als kaart onder "Plan van vandaag" met label "Cijfers bekijken 📊" → `vm.goTab(Tab.STATS)`. *Keuze in implementatie; voorkeur: derde CircleButton "📊" in de header.*
- **Routine-chips:** nieuwe horizontaal scrollbare chiprij **boven** "Plan van vandaag".
  - Eerste chip: `Persoonlijk` (de huidige standaardroutine; altijd aanwezig, niet verwijderbaar).
  - Daarna één chip per toegevoegde routine (naam uit catalogus).
  - Actieve chip = gevuld (Green 15% bg + GreenDark tekst), inactief = omrand. Zelfde stijl als de nav-tabs.
  - Tik chip → `vm.setActiveRoutine(id)` → "Plan van vandaag" toont de taken van die routine.
  - Lang-indrukken op een toegevoegde chip → bevestiging "Verwijderen?" (geen voor `Persoonlijk`). *Optioneel v1.*
- "Plan van vandaag" rendert `state.activeRoutineTasks` i.p.v. de vaste `state.tasks`.

### 4.3 Inspiratie-tab (`InspiratieScreen.kt`)
- **Titel:** "Inspiratie" + subtitel "Routines en check-ins die werken".
- **Categorie-chips** bovenaan (horizontaal scrollbaar): `Alles · Ochtend · Drang · Zelfzorg · Slaap`. Filtert de kaarten.
- **Kaartgrid** (2 koloms, zoals de referentie), portret-illustratie boven, titel onderaan op gekleurde voet. Twee typen:
  - **Routine-kaart** → tik → **routine-detail-overlay**: illustratie, naam, korte "waarom dit werkt"-tekst (met de methode), de stappenlijst, en knop **"Voeg toe aan mijn routines"**. Na toevoegen: toast "Toegevoegd ✓" + nieuwe chip op Home. Al toegevoegd → knop toont "Toegevoegd ✓" (disabled).
  - **Self-check-kaart** → tik → **self-check-overlay**: 3–5 korte vragen → **feedbackscherm** met ondersteunende, op onderzoek gebaseerde boodschap, gekoppeld aan je stemming (zie 4.4).

### 4.4 Self-check → "feedback over mijn stemming"
Dit realiseert de oorspronkelijke vraag ("klik en krijg feedback over mijn mood"):
- Korte check (bv. zelfcompassie: 5 stellingen op een 1–5 schaal; of HALT: 4 ja/nee).
- Resultaat = **geen score/diagnose**, maar een **warme, concrete reflectie** + 1 suggestie (vaak: "Wil je hier een routine voor? →" die naar de bijbehorende routine-kaart linkt, of "Start urge surfen 🌊").
- Feedback verwijst naar de huidige stemming uit de Stemming-tab indien aanwezig (`state.moodPicked`), bv. "Je gaf vandaag *Matig* aan — dat klopt met wat je net invulde. Wees zacht voor jezelf."
- Optioneel: knop "Bewaar in Stemming" → schrijft naar `mood_logs` (hergebruikt `vm.pickMood`). *v1: alleen tonen, niet verplicht opslaan.*

---

## 5. Componenten (isolatie & duidelijkheid)

Elk bestand één duidelijke taak:

| Bestand (nieuw) | Verantwoordelijkheid |
|---|---|
| `data/RoutineCatalog.kt` | Statische catalogus: prebuilt `Routine`s + `SelfCheck`s (alle Dutch copy, methode-uitleg, stappen, vragen, feedback). Net als `RiseDefaults`. |
| `data/Routine.kt` (of in `RiseUiState.kt`) | Datamodellen: `Routine`, `RoutineStep`, `SelfCheck`, `SelfCheckQuestion`, `RoutineCategory`, accent-enum. |
| `ui/screens/InspiratieScreen.kt` | De tab: categorie-chips + kaartgrid. Rendert vanuit catalogus + state. |
| `ui/overlays/RoutineDetailOverlay.kt` | Routine-detail + "Voeg toe"-actie. Overlay-patroon zoals bestaande overlays. |
| `ui/overlays/SelfCheckOverlay.kt` | Vragenflow + feedbackscherm. |
| `ui/common/InspoCard.kt` | Herbruikbare kaart (illustratie + titel-voet + accent). Gebruikt door het grid. |
| `ui/screens/HomeScreen.kt` (edit) | Voeg routine-chiprij + Cijfers-knop toe; render `activeRoutineTasks`. |
| `RiseApp.kt` (edit) | Nav: vervang Cijfers door Inspiratie; voeg `Tab.INSPIRATIE` toe aan host + overlay-host voor de 2 nieuwe overlays. |

**Overlays** volgen het bestaande patroon: gedreven door velden op `RiseUiState` (bv. `openRoutine: String?`, `openSelfCheck: String?`), ge-z-stackt in `RiseApp.kt`, gesloten via BackHandler.

---

## 6. Datamodel & state

### 6.1 Modellen (catalogus)
```kotlin
enum class RoutineCategory { OCHTEND, DRANG, ZELFZORG, SLAAP }      // chip-filters; "Alles" = geen filter
enum class InspoAccent { GREEN, LAVENDER, TEAL, YELLOW, CORAL, BERRY } // mapt naar Color.kt-tokens

data class RoutineStep(val time: String, val label: String, val icon: String)
data class Routine(
    val id: String, val name: String, val category: RoutineCategory,
    val accent: InspoAccent, val illustration: Int /* drawable res */,
    val why: String,                 // "waarom dit werkt" (methode)
    val steps: List<RoutineStep>,
)
data class SelfCheckQuestion(val text: String, val kind: Kind /* LIKERT_1_5 | YES_NO */)
data class SelfCheck(
    val id: String, val name: String, val category: RoutineCategory,
    val accent: InspoAccent, val illustration: Int,
    val intro: String, val questions: List<SelfCheckQuestion>,
    val feedback: (answers) -> String,   // ondersteunende boodschap, mag state.moodPicked gebruiken
)
```

### 6.2 `RiseUiState` toevoegingen
```kotlin
val activeRoutineId: String = "personal",
val addedRoutineIds: List<String> = emptyList(),   // catalogus-id's die de user toevoegde
val inspoCategory: RoutineCategory? = null,        // null = "Alles"
val openRoutine: String? = null,
val openSelfCheck: String? = null,
// computed:
val homeChips: List<RoutineChip>  // [Persoonlijk] + addedRoutineIds → naam
val activeRoutineTasks: List<TaskItem> // Persoonlijk → huidige RiseDefaults.tasks; anders catalogus-steps → TaskItem
```
`Persoonlijk` = de bestaande `tasks`-lijst (ongewijzigd gedrag). Een toegevoegde routine levert zijn `steps` als `TaskItem`s met **namespaced ids** (`routineId.hashCode()*100 + index`) zodat `task_completions` per routine uniek blijft.

### 6.3 VM-intents (nieuw)
`setActiveRoutine(id)`, `addRoutine(catalogId)`, `removeRoutine(id)`, `setInspoCategory(cat?)`, `openRoutine(id)`/`closeRoutine()`, `openSelfCheck(id)`/`closeSelfCheck()`. Patroon: `_state.update { it.copy(...) }`, plus repo-call waar persistentie nodig is.

### 6.4 Persistentie (Supabase)
Nieuwe tabellen (RLS `auth.uid() = user_id`, zoals bestaande):
- `user_routines (user_id, routine_id text, position int)` — welke catalogus-routines toegevoegd zijn + volgorde.
- `profiles.active_routine text default 'personal'` — actieve chip.
- `task_completions` krijgt extra kolom `routine_id text default 'personal'`; `onConflict = "user_id,date,routine_id,task_id"`.
- Self-check-uitkomsten worden **niet** apart bewaard in v1 (alleen optioneel als mood). Geen nieuwe tabel nodig.

`RiseRepository`: `load()` laadt `user_routines` + `active_routine`; nieuwe methods `addRoutine`, `removeRoutine`, `setActiveRoutine`. `RemoteState` krijgt `addedRoutineIds` + `activeRoutineId`.

> Migratie: 1 SQL-migratie (nieuwe tabel + 2 kolommen). Via Supabase MCP `apply_migration`.

---

## 7. Content-catalogus (Dutch, bewezen methodes)

### 7.1 Routines (prebuilt)
1. **Ochtendroutine — Sterke start** · `OCHTEND` · green · `inspo_ochtend`
   *Waarom:* implementatie-intenties + habit stacking — koppel kleine stappen aan wakker worden.
   - 07:30 · "Glas water bij opstaan" · 💧
   - 07:35 · "1 minuut ademhaling" · 🌬️
   - 07:40 · "Noem 1 ding waar je dankbaar voor bent" · 🙏
   - 07:45 · "Zet je intentie voor vandaag" · ✨
2. **Drang de baas — HALT & golf** · `DRANG` · teal · `inspo_drang`
   *Waarom:* HALT dekt basisbehoeften; urge surfing — de golf zakt altijd.
   - "Check HALT: honger, boos, eenzaam, moe?" · 🔍
   - "Drink water of eet iets kleins" · 🍎
   - "Surf de golf — 8 min" · 🌊
   - "App iemand of loop naar buiten" · 🚶
3. **Zelfcompassie — Wees zacht voor jezelf** · `ZELFZORG` · lavender · `inspo_zelfcompassie`
   *Waarom:* Neff's 3 componenten; zachter en effectiever dan zelfkritiek.
   - "Hand op je hart, 3 diepe ademhalingen" · 💚
   - "Zeg: dit is een moeilijk moment" · 🌧️
   - "Zeg: moeilijk hebben hoort bij mens-zijn" · 🤝
   - "Vraag: wat heb ik nu nodig?" · 🌱
4. **Avond wind-down — Zacht afsluiten** · `SLAAP` · yellow · `inspo_avond`
   *Waarom:* schermrust + dankbaarheid + reflectie → betere slaap, minder terugval.
   - 21:00 · "Scherm weg, 30 min voor bed" · 📵
   - 21:15 · "Schrijf 3 dingen die goed gingen" · ✍️
   - 21:30 · "Avond check-in" · 🌙
   - 21:45 · "Slaap & loslaten meditatie" · 😴
5. **Dankbaarheid — 3 goede dingen** · `ZELFZORG` · coral · `inspo_dankbaarheid`
   *Waarom:* dagelijkse dankbaarheid verbetert stemming en herstelmotivatie.
   - "Schrijf 3 concrete dingen op" · ✍️
   - "Waarom maakte het je blij?" · 💭
   - "Stuur 1 iemand een bedankje" · 💌
6. **1% beter — Mini-gewoontes** · `OCHTEND` · green · `inspo_1procent`
   *Waarom:* Atomic Habits — maak het zó klein dat falen onmogelijk is, koppel aan een anker.
   - "Kies 1 mini-gewoonte (max 2 min)" · 🎯
   - "Koppel het aan iets dat je al doet" · 🔗
   - "Doe het direct na je anker" · ▶️
   - "Vink af, vier de win" · 🎉

### 7.2 Self-checks (niet-diagnostisch)
1. **Zelfcompassie-check** · lavender — 5 stellingen (1–5), bv. "Als ik faal, ben ik streng voor mezelf", "Ik gun mezelf rust als het zwaar is". Feedback: warm, normaliserend, verwijst naar zelfcompassie-routine.
2. **HALT-check** · teal — 4 ja/nee (honger/boos/eenzaam/moe). Feedback: per geraakte behoefte een concrete suggestie + optie "Start Drang-routine".
3. **Drang-check** · teal — "Hoe sterk is je drang nu?" (0–10). Feedback bij hoog: "De golf zakt altijd weer — wil je samen surfen? 🌊" → urge surfing.
4. **Stemming-check** · berry — korte reflectie die `state.moodPicked` ophaalt en spiegelt; suggestie op maat.

> Exacte copy-strings worden in de implementatie definitief gemaakt en in `RoutineCatalog.kt` gezet (verbatim, zoals het design-contract vereist voor bestaande copy).

---

## 8. Veiligheid & ethiek

- **Niet-diagnostisch:** geen "depressietest" / "trauma-test" zoals de referentie-app. Alle self-checks zijn ondersteunende check-ins, expliciet zonder score of label.
- **Disclaimer** onderaan elke self-check: "Dit is geen medisch advies of diagnose."
- **Crisis-vangnet:** bij een zware uitkomst (bv. drang ≥ 8, of stemming "Zwaar") toont de feedback ook: "Heb je nu iemand nodig? Bel 113 (Zelfmoordpreventie, gratis, 24/7) of de Hulp-knop." Sluit aan op de bestaande `Hulp`-knop.
- **IP/branding:** illustraties zijn zelf gegenereerd in Rise's eigen palet; geen overgenomen assets/copy/branding van de referentie-app (conform de isolatieregel in de workspace-CLAUDE.md). De referentie levert alleen *stijl- en patroon*-inspiratie.

---

## 9. Illustratie-pipeline

- **Model:** Nano Banana = `google/gemini-2.5-flash-image` via **OpenRouter** (`OPENROUTER_API_KEY` in `.env`). De gratis `GEMINI_API_KEY` kan dit niet (image = `limit: 0`).
- **Stijl (goedgekeurd 2026-06-27):** rustige flat-vector character-illustratie, Rise-palet (greens/cream/terracotta + 1 in-systeem accent per kaart), zachte cel-shading, subtiele paper-grain, geen harde outlines, vrolijk-ondersteunend.
- **Productie-fixes t.o.v. de previews:** portret **3:4**, **full-bleed (geen kaartrand/witte border)**, ruimte bovenaan voor de titel.
- **Output:** `app/src/main/res/drawable-nodpi/inspo_*.png`, ~512px breed (Pillow-resize), één per routine/self-check.
- **Script:** hergebruik `scratchpad/gen_set.py` (al werkend); approved previews staan in `RESOURCES/style-previews/`.

---

## 10. Fasering (voorgestelde build-volgorde)

1. **Nav + Home-chips fundament** — `Tab.INSPIRATIE`, Cijfers-knop op Home, lege Inspiratie-tab, `Persoonlijk`-chip (alleen bestaande taken). Verifieer: build groen, nav werkt, chip toont taken.
2. **Routine-catalogus + toevoegen** — `RoutineCatalog`, `InspoCard`, grid + categorie-chips, `RoutineDetailOverlay`, "Voeg toe" → chip op Home. Persistentie (`user_routines`, `active_routine`, `routine_id` op completions).
3. **Self-checks** — `SelfCheckOverlay`, 4 checks, feedback + veiligheid/crisis.
4. **Illustraties** — genereer de definitieve 3:4 full-bleed assets, plaats in `res/`, koppel aan catalogus.
5. **Polish** — animaties (kaart-pop, chip-wissel-fade), a11y labels.

Elke fase: `./gradlew :app:compileDebugKotlin` groen + visuele check tegen dit ontwerp.

---

## 11. Open beslissingen

- Cijfers-knop: header-CircleButton (📊) vs. losse kaart onder de takenlijst? *Voorstel: CircleButton.*
- `Persoonlijk` bewerkbaar maken (eigen taken toevoegen/aanpassen)? *Voorstel: niet in v1.*
- Self-check-uitkomsten loggen voor trends in Cijfers? *Voorstel: niet in v1.*
