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

---

## Post-chunk-7 polish (PR ledger)

A short stretch of polish PRs landed after chunk 7 closed Phase 1's
structural deliverables. They surfaced from real-device smoke tests
(phone + tablet) and one Play-Store-readiness pass. Logged here so
the post-chunk delta from spec is visible in one place.

| PR | Type | Notes |
|----|------|-------|
| #59 | Aligned | Chunk 7 review fixes — restored Settings access from path home, made auth-expired nav a one-shot Channel event, scoped completion cache per user. |
| #60 | Correction | Markdown rendering in unit reader — chunk 7 shipped raw text; EXECUTION.md L74 had explicitly required `markdown-rendered`. |
| #61 | Aligned | Friendlier 401 message; collapsed trade-off framing behind a disclosure (P4 "bite first, depth on tap"). |
| #62 | Correction | Re-ordered unit reader to **Bite → Decide → Calibrate** per STRATEGY.md Loop (chunk 7 had used the 9-slot anatomy order, which primes consensus answers before the question). Marked `docs/roadmap/ROADMAP.md` as historical. |
| #63 | Bug fix | Unit reader loads completion state from cache on open; clean back-stack handling on auth-expired redirect. |
| **#64** | **Extension** | Cross-device completion sync. **Adds `GET /api/v1/completions`** — not in chunk 7's spec, which deferred this as "v1 cache is fine." Doesn't contradict any locked decision; goes beyond. Phase 2 will inherit the same shape for grades. |
| #65 | Bug fix | Encrypted-prefs crash on restored installs. Added Play-Store-aligned backup rules + recovery in `EncryptedTokenStorage`. |
| #66 | Bug fix + new doc | Path-home reload on every resume so cross-device sync runs after sign-in. **Added `docs/ANDROID_BEST_PRACTICES.md`** capturing Android-specific decisions for Play Store readiness — see that file for the full ledger and maintenance discipline. |

**Net spec impact:**
- Two corrections of chunk-7 drift (#60, #62) brought us back to spec.
- One extension (#64) added one auth-gated GET endpoint. Doesn't violate anything in STRATEGY/EXECUTION/AUDIT; flagged here so it's visible at the Phase 2 boundary.
- One new doc category (`ANDROID_BEST_PRACTICES.md`) — complementary to existing source-of-truth docs, not a rewrite of any of them.
- Zero modifications to STRATEGY.md, EXECUTION.md, or AUDIT.md.
