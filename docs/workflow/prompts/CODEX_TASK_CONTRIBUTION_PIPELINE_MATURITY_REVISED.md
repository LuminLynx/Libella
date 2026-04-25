# CODEX TASK — CONTRIBUTION PIPELINE MATURITY

## Goal
Make the existing Term Draft feature operationally reliable: drafts can be submitted, reviewed, approved/rejected, published intentionally, and traced back to contributors.

This is a backend-first workflow hardening task with only focused Android/UI changes where needed.

## Required result
After this task:

- users can still create term drafts from missing-term searches
- drafts are stored with canonical fields
- drafts do not become live terms automatically
- draft status transitions are explicit and safe
- approval and publishing are separate steps
- rejected drafts remain traceable
- published terms can be traced back to their source draft
- contributor metadata is preserved through the workflow
- future scoring/gamification can be added later without rewriting the data model

## Scope

### 1. Harden draft submission
Ensure draft submission preserves:
- slug
- term
- definition
- explanation
- humor
- seeAlso
- tags
- controversyLevel
- categoryId
- contributorId / contributor metadata where currently supported

Validation requirements:
- term, definition, explanation, and categoryId are required
- controversyLevel must be in the supported range
- submitted drafts must not appear as live glossary terms

### 2. Formalize draft lifecycle
Support clear statuses:

- submitted
- approved
- rejected
- published

Rules:
- submitted -> approved is allowed
- submitted -> rejected is allowed
- approved -> published is allowed
- rejected -> published should not be allowed without re-approval
- published drafts should not be published twice
- publishing must create/update the live term only through the intended publish path

### 3. Preserve contributor tracking
Ensure contributor identity/metadata is stored on draft submission and remains attached through:
- status updates
- approval
- rejection
- publication

Do not build a full contributor profile UI.
Backend/data correctness is the priority.

### 4. Link drafts to missing-term intent where practical
If missing search logging already exists, connect drafts to the originating missing search where the current architecture supports it.

Do not build complex dedupe/ranking yet.
Only add clean data hooks needed for future prioritization.

### 5. Add or update verification
Add/update scripts or tests to verify:
- draft creation
- status transitions
- publish validation
- contributor preservation
- published draft creates a live term
- rejected draft cannot be published
- already-published draft cannot be published again
- live glossary flows still work

## Android/UI scope
Only make focused Android changes needed to keep the existing draft flow usable:
- clear success/error states
- correct field validation
- submitted draft messaging
- no accidental live-term display before approval/publish

Do not build:
- admin dashboard
- moderation UI
- gamification UI
- contributor profile UI

## Non-goals
Do not implement:
- badges
- scoring algorithms
- large-scale duplicate detection
- AI enrichment workflow
- complex moderation dashboard
- broad navigation redesign

## Deliverables
Codex must provide:
- changed files summary
- migration/schema changes, if any
- API changes, if any
- verification commands run
- results of verification
- known limitations

## Merge criteria
Do not consider this complete unless:

- backend compiles
- Android compiles if touched
- draft submission still works
- approved draft can be published
- rejected draft cannot be published
- published draft cannot be published twice
- contributor linkage is preserved
- Browse, Search, Categories, and Term Details still work
