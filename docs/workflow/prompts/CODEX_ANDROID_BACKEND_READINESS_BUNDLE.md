# CODEX_ANDROID_BACKEND_READINESS_BUNDLE.md

## Task

Implement the Android backend-readiness bundle for the AI-101 Android app, and prepare the work as a focused GitHub PR.

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
- do **not** build backend code in this task
- this task is about making the Android app genuinely ready for backend integration

---

## Goal

Prepare the Android app for real backend integration in one broad, coherent bundle, while preserving all currently working MVP flows.

This task should create real implementation progress, not just internal cleanup.

---

## Required Scope

This task should include all of the following:

### 1. Define Android remote data models for glossary content
Introduce the remote DTO/model layer needed for backend responses.

Expected outcome:
- clear remote models for terms and categories
- shape aligned to the approved backend/documentation scope
- separation between remote models and app/domain models where appropriate

### 2. Define Android API service interfaces for glossary endpoints
Prepare the Android-side service contract for the backend MVP.

Expected outcome:
- service/interface definitions for:
  - terms list
  - term details
  - categories list
  - category-filtered terms
  - search terms
- structure ready for later real implementation

### 3. Add the Android network/client foundation
Introduce the minimum client/network layer needed for backend integration readiness.

Expected outcome:
- clean network/client setup
- minimal and intentional dependencies only if truly needed
- no unnecessary architecture/framework sprawl

### 4. Prepare the repository swap path from mock to API
Make the repository layer cleanly ready for remote integration.

Expected outcome:
- clear path from `MockGlossaryRepository` to `ApiGlossaryRepository`
- repository abstraction remains the central access point
- no UI screen should need major rewrites later just to switch data sources

### 5. Align screen state for backend-ready loading/error handling
Prepare current MVP flows for remote-backed states.

Expected outcome:
- Browse, Categories, Search, and Details are ready for real remote loading/error behavior
- current visible UX remains stable
- do not break working flows

### 6. Preserve current app behavior
Do not turn this into a visible redesign task.

Expected outcome:
- the app should remain functionally familiar
- this is a capability-enabling milestone
- visible regressions are unacceptable

---

## Constraints

- Keep the diff focused and PR-ready
- Do not add accounts, chat, AI-answer-generation, trend features, admin features, bookmarks, or backend code
- Do not refactor unrelated screens
- Do not run Gradle commands for this task
- Do not attempt Android build validation in Codex
- Validation will be performed manually in Android Studio after PR review
- Preserve all currently working MVP flows

---

## Expected Files Likely Involved

- `app/build.gradle.kts` only if minimal dependencies are truly required
- `app/src/main/java/com/example/foss101/data/remote/*`
- `app/src/main/java/com/example/foss101/data/repository/*`
- `app/src/main/java/com/example/foss101/viewmodel/*`
- `app/src/main/java/com/example/foss101/ui/browse/*`
- `app/src/main/java/com/example/foss101/ui/categories/*`
- `app/src/main/java/com/example/foss101/ui/search/*`
- `app/src/main/java/com/example/foss101/ui/details/*`
- `app/src/main/java/com/example/foss101/navigation/*`

Only touch other files if truly required.

---

## Acceptance Criteria

- Android remote data models exist and are coherent
- Android API service interfaces exist for the approved glossary endpoints
- a clean network/client foundation is added if required
- repository wiring is clearly ready for later `ApiGlossaryRepository`
- MVP screens are more backend-ready without visible regressions
- diff remains focused on Android backend-readiness only

---

## PR Requirements

When finished:
- summarize the changed files
- state any assumptions made
- open a focused PR

Do not include unrelated cleanup.
