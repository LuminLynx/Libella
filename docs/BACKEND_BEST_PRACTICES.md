# Backend Best Practices & Launch-Readiness Checklist

> **Scope.** Backend-side decisions (FastAPI / Postgres / Anthropic
> grader) made during Phase 1 and Phase 2 to keep the service
> production-ready. Complementary to `STRATEGY.md` (product strategy),
> `EXECUTION.md` (phase sequencing), and `ANDROID_BEST_PRACTICES.md`
> (Android client decisions); not a substitute for any of them.

---

## How this file is maintained

**Update in the same PR** that makes the change when the PR:

- Introduces a new architectural decision (auth strategy, migration
  framework, AI provider integration shape, persistence layer).
- Reverses or significantly modifies an existing decision in this doc.
- Touches deploy-relevant config (env vars, startup gating,
  release-command pattern, secret handling, scaling assumptions).
- Adds a new backend subsystem with a *why-is-it-shaped-this-way*
  justification (background workers, scheduled jobs, cache layers).

**Skip the update for** bug fixes that don't change the underlying
decision, content tweaks, test-only changes, pure refactors, or
one-off polish.

**PR discipline.** The PR description should explicitly say either
*"adds §N to BACKEND_BEST_PRACTICES.md"* or *"no documented decisions
change."* Reviewers ask "should this be in the best-practices doc?"
during review.

**Periodic audit.** At each phase boundary, sweep the file: confirm
each section still matches the code, retire entries replaced by new
decisions, refresh external links if upstream docs moved.

---

## 1. Production-secret enforcement (`APP_ENV=production` gate)

**Decision.** `validate_production_config()` runs at FastAPI startup
and refuses to boot when `APP_ENV=production` is set but any of these
config values are still defaults:

- `JWT_SECRET` is the literal `"change-me-in-production"`.
- `POSTGRES_PASSWORD` is the literal `"postgres"`.
- `AI_PROVIDER_API_KEY` is empty.

In any other environment (development, test, ci, staging — anything
other than the literal string `"production"`) the gate is a no-op so
local work and CI are never blocked.

**Why.** Dev-friendly defaults coexist with production-grade
deployments only when there's an explicit barrier between them. The
bare `os.getenv(..., "default")` pattern can ship a misconfigured
deploy with weak credentials silently. A startup-time gate makes the
misconfiguration loud and fast.

**Operator action.** Set `APP_ENV=production` on the deploy platform
(Railway: Variables tab on the API service). Existing required vars
(`DATABASE_URL`, `JWT_SECRET`, `AI_PROVIDER_API_KEY`) should already
be set; the gate just enforces it.

**Source.** [PR introducing this section][prod-hardening-pr].

[prod-hardening-pr]: https://github.com/LuminLynx/FOSS-101/pulls?q=is:pr+production+hardening

---

## 2. Migrations off the web request-serving path

**Decision.** Two layers:

1. **Recommended deploy pattern**: run
   `python -m backend.scripts.migrate` as a release / pre-deploy
   command (Railway "Pre-Deploy Command", Heroku release phase, etc.).
   That applies migrations once, before any web instance starts
   accepting traffic. Idempotent — skips migrations already in
   `schema_migrations`.

2. **Belt-and-suspenders**: `run_migrations()` is also called from
   FastAPI's `on_startup` hook (legacy behavior). Both code paths
   share the same `run_migrations()`, which now wraps the work in a
   Postgres transactional advisory lock (`pg_advisory_xact_lock`).
   Two web instances starting simultaneously serialize on the lock;
   only one applies pending migrations.

**Why.** Running migrations purely on `on_startup` is a race under
horizontal scale (two cold-start workers can begin applying the same
migration simultaneously, locking each other out or producing partial
state). Moving them to an explicit release command also moves
migration failure detection *before* any traffic hits the new code,
which matters once the service has real users.

The advisory lock is the safety net for any deploy that hasn't been
configured with a release command yet.

**Operator action (recommended).** Add a Railway Pre-Deploy Command:

```
python -m backend.scripts.migrate
```

After that, on_startup migrations effectively become a no-op
verification on each instance.

**Source.** [PR introducing this section][prod-hardening-pr].

---

## 3. F4 grader output validation (T2-D guardrails)

**Decision.** `app/ai_service.py:_validate_grader_output` enforces the
four T2-D guardrails on every Anthropic response before any grade is
persisted. If any guardrail fails, the service raises
`AIServiceError`, the endpoint returns 502, and **no completion or
grade rows are written**.

The guardrails:

1. **Strict tool-call schema.** `tool_choice` forces the model to
   emit a `submit_grades` call. Free-text content is ignored.
2. **Source-grounding.** Cached system prompt contains the unit's
   bite + depth + trade-off framing + sources + rubric criteria. The
   grader has no path to invent claims from prior knowledge.
3. **Answer-quote requirement.** A `met=true` grade with empty
   `answer_quote` is rejected. The grader must point at the span of
   the answer that supports the judgement.
4. **Structured-only output.** The parser reads exclusively the
   tool-use block; any side-text is discarded.

Plus the validator overrides `flagged=true` whenever any criterion's
confidence is below `CONFIDENCE_FLAG_THRESHOLD` (0.6), to compensate
for the model under-reporting its own uncertainty.

**Why.** STRATEGY.md § T2 is explicit: the grader has to be provably
calibrated, not just superficially competent. The validator turns
calibration into a structural property — a payload that fails any
guardrail can't reach the database.

**Source.** PR #67 (P2.1 grader service).

---

## 4. Per-completion grade replacement on re-grade

**Decision.** `grade_repository.upsert_grades` DELETEs every existing
row for the completion whose `criterion_id` is *not* in the incoming
submission, then UPSERTs each incoming grade. All in one transaction.

**Why.** `record_completion` is idempotent on `(user_id, unit_id)` —
re-grading the same unit reuses the same `completion_id`. A naive
INSERT … ON CONFLICT only refreshes rows whose criterion id is in
the new submission. Chunk 6's append-only rubric versioning gives a
re-authored rubric a fresh set of criterion ids; without the DELETE
step, the grades table would accumulate stale rows for retired
criteria and reads would return a mixed old/new result with the wrong
criterion count.

**Source.** PR #67 review fix.

---

## 5. Anthropic prompt caching on the rubric (T2-E)

**Decision.** The grader's system prompt is split into two text blocks:

- The static instructions and tool schema reference (uncached).
- The unit's bite + depth + trade-off framing + sources + rubric
  criteria, marked with `cache_control: {"type": "ephemeral"}`
  (cached).

Re-grading the same unit (the common case during a regression-set
run) reuses cached tokens at ~10% of the normal input rate.

**Why.** STRATEGY.md § T2-E explicitly calls out prompt caching as
"load-bearing on unit economics." Concrete validation from the first
P2.2 live run: cache reads (6,645 tokens) ≈ 3.2× input tokens (2,066)
across 4 grading calls, which puts cost per call at roughly $0.01 vs
the ~$0.03 it would have been without caching.

**Source.** PR #67 (P2.1 grader service); cost numbers from the
first live regression run logged in this thread.

---

## Launch-readiness checklist (Phase 4)

These are deferred items called out in code review but not action-able
yet — they belong on the Phase 4 launch readiness sweep
(EXECUTION.md § Phase 4) and should be re-read at that boundary.

- **Token TTL + refresh flow.** Access tokens are 30-day; consider
  shorter-lived access + refresh-token rotation for blast-radius
  reduction. Not load-bearing for closed beta.
- **Token revocation.** No `jti`/denylist or rotating token version
  today. Add for "log out all devices" / account-compromise response.
- **Connection pooling.** `db.py` opens a fresh psycopg connection
  per request. `psycopg_pool` is a small drop-in. Becomes meaningful
  under real concurrent load — not under closed-beta traffic.
- **Flavor-based base URLs (Android).** Client has one hardcoded
  Railway URL. Phase 4 wants dev / staging / prod build flavors plus
  a release-time check that debug endpoints don't ship in production
  builds. Tracked also in `ANDROID_BEST_PRACTICES.md`.
- **Streaming the rationale (TT2).** STRATEGY.md flags this as a UX
  improvement; not a gate criterion. Can land any time.

**Audit at each phase boundary.** When closing a phase, re-read this
list, mark items still pending, retire items shipped or invalidated.

---

## Reading list

- [FastAPI deployment notes](https://fastapi.tiangolo.com/deployment/)
- [Postgres advisory locks](https://www.postgresql.org/docs/current/explicit-locking.html#ADVISORY-LOCKS)
- [Anthropic prompt caching](https://docs.anthropic.com/claude/docs/prompt-caching)
- [Railway release / pre-deploy commands](https://docs.railway.app/deploy/deployments)
