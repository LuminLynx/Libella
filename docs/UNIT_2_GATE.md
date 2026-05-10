# Unit 2 Gate — Context Window

> **Per-unit gate audit** for `context-window-bundle-0` (Phase 3,
> Unit 2). Mirrors the structure of `docs/PHASE_2_GATE.md` but is
> scoped to one unit, per the discipline that Phase 3 ships
> per-unit gates as recurring, independently reviewable artifacts.

---

## Decision

**Initial run passed the per-criterion bar (87% ≥ 80%); realignment
applied; second run pending re-deploy.**

The 21-pair regression set was run live against the deployed Railway
grader on 2026-05-09. Per-criterion agreement was 87% (55/63), above
the 80% publish threshold. The disagreements were investigated with
a one-off diagnostic script (`backend/scripts/_inspect_pairs.py` on
the throwaway branch `claude/unit-2-gate-diagnostics`, deleted after
triage) and triaged per-pair. Six pairs realigned, two preserved as
documented disagreements, one new pair (p022) authored to backfill
flagged-expected coverage.

| Criterion | Required | Observed (initial run) | Verdict |
|---|---|---|---|
| Per-criterion agreement | ≥ 80% | **87% (55/63)** | ✅ |
| Honest flagged behavior | spec-faithful | Grader's T2-B reading more spec-faithful than YAML on p014/p015 — realigned | ✅ post-realign |
| Cost / call | reasonable | **~$0.011/call**, cache ratio **5.4×** (vs PHASE_2_GATE's 3.8×) | ✅ |

---

## Initial run evidence (2026-05-09)

21 pairs through the live grader (Anthropic Claude Sonnet 4.6,
prompt caching on rubric per STRATEGY.md § T2-E) on production
Railway. Same environment as `docs/PHASE_2_GATE.md`.

```
Pairs scored:               21
Errored (no score):         1
Fully passed (all crit + flagged):  14 (66%)
Per-criterion agreement:    55/63 (87%)
Flagged-correct:            18/21

Token usage (cost-relevant):
  input tokens:        11958
  cache reads:         65132
  output tokens:       10910
```

Cost ≈ $0.22 total (~$0.011/call). Cache discipline improved over
PHASE_2_GATE: 5.4× cache-read ratio vs the 3.8× observed during the
Phase 2 gate, validating that Unit 2's longer rubric (with sources
and depth context) still benefits from the prompt-caching strategy.

### Per-pair outcomes

| ID | Outcome | Note |
|---|---|---|
| p001 | PASS | All three met — balanced two-phase answer |
| p002 | PASS | All three met — cost-led framing |
| p003 | PASS | Off-topic, gradable, all-not-met — T2-B confirmed |
| p004 | PASS | All-missed on-topic — "always RAG" extreme |
| p005 | PASS | All three met — terse engineering voice |
| p006 | PASS | c1+c2 met, c3 missing |
| p007 | ERROR | Grader payload missing `grades`/`flagged` — T2-D guardrail rejection |
| p008 | FAIL | Grader marked c1 met (YAML: not met) |
| p009 | PASS | c1 only |
| p010 | FAIL | Grader marked c2 not met (YAML: met) |
| p011 | PASS | c3 only |
| p012 | PASS | All-missed on-topic — RAM analogy |
| p013 | PASS | Off-topic vendor pitch, gradable |
| p014 | FAIL | Grader marked flagged=false (YAML: true) |
| p015 | FAIL | Grader marked flagged=false (YAML: true) |
| p016 | PASS | Portuguese, all three met |
| p017 | FAIL | Grader: c1 not met, c2 met (YAML: c1 met, c2 not met) |
| p018 | PASS | Emoji + informal voice, all three met |
| p019 | FAIL | Grader marked c1 not met (YAML: met) |
| p020 | PASS | All-missed on-topic — refuses prompt premise |
| p021 | PASS | All three met — concise / punchy |

---

## Diagnostic dump (per-criterion detail)

Diagnostic script `backend/scripts/_inspect_pairs.py` was authored
on a throwaway branch (`claude/unit-2-gate-diagnostics`) to expose
the grader's per-criterion `met`/`confidence`/`answer_quote`/
`rationale` for the four borderline FAILs (p008, p010, p017, p019)
and to re-run p007 in isolation. Total diagnostic cost ≈ $0.05.
The branch and script were deleted after triage; this section
preserves the findings.

### p007 — transient ERROR, re-run scored cleanly

The original ERROR was a one-off malformed Anthropic response that
the T2-D guardrail correctly rejected. The isolated re-run scored
without error. Grader output on re-run:

- c1 met (YAML: met) ✓ confidence 0.9
- c2 met (YAML: not met) **✗ DISAGREE** confidence 0.85
- c3 met (YAML: met) ✓ confidence 0.92

The c2 disagreement: the answer says *"they cost more linearly with
what you put in"* — that names the linear-pricing mechanism. One
mechanism + one failure mode crosses the c2 bar per the rubric.
The "may not use long context well" gesture isn't carrying c2 here;
the cost-mechanism naming is. This was an authoring oversight —
p007 was supposed to be the "gestures-without-mechanism" borderline,
but the answer accidentally named the cost mechanism while gesturing
at recall. **Realign c2 → true.**

### p008 — preserved disagreement (grader-lenient on c1)

Grader output:
- **c1 met (YAML: not met) ✗ DISAGREE confidence 0.88**
- c2 met (YAML: met) ✓ confidence 0.92
- c3 met (YAML: met) ✓ confidence 0.93

Grader rationale on c1: *"the 'fits-more' dimension is slightly
implicit rather than explicitly named as a separate axis"* — but
calls it crossing the bar anyway based on the budget treatment.

The rubric says: *"Names the three-way trade-off ... AND treats
the context window as a budget."* Strict reading: both clauses
must be present. p008 demonstrates the budget treatment ("phase 1
keeps that bounded ... phase 2 multiplies it") but doesn't name
the three-way trade-off explicitly.

**Held YAML c1=false. Documented as preserved disagreement.**
Realigning to grader's lenient reading would teach the regression
set that implicit-is-enough on c1, drifting toward soft grading
on every future pair. Keeping the strict reading visible surfaces
the c1 phrasing slack for future rubric tightening.

### p010 — preserved disagreement (grader-strict on c2)

Grader output:
- c1 not met (YAML: not met) ✓ confidence 0.85
- **c2 not met (YAML: met) ✗ DISAGREE confidence 0.8**
- c3 not met (YAML: not met) ✓ confidence 0.9

Grader rationale on c2: *"Only one of the three failure modes from
the unit is addressed."* The grader is reading c2 as requiring
multiple mechanisms, or at least one mechanism + a gesture toward
another. p007 (with cost mechanism + recall gesture) crossed the
bar; p010 (cost mechanism alone) didn't.

The rubric says: *"identifies a concrete failure mode of 'just use
the long window' beyond hitting the hard limit, AND identifies
the mechanism behind it"* — singular *"a"*. p010 names the cost
mechanism cleanly. By literal reading, c2 should be met.

**Held YAML c2=true. Documented as preserved disagreement.**
Same direction as tokenization p020 in PHASE_2_GATE — grader more
rigorous than rubric language requires. Two preserved disagreements
across this set (p008, p010) bracket the rubric language slack
from both sides: lenient on c1, strict on c2.

### p017 — partial realign

Grader output:
- **c1 not met (YAML: met) ✗ DISAGREE confidence 0.72**
- **c2 met (YAML: not met) ✗ DISAGREE confidence 0.75**
- c3 met (YAML: met) ✓ confidence 0.8

Grader rationale on c1: *"the answer never explicitly articulates
the three-way trade-off as 'fits-more vs. costs-more vs. recall-
can-degrade.' The context window is never framed as a budget rather
than a capacity ceiling."* Pseudocode comments mention "Three-axis
check: cost, recall, volume" but in code-comment form, not as
PM-decision prose.

Grader rationale on c2: *"linear cost scaling ('Linear cost scales
with corpus size') and recall degradation on multi-needle cross-doc
queries ('Cross-doc multi-needle queries → recall degrades') ...
two concrete failure modes."*

**Realign c1 → false, c2 → true. c3 stays true.** Discovery:
pseudocode-shaped answers don't surface explicit budget framing
to the grader even when the substance is right. Worth carrying
into future regression-set authoring.

### p019 — realign c1, depth source-grounding confirmed working

Grader output:
- **c1 not met (YAML: met) ✗ DISAGREE confidence 0.85**
- c2 not met (YAML: not met) ✓ confidence 0.9
- c3 not met (YAML: not met) ✓ confidence 0.9

Grader rationale on c1: *"never actually works through the
three-way trade-off ... the budget framing is immediately
abandoned in favor of treating the 1M window as a capacity number
to fill for both phases."*

Grader rationale on c2: *"mid-context recall degradation is waved
away by citing vendor claims."* This explicitly confirms the
grader's depth source-grounding is working — it caught the
vendor-claim-as-evidence anti-pattern that the unit's depth
section warns against.

**Realign c1 → false.** Pre-gate concern that the grader might
read vendor claims as evidence-of-mechanism turned out wrong. The
depth is doing its job; this is a YAML-only fix, not a rubric or
depth tightening problem.

### p014, p015 — flag realignment

Both pairs had `flagged=true` expected. Grader marked both as
`flagged=false` with all criteria confidently not-met.

- p014 ("Use RAG.", three words): grader confidently graded all
  not-met. No uncertainty → no flag, per T2-B literal reading.
- p015 (verbose punt): grader confidently graded all not-met.
  The "rambling" surface form didn't trip uncertainty; absence-of-
  engagement is itself a confident judgment.

**Realign both flagged → false.** Same pattern as PHASE_2_GATE
PR #72 realignment of p003/p014/p015 in tokenization-bundle-0:
off-topic-but-confidently-gradable = `flagged=false`.

---

## Realignment summary

Six pairs realigned (seven expected-value flips):

| Pair | YAML before | Realigned to |
|---|---|---|
| p007 | c2=false | c2=true |
| p014 | flagged=true | flagged=false |
| p015 | flagged=true | flagged=false |
| p017 | c1=true, c2=false | c1=false, c2=true |
| p019 | c1=true | c1=false |

Two pairs preserved as documented disagreements:

| Pair | Disagreement axis | Direction |
|---|---|---|
| p008 | c1 | grader-lenient (rubric strict, grader implicit-framing) |
| p010 | c2 | grader-strict (rubric singular, grader multi-mechanism) |

One new pair authored to backfill flag coverage:

| Pair | Shape |
|---|---|
| p022 | Partial credit (c2 met) with hedge language designed to trigger genuine grader uncertainty on c1 framing and c3 regime distinction. Expected `flagged=true` from confidence-floor override. |

---

## What this unlocks

After the realigned set re-runs against the deployed grader and
passes the per-criterion bar a second time, Unit 2 publishes:

- `content/units/context-window-bundle-0.md` status flips from
  `draft` to `published`.
- The unit becomes the second unit on the canonical Phase 1 path
  (`llm-systems-for-pms`), as locked in
  `docs/curriculum/v1-path-outline.md`.
- Authoring opens for **Unit 3 — Latency** per the same path
  outline, with the per-unit gate discipline established here as
  the recurring artifact for every subsequent unit.

The two preserved disagreements (p008, p010) become inputs for a
future rubric-tightening pass — likely as part of authoring Unit 3
or Unit 4, when the rubric language gets re-read against accumulated
gate evidence.

---

## References

- Regression set: `content/regression-sets/context-window-bundle-0.yml`
  (PR #75 authored 21 pairs; this PR realigned 6, preserved 2,
  added 1).
- Initial gate run: 2026-05-09 against production Railway.
- Diagnostic script: `backend/scripts/_inspect_pairs.py` on
  throwaway branch `claude/unit-2-gate-diagnostics` (deleted
  post-triage).
- Phase 2 gate precedent: `docs/PHASE_2_GATE.md`.
- Rubric source: `content/units/context-window-bundle-0.md` slot 8.
- T2-B locked interpretation: `docs/PHASE_2_GATE.md` § Realignment
  (PR #72) and STRATEGY.md § T2-B.
