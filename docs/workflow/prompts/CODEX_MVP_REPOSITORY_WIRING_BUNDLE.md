# CODEX_MVP_REPOSITORY_WIRING_BUNDLE.md

## Task

Implement the MVP repository wiring cleanup bundle for the FOSS-101 Android app, and prepare the work as a focused GitHub PR.

---

## Project Context

Use these repo documents as source of truth:

- `docs/roadmap/ROADMAP.md`
- `docs/architecture/BACKEND_DATABASE_SCOPE.md`
- `docs/workflow/AGENTS.md`
- `docs/workflow/TASKS.md`
- `docs/workflow/GEMINI_AGENT_ROADMAP.md`

### Constraints
- MVP only
- Do not add accounts, chat features, AI tools, trend logic, admin features, bookmarks, or backend code
- Keep the implementation compile-safe and Android Studio-friendly
- Keep the diff focused and PR-reviewable
- Do not run Gradle commands for this task
- Do not attempt Android build validation in Codex; validation will be performed manually in Android Studio after PR review

---

## Goal

Clean up repository usage across the current MVP screens so the app no longer directly instantiates `MockGlossaryRepository()` inside multiple composables, while keeping the app behavior unchanged and preserving the mock-first approach.

---

## Requirements

1. Refactor the MVP screen flows so direct `MockGlossaryRepository()` creation inside composables is reduced or eliminated where reasonably possible.

2. Keep the app behavior unchanged for:
   - Browse Terms
   - Categories
   - Search
   - Term Details
   - Home

3. Prefer a simple, conservative repository wiring improvement, not a large architecture rewrite.

4. Acceptable approaches include:
   - introducing a lightweight shared repository provider
   - passing repository dependencies in a clean way
   - making the app more ready for future `ApiGlossaryRepository` integration

5. Do not introduce unnecessary complexity.

6. Do not add full ViewModel architecture yet unless it is truly required for the cleanup and remains very limited.

7. Do not change the user-visible behavior of the app except for harmless internal cleanup.

8. Keep current navigation behavior unchanged.

9. Do not touch backend code.

10. Do not refactor unrelated screens or non-MVP flows.

11. Use existing reusable UI components as they are unless a minimal change is required.

---

## Expected Files Likely Involved

- `app/src/main/java/com/example/foss101/data/repository/*`
- `app/src/main/java/com/example/foss101/navigation/AppNav.kt`
- `app/src/main/java/com/example/foss101/ui/browse/BrowseTermsScreen.kt`
- `app/src/main/java/com/example/foss101/ui/categories/CategoriesScreen.kt`
- `app/src/main/java/com/example/foss101/ui/search/SearchScreen.kt`
- `app/src/main/java/com/example/foss101/ui/details/TermDetailsScreen.kt`

Only touch other files if truly required.

---

## Acceptance Criteria

- MVP screens no longer directly instantiate `MockGlossaryRepository()` in multiple composables, or this is meaningfully reduced through a cleaner shared approach
- App behavior remains unchanged
- Browse, Categories, Search, and Details flows continue to work the same way
- The codebase is better prepared for future `ApiGlossaryRepository` integration
- The diff remains focused on repository wiring cleanup only
- No backend code is touched

---

## PR Requirements

- keep the PR focused
- include a short summary of changed files
- mention any assumptions made
- do not include unrelated cleanup

---

## Deliverable

Provide a focused implementation for MVP repository wiring cleanup as a GitHub PR-ready change set.
