---
id: sample-clean
slug: sample-clean
path_id: llm-systems-for-pms
position: 1
prereq_unit_ids: []
status: draft
definition: A clean fixture exercising every required field for the schema linter.
calibration_tags:
  - claim: "The linter accepts settled tier."
    tier: settled
  - claim: "The linter accepts contested tier."
    tier: contested
  - claim: "The linter accepts unsettled tier."
    tier: unsettled
sources:
  - url: "https://example.com/primary"
    title: "Primary Source Title"
    date: 2024-01-15
    primary_source: true
  - url: "https://example.com/secondary"
    title: "Secondary Source Title"
    date: 2024-03-22
rubric:
  - text: "Names the trade-off the unit is built around."
  - text: "Identifies a concrete scenario where the trade-off bites."
  - text: "Distinguishes when this matters from when it doesn't."
---

# Sample Clean Unit

## Trade-off framing

When this matters, when this breaks, what it costs.

## 90-second bite

The 90-second read.

## Depth

The longer reader.

## Decision prompt

The open-ended decision question for the user.
