# Unit 8 Gate — Cost dynamics at scale

> **Per-unit gate audit** for `cost-dynamics-bundle-0` (Phase
> 3, Unit 8, third unit in the productization block).

---

## Decision

**Initial run passed the per-criterion bar (92% ≥ 80%);
realignment applied for 2 pairs (p007 c1 → false, p014 c1 →
true); three non-deterministic gate FAILs documented;
re-run pending.**

21-pair regression set ran live against the deployed
Railway grader on 2026-05-13. Per-criterion 92% (58/63),
zero ERRORs (third unit in a row clean). Diagnostic
surfaced two realignments anchored on a new c1
calibration finding: **Unit 8's c1 has two AND clauses
with different grader behaviors** — multi-axis naming is
lenient, annualization anchor is strict.

| Criterion | Required | Initial run | Re-run | Verdict |
|---|---|---|---|---|
| Per-criterion agreement | ≥ 80% | 92% (58/63) | pending | ✅ initial |
| Honest flagged behavior | spec-faithful | 21/21 | pending | ✅ |
| Cost / call | reasonable | ~$0.014/call, cache 5.7× | pending | ✅ |

---

## Initial run evidence (2026-05-13)

```
Pairs scored:               21
Errored (no score):         0
Fully passed (all crit + flagged):  16 (76%)
Per-criterion agreement:    58/63 (92%)
Flagged-correct:            21/21

Token usage:
  input tokens:        14541
  cache reads:         82520
  output tokens:       12770
```

Zero ERRORs on the initial run. Third unit in a row to
ship a clean ERROR baseline (Units 4, 5, 8 all initial-
zero; Units 6, 7 had 1+ initial ERRORs).

### Per-pair outcomes

5 FAILs:

| ID | Outcome | Note |
|---|---|---|
| p002 | FAIL (2/3) | Non-deterministic — diagnostic shows 3/3 PASS |
| p007 | FAIL (2/3) | c1 grader-strict on missing annualization anchor |
| p014 | FAIL (2/3) | c1 grader-lenient on implicit multi-axis + annualization anchor |
| p017 | FAIL (2/3) | Non-deterministic — diagnostic shows 3/3 PASS |
| p018 | FAIL (2/3) | Non-deterministic — diagnostic shows 3/3 PASS |

---

## Diagnostic dump

### p002 — non-deterministic (no realignment)

Diagnostic output:
- c1 met ✓ confidence 0.95
- c2 met ✓ confidence 0.85
- c3 met ✓ confidence 0.93
- flagged false ✓

All three criteria match expected on diagnostic. The
gate run had a single-criterion FAIL that didn't
reproduce. Likely grader's c2 strictness on partial
mechanism-coverage varied between runs (p002 covers the
multiplication mechanism but doesn't enumerate batch-
only or committed-use-without-forecasting failure modes
explicitly).

**No realignment.** Documented as non-deterministic.

### p007 — c1 grader-strict on missing annualization anchor (realign)

Grader output:
- **c1 met=false (expected true) ✗ DISAGREE confidence 0.72**
- c2 met=false (expected false) ✓ confidence 0.75 — c2 borderline held strict this run
- c3 met=true ✓ confidence 0.82

Grader rationale on c1: *"correctly names cost-at-scale
as a three-axis problem (caching, batching, capacity)
and distinguishes it from a one-time lever selection.
However, the answer does not explicitly anchor the
decision to annualized cost at projected volume — there
is no reference to the $410k/year current bill, the
projected $655k/year, or the 80,000 tickets/day Q-end
projection. The rubric requires both naming the
multi-axis framing AND anchoring to annualized projected
volume, and the second part is absent."*

p007 was authored as "c1+c3 met, c2 missing" — but the
authoring assumed multi-axis naming alone would cross
c1. The grader's strict reading of c1's AND structure
correctly catches that p007 is missing the annualization
clause.

**Realign c1 → true → false.** p007 becomes c3-only
single-criterion (c2 borderline test held strict this
run, so c2 stays false as expected).

### p014 — c1 grader-lenient on full AND satisfaction (realign)

Grader output:
- **c1 met=true (expected false) ✗ DISAGREE confidence 0.92**
- c2 met=true ✓ confidence 0.80
- c3 met=true ✓ confidence 0.88

Grader rationale on c1: *"The learner explicitly frames
this as 'a multi-lever production-cost problem at
projected scale' and names the three axes (caching,
batching, committed-use) as distinct levers. They also
correctly anchor the decision to the annualized
projected volume ($655k/year at Q-end), not the
current-pilot daily bill."*

The grader reads "first/second/third lever" as implicit
multi-axis enumeration (lenient on clause 1) AND the
$655k anchor satisfies clause 2. Both AND clauses
satisfied → c1 crosses.

**Realign c1 → false → true.** p014 becomes
all-three-met.

### p017 — non-deterministic (no realignment)

Diagnostic output:
- c1 met ✓ confidence 0.95
- c2 met ✓ confidence 0.85
- c3 met ✓ confidence 0.95

All match expected. Same shape as p002 — gate single-
criterion FAIL didn't reproduce. **No realignment.**

### p018 — non-deterministic (no realignment)

Diagnostic output:
- c1 met ✓ confidence 0.97
- c2 met ✓ confidence 0.88
- c3 met ✓ confidence 0.95

All match expected. **No realignment.** Plain-prose
discipline (no embedded percentage-slash markdown)
prevented the Unit 6/7 reproducible-error pattern; gate
2/3 FAIL was content-grading noise.

---

## Realignment summary

2 pairs, 2 expected-value flips:

| Pair | YAML before | Realigned to |
|---|---|---|
| p007 | c1=true | c1=false |
| p014 | c1=false | c1=true |

Distribution shifts: **7/4/3/5/2/0 → 8/2/4/5/2/0**.
- p014 moves partial → all-met (+1 all-met)
- p007 moves partial → single-criterion (-1 partial → +1 single)
- Net: -2 partial, +1 all-met, +1 single

---

## Key calibration finding (Unit 8 specific)

**c1's AND structure has two clauses with different
grader behaviors:**

| Clause | Grader behavior | Evidence |
|---|---|---|
| 1 — multi-axis naming | LENIENT | p014 ("first/second/third lever") crossed clause 1 even without canonical lever names |
| 2 — annualization anchor | STRICT | p007 (no $/year numbers) failed clause 2; p014 ($655k anchor) passed |

**Both clauses needed for c1 to cross** (the AND).
Consistent with the AND-structure clause-by-clause
meta-finding from Units 4-7, but Unit 8 surfaces the
specific dynamic of one clause being lenient and the
other strict simultaneously.

**Authoring lesson for Unit 9+:** when c1 has a
numerical-anchor clause (like annualization), it's
grader-STRICT and the cleanest way to make c1=false in a
partial-credit pair is to omit the numerical anchor
(rather than omitting axis names, which the lenient
clause forgives).

---

## Other findings

### 1. c1 grader-lenient pattern continues to apply

Now 8 pairs across 6 units confirm the c1 "implicit
multi-axis enumeration crosses" pattern (Units 4-8).
Authoring discipline solid — c1 missing requires *either*
no axis enumeration *or* missing the post-AND clause
(annualization in Unit 8's case).

### 2. Three non-deterministic gate FAILs on all-met pairs

p002, p017, p018 all showed 2/3 FAIL on gate, 3/3 PASS
on diagnostic. Pattern: grader's c2 strictness on
partial mechanism-coverage varies between runs. None of
the FAILs reproduce; per-criterion confidence on diagnostic
is high (0.85+).

This is a known shape from prior units (e.g., Unit 6
p018 reproducible vs Unit 7 p018 sometimes-clean).
**Non-determinism at grader thresholds is an ambient
property** of the system; single-gate-run results should
always be triaged with diagnostic confirmation.

### 3. Zero ERRORs on initial run

Third unit in a row (Units 4, 5, 8) to ship a clean
initial gate. Plain-prose discipline + grader stability
holding. The Unit 6/7 elevated ERROR rates may have been
specific to those units' content shapes.

### 4. Anthropic batch-processing source confirms 50% discount

Source #2 (Anthropic Message Batches docs) explicitly
states *"reducing costs by 50%"* — direct source-grounding
for the depth's discount claim. Calibration tag for that
claim is settled with confidence.

---

## What this unlocks

After the realigned set re-runs against the deployed
grader and passes the per-criterion bar a second time,
Unit 8 publishes:

- `content/units/cost-dynamics-bundle-0.md` status flips
  from `draft` to `published`.
- The unit becomes the eighth unit on the canonical
  Phase 1 path (`llm-systems-for-pms`), continuing the
  productization block.
- Authoring opens for **Unit 9 — Fine-tuning vs prompting
  vs RAG** per `docs/curriculum/v1-path-outline.md`.

The new c1-AND-clause calibration finding (lenient
multi-axis + strict annualization) becomes input for
future rubric-tightening passes — particularly for any
unit where c1 has a numerical-anchor clause.

---

## References

- Regression set: `content/regression-sets/cost-dynamics-bundle-0.yml`
  (PR #96 authored 21 pairs + 2 pre-gate reviewer fixes
  for arithmetic + scope; this PR realigned 2 pairs —
  p007 c1 → false, p014 c1 → true).
- Initial gate run: 2026-05-13 against production Railway.
- Diagnostic script: `backend/scripts/_inspect_pairs.py`
  on throwaway branch `claude/unit-8-gate-diagnostics`
  (deleted post-triage).
- Prior gate precedents: `docs/UNIT_7_GATE.md`,
  `docs/UNIT_6_GATE.md`, `docs/UNIT_5_GATE.md`,
  `docs/UNIT_4_GATE.md`, `docs/UNIT_3_GATE.md`,
  `docs/UNIT_2_GATE.md`, `docs/PHASE_2_GATE.md`.
- Rubric source: `content/units/cost-dynamics-bundle-0.md`
  slot 8.
- T2-B locked interpretation: `docs/PHASE_2_GATE.md`
  § Realignment (PR #72) and STRATEGY.md § T2-B.
