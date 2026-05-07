-- Spaced review (F5 in EXECUTION.md, Loop step 6 in STRATEGY.md).
--
-- Without spaced review, the path evaporates (STRATEGY.md is explicit on
-- this). Spaced reviews appear the next day alongside the next new unit.
--
-- Algorithm choice (SM-2, FSRS, simpler interval doubling) is deferred
-- to Phase 3 per EXECUTION.md. This table is shape-only — whichever
-- algorithm Phase 3 picks must populate `due_at`, `interval_days`, and
-- `last_reviewed_at` on every review tick.
--
-- "interval" is a SQL reserved word in some dialects; we use
-- `interval_days INTEGER` to avoid quoting and be explicit about units.

CREATE TABLE IF NOT EXISTS review_schedule (
    id BIGSERIAL PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    unit_id TEXT NOT NULL REFERENCES units(id) ON DELETE CASCADE,
    due_at TIMESTAMPTZ NOT NULL,
    interval_days INTEGER NOT NULL DEFAULT 1,
    last_reviewed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (interval_days >= 0),
    UNIQUE (user_id, unit_id)
);

CREATE INDEX IF NOT EXISTS idx_review_schedule_due
    ON review_schedule(user_id, due_at);
