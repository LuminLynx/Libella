# AI-101 — Canonical Term Detail Schema Realignment

## Milestone
Canonical Term Detail Schema Realignment.

## Task Type
Large implementation pass.

This is a product-completion follow-up milestone for AI-101.

## Objective
Realign the Term Details experience so it faithfully reflects the actual intended displayed term schema, removing schema drift introduced by prior assumptions and ensuring the UI presents only the correct canonical structure for a term.

## Project Context
- App name: AI-101
- Product: AI Terms Glossary
- Source of truth is backend/database, not YAML
- PR #19 established backend/data integrity and canonical term normalization
- Subsequent Android follow-up work improved canonical term rendering
- Manual validation shows the details screen still does not match the intended displayed schema
- Prior implementation incorrectly treated "Explanation" as part of the canonical displayed schema

## Corrected Displayed Term Schema
The app must not display YAML, but the term details experience must preserve and present the intended displayed schema.

For the displayed canonical term format, treat these as the schema-defining fields/sections:
- term
- definition
- humor
- see_also
- tags
- controversy_level
- optionally slug as metadata if useful

## Important Correction
"Explanation" is not part of the displayed canonical term schema.
Do not render Explanation as a first-class schema section in the canonical term details experience.

## Observed Product Issues from Manual Validation
- The details screen still includes an Explanation section even though it is not part of the intended displayed schema
- The screen still feels framed as a generic details page instead of a schema-faithful glossary term experience
- The global header still uses generic framing such as "Term Details" / "Canonical glossary entry"
- The real term identity is still not sufficiently driving the overall page structure
- Non-schema extras such as AI Learning Scenario and AI Learning Challenge visually compete with the canonical term content
- Some metadata pill layouts have improved, but the full details experience still mixes schema content and extra feature content too loosely

## Mission
Make the Term Details screen faithfully represent the corrected displayed canonical term schema and clearly separate core glossary content from auxiliary AI-learning features.

## Working Style Requirements
- Do not use MVP framing
- Do not propose tiny cleanup tasks
- Use a broad, production-ready implementation pass
- Optimize for finished product behavior, not partial technical compliance
- Prioritize schema fidelity and clean product structure

## Required Outcomes
1. Audit the Term Details screen against the corrected displayed schema
2. Remove Explanation as a canonical displayed section
3. Ensure the real term identity is the primary page identity
4. Keep Definition as a first-class section when present
5. Keep Humor as a first-class section when present
6. Keep See Also as a first-class section when present
7. Keep Tags as a first-class section when present
8. Keep Controversy Level visible and polished
9. Optionally keep slug as secondary metadata if it improves the glossary feel
10. Reframe the page so generic headers/subheaders do not overpower the real term identity
11. Clearly separate auxiliary AI-learning modules (Scenario, Challenge, etc.) from the canonical term schema area
12. Preserve working app flows:
    - Browse Terms
    - Categories
    - Search
    - Details
13. Avoid unrelated architecture churn

## Execution Rules
- Treat this as a schema-fidelity correction pass
- Do not render Explanation as part of the canonical term display contract
- Do not invent new schema sections that were not explicitly requested
- Do not solve the issue by merely renaming Explanation to something else
- Do not remove useful backend fields unless genuinely required; this milestone is about displayed schema fidelity
- Auxiliary product features may remain on the screen, but they must be visually and semantically secondary to the canonical term content
- Prefer polished Compose layouts and strong hierarchy
- Keep the UI production-ready and consistent with the app's design system

## Deliverables
1. Updated Term Details UI aligned with the corrected displayed schema
2. Any required Android model/mapping adjustments if UI assumptions still encode Explanation as canonical
3. Visual separation of schema content vs auxiliary AI-learning sections
4. Summary of files changed
5. Remaining risks, if any
6. Merge readiness assessment

## Acceptance Criteria
- Explanation is no longer presented as a canonical schema section on the details screen
- The term itself is the dominant page identity
- Definition is clearly shown when available
- Humor is shown when available
- See Also is shown cleanly when available
- Tags are shown cleanly when available
- Controversy level is visible and polished
- Slug, if shown, behaves as metadata rather than the main content
- The details screen feels like a glossary-term experience, not a generic detail page
- AI Learning Scenario / Challenge are visually secondary and clearly not part of the canonical term schema
- Browse Terms, Categories, Search, and Details still work correctly after the changes

## Validation Expectations
- Run the Android app manually
- Validate multiple real terms
- Confirm Explanation is gone from the canonical term display area
- Confirm the term identity/header hierarchy feels correct
- Confirm auxiliary AI-learning sections remain functional but clearly secondary
- Validate Browse Terms, Categories, Search, and Details after changes
- Provide concrete manual verification notes with examples

## Important
This milestone is a schema-alignment correction.
The goal is to make the real product behavior match the corrected displayed glossary term format exactly.

## Codex Prompt
```text
Implement the next major follow-up milestone for AI-101: Canonical Term Detail Schema Realignment.

Project context:
- App name: AI-101
- Product: AI Terms Glossary
- Source of truth is backend/database, not YAML
- PR #19 established backend/data integrity and canonical term normalization
- Subsequent Android follow-up work improved canonical term rendering
- Manual validation shows the details screen still does not match the intended displayed schema
- Prior implementation incorrectly treated "Explanation" as part of the canonical displayed schema

Corrected displayed term schema:
The app must not display YAML, but the term details experience must preserve and present the intended displayed schema.

For the displayed canonical term format, treat these as the schema-defining fields/sections:
- term
- definition
- humor
- see_also
- tags
- controversy_level
- optionally slug as metadata if useful

Important correction:
"Explanation" is not part of the displayed canonical term schema.
Do not render Explanation as a first-class schema section in the canonical term details experience.

Observed product issues from manual validation:
- The details screen still includes an Explanation section even though it is not part of the intended displayed schema
- The screen still feels framed as a generic details page instead of a schema-faithful glossary term experience
- The global header still uses generic framing such as "Term Details" / "Canonical glossary entry"
- The real term identity is still not sufficiently driving the overall page structure
- Non-schema extras such as AI Learning Scenario and AI Learning Challenge visually compete with the canonical term content
- Some metadata pill layouts have improved, but the full details experience still mixes schema content and extra feature content too loosely

Mission:
Make the Term Details screen faithfully represent the corrected displayed canonical term schema and clearly separate core glossary content from auxiliary AI-learning features.

Working style requirements:
- Do not use MVP framing
- Do not propose tiny cleanup tasks
- Use a broad, production-ready implementation pass
- Optimize for finished product behavior, not partial technical compliance
- Prioritize schema fidelity and clean product structure

Required outcomes:
1. Audit the Term Details screen against the corrected displayed schema
2. Remove Explanation as a canonical displayed section
3. Ensure the real term identity is the primary page identity
4. Keep Definition as a first-class section when present
5. Keep Humor as a first-class section when present
6. Keep See Also as a first-class section when present
7. Keep Tags as a first-class section when present
8. Keep Controversy Level visible and polished
9. Optionally keep slug as secondary metadata if it improves the glossary feel
10. Reframe the page so generic headers/subheaders do not overpower the real term identity
11. Clearly separate auxiliary AI-learning modules (Scenario, Challenge, etc.) from the canonical term schema area
12. Preserve working app flows:
    - Browse Terms
    - Categories
    - Search
    - Details
13. Avoid unrelated architecture churn

Execution rules:
- Treat this as a schema-fidelity correction pass
- Do not render Explanation as part of the canonical term display contract
- Do not invent new schema sections that were not explicitly requested
- Do not solve the issue by merely renaming Explanation to something else
- Do not remove useful backend fields unless genuinely required; this milestone is about displayed schema fidelity
- Auxiliary product features may remain on the screen, but they must be visually and semantically secondary to the canonical term content
- Prefer polished Compose layouts and strong hierarchy
- Keep the UI production-ready and consistent with the app's design system

Deliverables:
1. Updated Term Details UI aligned with the corrected displayed schema
2. Any required Android model/mapping adjustments if UI assumptions still encode Explanation as canonical
3. Visual separation of schema content vs auxiliary AI-learning sections
4. Summary of files changed
5. Remaining risks, if any
6. Merge readiness assessment

Acceptance criteria:
- Explanation is no longer presented as a canonical schema section on the details screen
- The term itself is the dominant page identity
- Definition is clearly shown when available
- Humor is shown when available
- See Also is shown cleanly when available
- Tags are shown cleanly when available
- Controversy level is visible and polished
- Slug, if shown, behaves as metadata rather than the main content
- The details screen feels like a glossary-term experience, not a generic detail page
- AI Learning Scenario / Challenge are visually secondary and clearly not part of the canonical term schema
- Browse Terms, Categories, Search, and Details still work correctly after the changes

Validation expectations:
- Run the Android app manually
- Validate multiple real terms
- Confirm Explanation is gone from the canonical term display area
- Confirm the term identity/header hierarchy feels correct
- Confirm auxiliary AI-learning sections remain functional but clearly secondary
- Validate Browse Terms, Categories, Search, and Details after changes
- Provide concrete manual verification notes with examples

Important:
This milestone is a schema-alignment correction.
The goal is to make the real product behavior match the corrected displayed glossary term format exactly.
```
