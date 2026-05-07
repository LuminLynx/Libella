# Next chunk: Phase 1 chunk 6

> **How this file works.** Every Phase 1 chunk's PR overwrites this file
> with the next chunk's prompt. When you start a fresh Claude session,
> read `STRATEGY.md`, `EXECUTION.md`, `AUDIT.md`, **and this file**. The
> prompt below is self-contained — paste it as-is to begin chunk 6.
> Chunk 6's PR will overwrite this file with chunk 7's prompt, etc.

---

## Prompt to paste into a new Claude session

```
Read docs/STRATEGY.md, docs/EXECUTION.md, and docs/AUDIT.md for
context (skim only — strategy is locked). Then read
docs/NEXT_CHUNK.md (this file) to confirm scope.

Then execute Phase 1 chunk 6: stub unit ingest into the database.

Goal: load the Tokenization stub unit (authored in chunk 4 at
content/units/tokenization-bundle-0.md) and the canonical
"LLM Systems for PMs" path into the database, so the path-centric
endpoints from chunk 5 return real data when chunk 7 wires the
Android client.

Scope:

1. Ingest script at backend/scripts/ingest_units.py
   - Reads every authored unit under content/units/ (skip files
     starting with `_`, same rule as the chunk 4 linter).
   - Lints each file with the chunk-4 linter first; refuses to
     ingest if any unit fails the schema check.
   - Idempotent. Re-running with no changes is a no-op (UPSERT on
     the path id and on each unit id; for sources / calibration
     tags / decision prompt / rubric: replace-on-write per unit so
     authoring edits propagate cleanly without leftover rows).
   - Run as: python -m backend.scripts.ingest_units content/units/
   - Exit codes: 0 on clean ingest, 1 on linter failure or DB error.

2. Path seed: a single canonical path "LLM Systems for PMs" with
   id `llm-systems-for-pms` (matches `path_id` in the authored
   tokenization-bundle-0.md). The path row is created/updated by
   the same ingest run if it doesn't exist; do not require a
   separate seed step. Title and description live as constants in
   the ingest script (one path in v1).

3. Tests at backend/tests/test_ingest_units.py
   - Postgres-gated on TEST_DATABASE_URL (skip cleanly when unset,
     consistent with chunk 5).
   - Cover: clean ingest creates the path + unit + sources + tags
     + decision prompt + rubric + criteria; second run is a no-op
     (no duplicates, no churned rows); editing a source URL in a
     fixture and re-ingesting replaces the source row cleanly.

4. CI wiring:
   - Extend the backend job in .github/workflows/ci.yml to run a
     dry-run / lint-only check of the ingest path against the
     stub unit (not a real DB ingest in CI; the linter already
     covers schema correctness, but a `python -m
     backend.scripts.ingest_units --check` mode that parses every
     file and validates path-id consistency without writing is
     useful as a guardrail). Implement `--check` in the script.

Out of scope (do NOT do):

- Do not implement the grader. Phase 2.
- Do not touch Android. Chunk 7.
- Do not edit migrations 010, 012, 013–015, 016–021, or 022. They
  are immutable history.
- Do not add new endpoints. The chunk-5 endpoints already cover
  what the Android client needs.
- Do not modify docs/STRATEGY.md, docs/EXECUTION.md, or
  docs/AUDIT.md.
- Do not modify the schema linter or markdown template authored
  in chunk 4, or the repositories / endpoints authored in chunk 5.
- Do not author new unit content beyond what's already in
  content/units/. Tokenization is the only Phase 1 canary.

Discipline:

- One PR. Branch suggestion:
  claude/phase-1-stub-unit-ingest-XXXXX. Do not auto-merge.
- pytest must pass; the new ingest tests must run when
  TEST_DATABASE_URL is set, and skip cleanly otherwise.
- The CI workflow from chunks 4–5 must continue to pass (backend
  job + android job both green), with the new --check step added.
- Overwrite docs/NEXT_CHUNK.md with chunk 7's prompt as part of
  this chunk's PR.

When done, report:
- Files created (ingest script, tests, CI workflow diff)
- pytest result (with the new tests visible; gated tests skipped
  when TEST_DATABASE_URL is unset is acceptable)
- CI workflow status on the PR (green/red and which job)
- Sample of what the path + unit endpoints return after ingest
  (in the PR description)
- The PR URL
```

---

## Subsequent chunks (preview)

After chunk 6 lands, this file will be overwritten with chunk 7's
prompt. The remaining sequence:

- **Chunk 7** — Android F1 (path home / "Continue") + F2 (unit
  reader: bite + depth on tap) + F7 (extend JWT auth for path
  progress). Replaces the current Home tile grid. Phase 1 exit
  criteria from EXECUTION.md are met after this chunk.

Phase 2 (the grader, F4) opens after chunk 7.
