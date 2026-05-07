# Next chunk: Phase 1 chunk 5

> **How this file works.** Every Phase 1 chunk's PR overwrites this file
> with the next chunk's prompt. When you start a fresh Claude session,
> read `STRATEGY.md`, `EXECUTION.md`, `AUDIT.md`, **and this file**. The
> prompt below is self-contained — paste it as-is to begin chunk 5.
> Chunk 5's PR will overwrite this file with chunk 6's prompt, etc.

---

## Prompt to paste into a new Claude session

```
Read docs/STRATEGY.md, docs/EXECUTION.md, and docs/AUDIT.md for
context (skim only — strategy is locked). Then read
docs/NEXT_CHUNK.md (this file) to confirm scope.

Then execute Phase 1 chunk 5: backend path-centric repositories +
endpoints. This is the server-side spine that the Android client
in chunk 7 will consume.

Goal: replace the term-centric backend surface (per AUDIT §2.3 and
§2.4) with the path-centric one. Three repositories backed by the
migrations 016–021 schema, three new endpoints exposing them, and
retirement of the two RESHAPE-pending pieces called out in AUDIT
(`POST /api/v1/learning-completions` and migrations 010 / 012).

Scope:

1. Repositories at backend/app/repositories/ — one module each:
   - PathRepository: get_path(path_id), list_paths(),
     next_unit_for_user(user_id, path_id).
   - UnitRepository: get_unit(unit_id) returning the full 9-slot
     payload (unit row + sources + calibration_tags +
     decision_prompt + rubric + rubric_criteria),
     list_units_for_path(path_id).
   - CompletionRepository: record_completion(user_id, unit_id),
     list_completions(user_id), idempotent on re-submit (don't
     duplicate if the same user+unit pair already exists).
   Each repository is a thin module of pure functions over a psycopg
   connection — no ORM, follow the style of the existing
   backend/app/repository.py.

2. Endpoints in backend/app/main.py (or split into a routers module
   if main.py is getting unwieldy):
   - GET /api/v1/paths/{path_id} — returns the path with its
     sequenced unit list (id, slug, title, position, status only —
     the unit list is a manifest, not the full payload).
   - GET /api/v1/units/{unit_id} — returns the full 9-slot unit
     payload. JWT-protected.
   - POST /api/v1/completions — body { unit_id }; records a
     completion for the authenticated user. Returns the completion
     row. JWT-protected.

3. Retirement:
   - Delete the existing POST /api/v1/learning-completions endpoint
     (per AUDIT §2.4 RESHAPE-pending).
   - Add a new forward migration (022_drop_learning_completions.sql)
     that drops the learning_completions table and any leftover
     references. Migrations 010 and 012 themselves remain immutable
     history per AUDIT §5 #5; this migration retires the table they
     created.

4. Tests at backend/tests/:
   - test_path_repository.py, test_unit_repository.py,
     test_completion_repository.py — repository-level integration
     tests against a real Postgres test DB (gated on a
     TEST_DATABASE_URL env var; skipped cleanly when not present,
     consistent with the existing backend test patterns). For each:
     a fixture seeds a path + 2 units + sources/tags/rubric, then
     asserts the read shape and idempotency behavior.
   - test_endpoints.py — FastAPI TestClient covering the three new
     endpoints, including auth-required paths and the 404 shape.

Out of scope (do NOT do):

- Do not seed real unit data into the database — that is chunk 6.
  Tests may seed fixtures inside the test DB, but no production
  seed runs in this chunk.
- Do not implement the grader. Phase 2.
- Do not touch Android. Chunk 7.
- Do not edit migrations 010, 012, 013–015, or 016–021. They are
  immutable history. Add 022 (and only 022) as a new forward
  migration.
- Do not modify docs/STRATEGY.md, docs/EXECUTION.md, or
  docs/AUDIT.md.
- Do not modify the schema linter or markdown template authored
  in chunk 4.

Discipline:

- One PR. Branch suggestion:
  claude/phase-1-path-repos-and-endpoints-XXXXX. Do not auto-merge.
- pytest must pass on the new repository + endpoint tests. Skipping
  the Postgres-gated tests when TEST_DATABASE_URL is unset is fine;
  they must run in CI when the env is provided.
- The CI workflow from chunk 4 must continue to pass (backend job +
  android job both green).
- Overwrite docs/NEXT_CHUNK.md with chunk 6's prompt as part of
  this chunk's PR.

When done, report:
- Files created (repositories, routers, migration 022, tests)
- pytest result (with the new tests visible; gated tests skipped
  when TEST_DATABASE_URL is unset is acceptable)
- CI workflow status on the PR (green/red and which job)
- Endpoint shapes (a sample JSON response for each, in the PR
  description)
- The PR URL
```

---

## Subsequent chunks (preview)

After chunk 5 lands, this file will be overwritten with chunk 6's
prompt. The remaining sequence:

- **Chunk 6** — Stub unit ingest into the database. Seeds the
  Tokenization unit + the canonical "LLM Systems for PMs" path so
  the Android client has real data to render in chunk 7.
- **Chunk 7** — Android F1 (path home / "Continue") + F2 (unit
  reader: bite + depth on tap) + F7 (extend JWT auth for path
  progress). Replaces the current Home tile grid. Phase 1 exit
  criteria from EXECUTION.md are met after this chunk.

Phase 2 (the grader, F4) opens after chunk 7.
