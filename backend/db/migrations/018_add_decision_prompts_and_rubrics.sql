-- Unit anatomy slot 8 (decision prompt + authored rubric) per docs/AUDIT.md §2.7.
--
-- The decision step is the wedge: open-ended user answer, LLM-graded
-- against an authored rubric, with the rubric and grader confidence
-- visible to the user (Loop step 3 in STRATEGY.md). One decision prompt
-- per unit; multiple versions of a rubric may exist over time.
--
-- rubrics carries both a versioned JSON snapshot of the full rubric (for
-- portability and audit) and a normalized rubric_criteria table that
-- grades.criterion_id will FK into (migration 019). Both shapes coexist
-- by design — the JSON snapshot is the authoring source of truth; the
-- normalized rows are the runtime FK target for grader output.

CREATE TABLE IF NOT EXISTS decision_prompts (
    id BIGSERIAL PRIMARY KEY,
    unit_id TEXT NOT NULL REFERENCES units(id) ON DELETE CASCADE,
    prompt_md TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (LENGTH(TRIM(prompt_md)) > 0),
    UNIQUE (unit_id)
);

CREATE TABLE IF NOT EXISTS rubrics (
    id BIGSERIAL PRIMARY KEY,
    decision_prompt_id BIGINT NOT NULL REFERENCES decision_prompts(id) ON DELETE CASCADE,
    version INTEGER NOT NULL DEFAULT 1,
    rubric_json JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (version >= 1),
    CHECK (jsonb_typeof(rubric_json) = 'object'),
    UNIQUE (decision_prompt_id, version)
);

CREATE INDEX IF NOT EXISTS idx_rubrics_decision_prompt
    ON rubrics(decision_prompt_id, version DESC);

CREATE TABLE IF NOT EXISTS rubric_criteria (
    id BIGSERIAL PRIMARY KEY,
    rubric_id BIGINT NOT NULL REFERENCES rubrics(id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    criterion_text TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (position >= 1),
    CHECK (LENGTH(TRIM(criterion_text)) > 0),
    UNIQUE (rubric_id, position)
);

CREATE INDEX IF NOT EXISTS idx_rubric_criteria_rubric
    ON rubric_criteria(rubric_id, position);
