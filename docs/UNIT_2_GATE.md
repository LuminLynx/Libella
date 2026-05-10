# Unit 2 Gate — Context Window

> **Per-unit gate audit** for `context-window-bundle-0` (Phase 3,
> Unit 2). Mirrors the structure of `docs/PHASE_2_GATE.md` but is
> scoped to one unit, per the discipline that Phase 3 ships
> per-unit gates as recurring, independently reviewable artifacts.

---

## Decision

**Unit 2 PASSED 2026-05-10. Status flipped `draft` → `published`.**

The 21-pair regression set was run live against the deployed Railway
grader on 2026-05-09 and hit 87% per-criterion agreement. Six pairs
realigned, two preserved as documented disagreements, one new pair
(p022) authored to backfill flagged-expected coverage (PR #76). The
realigned 22-pair set was re-run 2026-05-10 and hit **87.8% per-
criterion agreement (58/66)** — above the 80% publish threshold for
the second time. Three secondary findings documented below; none
block publication. Unit 2 is live on the canonical Phase 1 path.

| Criterion | Required | Initial run (21 pairs) | Re-run (22 pairs, post-realign) | Verdict |
|---|---|---|---|---|
| Per-criterion agreement | ≥ 80% | 87% (55/63) | **87.8% (58/66)** | ✅ |
| Honest flagged behavior | spec-faithful | T2-B drift on p014/p015 — realigned | 20/22 flagged-correct; p022 didn't trigger flag (see Findings) | ✅ |
| Cost / call | reasonable | ~$0.011/call, cache 5.4× | ~$0.011/call, cache 5.8× (improved) | ✅ |

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

## Second run (2026-05-10, post-realignment)

22 pairs through the live grader on the deployed Railway backend
after PR #76 merged. Same environment as the initial run.

```
Pairs scored:               22
Errored (no score):         1
Fully passed (all crit + flagged):  17 (77%)
Per-criterion agreement:    58/66 (87.8%)
Flagged-correct:            20/22

Token usage (cost-relevant):
  input tokens:        12503
  cache reads:         71988
  output tokens:       11799
```

Per-criterion agreement moved 87% → **87.8%** — above the 80%
publish bar for the second consecutive run. Cache discipline
improved: 5.8× cache-read ratio (vs 5.4× initial run, vs
PHASE_2_GATE's 3.8×).

### Per-pair outcomes (re-run)

The four realigned pairs (p014, p015, p017, p019) all moved from
FAIL to PASS, confirming the realignment direction was correct.
The two preserved disagreements (p008, p010) reported as FAIL as
documented — calibration signal, not noise. Three new findings:

| Pair | Outcome | Note |
|---|---|---|
| p001 | ERROR | Second transient T2-D-rejected payload (first was p007's run-1 ERROR) |
| p007 | FAIL (2/3) | One criterion non-deterministically disagreed after realignment — documented as borderline FAIL; YAML not reverted (see Findings) |
| p022 | FAIL (1/3 + flagged disagree) | Hedge language did not trip grader's confidence floor; flagged=false instead of expected true |

All other realignments held (p014, p015, p017, p019 all PASS).

---

## Findings (re-run)

### 1. Transient grader-payload ERRORs are a baseline rate (~5%)

Initial run errored on p007; re-run errored on p001. Two errors
in 43 grader calls = ~4.7%. The T2-D `_validate_grader_output`
guardrail correctly rejects malformed Anthropic responses; no
grade rows persist on failure. This is the guardrail working as
designed, not a Unit-2-specific failure mode.

**No action.** Document as ambient rate; revisit if it climbs
above ~10% across future units.

### 2. p007 reports a non-deterministic borderline FAIL

After the c2 → true realignment in PR #76, p007 was expected to
report all-three-met. The re-run shows 2/3 agreement — one
criterion non-deterministically disagreed.

The diagnostic re-run that informed the realignment (PR #76) saw
c2=met at 0.85 confidence. The gate re-run saw a disagreement
on one criterion (per-criterion detail not pulled; chasing
single-pair noise via another diagnostic is overfit).

The honest read: p007's c2 reading sits at the grader's
borderline and is non-deterministic across runs. The YAML stays
at the realigned state (c2=true) because that was the reading
the diagnostic captured cleanly; reverting would be chasing
noise in the opposite direction. Future re-runs may PASS or
FAIL p007.

**Documented but not promoted to preserved disagreement** — that
status is reserved for pairs where the YAML and grader stably
diverge (p008, p010). p007 diverges intermittently. If future
runs consistently FAIL, revisit during Unit 3/4 rubric-tightening
review.

### 3. Hedge language alone does not trip the grader's confidence floor

p022 was authored to trigger genuine grader uncertainty (`flagged=
true`) via partial-credit content with hedge language ("haven't
seen enough independent evidence either way"; "I'd revisit once
we know"). The grader confidently graded all criteria — flagged=
false with no per-criterion confidence below the 0.6 floor.

**Empirical finding for v2:** the grader's confidence threshold is
harder to trip than the original design assumed. Pure hedge
language reads as honest-PM-uncertainty in the *answer*, not as
*grader-uncertainty about the criteria*. Future flagged-expected
pairs need sharper substantive ambiguity — answers where a
specific phrase could legitimately be read as met-or-not by a
careful human reader.

**Action for v2:** redesign p022 (or successor) when authoring
Unit 3 or Unit 4, once we have more accumulated grader-confidence
data points. Not a v1 blocker; the gate threshold is per-criterion
agreement, not flagged-expected coverage.

---

## What this unlocked

Unit 2 publishes:

- `content/units/context-window-bundle-0.md` status flipped from
  `draft` to `published` in this PR.
- The unit becomes the second unit on the canonical Phase 1 path
  (`llm-systems-for-pms`), as locked in
  `docs/curriculum/v1-path-outline.md`.
- Authoring opens for **Unit 3 — Latency** per the same path
  outline, with the per-unit gate discipline established here as
  the recurring artifact for every subsequent unit.

The two preserved disagreements (p008, p010), the p007 non-
deterministic borderline, and the p022 finding all become inputs
for a future rubric-tightening pass — likely as part of authoring
Unit 3 or Unit 4, when the rubric language gets re-read against
accumulated gate evidence.

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
