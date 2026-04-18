# AI-101 — Canonical Term Rendering in Android

## Milestone
Canonical Term Rendering in Android.

## Task Type
Large implementation pass.

This is an Android/product-rendering milestone for AI-101.

## Objective
Make the Android app consume and render the canonical backend term contract correctly so every term is presented as structured glossary content rather than as a partial or legacy representation.

## Product Context
- App name: AI-101
- Product: AI Terms Glossary
- Source of truth is backend/database, not YAML
- PR #19 already handled backend/data integrity and canonical term normalization
- The backend now owns the canonical term contract
- The app must not display YAML, but every term must preserve and present the same logical structure as the canonical term definition

## Canonical Term Structure
- slug
- term
- definition
- explanation
- humor
- see_also
- tags
- controversy_level

## Important Product Requirement
A well-formatted term in the app must preserve the same semantic structure as this schema, even though the UI is not YAML:
- slug
- term
- definition
- explanation
- humor
- see_also
- tags
- controversy_level

## Current Problem
The visible app experience is still not correctly reflecting the canonical term structure.
This must now be fixed in the Android client, end to end.

## Working Style Requirements
- Do not use MVP framing
- Do not propose tiny cleanup tasks
- Use a broad capability-based implementation
- Prioritize production readiness
- Prefer decisive, app-level completion over partial tweaks
- Do not push presentation responsibility back to the backend if the backend already provides the needed data

## Mission
Make the Android app consume and render the canonical term contract correctly, so the details experience reflects the intended glossary structure everywhere it matters.

## Required Outcomes
1. Audit the current Android term DTO/domain/UI model against the canonical backend term contract
2. Update the Android networking/model mapping so canonical fields are first-class citizens
3. Remove reliance on legacy compatibility aliases where appropriate
4. Update the term details flow so the app renders the full canonical structure cleanly and consistently
5. Ensure the UI presents these sections properly when data is present:
   - definition
   - explanation
   - humor
   - see also
   - tags
   - controversy level
6. Keep the presentation polished and production-ready
7. Avoid raw/debug-style data dumps
8. Preserve working app flows:
   - Terms
   - Categories
   - Search
   - Details
9. Ensure the app gracefully handles missing optional content without broken layouts
10. Keep unrelated architecture churn out of scope

## Execution Rules
- Treat this as an Android/product-rendering milestone
- Assume backend canonicalization from PR #19 is already in place
- Do not solve this by hardcoding term content in the app
- Do not reintroduce YAML concepts into the UI
- The UI should reflect structured content, not raw storage format
- Prefer clean composable sections and good spacing/typography
- Preserve compatibility where necessary, but bias toward canonical field usage
- Do not add hacky client-side data repair logic unless absolutely necessary for safe rendering

## Deliverables
1. Updated Android DTO/domain/UI models for canonical term data
2. Updated repository / mapping logic
3. Updated term details UI rendering the canonical structure
4. Summary of files changed
5. Remaining risks, if any
6. Merge readiness assessment

## Acceptance Criteria
- A term detail screen clearly presents the term as structured content, not just a title plus partial text
- Canonical fields are correctly consumed from backend responses
- Tags and related terms display cleanly
- Controversy level is shown in a sensible way
- Humor is shown as a real section when available
- Missing optional fields do not create ugly gaps or crashes
- Existing list/search/category flows still work
- No backend workaround PR is required for this Android milestone

## Validation Expectations
- Run the Android app
- Verify Terms
- Verify Categories
- Verify Search
- Open multiple term detail screens
- Confirm the canonical sections render correctly
- Confirm no regressions in navigation or theming
- Provide concrete verification notes, not assumptions

## Important
The goal is not merely “cleaner code.”
The goal is to fix the actual product issue:
the app must finally present terms according to the intended canonical term structure.

## Codex Prompt
```text
Implement the next major milestone for AI-101: Canonical Term Rendering in Android.

Project context:
- App name: AI-101
- Product: AI Terms Glossary
- Source of truth is backend/database, not YAML
- PR #19 already handled backend/data integrity and canonical term normalization
- The backend now owns the canonical term contract
- The app must not display YAML, but every term must preserve and present the same logical structure as the canonical term definition

Canonical term structure:
- slug
- term
- definition
- explanation
- humor
- see_also
- tags
- controversy_level

Important product requirement:
A well-formatted term in the app must preserve the same semantic structure as this schema, even though the UI is not YAML:
- slug
- term
- definition
- explanation
- humor
- see_also
- tags
- controversy_level

The current problem:
The visible app experience is still not correctly reflecting the canonical term structure.
This must now be fixed in the Android client, end to end.

Working style requirements:
- Do not use MVP framing
- Do not propose tiny cleanup tasks
- Use a broad capability-based implementation
- Prioritize production readiness
- Prefer decisive, app-level completion over partial tweaks
- Do not push presentation responsibility back to the backend if the backend already provides the needed data

Mission:
Make the Android app consume and render the canonical term contract correctly, so the details experience reflects the intended glossary structure everywhere it matters.

Required outcomes:
1. Audit the current Android term DTO/domain/UI model against the canonical backend term contract
2. Update the Android networking/model mapping so canonical fields are first-class citizens
3. Remove reliance on legacy compatibility aliases where appropriate
4. Update the term details flow so the app renders the full canonical structure cleanly and consistently
5. Ensure the UI presents these sections properly when data is present:
   - definition
   - explanation
   - humor
   - see also
   - tags
   - controversy level
6. Keep the presentation polished and production-ready
7. Avoid raw/debug-style data dumps
8. Preserve working app flows:
   - Terms
   - Categories
   - Search
   - Details
9. Ensure the app gracefully handles missing optional content without broken layouts
10. Keep unrelated architecture churn out of scope

Execution rules:
- Treat this as an Android/product-rendering milestone
- Assume backend canonicalization from PR #19 is already in place
- Do not solve this by hardcoding term content in the app
- Do not reintroduce YAML concepts into the UI
- The UI should reflect structured content, not raw storage format
- Prefer clean composable sections and good spacing/typography
- Preserve compatibility where necessary, but bias toward canonical field usage
- Do not add hacky client-side data repair logic unless absolutely necessary for safe rendering

Deliverables:
1. Updated Android DTO/domain/UI models for canonical term data
2. Updated repository / mapping logic
3. Updated term details UI rendering the canonical structure
4. Summary of files changed
5. Remaining risks, if any
6. Merge readiness assessment

Acceptance criteria:
- A term detail screen clearly presents the term as structured content, not just a title plus partial text
- Canonical fields are correctly consumed from backend responses
- Tags and related terms display cleanly
- Controversy level is shown in a sensible way
- Humor is shown as a real section when available
- Missing optional fields do not create ugly gaps or crashes
- Existing list/search/category flows still work
- No backend workaround PR is required for this Android milestone

Validation expectations:
- Run the Android app
- Verify Terms
- Verify Categories
- Verify Search
- Open multiple term detail screens
- Confirm the canonical sections render correctly
- Confirm no regressions in navigation or theming
- Provide concrete verification notes, not assumptions

Important:
The goal is not merely “cleaner code.”
The goal is to fix the actual product issue:
the app must finally present terms according to the intended canonical term structure.
```
