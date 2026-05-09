# v1 Path Outline — "LLM Systems for PMs"

> **Scope.** Phase 3 sequencing for the canonical Phase 1 path
> (`llm-systems-for-pms`). Locks units 1–6 with title, trade-off,
> prereqs, and position rationale; sketches units 7–15 thematically
> without committing; leaves 16–20 explicitly placeholder.
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

## Foundations (Units 1–6) — locked

These six form the front-half arc: a PM finishing Unit 6 can scope
an LLM-backed feature responsibly. Each answers "*can we ship this?*"
from a different angle, building toward the synthesis decision.

| # | Unit | Status | Trade-off it teaches | Prereqs |
|---|---|---|---|---|
| 1 | **Tokenization** | ✅ | Right unit of cost vs. wrong one ($/word) | (none) |
| 2 | **Context Window** | 🔒 | Fits more vs. costs more vs. degrades over long context | 1 |
| 3 | **Latency** | 🔒 | Fast small model vs. slow big model vs. streaming for perceived speed | 1 |
| 4 | **Evals** | 🔒 | Human eval vs. LLM-as-judge vs. golden-set vs. live A/B | 1, 2 |
| 5 | **Model selection** | 🔒 | Capability vs. cost vs. latency triangle (Sonnet vs. Haiku vs. Opus, etc.) | 1, 2, 3, 4 |
| 6 | **Prompt design basics** | 🔒 | System vs. user, instruction-following, few-shot vs. zero-shot | 5 |

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

## Productization (Units 7–10) — sketched

After Foundations, the path branches into "*can we make this reliable
enough to ship to users?*" Locks at the start of each unit's
authoring, not now.

| # | Unit (working title) | Why it's in this phase |
|---|---|---|
| 7 | Hallucination + reliability | The single biggest PM-AI surprise; costs trust if mishandled |
| 8 | Cost dynamics at scale | Caching, rate limits, batch APIs — the "second cost conversation" once volume kicks in |
| 9 | Fine-tuning vs. prompting vs. RAG | The customization trilemma; appears in every "should we improve quality?" PM meeting |
| 10 | RAG / vector search fundamentals | Most-asked-about pattern after the foundations are in place |

Open questions for this phase (lock when authoring begins):

- Should hallucination be Unit 7 or be folded into Evals (Unit 4) as
  the failure mode being measured? Authoring Unit 4 will resolve.
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

1. **Author Unit 2 (Context Window) next.** No detour into Unit 5
   or Unit 12 because something else is "more interesting."
2. **Lock Units 7–8 before starting Unit 6**, so Unit 6's depth
   section doesn't accidentally cannibalize them.
3. **Re-read this file at every phase boundary.** What looked obvious
   at outline time may not survive authoring. Document the change.

The point of this file is not to be right about all 20 units. It's
to be honest about what's locked vs what's open, so authoring
proceeds with confidence in the locked part and humility about the
rest.
