# Libella — Execution Plan

**Status:** Phase plan, locked. (recorded 2026-05-04)
**Anchored to:** [`docs/STRATEGY.md`](./STRATEGY.md) — read first if you are new to the project.
**Companion doc:** [`ROADMAP.md`](../ROADMAP.md) — bottom-up tactical breakdown for the *old* glossary product. Partially superseded.

This document translates strategy into sequenced phases. It does **not** commit to dates. Each phase has explicit entry criteria, work clusters, exit criteria, and a decision gate. Phases run as long as they need to. Time estimates, when given, are orientation only.

---

## How to use this document

If you are new — or returning in a fresh chat session after a context loss:

1. Read `STRATEGY.md` first. It defines what we are building and why.
2. Then read this document. It defines what we work on, in what order.
3. The *current phase* is whichever has met its entry criteria but not its exit criteria. Identify it by checking `git log` and the repo state against the criteria below.
4. Open sub-decisions from `STRATEGY.md` (the section *"Open sub-decisions / what remains"*) get resolved as their owning phase runs. This doc tracks which phase resolves each.
5. Decision gates are the natural re-evaluation points. If something at a gate looks wrong, surface it; do not push through.
6. Do not relitigate locked decisions. If a locked decision seems wrong in light of new evidence, raise it explicitly as a *strategy revision* request before proceeding.

---

## Phase summary

| Phase | Name | Goal | Estimated effort |
|---|---|---|---|
| 0 | Cleanup audit | Know what to keep, reshape, delete from existing repo | ~1 week |
| 1 | Foundation | Build the spine of the loop without grader or spaced review | ~4–6 weeks |
| 2 | Unit-1 pilot | Prove the grader works on real content (**critical gate**) | ~3–4 weeks |
| 3 | Full v1 | Ship 20 units, remaining features | ~3–5 months (authoring is the constraint) |
| 4 | Launch readiness | Beta cohort, web preview, trademark cleared | ~3–4 weeks |

Total elapsed: realistically 5–8 months. The critical path is authoring 20 units (T1 in `STRATEGY.md`). Engineering will finish before authoring does.

---

## Phase 0 — Cleanup audit

**Goal.** Walk the existing FOSS-101 codebase and produce a written cut list aligned to the locked strategy. Avoid building on top of dead code.

**Entry criteria.**
- `STRATEGY.md` locked. *(Done.)*

**Work.**
- Audit the Android app. Classify each screen / composable / viewmodel as **keep / reshape / delete**. Examples expected: Home → reshape (path-soul); Browse / Categories / Search top-level tabs → demote per S1; AI Learning Modules style picker → delete; Tokenization Bundle 0 → reshape into a flagship unit.
- Audit the backend. Which FastAPI endpoints map to the new path-centric model; which are obsolete (term-centric routes); which new ones are required for `Path`, `Unit`, `Rubric`, etc.
- Audit the database. Tables that survive (users, auth, completions partly) vs. tables that need reshape vs. new tables required.
- Audit `ROADMAP.md`. Mark phases obsolete / reusable / re-point. Stale phases should be explicitly noted, not silently kept.

**Exit criteria.**
- A written audit at `docs/AUDIT.md` (or appended here as Phase 0 output) that classifies every meaningful module/file as **keep / reshape / delete**.
- Cut list approved by founder.

**Decision gate.**
- Founder explicit sign-off on the cut list before any deletion.

**Resolves (from `STRATEGY.md` open sub-decisions):** #4 (repo cleanup pass).

---

## Phase 1 — Foundation

**Goal.** Build the bare loop spine — read a unit, see progress, persist state. No grader yet; no spaced review.

**Entry criteria.**
- Phase 0 cut list approved.

**Work.**

*Engineering:*
- Reshape data model. Introduce `Path`, `Unit`, `DecisionPrompt`, `Rubric`, `Source`, `CalibrationTag`, `Completion`, `ReviewSchedule` as first-class Postgres entities, with migrations.
- **F1 Path home / "Continue".** Replace the current retrieval-shaped Home with a path-shaped one.
- **F2 Unit reader.** Bite + depth on tap; markdown-rendered.
- **F7 Account + progress tracking.** Mostly inherited from existing JWT auth, extended for path progress.
- **Authoring pipeline scaffolding.** Markdown templates for the 9-slot unit anatomy. Schema linter (CI check that all slots present and well-formed; calibration tags valid; sources have URLs and dates; rubric well-formed).

*Authoring:*
- Author one *stub* unit with all 9 anatomy slots filled. Used as the canary for engineering. No rubric tested yet, no regression set yet.

**Exit criteria.**
- A user can open the app, see *Continue: Stub Unit*, read the stub unit end-to-end (bite + depth), and have completion persisted across sessions.
- Schema linter passes on the stub unit.
- Founder can author a *second* stub unit using only the markdown templates and have it appear in the app — proves the pipeline works without engineer intervention.

**Decision gate.**
- Stub unit reads correctly on a real device.
- Data model is stable enough that bulk authoring can begin without further reshape.

**Resolves:** Begins #5 (authoring kickoff).

---

## Phase 2 — Unit-1 pilot (critical gate)

**Goal.** Prove the grader works. This is the make-or-break phase for the wedge.

**Entry criteria.**
- Phase 1 foundation built and verified.
- One stub unit confirmed reading correctly.

**Work.**

*Authoring:*
- Author Unit 1 end-to-end. **Recommended choice: Tokenization.** Bundle 0 has reusable raw material for the bite, and tokenization is a natural starting point in *"LLM Systems for PMs."*
- Write the per-criterion rubric for the decision prompt.
- Generate **≥20 ground-truth answer-grade pairs** (the regression set). This is the discipline that proves the grader.

*Engineering:*
- **F3 Decision prompt UI.** Open-ended text input, contextualized.
- **F4 LLM grader service.** Integrate Anthropic Claude Sonnet 4.6 (per `STRATEGY.md` Architecture Q3 + T2 E1). Apply prompt caching on the rubric. Structured tool-call output. All four hallucination guardrails:
  - Strict JSON / tool-call schema
  - Source-grounding (grader sees unit content + sources)
  - Answer-quote requirement (grader must quote user text it grades on)
  - Structured tool-call output
- **Streaming UX (TT2 from `STRATEGY.md`).** Stream the rationale as it generates; useful loading state, not a bare spinner.
- **Regression-set runner.** Feed the ≥20 pairs through the grader; report agreement rate and drift; iterate prompts/rubric until acceptable.

**Exit criteria.**
- Regression set passes at an acceptable rate. *Recommended threshold to agree on at the gate: ≥80% agreement with human-assigned grades on first run, with the grader's own confidence correlated to its actual correctness.*
- Per-criterion confidence behavior is honest: when the grader is uncertain, the answer is *flagged* (B3 in T2) rather than guessed at.
- LLM cost per grading call is measured and within tolerance.

**Decision gate. STOP AND EVALUATE.**
- Is the grader trustworthy enough to ship under P2?
- Is the per-call cost sustainable at projected scale?
- Does the user-facing experience honor the locked Loop arc (Decide → Calibrate)?

**If the gate fails**, options:
- Rethink rubric structure (per-criterion granularity, criterion phrasing).
- Escalate to Opus 4.7 for harder rubrics, keep Sonnet 4.6 for the rest.
- Re-evaluate provider (per Architecture Q3 — pivot to OpenAI or Google).
- Revise the wedge as a strategy-level decision before continuing.

**Do not proceed to Phase 3 with an unverified grader.** This is the load-bearing phase for the entire product.

**Resolves:** #2 (LLM provider final confirmation — we will know from running with it).

---

## Phase 3 — Full v1

**Goal.** Author the remaining 19 units; ship the rest of the loop and sidewall.

**Entry criteria.**
- Phase 2 gate passed.
- LLM provider confirmed.

**Work.**

*Authoring (the pacing constraint):*
- Author the remaining 19 units against the locked 9-slot anatomy.
- Tiered regression sets per unit: ≥20 for flagship units, ≥10 for standard units, **never zero**.
- LLM-assisted drafting per Q2 of T1 — every claim human-verified, sourced, calibration-tagged before publish.

*Engineering (parallel):*
- **F5 Spaced review scheduler.** Algorithm choice (SM-2, FSRS, or simpler interval doubling) to be made early in this phase; whichever is chosen, it must integrate cleanly with `Completion` and `ReviewSchedule`.
- **F6 Path overview / "you are here".** Visual representation of the path with locked / unlocked / completed states.
- **S1 Glossary side-door.** Demote the top-level Browse / Categories / Search tabs. Expose glossary entries from inside a unit ("see related") and via search inside a unit context.
- **S2 Interactive widgets** — for 5–10 flagship units only (Bundle 0-style Try-It / Pitfall / Sources cards). Reuse existing Bundle 0 implementation where possible.
- **S3 Settings polish.**

**Exit criteria.**
- All 20 units published, each with the required regression-set tier met.
- All Spine features (F1–F7) shipping.
- Sidewall features (S1, S2 flagship-only, S3) shipping.
- An end-to-end user can start at Unit 1 and complete the full path on Android, including spaced reviews of earlier units appearing on schedule.

**Decision gate.**
- Founder reviews the full path as a learner — not a checkbox, an actual cover-to-cover read.
- Editorial quality bar held: no shortcut units, no skipped regression sets, no claims without sources, no calibration tags missing.

**Resolves:** #3 (T1 staffing path — by the end of this phase we will know whether solo founder + LLM-assist was realistic; if not, the gate is where we hire / contract).

---

## Phase 4 — Launch readiness

**Goal.** Beta cohort + web preview + trademark cleared.

**Entry criteria.**
- Phase 3 complete.

**Work.**
- **Trademark check on Libella** with counsel. Resolve clean / oppose-risk / fallback. Per `STRATEGY.md` Naming, the primary risk to weigh is Libelle IT Group (different spelling, different industry).
- **Q4 web surface** — read-only preview, shareable unit links, no auth. Stripped-down, marketing-flavored. Reuses unit content from the same DB as the Android app.
- **Landing page / marketing copy** aligned to the Value Proposition. Use the *external (kind)* register, not the *internal (harsh)* one.
- **Beta cohort recruitment.** Target 20–50 α-PM users with real stakes — ideally a mix of career-switchers, current PMs, founders, and product-adjacent execs.
- **Cost monitoring dashboards.** Track LLM spend per session, per user, per unit. The prompt-caching strategy on the rubric is load-bearing for unit economics — surface cache hit rate as a primary metric.
- **App store listing prep.** Description, screenshots, category, keywords aligned to positioning.

**Exit criteria.**
- Trademark cleared, or fallback name committed, or launch deferred pending clarity.
- Web preview live and shareable.
- Beta cohort signed up and onboarded.
- Cost dashboards in place; first sustainable per-session cost figure recorded.

**Decision gate.**
- Founder commits to public launch.

**Resolves:** #1 (trademark check).

---

## Cross-cutting workstreams

These do not fit linearly into phases — they run continuously.

- **Authoring throughput.** From Phase 1 onward, authoring is the pacing constraint. Track *units-per-week* as the leading indicator. If sustained throughput drops below ~1 unit/week during Phase 3, surface it as a risk; revisit Q2 of T1 (author model — pull in a paid contractor).
- **Content reuse pass.** Existing glossary entries (Attention, Embedding, Context Window, Fine-tuning, Inference, Latency, Quantization, etc.) are raw material for the *bite* slot of new units. The wedge slots — trade-off framing, decision prompt, rubric, regression set — are always fresh. Done unit-by-unit during authoring, not as a separate workstream.
- **Cost discipline.** From Phase 2 onward, watch LLM spend. If cache hit rate on the rubric drops below ~70%, costs balloon — investigate immediately.
- **Calibration discipline.** P2 is non-negotiable. No claim ships without source + calibration tag. Reaffirm at every phase gate. Under deadline pressure, the temptation to ship un-verified LLM-drafted text is real (per TR2 in `STRATEGY.md`). Do not yield.

---

## Critical path

Engineering across all phases is roughly 3 months total. Authoring across Phases 2–3 is **3–6 months**, gated by single-author throughput. The critical path is authoring 20 units.

If at any point the project is asked to compress its timeline, the *only* lever that meaningfully changes the schedule is authoring throughput (more authors, paid contractor, narrowed v1 scope). Engineering compression buys little; the path waits on units.

---

## Restart / recovery (in case of API error or new chat session)

If you are a new chat session reading this for the first time:

1. **Read `docs/STRATEGY.md` in full first.** Strategy is anchored there, not here.
2. **Then read this doc.** Identify the current phase by checking entry/exit criteria against the actual repo state. Run `git log --oneline -20` to see recent activity.
3. **Open sub-decisions** are listed in `STRATEGY.md` § *"Open sub-decisions / what remains"*. This doc tracks which phase resolves each.
4. **Locked decisions are not up for re-debate** unless flagged explicitly as a strategy revision. The wedge (P1 + P2 primary) and the soul (learning, not retrieval) are load-bearing. Change them with care.
5. **The user has lived through losing prior chat sessions to API errors.** Documentation is their resilience strategy. Treat both `STRATEGY.md` and `EXECUTION.md` as the canonical record; treat chat history as ephemeral.
6. **Android-specific implementation decisions** (backup rules, encrypted prefs, Compose state-production pattern, JWT propagation, per-user cache scoping, etc.) live in `docs/ANDROID_BEST_PRACTICES.md`. That file is complementary to this one, not a substitute — strategy stays in `STRATEGY.md`, execution sequencing here, Android platform decisions there. Cross-reference it when working on the Android client or preparing for a Play Store release.
7. **Backend-specific implementation decisions** (production-secret enforcement, migration deploy pattern, F4 grader guardrails, prompt caching, grade-row replacement semantics, launch-readiness checklist) live in `docs/BACKEND_BEST_PRACTICES.md`. Same complementary relationship as item 6. Cross-reference it when touching the API surface, Postgres schema, or anything in the deployed Railway service.

---

## Provenance

This document was produced as the execution-plan companion to `docs/STRATEGY.md`. It translates locked strategy into sequenced phases without committing to dates. Phases are intentionally coarse; in-phase task management belongs in your tracker of choice (issues, project board, etc.), not in this document.
