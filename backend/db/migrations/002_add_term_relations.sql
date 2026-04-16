CREATE TABLE IF NOT EXISTS term_relations (
    term_id TEXT NOT NULL REFERENCES terms(id) ON DELETE CASCADE,
    related_term_id TEXT NOT NULL REFERENCES terms(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (term_id, related_term_id),
    CHECK (term_id <> related_term_id)
);

CREATE INDEX IF NOT EXISTS idx_term_relations_term_id ON term_relations(term_id);
CREATE INDEX IF NOT EXISTS idx_term_relations_related_term_id ON term_relations(related_term_id);
