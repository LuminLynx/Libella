# Phase 3–4 Roadmap — What Comes After the Units

> **Scope.** Forward-looking plan for what happens after each unit
> publishes through public launch. Synthesizes
> `docs/STRATEGY.md` § Features and § Phase 3/4 work clusters,
> `docs/EXECUTION.md` Phase 3 + Phase 4, the Phase 4 launch-
> readiness checklist in `docs/BACKEND_BEST_PRACTICES.md`, and
> several decisions surfaced mid-session that hadn't been written
> to disk yet.
>
> **Status.** Living doc. Updated as Phase 3 engineering kicks off
> and Phase 4 begins. Stubs at the bottom get filled in as each
> implementation begins.
>
> **For future sessions.** Read this alongside `STRATEGY.md`,
> `EXECUTION.md`, and `docs/curriculum/v1-path-outline.md` during
> standard onboarding. The point of this file is that *"what
> happens after the units finish"* shouldn't live only in chat
> transcripts.

---

## Where we are (as of 2026-05-14)

- **Phase 1 complete** (Foundation — spine of the loop).
- **Phase 2 complete** (Unit-1 pilot — grader proven, gate passed).
- **Phase 3 in progress** — 9 of 20 units published. F5 / F6 not
  started. Authoring is the pacing constraint per STRATEGY.md T1.

Published units:

| # | Slug | Status |
|---|---|---|
| 1 | tokenization | ✅ |
| 2 | context-window | ✅ |
| 3 | latency | ✅ |
| 4 | evals | ✅ |
| 5 | model-selection | ✅ |
| 6 | prompt-design | ✅ |
| 7 | hallucination | ✅ |
| 8 | cost-dynamics | ✅ |
| 9 | customization-trilemma | ✅ |
| 10–20 | (locked per `v1-path-outline.md`, not yet authored) | — |

---

## The four work tracks ahead

After Unit 20 ships, three tracks of work remain before public
launch:

1. **Phase 3 engineering** — F5 / F6 / S1 / S2 / S3.
2. **Phase 4 launch-readiness** — trademark, web preview, beta
   cohort, dashboards, app store.
3. **Backend launch-readiness items** — deferred per
   `docs/BACKEND_BEST_PRACTICES.md` §  *Launch-readiness checklist*.

Authoring tracks 10–20 continues in parallel with track 1 (Phase
3 engineering). The handoff established that *"authoring is the
pacing constraint per STRATEGY.md T1"* — engineering has slack
capacity that should be used for F5/F6/S1/S2/S3 in parallel with
the remaining 11 unit-authoring cycles.

---

## Track 1 — Phase 3 engineering (start whenever)

Per `docs/EXECUTION.md` § Phase 3 work cluster.

### F6 — Path overview / "you are here"

**What it is.** Visual representation of the path with locked /
unlocked / completed states. Loop step 5 from `STRATEGY.md`.

**Scope.** Bounded Android UI work. Backend already done — the
`GET /api/v1/paths/{id}` endpoint returns the unit list with
positions, slugs, statuses, and per-user completion state. The
`prereq_unit_ids` field on each unit gives the locked/unlocked
state derivation.

**Open decisions (lock in F6_PATH_OVERVIEW.md before
implementation):**

- Surface shape: dedicated screen vs. expandable view from path
  home.
- Locked-state messaging: how does *"complete Unit X first"* render?
- Completed-unit tap behavior: re-open in read-only review mode,
  or open the unit reader normally?
- Progress affordance: *"9 of 20"* counter, progress bar, both, or
  neither?

**Estimate.** ~1–2 weeks Android-only at the current authoring
cadence. Cleanest part of Phase 3 engineering.

### F5 — Spaced review scheduler

**What it is.** Loop step 6 from `STRATEGY.md` — spaced review of
older units shows up the next day, alongside the next new unit.
STRATEGY explicitly says *"without it, the path evaporates."*

**Scope.** Cross-stack. DB scaffolding already there (migration
020 created `review_schedule` table: user_id, unit_id, due_at,
interval, last_reviewed_at).

What's not built:

- **Algorithm pick.** Three options from least to most
  sophisticated:
  - *Interval-doubling* (1 day → 3 → 7 → 21 → 60). ~50 lines
    backend. Honest default for v1.
  - *SM-2* (classic Anki algorithm; quality 0–5 → interval).
    ~150 lines.
  - *FSRS* (modern Anki replacement; better but stateful).
    ~400 lines + tuning.
- **Backend: scheduling logic.** When `POST /api/v1/completions`
  lands, write a `review_schedule` row. Add a quality-signal
  endpoint that advances or resets the interval.
- **Backend: read endpoint.** `GET /api/v1/review-schedule?due_before=NOW`.
  AUDIT.md § 2.4 flagged this as a required new endpoint.
- **Android: path home integration.** *"Continue"* surface needs
  to show 0–N due-for-review pills alongside the next new unit.
- **Android: review surface.** *What does reviewing actually feel
  like?* Three options:
  - (a) Same unit reader with a *"review mode"* banner.
  - (b) Stripped-down *"just the bite"* surface with a
    comprehension nudge.
  - (c) Re-run the decision prompt and re-grade.
  The session handoff explicitly noted *"the Return-step format
  decision belongs to Phase 3 when F5 is actually built"*.

**Open decisions (lock in F5_SPACED_REVIEW.md before
implementation):** algorithm choice, review-surface shape, and
quality-signal source (how do we know the user *"remembered"*?).

**Estimate.** Interval-doubling + endpoints + Android pill UI +
review surface (option b): ~2–3 weeks. SM-2 or FSRS adds 1–2
weeks.

### Sequencing: F6 before F5

The session handoff established this — F6 is *more contained;
sequence it before F5*. Three reasons documented here so the
rationale isn't lost:

1. **F6 is bounded Android UI work with backend already
   complete.** Low risk; ships fast.
2. **F6 gives users (and the founder, demoing) a visible artifact
   of path progress.** F5's value is invisible until weeks of
   completion data accumulate.
3. **F5's review-surface decision (a/b/c above) benefits from F6
   shipping first.** Once users can see the whole path, the
   question *"what does revisiting a unit feel like?"* gets
   concrete.

Both can run parallel with Units 10–20 authoring.

### S1 — Glossary side-door

**What it is.** Per AUDIT.md § 1.5 — demote the legacy Browse /
Categories / Search top-level tabs into a single *"see related"*
surface accessible from inside a unit.

**Scope.** Mostly Android navigation rework. The three legacy
screens (`BrowseTermsScreen`, `CategoriesScreen`, `SearchScreen`)
collapse to one combined glossary side-door. Backing data
(`terms`, `categories` tables) stays; just the prominence drops.

**Estimate.** ~1 week Android.

### S2 — Interactive widgets (flagship units only)

**What it is.** Per STRATEGY § S2 — *flagship units only*, not
every unit. Tokenization already has Bundle 0 widgets
(TokenizerPlayground, BpeWalkthrough, PitfallDemos). Other
flagships may want similar.

**Scope.** Decide per-flagship whether a widget earns its
editorial cost. Context Window is flagship-tier and could have
e.g., a recall-vs-position visualization. Other flagships TBD.

**Estimate.** Open. Founder call on each flagship.

### S3 — Settings polish

**What it is.** Per AUDIT.md § 1.9 — inherits as-is from
chunk-1 work. Minor edits remove links to deleted screens (AI
Tools, Trend Watcher, contribution flows).

**Estimate.** Hours.

### Phase 3 exit gate

Per `docs/EXECUTION.md` § Phase 3 decision gate:

- All 20 units published, each with the required regression-set
  tier met (≥ 20 flagship, ≥ 10 standard, never zero).
- All Spine features (F1–F7) shipping.
- Sidewall features (S1, S2 flagship-only, S3) shipping.
- End-to-end: a user can start at Unit 1, complete the full path
  on Android, see spaced reviews of earlier units appearing on
  schedule.
- Founder reviews the full path as a learner — cover-to-cover
  read, not a checkbox.
- Editorial quality bar held across every unit.

Then Phase 4 opens.

---

## Track 2 — Phase 4 launch-readiness

Per `docs/EXECUTION.md` § Phase 4. Most of this is **not coding
work**.

### Trademark check on Libella

**Workstream.** External — attorney engagement, not internal
code. STRATEGY § Naming flags it as non-optional before public
launch. Primary risk to weigh: *Libelle IT Group* (different
spelling, different industry).

**Estimate.** Calendar wait + attorney fee. Not on the engineering
critical path; start it when Phase 4 opens because clearance can
take weeks.

### Q4 — Web preview

**What it is.** Per STRATEGY Architecture Q4 — read-only,
shareable unit links, no auth. Marketing-flavored. Same content
DB the Android app reads.

**Scope.** New surface, new tech-stack decision. Open questions:

- Static-site generator vs server-rendered framework?
- URL shape (e.g., `libella.dev/units/context-window`)?
- What subset of unit content renders? Bite + depth (no decision
  prompt — there's no grader in the web flow)?
- Deploy target (Railway, Vercel, GitHub Pages)?

**Open decisions (lock in Q4_WEB_PREVIEW.md before
implementation):** all of the above, plus how marketing-flow
links (open graph, sharing previews) compose.

**Estimate.** ~2–3 weeks if SSG-based and stripped-down. Longer if
fancier.

### Landing page + marketing copy

**Voice.** Per STRATEGY § 3 — use the *external (kind)* register,
not the *internal (harsh)*. Aligned to the Value Proposition. Not
a long doc.

### Beta cohort recruitment

**Target.** 20–50 α-PM users with real stakes — career-switchers,
current PMs, founders, product-adjacent execs.

**Open process gap.** How do feedback signals flow back? Survey
in-app, periodic interview, lightweight Discord? Pick at Phase 4
boundary.

### Cost monitoring dashboards

**Primary metric.** Cache hit rate (load-bearing on unit economics
per T2-E). Threshold: alarm if it drops below ~70% — that's the
*"costs balloon"* signal from EXECUTION.md.

**Other metrics worth tracking:** per-session LLM spend, per-user
LLM spend, p95 grading latency, regression-set drift (if a unit's
regression set agreement drops over time, something changed).

**Tooling.** Open. Railway has basic metrics; might need
Grafana / a small custom dashboard.

### App store listing prep

**Standard playbook.** Description, screenshots, category,
keywords, app icon at multiple sizes. Aligned to positioning.

### Phase 4 exit gate

Per `docs/EXECUTION.md` § Phase 4:

- Trademark cleared (or fallback name committed, or launch
  deferred).
- Web preview live and shareable.
- Beta cohort signed up and onboarded.
- Cost dashboards in place; first sustainable per-session cost
  figure recorded.
- Founder commits to public launch.

---

## Track 3 — Backend launch-readiness items

Per `docs/BACKEND_BEST_PRACTICES.md` § Launch-readiness checklist
(Phase 4). Deferred items, not load-bearing for closed beta but
should be revisited at the Phase 4 boundary:

- **Token TTL + refresh flow.** Access tokens are 30-day; consider
  shorter-lived access + refresh-token rotation for blast-radius
  reduction.
- **Token revocation.** No `jti`/denylist or rotating token version
  today. Add for *"log out all devices"* / account-compromise
  response.
- **Connection pooling.** `db.py` opens a fresh psycopg connection
  per request. `psycopg_pool` is a small drop-in.
- **Flavor-based base URLs (Android).** Client has one hardcoded
  Railway URL. Phase 4 wants dev / staging / prod build flavors
  plus a release-time check that debug endpoints don't ship in
  production builds.
- **Streaming the rationale (TT2).** STRATEGY flags this as a UX
  improvement; not a gate criterion. Can land any time.

Each of these is ~few hours to a day. Cumulatively maybe 1 week.

---

## Track 4 — Post-launch (after public launch)

### Units 16–20 lock from real beta signal

Per `docs/curriculum/v1-path-outline.md` — Units 16–20 are
explicit placeholders, *"locked when the closed-beta cohort
surfaces what real users actually struggle with."*

**Process (lightweight, to follow once beta runs):**

1. Beta cohort interviews / feedback after each completes the
   first 15 units.
2. Surface 5–10 recurring *"I wish the path had…"* themes.
3. Map themes to one of the placeholder slots (16–20) plus the
   four working-hypotheses already drafted in `v1-path-outline.md`.
4. Lock one slot at a time per the same discipline as Units 1–9
   (lock outline → author slot (a) → bite → depth → regression
   set → gate run → publish).

Don't lock all five at once. Beta signal is the load-bearing
input here; design-from-the-armchair locks would defeat the
purpose of leaving them placeholder in the first place.

### v2 conversation

STRATEGY explicitly defers iOS, multiple paths, longer-form
content, and several other items to *"a v2 conversation."*
Re-open after v1 has been live for at least one quarter and has
real cohort data.

---

## Design-doc stubs (fill in before each implementation begins)

The discipline locked in PR #99 — *"lock before slot (a) begins,
position rationale written first"* — applies to engineering work
too. Each of these stubs gets filled in (and re-committed) before
its implementation begins:

### `docs/engineering/F6_PATH_OVERVIEW.md` — TBD

Lock before F6 implementation. Covers: surface shape, state
machine for locked / unlocked-but-incomplete / completed,
completed-unit tap behavior, locked-state messaging, progress
affordance.

### `docs/engineering/F5_SPACED_REVIEW.md` — TBD

Lock before F5 implementation. Covers: algorithm choice
(interval-doubling vs SM-2 vs FSRS) with rationale, review-surface
shape (option a/b/c), quality-signal source (how the system knows
the user "remembered" — implicit from completion-replay, explicit
self-rating, grader re-run).

### `docs/engineering/Q4_WEB_PREVIEW.md` — TBD

Lock before Phase 4 web-preview implementation. Covers: tech stack
(SSG vs server-rendered), URL shape, content subset that renders,
deploy target, sharing/open-graph composition.

---

## Critical-path estimate (rough)

From `EXECUTION.md` § Critical path: *"engineering across all
phases is roughly 3 months total. Authoring across Phases 2–3 is
3–6 months."* The dominant remaining work is authoring 11 units
(positions 10–20).

At the cadence observed so far (Unit 9 took roughly one week of
authoring + gate-run + publish cycle), 11 more units is ~11
weeks. F5 + F6 + S1 + S2 + S3 in parallel with authoring is
~4–6 weeks of engineering capacity, fully absorbed by parallel
work.

After Unit 20: roughly 3–4 weeks of Phase 4 work (mostly
external/operational, not coding), plus 1–2 weeks of backend
launch-readiness items.

**External waits** (not under engineering control): trademark
attorney clearance, app store review, beta cohort recruitment.
These run in parallel with Phase 4 internal work.

Net: **~3 months from today to public launch readiness** if
authoring cadence holds.

---

## Provenance

This doc was added 2026-05-14 to capture forward-looking
synthesis that previously lived only in a chat transcript and
was therefore vulnerable to session loss. The synthesis pulls
from:

- `docs/STRATEGY.md` § Features, § Architecture Q4, § Open
  sub-decisions.
- `docs/EXECUTION.md` § Phase 3, § Phase 4, § Critical path.
- `docs/AUDIT.md` § 2.4 (new endpoints), § 2.7 (new tables).
- `docs/BACKEND_BEST_PRACTICES.md` § Launch-readiness checklist.
- `docs/curriculum/v1-path-outline.md` — Units 16–20 placeholder
  rationale + the process retrospectives that establish the
  *"lock before authoring begins"* pattern this doc extends to
  engineering.
- The original session-handoff brief from the session that opened
  on 2026-05-14 — F6-before-F5 sequencing rationale and the
  Return-step format note.
