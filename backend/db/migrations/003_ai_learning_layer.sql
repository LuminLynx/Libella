CREATE TABLE IF NOT EXISTS ai_generated_content (
    id BIGSERIAL PRIMARY KEY,
    term_id TEXT NOT NULL REFERENCES terms(id) ON DELETE CASCADE,
    content_type TEXT NOT NULL,
    content_json JSONB NOT NULL,
    model_name TEXT,
    provider TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (term_id, content_type)
);

CREATE INDEX IF NOT EXISTS idx_ai_generated_term_type
    ON ai_generated_content(term_id, content_type);
