# CLAUDE.md

Behavioral guidelines to reduce common LLM coding mistakes. Merge with project-specific instructions as needed.

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

## 0. Geen em-dashes (hard verbod)

Gebruik NOOIT em-dashes (—) of en-dashes (–) in welke output dan ook: UI-tekst, code,
comments, commit messages, docs of chat. Vervang ze door een komma, dubbele punt, punt of
een gewoon koppelteken (-). Vermijd ook de ellipsis (…); schrijf gewoon "...". Dit geldt voor
alle projecten in deze workspace en mag nooit meer gebeuren.

## 1. Think Before Coding

Don't assume. Don't hide confusion. Surface tradeoffs.

Before implementing:

- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them, don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

Minimum code that solves the problem. Nothing speculative.

- No features beyond what was asked. only ask if you think it would be a real good add on
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

Touch only what you must. Clean up only your own mess.

When editing existing code:

- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it, don't delete it.

When your changes create orphans:

- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

Define success criteria. Loop until verified.

Transform tasks into verifiable goals:

- "Add validation" then "Write tests for invalid inputs, then make them pass"
- "Fix the bug" then "Write a test that reproduces it, then make it pass"
- "Refactor X" then "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:

```
1. [Step] -> verify: [check]
2. [Step] -> verify: [check]
3. [Step] -> verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

These guidelines are working if: fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.

---

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Setup op een nieuwe machine

Run deze commando's eenmalig in Claude Code na het clonen van de repo:

### Plugins

**Superpowers** (project-scoped):
```
/plugin marketplace add obra/superpowers
/plugin install superpowers@superpowers-dev
```

**Codex** (user-scoped, OpenAI):
```
/plugin marketplace add https://github.com/openai/codex-plugin-cc.git
/plugin install codex@openai-codex
```

**Ponytail** (laziest-solution review/audit):
```
/plugin marketplace add DietrichGebert/ponytail
/plugin install ponytail@ponytail
```

Daarna: `/reload-plugins`

### Skills

**Impeccable** (adapt, animate, audit, bolder, clarify, colorize, critique, delight, distill, harden, layout, optimize, overdrive, polish, quieter, shape, typeset):
```
/skill install pbakaus/impeccable
```

**Taste** (design-taste-frontend, full-output-enforcement, gpt-taste, high-end-visual-design, industrial-brutalist-ui, minimalist-ui, redesign-existing-projects, stitch-design-taste, image-to-code, imagegen-frontend-web, imagegen-frontend-mobile):
```
npx skills add Leonxlnx/taste-skill
```

**Nano Banana**:
```
/skill install nano-banana
```
Vereist ook `GEMINI_API_KEY` in `.env`.

**Frontend Design**, **ui-ux-pro-max**, **brandkit** en **find-docs** zitten al in git en hoeven niet apart geinstalleerd te worden.

### CLI-tools (in plaats van MCP)

- **Supabase CLI**: per `cms/`-project als dev-dependency, `npm install -D supabase`, aanroepen via `npx supabase ...`. Niet globaal (Supabase blokkeert `npm install -g`).
- **Playwright CLI**: globaal, `npm install -g @playwright/cli@latest`, daarna eenmalig `npx playwright install` voor de browsers. Commando: `playwright-cli <command>` (open, type, press, click, check, screenshot, ...).

---

## Wat dit project is

Dit is een **website builder**, een werkplek om meerdere projecten te bouwen en te onderhouden. Elk project staat in zijn eigen submap met zijn eigen git-repo. Er zijn twee soorten mappen:

- `Websites/`: websites (HTML/CSS/JS, Payload-CMS of WordPress)
- `Projecten/`: losse projecten die geen klassieke website zijn (bv. de Rise Android-app)

```
Websites/
  CV/              <- portfolio/CV website van Beau Nolten
  Van den Dam/     <- Payload-CMS klantsite
  Website soos/    <- WordPress (SCS Duiven)
Projecten/
  Habbit tracker/  <- Rise: on-device Android-app (Kotlin/Compose)
  GlowByGhaiya/    <- website
  <volgende>/      <- elk nieuw project krijgt zijn eigen map + eigen git-repo
```

Beide mappen worden door de parent-repo genegeerd (`.gitignore`): elk project pusht naar zijn eigen GitHub-remote, niet naar de website-builder repo.

---

## Skills gelden voor elk project (verplicht)

De skills staan op **workspace-niveau** geïnstalleerd (`.agents/skills/` en `.claude/skills/`) en zijn daardoor beschikbaar in **elk** project, ongeacht het type: pure HTML/CSS/JS-site, Payload-CMS-klantsite, WordPress-site of de Android-app. Een skill hoort niet bij één project, hij hoort bij de hele werkplek.

**Regel:** wanneer er in welk project dan ook (onder `Websites/` of `Projecten/`) werk gedaan wordt dat onder een skill valt, gebruik die skill. Niet alleen bij websites.

- **UI bouwen/herontwerpen** (een sectie, scherm, component, complete redesign): `/frontend-design`, en daarna de relevante Impeccable/Taste-skills (`/polish`, `/layout`, `/animate`, `/critique`, `/audit`, `/minimalist-ui`, `/high-end-visual-design`, etc.). Dit geldt ook voor de Compose-UI van de Rise-app, niet enkel voor web.
- **Afbeeldingen/iconen/illustraties**: `/nano-banana` (of de `imagegen-*` / `image-to-code` skills).
- **Documentatie van een library/SDK/CLI opzoeken**: `/find-docs` (niet uit het hoofd).
- **Na een feature**: `/simplify` of `/ponytail-review`; **voor mergen**: `/security-review`.

De volledige catalogus met wanneer-te-gebruiken staat verderop onder **## Skills**. De isolatieregel blijft gelden: skills brengen *techniek* over tussen projecten, nooit content, kleuren of branding.

---

## Actief project bepalen

Voordat er iets wordt aangepast in `Websites/` of `Projecten/`, altijd expliciet benoemen welk project in scope is.

**Hoe het actieve project bepalen (in volgorde van prioriteit):**
1. Kijk welke bestanden de gebruiker open heeft in de IDE: een geopend bestand uit `Websites/CV/` betekent CV is in scope
2. Kijk of de gebruiker het project expliciet noemt in zijn bericht
3. Als het na stap 1 en 2 nog onduidelijk is: **vraag het**, doe niets totdat dit duidelijk is

**Nooit aannames doen.** Als de context wisselt (vorige sessie was CV, nu is er ook Van den Dam), altijd het actieve project benoemen voordat er code wordt geschreven. Zo voorkom je dat het verkeerde project per ongeluk wordt aangepast.

---

## Schrijfstijl: verboden tekens

**Gebruik NOOIT het em-dash teken `—` in website-content.** Niet in koppen, bodytekst, meta-descriptions, footers of waar dan ook. Gebruik in plaats daarvan een komma, punt, dubbele punt of herformuleer de zin. Zie ook sectie 0 (hard verbod, geldt overal).

---

## Isolatieregel: elke website is onafhankelijk

**Gebruik NOOIT content, tekst, kleuren of structuur van de ene website bij het bouwen van een andere.**

Wat WEL mag meenemen tussen projecten:
- Technische technieken (glassmorphism, parallax, typed-animatie, i18n-patroon, etc.)
- CSS-patronen en animatie-aanpakken
- JavaScript-structuur die goed werkte

Wat NIET mag overzetten:
- Tekst, namen, koppen, beschrijvingen
- Kleurenpalet / design tokens
- Lettertypes, layout-keuzes
- Branding of persoonlijke info

Elke website begint met een schone lei qua design.

---

## Een nieuwe website starten

1. Maak een nieuwe map aan: `Websites/<projectnaam>/`
2. Gebruik `/frontend-design` voor het initiële ontwerp, geef het de doelgroep, het doel en de sfeer mee
3. Geen build-systeem nodig: pure HTML/CSS/JS tenzij de opdracht iets anders vraagt
4. Sla assets op in `Websites/<projectnaam>/resources/`

---

## Standaard CMS-stack voor klantsites (DE route)

**Voor elke klantsite die door de klant zelf bewerkbaar moet zijn, gebruik deze stack.** Dit is de bewezen aanpak (eerst gebouwd voor Van den Dam, `Websites/Van den Dam/cms/` als referentie/template):

- **CMS: Payload CMS v3** (open source, Next.js, MIT) onder `Websites/<naam>/cms/`. Front-end zit in dezelfde Next.js-app (de `(frontend)` route group) zodat **Live/visual editing** native werkt.
- **Database: Supabase Postgres** via de **transaction pooler (poort 6543)**. Adapter `@payloadcms/db-postgres`. Lokaal mag SQLite (`@payloadcms/db-sqlite`), conditioneel op `DATABASE_URI`.
- **Hosting: Vercel**, functieregio bij de DB-regio zetten (`vercel.json` -> `"regions": ["cdg1"]` voor eu-west).
- **Login: e-mail/wachtwoord** (Payload auth), **NOOIT GitHub-login voor klanten**. Admin-UI in het Nederlands (`i18n: { supportedLanguages: { nl }, fallbackLanguage: 'nl' }`).
- **Tweetalig (NL/EN)** met Payload `localization` + **DeepL auto-vertaling** op opslaan (`cms/src/hooks/autoTranslate.ts`): NL is bron, EN wordt automatisch afgeleid. Sleutel als `DEEPL_API_KEY` env.
- **Concept/publiceer-flow**: Payload drafts (`versions: { drafts: true, max: 20 }`). Aparte **voorbeeld-deployment** (`PREVIEW=1` + API-key) die concepten toont, met "Voorbeeld"-knop in de admin.
- **Auto-rebuild**: Payload `afterChange`-hooks pingen Vercel Deploy Hooks (live op publiceren, preview op elke opslag).
- **Media**: Vercel Blob (`@payloadcms/storage-vercel-blob`) voor afbeeldingen/video's.
- **SEO**: `@payloadcms/plugin-seo` + JSON-LD LocalBusiness.

**Belangrijke valkuilen (uit Van den Dam):** Payload v3 monorepo-template gebruikt `workspace:*`, pin alle `@payloadcms/*` op de echte versie. Voeg `declare module '@payloadcms/next/css'` toe (`src/types.d.ts`) anders faalt de type-check. De auto-vertaal-hook moet de transactie delen (`req` meegeven) en geneste row-`id`'s strippen, anders deadlock/validation error. DB-wachtwoorden/API-keys nooit in git of memory.

Voor simpele, niet-bewerkbare sites blijft pure HTML/CSS/JS prima, de CMS-stack is voor klanten die zelf content beheren.

---

## WordPress-route: bestaande WordPress-sites

**Voor sites die al op WordPress draaien en daar moeten blijven, gebruik deze route.** Dit is de derde route naast pure HTML/CSS/JS en de Payload CMS-stack. De Payload-stack blijft de standaard voor *nieuwe* klantsites; WordPress is voor het onderhouden en herontwerpen van *bestaande* WP-installaties die we niet migreren.

Officiele documentatie als naslagwerk: https://wordpress.org/documentation/

### Referentiesite: SCS Duiven (`Websites/Website soos/`)

De eerste WordPress-site in deze workspace, gebruik hem als template/referentie voor de aanpak:

- **Site:** SCS Duiven (Soos Centrum Senioren), `www.scsduiven.nl`, een seniorensoos. De **doelgroep en de beheerders zijn senioren met minimale WordPress-ervaring**. Dit stuurt elke keuze: alles wat we bouwen moet door hen zelf aanpasbaar blijven zonder iets te breken en zonder mij nodig te hebben.
- **Lokaal draaiend in Local:** de site draait in **Local** (Flywheel) als site `soos`, fysiek in `C:\Users\beaun\Local Sites\soos\app\public`. De projectmap `Websites/Website soos/` is een **directory junction** die daarnaar wijst, bewerken via de projectmap = de live Local-site bewerken, direct previewbaar. De originele Duplicator-backup staat apart in `Websites/Website soos (oude backup)/`.
- **Thema:** `mh-magazine-lite` (parent) met een bestaand **child-thema `mh-magazine-lite-child`**. Eigen aanpassingen bestaan al in `style.css`, `pstyle.css`, een eigen `header.php` en `js/back-to-top.js`.
- **Bewerk-model:** **Classic Editor** + **Simple Custom CSS**-plugin + shortcodes (`display-posts-shortcode`, `list-category-posts`, `current-date`). Bewust zo opgezet zodat niet-technische mensen posts beheren in de vertrouwde editor.
- **Relevante plugins:** Contact Form 7 (formulieren), Yoast SEO, FileBird (media-mappen), Really Simple SSL, Easy WP SMTP, WPFront Scroll Top, Sticky Menu. Backups via UpdraftPlus + Duplicator.

### Gouden regels (verplicht)

- **NOOIT de parent-theme of WP-core bewerken.** Alle code-aanpassingen gaan in het **child-thema** (`wp-content/themes/<thema>-child/`). Core/parent updaten zou eigen werk overschrijven.
- **Bewerkbaarheid boven alles.** Hardcode nooit content (teksten, datums, activiteiten) in theme-bestanden als het ook via de admin kan. De beheerders moeten alles in de Classic Editor / widgets / Simple Custom CSS kunnen wijzigen. Een mooie wijziging die zij niet zelf kunnen onderhouden is een verkeerde wijziging.
- **CSS-keuze:** kleine, door de beheerder aanpasbare styling -> **Simple Custom CSS** (in de admin). Structurele/duurzame styling -> **child-thema `style.css`**. Leg in een wijziging uit waar iets staat.
- **Toegankelijkheid (a11y) is functioneel, niet optioneel:** grote klikdoelen, hoog contrast, leesbare lettergroottes, geen muisafhankelijke interacties. Doelgroep zijn senioren.
- De bestaande regels gelden onverkort: **geen em-dash `—`**, geen focus-outline-troep (globale reset), en de **isolatieregel** (geen content/kleuren/branding van andere sites overnemen).

### Lokaal draaien (preview)

Deze machine heeft **alleen Node + git**, geen PHP, MySQL, Docker of Local WP. Code lezen/bewerken kan zonder, maar **faithful previewen vereist een lokale stack.** Twee opties:

- **Local WP (aanbevolen, geen Docker):** GUI van Flywheel, kan de Duplicator-backup direct importeren. Beste fit voor deze niet-technische context. Vereist eenmalige handmatige installatie.
- **`@wordpress/env` (wp-env):** reproduceerbaar via Docker, code-centrisch. Alleen als Docker Desktop geïnstalleerd wordt.

Tot er een lokale stack staat, werk ik op bestandsniveau in het child-thema en documenteer ik wat in de admin (Simple Custom CSS, widgets, posts) gewijzigd moet worden.

---

## Git & GitHub

De CV-repo heeft zijn eigen `.git` in `Websites/CV/` met remote `https://github.com/Curbi6969/beau-nolten-cv.git`. Elk ander project (Van den Dam, GlowByGhaiya, Habbit tracker, ...) heeft net zo zijn eigen `.git` en eigen remote; de parent-repo tracket ze niet.

**Auto-push staat aan:** na elke `git commit` in een Bash-aanroep vuurt een PostToolUse-hook automatisch `git push` in `Websites/CV/`. Geen handmatige push nodig.

Committen gaat zo:
```bash
cd "c:/Coding/Website builder/Websites/CV"
git add index.html
git commit -m "..."   # push volgt automatisch via hook
```

**ABSOLUUT VERBODEN in alle commits, PR's en GitHub-interacties:**
- Geen `Co-Authored-By: Claude` of andere Claude/Anthropic-attributie
- Geen vermelding van Claude Code, Anthropic of AI-tools in commit messages, PR descriptions, of comments
- Commit messages zijn neutraal en beschrijven alleen de wijziging zelf

---

## Skills

Skills worden aangeroepen met `/skill-naam` of via de `Skill` tool. Ze gelden voor elk project (zie **## Skills gelden voor elk project**).

### `/frontend-design`
**Gebruik voor:** nieuwe pagina's, secties, componenten of complete redesigns bouwen.

Werkt door eerst een duidelijke design-richting te kiezen (brutalist, maximalist, retro-futuristisch, refined minimal, etc.) voordat er een regel code geschreven wordt. Geen generieke AI-slop, elke output heeft een eigen, intentionele stijl.

Geef altijd mee: doel van de pagina, doelgroep, gewenste sfeer. Voor iteraties op bestaand werk: geef de bestaande design tokens mee.

---

### `/nano-banana`
**Gebruik voor:** afbeeldingen genereren of bewerken: thumbnails, icons, diagrammen, illustraties, patronen, foto-edits.

Vereist `GEMINI_API_KEY` in `.env`. Gebruikt Gemini CLI onder de motorkap.

| Wat je wil | Commando intern |
|---|---|
| Afbeelding genereren | `/generate 'prompt'` |
| App-icoon / favicon | `/icon 'beschrijving'` |
| Flowchart / diagram | `/diagram 'beschrijving'` |
| Bestaande afbeelding aanpassen | `/edit bestand.png 'instructie'` |
| Naadloos patroon / texture | `/pattern 'beschrijving'` |
| Foto restaureren | `/restore foto.jpg` |

Output belandt in `./nanobanana-output/`.

---

### Impeccable skills (design-verfijning)

Deze skills werken op bestaand HTML/CSS (of Compose-UI) en verfijnen specifieke aspecten van een ontwerp:

| Skill | Wat het doet |
|---|---|
| `/adapt` | Past het design aan voor andere context of schermgrootte |
| `/animate` | Voegt animaties en motion toe aan UI-elementen |
| `/audit` | Controleert het design op problemen, inconsistenties of zwakke punten |
| `/bolder` | Maakt het design gedurfder, meer impact, minder timide |
| `/clarify` | Vereenvoudigt en verduidelijkt: minder ruis, meer focus |
| `/colorize` | Verbetert of herwerkt het kleurenschema |
| `/critique` | Geeft een kritische designbeoordeling met concrete verbeterpunten |
| `/delight` | Voegt leuke micro-interacties en verrassingselementen toe |
| `/distill` | Reduceert naar de essentie: alles wat er niet toe doet eraf |
| `/harden` | Maakt het design robuuster tegen edge cases en foutsituaties |
| `/layout` | Verbetert de ruimtelijke compositie en lay-out |
| `/optimize` | Optimaliseert voor snelheid en performance |
| `/overdrive` | Drijft het design naar het uiterste: maximaal effect |
| `/polish` | Verfijnt details, spacing, schaduwen en kleine onvolkomenheden |
| `/quieter` | Maakt het design subtieler en rustiger |
| `/shape` | Werkt met vormen, geometrie en visuele structuur |
| `/typeset` | Verbetert typografie: hiërarchie, regelafstand, lettertypes |

---

### Taste skills (design-smaak en richting)

Stijlrichtingen en aesthetic-lagen die op een ontwerp toegepast kunnen worden:

| Skill | Wat het doet |
|---|---|
| `/design-taste-frontend` | Past verfijnde designsmaak toe op frontend-code (anti-slop, v2) |
| `/design-taste-frontend-v1` | De originele v1, alleen voor exacte backward-compat |
| `/gpt-taste` | Aesthetic guidance, helpt bij het kiezen van de juiste designtaal |
| `/high-end-visual-design` | Luxury/premium visueel design, gedetailleerd en verfijnd |
| `/industrial-brutalist-ui` | Industrieel / brutalist design, rauw, direct, functioneel |
| `/minimalist-ui` | Strak minimalistisch design, witruimte, essentie, no-fluff |
| `/redesign-existing-projects` | Redesignt bestaand werk met frisse ogen |
| `/stitch-design-taste` | Genereert een DESIGN.md design-system voor Google Stitch |
| `/ui-ux-pro-max` | Brede UI/UX-intelligentie: stijlen, paletten, font-pairings, stacks |
| `/full-output-enforcement` | Zorgt dat de volledige code-output gegenereerd wordt, geen placeholders |

---

### Beeld-skills (alleen afbeeldingen, geen code)

| Skill | Wat het doet |
|---|---|
| `/brandkit` | High-end brand-guideline boards, logo-systemen, identity decks |
| `/image-to-code` | Genereert eerst design-images, analyseert ze, bouwt de site ernaar |
| `/imagegen-frontend-web` | Premium website-design-referenties, een aparte image per sectie |
| `/imagegen-frontend-mobile` | App-native schermconcepten in een nette phone-mockup |

---

### Overige skills

| Skill | Wanneer gebruiken |
|---|---|
| `/find-docs` | Actuele docs/API/CLI van een library opzoeken (niet uit het hoofd) |
| `/simplify` | Na het toevoegen van een feature: controleert op overbodige code en ruimt op |
| `/ponytail-review` | Review puur op over-engineering: wat kan weg, wat vervangt het |
| `/ponytail-audit` | Hele repo scannen op bloat, geranked wat te schrappen/vereenvoudigen |
| `/security-review` | Audit van pending changes op beveiligingsproblemen |
| `/review` | Reviewt een pull request |
| `/fewer-permission-prompts` | Scant transcripts en voegt veelgebruikte commands toe aan de allowlist |
| `/update-config` | Wijzigt `.claude/settings.local.json`: permissies, env vars, hooks |

---

## Werkwijze bij een nieuw website-project

1. **Briefing**: Wat is het doel? Wie is de doelgroep? Welke sfeer?
2. **`/frontend-design`** aanroepen met die context, het kiest een design-richting
3. **Itereren** met Impeccable/Taste skills voor verfijning
4. **Assets** genereren met `/nano-banana` waar nodig
5. **Testen** in browser (geen build-stap)
6. **Committen** vanuit de juiste `Websites/<naam>/` map
