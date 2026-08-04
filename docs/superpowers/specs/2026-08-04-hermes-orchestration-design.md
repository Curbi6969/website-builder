# Hermes Agent orchestration for apps, websites and games

Date: 2026-08-04
Status: design, awaiting approval
Target: Hermes Agent v0.20.0 (Nous Research), self-hosted at https://hermes.beaunolten.nl/

## 0. Goal

Move the way of working currently done in Claude Code on Windows onto Hermes, so a set of
agents can research, design, build and verify apps, websites and games at the same quality
level as the existing portfolio. Human surface eventually moves to Buzz.

## 1. Verified facts this design rests on

Everything below was checked against the running instance or the official docs, not assumed.

| Fact | Source | Consequence |
|---|---|---|
| Hermes runs on Linux, home `/home/hermes` | Files page of the dashboard | It cannot see `C:\Coding\Website builder`. Git is the only bridge. |
| `projects/`, `.npm`, `.cua-driver` already present | Files page | Node and a computer-use driver are installed. |
| Skills use the agentskills.io spec (`SKILL.md` + `name`/`description` frontmatter) | agentskills.io/specification | The 32 skills in `.agents/skills/` are already valid Hermes skills. No porting. |
| `config.yaml` supports `skills.external_dirs` | Hermes skills docs | One shared skills directory can serve both Claude Code and Hermes. |
| Prompt context tier auto-reads `CLAUDE.md`, `AGENTS.md`, `.hermes.md` | Hermes prompt-assembly docs | Every per-project CLAUDE.md carries over for free. This is the single biggest asset. |
| `delegate_task` children inherit the parent toolset and cannot be given per-call tools or skills | Hermes delegation docs | Subagents are for fan-out, NOT for role specialisation. |
| `delegation.model` can differ from the parent model | Hermes delegation docs | Main cost lever: fan-out on a cheap model. |
| Profiles are isolated instances with own config, model, memory, sessions, gateway | Hermes architecture docs | Profiles are the correct unit for role specialisation. |
| `hermes kanban` does multi-profile assign/dispatch | CLI reference | Built-in hand-off mechanism between roles. |
| Buzz is a supported channel, needs the `buzz` CLI on PATH or `BUZZ_CLI_PATH` | Channels page of the dashboard | The Buzz endgame is a config step. |
| Webhooks channel accepts GitHub/GitLab events | Channels page | Push and PR events can wake an agent. |
| Built-in tools include web search, terminal, file edit, image generation (FAL.ai, 11 models), browser automation, sandboxed Python, vision | Hermes features docs | No custom tooling needed for the research and design roles. |

## 2. Core architectural decision

**Roles are profiles. Parallelism is subagents. Domain knowledge is the repo's own CLAUDE.md.**

The obvious design is one subagent per co-worker: a researcher, a designer, a builder. That
design does not work here, because `delegate_task` children inherit the parent's toolset and
cannot be given their own model or skills per call. A "designer" subagent would have the same
tools and the same expensive model as the builder that spawned it.

Profiles do give isolation: own model, own tools, own skills, own memory, own sessions. So:

- **Role** maps to a Hermes profile.
- **Domain** (Kotlin vs Flutter vs Next.js vs vanilla HTML) needs no configuration at all,
  because Hermes reads each repo's `CLAUDE.md` into the prompt automatically.
- **Parallelism inside one role** maps to `delegate_task` with a cheap `delegation.model`.

That third point is what makes this affordable. Researching six competitors fans out to six
children on a cheap model, and only their summaries return to the parent.

## 3. The roles

Four profiles, phased. Phase 1 proves the quality bar, phase 2 adds the specialists.

### Phase 1

**`forge`** builds. Full toolset: terminal, file edit, git, browser. Top-tier model, this is
where the money goes. Skills: the design skills plus find-docs plus full-output-enforcement.
Owns the repos under `/home/hermes/projects/`.

**`warden`** verifies, and has authority to reject. Tools: browser automation, terminal, read
access. Skills: security-review, ponytail-review, simplify. Mid-tier model. `forge` cannot
close a task; `warden` does, or kicks it back with the failing screenshot attached.

### Phase 2

**`scout`** researches. Tools: web search, browser, no repo write. Cheap or free model, the
Nemotron currently configured is genuinely fine for this. Fans out heavily via delegation.

**`studio`** designs and generates imagery. Tools: image generation, browser, write access to
`resources/` and `assets/` only. Skills: the full Impeccable and Taste sets, brandkit,
imagegen-frontend-web, imagegen-frontend-mobile.

Rationale for phasing: `forge` and `warden` together already produce shippable work. `scout`
and `studio` are cost and quality optimisations on top. Building all four before any of them
has run a real job would be guessing at the interfaces between them.

## 4. The quality gate

This is the part that decides whether the output matches the existing portfolio.

The current quality does not come from the model alone. It comes from five things: the skills,
the per-project CLAUDE.md, a strong model, mandatory Playwright QA, and a human reviewing.
The first three carry over directly. The fourth is already written down as a rule. The fifth is
exactly what automation removes, so it has to be mechanised or the quality drops.

`warden` is that mechanism. Hard gate, not advisory:

1. `forge` finishes and marks the kanban task "needs review".
2. `warden` pulls the branch, builds it, and drives it with Playwright.
3. Screenshots at desktop and mobile widths, both themes, cache busted. This mirrors the
   existing mandatory-QA rule.
4. Pass posts the diff and screenshots to the Buzz channel for a human yes.
5. Fail reopens the task with the failing screenshot attached.

Nothing merges or deploys off the back of `warden` alone. The final yes stays human until this
pipeline has a track record.

## 5. Repository and skill sync

No shared filesystem exists, so git does the work.

- Each project is cloned into `/home/hermes/projects/<name>`. Every project already has its own
  remote, so this needs no new infrastructure.
- The skills directory `.agents/skills/` becomes its own git repo, cloned to
  `/home/hermes/skills-shared`, and referenced from `config.yaml` under `skills.external_dirs`.
  One source of truth, consumed by both Claude Code on Windows and Hermes on Linux.
- Caveat: skills that are pure prompt guidance port perfectly. Skills carrying `scripts/` that
  assume Claude Code tool names need checking one by one. The design and taste skills, which
  are the majority, are pure prompt.

## 6. Models

Staying on the free Nemotron for build work would not reach the required quality bar.

| Profile | Tier | Reason |
|---|---|---|
| `forge` | Top | Writes production code. The one place not to economise. |
| `warden` | Mid to high | Must catch real defects, not rubber-stamp. |
| `scout` | Cheap or free | Summarising search results. Nemotron is fine. |
| `studio` | Mid, plus FAL.ai for images | Image models matter more than the text model here. |
| `delegation.model` (global) | Cheap | Fan-out must not burn top-tier tokens. |

Configure `hermes fallback` so a provider outage does not stall an unattended overnight run.

## 7. Toolchain on the Linux box

| Stack | Feasible on the box | Notes |
|---|---|---|
| Vanilla HTML/CSS/JS (CV, GlowByGhaiya, Turkije) | Yes | Node already present. |
| Next.js (Herritage, Van den Dam cms) | Yes | Already the natural environment. |
| Playwright QA | Yes | Needs `npx playwright install` plus system deps. |
| Flutter (Job-tracker) | Yes | Flutter SDK plus Android SDK. |
| Kotlin/Compose Android (Rise) | Yes | JDK plus Android SDK plus Gradle, headless assemble works. |
| Unity (MegaBonk) | **No** | See below. |

### The Unity exception

Unity does ship a Linux editor, but headless CI use needs a licence activation and the Unity
MCP integration needs a running Editor. This is not worth fighting.

**MegaBonk stays on Windows in Claude Code.** Hermes can still do useful work on it: research,
design direction, asset generation, and writing C# scripts into the repo. It cannot run the
Editor or drive the existing Unity MCP server. Any plan that claims otherwise is wrong.

## 8. Human surface and triggers

- **Buzz**: install the `buzz` CLI from `github.com/block/buzz` on the box, enable the channel,
  one channel per project. Agents post there, humans approve there. Buzz is pre-1.0, so treat
  this as the layer most likely to change under us.
- **Webhooks channel**: GitHub push and PR events wake the relevant profile.
- **Cron**: overnight jobs. Dependency checks, Lighthouse and SEO runs on the live sites, link
  rot checks. Cron jobs can attach skills and deliver results to a channel.
- **Home Assistant** is already connected and stays as-is, unrelated to this pipeline.

## 9. Security

Agents on this box will have a terminal, git push rights, and API keys, behind a
tunnel-exposed dashboard. Non-negotiables:

- Keep Cloudflare Access in front of the dashboard. It is what returned the 403 during this
  investigation, which is correct behaviour.
- Secrets go in `hermes auth` credential pools or the Bitwarden integration, never in a repo
  and never in a skill.
- Deploy credentials stay out of `forge`. Deployment is a separate, human-triggered step until
  the pipeline has earned trust.
- Hermes scans hub-installed skills for prompt injection. Skills sourced from anywhere other
  than the shared repo get read before being enabled.

## 10. Open questions

1. Is the box a VM, an LXC container, or the NAS itself? This decides whether Android SDK and
   Flutter can be installed and whether there is disk for them.
2. Does the `projects/` directory already contain clones, or is it empty?
3. Which model provider accounts exist for the top-tier `forge` model?
4. Confirm whether Buzz self-hosting is wanted, or Block's hosted instance.

## 11. Build order

1. Inspect the box: OS, resources, disk, what is in `projects/`.
2. Skills repo, clone to the box, wire `skills.external_dirs`, verify skills list.
3. Clone one website repo. Vanilla, lowest risk. Confirm CLAUDE.md is picked up.
4. Create `forge`, set a top-tier model, run one real task end to end.
5. Create `warden`, install Playwright, wire the QA gate.
6. Run one full task through both profiles on a real project. This is the go/no-go.
7. Buzz CLI plus channel, one project channel.
8. Add `scout` and `studio`.
9. Add the second and third stacks (Next.js, then Flutter/Android).
10. Cron jobs for maintenance runs.
