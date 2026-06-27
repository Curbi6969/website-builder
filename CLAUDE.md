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
- If multiple interpretations exist, present them — don't pick silently.
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
- If you notice unrelated dead code, mention it — don't delete it.

When your changes create orphans:

- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

Define success criteria. Loop until verified.

Transform tasks into verifiable goals:

- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:

```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

These guidelines are working if: fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.

---

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Wat dit project is

Dit is een **website builder** — een werkplek om meerdere websites te bouwen en te onderhouden. Elke website staat in zijn eigen submap onder `Websites/`.

```
Websites/
  CV/          ← portfolio/CV website van Beau Nolten
  <volgende>/  ← elke nieuwe website krijgt zijn eigen map
```

---

## Actieve website bepalen

Voordat er iets wordt aangepast in `Websites/`, altijd expliciet benoemen welke website in scope is.

**Hoe de actieve website bepalen (in volgorde van prioriteit):**
1. Kijk welke bestanden de gebruiker open heeft in de IDE — een geopend bestand uit `Websites/CV/` betekent CV is in scope
2. Kijk of de gebruiker de website expliciet noemt in zijn bericht
3. Als het na stap 1 en 2 nog onduidelijk is: **vraag het** — doe niets totdat dit duidelijk is

**Nooit aannames doen.** Als de context wisselt (vorige sessie was CV, nu is er ook Van den Dam), altijd de actieve website benoemen vóórdat er code wordt geschreven. Zo voorkom je dat de verkeerde website per ongeluk wordt aangepast.

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
2. Gebruik `/frontend-design` voor het initiële ontwerp — geef het de doelgroep, het doel en de sfeer mee
3. Geen build-systeem nodig: pure HTML/CSS/JS tenzij de opdracht iets anders vraagt
4. Sla assets op in `Websites/<projectnaam>/resources/`

---

## Git & GitHub

De CV-repo heeft zijn eigen `.git` in `Websites/CV/` met remote `https://github.com/Curbi6969/beau-nolten-cv.git`.

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

Skills worden aangeroepen met `/skill-naam` of via de `Skill` tool.

### `/frontend-design`
**Gebruik voor:** nieuwe pagina's, secties, componenten of complete redesigns bouwen.

Werkt door eerst een duidelijke design-richting te kiezen (brutalist, maximalist, retro-futuristisch, refined minimal, etc.) vóór er een regel code geschreven wordt. Geen generieke AI-slop — elke output heeft een eigen, intentionele stijl.

Geef altijd mee: doel van de pagina, doelgroep, gewenste sfeer. Voor iteraties op bestaand werk: geef de bestaande design tokens mee.

---

### `/nano-banana`
**Gebruik voor:** afbeeldingen genereren of bewerken — thumbnails, icons, diagrammen, illustraties, patronen, foto-edits.

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

Deze skills werken op bestaand HTML/CSS en verfijnen specifieke aspecten van een ontwerp:

| Skill | Wat het doet |
|---|---|
| `/adapt` | Past het design aan voor andere context of schermgrootte |
| `/animate` | Voegt animaties en motion toe aan UI-elementen |
| `/audit` | Controleert het design op problemen, inconsistenties of zwakke punten |
| `/bolder` | Maakt het design gedurfder, meer impact, minder timide |
| `/clarify` | Vereenvoudigt en verduidelijkt — minder ruis, meer focus |
| `/colorize` | Verbetert of herwerkt het kleurenschema |
| `/critique` | Geeft een kritische designbeoordeling met concrete verbeterpunten |
| `/delight` | Voegt leuke micro-interacties en verrassingselementen toe |
| `/distill` | Reduceert naar de essentie — alles wat er niet toe doet eraf |
| `/layout` | Verbetert de ruimtelijke compositie en lay-out |
| `/optimize` | Optimaliseert voor snelheid en performance |
| `/overdrive` | Drijft het design naar het uiterste — maximaal effect |
| `/polish` | Verfijnt details, spacing, schaduwen en kleine onvolkomenheden |
| `/quieter` | Maakt het design subtieler en rustiger |
| `/shape` | Werkt met vormen, geometrie en visuele structuur |
| `/typeset` | Verbetert typografie — hiërarchie, regelafstand, lettertypes |

---

### Taste skills (design-smaak & richting)

Stijlrichtingen en aesthetic-lagen die op een ontwerp toegepast kunnen worden:

| Skill | Wat het doet |
|---|---|
| `/design-taste-frontend` | Past verfijnde designsmaak toe op frontend-code |
| `/full-output-enforcement` | Zorgt dat de volledige code-output gegenereerd wordt, geen placeholders |
| `/gpt-taste` | Aesthetic guidance — helpt bij het kiezen van de juiste designtaal |
| `/high-end-visual-design` | Luxury/premium visueel design — gedetailleerd en verfijnd |
| `/industrial-brutalist-ui` | Industrieel / brutalist design — rauw, direct, functioneel |
| `/minimalist-ui` | Strak minimalistisch design — witruimte, essentie, no-fluff |
| `/redesign-existing-projects` | Redesignt bestaand werk met frisse ogen |
| `/stitch-design-taste` | Combineert losse design-elementen tot een coherent geheel |

---

### Overige skills

| Skill | Wanneer gebruiken |
|---|---|
| `/simplify` | Na het toevoegen van een feature: controleert op overbodige code en ruimt op |
| `/security-review` | Audit van pending changes op beveiligingsproblemen |
| `/review` | Reviewt een pull request |
| `/fewer-permission-prompts` | Scant transcripts en voegt veelgebruikte commands toe aan de allowlist |
| `/update-config` | Wijzigt `.claude/settings.local.json` — permissies, env vars, hooks |

---

## Werkwijze bij een nieuw website-project

1. **Briefing** — Wat is het doel? Wie is de doelgroep? Welke sfeer?
2. **`/frontend-design`** aanroepen met die context — het kiest een design-richting
3. **Itereren** met Impeccable/Taste skills voor verfijning
4. **Assets** genereren met `/nano-banana` waar nodig
5. **Testen** in browser (geen build-stap)
6. **Committen** vanuit de juiste `Websites/<naam>/` map
