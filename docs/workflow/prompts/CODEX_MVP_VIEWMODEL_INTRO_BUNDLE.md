# CODEX_MVP_VIEWMODEL_INTRO_BUNDLE.md

## Task

Implement a lightweight MVP ViewModel introduction bundle for the FOSS-101 Android app, and prepare the work as a focused GitHub PR.

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

Introduce lightweight ViewModel-based state handling for the key MVP flows so the app moves away from keeping most screen state directly inside composables, while preserving current behavior and keeping architecture simple.

---

## Requirements

1. Introduce limited, conservative ViewModel usage for the most relevant MVP screens:
   - Browse Terms
   - Search
   - Term Details
   - optionally Categories if it fits naturally and remains simple

2. Keep the current app behavior unchanged:
   - same visible screens
   - same navigation
   - same repository-backed data
   - same mock-first flow

3. ViewModels should use the existing `GlossaryRepository` abstraction.

4. Do not introduce a large dependency injection framework.

5. Do not over-engineer state models.
   - keep state simple
   - prefer readability over abstraction

6. The app should be more ready for future backend integration, especially for:
   - loading states
   - error states
   - repository swapping

7. Do not change user-visible UI behavior except for harmless internal cleanup.

8. Do not touch backend code.

9. Do not refactor unrelated screens or non-MVP flows.

10. Use existing reusable UI components as they are unless a minimal change is required.

11. Add ViewModel-related dependency only if it is truly needed and keep it minimal.

---

## Expected Files Likely Involved

- `app/src/main/java/com/example/foss101/viewmodel/*`
- `app/src/main/java/com/example/foss101/ui/browse/BrowseTermsScreen.kt`
- `app/src/main/java/com/example/foss101/ui/search/SearchScreen.kt`
- `app/src/main/java/com/example/foss101/ui/details/TermDetailsScreen.kt`
- optionally `app/src/main/java/com/example/foss101/ui/categories/CategoriesScreen.kt`
- `app/build.gradle.kts` only if a minimal ViewModel dependency is required

Only touch other files if truly required.

---

## Acceptance Criteria

- key MVP screen state is meaningfully moved toward ViewModel usage
- current app behavior remains unchanged
- Browse, Search, and Details still work the same way
- repository abstraction remains in use
- the codebase is better prepared for future backend integration
- the diff remains focused on ViewModel introduction only
- no backend code is touched
- no unnecessary architecture complexity is introduced

---

## PR Requirements

- keep the PR focused
- include a short summary of changed files
- mention any assumptions made
- do not include unrelated cleanup

---

## Deliverable

Provide a focused implementation for lightweight MVP ViewModel introduction as a GitHub PR-ready change set.
