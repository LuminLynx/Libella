# AI-101 — PostgreSQL Production Hardening Follow-up Task

## Task Type
Follow-up of the previous Codex task.

This is **not** a new milestone. It completes the same unfinished milestone: **production-grade PostgreSQL backend hardening**.

## Why this follow-up exists
The previous Codex pass completed a substantial portion of the PostgreSQL migration, including PostgreSQL configuration, `psycopg` database access, PostgreSQL-compatible SQL updates, and deployment-oriented documentation.

However, based on the task summary, the production milestone is still **not merge-ready** because:

- runtime schema initialization still appears to happen on startup
- startup seeding still appears to happen automatically when tables are empty
- no proper migration system or migration files were reported
- validation was too weak for production merge readiness
- there may be a logging/security concern if connection URLs are printed without redaction

## Objective
Finish the PostgreSQL migration properly and make the backend **merge-ready for production deployment**.

## Required outcomes
1. Remove production runtime schema creation from app startup.
2. Remove production runtime auto-seeding from app startup.
3. Introduce a proper migration system with explicit migration files.
4. Ensure schema changes are applied through an explicit migration command, not implicit startup behavior.
5. Keep any seed/bootstrap behavior explicit and operator-invoked, or strictly gated to local/dev only.
6. Verify that these API flows still work unchanged against PostgreSQL:
   - Browse
   - Categories
   - Details
   - Search via `/api/v1/search/terms`
7. Ensure logs and scripts do not expose raw `DATABASE_URL` credentials.
8. Keep the Android-facing API contract unchanged.
9. Assess whether `term_relations` is harmless schema-only scope drift or should be split into a separate PR.

## Non-goals
- No unrelated cleanup.
- No MVP framing.
- No micro-task decomposition.
- No API contract redesign unless absolutely required.
- No speculative refactor unrelated to production deployment safety.

## Merge blockers to fix
### Blocker 1 — Startup-managed schema lifecycle
The app should **not** create or initialize production schema implicitly on startup.

### Blocker 2 — Startup-managed seed lifecycle
The app should **not** auto-seed production data just because tables are empty.

### Blocker 3 — Missing migration system
A production backend needs explicit migrations with a clear operator workflow.

### Blocker 4 — Weak verification
`compileall` and grep checks are not enough. PostgreSQL-backed verification is required for the live API flows.

### Blocker 5 — Possible credential leakage
Any logging or script output that includes a raw connection URL must be removed or redacted.

## Expected implementation scope
- Add migration tooling appropriate to the backend stack.
- Create baseline migration files for the current PostgreSQL schema.
- Refactor startup so production does not perform implicit schema creation.
- Refactor seed strategy so it is explicit and environment-safe.
- Validate unchanged API behavior end to end on PostgreSQL.
- Update deployment docs with exact migration and seed workflow for Railway.

## Acceptance criteria
The work is ready for merge only if all of the following are true:

- production startup does not create schema automatically
- production startup does not auto-seed automatically
- migrations exist and are runnable explicitly
- PostgreSQL-backed verification confirms Browse, Categories, Details, and Search all still work
- Android-facing API envelope and response mapping remain unchanged
- no raw credentials are printed in logs or scripts
- any scope drift is clearly identified and justified

## Codex prompt
```text
Review commit f6245cd for AI-101 backend and finish the PostgreSQL production-hardening milestone.

Current situation:
- PostgreSQL config and psycopg migration were implemented
- Runtime still appears to initialize schema on startup and seed when tables are empty
- Validation reported so far is compileall + grep, which is not enough for merge
- term_relations was added during the migration task; preserve only if it is inert and does not change API behavior

Your mission:
Make this branch merge-ready for production backend deployment.

Required outcomes:
1. Remove production runtime schema creation and production runtime auto-seeding from app startup
2. Introduce a proper migration system with explicit migration files and a documented migration command
3. Keep any bootstrap/seed flow explicit and operator-invoked, or strictly gated to local/dev only
4. Add PostgreSQL-backed verification for these unchanged API flows:
   - browse
   - categories
   - details
   - search via /api/v1/search/terms
5. Ensure logs never expose raw DATABASE_URL credentials
6. Keep Android-facing API contract unchanged
7. Flag whether term_relations is harmless schema-only scope drift or should be split out

Deliverables:
- migration tooling and migration files
- startup path cleaned for production safety
- explicit seed strategy
- PostgreSQL smoke/integration verification
- concise handoff note with:
  - blockers fixed
  - exact commands to run locally
  - exact Railway deploy/migration steps
  - remaining risks, if any

Do not do unrelated cleanup.
Do not reintroduce SQLite fallback behavior into production code paths.
Optimize for merge readiness, not partial progress.
```

## Handoff note
This task should be treated as the **completion pass** for the current backend milestone, not the start of a new milestone.
