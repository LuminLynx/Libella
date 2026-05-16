# v1 Path Outline — "LLM Systems for PMs"

> **Scope.** Phase 3 sequencing for the canonical Phase 1 path
> (`llm-systems-for-pms`). Tracks units 1–8 as shipped, locks
> units 9, 10, and 11 with title, trade-off, prereqs, and
> position rationale, and leaves 12–20 in their original
> sketched/placeholder states.
>
> **Relationship to other docs.** STRATEGY.md says "20 units, well-built."
> This file says *which* 20, in *which order*. EXECUTION.md says
> Phase 3 is the authoring phase; this file is the input that keeps
> Phase 3 from authoring units that don't fit. AUDIT.md is the
> source/inventory triage for inherited code, not relevant here.

---

## How this file is maintained

**Update when:**

- A unit ships and its position is now empirical rather than planned (move from "locked" to "shipped" + retain in the table).
- Authoring a locked unit reveals that the next 1–2 should be re-ordered or replaced (lock the next unit, leave the rest flexible).
- A planned unit gets cut (mid-Phase-3 scope reduction) — record the cut + rationale.
- Real-user signal from the closed beta forces a curriculum revision (Phase 4+).

**Do NOT update when:**

- A unit's content changes (that lives in `content/units/<id>.md`).
- Rubric criteria are tweaked (that lives in the unit's front matter).
- Regression-set behavior shifts (that lives in `content/regression-sets/`).

This file is for *which units exist and in what order* — not how
they're authored.

**Authoring discipline.** Don't author Unit N until Unit N is locked
in this file. If you find yourself wanting to author Unit N out of
sequence, that's a signal the outline needs updating first.

---

## Status legend

- ✅ **Shipped** — unit exists in `content/units/`, ingested, regression set passes the gate.
- 🔒 **Locked** — title, trade-off, and position are committed; can author against this.
- 🟡 **Sketched** — thematic placeholder; specifics not committed.
- 🟧 **Placeholder** — exists only to hold a slot count; expect significant revision.

---

## Foundations (Units 1–6) — shipped

These six form the front-half arc: a PM finishing Unit 6 can scope
an LLM-backed feature responsibly. Each answers "*can we ship this?*"
from a different angle, building toward the synthesis decision.

| # | Unit | Status | Trade-off it teaches | Prereqs |
|---|---|---|---|---|
| 1 | **Tokenization** | ✅ | Right unit of cost vs. wrong one ($/word) | (none) |
| 2 | **Context Window** | ✅ | Fits more vs. costs more vs. degrades over long context | 1 |
| 3 | **Latency** | ✅ | Fast small model vs. slow big model vs. streaming for perceived speed | 1 |
| 4 | **Evals** | ✅ | Human eval vs. LLM-as-judge vs. golden-set vs. live A/B | 1, 2 |
| 5 | **Model selection** | ✅ | Capability vs. cost vs. latency triangle (Sonnet vs. Haiku vs. Opus, etc.) | 1, 2, 3, 4 |
| 6 | **Prompt design basics** | ✅ | System vs. user, instruction-following, few-shot vs. zero-shot | 5 |

### Position rationale

- **Tokenization first** because every other cost / capacity / latency
  conversation downstream assumes the unit-of-account. A PM who
  doesn't know what a token is can't reason about anything else
  honestly. (Already authored; gate-passed.)

- **Context Window as Unit 2** is the direct successor — tokens are
  what fill the context, and "200k tokens" is the most common PM
  mis-intuition after $/word. Together with Tokenization this gives
  the basic *cost-and-capacity* mental model. The depth section of
  Unit 1 already teases this.

- **Latency as Unit 3** introduces the *speed* axis. PMs scope
  features by feel of "snappy / acceptable / slow." Streaming UX is
  the load-bearing concept that's surprisingly absent from most PM
  AI literacy.

- **Evals as Unit 4 (before Model selection)** because the discipline
  of "before you choose, know how you'll measure" is exactly what
  PMs need most. Vendor bake-offs are real PM work, and they
  require eval thinking first. Putting Evals after Model selection
  would teach the wrong order of operations.

- **Model selection as Unit 5** synthesizes 1–4. By this point the
  learner can hold the capability / cost / latency triangle in
  their head and weigh trade-offs against an evaluation strategy.
  This is the first unit that's substantively a *decision* in the
  trade-off-framing sense, and it's the right place for that depth.

- **Prompt design as Unit 6** comes after model selection because
  prompts are model-specific in subtle ways (Claude responds to
  XML tags well, GPT prefers JSON-shaped instructions, Gemini has
  different defaults). Teaching prompt design before the learner
  has chosen a model is teaching it in the wrong order.

---

## Productization (Units 7–10) — partially shipped

After Foundations, the path branches into "*can we make this reliable
enough to ship to users?*" Units 7 and 8 are shipped; Units 9 and 10
are locked.

| # | Unit | Status | Trade-off it teaches | Prereqs |
|---|---|---|---|---|
| 7 | **Hallucination + reliability** | ✅ | Detection vs. mitigation vs. containment for the structural base-rate problem | 4 |
| 8 | **Cost dynamics at scale** | ✅ | Caching vs. batching vs. capacity — the "second cost conversation" multi-lever optimization | 1, 5 |
| 9 | **Fine-tuning vs. prompting vs. RAG** | 🔒 | The customization trilemma: which approach matches which kind of quality problem (knowledge gap → RAG; behavior shift → fine-tuning; spec clarity → prompting) | 4, 5, 6 |
| 10 | **Vector search / RAG fundamentals** | 🔒 | Recall (did retrieval find the right chunks?) vs. groundedness (did the model use them?) vs. citation faithfulness (does the cited source actually say what's claimed?) — three different RAG quality dimensions, three different failure modes, three different measurement disciplines | 1, 2, 4, 9 |

### Position rationale (Units 7–10)

- **Hallucination as Unit 7** opens the productization block because
  it's the single biggest PM-AI surprise — every team that ships an
  LLM feature hits hallucination, and the right frame ("base rate to
  manage, not bug to fix") needs to land before later units assume
  it. The c1 reframe is the unit's load-bearing pedagogy. Resolved
  the earlier open question by authoring it as a standalone unit
  rather than folding it into Unit 4 (Evals) — the design-around
  framing is too rich for a sub-section.

- **Cost dynamics at Unit 8** is the "second cost conversation"
  the path-outline note from Unit 5 promised. The synthesis block
  (Units 1–5) ends with annualized math at projected volume; Unit 8
  adds the production levers (caching, batching, committed-use) that
  PMs reach for when finance starts asking about the line item. It
  comes after Unit 7 because reliability is the first production
  concern; cost is the second.

- **Fine-tuning vs. prompting vs. RAG as Unit 9** introduces the
  customization trilemma after the learner already knows what
  prompting is (Unit 6) and how to evaluate whether a customization
  worked (Unit 4). The unit's core pedagogy is that the three
  approaches are not interchangeable — each fixes a different
  *kind* of quality problem — and a PM who picks by "what eng knows
  best" instead of by failure-mode-diagnostic misses the trilemma.
  This unit sits before Unit 10 (RAG fundamentals) because the
  trilemma framing has to be established before RAG depth makes
  sense; otherwise Unit 10 reads as "RAG is the answer to quality
  problems" instead of "RAG is the answer to *knowledge-gap*
  problems."

- **Vector search / RAG fundamentals as Unit 10** closes the
  Productization block by going deep on the pattern Unit 9 named
  as the right tool for knowledge-gap problems. The unit's
  load-bearing pedagogy is that RAG quality is not a single
  thing — it splits into three measurable dimensions (retrieval
  recall, model groundedness, citation faithfulness), each with
  different failure modes and different eval methods. A PM
  scoping a RAG feature without these three dimensions tends to
  ship a system where *"the retrieval looks fine"* but the model
  ignores the chunks, or the citations point at the wrong
  document, or both. The unit sits after Unit 9 because Unit 9
  frames RAG as one approach among three; Unit 10 takes *"RAG is
  the answer"* as given and teaches what scoping a RAG system
  actually entails. It closes Productization because the next
  block (Units 11–15, Production) covers more specialized
  patterns that often compose with RAG (streaming UX, tool use,
  multimodal) — RAG is the workhorse foundation.

  **Resolved at lock (2026-05-14): RAG ships as one unit, not
  two.** The earlier open question of *"one unit or two
  (vector-search-as-tool + RAG-as-pattern)?"* resolves toward
  one unit. Vector search is a mechanism inside RAG, not a
  separate PM-decision; teaching them as one unit keeps the
  measurement-led framing intact (recall is what vector search
  produces; groundedness and citation faithfulness are downstream
  of it). A future bundle-1 iteration could split if real PM
  decisions diverge, but for v1 one unit is enough.

---

## Production (Units 11–15) — partially locked

"*Can we operate this in front of real users?*" Units 11 and 12
are locked; Units 13–15 remain sketched.

| # | Unit | Status | Trade-off it teaches | Prereqs |
|---|---|---|---|---|
| 11 | **Streaming UX** | 🔒 | What to stream (raw tokens / semantic chunks / status only) vs. how to render the streaming state (continuous flow / progressive sections / typed-out) vs. how to handle mid-stream failure (silent retry / partial accept / full restart) — three coupled decisions whose pairings produce three different user feels; mismatched pairings produce a UI the user reads as broken | 1, 3 |
| 12 | **Tool use / function calling** | 🔒 | Tool granularity (many narrow tools / few broad composites) vs. schema strictness (strict validation / lenient acceptance) vs. error-recovery locus (model retries on tool-error / orchestrator catches and re-prompts) — three coupled decisions that determine whether a tool-using LLM feature feels reliable, capable, and debuggable, or feels brittle, narrow, and opaque to ship and operate | 1, 4, 6, 10 |
| 13 | Multimodal (vision basics) | 🟡 | Image-input use cases at PM level | — |
| 14 | Agents / multi-step reasoning | 🟡 | When chain-of-thought helps vs. when it's expensive theater | — |
| 15 | Safety + content moderation | 🟡 | What stops a feature from getting your team in trouble | — |

### Position rationale (Unit 11)

- **Streaming UX as Unit 11** picks up the streaming axis from
  Unit 3 (Latency) — where it was named as one of three latency
  levers — and goes deep on what shipping it actually entails.
  Unit 3 framed streaming as a *choice* (stream on chat-shaped
  surfaces, don't on atomic-output ones); Unit 11 frames it as a
  *discipline*. The unit's load-bearing pedagogy is that *"stream
  the output"* isn't a single decision but a three-decision
  trilemma — what to stream, how to render it, how to recover
  from mid-stream failure. Each pairing produces a different
  feel; mismatched pairings produce a janky UI that the user
  reads as broken. STRATEGY.md § TT2 explicitly flags streaming
  the rationale as load-bearing for the grader's UX
  (*"User sees a useful loading state, not a bare spinner"*);
  this unit is the deep-dive that flag promises. Sits as Unit 11
  because it's the first Production-block unit and the most
  user-visible — every PM shipping an LLM feature with
  non-trivial output length faces this decision, and faces it
  before more specialized patterns (Tool use, Multimodal,
  Agents, Safety).

### Position rationale (Unit 12)

- **Tool use as Unit 12** is the second Production-block unit and
  the structural prerequisite for the three that follow.
  Multimodal pipelines (Unit 13) invoke tools to route image
  inputs; Agents (Unit 14) *are* tool-use orchestration loops —
  the agentic pattern is unintelligible without tool-call
  mechanics first; Safety (Unit 15) governs the action surface
  that tool use creates (a model that can only emit text has a
  much smaller blast radius than one that can call your API).
  Putting Tool use early in the block makes the rest of the
  block legible.
- **It generalizes Unit 10's retrieval-as-a-tool framing.** Unit
  10 (Vector search / RAG) taught retrieval as the canonical
  knowledge-injection pattern; Unit 12 reframes retrieval as one
  instance of the broader *"the LLM can call your API"* pattern
  and teaches the design trade-offs that apply to every tool,
  not just retrieval. This is why Unit 10 is a prereq, not just
  Unit 1.
- **The load-bearing pedagogy is that "give the model tools"
  isn't one decision but three coupled ones** — granularity,
  schema strictness, and error-recovery locus. As with the
  Streaming UX trilemma (Unit 11), the pairings interact:
  lenient schemas with model-side recovery can self-heal but
  hide failures from the orchestrator; strict schemas with
  orchestrator-side recovery are debuggable but brittle to
  novel inputs. Mismatched pairings produce a feature that
  *demos well and operates badly*.
- **Prereqs 1, 4, 6, 10.** Unit 1 (what a tool call is at the
  API boundary), Unit 6 (tool descriptions *are* prompts — same
  discipline), Unit 10 (retrieval-as-tool, the pattern this
  generalizes), and Unit 4 (Evals — the trade-off is only
  resolvable empirically; "which granularity is right?" is an
  eval question, and tool use without an eval discipline is
  exactly the failure mode the curriculum exists to prevent).
  Consistent with Units 9 and 10, which both list Evals as a
  prereq.

---

## Operating (Units 16–20) — placeholder

Locked when the closed-beta cohort surfaces what real users actually
struggle with. Don't pretend to know which 5 units these will be.

| # | Slot | Working hypothesis |
|---|---|---|
| 16 | 🟧 | Live monitoring + drift detection in production |
| 17 | 🟧 | Vendor risk / multi-provider / what-if-Anthropic-doubles-prices |
| 18 | 🟧 | A/B testing LLM features |
| 19 | 🟧 | Fallback strategies / graceful degradation |
| 20 | 🟧 | Reserved — to be determined by what authoring units 1–19 teaches us |

Honest note: by the time we're authoring Units 16–20, the front
half will have shipped and beta users will be telling us what's
missing. Locking Unit 20 today would be design-from-the-armchair.
The placeholder slot is intentional.

---

## Cuts: explicit non-units

Things a v1 PM curriculum could plausibly include but **deliberately
won't**, because they don't earn their slot in 20:

- **History of language models / transformer internals** — too
  academic; PMs don't need to know what attention is mathematically.
- **Embedding model internals** — Unit 10 (RAG) covers what's
  PM-relevant; the model architecture is out of scope.
- **MLOps / model serving infrastructure** — those are eng concerns,
  not PM-decision concerns.
- **Image generation** — narrower use case; cut for v1.
- **Voice / audio** — same.
- **Open-source model deployment** — too operationally specialized
  for v1.

Revisit after closed beta.

---

## What this file commits us to

1. **Author Unit 11 (Streaming UX) next.** Unit 10 (RAG
   fundamentals) shipped and passed gate 2026-05-15 (see
   docs/UNIT_10_GATE.md — 100% per-criterion re-run). No detour
   into Unit 13 or Unit 15 because something else is "more
   interesting." Unit 12 (Tool use / function calling) is locked
   2026-05-16, satisfying the one-unit-ahead lock buffer for
   Unit 11.
2. **Lock Unit 13 (Multimodal / vision basics) before Unit 12
   authoring begins.** Maintain the one-unit-ahead lock buffer
   so the prereq chain is always settled before slot (a) begins
   on the active unit.
3. **Re-read this file at every phase boundary.** What looked obvious
   at outline time may not survive authoring. Document the change.
4. **Lock each next unit before authoring slot (a).** The discipline
   below (Process retrospective) explains why this matters; the
   short version is that locking forces position rationale to be
   written *before* authoring, not derived after the fact.

The point of this file is not to be right about all 20 units. It's
to be honest about what's locked vs what's open, so authoring
proceeds with confidence in the locked part and humility about the
rest.

---

## Process retrospective (2026-05-13)

**Discipline violation.** The file's own discipline says *"Don't
author Unit N until Unit N is locked in this file."* In practice,
Units 7 (Hallucination) and 8 (Cost dynamics) were authored without
being moved from 🟡 Sketched to 🔒 Locked in this file first. Unit
9 (Fine-tuning vs. prompting vs. RAG) was caught at slot (a) draft
and paused until this retroactive lock could happen.

**What we lost by skipping the lock step.** The position rationale
for Units 7 and 8 was derived *after* authoring rather than written
*before* — there's a meaningful difference: the rationale-before
forces honest reasoning about why a unit comes where it does and
whether the prereq chain is right; the rationale-after risks
post-hoc justification of whatever shape the authoring took.

**Retroactive lock.** Units 7 and 8 are marked ✅ Shipped above
with the trade-off framings they actually authored against, and
the position-rationale section retroactively records why each
unit landed where it did. Honest disclosure: the rationale for
Units 7 and 8 was written *after* the unit content, not before,
and may smell like justification more than reasoning. For Unit 9
the rationale was written before slot (a) resumed, restoring the
discipline going forward.

**Going forward.** Unit 10 will be locked in this file (with
position rationale written first) before any slot-(a) work
begins. Same for Units 11+.

---

## Process retrospective — English-only authoring discipline (2026-05-14)

**The drift.** Units 1–8 each carry a non-English regression-set
pair — Tokenization (p017, Spanish), Units 2–8 (p016, Portuguese).
The pattern propagated: Tokenization's Spanish pair had legitimate
unit-content justification (the unit teaches that non-English text
costs more tokens), but the Context Window PR (Unit 2) imported a
Portuguese pair without that justification, and Units 3–8 carried
the precedent forward across the Phase 3 authoring sessions.

**The discipline (locked 2026-05-14).** **English-only is the
default for all authoring — units, regression sets, prompts,
examples, every slot.** A non-English addition requires explicit
founder approval per case, with a stated justification, not
inherited from precedent.

Why English-only is the right baseline:

- The product surface (Android app, curriculum, decision prompts)
  ships in English.
- Regression-set pairs never reach end users — they're internal
  grader-calibration test data — so non-English coverage has no
  user-facing audience reason.
- Grader multilingual capability is a property of the underlying
  model (Sonnet 4.6), not something regression-set coverage
  trains in.
- Non-English authoring is real effort (curated answer text,
  reviewer cognitive load, repo footprint) with no offsetting win.

**Forward-only correction.** Units 1–8 keep their non-English
pairs as shipped. The gates already passed; the pairs are
internal test data with no exposure path; cleaning up 8 published
units is overhead with no user-facing benefit. The discipline
applies from Unit 9 onward (Unit 9 ships English-only across all
21 regression pairs).

**For future units.** If a non-English pair seems valuable for a
specific unit (e.g., the unit's content concerns multilingual
handling — the way Tokenization does), require an explicit
founder approval at slot (d) authoring with the justification
recorded. Default = English-only; non-English = exception with
named rationale.

---

## Session operations discipline — PR auto-subscribe (2026-05-14)

**The rule.** Every PR a Claude Code session opens against this
repo gets an immediate `subscribe_pr_activity` call on it.
Subscription covers PR activity end-to-end: CI status changes,
review comments, reviews, merges, and any other webhook event
GitHub sends for the PR.

**Why.** PR-activity subscriptions are per-PR, not session-wide.
Without an explicit default, every PR opened in a session
requires asking the operator *"want me to subscribe?"* — overhead
that adds up when authoring runs through multiple PRs (a unit
authoring PR, a gate audit PR, a publish PR per cycle). Adopting
auto-subscribe as the default removes the ask and makes CI
failures and review comments reach the authoring session by
default, where they can be triaged in context.

**Handling discipline (carried from prior sessions).** Once
subscribed:

- **Confident + small fix:** apply directly, commit, push.
- **Ambiguous, architecturally significant, or a content/spec
  call:** ask the operator before acting.
- **Duplicate webhook (e.g., echo of a reply just posted, or a
  webhook for an event the session caused itself):** skip
  silently.
- **Deliberate-borderline disagreement (the regression-set
  pattern documented in `docs/UNIT_2_GATE.md` and successors):**
  reply to the comment explaining the rationale and pointing at
  the YAML header / gate-doc reference; no YAML change.

**Auto-unsubscribe behavior.** When a subscribed PR merges or
closes, GitHub's webhook flow automatically unsubscribes the
session. No manual cleanup needed.

**For new sessions.** A Claude Code session resuming this repo
should adopt this discipline before opening its first PR. The
rule is in this file (not in `STRATEGY.md` / `EXECUTION.md`
because it's a session-operations concern, not strategy or
phase sequencing) so the next authoring session reads it during
the standard onboarding sweep of curriculum docs.

---

## Process discipline — regression-set density (2026-05-14)

**Observation.** Every published unit (1–9) shipped with ≥ 20
regression pairs:

| Unit | Designation | Pairs shipped |
|---|---|---|
| 1 — Tokenization | flagship | 20 |
| 2 — Context window | flagship | 22 |
| 3 — Latency | standard | 22 |
| 4 — Evals | standard | 21 |
| 5 — Model selection | standard | 21 |
| 6 — Prompt design | standard | 21 |
| 7 — Hallucination | standard | 21 |
| 8 — Cost dynamics | standard | 21 |
| 9 — Customization trilemma | flagship | 21 |

`STRATEGY.md` § T2-C / § T1 Q5 sets the tier thresholds at
**≥ 20 flagship / ≥ 10 standard / never zero.** In practice the
standard-designated units all converged on flagship density — the
natural shape of the distribution buckets (all-three-met /
partial / single-criterion / all-missed-on-topic / off-topic-
gradable / flagged-expected) produces ~20 pairs organically once
the authoring discipline is applied.

**The discipline going forward.** Author every unit to ≥ 20
pairs by default, regardless of flagship/standard designation in
`STRATEGY.md`. Reasons:

1. Distribution buckets need that many pairs to be honestly
   represented (you can't usefully have 1.5 pairs in a bucket).
2. Deliberate borderlines (the Unit 2 p008/p020 pattern; Unit 9
   had four of them) need budget that 10 pairs can't afford.
3. Cost is bounded — ~$0.20 per gate run regardless of pair count
   (prompt caching dominates), so author-density doesn't move the
   cost curve.

**Not a STRATEGY revision.** The tier system in `STRATEGY.md` is
intact; the ≥ 10 standard floor still allows a smaller follow-on
unit to ship at 10–14 pairs if there's a specific reason (e.g., a
small appendix unit or a unit whose distribution buckets
genuinely don't have 20 honest pair shapes). This retrospective
documents the de-facto practice without overriding the spec
floor.

**For future authoring sessions.** Default = ≥ 20 pairs.
Deviations (10–19 pairs) need an explicit rationale at slot (d)
authoring time, recorded in the regression-set YAML header.
