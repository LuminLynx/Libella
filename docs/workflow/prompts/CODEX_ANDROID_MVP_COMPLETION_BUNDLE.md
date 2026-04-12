# CODEX_ANDROID_MVP_COMPLETION_BUNDLE.md

## Task

Implement the remaining Android MVP completion bundle for the AI-101 Android app, and prepare the work as a focused GitHub PR.

---

## Source of Truth

Use these repo documents as source of truth:

- `docs/roadmap/ROADMAP.md`
- `docs/architecture/BACKEND_DATABASE_SCOPE.md`
- `docs/workflow/AGENTS.md`
- `docs/workflow/TASKS.md`
- `docs/workflow/GEMINI_AGENT_ROADMAP.md`

---

## Project Direction

This app is now:

- **AI-101**
- **AI Terms Glossary**

Important:
- update app-facing content and mock glossary content toward AI terms
- do **not** rename repository, folders, file names, or package names
- do **not** add out-of-scope MVP features

---

## Goal

Complete the remaining Android MVP foundation in one bundle, while preserving all currently working flows.

---

## Required Scope

This task should include all of the following:

### 1. Refresh mock glossary content to AI-first terms and categories
Replace the current sample/mock glossary direction with AI-focused content.

Expected outcome:
- AI-first glossary terms
- AI-relevant categories
- content suitable for Browse, Categories, Search, and Details
- sample content feels coherent as an AI glossary

### 2. Complete Categories alignment
Bring the Categories flow into alignment with the current architecture direction.

Expected outcome:
- Categories state handling is cleaned up where needed
- ViewModel/state alignment is completed if appropriate
- behavior remains stable for the user

### 3. Standardize loading, empty, and error states across active MVP screens
Apply a consistent approach across:
- Browse
- Categories
- Search
- Details
- Settings if relevant

Expected outcome:
- the screens feel internally consistent
- no unnecessary complexity
- visible behavior remains clean and predictable

### 4. Finalize Settings screen structure for MVP
Review and complete the Settings screen so it is MVP-appropriate.

Expected outcome:
- simple and clean structure
- no expansion into post-MVP features
- compile-safe and navigation-safe

### 5. Verify non-MVP isolation
Keep non-MVP routes and features isolated from the active user flow.

Expected outcome:
- non-MVP screens are not promoted in the active UX
- currently working MVP flow remains the focus

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

- `app/src/main/java/com/example/foss101/data/repository/*`
- `app/src/main/java/com/example/foss101/viewmodel/*`
- `app/src/main/java/com/example/foss101/ui/browse/*`
- `app/src/main/java/com/example/foss101/ui/categories/*`
- `app/src/main/java/com/example/foss101/ui/search/*`
- `app/src/main/java/com/example/foss101/ui/details/*`
- `app/src/main/java/com/example/foss101/ui/settings/*`
- possibly a small number of shared component files if truly needed

Only touch other files if truly required.

---

## Acceptance Criteria

- mock glossary content is clearly AI-first
- Browse, Categories, Search, Details, and Settings still work
- Categories is aligned with the current architecture direction
- loading / empty / error states are more consistent across active MVP screens
- Settings is MVP-ready
- non-MVP flows remain isolated from the active user path
- diff remains focused on remaining Android MVP completion only

---

## PR Requirements

When finished:
- summarize the changed files
- state any assumptions made
- open a focused PR

Do not include unrelated cleanup.
