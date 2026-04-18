# AI-101 — Canonical Term Detail Completion and Content Fidelity

## Milestone
Canonical Term Detail Completion and Content Fidelity.

## Task Type
Large implementation pass.

This is a product-completion follow-up milestone for AI-101.

## Objective
Complete the Term Details experience so it faithfully presents the canonical glossary term structure, and audit whether missing sections are caused by client rendering gaps, backend payload gaps, or incomplete seeded content.

## Project Context
- App name: AI-101
- Product: AI Terms Glossary
- Source of truth is backend/database, not YAML
- PR #19 established backend/data integrity and canonical term normalization
- A later Android task promoted canonical term fields into the client and improved term rendering
- However, the Term Details experience still does not fully honor the intended canonical term format in real app behavior

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
The app must not display YAML, but a term must preserve and present the same logical structure as the canonical schema above.

## Observed Product Issues from Manual App Validation
- Term Details still feels partial rather than schema-faithful
- The actual term identity is not surfaced strongly enough in the detail header
- Definition is not clearly presented as its own dedicated section
- Humor is not visibly represented in the observed term details experience
- See Also presentation is still constrained/truncated
- Tag chips and related chips can render awkwardly in narrow layouts
- The detail screen still does not convincingly present the term as a complete structured glossary object

## Mission
Complete the Term Details experience so it faithfully presents the canonical glossary term structure, and audit whether missing sections are caused by client rendering gaps, backend payload gaps, or incomplete seeded content.

## Working Style Requirements
- Do not use MVP framing
- Do not propose tiny cleanup tasks
- Use a broad, production-ready implementation pass
- Optimize for finished product behavior, not partial technical compliance
- Prefer end-to-end fidelity over transitional workaround logic

## Required Outcomes
1. Audit the live term detail data path end to end:
   - backend response
   - Android DTO/domain mapping
   - UI rendering
   - seeded/database content completeness
2. Ensure the detail screen prominently presents the real term identity
3. Render Definition as a first-class dedicated section when present
4. Render Explanation as a distinct section, not as a substitute for Definition
5. Render Humor as a distinct section when present
6. Render See Also completely and cleanly, without arbitrary low caps that hide canonical structure
7. Render Tags cleanly with better wrapping/spacing behavior
8. Render Controversy Level cleanly and consistently
9. Optionally show slug as metadata if it improves the structured glossary feel
10. Determine whether any missing sections are due to:
    - absent backend fields
    - mapping issues
    - incomplete seed/content data
11. If data completeness is the issue, fix the content/source layer rather than masking the issue in UI
12. Preserve existing working flows:
    - Browse Terms
    - Categories
    - Search
    - Details
13. Avoid unrelated architecture churn

## Execution Rules
- Treat this as a product-completion follow-up
- Do not solve missing sections with fake placeholder content
- Do not collapse definition and explanation into one field unless the canonical source truly only has one
- Do not over-truncate chips or related-term labels on the detail screen
- Card/list surfaces may remain summary-oriented, but the detail screen must be schema-faithful
- Prefer polished Compose layouts that wrap well on narrow mobile screens
- Keep compatibility logic only where necessary; bias toward canonical behavior
- If seed/content is insufficient, fix it explicitly rather than pretending the UI is complete

## Deliverables
1. Updated Term Details UI that fully reflects canonical structure
2. Any required Android model/mapping adjustments
3. Any required backend/content/seed follow-up changes if missing data is the real blocker
4. Summary of files changed
5. Remaining risks, if any
6. Merge readiness assessment

## Acceptance Criteria
- The detail screen clearly presents the real term as a structured glossary object
- The term name is prominent and not reduced to a generic header experience
- Definition appears as its own section when available
- Explanation appears as its own section when available
- Humor appears when available
- See Also is presented without artificial low-item truncation
- Tags render cleanly without awkward chip clipping/wrapping
- Controversy level is visible and polished
- Missing optional sections are handled gracefully
- If canonical content is missing in the dataset, that issue is surfaced and corrected
- The final result matches the intended canonical term contract much more faithfully than the current app

## Validation Expectations
- Run the Android app manually
- Verify multiple real terms, not just one
- Check detail screens across terms with different field completeness
- Confirm whether missing sections are absent in payload or absent in UI
- Validate Browse Terms, Categories, Search, and Details after changes
- Provide concrete manual verification notes with examples

## Important
This is not just a styling pass.
This milestone exists to make the actual product behavior finally match the intended canonical glossary term structure.

## Codex Prompt
```text
Implement the next major follow-up milestone for AI-101: Canonical Term Detail Completion and Content Fidelity.

Project context:
- App name: AI-101
- Product: AI Terms Glossary
- Source of truth is backend/database, not YAML
- PR #19 established backend/data integrity and canonical term normalization
- A later Android task promoted canonical term fields into the client and improved term rendering
- However, the Term Details experience still does not fully honor the intended canonical term format in real app behavior

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
The app must not display YAML, but a term must preserve and present the same logical structure as the canonical schema above.

Observed product issues from manual app validation:
- Term Details still feels partial rather than schema-faithful
- The actual term identity is not surfaced strongly enough in the detail header
- Definition is not clearly presented as its own dedicated section
- Humor is not visibly represented in the observed term details experience
- See Also presentation is still constrained/truncated
- Tag chips and related chips can render awkwardly in narrow layouts
- The detail screen still does not convincingly present the term as a complete structured glossary object

Mission:
Complete the Term Details experience so it faithfully presents the canonical glossary term structure, and audit whether missing sections are caused by client rendering gaps, backend payload gaps, or incomplete seeded content.

Working style requirements:
- Do not use MVP framing
- Do not propose tiny cleanup tasks
- Use a broad, production-ready implementation pass
- Optimize for finished product behavior, not partial technical compliance
- Prefer end-to-end fidelity over transitional workaround logic

Required outcomes:
1. Audit the live term detail data path end to end:
   - backend response
   - Android DTO/domain mapping
   - UI rendering
   - seeded/database content completeness
2. Ensure the detail screen prominently presents the real term identity
3. Render Definition as a first-class dedicated section when present
4. Render Explanation as a distinct section, not as a substitute for Definition
5. Render Humor as a distinct section when present
6. Render See Also completely and cleanly, without arbitrary low caps that hide canonical structure
7. Render Tags cleanly with better wrapping/spacing behavior
8. Render Controversy Level cleanly and consistently
9. Optionally show slug as metadata if it improves the structured glossary feel
10. Determine whether any missing sections are due to:
    - absent backend fields
    - mapping issues
    - incomplete seed/content data
11. If data completeness is the issue, fix the content/source layer rather than masking the issue in UI
12. Preserve existing working flows:
    - Browse Terms
    - Categories
    - Search
    - Details
13. Avoid unrelated architecture churn

Execution rules:
- Treat this as a product-completion follow-up
- Do not solve missing sections with fake placeholder content
- Do not collapse definition and explanation into one field unless the canonical source truly only has one
- Do not over-truncate chips or related-term labels on the detail screen
- Card/list surfaces may remain summary-oriented, but the detail screen must be schema-faithful
- Prefer polished Compose layouts that wrap well on narrow mobile screens
- Keep compatibility logic only where necessary; bias toward canonical behavior
- If seed/content is insufficient, fix it explicitly rather than pretending the UI is complete

Deliverables:
1. Updated Term Details UI that fully reflects canonical structure
2. Any required Android model/mapping adjustments
3. Any required backend/content/seed follow-up changes if missing data is the real blocker
4. Summary of files changed
5. Remaining risks, if any
6. Merge readiness assessment

Acceptance criteria:
- The detail screen clearly presents the real term as a structured glossary object
- The term name is prominent and not reduced to a generic header experience
- Definition appears as its own section when available
- Explanation appears as its own section when available
- Humor appears when available
- See Also is presented without artificial low-item truncation
- Tags render cleanly without awkward chip clipping/wrapping
- Controversy level is visible and polished
- Missing optional sections are handled gracefully
- If canonical content is missing in the dataset, that issue is surfaced and corrected
- The final result matches the intended canonical term contract much more faithfully than the current app

Validation expectations:
- Run the Android app manually
- Verify multiple real terms, not just one
- Check detail screens across terms with different field completeness
- Confirm whether missing sections are absent in payload or absent in UI
- Validate Browse Terms, Categories, Search, and Details after changes
- Provide concrete manual verification notes with examples

Important:
This is not just a styling pass.
This milestone exists to make the actual product behavior finally match the intended canonical glossary term structure.
```
