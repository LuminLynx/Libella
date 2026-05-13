# v1 Path Outline — "LLM Systems for PMs"

> **Scope.** Phase 3 sequencing for the canonical Phase 1 path
> (`llm-systems-for-pms`). Tracks units 1–8 as shipped, locks
> unit 9 with title, trade-off, prereqs, and position rationale,
> sketches unit 10 thematically without committing, and leaves
> 11–20 in their original locked/sketched/placeholder states.
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
enough to ship to users?*" Units 7 and 8 are shipped; Unit 9 is
locked; Unit 10 remains sketched.

| # | Unit | Status | Trade-off it teaches | Prereqs |
|---|---|---|---|---|
| 7 | **Hallucination + reliability** | ✅ | Detection vs. mitigation vs. containment for the structural base-rate problem | 4 |
| 8 | **Cost dynamics at scale** | ✅ | Caching vs. batching vs. capacity — the "second cost conversation" multi-lever optimization | 1, 5 |
| 9 | **Fine-tuning vs. prompting vs. RAG** | 🔒 | The customization trilemma: which approach matches which kind of quality problem (knowledge gap → RAG; behavior shift → fine-tuning; spec clarity → prompting) | 4, 5, 6 |
| 10 | Vector search / RAG fundamentals | 🟡 | Most-asked-about pattern after the foundations are in place |

### Position rationale (Units 7–9)

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

### Open questions remaining for Unit 10

- Is RAG one unit (10) or two (vector-search-as-tool + RAG-as-pattern)?
  Authoring will resolve.

---

## Production (Units 11–15) — sketched

"*Can we operate this in front of real users?*"

| # | Unit (working title) | Why it's in this phase |
|---|---|---|
| 11 | Streaming UX | The TT2 UX flag in STRATEGY.md; deep-dive at this point makes more sense than as a one-liner in Latency |
| 12 | Tool use / function calling | The shift from "answer-shaped output" to "the LLM can call your API" |
| 13 | Multimodal (vision basics) | Image-input use cases at PM level |
| 14 | Agents / multi-step reasoning | When chain-of-thought helps vs. when it's expensive theater |
| 15 | Safety + content moderation | What stops a feature from getting your team in trouble |

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

1. **Author Unit 10 (RAG fundamentals) next.** No detour into
   Unit 12 or Unit 15 because something else is "more interesting."
   Lock Unit 10 in this file before slot (a) begins.
2. **Re-read this file at every phase boundary.** What looked obvious
   at outline time may not survive authoring. Document the change.
3. **Lock the next unit before authoring slot (a).** The discipline
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
