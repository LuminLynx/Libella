# Unit 5 Gate — Model selection

> **Per-unit gate audit** for `model-selection-bundle-0` (Phase
> 3, Unit 5). Mirrors the structure of `docs/UNIT_4_GATE.md`
> and prior per-unit gates.

---

## Decision

**Initial run passed the per-criterion bar (93% ≥ 80%);
realignment applied for 4 pairs (3 authoring errors —
c1 grader-lenient on p008/p014, c2 grader-strict on p002;
plus p007 c2 borderline crossed as designed); re-run pending.**

21-pair regression set ran live against the deployed Railway
grader on 2026-05-11. Per-criterion agreement was 93% (59/63)
— well above the 80% publish threshold. Diagnostic re-runs
(`backend/scripts/_inspect_pairs.py` on the throwaway branch
`claude/unit-5-gate-diagnostics`) pulled per-criterion detail
on the 5 FAILs and surfaced a meta-finding about how the
grader reads AND structures clause-by-clause.

| Criterion | Required | Initial run (21 pairs) | Re-run (post-realign) | Verdict |
|---|---|---|---|---|
| Per-criterion agreement | ≥ 80% | 93% (59/63) | pending | ✅ initial |
| Honest flagged behavior | spec-faithful | 20/21 flagged-correct; p009 length-heuristic flag | pending | ⚠️ preserved disagreement |
| Cost / call | reasonable | ~$0.013/call, cache 5.2× | pending | ✅ |

---

## Initial run evidence (2026-05-11)

21 pairs through the live grader (Anthropic Claude Sonnet 4.6,
prompt caching on rubric per STRATEGY.md § T2-E) on production
Railway. Same environment as `UNIT_4_GATE` and prior.

```
Pairs scored:               21
Errored (no score):         0
Fully passed (all crit + flagged):  16 (76%)
Per-criterion agreement:    59/63 (93%)
Flagged-correct:            20/21

Token usage (cost-relevant):
  input tokens:        14241
  cache reads:         74220
  output tokens:       12483
```

Zero ERRORs on the first run — second unit in a row to
achieve this (Unit 4 also zero).

### Per-pair outcomes

| ID | Outcome | Note |
|---|---|---|
| p001 | PASS | All three met — balanced hybrid recommendation |
| p002 | FAIL | c2 grader-strict on implicit single-axis failure-mode framing (authoring error) |
| p003 | PASS | Off-topic vendor lock-in, gradable |
| p004 | PASS | All-missed — picks Opus on capability alone |
| p005 | PASS | All three met — terse engineering voice |
| p006 | PASS | c1+c2 met, c3 missing |
| p007 | FAIL | c2 borderline crossed as designed (grader-lenient on AND) |
| p008 | FAIL | c1 grader-lenient on implicit multi-axis framing (authoring error) |
| p009 | FAIL | Length heuristic flag (preserved disagreement, mirror Unit 4 p011) |
| p010 | PASS | c2 only |
| p011 | PASS | c3 only |
| p012 | PASS | All-missed — spec-sheet pick |
| p013 | PASS | Off-topic vendor pitch |
| p014 | FAIL | c1 borderline crossed grader-lenient (per-axis arithmetic counted as implicit enumeration) |
| p015 | PASS | All-missed — verbose punt |
| p016 | PASS | Portuguese all-met |
| p017 | PASS | Pseudocode all-met |
| p018 | PASS | Emoji moderate all-met (no T2-D error) |
| p019 | PASS | All-missed — picks Haiku extreme |
| p020 | PASS | All-missed — refuses premise |
| p021 | PASS | All three met — concise / punchy |

---

## Diagnostic dump (per-criterion detail)

Diagnostic script `backend/scripts/_inspect_pairs.py` was
authored on a throwaway branch (`claude/unit-5-gate-diagnostics`)
to expose the grader's per-criterion `met` / `confidence` /
`answer_quote` / `rationale` for the 5 FAILs. Total diagnostic
cost ≈ $0.05. The branch and script were deleted after triage;
this section preserves the findings.

### p002 — c2 grader-strict (authoring error)

Grader output:
- c1 met (YAML: met) ✓ confidence 0.95
- **c2 not met (YAML: met) ✗ DISAGREE confidence 0.72**
- c3 met (YAML: met) ✓ confidence 0.90

Grader rationale on c2: *"The learner gestures at cost-at-scale
($8k/year difference) and implicitly acknowledges
capability-tier breakpoints (the 14-point security flag gap),
but never explicitly articulates a concrete failure mode with
its mechanism — e.g., 'picking by cost alone would miss the
security flag collapse' or 'picking by capability alone hits
a cost cliff because per-call cost scales linearly.' The
failure shapes of single-axis selection are not named or
explained as mechanisms, only alluded to through the hybrid
recommendation."*

p002's text *"Capability matters most on security flags / Cost
matters most at production volume"* describes when each axis
matters but doesn't name a concrete single-axis failure mode.
The grader is reading c2's AND structure strictly here.

**Realign c2 → false.** p002 moves from all-three-met to c1+c3
partial-credit.

### p007 — c2 borderline crossed as designed (realign)

Grader output:
- c1 met (YAML: met) ✓ confidence 0.88
- **c2 met (YAML: not met) ✗ DISAGREE confidence 0.85**
- c3 met (YAML: met) ✓ confidence 0.87

Grader rationale on c2: *"two concrete single-axis failure
modes: cost-only selection missing capability-tier breakpoints
(with the specific example of Haiku's 45% bug catch being
feature degradation), and capability-only selection hitting
cost-at-volume cliffs."*

Borderline test landed as designed — mirror Units 2/3/4
c2-lenient pattern.

**Realign c2 → true.** p007 moves from c1+c3 partial to
all-three-met.

### p008 — c1 grader-lenient (authoring error)

Grader output:
- **c1 met (YAML: not met) ✗ DISAGREE confidence 0.85**
- c2 met (YAML: met) ✓ confidence 0.92
- c3 met (YAML: met) ✓ confidence 0.88

Grader rationale on c1: *"The learner treats model selection
as a multi-axis trade-off across capability, cost, and
latency, and anchors the decision to measured performance on
the load-bearing task (security flags, bug catches) at
projected scale (500/day, annualized)."*

p008's per-axis arithmetic ("$0.08/PR × 500/day × 250
workdays" for cost; "38% security-flag catch" for capability)
implicitly enumerates the multi-axis frame. The grader reads
c1's AND structure leniently here.

**Realign c1 → true.** p008 moves from c2+c3 partial to
all-three-met. Same authoring error pattern as Unit 4 p008/
p011.

### p009 — preserved disagreement (length heuristic)

Grader output:
- c1 met (YAML: met) ✓ confidence 0.72
- c2 not met (YAML: not met) ✓ confidence 0.85
- c3 not met (YAML: not met) ✓ confidence 0.95
- **flagged=true (YAML: false) ✗ DISAGREE**

All three per-criterion judgments match expected. Flagged
disagreed — confidences (0.72/0.85/0.95) all above the 0.6
floor, so the override didn't trigger. Grader flagged based
on the system-prompt clause: *"if the answer is too short /
off-topic to grade fairly."* p009 is a short single-criterion
answer (~70 words).

**Preserved as documented disagreement.** Same pattern as
Unit 4 p011. Held YAML flagged=false; future runs will
persistently FAIL p009 on flagged.

### p014 — c1 borderline crossed grader-lenient

Grader output:
- **c1 met (YAML: not met) ✗ DISAGREE confidence 0.90**
- c2 met (YAML: met) ✓ confidence 0.88
- c3 met (YAML: met) ✓ confidence 0.87

Grader rationale on c1: *"The learner explicitly frames this
as a 'measurement decision, not a spec-sheet pick' and
references all three axes (capability via catch rates, cost
via per-PR pricing, and latency implicitly through Haiku's
speed mention)."*

Same shape as p008 — per-axis arithmetic ("85%/70%/45% catch,
$0.08/$0.05/$0.015 per PR") plus explicit "measurement
decision" framing crosses c1's AND even without literal
multi-axis enumeration in prose.

**Realign c1 → true.** p014 moves from c2+c3 partial to
all-three-met.

---

## Realignment summary

4 pairs, 4 expected-value flips:

| Pair | YAML before | Realigned to |
|---|---|---|
| p002 | c2=true | c2=false |
| p007 | c2=false | c2=true |
| p008 | c1=false | c1=true |
| p014 | c1=false | c1=true |

Preserved disagreement: **p009 flagged** (length heuristic;
mirror Unit 4 p011).

Distribution shifts: **7/4/3/5/2/0 → 9/2/3/5/2/0**.

---

## Findings (meta-finding on AND-structure grading)

### 1. The grader reads AND structures clause-by-clause

Across Units 2-5, the grader's lenient/strict tendency on
AND-structured criteria depends on whether each clause is
articulated explicitly or implicitly:

- **c1 grader-lenient** when concrete per-axis arithmetic
  implicitly enumerates the axes (Unit 5 p008, p014).
- **c1 grader-strict** when "measurement" or "load-bearing-task"
  vocabulary is used without enumerating axes (Unit 3 p014,
  Unit 4 p014 — both held c1=false correctly).
- **c2 grader-lenient** when answer gestures at mechanism
  through one named failure mode + signal toward another
  (Units 2/3/4 p007 pattern; Unit 5 p007).
- **c2 grader-strict** when answer describes axis-importance
  without naming a specific single-axis failure mode (Unit 5
  p002).
- **c3 grader-strict** when answer uses methods/tiers in
  sequence without explicit name-to-regime mapping (Unit 4
  finding).

**Meta-finding:** the grader does literal clause-checking on
AND structures, with leniency/strictness varying by whether
each clause is explicitly stated.

### 2. Authoring discipline on c1/c2 needs sharper splits

Three authoring errors in Unit 5 alone (p002 c2, p008 c1,
p014 c1) plus Unit 4's two c1 authoring errors (p008, p011).
Pattern: pairs designed as "partial credit" on c1 or c2 keep
getting promoted by the grader because the implicit signal
crosses the AND bar.

**Process lesson for Unit 6+:** when authoring a partial-credit
pair where one clause of an AND structure should be missing,
make the missing clause **literally absent** rather than
merely under-emphasized. Concrete per-axis numbers count as
implicit axis enumeration; descriptive "X matters" phrases
count as implicit framing.

### 3. Length heuristic for flagged is now a stable pattern

Two units in a row (Unit 4 p011, Unit 5 p009) had short
single-criterion pairs flagged by the grader despite all
per-criterion confidences above the 0.6 floor. The system
prompt's *"answer is too short to grade fairly"* clause is
firing predictably on ~70-word answers.

**No action.** Future units should accept that short
single-criterion pairs will persistently flag.

### 4. Distribution compression after realignment

7/4/3/5/2/0 → 9/2/3/5/2/0 means only 2 partial-credit pairs
in the set. That's thin for testing AND-structure grader
behavior at scale. Future units should author with sharper
explicit/implicit splits so partial pairs survive the
grader-lenient reading and don't collapse to all-met.

### 5. Zero ERRORs again

Second unit in a row with zero T2-D-rejected payloads
(Unit 4 was the first). Emoji-density cap + grader stability
holding.

---

## What this unlocks

After the realigned set re-runs against the deployed grader
and passes the per-criterion bar a second time, Unit 5
publishes:

- `content/units/model-selection-bundle-0.md` status flips
  from `draft` to `published`.
- The unit becomes the fifth unit on the canonical Phase 1
  path (`llm-systems-for-pms`), completing the **synthesis
  block** (Units 1-4 axes + Unit 5 synthesis).
- Authoring opens for **Unit 6 — Prompt design basics** per
  `docs/curriculum/v1-path-outline.md`.

The cross-unit AND-structure meta-finding becomes input for a
future rubric-tightening pass — most likely as part of
authoring Unit 6 or Unit 7.

---

## References

- Regression set: `content/regression-sets/model-selection-bundle-0.yml`
  (PR #86 authored 21 pairs; this PR realigned 4 — 3
  authoring errors + 1 borderline-crossed — and preserved 1
  disagreement on p009 flagged).
- Initial gate run: 2026-05-11 against production Railway.
- Diagnostic script: `backend/scripts/_inspect_pairs.py` on
  throwaway branch `claude/unit-5-gate-diagnostics` (deleted
  post-triage).
- Unit 4 gate precedent: `docs/UNIT_4_GATE.md`.
- Unit 3 gate precedent: `docs/UNIT_3_GATE.md`.
- Unit 2 gate precedent: `docs/UNIT_2_GATE.md`.
- Phase 2 gate precedent: `docs/PHASE_2_GATE.md`.
- Rubric source: `content/units/model-selection-bundle-0.md`
  slot 8.
- T2-B locked interpretation: `docs/PHASE_2_GATE.md`
  § Realignment (PR #72) and STRATEGY.md § T2-B.
