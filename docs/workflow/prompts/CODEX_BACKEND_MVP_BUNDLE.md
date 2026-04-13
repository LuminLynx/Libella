# CODEX_BACKEND_MVP_BUNDLE.md

## Task

Implement the full backend MVP for the AI-101 project and prepare the work as a focused GitHub PR.

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

This project is now:

- **AI-101**
- **AI Terms Glossary**

Important:
- do **not** rename repository, folders, file names, or package names unless explicitly required
- do **not** add out-of-scope MVP features
- do **not** add user accounts, chat, AI-answer-generation, trend features, admin features, or bookmarks
- this task is about building the real backend MVP capability

---

## Goal

Build the backend MVP in one broad, coherent bundle so the project has a real backend foundation for glossary data.

This task should create a material product capability milestone.

---

## Required Scope

This task should include all of the following:

### 1. Create the backend project skeleton
If a backend project already exists, extend it.
If no backend project exists yet, scaffold one in a clean, isolated backend area of the repo.

Expected outcome:
- clear backend entry point
- clear package / folder structure
- configuration structure appropriate for MVP
- minimal and maintainable setup

Important:
- choose the most conservative, lightweight implementation path that fits the existing repo and docs
- if a backend stack must be chosen because none exists yet, state that assumption clearly in the PR

### 2. Create the database schema for glossary content
Implement the initial schema required for the glossary core.

Expected outcome:
- categories table / model
- glossary terms table / model
- related-terms structure if included
- appropriate identifiers and basic metadata fields
- schema aligned with the approved backend/database scope docs

### 3. Add seed data for AI glossary content
Create seed data that matches the AI-first glossary direction.

Expected outcome:
- AI-focused categories
- AI-focused glossary terms
- seed content coherent with the current Android mock content direction
- enough data to support Browse, Categories, Search, and Details

### 4. Implement the approved MVP glossary endpoints
Add backend support for the approved glossary operations.

Expected outcome:
- terms list endpoint
- term details endpoint
- categories list endpoint
- category-filtered terms endpoint
- term search endpoint

### 5. Standardize backend response shape
Keep responses clean, predictable, and easy for Android integration.

Expected outcome:
- stable response structure
- consistent naming
- not-found handling
- empty-result handling
- clear error response baseline

### 6. Keep the backend MVP focused
Do not turn this into a production-hardening or admin-platform task.

Expected outcome:
- backend is usable for Android integration
- backend remains within MVP scope
- no speculative platform expansion

---

## Constraints

- Keep the diff focused and PR-ready
- Do not add authentication, accounts, chat, admin tooling, AI generation, trend ingestion, analytics platforms, or unrelated infrastructure
- Do not refactor unrelated Android files
- Do not attempt production deployment work
- Prefer the simplest stack and implementation path consistent with the repo and source-of-truth docs
- If a backend stack choice is necessary because none exists, keep it minimal and explain the assumption
- Do not include secrets or credentials
- Keep the backend easy to connect from the Android app in the next milestone

---

## Expected Files Likely Involved

Depending on the current repo state, likely areas include:

- a backend project directory or module
- backend source files
- backend config files
- database schema / migration / seed files
- backend route / handler / service files
- backend model files
- backend repository / data-access files
- backend README or minimal usage notes only if truly needed

Only touch Android files if absolutely necessary for shared contracts or documentation alignment.

---

## Acceptance Criteria

- backend project skeleton exists and is coherent
- glossary schema exists for categories and terms
- AI-first seed data exists
- approved MVP glossary endpoints exist
- response and error handling are reasonably standardized
- backend scope remains disciplined and MVP-only
- diff remains focused on backend MVP only

---

## PR Requirements

When finished:
- summarize the changed files
- state any assumptions made
- explain the chosen backend stack if one had to be introduced
- open a focused PR

Do not include unrelated cleanup.
