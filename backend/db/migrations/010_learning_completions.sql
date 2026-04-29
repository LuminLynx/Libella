CREATE TABLE IF NOT EXISTS learning_completions (
    id BIGSERIAL PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    term_id TEXT NOT NULL REFERENCES terms(id) ON DELETE CASCADE,
    artifact_type TEXT NOT NULL,
    confidence TEXT NOT NULL,
    reflection_notes TEXT,
    completed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (artifact_type IN ('scenario', 'challenge')),
    CHECK (confidence IN ('low', 'medium', 'high'))
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_learning_completions_user_term_type
    ON learning_completions(user_id, term_id, artifact_type);

CREATE INDEX IF NOT EXISTS idx_learning_completions_user_completed
    ON learning_completions(user_id, completed_at DESC);
