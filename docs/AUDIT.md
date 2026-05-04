# Phase 0 — Cleanup Audit

**Status:** Draft for founder sign-off — revision pass applied 2026-05-04.
**Anchored to:** [`STRATEGY.md`](./STRATEGY.md) and [`EXECUTION.md`](./EXECUTION.md).
**Branch:** `claude/audit-revision-I7CvQ` (this revision).
**Revision summary:** Original draft on `claude/cleanup-audit-YUHEh`. This pass
addresses review push-backs without re-litigating locked strategy: explicit
data-preservation policy in §2.6, clarified Phase 0 vs Phase 2 provider-switch
scope in §5 #6, MCQ-as-Return-step option surfaced in §1.8 + §5 #7, fresh
Phase 1 mocks justified for §1.11, consistent archive-or-delete policy across
§3 + §5 #4, new sub-sections for AndroidManifest (§1.13), Resources expanded
(§1.14), CI/CD pipelines absent (§1.15), and forward-looking Phase 1 test
scaffolding (§7).

This audit walks the FOSS-101 repo and classifies every meaningful module/file
against the locked Libella strategy as one of:

- **KEEP** — survives near as-is under the new strategy.
- **RESHAPE** — the asset (code, data, or UI) has reusable substance but must
  be repointed at the path-centric model, the new loop, or the new authoring
  pipeline before it earns its place in v1.
- **DELETE** — has no place in v1 under the locked wedge (P1 + P2 primary)
  and the *path, not catalog* soul. Removal is the cleanup; not a strategic
  decision waiting to be re-litigated.

The cut list below is recommendation only — **no deletions until founder
sign-off** (per the EXECUTION.md Phase 0 decision gate).

---

## 0. Scoring lens

A file is judged on whether it serves the *six-step session arc* (Continue →
Bite → Decide → Calibrate → Progress → Return) in the path-centric product, or
whether it serves the old retrieval-soul glossary product. Anything that
exists only to support the old soul (term-centric Browse / Categories / Search
as front-door, AI-tools tab, contribution flows, learning-style picker) is in
**DELETE** unless its substance is reusable (which earns RESHAPE).

The S1 sidewall (glossary entries accessible *from inside a unit* as "see
related") is the only place stale glossary-shaped UI is allowed to keep
breathing — and even there, the *home* tabs are demoted, not the data.

---

## 1. Android client (`app/src/main/java/com/example/foss101`)

### 1.1 App shell + theme — **KEEP**

| File | Verdict | Notes |
|---|---|---|
| `MainActivity.kt` | KEEP | Plain Compose host, repository init. No coupling to retrieval soul. |
| `ui/theme/Color.kt` `Shape.kt` `Theme.kt` `Type.kt` | KEEP | Stack-level theme, reusable as-is. May want a polish pass during Phase 4 launch readiness, not now. |
| `ui/components/AppScaffold.kt` | KEEP | Generic scaffold + screenContentPadding helper, no domain coupling. |
| `ui/components/PrimaryActionButton.kt` `SecondaryActionButton.kt` `TertiaryActionButton.kt` | KEEP | Pure-presentation wrappers, reusable. |
| `ui/components/SectionHeader.kt` `TagChip.kt` `NavigationTile.kt` `ScreenState.kt` (Loading/Error/Empty) | KEEP | Generic UI primitives. |

### 1.2 Navigation — **RESHAPE**

| File | Verdict | Notes |
|---|---|---|
| `navigation/AppNav.kt` | RESHAPE | Currently routes to `home`, `browse`, `categories`, `search`, `details/{termId}`, `ai_tools`, `trend_watcher`, `ask_glossary`, `settings`, `auth_login`, `auth_signup`, `term_draft?query=`, `preview_tokenization`, `preview_tokenization_bite`. Under the new strategy: keep `home`, `settings`, `auth_*`. Replace `details/{termId}` with `unit/{unitId}`. Demote (move into "see related" inside a unit, not top-level) `browse`, `categories`, `search`. Delete `ai_tools`, `trend_watcher`, `ask_glossary` as front-door routes, `term_draft`. The two `preview_tokenization*` routes survive only as the seed for the Tokenization flagship unit and should fold into the unit reader once that exists. |

### 1.3 Home / path entry point — **RESHAPE**

| File | Verdict | Notes |
|---|---|---|
| `ui/home/HomeScreen.kt` | RESHAPE | Today it is a *retrieval-shaped* tile grid (AI Learning Layer, Ask Glossary, Browse, Categories, Search, Settings) plus a "Concept previews" section. Under P3 + Loop step 1 this becomes a **path-shaped** "Continue your path" home: next unit card, your-position-on-path, optional spaced-review pill. The current "Concept previews" section conflicts with the explicit cut in `STRATEGY.md` § Features and must go. |

### 1.4 Unit reader (the F2 spine) — **RESHAPE**

| File | Verdict | Notes |
|---|---|---|
| `ui/details/TermDetailsScreen.kt` (~hundreds of lines including segmented preset pickers, scenario/challenge generators, completion grading) | RESHAPE → **gut and rebuild** | This screen carries most of the old soul: it picks a `LearningPreset` (the Quick recap / Interview prep / Hands-on coding / Conceptual deep-dive style picker that is **explicitly cut**), invokes `generateScenario` / `generateChallenge`, and runs an in-app per-criterion checkbox grader. The shell (term title, definition, related-terms, action-bar layout) is reusable raw material for F2's bite + depth-on-tap reader; everything wired through `LearningPreset`, `LearningScenario`, `LearningChallenge`, and the in-app self-grading widgets must come out and be replaced by Loop steps 2–5 (bite, decision prompt, LLM grader, calibration, complete). Easier to scaffold a new `ui/unit/UnitReaderScreen.kt` and lift the surviving layout pieces over than to surgically edit. |
| `ui/components/GlossaryTermCard.kt` | RESHAPE | Useful card layout. Becomes the "see related" / glossary-side-door card under S1, not a primary list item. |
| `ui/components/PresetSelector.kt` | DELETE | Style picker is explicitly cut by `STRATEGY.md` § Features § "Cut for v1". |

### 1.5 Browse / Categories / Search top-level tabs — **RESHAPE (demote)**

| File | Verdict | Notes |
|---|---|---|
| `ui/browse/BrowseTermsScreen.kt` | RESHAPE | Per S1, the browse-list shape survives only as a glossary side-door from inside a unit. Demote off home; expose only via "see related" navigation. Keep the screen for now; remove it from primary nav. |
| `ui/categories/CategoriesScreen.kt` | RESHAPE | Same treatment as Browse. Useful as filtered side-door, not a top-level tab. |
| `ui/search/SearchScreen.kt` | RESHAPE | Same. Strategy explicitly excludes search-as-primary navigation (P3); search can return as in-unit glossary search later. |
| `viewmodel/BrowseTermsViewModel.kt` `CategoriesViewModel.kt` `SearchViewModel.kt` | RESHAPE | Survive only if their screens survive as side-doors. If S1 is implemented as a single combined "browse all terms" entry from inside a unit, two of these collapse into one. |

### 1.6 AI Tools / Ask Glossary / Chat / Trend Watcher — **DELETE**

| File | Verdict | Notes |
|---|---|---|
| `ui/ai/AiToolsScreen.kt` | DELETE | Hub for "AI Learning Layer" — functionally a tile grid pointing at scenarios / challenges / Ask Glossary. Each child is also cut. The hub screen has no future home. |
| `ui/chat/ChatScreen.kt` | DELETE | Free-text "Ask Glossary" front door is explicitly cut by `STRATEGY.md` § Features. May survive later as per-unit help inside F2, but that is a different UI built against the path context — not this screen. |
| `viewmodel/AskGlossaryViewModel.kt` | DELETE | Couples ChatScreen to the deleted `askGlossary` repository call. |
| `ui/trendwatcher/TrendWatcherScreen.kt` | DELETE | One-line `Text("…Placeholder")`. Long-since stub; not on any roadmap aligned to the new strategy. |

### 1.7 Term contribution / drafts — **DELETE**

| File | Verdict | Notes |
|---|---|---|
| `ui/draft/TermDraftScreen.kt` | DELETE | Contribution / "Create Term Draft" flows are explicitly cut in v1 (`STRATEGY.md` § Features). Authoring is founder-side via markdown + Git (Architecture Q2). |
| `viewmodel/TermDraftViewModel.kt` | DELETE | Same. |
| `model/TermDraft.kt` | DELETE | Same. |
| `data/remote/model/RemoteTermDraft.kt` | DELETE | Same. |

### 1.8 Bundle 0 — Tokenization concept preview — **RESHAPE → flagship unit**

These screens are the highest-quality interactive material in the app and are
exactly the seed `EXECUTION.md` Phase 2 calls for ("Recommended choice:
Tokenization. Bundle 0 has reusable raw material for the bite…").

| File | Verdict | Notes |
|---|---|---|
| `ui/preview/TokenizationProofScreen.kt` | RESHAPE | The static stacked-page rendering of the tokenization unit. Becomes the *depth-on-tap* mode of the Tokenization flagship unit (F2). Drop the standalone `preview_tokenization` route once it lives inside the unit reader. |
| `ui/preview/bite/BiteFeedScreen.kt` | RESHAPE | Vertical pager bite-feed shell. Useful as the *bite* mode of F2 (Loop step 2). Generalize from "Tokenization bites" to "any unit's bites". |
| `ui/preview/bite/TokenizationBites.kt` | RESHAPE | Concrete bite list for Tokenization. Becomes the seed payload for the Tokenization flagship unit when authoring re-expresses it in the markdown 9-slot anatomy. Eventually this hardcoded list is replaced by data loaded from the authored unit. |
| `ui/preview/bite/McqCheckpoint.kt` | RESHAPE → DELETE-leaning | Multiple-choice checkpoint widget. Strategy says: *"Multiple-choice and self-grade are explicitly rejected"* for the Decide step. **The MCQ checkpoint cannot survive as Loop step 3.** Two possible survival paths to consider before deleting: (i) an *in-bite comprehension nudge* preceding the decision prompt, or (ii) the **Return step (Loop step 6, spaced review)** where the question is retention rather than first-encounter learning — strategy's explicit rejection of MCQ is scoped to *Decide*, and MCQ is more defensible for retention checks. Both are founder calls (see §5 #1 and §5 #7). The Return-step path is the new option surfaced in this revision; the bite-nudge path was the original recommendation. Recommend DELETE only after the founder closes both options; otherwise hold pending decision. |
| `ui/preview/SimpleTokenizer.kt` | KEEP | Pure tokenizer logic backing the playground demo; reusable verbatim by the Tokenization unit. |
| `ui/preview/TokenizerPlayground.kt` | RESHAPE | Interactive widget; survives as one of the S2 flagship-only interactive widgets inside the Tokenization unit. |
| `ui/preview/BpeWalkthrough.kt` | RESHAPE | Same as above — flagship-only interactive widget. |
| `ui/preview/ProblemComparison.kt` | RESHAPE | Same. |
| `ui/preview/PitfallDemos.kt` (`JavaScriptSplitDemo`, `EmojiCostDemo`) | RESHAPE | Same — pitfall live demos slot directly into the trade-off framing slot of the unit anatomy. |

### 1.9 Auth + settings — **KEEP**

| File | Verdict | Notes |
|---|---|---|
| `ui/auth/AuthScreen.kt` | KEEP | Login/signup is part of F7. UI may polish later. |
| `viewmodel/AuthViewModel.kt` | KEEP | Same. |
| `ui/settings/SettingsScreen.kt` | KEEP | S3: keep as-is per strategy. May lose links to deleted screens (AI tools, trend watcher) — minor edit, not a reshape. |
| `data/auth/AuthApiService.kt` `TokenStorage.kt` | KEEP | EncryptedSharedPreferences-backed JWT storage. Inherits cleanly. |
| `data/repository/AuthRepository.kt` | KEEP | Inherits. |
| `model/AuthSession.kt` | KEEP | Inherits. |

### 1.10 Domain models — mixed

| File | Verdict | Notes |
|---|---|---|
| `model/GlossaryTerm.kt` | RESHAPE | Becomes one of two things in the new model: (a) `Unit` (as the path-centric first-class entity, with bite/depth/trade-off/sources/decision/rubric/calibration), and/or (b) `GlossaryEntry` for the S1 side-door. Most fields don't carry over. New strategy demands: trade-off framing, calibration tags, sources with dates, prereqs, decision-prompt id, rubric id. |
| `model/Category.kt` | KEEP, demote | Categories survive as a glossary-side-door classifier, not as a primary navigation taxonomy. The model itself is fine; its prominence drops. |
| `model/AiLearningModels.kt` (`AskGlossaryResponse`, `LearningScenario`, `LearningChallenge`, `GeneratedArtifactResult`) | DELETE | Each maps to a deleted feature: Ask Glossary as front door, scenario picker, challenge picker, generation cache wrapper. None of these survive in F1–F7. |
| `model/LearningPreset.kt` (`LearningPreset` enum + `CompletionConfidence` + `ArtifactKind` + `TaskState` + `CriterionGrade` + `LearningCompletion` + `LearningCompletionResult`) | RESHAPE → mostly DELETE | `LearningPreset` is the deleted style picker. `ArtifactKind = scenario|challenge` is deleted. `TaskState` (the in-app per-criterion checklist) is deleted under T2-A: rubric is grader-evaluated, not user-self-graded. What survives: a much-smaller `Completion` (user_id, unit_id, completed_at) and a new `Grade` shape (per-criterion Met/NotMet from grader, with confidence). Don't try to surgically edit this file — write a new `model/Unit.kt`, `model/Path.kt`, `model/Decision.kt`, `model/Grade.kt`, `model/Completion.kt`, etc., and delete this one. |
| `model/TermDraft.kt` | DELETE | Contribution flow is cut. |

### 1.11 Data layer (repository + remote) — **RESHAPE**

| File | Verdict | Notes |
|---|---|---|
| `data/repository/RepositoryProvider.kt` | KEEP | Provider object, simple lazy DI; survives. |
| `data/repository/GlossaryRepository.kt` (interface) | RESHAPE → replace | Currently a fat catch-all (terms, categories, search, askGlossary, generateScenario, generateChallenge, submitTermDraft, submitLearningCompletion). Strategy splits this cleanly into: `PathRepository`, `UnitRepository`, `DecisionRepository` (submit answer → grade), `CompletionRepository`, `ReviewScheduleRepository`. Plus a small `GlossaryRepository` retained for S1. Don't extend; replace. |
| `data/repository/ApiGlossaryRepository.kt` | RESHAPE → replace | Same — implementation against deleted endpoints. |
| `data/repository/MockGlossaryRepository.kt` | DELETE — **fresh mocks in Phase 1** | Currently unused (`repositoryMode = API`). The instinct to keep it as a template is reasonable but doesn't pay off: Phase 1 splits the fat `GlossaryRepository` into 4–5 small path-centric repositories (`PathRepository`, `UnitRepository`, `DecisionRepository`, `CompletionRepository`, `ReviewScheduleRepository`), and the existing mock implements *the deleted shape* on every method. Renaming and gutting it to fit a single new interface saves nothing over writing a 30-line in-memory mock per new repo from scratch — and the seed data inside (categories, terms) is not the right test fixture for a path-centric world either. **Phase 1 will author fresh mocks per new repository interface; the cost is acceptable and the result is cleaner.** This delete is contingent on Phase 1 actually authoring those mocks (called out in §7 below). |
| `data/remote/api/GlossaryApiService.kt` | RESHAPE → replace | Interface mirrors the deleted endpoints. New shape will have `getPath`, `getUnit`, `submitDecisionAnswer`, `streamGradeResult`, etc. |
| `data/remote/network/ApiConfig.kt` `GlossaryApiServiceFactory.kt` | RESHAPE | HTTP plumbing (URL, JSON, auth header). The plumbing is fine; the surface it exposes changes. |
| `data/remote/model/RemoteGlossaryTerm.kt` | RESHAPE | DTOs need to follow the new path-centric server shape. |
| `data/remote/model/RemoteCategory.kt` | KEEP, demote | DTO fine; usage demoted. |
| `data/remote/model/RemoteAiModels.kt` | DELETE | Mirrors the deleted AI Learning Layer endpoints. |
| `data/remote/model/RemoteTermDraft.kt` | DELETE | Contribution. |

### 1.12 ViewModels — mixed

| File | Verdict | Notes |
|---|---|---|
| `viewmodel/TermDetailsViewModel.kt` | DELETE | 407 lines of preset-picker + scenario/challenge generation + in-app self-grading. The shape is fundamentally wrong for F2/F3/F4 (bite + decision prompt + LLM grader). Write a new `UnitReaderViewModel` and `DecisionGradingViewModel` from scratch. |
| `viewmodel/AskGlossaryViewModel.kt` | DELETE | Backs the deleted ChatScreen. |
| `viewmodel/TermDraftViewModel.kt` | DELETE | Contribution. |
| `viewmodel/BrowseTermsViewModel.kt` `CategoriesViewModel.kt` `SearchViewModel.kt` | RESHAPE | Survive only if their screens survive (S1 side-door). Likely all collapse into one small viewmodel for the side-door in Phase 3. |
| `viewmodel/AuthViewModel.kt` | KEEP | F7. |

### 1.13 AndroidManifest — **KEEP**

| File | Verdict | Notes |
|---|---|---|
| `app/src/main/AndroidManifest.xml` | KEEP | Inspected. Declares `INTERNET` permission, an `application` block with theme/label, and exactly one `activity` (`.MainActivity`) with the `MAIN` / `LAUNCHER` intent filter. **Nothing in the manifest references the deleted screens** — the cut UIs (`AiToolsScreen`, `ChatScreen`, `TrendWatcherScreen`, `TermDraftScreen`) are Compose composables routed through `navigation/AppNav.kt`, not Android `Activity` declarations, so the manifest is unaffected by their removal. No intent filters, no exported components, no permissions tied to deleted features. No edits required during Phase 0. (If any future feature requires storage access, push notifications, or share intents, add at that time.) |

### 1.14 Resources — **KEEP-light, with a Phase 1 watch-item**

| File | Verdict | Notes |
|---|---|---|
| `res/values/strings.xml` | KEEP | Today contains exactly one entry: `<string name="app_name">FOSS 101</string>`. **All user-facing copy is currently hardcoded inline in Compose composables** (e.g. `Text("AI Learning Layer")` inside `HomeScreen.kt`, scenario/challenge UX strings inside `TermDetailsScreen.kt`, term-draft form labels inside `TermDraftScreen.kt`, contribution scoring strings inside the contribution-flow viewmodels). Because those composables themselves are DELETE / RESHAPE per §1.3 / §1.6 / §1.7 / §1.4, the strings vanish with them. No surgical sweep of `strings.xml` needed *now*. |
| **Watch-item for Phase 1** | — | When Phase 1 begins extracting hardcoded copy into `strings.xml` (a likely a11y / future-i18n step alongside the path-centric reshape), the categories that must **not** carry over from the old surfaces are: (a) AI Tools menu items (*"AI Learning Layer"*, *"Ask Glossary"*, *"Trend Watcher"*); (b) scenario / challenge UX copy (preset-picker labels, scenario card titles, challenge prompts); (c) term-draft form labels (*"Submit term"*, contributor-confirmation strings); (d) contribution scoring strings (*"You earned N points"*, contributor-summary copy). These are surfaces of cut features; their copy does not get a second life in v1. |
| **Localization** | — | No `res/values-*/` directories exist (no localized resource sets). No translations to retire, no localization implications from the cut. If localization is ever added, it begins fresh against the path-centric vocabulary, not the old soul's. |
| `app_name` itself | KEEP, **rename watch-item for Phase 4** | `app_name = "FOSS 101"` will need to flip to `"Libella"` (or whatever the trademark check clears under `STRATEGY.md` § Naming) at the Phase 4 launch-readiness gate. Not a Phase 0 concern. |

### 1.15 CI / CD pipelines — **ABSENT (state explicitly)**

Walked the repo for `.github/workflows/`, `.gitlab-ci.yml`, `.circleci/`,
`Jenkinsfile`, `azure-pipelines.yml`, and any `*.yml` / `*.yaml` at the root.
**None present.** There are no CI workflows to triage against the cut list.

Implications:

- No automated test runner today means `tests/test_presets.py` and
  `tests/test_scoring.py` (DELETE per §2.8) are not currently being
  exercised in CI; their deletion silently removes nothing scheduled.
- No build pipeline today means there is no Android build job that will fail
  the moment the deleted Compose screens stop resolving — local `gradlew`
  and developer attention are the only signal.
- The `scripts/claude/capture-gradle-cache.sh` / `restore-gradle-cache.sh`
  pair under §3 is *cache plumbing for Claude web sessions*, not CI per se.

This is a gap, not a verdict. **Phase 1 should add CI** — at minimum: a
GitHub Actions workflow that runs `pytest backend/tests/`, runs the
schema-linter on authored unit markdown (per `EXECUTION.md` Phase 1 work),
and runs `gradle assembleDebug` on the Android module. The grader regression
runner (`EXECUTION.md` Phase 2) will live in CI by Phase 2. Tracked further
in §7.

---

## 2. Backend (`backend/`)

### 2.1 Framework + plumbing — **KEEP**

| File | Verdict | Notes |
|---|---|---|
| `app/__init__.py` | KEEP | Empty. |
| `app/db.py` | KEEP | psycopg connection helper, no domain coupling. |
| `app/config.py` | RESHAPE-light | Generic env loader. The `AI_PROVIDER` defaults (OpenAI / `gpt-4.1-mini`) need replacing with Anthropic / Claude Sonnet 4.6 per Architecture Q3 + T2. Treat as a one-line value change, not a reshape of the file. |
| `app/migrations.py` | KEEP | Idempotent migrations runner with a `schema_migrations` table. Reuse for the path-model migrations in Phase 1. |
| `requirements.txt` | RESHAPE-light | Drop OpenAI-flavored deps; add `anthropic`. Otherwise fine. |
| `__init__.py` (root) | KEEP — trivial. |
| `README.md` | RESHAPE | Repoint at the new product framing. |

### 2.2 Auth — **KEEP**

| File | Verdict | Notes |
|---|---|---|
| `app/auth.py` | KEEP | bcrypt + JWT + email/password validators. F7. Inherits cleanly. |
| Migration `009_users.sql` | KEEP | `users` table, exactly what F7 needs. |

### 2.3 Repository (data access) — **RESHAPE → mostly replace**

| File | Verdict | Notes |
|---|---|---|
| `app/repository.py` (~1098 lines) | RESHAPE → mostly replace | Catch-all module: `list_terms`, `get_term_by_id`, `list_categories`, `search_terms`, `create_term_draft`, `publish_term_draft`, `update_draft_status`, `get_contributor_summary`, `get_top_missing_queries`, `record_learning_completion`, `upsert_generated_content`, `build_term_context`, `create_user`, etc. Per-feature triage: term/category/search reads — RESHAPE-light, keep for S1 side-door (with smaller surface). User/auth helpers — KEEP. Term-draft + contribution-event + contributor-score helpers — DELETE. AI-generated-content cache helpers — DELETE (different cache strategy under T2: prompt cache on rubric, not row cache on artifacts). `record_learning_completion` — RESHAPE (becomes path-centric Completion + Grade). Search-event logging — DELETE-leaning unless we want missing-query telemetry to survive S1; recommend keeping a thin slice if it's cheap. |

### 2.4 API surface (`app/main.py`) — **RESHAPE per endpoint**

| Endpoint | Verdict | Notes |
|---|---|---|
| `GET /health` | KEEP | Standard. |
| `GET /api/v1/terms` | RESHAPE → demote | Keep as a side-door read for S1; not a primary endpoint. |
| `GET /api/v1/terms/{term_id}` | RESHAPE → demote | Same. |
| `GET /api/v1/categories` | RESHAPE → demote | Same. |
| `GET /api/v1/categories/{category_id}/terms` | RESHAPE → demote | Same. |
| `GET /api/v1/search/terms` | RESHAPE → demote | Same. |
| `GET /api/v1/search/missing-queries` | DELETE-leaning | Telemetry for the contribution loop. If we keep search as a side-door, log misses cheaply; otherwise delete. Founder call. |
| `POST /api/v1/term-drafts` | DELETE | Contribution flow. |
| `POST /api/v1/term-drafts/{id}/status` | DELETE | Contribution flow. |
| `POST /api/v1/term-drafts/{id}/publish` | DELETE | Contribution flow. |
| `GET /api/v1/contributors/{id}/summary` | DELETE | Contribution gamification. |
| `POST /api/v1/ai/ask-glossary` | DELETE | Ask-Glossary front door is cut. |
| `POST /api/v1/ai/terms/{term_id}/scenario` | DELETE | Scenario generator is cut. |
| `POST /api/v1/ai/terms/{term_id}/challenge` | DELETE | Challenge generator is cut. |
| `POST /api/v1/auth/signup` | KEEP | F7. |
| `POST /api/v1/auth/login` | KEEP | F7. |
| `GET /api/v1/auth/me` | KEEP | F7. |
| `POST /api/v1/learning-completions` | RESHAPE | Becomes the Completion endpoint for path progress. The shape changes (no `artifactType` of scenario/challenge; no in-app `criteriaGrades`; instead bound to a `unitId` and a grader-produced `Grade`). Easier to rebuild than to migrate. |
| **NEW required** | — | `GET /api/v1/paths/{id}` (path with sequenced unit list), `GET /api/v1/units/{id}` (full 9-slot unit), `POST /api/v1/units/{id}/decisions/{decision_id}/grade` (LLM grading with streaming response, T2-D guardrails), `POST /api/v1/completions` (path-centric replacement), `GET /api/v1/review-schedule` (F5). |

### 2.5 AI service — **RESHAPE → rewrite**

| File | Verdict | Notes |
|---|---|---|
| `app/ai_service.py` | RESHAPE → rewrite | Currently an OpenAI-style `chat/completions` client with two features (`ask_glossary`, `generate_artifact` for scenario/challenge), and a `LearningPreset` registry. None of those features survive. The new service is the **F4 grader**: Anthropic Claude Sonnet 4.6 with prompt caching on the rubric, structured tool-call output, all four T2-D guardrails, and streaming the rationale (TT2). Keep the file *only* as scaffolding for the rewrite — its current shape is wrong end-to-end. |

### 2.6 Database migrations — **status by file**

The migrations form a sequence that built the *old* term/draft/contribution
soul on top of `users`. Most ride along under the new model, but several
encode features that are explicitly cut. Strategy here:

- **Existing migrations are immutable history** (they have already been
  applied by the deployed Railway DB and recorded in `schema_migrations`).
  We don't edit them — we add new migrations that reshape on top.
- **Tables to keep around for S1** (terms, categories, term_relations) survive
  but get demoted; their existing migrations stay.
- **Tables to retire** (term_drafts, contribution_events, contributor_scores,
  term_search_events, ai_generated_content) get retired by future migrations
  (drop or rename), not by editing the migrations that created them.

**Data preservation before drops — founder call required.** The retire-by-drop
approach above destroys whatever rows currently live in `term_drafts`,
`contribution_events`, `contributor_scores`, `ai_generated_content`, and
`term_search_events`. Two acceptable paths; founder must pick one before any
drop migration lands:

- **(a) No preservation needed.** Confirm in writing that the deployed Railway
  DB contains only synthetic / development data — i.e. no real contributor
  records, no real search telemetry worth retaining for analysis. If true, drop
  freely. *Working assumption: this is the case, since the contribution flow
  was never publicly launched. Founder confirmation required.*
- **(b) Snapshot, then drop.** Before the retirement migration ships, export
  each retiring table to a SQL dump committed under `db/archive/` (e.g.
  `db/archive/2026-05_term_drafts.sql`). Dump format: `pg_dump
  --data-only --table=<name>`. Future analysis can re-load from the dump if
  needed, and the drop is reversible in the historical sense. Adds ~30 minutes
  of work to the retirement migration; no recurring cost.

Either way, this is a founder call (#8 in §5), not an audit-level decision.
The audit assumes (a) until told otherwise.

| Migration | Subject | Verdict |
|---|---|---|
| `001_initial_schema.sql` | terms, categories | KEEP, demote (S1 backing data) |
| `002_add_term_relations.sql` | term_relations | KEEP, demote (S1 "see related") |
| `003_ai_learning_layer.sql` | ai_generated_content cache table | RESHAPE → retire in a new migration. Cache strategy under T2 is provider-side prompt caching on the rubric, not row-cached artifact storage. |
| `004_term_schema_enforcement.sql` | tightening checks on terms/drafts | KEEP for the surviving terms table; the term_drafts portions retire when drafts retire. Don't edit the migration; layer over it. |
| `005_search_logging_and_term_drafts.sql` | term_search_events + term_drafts | RESHAPE → retire term_drafts. Decide on term_search_events: retire unless we keep a cheap miss-log under S1. |
| `006_contribution_tracking.sql` | contribution_events, contributor_scores | RESHAPE → retire. Contribution flow is cut. |
| `007_contribution_pipeline_hardening.sql` | indexes/constraints on the contribution pipeline | RESHAPE → retire alongside its tables. |
| `008_replace_term_draft_trigger.sql` | trigger on term_drafts | RESHAPE → retire alongside term_drafts. |
| `009_users.sql` | users table | KEEP — F7 spine. |
| `010_learning_completions.sql` | learning_completions tied to scenario/challenge | RESHAPE → the *table* survives in spirit (path completion) but the columns drift (drop `artifact_type`, `confidence`, `reflection_notes` if not retained; add `unit_id`, `path_id`, `grade_id` FK). Replace via a new migration that creates a new `completions` table and (eventually) drops `learning_completions`, rather than editing 010 itself. |
| `011_completion_event_types.sql` | extends contribution_events.event_type to include scenario/challenge completions, links events to completions | DELETE-equivalent — this layered the cut feature on top of contribution events; will retire alongside contribution_events. |
| `012_completion_payload.sql` | task_states, challenge_response, criteria_grades, earned_points columns on learning_completions | DELETE-equivalent — every column added here is the in-app self-grading shape rejected by T2. Retire when the new completions table replaces this one. |
| `db/schema.sql` | static snapshot | RESHAPE → regenerate or delete. The header already says "Legacy schema snapshot — active schema changes must be added via migrations". Once the new migrations land, regenerate this from the live schema or drop it; don't hand-edit. |
| `db/seed.sql` | category + sample term seeds | RESHAPE → demote. Useful raw material for S1 glossary entries; not the spine. |

### 2.7 New tables required (per `STRATEGY.md` Architecture)

The Phase 1 migration will add (one migration per concern, smallest viable
forward step each time):

- `paths` (id, slug, title, description)
- `units` (id, path_id, slug, position, title, definition, trade_off_framing,
  bite_md, depth_md, prereq_ids, status)
- `unit_sources` (unit_id, url, title, date, primary_source bool)
- `calibration_tags` (unit_id, claim, tier ENUM('settled','contested','unsettled'))
- `decision_prompts` (id, unit_id, prompt_md)
- `rubrics` (id, decision_prompt_id, version, json) and `rubric_criteria`
- `completions` (id, user_id, path_id, unit_id, completed_at)
- `grades` (id, completion_id, criterion_id, met BOOL, confidence FLOAT,
  rationale, flagged BOOL)
- `review_schedule` (user_id, unit_id, due_at, interval, last_reviewed_at)
- `regression_pairs` (id, unit_id, answer_md, expected_per_criterion JSON,
  human_grader, source) — F4 / T2 discipline.

### 2.8 Backend scripts + tests

| File | Verdict | Notes |
|---|---|---|
| `scripts/migrate_db.py` | KEEP | Generic migrate runner; reuse. |
| `scripts/seed_db.py` | RESHAPE | Becomes the path/unit-seed runner once authoring pipeline lands. The current term-seed payload is reusable raw material under S1. |
| `scripts/audit_term_schema.py` | DELETE-leaning | Audit script for term schema, written for the old soul. If retained, it must be re-pointed at the unit schema validator (which is new work — see schema linter under Phase 1). Recommend delete; rebuild as `scripts/lint_unit_markdown.py` from scratch. |
| `scripts/verify_postgres_api.py` | RESHAPE | API smoke-checker; will need new endpoints. Easier to rewrite than edit. |
| `tests/test_auth.py` | KEEP | F7. |
| `tests/test_presets.py` | DELETE | Tests the deleted style-picker logic. |
| `tests/test_scoring.py` | DELETE | Tests the deleted scenario/challenge in-app scoring logic. |
| `tests/__init__.py` | KEEP — trivial. |

---

## 3. Repo-level + docs

| Path | Verdict | Notes |
|---|---|---|
| `build.gradle.kts` `settings.gradle.kts` `app/build.gradle.kts` `gradle.properties` `gradle/wrapper/*` `gradlew` `gradlew.bat` | KEEP | Build system. Inherits. |
| `.gitignore` | KEEP, polish | Tracks `docs/test.kt` and the gradle-cache tarball; fine. |
| `.gitkeep` (root and `docs/roadmap`, `docs/workflow`) | KEEP | Harmless. |
| `scripts/claude/capture-gradle-cache.sh` `restore-gradle-cache.sh` | KEEP | Web-session gradle-cache plumbing; inherits. |
| `docs/STRATEGY.md` | KEEP | Canonical. |
| `docs/EXECUTION.md` | KEEP | Canonical. |
| `docs/AUDIT.md` (this file) | KEEP | This file. |
| `ROADMAP.md` (root) | RESHAPE → archive | Already self-marked as superseded; either move under `docs/roadmap/` and rename to `ARCHIVE_ROADMAP.md` or delete the duplicate `docs/roadmap/ROADMAP.md`. Recommend: keep one copy under `docs/roadmap/` with an *Archived* header; delete the root duplicate. |
| `docs/roadmap/ROADMAP.md` | RESHAPE → archive (single canonical archived copy of the old roadmap) | Same body as root ROADMAP.md, no top-of-file supersession note. Pick one location, mark archived, drop the other. |
| `docs/architecture/BACKEND_DATABASE_SCOPE.md` | DELETE | Begins with "No user accounts. No chat / AI tools. Online-first. Content managed manually." Most decisions are obsolete or contradicted by the new strategy (we *do* have user accounts; we *do* have an LLM grader). The misalignment is worse than no doc at all. |
| `docs/test.kt` | DELETE | 1-line file, ignored by `.gitignore`. Not needed. |
| `docs/workflow/AGENTS.md` `CLAUDE_CAPABILITIES.md` `CODEX_EXECUTION_ROADMAP.md` `CODEX_HOME_THEME_ALIGNMENT_BUNDLE.md` `GEMINI_AGENT_ROADMAP.md` `TASKS.md` | DELETE | Old workflow docs anchored to the old soul. Each one was a guide for executing against features that are now cut (term-draft contribution pipelines, AI Learning Layer style picker, ask-glossary as a primary surface). Under P5 (quality ceiling, not content scale), keeping confusing dead docs around is a tax that compounds — every future agent has to figure out which doc is current and risks executing against a dead spec. **Recommended: delete.** Git preserves the history if anyone ever needs to reread them. *If founder prefers archival, the consistent fallback is to archive these alongside `prompts/` under `docs/workflow/_archive/` — see §5 #4.* |
| `docs/workflow/prompts/*` (25 prompt bundles named e.g. `AI_LEARNING_LAYER.md`, `TERM_DRAFT_*`, `CATEGORIES_SEARCH_MVP_BUNDLE.md`, etc.) | DELETE | Each prompt bundle drove a feature that is now cut (term drafts, contribution pipeline, AI Learning Layer with style picker, category/search MVP as front door). Same P5 logic as the workflow docs above: dead specs invite re-execution against a dead world. **Recommended: delete.** Git preserves them. *Same fallback if founder prefers archival — both directories archived together under `_archive/`, never one and not the other (§5 #4).* |

---

## 4. Cut-list summary (one-line each)

### Delete

- Android: `ui/ai/AiToolsScreen.kt`, `ui/chat/ChatScreen.kt`,
  `ui/trendwatcher/TrendWatcherScreen.kt`, `ui/draft/TermDraftScreen.kt`,
  `viewmodel/AskGlossaryViewModel.kt`, `viewmodel/TermDraftViewModel.kt`,
  `viewmodel/TermDetailsViewModel.kt`,
  `ui/components/PresetSelector.kt`, `model/TermDraft.kt`,
  `model/AiLearningModels.kt`, `model/LearningPreset.kt` (replace with
  new path-centric models), `data/repository/MockGlossaryRepository.kt`
  (Phase 1 authors fresh mocks per new interface — see §1.11 + §7),
  `data/remote/model/RemoteAiModels.kt`,
  `data/remote/model/RemoteTermDraft.kt`. Recommend delete:
  `ui/preview/bite/McqCheckpoint.kt` (founder call).
- Backend: `app/ai_service.py` (rewrite from scratch — preserve filename if
  desired, replace contents); `tests/test_presets.py`, `tests/test_scoring.py`,
  `scripts/audit_term_schema.py`. Endpoints in `main.py` for term-drafts,
  contributors, ask-glossary, scenario, challenge.
- Repo: `docs/test.kt`, `docs/architecture/BACKEND_DATABASE_SCOPE.md`,
  `docs/workflow/*.md` (the workflow docs) **and**
  `docs/workflow/prompts/*` (the prompt bundles) — applied as a single
  consistent policy per §5 #4 (delete both, or archive both; never split).

### Reshape

- Android: `navigation/AppNav.kt`, `ui/home/HomeScreen.kt`,
  `ui/details/TermDetailsScreen.kt` (gut), `ui/browse/`, `ui/categories/`,
  `ui/search/` (demote off home into S1 side-door),
  `model/GlossaryTerm.kt` (split into `Unit` + `GlossaryEntry`),
  `data/repository/GlossaryRepository.kt` and its API/Mock impls (replace
  interface with path-centric repositories), `data/remote/api/GlossaryApiService.kt`
  (replace), `data/remote/model/RemoteGlossaryTerm.kt`. All Bundle 0
  preview screens fold into the Tokenization flagship unit.
- Backend: `app/repository.py` (split, retire half), `app/main.py` (replace
  most endpoints), `app/config.py` (provider switch to Anthropic),
  `requirements.txt` (Anthropic SDK), `db/schema.sql` (regenerate),
  `db/seed.sql` (demote to S1 seed material), `scripts/seed_db.py`,
  `scripts/verify_postgres_api.py`. Migrations 003, 005, 006, 007, 008, 010,
  011, 012 retired by *new* forward migrations, not by editing.
- Repo: `ROADMAP.md` (root) → fold into single archived copy under
  `docs/roadmap/`. (The workflow docs and prompt bundles are now in the
  Delete bucket above per §5 #4 — only the archival fallback would move
  them to `docs/workflow/_archive/`.)

### Keep

- Android: theme + generic components (`AppScaffold`, buttons,
  `SectionHeader`, `TagChip`, `NavigationTile`, `ScreenState`),
  `MainActivity.kt`, auth (screen + viewmodel + storage + repository + DTOs),
  settings, `ui/preview/SimpleTokenizer.kt`,
  `model/Category.kt` (demoted), `model/AuthSession.kt`,
  `data/repository/RepositoryProvider.kt`, `data/remote/network/ApiConfig.kt`
  + `GlossaryApiServiceFactory.kt` (plumbing).
- Backend: `app/auth.py`, `app/db.py`, `app/migrations.py`, `tests/test_auth.py`,
  migrations 001, 002, 004, 009 (with later layers reshaping atop them).
- Repo: build files, `scripts/claude/*.sh`, `STRATEGY.md`, `EXECUTION.md`,
  this `AUDIT.md`.

---

## 5. Open founder calls before any deletion

Each of these is a small judgment call I'd like signed off explicitly because
the wrong call wastes work later:

1. **MCQ checkpoint** (`ui/preview/bite/McqCheckpoint.kt`). Strategy rejects
   MCQ as the *Decide* step. Does the MCQ survive as a *pre-Decide
   comprehension nudge* inside a bite, or does it go entirely? Recommend: go.
2. **Glossary side-door scope** (S1). Three current screens (Browse,
   Categories, Search) all become side-door candidates. Recommend collapsing
   to one — a single in-unit "browse glossary" entry — rather than three.
   Confirms dead-code on two screens.
3. **`term_search_events`** (missing-query telemetry). Useful diagnostic; not
   load-bearing. Keep cheap, or drop entirely?
4. **Workflow archive vs. delete**. `docs/workflow/*.md` and
   `docs/workflow/prompts/*`. The "git preserves history" rationale applies
   equally to both, so the audit must pick *one* policy and apply it
   consistently. **Recommendation: delete both**, citing P5 — keeping
   confusing dead docs is a tax on every future agent that opens the repo
   and tries to figure out which spec is current. If founder prefers
   archival, archive *both* under `docs/workflow/_archive/` (not just one).
   What is not on the table: deleting one and archiving the other.
5. **Migration retirement strategy**. Confirm we will retire dead tables via
   *new* forward migrations (drop / rename) rather than editing the
   already-applied historical ones. (This is standard Postgres practice; just
   want it on the record.)
6. **Provider switch ordering**. The `AI_PROVIDER`/`AI_MODEL` env defaults
   currently point at OpenAI. The strategy locks Anthropic Claude Sonnet 4.6
   "pending final confirmation". Should the env defaults flip in Phase 0
   (cheap) or wait for Phase 2 (`#2` resolves there per `EXECUTION.md`)?
   Recommend: flip in Phase 0 — costs nothing, removes confusion. **This is
   a configuration-only change, not a Phase 2 pre-emption.** The Phase 0 flip
   updates `app/config.py` defaults and `requirements.txt` so the codebase
   stops carrying a contradiction with `STRATEGY.md` Q3. The provider
   *final-lock* per `EXECUTION.md` Phase 2 still happens after the grader is
   integrated and run against the regression set — that is the empirical step
   that confirms the choice. Phase 0's flip is paperwork; Phase 2's lock is
   evidence. No one should read this Phase 0 change as locking the provider.
7. **MCQ as Return-step (spaced review), not Decide-step.** Strategy
   *explicitly* rejects MCQ for Decide (Loop step 3) — that decision is
   locked. Strategy is *silent* on the format of Return (Loop step 6).
   Return is checking *retention* of an already-encountered concept, not
   first-encounter learning, and MCQ is much more defensible there: cheap to
   render, no LLM cost, fast feedback, well-suited to "do you still know
   this?" rather than "can you reason about trade-offs?". Question for the
   founder: *Could `McqCheckpoint.kt` survive as the Return-step format
   (perhaps alongside or as an alternative to re-running the bite)?* If yes,
   §1.8's verdict on `McqCheckpoint.kt` becomes RESHAPE (move to Return);
   if no, it stays DELETE. The audit does not pre-empt this — surfacing the
   option only.
8. **Data preservation before retirement migrations.** See §2.6. Either
   confirm in writing that the deployed Railway DB carries only synthetic /
   development data in the retiring tables (`term_drafts`,
   `contribution_events`, `contributor_scores`, `ai_generated_content`,
   `term_search_events`) — in which case drop without snapshot — or specify
   the snapshot procedure (`pg_dump --data-only --table=…` committed under
   `db/archive/`) before the drop migration lands. The audit assumes
   synthetic-only until told otherwise.

---

## 6. What this audit explicitly does *not* do

- **No file deletions.** This is the cut list, not the cut. Per
  `EXECUTION.md` Phase 0 decision gate, deletion waits on founder sign-off.
- **No new code.** The new path-centric models, migrations, endpoints, and
  reshape implementations are Phase 1 work.
- **No re-litigation of locked decisions.** Strategy is locked; this audit
  only translates it into file-level verdicts. If a verdict here looks
  strategically wrong, the path is to surface a strategy revision, not to
  edit the verdict in isolation.

---

## 7. Phase 1 test scaffolding that this audit anticipates

This section is forward-looking, not Phase 0 work. It captures the test
scaffolding Phase 1 should ship alongside the path-centric reshape, so the
foundation phase doesn't end with reshaped code and no safety net under it.
None of the items below need to land before founder sign-off on the cut
list, but they should be planned for so Phase 1 isn't surprised by them
mid-flight.

### Repository contract tests

Phase 1 splits the fat `GlossaryRepository` into path-centric repositories
(§1.11). Each new repository interface needs a small contract test that runs
against both an in-memory mock and the real API-backed implementation:

- `PathRepository` — getPath, list-paths-for-user, "next unit on path".
- `UnitRepository` — getUnit (full 9-slot payload), list-units-on-path,
  prereq resolution.
- `DecisionRepository` — submitAnswer, observe streamed grade, fetch
  per-criterion confidence and flagged state.
- `CompletionRepository` — record completion, list completions, idempotency
  on re-submit.
- `ReviewScheduleRepository` — list-due-today, advance-on-correct,
  reschedule-on-incorrect (interactions with whichever spaced-review
  algorithm is selected in Phase 3, but the contract is testable now).

Each contract test enforces the same behavior against both implementations
(mock + real) so the mocks authored fresh in Phase 1 (per §1.11) stay
faithful to the real API surface as it evolves. *This is the test work that
makes the §1.11 "fresh mocks per new repo interface" verdict cheap.*

### Schema linter tests for unit markdown

`EXECUTION.md` Phase 1 calls for a schema linter that checks every authored
unit has all 9 anatomy slots (title, definition, trade-off framing, bite,
depth, calibration tags, sources, decision prompt + rubric, prereqs) and
that calibration tags / sources / rubrics are well-formed. The linter
itself needs tests:

- **Positive fixtures** — at least one valid stub unit that lints clean.
- **Negative fixtures** — one fixture per failure mode: missing slot,
  malformed calibration tag tier, source without URL or date, rubric with
  no criteria, prereq pointing at a non-existent unit. Each should fail the
  linter with a specific, named error.
- **CI integration** — the linter runs on every commit that touches
  `content/units/` (or wherever the markdown lives). A unit that stops
  linting must fail CI.

### Migration tests for the new path tables

Phase 1 introduces the new tables listed in §2.7 (`paths`, `units`,
`unit_sources`, `calibration_tags`, `decision_prompts`, `rubrics`,
`rubric_criteria`, `completions`, `grades`, `review_schedule`,
`regression_pairs`). Migration tests should verify:

- **Forward migration** runs idempotently against an empty DB and against a
  DB at the previous schema version.
- **Constraints** — the FK / NOT NULL / CHECK constraints match the audit
  shape (e.g. `calibration_tags.tier` only accepts
  `'settled' | 'contested' | 'unsettled'`).
- **Rollback semantics** — for each retirement migration that drops a table
  retired per §2.6, the test confirms the drop is irreversible *unless* the
  pg_dump archive (founder call #8) has been written. This is more of a CI
  guard than a unit test: the retirement migration should refuse to run if
  the archive path is empty and option (a) of §2.6 hasn't been confirmed.
- **Data preservation path** (only if §5 #8 lands as option b) — a test that
  loads the archived dump back into a clean DB and verifies row counts and
  representative columns survive the round trip.

### Grader regression runner self-test

`EXECUTION.md` Phase 2 introduces the regression runner (≥20 ground-truth
pairs per flagship unit, run against the grader, agreement rate reported).
The runner *itself* is non-trivial code and warrants its own self-test in
Phase 1, before Unit 1 authoring stresses it:

- A **synthetic regression pair fixture** — a tiny rubric, two known
  answers (one passing, one failing), expected per-criterion grades.
- A **mock grader** that returns predetermined grades. The runner test
  verifies the agreement-rate calculation, the drift-flagging logic, and
  the "flagged" pathway (T2-B *Flagged-or-graded*) are correct **before**
  real LLM calls go through it.
- A **CI smoke** that runs the self-test on every commit; this protects the
  runner from silent regressions during Phase 2 authoring.

Building this in Phase 1 (against a mock grader) means Phase 2 starts with
a working scoreboard rather than building one mid-evaluation.

### CI bootstrap

§1.15 noted that no CI exists today. Phase 1 should bring up the minimum
viable pipeline that makes the above tests load-bearing:

- `pytest backend/tests/` (auth tests inherit; new contract tests, schema
  linter tests, migration tests, regression-runner self-test added).
- `gradle assembleDebug` on the Android module, plus whatever JUnit /
  instrumented-test scope Phase 1 introduces against the new repository
  interfaces.
- A `lint-units` job that runs the schema linter on authored unit markdown.

### Scope discipline

This section is forward-looking. It is **not** a Phase 0 deliverable, and
nothing here changes any verdict in §1–§3. Its purpose is to make sure
Phase 1 inherits a complete picture of the test scaffolding the audit
*assumes will exist* when it called certain deletes "cheap" (e.g.
MockGlossaryRepository in §1.11) and certain reshapes "safe" (e.g. the
repository split in §1.11 and the new endpoints in §2.4).
