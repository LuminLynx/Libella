---
id: context-window-bundle-0
slug: context-window
path_id: llm-systems-for-pms
position: 2
prereq_unit_ids:
  - tokenization-bundle-0
status: draft
definition: A context window is the budget of tokens a model can consider in a single call — the prompt sent and the output generated, both counted against the same per-call ceiling.
calibration_tags:
  - claim: "Headline context-window numbers are per-call ceilings that include prompt input, generated output, and (provider-specific) tool definitions and cached prefixes."
    tier: settled
  - claim: "Effective recall can degrade on long inputs, especially when the relevant content is buried mid-context."
    tier: settled
  - claim: "Per-call cost scales linearly with input tokens; prompt caching can substantially reduce the cost of repeated long prefixes."
    tier: settled
  - claim: "Frontier models in 2025–2026 generally outperform their 2023 predecessors on long-context recall, but the degradation pattern persists — particularly on multi-needle queries."
    tier: contested
  - claim: "Whether continued growth in context windows + prompt caching will obviate retrieval architectures for most product use cases is unsettled."
    tier: unsettled
sources:
  - url: "https://arxiv.org/abs/2307.03172"
    title: "Lost in the Middle: How Language Models Use Long Contexts (Liu, Lin, Hewitt, Paranjape, Bevilacqua, Petroni, Liang)"
    date: 2023-07-06
    primary_source: true
  - url: "https://arxiv.org/abs/2404.06654"
    title: "RULER: What's the Real Context Size of Your Long-Context Language Models? (Hsieh, Sun, Kriman, Acharya, Rekesh, Jia, Zhang, Ginsburg — NVIDIA)"
    date: 2024-04-09
    primary_source: true
  - url: "https://platform.claude.com/docs/en/build-with-claude/context-windows"
    title: "Anthropic — Context windows (Claude API docs)"
    date: 2026-05-09
    primary_source: true
  - url: "https://platform.claude.com/docs/en/build-with-claude/prompt-caching"
    title: "Anthropic — Prompt caching (Claude API docs)"
    date: 2026-05-09
    primary_source: true
  - url: "https://platform.openai.com/docs/models/gpt-4.1"
    title: "OpenAI — GPT-4.1 model reference"
    date: 2026-05-09
    primary_source: true
  - url: "https://openai.com/index/gpt-4-1/"
    title: "OpenAI — Introducing GPT-4.1 in the API (launch post, source for the long-context needle-retrieval claim)"
    date: 2025-04-14
    primary_source: true
  - url: "https://ai.google.dev/gemini-api/docs/long-context"
    title: "Google — Gemini API long context (developer docs)"
    date: 2026-05-09
    primary_source: true
rubric:
  - text: "Names the three-way trade-off the unit is built around — fits-more vs. costs-more vs. recall-can-degrade-over-long-context — and treats the context window as a budget, not a capacity number to be filled."
  - text: "Identifies a concrete failure mode of \"just use the long window\" beyond hitting the hard limit, AND identifies the mechanism behind it — e.g., per-call cost scales linearly because input tokens are priced linearly; effective recall can degrade mid-context, as documented in long-context recall evals; single-document architectures break under multi-document or growing-state load because retrieval and summarization weren't designed in."
  - text: "Distinguishes a regime where \"use the long window\" is a reasonable default (single bounded document, query shape verified on representative input, cost acceptable for projected volume) from a regime that forces retrieval or summarization (growing corpora, growing agent state, query shapes that benefit from focused context)."
---

# Context Window

## Trade-off framing

- **When this matters:** any feature that touches long inputs (large
  documents, multi-turn agents, accumulating chat history, multi-document
  libraries) or any model-selection decision where one candidate
  advertises a much larger context window than another. The number on
  the spec sheet — 200k, 1M, 2M — is a *ceiling*, not a *budget you
  should plan to fill*.

- **When this breaks:** when the team treats *"fits in the window"* as
  *"works at quality."* Three failure modes compound: (1) input cost
  scales linearly with what you stuff in, so a 50× larger context is
  roughly a 50× larger per-call bill; (2) effective recall can degrade
  as the window fills — the right answer is in the input but the model
  may not find it, especially mid-context; (3) input grows over time
  (a chat agent, a doc library) and the architecture picked for one
  document doesn't survive fifty.

- **What it costs:** the discipline to size context against
  representative input rather than headline numbers; the willingness
  to ship retrieval or summarization as the default when volume or
  query shape pushes back; and the patience to verify recall on real
  fixtures before promising *"the model can hold the whole thing."*

## 90-second bite

A **context window** is the budget of tokens a model can consider in
a single call — the prompt you send plus the output you ask back,
counted against the same ceiling. The number on the spec sheet (200k,
1M, 2M) is the ceiling, not a target. The PM trap is treating *"the
input fits"* as *"the model will use it well, and the bill will look
how I expect."*

Three things follow, and they're the entire reason a PM cares:

1. **Cost scales linearly with what you stuff in.** Input tokens are
   billed linearly; doubling stuffed content roughly doubles the
   per-call input bill. A feature that drops a 100k-token document in
   costs ~5× one that summarizes to 20k first. At any meaningful
   volume, that's the difference between a sustainable feature and
   one finance kills.

2. **Effective recall can fall as the window fills.** A model
   advertised at 1M tokens is not a model that *uses* 1M tokens
   equally well. Long-context evals show recall can drop on content
   buried mid-input — the right answer is technically in context, and
   the model still misses it. The longer the window, the more this
   matters; the more decision-relevant the query, the more you need
   to verify recall on representative fixtures before promising
   *"it's read everything."*

3. **The same volume that drives cost and erodes recall reshapes what
   "fits" means.** A single-doc-fits-in-context feature is a different
   system from a multi-doc or growing-corpus feature. Retrieval,
   chunking, and summarization aren't a v2 — they're what the system
   needs once volume or query shape pushes back.

If you're scoping a long-input feature, you're scoping a budget, not
a capacity. Size it against the actual workload, the actual queries,
and the actual cost line.

## Depth

The headline numbers shift fast; what doesn't shift is the trade-off
they expose. As of mid-2026, frontier providers advertise context
windows in the **1M–2M** range; the previous 200k frontier tier is
being retired this quarter. The numbers have grown roughly an order
of magnitude over three years and will keep growing. The constants
worth a PM's attention: the headline is a *ceiling*, the *budget* is
what you actually use, and the gap between them widens with every
release.

**The recall finding.** Liu et al. (2023) — *Lost in the Middle: How
Language Models Use Long Contexts* — showed that long-context models
are most accurate when the relevant information sits at the beginning
or end of the input, and worst when it's buried in the middle.
Subsequent work (RULER and needle-in-a-haystack–style probes)
replicates the pattern across newer models: 2025–2026 frontier models
generally outperform their 2023 predecessors on long-context recall,
but the degradation persists — most visibly on *multi-needle* queries
that require integrating several buried facts. The PM consequence: a
200k-token document fitting in a 1M window is not the same fact as
the model **finding** a specific clause inside it. Verify on
representative queries, not on your favorite test query.

**Vendor benchmarks and independent benchmarks routinely disagree.**
OpenAI claims GPT-4.1 retrieves needles accurately across all
positions up to 1M tokens; RULER finds that only half of frontier-tier
models maintain quality even at 32k. Verify recall on your own
representative queries, not on the vendor's.

**Input and output share the window — the accounting differs by
provider.** The headline is a per-call ceiling that includes the
prompt, the model's output, and — depending on provider —
tool/function definitions, tool-call traces, and cached prefixes.
Output length isn't free: a long generation eats input budget.
Tool-heavy systems (function calling, multi-step agents) consume more
of the window than their raw input suggests. *Don't generalize a
single provider's accounting rule.* Read the docs for the API you're
shipping on; the gotchas (separate output caps, tool-definition
counting, cache-window interaction) live in the footnotes.

**Prompt caching changes the cost curve.** Major providers now offer
prompt caching that drops the per-call cost of repeated long prefixes
substantially — Anthropic documents up to roughly an order of
magnitude on cache hits; OpenAI and Google offer similar mechanisms
with provider-specific discount rates. For features that reuse the
same long input across many queries — Q&A on a fixed document, a
long system prompt, a stable RAG context — caching can shift the
economics from *"long context is expensive"* to *"long context is
acceptable for this query shape."*

**The three architectural responses.** When the trade-off pushes back
— cost, recall, or growing volume — three patterns are on the table:

- **Retrieval (RAG).** Pull only the relevant chunks per query. Wins
  on cost and recall; loses on architectural simplicity. Default for
  multi-doc or growing corpora. (Unit 10 goes deeper.)
- **Summarization.** Compress long inputs into shorter, denser
  representations before the call. Wins on cost; can lose nuance.
  Default when input is one long thing that compresses cleanly
  (transcripts, chat history).
- **Stuff-it-all-in.** Bounded input, verified queries, acceptable
  cost. Wins on simplicity and faithfulness; loses as volume grows.

Production systems usually blend two or all three. The decision isn't
permanent — it's the one you make for the query shape and volume you
have *now*, with a checkpoint for when those change.

**Cross-vendor caveat.** Context windows aren't apples-to-apples
across providers. The numbers reflect different tokenizers (Unit 1),
different recall behaviors at depth, and different cost curves. The
right vendor comparison is *cost-per-quality on your actual query
shape*, not the spec-sheet token count.

## Decision prompt

Your team is scoping a "policy document Q&A" feature: employees
upload internal policy PDFs (typically 80–250 pages each) and ask
plain-English questions like *"what's our remote-work policy for
international hires?"* The eng lead pitches: *"the new frontier model
has a ~1M-token context — let's just dump the whole document in and
skip building any retrieval system; we ship in two weeks instead of
two months."* Finance is asking how this scales next quarter, when
the plan is to let users upload their full policy library — 20–50
documents per user — and ask **needle-in-haystack questions like
*"find any policy in our library that contradicts the EU AI Act's
transparency requirements,"*** where the right answer is a specific
clause buried somewhere in the corpus.

Walk through how you'd decide between stuff-it-all-in and
retrieval-backed for each phase (single-doc launch, multi-doc next
quarter). What evidence would you want before committing? Where is
the eng lead right, and where would the argument quietly cost the
team? Be specific about which trade-offs you're weighing and where
you'd be willing to be wrong.
