# AI-101 — Term Details Schema Clean Fix

## Codex Prompt
```text
Fresh task. Do not continue or build on the previous stacked follow-up implementation line. Treat this as a narrowly scoped fix for the Term Details screen only.

Fix the Term Details screen so it matches the corrected displayed canonical schema exactly.

Goal:
Make the term details experience schema-faithful and production-ready.

Displayed canonical term format:
- term
- definition
- humor
- see_also
- tags
- controversy_level
- optionally slug as secondary metadata

Important correction:
- Explanation is NOT part of the displayed canonical schema.
- Do not render Explanation as part of the canonical term display.
- Do not simply rename it.

Requirements:
1. Make the actual term name the primary page identity.
2. Show Definition as a first-class section.
3. Show Humor when present.
4. Show See Also when present.
5. Show Tags when present.
6. Show Controversy Level clearly.
7. Keep slug only as secondary metadata if useful.
8. AI Learning Scenario / AI Learning Challenge may remain, but must be clearly secondary optional modules and not part of the canonical term schema.
9. The screen must feel like a glossary-term experience, not a generic details page.
10. Preserve existing Browse Terms, Categories, Search, and Details flows.
11. Keep the implementation narrowly scoped. Do not stack unrelated follow-ups or broaden the branch.

Acceptance criteria:
- term name is prominent
- Explanation is gone from the canonical term area
- Definition is visible
- Humor / See Also / Tags / Controversy render cleanly when present
- AI Learning modules are visually secondary
- no unrelated churn

Validation:
Run the app and manually verify multiple term detail screens.
Provide concrete verification notes.
```
