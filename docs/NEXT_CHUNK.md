# Next chunk: Phase 1 chunk 4

> **How this file works.** Every Phase 1 chunk's PR overwrites this file
> with the next chunk's prompt. When you start a fresh Claude session,
> read `STRATEGY.md`, `EXECUTION.md`, `AUDIT.md`, **and this file**. The
> prompt below is self-contained — paste it as-is to begin chunk 4.
> Chunk 4's PR will overwrite this file with chunk 5's prompt, etc.

---

## Prompt to paste into a new Claude session

```
Read docs/STRATEGY.md, docs/EXECUTION.md, and docs/AUDIT.md for
context (skim only — strategy is locked). Then read
docs/NEXT_CHUNK.md (this file) to confirm scope.

Then execute Phase 1 chunk 4: authoring pipeline scaffolding +
schema linter + CI bootstrap.

Goal: ship the file-based authoring pipeline (markdown templates
for the 9-slot unit anatomy + a schema linter that CI runs against
every authored unit), and bring up the GitHub Actions CI workflow
that has been deferred since chunk 2. CI is bound into this chunk
because the linter must run in CI to be load-bearing.

Scope:

1. Markdown template for one unit, at content/units/_TEMPLATE.md
   (or wherever reads best). It must include all 9 anatomy slots
   from STRATEGY.md § Unit anatomy:
     1. Title
     2. Single-sentence definition
     3. Trade-off framing (when this matters / when this breaks /
        what it costs)
     4. 90-second bite (the read)
     5. Depth (longer reader OR interactive widget OR both)
     6. Calibration tags on key claims
     7. Sources (primary preferred, dated)
     8. Decision prompt + authored rubric
     9. Prereq pointers
   Front-matter: YAML at the top of the file with id, slug,
   path_id, position, prereq_unit_ids, status. Body: the bite +
   depth + decision prompt as markdown sections. Sources + rubric +
   calibration tags as YAML structures.

2. Schema linter at backend/scripts/lint_unit_markdown.py
   (replaces the deleted audit_term_schema.py). Validates:
   - all 9 anatomy slots present
   - calibration tags use only the three tiers ('settled',
     'contested', 'unsettled')
   - every source has a non-empty url, title, and date
   - rubric is well-formed (>= 1 criterion, each criterion has
     a non-empty text)
   - prereq_unit_ids reference units that exist (or are listed
     elsewhere in the same lint pass — accept either)
   Exit code: 0 on clean, non-zero on any violation. Print the
   file path + line number + reason for each violation.

3. Linter tests at backend/tests/test_unit_markdown_linter.py:
   - Positive: one clean fixture under
     backend/tests/fixtures/units/clean/ that lints with zero
     errors.
   - Negative: one fixture per failure mode under
     backend/tests/fixtures/units/broken/ — missing slot,
     malformed tier, undated source, empty rubric, prereq pointing
     at unknown unit. Each must fail with the specific named error.

4. CI bootstrap at .github/workflows/ci.yml. Two jobs run on every
   PR to main and every push to main:
   - backend: ubuntu-latest, set up Python 3.11, pip install -r
     backend/requirements.txt, then run:
       pytest backend/tests/
       python -m backend.scripts.lint_unit_markdown content/units/
   - android: ubuntu-latest, set up JDK 21, run gradle assembleDebug.
   Cache strategies: actions/cache for pip and ~/.gradle so warm
   runs stay under a couple minutes.

5. One stub unit at content/units/tokenization-bundle-0.md that
   lints clean (the Phase 1 canary from EXECUTION.md). Use
   tokenization as the seed (per EXECUTION.md Phase 2: "Recommended
   choice: Tokenization. Bundle 0 has reusable raw material for the
   bite"). All 9 slots filled, including a real rubric (>= 3
   criteria) and a primary-source URL with a date. No regression
   pairs yet — that's Phase 2.

Out of scope (do NOT do):

- Do not load the stub unit into the database. The DB ingest path
  is chunk 5 (backend repos + endpoints). Chunk 4 is file-only.
- Do not introduce a Path entity author flow. The single seeded
  path will land in chunk 5 alongside the unit ingest.
- Do not implement the grader. That is Phase 2.
- Do not edit migrations 016–021. They land in chunk 3 and are
  immutable from then on.
- Do not touch Android. Chunk 7 is the Android wiring.
- Do not modify docs/STRATEGY.md, docs/EXECUTION.md, or
  docs/AUDIT.md.

Discipline:

- One PR. Branch suggestion:
  claude/phase-1-authoring-and-ci-XXXXX. Do not auto-merge.
- pytest must pass (5/6 in the sandbox is fine; document the
  pre-existing passlib/bcrypt failure as PRs #48–#52 did).
- The new linter tests run as part of pytest backend/tests/.
- CI must pass on the PR before merge — if the workflow has a
  configuration error, fix it in this PR rather than deferring.
- Overwrite docs/NEXT_CHUNK.md with chunk 5's prompt as part of
  this chunk's PR. The forcing function only works if every chunk
  ships the next.

When done, report:
- Files created (template, linter, fixtures, workflow, stub unit)
- pytest result (with the new linter tests visible)
- CI workflow status on the PR (green/red and which job)
- Cascade effects (e.g. content/ as a new top-level directory,
  any .gitignore updates needed)
- The PR URL
```

---

## Why CI is bound to chunk 4

Chunk 2 (CI bootstrap on its own) was deferred because the test surface
was too thin to justify the setup overhead. Chunk 4 introduces the
schema linter, which is only load-bearing if it runs automatically on
every authored unit. CI is therefore a precondition for chunk 4 to ship,
not a separate piece of work.

This is the forcing function: the deferred work is wired into a chunk
the founder will execute anyway, so it cannot be forgotten.

## Subsequent chunks (preview)

After chunk 4 lands, this file will be overwritten with chunk 5's
prompt. The remaining sequence:

- **Chunk 5** — Backend path-centric repos + endpoints.
  `PathRepository`, `UnitRepository`, `CompletionRepository`. New
  endpoints: `GET /api/v1/paths/{id}`, `GET /api/v1/units/{id}`,
  `POST /api/v1/completions`. Replaces the RESHAPE-pending
  `/api/v1/learning-completions` endpoint and retires migrations
  010 / 012.
- **Chunk 6** — Stub unit ingest into the database. Seeds the
  Tokenization unit + the canonical "LLM Systems for PMs" path so
  the Android client has real data to render in chunk 7.
- **Chunk 7** — Android F1 (path home / "Continue") + F2 (unit
  reader: bite + depth on tap) + F7 (extend JWT auth for path
  progress). Replaces the current Home tile grid. Phase 1 exit
  criteria from EXECUTION.md are met after this chunk.

Phase 2 (the grader, F4) opens after chunk 7.
