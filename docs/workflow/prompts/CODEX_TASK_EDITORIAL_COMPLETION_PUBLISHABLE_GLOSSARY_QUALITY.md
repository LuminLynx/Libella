# CODEX TASK — EDITORIAL COMPLETION + PUBLISHABLE GLOSSARY QUALITY

## Goal
Finish the live glossary so it is canonically structured, content-complete, and presentation-ready on phone and tablet.

## Required result
All live terms must have:
- term
- definition
- explanation
- humor
- seeAlso
- tags
- controversyLevel
- categoryId

And the app must render those fields cleanly.

## Work to do

### 1. Editorial pass on live glossary content
Update the backend source of truth for the live glossary.

Primary target:
- `backend/db/seed.sql`

Requirements:
- no blank `humor` on published terms
- improve weak humor lines
- definitions must be direct and upfront
- explanations must be concrete and useful
- `seeAlso` must be meaningful
- tags must be useful and consistent
- controversy values must be intentional

### 2. Preserve canonical backend payloads
Ensure backend responses for live terms return:
- `slug`
- `definition`
- `explanation`
- `humor`
- `seeAlso`
- `tags`
- `controversyLevel`
- `categoryId`

Keep `seeAlso` human-readable everywhere.

### 3. Focused UI polish only where glossary quality depends on it
Check and fix only glossary-related presentation issues, especially:
- raw slug/id-like labels in `See Also`
- ugly chip truncation for normal related term names
- remaining legacy term-details behavior
- tablet layout issues in glossary screens

Target screens:
- Browse Terms
- Categories
- Term Details

Do not do broad redesign work.

## Non-goals
Do not work on:
- gamification
- contribution scoring UI
- large moderation systems
- new AI features
- major navigation redesign
- speculative architecture changes

## Verification
Verify:
1. live API payload shape is canonical
2. multiple live terms have strong humor / explanation / seeAlso / tags / controversy
3. phone rendering looks correct
4. tablet rendering looks correct

## Deliverables
- updated `backend/db/seed.sql`
- any necessary backend serialization fixes
- any necessary Android UI polish fixes
- summary of files changed and what was verified

## Quality bar
This is not a “non-empty fields” task.
This is a “publish-ready glossary quality” task.

Prefer fewer, stronger, consistent improvements over shallow churn.
