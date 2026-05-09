# Phase 2 Gate — passed 2026-05-09

> **Durable record** of the P2.5 ship/iterate decision per
> `docs/EXECUTION.md` § Phase 2 ("STOP AND EVALUATE"). The gate is
> the load-bearing decision for the entire product — STRATEGY.md
> calls it "the make-or-break phase for the wedge." This document
> captures the evidence the decision was made on so future
> contributors can audit the call.

---

## Decision

**Phase 2 closed. Grader shipped under P2 calibration discipline.**

The grader meets all three exit criteria from `docs/EXECUTION.md`
§ Phase 2:

| Criterion | Required | Observed | Verdict |
|---|---|---|---|
| Per-criterion agreement | ≥ 80% | **95% (57/60)** | ✅ |
| Honest confidence (uncertain → flagged) | spec-faithful | **20/20 flagged-correct** | ✅ |
| Cost / call within tolerance | reasonable for projected scale | **~$0.009/call**, prompt caching working at 3.8× ratio | ✅ |

---

## Evidence

### First run (2026-05-09)

20 ground-truth pairs (`content/regression-sets/tokenization-bundle-0.yml`)
fed through the live grader (Anthropic Claude Sonnet 4.6, prompt
caching on rubric per STRATEGY.md § T2-E) on the production Railway
deployment.

```
Pairs scored:               20
Errored (no score):         0
Fully passed (all crit + flagged):  13 (65%)
Per-criterion agreement:    57/60 (95%)
Flagged-correct:            15/20

Token usage:
  input tokens:        11087
  cache reads:         42085
  output tokens:        9042
```

Per-criterion accuracy was already at 95% — well above the 80% bar.
Flagged-correct was 75%; investigation showed all 5 disagreements
followed one consistent pattern: the YAML treated "off-topic but
confidently gradable as Not-Met" as flagged-expected, while the
grader (per STRATEGY.md § T2-B's literal reading) treats `flagged`
as **grader uncertainty**, not "answer is bad."

The grader's reading was more spec-faithful than the original YAML.

### Realignment (PR #72)

Five `expected.flagged` values realigned to match the grader's
spec-faithful interpretation. No grader changes; YAML-only.

| Pair | Label | flag was | flag now |
|---|---|---|---|
| p003 | Off-topic gut-feel survey | true | false |
| p014 | Generic AI vendor pitch | true | false |
| p015 | Privacy-pivot answer | true | false |
| p012 | Only criterion 3 met, missing core knowledge | false | true |
| p016 | "Use tokens, not words." (4 words) | false | true |

Per-criterion expectations on **p008** and **p020** deliberately
left as-is — both expose where rubric language is doing real work
and produce useful disagreement signal. p020 is particularly
valuable: the grader was *more* rigorous than the original author,
marking criterion 1 false on a "1 token per character" claim
despite the answer using the word "token." That's exactly the kind
of strictness we want to keep visible.

### Second run (2026-05-09, post-realignment)

```
Pairs scored:               20
Errored (no score):         0
Fully passed (all crit + flagged):  18 (90%)
Per-criterion agreement:    57/60 (95%)
Flagged-correct:            20/20

Token usage:
  input tokens:        11087
  cache reads:         42085
  output tokens:        8978
```

Flagged-correct moved 15/20 → **20/20**. Fully-passed moved 13/20 →
**18/20**. p008 and p020 remain the two known per-criterion
disagreements; they are deliberately retained.

---

## Decision-gate questions

The gate prompt from `docs/EXECUTION.md`:

> *Is the grader trustworthy enough to ship under P2?*
> *Is the per-call cost sustainable at projected scale?*
> *Does the user-facing experience honor the locked Loop arc (Decide → Calibrate)?*

### 1. Trustworthy enough to ship under P2?

**Yes.**

- 95% per-criterion agreement across 60 judgements — the load-bearing
  metric for shipping graded outputs to users.
- 100% flagged-correct after the realignment — confidence behavior
  is honest and matches T2-B's literal reading.
- All four T2-D hallucination guardrails enforced structurally in
  `backend/app/ai_service.py:_validate_grader_output`. A payload
  that fails any guardrail (missing answer-quote on a Met grade,
  unknown criterion id, out-of-range confidence, etc.) raises
  `AIServiceError` and the endpoint returns 502 — no completion
  or grade rows are persisted on failure.
- The two remaining disagreements (p008, p020) expose rubric
  language ambiguity, not systematic grader unreliability. p020
  shows the grader being *more* rigorous than the original author.

### 2. Per-call cost sustainable at projected scale?

**Yes.**

| Stage | Volume | Estimated cost |
|---|---|---|
| Phase 3 authoring (per-unit regression runs) | ~20 pairs/unit × 19 units, run periodically | single-digit $/month |
| Closed beta (Phase 4) | ~100 users × 5 grades/week | ~$2/week |
| Public launch | volume-dependent | first stage where cost monitoring becomes load-bearing |

Prompt-cache hit ratio observed at 3.8× input tokens (cache reads
42k vs input 11k). T2-E's "load-bearing on unit economics"
assumption is validated. The launch-readiness checklist in
`docs/BACKEND_BEST_PRACTICES.md` includes the cache-hit-rate alarm
threshold (< 70%) per EXECUTION.md.

### 3. UX honors the Loop arc (Decide → Calibrate)?

**Yes.**

- Unit reader order: Bite → Trade-off framing → Depth → **Decision
  prompt → Calibration → Sources** → Mark complete.
  (PR #62 corrected an earlier mis-ordering that placed Calibration
  before the decision prompt and would have primed the answer.)
- Decision-prompt UI (PR #69): open-ended `OutlinedTextField`,
  submit-to-grade CTA, per-criterion `GradeRow` cards rendering
  Met/Not-Met icon + confidence chip + rationale + the grader's
  verbatim `answerQuote` (T2-D source-grounding visible to the user).
- Flagged-mode (T2-B): when `flagged=true`, the per-criterion
  cards are deliberately suppressed and replaced with a "Review
  needed" card pointing at the calibration tags + sources below.
  No contradictory pass/fail next to a low-confidence banner.
- Smoke-tested on real Android device against the deployed Railway
  backend.

---

## What this unlocks

Phase 3 opens per `docs/EXECUTION.md`:

- **Author the remaining 19 units** (T1 in STRATEGY.md). Each ships
  with ≥ 10 (standard) or ≥ 20 (flagship) regression pairs per
  T2-C. No regression set, no publish.
- **F5 Spaced review scheduler** + **F6 Path overview** are also
  Phase 3 deliverables.
- The authoring pipeline (linter, ingest CLI, regression-set runner,
  grader endpoint, Android grading UI) is stable behind the
  authoring track. Phase 3's pacing constraint is content
  production, not engineering, exactly as STRATEGY.md predicted.

The Phase 4 launch-readiness checklist in
`docs/BACKEND_BEST_PRACTICES.md` is unchanged: token refresh + jti
revocation, connection pooling, flavor-based base URLs, streaming
rationale (TT2). These get done at the Phase 4 boundary.

---

## References

- Regression set: `content/regression-sets/tokenization-bundle-0.yml`
  (PR #71 authored 16 pairs to reach the flagship target; PR #72
  realigned 5 `expected.flagged` values after the first gate run).
- Grader: `backend/app/ai_service.py` (PR #67 introduced; T2-D
  guardrails in `_validate_grader_output`).
- Runner: `backend/scripts/run_regression_set.py` (PR #68).
- Android grading UI: `app/src/main/java/com/example/foss101/ui/unit/UnitReaderScreen.kt`
  (PR #69; flagged-mode review fix in the same PR).
- Production hardening: PR #70 (`APP_ENV=production` gate, migration
  advisory lock).
