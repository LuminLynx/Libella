---
id: tokenization-bundle-0
slug: tokenization
path_id: llm-systems-for-pms
position: 1
prereq_unit_ids: []
status: published
definition: Tokenization is the step that splits raw text into the discrete units (tokens) a language model actually reads, generates, and bills on.
calibration_tags:
  - claim: "Models bill and meter context windows in tokens, not characters or words."
    tier: settled
  - claim: "BPE-style subword tokenization is the dominant scheme across frontier models."
    tier: settled
  - claim: "Token counts for the same prompt vary meaningfully across providers' tokenizers."
    tier: settled
  - claim: "Whether token-level pricing is the right unit of cost for product decisions long-term — vs. characters, semantic units, or compute-time billing — is unsettled as the market evolves."
    tier: contested
sources:
  - url: "https://arxiv.org/abs/1508.07909"
    title: "Neural Machine Translation of Rare Words with Subword Units (Sennrich, Haddow, Birch — the BPE paper)"
    date: 2015-08-31
    primary_source: true
  - url: "https://platform.openai.com/tokenizer"
    title: "OpenAI Tokenizer (interactive)"
    date: 2024-09-01
    primary_source: true
  - url: "https://github.com/openai/tiktoken"
    title: "tiktoken — OpenAI's open-source BPE tokenizer"
    date: 2024-11-12
    primary_source: true
rubric:
  - text: "Identifies that token count — not character or word count — is what drives cost, latency, and the context-window ceiling."
  - text: "Names a concrete scenario where tokenization choice changes a product outcome (e.g. non-English text, code, emoji, or names inflating token counts vs. expectations)."
  - text: "Distinguishes when tokenization matters for a PM decision (cost forecasting, prompt length budgeting, multilingual support) from when it can be safely ignored (most English-only, short-prompt features)."
---

# Tokenization

## Trade-off framing

- **When this matters:** any decision that touches *cost-per-call*,
  *prompt length budgets*, *context-window ceilings*, or *non-English /
  code / emoji / unusual-name* text. Tokenization is the unit your
  vendor bills on; if you're sizing a feature, you're sizing it in
  tokens whether you know it or not.
- **When this breaks:** when the team estimates costs in
  "characters" or "words" and ships a feature that quietly costs
  3–5× expected on multilingual or code-heavy inputs. Or when the
  team assumes a "32k context window" means 32k *words* and runs
  out of room mid-conversation.
- **What it costs:** a few hours to learn the model, the discipline
  to estimate in tokens going forward, and the willingness to count
  before promising. The downside of *not* paying that cost compounds:
  every cost forecast and every context-budget decision drifts.

## 90-second bite

A language model doesn't see your prompt as letters or words — it
sees a sequence of *tokens*, each one a chunk of text from a
fixed vocabulary the model was trained on. The mapping is fixed
per model: GPT-4o, Claude Sonnet 4.6, and Gemini all carve text
slightly differently.

Three things follow, and they're the entire reason a PM cares:

1. **You pay per token, not per character or word.** A 100-word
   English prompt and a 100-word Hindi prompt can differ by 3–5×
   in token count, and so in cost.
2. **Context windows are token-counted.** "200k tokens" sounds
   roomy, but a 200k-token context with a chatty multi-turn agent
   and a 50k-token document attached fills up faster than the
   number suggests.
3. **The tokenizer is part of the product.** Switching from one
   provider to another changes your token math; the same prompt
   isn't the same prompt across vendors.

If you're sizing a feature, sizing a budget, or comparing two
vendors, you're doing it in tokens. The unit is real, even if it's
invisible.

## Depth

The dominant tokenization scheme across frontier models is **byte-pair
encoding** (BPE) and its variants (SentencePiece, tiktoken's
cl100k_base / o200k_base). The original BPE algorithm comes from
data compression — *Sennrich, Haddow, and Birch (2015)* adapted it
for neural MT, and it's now the default. The mechanic: start from
characters, repeatedly merge the most frequent adjacent pair into a
new symbol, stop when the vocabulary hits a target size (typically
32k–200k entries). Common substrings ("the", " ing", " function")
become single tokens. Rare substrings (a misspelled name, an emoji,
a CJK character outside the model's training mix) split into many.

This explains the cost asymmetries:

- **English-language ASCII** ≈ 1 token per ~4 characters / ~0.75 words.
- **Code** is usually denser per token than English (familiar keywords,
  short identifiers) but explodes on long unique identifiers.
- **Non-Latin scripts** (Hindi, Arabic, CJK) often run 2–3× the token
  count of equivalent English content.
- **Emoji and unusual names** can split into 4+ tokens each.

Two practical implications most teams discover the hard way:

1. **Estimate token cost on representative input, not toy English.**
   If your real users will paste resumes, paste error logs, or write
   in Spanish, your cost model should reflect that.
2. **Measure context window in tokens, not in vibes.** Tools like
   OpenAI's tokenizer page and Anthropic's tokenizer-counting endpoint
   let you measure exact counts. Use them on real fixtures before
   promising "the model can hold the whole document."

For PMs comparing vendors: token counts for the same input *do not
match* across providers. A prompt that's 1,200 tokens in tiktoken's
o200k_base may be 1,400 in Claude's tokenizer and 1,100 in Gemini's.
This shifts both cost and what fits in the window. When a vendor
benchmark cites "X tokens / second", the X is in *that vendor's*
tokens.

## Decision prompt

You're scoping a feature that lets users paste a job description
(usually 400–800 English words, but ~25% of users paste resumes
or descriptions in Spanish, Portuguese, or Hindi instead) and get
an AI-generated summary back. Finance is asking for a per-call
cost estimate to greenlight launch.

How would you produce that estimate, and what's the risk if you
estimated it the way most teams do — by averaging word count and
multiplying by a rough $/word figure? Be specific about what you'd
measure, what you'd ignore, and where your estimate might still
be wrong.
