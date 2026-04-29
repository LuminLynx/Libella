ALTER TABLE learning_completions
    ADD COLUMN IF NOT EXISTS task_states JSONB,
    ADD COLUMN IF NOT EXISTS challenge_response TEXT,
    ADD COLUMN IF NOT EXISTS criteria_grades JSONB,
    ADD COLUMN IF NOT EXISTS earned_points INTEGER NOT NULL DEFAULT 0;
