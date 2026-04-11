# BROWSE_DETAILS_MVP_BUNDLE.md

## Task

Implement the MVP Browse + Details flow for the FOSS-101 Android app using the existing mock repository.

---

## Project Context

Follow these repo docs as source of truth:

- `docs/roadmap/ROADMAP.md`
- `docs/architecture/BACKEND_DATABASE_SCOPE.md`
- `docs/workflow/AGENTS.md`
- `docs/workflow/TASKS.md`
- `docs/workflow/GEMINI_AGENT_ROADMAP.md`

### Constraints
- MVP only
- Do not add accounts, chat features, AI tools, trend logic, admin features, bookmarks, or backend code
- Keep the implementation compile-safe and Android Studio-friendly
- Use a bounded, focused diff

---

## Goal

Implement a working mock-data Browse Terms flow and matching Term Details flow.

---

## Requirements

1. Use the existing repository abstraction:
   - `GlossaryRepository`
   - `MockGlossaryRepository`

2. Expand the mock glossary dataset to a more realistic MVP sample size if still needed.

3. Update `BrowseTermsScreen` so it:
   - reads glossary terms from `MockGlossaryRepository`
   - shows a scrollable list of terms
   - shows at least `term` and `shortDefinition` for each item

4. Clicking a term must navigate to:
   - `details/{termId}`

5. Update `TermDetailsScreen` so it:
   - accepts `termId`
   - loads the selected term from the repository
   - displays term details
   - shows a simple fallback state if the term is missing

6. Use existing reusable components where appropriate, but do not over-engineer.

7. Keep UI simple and MVP-safe.

8. Do not refactor unrelated screens.

9. Do not touch backend code.

10. Do not expand non-MVP routes except where strictly required for compilation.

---

## Expected Files Likely Involved

- `app/src/main/java/com/example/foss101/ui/browse/BrowseTermsScreen.kt`
- `app/src/main/java/com/example/foss101/ui/details/TermDetailsScreen.kt`
- `app/src/main/java/com/example/foss101/navigation/AppNav.kt`
- `app/src/main/java/com/example/foss101/data/repository/MockGlossaryRepository.kt`

Optional:
- one small reusable glossary item component if helpful

Only touch other files if truly required.

---

## Acceptance Criteria

- `BrowseTermsScreen` compiles
- It uses `MockGlossaryRepository` data
- It shows a scrollable list of glossary terms
- Clicking a term navigates using `details/{termId}`
- `TermDetailsScreen` reads the selected term by id
- Missing term state is handled simply
- Project builds successfully
- Diff remains focused on Browse + Details MVP flow only

---

## Deliverable

Provide a small, focused implementation for the Browse + Details MVP flow.
