# Unit 12 Gate — Tool use / function calling

> **Per-unit gate audit** for `tool-use-bundle-0` (Phase 3,
> Unit 12). Mirrors the structure of `docs/UNIT_11_GATE.md` and
> `docs/UNIT_10_GATE.md` — per-unit gates ship as recurring,
> independently reviewable artifacts.

---

## Decision

**Unit 12 PASSED 2026-05-17 — true 100% (63/63).** Initial run
hit 95% (60/63) with zero payload ERRORs and 21/21
flagged-correct — the cleanest initial run in the path. Three
cross-pair-consistent c3 realignments (p008, p011, p021, all c3
True→False) landed; the realignment re-run hit **100% (63/63),
0 ERRORs, 21/21 flagged-correct, 21/21 fully passed** with no
residual blemish — cleaner than Unit 11, which needed a post-
re-run p014 rewrite. The unit was published on author (PR #123,
`status: published`); no flip required. **Zero rewrites, zero
flag realignments, zero preserved disagreements** — continuing
the Unit 9/10/11 precedent and setting a new best.

| Criterion | Required | Initial run (21 pairs) | Realignment re-run | Verdict |
|---|---|---|---|---|
| Per-criterion agreement | ≥ 80% | 95% (60/63), 0 ERRORs | **100% (63/63), 0 ERRORs** | ✅ |
| Honest flagged behavior | spec-faithful | 21/21 | 21/21 | ✅ |
| Cost / call | reasonable | ~$0.011/call | ~$0.011/call | ✅ |

### Re-run evidence (2026-05-17, post-realignment)

```
Pairs scored:               21
Errored (no score):         0
Fully passed (all crit + flagged):  21 (100%)
Per-criterion agreement:    63/63 (100%)
Flagged-correct:            21/21
```

All three realigned pairs (p008, p011, p021) graded `crit=3/3
flagged=ok` against the realigned c3=False reading. Zero payload
ERRORs on the re-run confirms the no-parenthetical-option-lists
constraint holds across runs, not just by luck. 21/21
flagged-correct confirms the flagged-expected retirement holds.
Unit 12 is the first unit in the path to reach a clean 100% on
the realignment re-run with **no residual rewrite** (Unit 4 and
Unit 8 reached 100% but via different paths; Unit 11 needed a
post-re-run p014 rewrite). 95% → 100%, three realignments, zero
rewrites — the cleanest gate closeout to date.

---

## Headline: three carried-forward constraints validated

Unit 12 is the first unit authored under the `UNIT_11_GATE.md`
constraints. The initial run is the test of whether those
constraints were correct. All three held:

1. **No parenthetical option-lists → zero payload ERRORs.** Unit
   11 had two *deterministic* grader-payload bugs (p007, p014),
   both traced to parenthetical option-lists in answer text. Unit
   12 enforced the prose-not-parentheticals constraint
   structurally across all 21 answers and produced **zero
   payload ERRORs**. The Unit 11 root-cause hypothesis is
   confirmed; the rewrite-cycle cost is eliminated structurally
   rather than reactively.
2. **Flagged-expected retirement → 21/21 flagged-correct, zero
   flag realignments.** Every prior gated unit (2, 9, 10, 11)
   required at least one flag realignment because authored
   flagged-expected pairs never triggered the flag. Unit 12
   retired the slot entirely; the flag axis was clean on the
   first pass. The retirement decision is validated.
3. **Compounding authoring discipline → cleanest gate in the
   path.** Initial per-criterion agreement by unit: Unit 9 85%,
   Unit 10 87%, Unit 11 87%, **Unit 12 95%**. Realignment count:
   Unit 9 seven + one rewrite, Unit 10 four, Unit 11 four + two
   rewrites, **Unit 12 three + zero rewrites**. The discipline
   accumulated across gate docs is measurably paying down.

---

## Initial run evidence (2026-05-17)

21 pairs through the live grader (Anthropic Claude Sonnet 4.6,
prompt caching on rubric per STRATEGY.md § T2-E), run via the
`.env`-loaded environment from PR #115 (no shell exports).

```
Pairs scored:               21
Errored (no score):         0
Fully passed (all crit + flagged):  18 (85%)
Per-criterion agreement:    60/63 (95%)
Flagged-correct:            21/21

Token usage (cost-relevant):
  input tokens:        12574
  cache reads:         71920
  output tokens:       11557
```

Cost ≈ $0.22 total (~$0.011/call).

### Disagreements identified

Three pairs FAILed crit=2/3 — **all deliberate partial-credit
borderlines** (p008, p011, p021). No all-met, all-missed,
single-criterion, or off-topic/boundary-probe pair disagreed; the
two Unit 14/15 boundary probes (p003, p013) graded clean 0/3 and
unflagged, confirming the grader treats agents/safety and
transport-protocol concerns as off-topic for this unit's *how*
scope. Diagnostic (`backend/scripts/_inspect_pairs.py`, throwaway
from `claude/unit-12-gate-diagnostics`) was run on the three
borderlines (~$0.03).

| Pair | Disagreement | Grader's reading | Decision |
|---|---|---|---|
| **p008** | c3 T→F (0.75) | Gestures vaguely at the PM error (*"reaching for the forgiving default because it is less upfront work"*) instead of naming the specific skipping-recovery / SDK-default failure c3 requires | **REALIGN c3 → False** (becomes c2-only) |
| **p011** | c3 T→F (0.85) | Explicitly dismisses recovery as *"an implementation detail the framework handles"* — commits the recovery-skip error c3 warns against rather than naming it | **REALIGN c3 → False** (becomes c1-only) |
| **p021** | c3 T→F (0.80) | Maps the combination but never explains why stakes drive the choice, doesn't contrast surfaces, omits the specific PM-error point | **REALIGN c3 → False** (becomes c1-only) |

**Three realignments, all c3, all the same direction and
rationale. Zero rewrites. Zero preserved disagreements.** p008's
0.75 confidence is in the sub-0.8 stochasticity zone, but the
identical rationale at 0.85 and 0.80 on p011/p021 makes this a
consistent *pattern*, not noise — the same realign-over-preserve
reasoning applied to Unit 11 p008.

---

## Findings

### c3-strict is now a four-unit pattern — escalating it

The Unit 12 c3 realignments have the identical root cause as Unit
9 c2-strict, Unit 10 c2-strict, and Unit 11 c3-strict: **a rubric
criterion with a conjunctive `AND` clause, authored as if the
first conjunct alone satisfied it.** c3 requires *map the matched
combination by stakes* **AND** *name the specific PM error
(skipping the recovery decision because it has no happy-path demo
/ inheriting SDK defaults)*. Pairs that do the first and gesture
vaguely at — or outright commit — the second do not meet c3. The
grader is correct each time; the authoring drifts the same way
each time.

This has now cost realignments in **four of the last four gated
units**. It is no longer a someday-note. **Escalation: the
bundle-1 rubric pass must split every `AND`-clause criterion into
separately-scored sub-criteria**, and until that pass lands, the
authoring checklist gains a live item:

> When authoring a pair intended to *meet* a criterion that
> contains "AND", verify the answer satisfies **every** conjunct
> explicitly, not just the first. When authoring a *partial*
> pair, do not assume satisfying one conjunct earns the
> criterion.

Recorded here and in the regression YAML header; the concrete
rubric-splitting work is tracked for the bundle-1 iteration.

### The constraints work better as structure than as reaction

Units 9 and 11 discovered the markdown-header and
parenthetical-option-list payload triggers *reactively* — via
ERRORs, diagnostics, and rewrite cycles. Unit 12 encoded them as
*authoring constraints up front* and incurred zero payload
ERRORs. The lesson generalizes: gate findings that are structural
(answer-text shape, distribution shape) should become
pre-authoring constraints in the next unit's slot (d), not be
re-discovered per unit. The flagged-expected retirement is the
same move on the distribution axis.

---

## Decision-gate questions

From `docs/EXECUTION.md`:

> *Is the grader trustworthy enough to ship under P2?*
> *Is the per-call cost sustainable at projected scale?*
> *Does the user-facing experience honor the locked Loop arc?*

### 1. Trustworthy enough to ship under P2?

**Yes.** 95% initial, projected 100% post-realignment. Zero
payload ERRORs (the constraint held), 21/21 flagged-correct (the
retirement held). All three realignments defensible per the
rubric's literal text and cross-pair-consistent.

### 2. Per-call cost sustainable at projected scale?

**Yes.** ~$0.011/call, unchanged across Units 1–12.

### 3. UX honors the Loop arc?

**Yes.** No UX change for Unit 12 beyond the unit content; the
decision-prompt UI handles all units uniformly.

---

## What this unlocks

Unit 12 is the twelfth unit in the canonical *"LLM Systems for
PMs"* path. **12 of 20 units published.**

**Unit 13 (Multimodal / vision basics)** is locked
(`docs/curriculum/v1-path-outline.md`, PR #122). Per the
one-unit-ahead lock buffer, **Unit 14 (Agents / multi-step
reasoning) must be locked before Unit 13 authoring begins** — and
Unit 13's regression set is authored under the now-cumulative
constraints: no flagged-expected, no parenthetical option-lists /
markdown headers / quote-led sentences, plus the new
AND-clause-criterion authoring check.

---

## References

- Regression set: `content/regression-sets/tool-use-bundle-0.yml`
  (this PR: p008/p011/p021 c3 realignments).
- Unit: `content/units/tool-use-bundle-0.md` (published on
  author; no flip in this PR).
- Grader: `backend/app/ai_service.py` (no payload ERRORs this
  run — the no-parenthetical constraint held).
- Runner: `backend/scripts/run_regression_set.py`.
- Diagnostic (throwaway): `backend/scripts/_inspect_pairs.py` on
  `claude/unit-12-gate-diagnostics` — delete after this PR
  merges.
- Authoring PR: #123.
