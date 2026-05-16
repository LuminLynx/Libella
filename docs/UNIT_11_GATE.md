# Unit 11 Gate — Streaming UX

> **Per-unit gate audit** for `streaming-ux-bundle-0` (Phase 3,
> Unit 11). Mirrors the structure of `docs/UNIT_10_GATE.md` and
> `docs/UNIT_9_GATE.md` — per-unit gates ship as recurring,
> independently reviewable artifacts.

---

## Decision

**Unit 11 PASSED 2026-05-16 — effective 100% (63/63).** Initial
run hit 87% (55/63); the realignment re-run hit 95% (60/63) with
every realigned pair (p007, p008, p011, p021) PASSing 3/3; the
sole remaining blemish, p014's deterministic payload ERROR, was
fixed by the p014 rewrite (PR #118) and confirmed clean on a
targeted isolated re-verify (c1=met/c2=not-met/c3=not-met, not
flagged, no payload error). Every one of the 21 pairs now grades
in agreement with the YAML. The unit was published on author
(PR #114, `status: published`); no flip required. **Zero
preserved disagreements**, continuing the Unit 9/10 precedent.

Two answer rewrites (p007, p014 — both deterministic
grader-payload bugs from parenthetical option-lists) plus three
c-realignments (p008 c3, p011 c1+c3, p021 c3) and one flag
realignment (p014). One stochastic ERROR documented without
change (p011's full-run payload error; graded clean in
isolation). **Zero preserved disagreements**, continuing the
Unit 9/10 precedent.

| Criterion | Required | Initial run (21 pairs) | Realignment re-run | Post-p014-rewrite | Verdict |
|---|---|---|---|---|---|
| Per-criterion agreement | ≥ 80% | 87% (55/63), 2 ERRORs | 95% (60/63), 1 ERROR (p014) | **100% (63/63), 0 ERRORs** | ✅ |
| Honest flagged behavior | spec-faithful | 18/21 — p014 experiment caught | 20/21 | 21/21 | ✅ |
| Cost / call | reasonable | ~$0.011/call | ~$0.011/call | ~$0.011/call | ✅ |

### p014 re-verify evidence (2026-05-16, post-rewrite)

Isolated grader pass on the rewritten p014, confirming the
deterministic parenthetical-option-list trigger is cleared:

```
position 1: met=True  (expected True)  [✓] confidence=0.72
position 2: met=False (expected False) [✓] confidence=0.85
position 3: met=False (expected False) [✓] confidence=0.90
flagged=False (expected False)
```

Clean grade, no payload ERROR. c1's 0.72 confidence sits in the
sub-0.8 stochasticity zone but *agrees* with the YAML — a correct
grade at modest confidence, not a disagreement. With p014 now
grading, all 21 pairs agree: 63/63 effective. The p007 rewrite
precedent (same fix, same outcome) holds for p014.

---

## Headline finding: the flagged-expected experiment (p014)

Units 2, 9, and 10 all showed the same pattern: verbose-*punt*
answers authored as flagged-expected get **confidently graded
all-not-met** — the grader has enough signal to resolve them, so
it never flags. `docs/UNIT_10_GATE.md` left an open question:
*would a genuine one-criterion ambiguity (not a punt) trigger the
flag?* and flagged it for a decision after two more occurrences.

**Unit 11 ran the experiment.** p014 was deliberately authored with
a real partial-credit shape — c1 clearly met, c3 clearly missed,
and **c2 a true coin-flip** (the answer says the decisions "affect
each other" without explaining *how* one constrains another —
genuinely gradeable either way). The hypothesis: this should
produce grader uncertainty and a flag.

**Result: it did not.** The grader matched all three criteria
exactly as designed (c1 met, c2 *false*, c3 missed) and **did not
flag** — it confidently resolved the coin-flip c2 rather than
expressing uncertainty.

**Conclusion — hypothesis refuted.** Genuine one-criterion
ambiguity does not trigger the flag any more than a verbose punt
does. Under the current grader prompt and rubric, the flag
mechanism effectively does not fire on real answers regardless of
shape. This is now observed across four units (2, 9, 10, 11) and
two answer archetypes (punt, ambiguous).

**Decision: retire flagged-expected from the authoring
distribution from Unit 12 onward.** Continuing to author
flagged-expected pairs manufactures guaranteed realignments every
gate. The flag is not a property of answer content the grader
surfaces; if flag behavior is wanted in future it is a
grader-prompt feature to design (T2-B/T2-D), not a regression-set
authoring target. This supersedes the open "lock partial-credit
shape" question in `UNIT_10_GATE.md`.

---

## Initial run evidence (2026-05-16)

21 pairs through the live grader (Anthropic Claude Sonnet 4.6,
prompt caching on rubric per STRATEGY.md § T2-E). First run after
the env-durability fix (PR #115 — gitignored `.env` + dotenv
loader; the prior multi-attempt failure was a missing
`AI_PROVIDER_API_KEY`, not content).

```
Pairs scored:               21
Errored (no score):         2
Fully passed (all crit + flagged):  16 (76%)
Per-criterion agreement:    55/63 (87%)
Flagged-correct:            18/21

Token usage (cost-relevant):
  input tokens:        11693
  cache reads:         73720
  output tokens:       10530
```

Cost ≈ $0.22 total (~$0.011/call). 87% initial agreement matches
Unit 10's initial run; same healthy starting point.

### Disagreements identified

Diagnostic (`backend/scripts/_inspect_pairs.py`, throwaway from
`claude/unit-11-gate-diagnostics`) was run on p007, p008, p011,
p021 to pull per-criterion detail (~$0.04).

| Pair | Disagreement | Grader's reading | Decision |
|---|---|---|---|
| **p007** | ERROR — payload missing 'grades'/'flagged' | **Deterministic** — reproduced in isolation, like Unit 9 p016. Original had a quote-led sentence + parenthetical triple; no markdown headers, so a different trigger than p016 | **REWRITE** answer to plain flowing prose, c1=T/c2=F/c3=T profile preserved; re-verify on re-run |
| **p011** | ERROR (full run only) | **Stochastic** — graded clean in isolation (like Unit 10 p005). No content bug | **DOCUMENT** the stochastic error, no rewrite |
| **p011** | c1 T→F (0.9), c3 T→F (0.82) | Answer explicitly decouples recovery (*"mostly takes care of itself"*, *"implementation detail the framework handles"*) — fails the coupled-c1 bar and the full-triple-c3 bar | **REALIGN c1+c3 → False** (becomes all-not-met) |
| **p008** | c3 T→F (0.75) | Matched-combination map omits the explicit recovery leg and the "teams skip recovery" PM-error callout — both required by c3. Confidence in the stochasticity zone but reasoning identical to p011/p021 | **REALIGN c3 → False** (becomes c2-only) |
| **p021** | c3 T→F (0.82) | Dismisses recovery as *"falls out of the rendering choice … for free"* — commits the exact error c3 warns against | **REALIGN c3 → False** (becomes c1-only) |
| **p014** | flag T→F, then ERROR on re-run | The experiment — grader confidently resolved c2, did not flag (first run). On the realignment re-run + isolation it then errored **deterministically** with the payload bug (like p007, not stochastic like p011) | **REALIGN flag → False** (experiment captured from first-run data) **+ REWRITE** answer (remove parenthetical option-lists) |

**Two rewrites (p007, p014) + four realignments. Zero preserved
disagreements.** The realignment re-run (2026-05-16) scored 60/63
(95%) with every realigned pair PASSing 3/3; the only non-pass was
p014's then-undiscovered deterministic payload ERROR, fixed by the
rewrite documented here.

### Grader-payload trigger: parenthetical option-lists

p007 and p014 both errored **deterministically**, and both
contained **parenthetical option-lists** in the answer text —
e.g. *"what to stream (tokens, semantic chunks, or status), how
to render the streamed state (continuous flow, progressive
sections, or typed-out), …"*. p007 errored 2×, was rewritten
without the parenthetical, and passed. p014 graded once (lucky),
then errored 2× (re-run + isolation); rewritten without the
parentheticals.

p006 has the same shape and passed both runs, so this is a
**propensity raiser, not a hard guarantee** — but two
deterministic failures from the same structure is enough to make
it an authoring rule. Combined with Unit 9's markdown-header
finding, the answer-text constraints are now: **no markdown-style
headers, no quote-led sentences, no parenthetical option-lists.**
The T2-D guardrail keeps all of these safe (rejected, never
mis-graded) — but each costs a rewrite/re-run cycle, so they are
authoring-time constraints, not runtime risks. This directly
shapes Unit 12+ regression authoring.

### The c3-strict authoring lesson

p008, p011, and p021 disagreed on c3 in the *same direction with
the same reasoning*: c3's literal text requires the recovery leg
to be **explicitly named** in the matched triple AND the "teams
skip the recovery decision because it has no happy-path demo"
PM-error to be **called out**. Pairs that underweight or actively
dismiss recovery cannot meet c3 — the grader is correct, the
authored expected values were too lenient.

This is the same class as Unit 9's c2-strict and Unit 10's
c2-strict drift: a rubric clause with an `AND` that the authoring
treated as satisfied by the first conjunct alone. **Three
cross-pair-consistent realignments, not stochastic
cherry-picking** — the consistency (identical rationale at 0.75,
0.82, 0.82+0.9) is what justifies realign over preserve, even for
p008's sub-0.8 confidence.

**Authoring constraint going forward:** a pair intended to meet c3
must explicitly name all three legs of a coherent combination
(including the recovery leg by name) and explicitly flag the
recovery-skip error. "Maps a combination but underweights
recovery" is a c3=**False** shape, not partial credit.

### p007 — second deterministic grader-payload bug

p007 errored on the full run and **reproduced in isolation** —
deterministic, like Unit 9 p016 (whose T2-D guardrail correctly
rejected the malformed payload; no bad grade reached the DB). p007
has no markdown-style headers, so the trigger differs from p016.
Suspected triggers: a quote-led sentence (*"Stream it like chat"
is the classic PM error…*) and a parenthetical decision-triple.

**Fix:** rewrote the answer in plain flowing prose — no
quote-initial sentences, no parenthetical lists — preserving the
intended c1=T/c2=F/c3=T profile. Must grade cleanly on the re-run;
if it errors again the trigger hypothesis is wrong and we
re-investigate.

**Known authoring constraint reinforced:** beyond markdown headers
(Unit 9), avoid quote-led sentences and parenthetical option-lists
inside answer text until the grader-payload robustness issue is
understood. The T2-D guardrail keeps these safe (rejected, not
mis-graded), but they cost a realignment cycle.

---

## Findings

### Flagged-expected is retired (decision)

See the headline finding. Net effect on future authoring
distributions (Unit 12+): drop the flagged-expected slot
entirely; the 21-pair shape becomes all-met / partial-credit /
single-criterion / all-missed-on-topic / off-topic-gradable, with
no flagged-expected pair. `UNIT_10_GATE.md`'s open question is
resolved by this decision rather than deferred further.

### c3-strict joins the cross-unit "AND-clause leniency" pattern

Unit 9 c2-strict, Unit 10 c2-strict, Unit 11 c3-strict — three
units, same root cause: a rubric criterion with a conjunctive
requirement authored as if the first conjunct alone satisfied it.
Candidate for a bundle-1 rubric-tightening pass: criteria with
`AND` clauses should be split into separately-scored sub-criteria
so the authoring can't drift. Captured here, not as live YAML
disagreements.

### Env-durability fix validated under load

The first real Unit 11 gate run is also the first run under PR
#115's gitignored-`.env` + dotenv loader. Zero env-related
failures across 21 pairs confirms the fix holds; the long
debugging detour that preceded it was entirely a missing
`AI_PROVIDER_API_KEY` (plus a paste-newline and a `read` clobber),
not content. Standard gate invocation going forward is simply
`python -m backend.scripts.run_regression_set …` with `.env`
present — no shell exports.

---

## Decision-gate questions

From `docs/EXECUTION.md`:

> *Is the grader trustworthy enough to ship under P2?*
> *Is the per-call cost sustainable at projected scale?*
> *Does the user-facing experience honor the locked Loop arc?*

### 1. Trustworthy enough to ship under P2?

**Yes.** 87% initial, projected ≥95% post-realignment. All four
realignments defensible per the rubric's literal text; the
c3-strict trio is cross-pair-consistent. Both payload ERRORs were
caught by the T2-D guardrail (no mis-graded data); one
deterministic (rewritten), one stochastic (documented). The p014
experiment behaved informatively — the grader is *consistent*,
just not flag-prone.

### 2. Per-call cost sustainable at projected scale?

**Yes.** ~$0.011/call, unchanged across Units 1–11.

### 3. UX honors the Loop arc?

**Yes.** No UX change for Unit 11 beyond the unit content; the
decision-prompt UI handles all units uniformly.

---

## What this unlocks

Unit 11 is the eleventh unit in the canonical *"LLM Systems for
PMs"* path. **11 of 20 units published.**

**Unit 12 (Tool use / function calling)** is locked
(`docs/curriculum/v1-path-outline.md`, PR #113). Per the
one-unit-ahead lock buffer, **Unit 13 (Multimodal) must be locked
before Unit 12 authoring begins** — and Unit 12's regression set
is authored **without a flagged-expected pair** per this gate's
decision.

---

## References

- Regression set: `content/regression-sets/streaming-ux-bundle-0.yml`
  (this PR: p007 rewrite + p008/p011/p021/p014 realignments).
- Unit: `content/units/streaming-ux-bundle-0.md` (published on
  author; no flip in this PR).
- Grader: `backend/app/ai_service.py` (T2-D guardrails rejected
  both malformed payloads correctly).
- Runner: `backend/scripts/run_regression_set.py`.
- Diagnostic (throwaway): `backend/scripts/_inspect_pairs.py` on
  `claude/unit-11-gate-diagnostics` — delete after this PR merges.
- Authoring PR: #114. Env-durability PR: #115.
