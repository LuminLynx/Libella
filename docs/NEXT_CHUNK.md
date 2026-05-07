# Next chunk: Phase 1 chunk 7

> **How this file works.** Every Phase 1 chunk's PR overwrites this file
> with the next chunk's prompt. When you start a fresh Claude session,
> read `STRATEGY.md`, `EXECUTION.md`, `AUDIT.md`, **and this file**. The
> prompt below is self-contained — paste it as-is to begin chunk 7.
> Chunk 7 is the final Phase 1 chunk; on landing, Phase 1 closes and
> Phase 2 (the grader, F4) opens.

---

## Prompt to paste into a new Claude session

```
Read docs/STRATEGY.md, docs/EXECUTION.md, and docs/AUDIT.md for
context (skim only — strategy is locked). Then read
docs/NEXT_CHUNK.md (this file) to confirm scope.

Then execute Phase 1 chunk 7: Android F1 (path home / "Continue") +
F2 (unit reader: bite + depth on tap) + F7 (extend JWT auth so the
client carries the token through path/unit calls). This is the
last Phase 1 chunk. After it lands, Phase 1 exit criteria from
EXECUTION.md are met and Phase 2 (the grader) opens.

The backend spine is already in place:
- chunk 5 shipped GET /api/v1/paths/{id}, GET /api/v1/units/{id},
  POST /api/v1/completions (JWT-protected).
- chunk 6 shipped the ingest script and a CI guardrail; one canonical
  path (`llm-systems-for-pms`) with the Tokenization unit is the
  only authored content in v1.

Scope:

1. F1 — Path home / "Continue":
   - Replace the current Home tile grid (app/src/main/.../ui/home/
     HomeScreen.kt and its surrounding nav) with a path-home screen
     that:
       a. Loads the canonical path (`llm-systems-for-pms`) on entry
          via GET /api/v1/paths/{path_id}.
       b. Renders the path title + sequenced unit list (use the
          manifest fields: id/slug/title/position/status).
       c. Surfaces a single primary "Continue" CTA that resolves to
          the next uncompleted unit for the signed-in user. Use the
          unit list from the path payload + a local read of the
          user's completed unit ids (a small client cache backed
          by POST /api/v1/completions return values is fine for v1).
       d. Tapping any unit row opens the unit reader (F2). Tapping
          "Continue" opens the resolved next unit.
   - Strip glossary tiles from the home surface; the glossary side
     door (chunk 2's collapse) stays reachable from settings or
     equivalent secondary nav, not from the path home.

2. F2 — Unit reader (bite → depth on tap):
   - New screen for a single unit fetched via
     GET /api/v1/units/{unit_id}. Renders the 9-slot payload:
       - title + definition (one-sentence)
       - trade-off framing
       - 90-second bite (always visible)
       - depth (collapsed behind a "Depth" disclosure; expands on
         tap — STRATEGY.md "bite + depth on tap")
       - calibration tags (claim + tier chip)
       - sources (url + title + date; tappable opens browser)
       - decision prompt (visible — the wedge for Phase 2's grader)
   - "Mark complete" CTA at the bottom posts to
     POST /api/v1/completions with the unit id and updates the
     in-memory completed set so "Continue" advances on return to
     home.

3. F7 — JWT propagation:
   - The chunk-3-era auth flow already issues + persists a JWT.
     Extend the API client to attach `Authorization: Bearer <jwt>`
     to every path / unit / completion call. 401 from the server
     should bubble up as a recoverable auth error (kick to login),
     not crash. No new auth screens.

4. Tests:
   - Unit tests in app/src/test/.../ for the new ViewModels (path
     home + unit reader): cover the loading / loaded / error states
     using a fake repository that returns canned payloads. No
     Espresso / instrumented tests required — the F1/F2 wiring is
     compose-friendly enough that view-model tests give real
     coverage.
   - Backend tests stay as they are; chunk 7 does not touch the
     backend.

5. CI:
   - The android job in .github/workflows/ci.yml must continue to
     pass (assembleDebug + the new unit tests). If you add a
     `testDebugUnitTest` step, ensure it runs after assemble.

Out of scope (do NOT do):

- Do not implement the grader. Phase 2.
- Do not author new unit content. Tokenization stays the only
  Phase 1 unit.
- Do not edit migrations 010, 012, 013–015, 016–021, or 022. They
  are immutable history.
- Do not edit chunk 5's repositories / endpoints, chunk 4's linter
  / template, or chunk 6's ingest script.
- Do not modify docs/STRATEGY.md, docs/EXECUTION.md, or
  docs/AUDIT.md.
- Do not add tablet/landscape adaptive layouts; v1 is phone
  portrait per EXECUTION.md.

Discipline:

- One PR. Branch suggestion:
  claude/phase-1-android-path-and-unit-reader-XXXXX. Do not
  auto-merge.
- gradlew assembleDebug must succeed. New unit tests must pass.
- The backend job + android job in CI must both stay green.
- This is the last chunk in Phase 1. Overwrite docs/NEXT_CHUNK.md
  with a Phase 1 closeout note pointing to Phase 2 (the grader,
  F4) — the file should make it clear that Phase 1 chunks are
  complete and the next chunk is Phase 2.

When done, report:
- Files created / modified (Android sources, tests, CI workflow
  diff if any)
- gradlew assembleDebug + testDebugUnitTest results
- CI workflow status on the PR (green/red and which job)
- Screenshots or compose previews if available, otherwise a brief
  description of the path home + unit reader rendering against
  the canonical Tokenization unit
- The PR URL
```

---

## What's after chunk 7

Phase 1 closes when chunk 7 lands. The Phase 1 exit criteria from
`docs/EXECUTION.md` — path-centric data model, authoring pipeline,
backend endpoints, ingest, and an Android client that renders a
real unit end-to-end — are all satisfied at that point.

Phase 2 opens with F4 (the grader): authored rubric + LLM-graded
open-ended decision prompt with rubric + grader confidence visible
to the user. That's a separate planning pass — STRATEGY.md is the
source of truth for the Loop step 3 design.
