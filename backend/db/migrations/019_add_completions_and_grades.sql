-- Path-centric Completion + Grade per docs/AUDIT.md §2.7.
--
-- Replaces the old learning_completions shape (scenario/challenge with
-- in-app self-grading via task_states/criteria_grades). The new shape:
--   - completions: a user finished a unit on a path. Idempotent on
--     (user_id, unit_id) — re-submitting returns the existing row.
--   - grades: per-criterion grader output for a completion's decision-
--     prompt answer, FK to rubric_criteria so the rubric version that
--     was graded against is unambiguous.
--
-- Per T2-A in STRATEGY.md: per-criterion checklist (Met / Not Met for
-- each criterion). No holistic score.
-- Per T2-B: when the grader is uncertain anywhere, the answer is
-- *flagged* "review needed" — captured here as `flagged BOOLEAN` and
-- per-criterion `confidence`.
--
-- learning_completions and migrations 010/012 are NOT touched here.
-- They retire when the path-centric Completion endpoint replaces the
-- /api/v1/learning-completions endpoint (later Phase 1 PR).

CREATE TABLE IF NOT EXISTS completions (
    id BIGSERIAL PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    path_id TEXT NOT NULL REFERENCES paths(id) ON DELETE CASCADE,
    unit_id TEXT NOT NULL REFERENCES units(id) ON DELETE CASCADE,
    completed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, unit_id)
);

CREATE INDEX IF NOT EXISTS idx_completions_user_completed
    ON completions(user_id, completed_at DESC);
CREATE INDEX IF NOT EXISTS idx_completions_path
    ON completions(path_id);

CREATE TABLE IF NOT EXISTS grades (
    id BIGSERIAL PRIMARY KEY,
    completion_id BIGINT NOT NULL REFERENCES completions(id) ON DELETE CASCADE,
    criterion_id BIGINT NOT NULL REFERENCES rubric_criteria(id) ON DELETE CASCADE,
    met BOOLEAN NOT NULL,
    confidence REAL NOT NULL,
    rationale TEXT NOT NULL DEFAULT '',
    flagged BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (confidence >= 0.0 AND confidence <= 1.0),
    UNIQUE (completion_id, criterion_id)
);

CREATE INDEX IF NOT EXISTS idx_grades_completion
    ON grades(completion_id);
CREATE INDEX IF NOT EXISTS idx_grades_flagged
    ON grades(flagged) WHERE flagged = TRUE;
