# AI-101 — Term Draft Schema Completion

## Milestone
Term Draft Schema Completion.

## Task Type
Focused implementation pass.

## Objective
Make the Term Draft feature follow the canonical glossary term schema more completely so draft submissions can capture the same structured content model expected by the product.

## Product Context
- App name: AI-101
- Product: AI Terms Glossary
- The glossary term schema is:
  - slug
  - term
  - definition
  - explanation
  - humor
  - see_also
  - tags
  - controversy_level
- The current Term Draft flow is incomplete and does not expose the full canonical structure
- Drafts should remain drafts pending review; this task is about schema completeness, not auto-publishing

## Mission
Expand the Term Draft flow so draft submissions can capture the full canonical term shape expected by the glossary, while preserving a clean user experience and correct backend integration.

## Required Outcomes
1. Extend the Term Draft form to support:
   - humor
   - see_also
   - tags
   - controversy_level
   - while retaining:
     - term
     - definition
     - explanation
     - categoryId
2. Keep required validation for:
   - term
   - definition
   - explanation
   - categoryId
3. Keep optional fields optional unless backend explicitly requires otherwise:
   - humor
   - see_also
   - tags
   - controversy_level
4. Update Android models so Term Draft submissions align with the canonical schema more closely
5. Update remote draft models and repository/API wiring so the payload includes the new fields correctly
6. Preserve the existing draft submission success behavior and “pending review” messaging
7. Avoid unrelated churn

## Execution Rules
- Treat this as a schema-completion fix for Term Draft
- Do not remove existing working validation for term, definition, explanation, or categoryId
- Do not turn draft submission into live publishing
- Do not broaden scope into moderation UI or gamification UI
- Keep the implementation focused on draft form completeness and payload correctness

## Deliverables
1. Updated `TermDraftScreen.kt`
2. Updated `TermDraftViewModel.kt`
3. Updated `TermDraft.kt`
4. Updated `RemoteTermDraft.kt`
5. Any required repository/API mapping updates
6. Summary of files changed
7. Merge readiness assessment

## Acceptance Criteria
- Term Draft form includes fields for humor, see_also, tags, and controversy_level
- Existing required fields still validate correctly
- Draft submission succeeds with the richer schema payload
- Draft success message still clearly says the submission is pending review
- No regressions in Search fallback or draft navigation flow
- No unrelated UI churn

## Validation Expectations
- Run the app
- Search for a missing term
- Open Create Term Draft
- Confirm the new fields are present
- Submit a valid draft including optional canonical fields
- Confirm draft submission succeeds
- Confirm no live publication occurs automatically

## Codex Prompt
```text
Implement the next focused milestone for AI-101: Term Draft Schema Completion.

Project context:
- App name: AI-101
- Product: AI Terms Glossary
- The glossary term schema is:
  - slug
  - term
  - definition
  - explanation
  - humor
  - see_also
  - tags
  - controversy_level
- The current Term Draft flow is incomplete and does not expose the full canonical structure
- Drafts must remain drafts pending review

Mission:
Expand the Term Draft flow so draft submissions can capture the full canonical term shape expected by the glossary, while preserving a clean user experience and correct backend integration.

Required outcomes:
1. Extend the Term Draft form to support:
   - humor
   - see_also
   - tags
   - controversy_level
   - while retaining:
     - term
     - definition
     - explanation
     - categoryId
2. Keep required validation for:
   - term
   - definition
   - explanation
   - categoryId
3. Keep optional fields optional unless backend explicitly requires otherwise:
   - humor
   - see_also
   - tags
   - controversy_level
4. Update Android models so Term Draft submissions align with the canonical schema more closely
5. Update remote draft models and repository/API wiring so the payload includes the new fields correctly
6. Preserve the existing draft submission success behavior and “pending review” messaging
7. Avoid unrelated churn

Execution rules:
- Treat this as a schema-completion fix for Term Draft
- Do not remove existing working validation for term, definition, explanation, or categoryId
- Do not turn draft submission into live publishing
- Do not broaden scope into moderation UI or gamification UI
- Keep the implementation focused on draft form completeness and payload correctness

Acceptance criteria:
- Term Draft form includes fields for humor, see_also, tags, and controversy_level
- Existing required fields still validate correctly
- Draft submission succeeds with the richer schema payload
- Draft success message still clearly says the submission is pending review
- No regressions in Search fallback or draft navigation flow
- No unrelated UI churn

Validation:
- Run the app
- Search for a missing term
- Open Create Term Draft
- Confirm the new fields are present
- Submit a valid draft including optional canonical fields
- Confirm draft submission succeeds
- Confirm no live publication occurs automatically
```
