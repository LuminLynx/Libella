---
id: prompt-design-bundle-0
slug: prompt-design
path_id: llm-systems-for-pms
position: 6
prereq_unit_ids:
  - model-selection-bundle-0
status: published
definition: Prompt design is the discipline of specifying behavior to a model — what's locked in the system prompt versus what varies per call, and whether the specification is carried by instructions, examples, or a mix — anchored to the feature's actual specification needs, not to "writing tips."
calibration_tags:
  - claim: "System prompt is loaded once per session (and benefits from prompt caching); user prompt varies per call. Where each piece of the contract lives is a deliberate design decision."
    tier: settled
  - claim: "Few-shot examples capture nuance that instruction-only specification can't fully cover, especially on ambiguous-criteria tasks. Brown et al. (2020) is the foundational reference."
    tier: settled
  - claim: "Per-provider structural conventions (XML for Claude, JSON for GPT-4.1, etc.) materially affect model behavior; a prompt that works on one provider may degrade on another."
    tier: settled
  - claim: "The exact instruction-vs-few-shot threshold (how many examples to include, when to switch) is task-dependent and not generalizable across features."
    tier: contested
  - claim: "Whether prompt design will remain a load-bearing PM-decision discipline or be largely automated by next-generation model self-prompting / agentic-loop systems is unsettled."
    tier: unsettled
sources:
  - url: "https://arxiv.org/abs/2005.14165"
    title: "Language Models are Few-Shot Learners (Brown, Mann, Ryder, Subbiah, Kaplan, Dhariwal, Neelakantan, Shyam, Sastry, Askell, Agarwal, Herbert-Voss, Krueger, Henighan, Child, Ramesh, Ziegler, Wu, Winter, Hesse, Chen, Sigler, Litwin, Gray, Chess, Clark, Berner, McCandlish, Radford, Sutskever, Amodei — OpenAI / GPT-3)"
    date: 2020-05-28
    primary_source: true
  - url: "https://platform.claude.com/docs/en/build-with-claude/prompt-engineering/claude-prompting-best-practices"
    title: "Anthropic — Prompting best practices (Claude API docs)"
    date: 2026-05-11
    primary_source: true
  - url: "https://platform.openai.com/docs/guides/prompt-engineering"
    title: "OpenAI — Prompt engineering guide (developer docs)"
    date: 2026-05-11
    primary_source: true
  - url: "https://ai.google.dev/gemini-api/docs/prompting-strategies"
    title: "Google — Gemini prompt design strategies (developer docs)"
    date: 2026-05-11
    primary_source: true
rubric:
  - text: "Names prompt design as a multi-axis trade-off (system-vs-user prompt, instructions-vs-examples) AND treats it as a behavior-shaping contract decision anchored to the feature's specification needs — not as a writing-quality exercise."
  - text: "Identifies a concrete failure mode of single-axis prompt design AND explains the mechanism — e.g., instruction-only misses edge cases that examples catch (mechanism: rules can't fully specify ambiguous-criteria tasks); few-shot-only stales when data distribution shifts (mechanism: the example set locks in a specific data distribution and degrades as the corpus drifts); putting per-query variability in the system prompt breaks prompt caching and inflates cost-per-call (mechanism: prompt caching is a prefix-reuse optimization — each unique system-prompt prefix is a separate cache entry, so per-call variability in the system prompt loses cache hits proportionally to the variability)."
  - text: "Distinguishes which approach is load-bearing in which regime — system prompt for stable behavior contracts (locked rules, output schema, role definition); user prompt for per-call variability (the actual content being processed); zero-shot instructions for cleanly-specifiable rules with low edge-case density; few-shot examples for ambiguous-criteria or edge-heavy tasks where instructions can't fully cover the space; and recognizes that the PM-default is usually a hybrid (instructions for the rule-shaped part + 3–5 examples for the boundaries) rather than picking one extreme."
---

# Prompt design basics

## Trade-off framing

- **When this matters:** any feature where the team is
  committing to a specific model and needs to encode the
  feature's specification into a form the model can act on
  consistently. Prompt design lives downstream of model
  selection (Unit 5) because prompts are model-specific in
  subtle ways — Claude responds best to XML-tag-delimited
  structure, GPT-4.1 prefers JSON-shaped instruction blocks,
  Gemini has its own conventions. Teaching prompt design
  before a tier choice is teaching it in the wrong order.

- **When this breaks:** when the team treats prompt design
  as a writing exercise instead of a contract decision.
  Three failure shapes recur: (1) putting per-query
  variability in the system prompt breaks prompt caching —
  each unique system prompt is a separate cache entry, so
  per-call variability cancels the cost savings caching
  was meant to deliver; (2) specifying ambiguous-criteria
  tasks with instructions only misses edge cases that
  examples catch (the model fills the gap with plausible
  guesses, which surfaces as the hallucination rate); (3)
  reusing a Claude-shaped prompt on GPT-4.1 degrades
  because the structural conventions encode behavior signal
  the new model doesn't parse the same way.

- **What it costs:** the discipline to think through both
  axes (where the contract lives + how it's expressed)
  before writing prose; the willingness to measure on a
  representative eval set rather than the prompt-author's
  intuition; the maintenance commitment to update examples
  as the data distribution drifts; and the understanding
  that a vendor swap is also a prompt-rewrite, not just a
  model-ID swap.

## 90-second bite

**Prompt design** is the discipline of specifying behavior
to a model — *what the feature does* expressed in a form
the model can act on consistently. The trap is treating it
as a *writing exercise* when it's actually a
*contract-shape decision*.

Two axes, not one:

1. **Where the contract lives — system vs user prompt.**
   System prompt carries the stable parts of the contract
   that apply to every call (rules, role, output schema).
   User prompt varies per call and carries the specific
   request (the transcript, the question, the document).
   The system-vs-user split is what makes prompt caching
   (Unit 2) work: a stable system-prompt prefix is reused
   across calls at ~10% of the input rate. *Wrong move:*
   putting per-query variability in the system prompt —
   each unique system prompt is a separate cache entry,
   so caching breaks and per-call cost climbs proportionally
   to the variability. *Symmetric wrong move:* putting
   stable behavior contracts in the user prompt — the
   rules are re-billed at full rate on every call instead
   of being cached.

2. **How the contract is expressed — instructions vs
   examples.** Instructions are declarative ("classify
   tickets as P0/P1/P2 based on these rules"). Cheap in
   tokens, easy to update, but miss edge cases the rules
   don't cover. Examples (few-shot) are demonstrative
   ("here are 10 tickets with their labels"). Token-heavy,
   capture nuance instructions can't, but stale when the
   data distribution shifts.

The PM-default is almost always a **hybrid**: clear
instructions for the rule-shaped part of the specification,
3–5 examples for the boundary cases the rules can't cleanly
cover. Pure instructions miss what examples catch. Pure
few-shot stales the moment your data drifts. Hybrid hedges
both directions.

What the PM call actually is: figuring out which parts of
the feature's behavior are *rule-specifiable* (instructions),
which are *example-shaped* (few-shot), and which are
*per-query variable* (user prompt, not system). Then writing
it once, in a form the model can act on consistently across
every call, with the costs (token bill, update frequency,
edge-case coverage) all visible.

If you're designing a prompt, you're not writing — you're
encoding a behavior contract. The writing quality matters
less than where each piece of the specification lives.

## Depth

Prompt design separates what a feature *does* (the
specification) from how it *says* what it does (the
writing). PMs scope features by specification — *"the bot
classifies tickets into P0/P1/P2"* — but ship features
through prompts. The translation matters because the prompt
is where the specification gets compressed into a form the
model can act on every call, consistently, at production
volume.

**Three structural questions PMs should answer before the
eng team writes a prompt:**

- **What's locked vs what varies?** Behavior contracts
  (rules, role, output schema) belong in the system prompt
  — they apply to every call and benefit from prompt
  caching (Unit 2). Per-call content (the transcript, the
  user query, the document) belongs in the user prompt.
  The dividing line is *"does this change per request?"*
  If yes, user. If no, system.
- **What's rule-specifiable vs example-shaped?** Cleanly
  specifiable rules (output format, required fields, hard
  category boundaries) work as instructions. Ambiguous
  criteria (what counts as a *high-priority* ticket, what
  tone is *too aggressive*, what action item is *actually
  actionable*) are example-shaped — the model needs to see
  3–10 instances to internalize the boundary.
- **What's the maintenance cadence?** Instructions are
  cheap to update; few-shot examples need re-curation when
  the data drifts. A specification that changes weekly
  should bias toward instructions; one that's locked for a
  quarter can afford the example-curation cost.

**Instructions: what they do well.** Declarative rules work
when the specification is clean — output schema, hard
category boundaries, role definition, refusal conditions.
Token cost is low (a 600-token instruction-only system
prompt is small). Updates are textual: change a sentence,
redeploy. Failure mode: any criterion that requires
*judgment* leaks edge cases the rules don't cover.
Instructions are good at *what* but bad at *how much* and
*which kind* on ambiguous-criteria tasks.

**Few-shot: what it does well.** Examples capture nuance
that instructions can't fully specify. They're load-bearing
when the task is *"learn this distribution"* rather than
*"apply these rules"* — classification with ambiguous
categories, style matching, structured-output formatting
with implicit conventions. Token cost compounds (10 example
pairs at 200 tokens each = 2,000 tokens of system-prompt
overhead per call, even with caching). Update cost is real:
a new product launch with new vocabulary breaks examples
that don't include it.

**The hybrid pattern (the PM-default).** Almost every
production prompt is a hybrid: ~200–600 tokens of
instructions covering the rule-shaped part, plus 3–5
boundary examples covering the cases instructions can't
reach. The hybrid catches what either pure approach misses:
instructions provide the structural backbone, examples
cover the judgment-shaped edges. Bake-off advice from
Unit 4 applies — measure on a representative eval set, not
on the prompt-author's intuition.

**Provider-specific format conventions.** Each provider has
structural preferences that shape model behavior:

- **Claude** responds best to XML-tag-delimited structure
  (`<rules>...</rules>`, `<examples>...</examples>`). The
  Anthropic prompt-engineering docs make this explicit.
- **OpenAI GPT-4.1** prefers JSON-shaped instruction blocks
  and Markdown for hierarchy.
- **Gemini** has its own conventions documented in Google's
  prompt-design guide.

This matters most at vendor-swap time. A prompt that works
on Claude reused on GPT-4.1 will often degrade — not
because the rules are different but because the structural
conventions encode behavior signal the new model doesn't
parse the same way. **A vendor swap is also a
prompt-rewrite.** Plan accordingly.

**Cross-axis caveat.** The two axes interact. A 12-example
few-shot block belongs in the system prompt (it's stable),
not the user prompt (which carries variable content).
Putting examples in the user prompt blows away prompt
caching and re-bills the example tokens on every call.
Conversely, putting per-query content in the system prompt
breaks the cache-prefix reuse — each unique system prompt
is a separate cache entry, so per-call variability cancels
the caching cost savings. *The axes aren't independent;
the system-vs-user choice is what makes prompt caching
work, and breaking the split breaks the caching.*

## Decision prompt

Your team is shipping a meeting-action-item extractor.
Input: a meeting transcript (~5–15k tokens). Output: a JSON
list where each item has `{owner, action, due_date}`. The
team has chosen Sonnet-class for the model tier (Unit 5
decision).

Engineering has prototyped three prompt strategies and run
them on a 50-transcript eval set:

- **(A) Instruction-only system prompt:** ~600 tokens of
  detailed rules ("an action item must have an explicit
  assignee, an explicit verb, and a target completion
  timeframe..."). Catches 78% of true action items,
  hallucinates owners on 8% of outputs.
- **(B) Few-shot system prompt:** 12 example transcripts
  with annotated action-item lists, ~3,500 tokens of
  system-prompt overhead. Catches 88% of true action
  items, hallucinates owners on 3%, but the annotations
  need updating when meeting style shifts (new team naming
  conventions, new project-tracking vocab).
- **(C) Hybrid:** ~250 tokens of core instructions + 4
  boundary examples (~1,200 tokens total). Catches 85%,
  hallucinates 4%.

The PM has been told the feature ships in 3 weeks and the
eng team needs the prompt locked in 1 week so the rest of
the integration can proceed. How do you scope the
decision? What ships, and how do you defend the choice?
Walk through which axis the trade-offs live on, where each
single-tier pick would miss, and where you'd be willing to
be wrong.
