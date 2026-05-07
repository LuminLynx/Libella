-- Grader regression discipline (T2 in STRATEGY.md, F4 in EXECUTION.md).
--
-- Every published unit ships with answer-grade pairs that the regression
-- runner feeds through the grader to measure agreement with human
-- judgment. Tiered hold per AUDIT.md §2.7:
--   - flagship units: >= 20 pairs
--   - standard units: >= 10 pairs
--   - never zero
--
-- This table is the storage shape; the regression runner that consumes
-- it is Phase 2 work. The schema is minimal on purpose: a sample answer,
-- the expected per-criterion grade JSON, who graded it, and where the
-- pair came from (authored, beta-cohort feedback, etc.).
--
-- expected_per_criterion is JSONB because the criterion set is rubric-
-- versioned and varies per unit. Schema:
--   {"<criterion_id>": {"met": true, "confidence": 0.9}, ...}
-- Runtime validation lives in the regression runner, not here.

CREATE TABLE IF NOT EXISTS regression_pairs (
    id BIGSERIAL PRIMARY KEY,
    unit_id TEXT NOT NULL REFERENCES units(id) ON DELETE CASCADE,
    answer_md TEXT NOT NULL,
    expected_per_criterion JSONB NOT NULL,
    human_grader TEXT NOT NULL,
    source TEXT NOT NULL DEFAULT 'authored',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (LENGTH(TRIM(answer_md)) > 0),
    CHECK (jsonb_typeof(expected_per_criterion) = 'object'),
    CHECK (LENGTH(TRIM(human_grader)) > 0),
    CHECK (source IN ('authored', 'beta_cohort', 'user_feedback'))
);

CREATE INDEX IF NOT EXISTS idx_regression_pairs_unit
    ON regression_pairs(unit_id);
