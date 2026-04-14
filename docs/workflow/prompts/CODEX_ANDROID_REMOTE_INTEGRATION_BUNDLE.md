# CODEX_ANDROID_REMOTE_INTEGRATION_BUNDLE.md

## Task

Implement the Android remote-integration bundle for the AI-101 Android app, and prepare the work as a focused GitHub PR.

---

## Source of Truth

Use these repo documents as source of truth:

- `docs/roadmap/ROADMAP.md`
- `docs/architecture/BACKEND_DATABASE_SCOPE.md`
- `docs/workflow/AGENTS.md`
- `docs/workflow/TASKS.md`
- `docs/workflow/GEMINI_AGENT_ROADMAP.md`
- `docs/workflow/CODEX_EXECUTION_ROADMAP.md`

---

## Project Direction

This app is now:

- **AI-101**
- **AI Terms Glossary**

Important:
- do **not** rename repository, folders, file names, or package names
- do **not** add out-of-scope MVP features
- do **not** change backend code in this task
- this task is about making the Android app consume the real backend

---

## Goal

Connect the Android app to the real backend MVP in one broad, coherent bundle, while preserving current app behavior and keeping the diff reviewable.

This task should create a material product capability milestone:
the app should stop depending on mock-only data for active MVP flows.

---

## Required Scope

This task should include all of the following:

### 1. Implement the real Android HTTP client path
Complete the Android network/service factory so API calls can be made to the backend.

Expected outcome:
- `GlossaryApiServiceFactory` creates a real service implementation
- backend response envelope `{ data, error }` is handled correctly
- minimal and intentional networking implementation only
- no unnecessary framework sprawl

### 2. Wire Android to the backend through `ApiGlossaryRepository`
Use the existing repository abstraction to connect active MVP flows to backend data.

Expected outcome:
- `ApiGlossaryRepository` becomes the real remote data source
- Browse, Categories, Search, and Details use backend-backed repository behavior
- repository abstraction remains the single access path for UI/ViewModels

### 3. Make backend base URL configuration practical for local development
Keep local testing realistic and reviewable.

Expected outcome:
- API base URL is explicit and easy to change
- do **not** hardcode a device-specific one-off solution deep inside the code
- keep configuration simple for emulator / local-network testing
- if a default is chosen, document the assumption in the PR

### 4. Switch the app from mock mode to API mode in a controlled way
Do not leave the main app path on mock data after this task.

Expected outcome:
- `RepositoryProvider` is switched to the API-backed path for the active app flow
- mock fallback can remain available only if useful and clearly isolated
- active user flow should now use the real backend

### 5. Keep loading, empty, and error behavior stable
Remote integration must not make the app feel broken.

Expected outcome:
- Browse, Categories, Search, and Details still handle loading / empty / error states cleanly
- no regression in navigation flow
- category selection, search, and details navigation still work correctly

### 6. Preserve scope discipline
Do not turn this into a redesign or backend refactor task.

Expected outcome:
- no backend file changes
- no unrelated Android refactors
- no post-MVP features
- focused remote integration only

---

## Constraints

- Keep the diff focused and PR-ready
- Do not change backend code
- Do not add accounts, chat, AI-answer-generation, trend features, admin features, bookmarks, or unrelated infrastructure
- Do not refactor unrelated screens
- Do not run Gradle commands for this task
- Do not attempt Android build validation in Codex
- Validation will be performed manually in Android Studio after PR review
- Preserve currently working MVP navigation and UX patterns as much as possible

---

## Expected Files Likely Involved

- `app/build.gradle.kts` only if minimal networking dependencies are truly required
- `app/src/main/java/com/example/foss101/data/remote/*`
- `app/src/main/java/com/example/foss101/data/repository/*`
- `app/src/main/java/com/example/foss101/viewmodel/*`
- `app/src/main/java/com/example/foss101/ui/browse/*`
- `app/src/main/java/com/example/foss101/ui/categories/*`
- `app/src/main/java/com/example/foss101/ui/search/*`
- `app/src/main/java/com/example/foss101/ui/details/*`

Only touch other files if truly required.

---

## Acceptance Criteria

- Android can call the backend successfully
- Browse uses real backend term data
- Categories uses real backend category and category-term data
- Search uses real backend search results
- Details uses real backend term-detail data
- backend envelope parsing works correctly
- active app flow is no longer mock-only
- loading / empty / error behavior remains reasonable
- diff remains focused on Android remote integration only

---

## PR Requirements

When finished:
- summarize the changed files
- state any assumptions made
- state what backend base URL assumption or configuration path was used
- open a focused PR

Do not include unrelated cleanup.
