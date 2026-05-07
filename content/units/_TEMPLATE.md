---
# Unit identity (slot 9 prereq pointers also live here as prereq_unit_ids).
id: example-unit
slug: example-unit
path_id: llm-systems-for-pms
position: 1
prereq_unit_ids: []
status: draft

# Slot 2 — single-sentence definition. One sentence. End with a period.
definition: One-sentence definition of the concept this unit teaches.

# Slot 6 — calibration tags. One entry per key claim. tier must be one of:
# settled | contested | unsettled. P2 ("calibrate, don't bluff") is
# non-negotiable: every claim worth tagging gets tagged.
calibration_tags:
  - claim: "A concrete claim made in the bite or depth."
    tier: settled
  - claim: "A claim where the field is split."
    tier: contested
  - claim: "A claim that nobody really knows yet."
    tier: unsettled

# Slot 7 — sources. Primary preferred. Every source must have url, title,
# and date (YYYY-MM-DD). primary_source defaults to false; flip to true
# for original papers, official docs, etc.
sources:
  - url: "https://example.com/primary-source"
    title: "Title of the primary source"
    date: 2024-01-15
    primary_source: true
  - url: "https://example.com/secondary"
    title: "Title of the secondary source"
    date: 2024-03-22
    primary_source: false

# Slot 8 (rubric half) — at least one criterion. Each criterion is a
# non-empty text string the LLM grader will check the user's answer against
# (per-criterion checklist, T2-A in STRATEGY.md).
rubric:
  - text: "Names the trade-off the unit is built around."
  - text: "Identifies at least one concrete scenario where the trade-off bites."
  - text: "Distinguishes the case where this concept matters from the case where it doesn't."
---

# Slot 1 — Title goes here

## Trade-off framing

Slot 3. Three short paragraphs or bullets:

- **When this matters:** ...
- **When this breaks:** ...
- **What it costs:** ...

## 90-second bite

Slot 4. The read. Roughly 90 seconds. Plain language, decision-flavored.
This is what the user sees first when they tap into the unit.

## Depth

Slot 5. The longer reader (or a description of the interactive widget,
or both). This is the "depth on tap" surface from P4.

## Decision prompt

Slot 8 (prompt half). The open-ended question the user answers. Decision-
flavored, not factual recall. The rubric above grades the answer.
