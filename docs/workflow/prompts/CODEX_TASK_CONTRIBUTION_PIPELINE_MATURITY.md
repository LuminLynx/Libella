# CODEX TASK — CONTRIBUTION PIPELINE MATURITY

## Goal
Turn the existing Term Draft feature into a real, manageable contribution pipeline.

The system already allows draft submission. This task should make that pipeline reviewable, structured, and ready for growth.

---

## Objective
Move AI-101 from:
- “users can submit term drafts”

to:
- “drafts can be reviewed, approved, rejected, tracked, and managed as a real contribution workflow”

---

## Required outcome
When this task is done:

- draft submission is polished and reliable
- draft records are easy to review
- moderation / approval flow is clear
- contributor tracking is real and preserved
- missing-term intake is manageable
- the system is ready for future scoring/gamification, without implementing the full gamification layer yet

---

## Scope

### 1. Harden the Term Draft workflow
Review and improve the current draft submission pipeline end to end.

Requirements:
- draft submission must remain stable
- required fields must be enforced properly
- canonical draft fields must be preserved
- success/error states must be clear
- no accidental publishing of unapproved drafts

### 2. Introduce real draft moderation flow
Make draft lifecycle states explicit and usable.

At minimum support:
- submitted
- approved
- rejected
- published

If additional internal states are helpful, they can be added, but keep the workflow understandable.

Requirements:
- approval must be distinct from publication
- rejected drafts must remain traceable
- published drafts must be clearly linked to resulting live terms

### 3. Improve contributor tracking
Make contributor data useful and durable.

Requirements:
- preserve contributor identity on draft submission
- keep contributor metadata attached through approval/publish flow
- do not lose attribution when drafts are reviewed or transformed into live terms
- structure backend data so future contribution scoring can be added cleanly

### 4. Make missing-term intake manageable
The system should support the reality that many users may search for missing terms.

Requirements:
- keep draft creation tied cleanly to missing search intent
- make it easier to identify high-value missing terms
- improve the relationship between missing searches and created drafts
- avoid turning raw intake into chaos

### 5. Prepare for future moderation tooling
Do not build a huge admin product yet, but make the data/model/backend ready for it.

Examples:
- clear draft status handling
- review-friendly summaries
- straightforward publish path
- ability to inspect contribution records later

---

## Non-goals
Do not implement in this task:
- full gamification UI
- badges
- ranking systems
- complex admin dashboard redesign
- large-scale dedupe intelligence
- broad AI content generation systems
- major glossary redesign

This milestone is about making the contribution pipeline solid, not fancy.

---

## Deliverables
Codex should provide:

1. stable draft submission flow
2. clear moderation / publish lifecycle
3. contributor tracking that survives the workflow
4. backend/data model improvements needed for the above
5. any focused UI improvements necessary to support the workflow
6. summary of files changed and what was verified

---

## Verification requirements

### Draft flow
Verify:
- a missing term can still become a draft
- required fields are enforced
- draft submission succeeds
- submitted drafts do not become live automatically

### Moderation flow
Verify:
- a draft can be approved
- a draft can be rejected
- a draft can be published only through the intended path
- published output is traceable back to the draft

### Contributor tracking
Verify:
- contributor identity is stored
- contributor linkage survives status transitions
- publishing does not discard attribution

### Regression safety
Verify:
- Browse Terms still works
- Categories still works
- Term Details still works
- Search still works
- current glossary publishing behavior does not regress

---

## Quality bar
This task should make the contribution pipeline feel intentional and durable.

Do not optimize for mock completeness.
Optimize for:
- real workflow clarity
- production readiness
- future extensibility for scoring and moderation

---

## Final instruction
Treat this as the milestone that makes user contributions operationally real.

The result should be a contribution pipeline that can support growth without becoming messy, while setting up the foundation for future gamification and moderation improvements.
