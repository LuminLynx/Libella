# CODEX_BACKEND_POSTGRES_DEPLOYMENT_BUNDLE.md

## Task

Replace the current SQLite-based backend persistence with PostgreSQL and prepare the backend for real internet deployment, while keeping the glossary API contract stable for the Android app.

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
- do **not** rename repository, folders, file names, or package names unless strictly required
- do **not** add out-of-scope features
- this task is about making the backend production-oriented enough for public deployment
- keep the Android-facing API contract stable

---

## Goal

Upgrade the backend from SQLite to PostgreSQL and prepare it for deployment to a public internet endpoint, while preserving the glossary API behavior already introduced.

This task should create a real production-facing backend milestone.

---

## Required Scope

This task should include all of the following:

### 1. Replace SQLite persistence with PostgreSQL
Move backend data access and configuration from SQLite to PostgreSQL.

Expected outcome:
- PostgreSQL connection path using environment-based configuration
- no dependence on local SQLite files for the active backend path
- database access layer updated accordingly
- backend still supports the same glossary operations

### 2. Migrate schema to PostgreSQL
Implement the glossary schema in a PostgreSQL-compatible way.

Expected outcome:
- categories storage
- terms storage
- related-term support
- useful indexes
- schema aligned with current backend contract and glossary scope

### 3. Migrate seed/setup flow to PostgreSQL
Keep backend startup and data seeding practical.

Expected outcome:
- seed content remains AI-first
- seed/setup path works against PostgreSQL
- local/dev bootstrap remains clear
- no admin panel added

### 4. Preserve and stabilize the existing API contract
Do not break the Android remote integration path.

Expected outcome:
- same approved MVP endpoints
- same `{ data, error }` response envelope
- same not-found behavior for missing term IDs
- same search/category/details/list behavior

### 5. Add deployment-oriented configuration
Prepare the backend for a public deployment target.

Expected outcome:
- environment-variable based config for database and runtime settings
- clear startup command/path
- no secrets committed to the repo
- repo is ready for deployment to a public platform such as Railway

### 6. Keep the task focused
Do not turn this into a full platform rewrite.

Expected outcome:
- no unrelated Android changes
- no auth/accounts
- no admin system
- no analytics platform
- no speculative infrastructure sprawl

---

## Constraints

- Keep the diff focused and PR-ready
- Do not change unrelated Android files
- Do not add accounts, chat, AI-answer-generation, trend features, admin features, bookmarks, or unrelated infrastructure
- Keep the public API contract stable for Android
- Prefer the simplest PostgreSQL implementation path that is reviewable and deployable
- Do not include secrets or credentials
- State all environment variable assumptions clearly in the PR

---

## Expected Files Likely Involved

- `backend/requirements.txt`
- `backend/app/config.py`
- `backend/app/db.py`
- `backend/app/main.py`
- `backend/app/repository.py`
- `backend/db/schema.sql`
- `backend/db/seed.sql`
- `backend/scripts/*`
- `backend/README.md`
- deployment/config helper files only if truly needed

Only touch Android files if absolutely necessary to preserve API contract awareness.

---

## Acceptance Criteria

- backend uses PostgreSQL instead of SQLite for the active path
- schema and seed flow work for PostgreSQL
- glossary endpoints remain stable
- response envelope remains stable
- repo is prepared for public deployment
- no secrets are committed
- diff remains focused on PostgreSQL migration and deployment readiness

---

## PR Requirements

When finished:
- summarize the changed files
- state any assumptions made
- explain the PostgreSQL configuration path and environment variables used
- state how deployment is expected to work
- open a focused PR

Do not include unrelated cleanup.
