# Phase 1: complete

> **Phase 1 closeout.** Chunks 1–7 have all landed. The Phase 1 exit
> criteria from `docs/EXECUTION.md` are met:
>
> - Path-centric data model on Postgres (migrations 016–021, plus 022
>   retiring `learning_completions`).
> - Authoring pipeline: schema linter + markdown template (chunk 4),
>   ingest script with `--check` guardrail (chunk 6), one canonical
>   path "LLM Systems for PMs" with the Tokenization unit ingested.
> - Backend endpoints: `GET /api/v1/paths/{id}`, `GET /api/v1/units/{id}`,
>   `POST /api/v1/completions` (chunk 5).
> - Android client: path home with "Continue", unit reader with bite +
>   depth on tap, completion submission, JWT propagation through every
>   path/unit/completion call (chunk 7).
>
> The chunk-prompt rotation that drove Phase 1 is over. There is no
> chunk 8.

---

## What's next: Phase 2

The next chunk opens **Phase 2 — the grader (F4).** That's a
separate planning pass against `docs/STRATEGY.md` (Loop step 3 and
the T2-A / T2-B principles) and `docs/AUDIT.md` § the grader. The
shape:

- Decision-prompt answers from F2 are graded against the unit's
  authored rubric criteria (already loaded by chunk 6's ingest).
- Per-criterion `Met / Not Met` checklist with confidence — no
  holistic score.
- Low-confidence answers are flagged "review needed" rather than
  silently passed.
- The data model is already in place: `grades` (from migration 019)
  FKs into `rubric_criteria`, and chunk 6's append-only rubric
  versioning preserves grade history across re-ingests.

When you're ready to start Phase 2, plan it from STRATEGY +
EXECUTION + AUDIT directly — there is no `NEXT_CHUNK` prompt to
paste, because Phase 2 is a different shape of work (model + grader
service + Android grading UI) than Phase 1's discrete chunks.

---

## Operational note for any release between Phase 1 and Phase 2

Whenever the production database is wiped or freshly provisioned,
run the ingest before pointing the Android client at it:

```
DATABASE_URL=postgres://...  python -m backend.scripts.ingest_units content/units/
```

Without this step, the path home renders an empty list. The ingest
is idempotent; running it on a populated DB is safe.
