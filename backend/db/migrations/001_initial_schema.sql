CREATE TABLE IF NOT EXISTS categories (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    description TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS terms (
    id TEXT PRIMARY KEY,
    term TEXT NOT NULL UNIQUE,
    short_definition TEXT NOT NULL,
    full_explanation TEXT NOT NULL,
    category_id TEXT NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    tags TEXT NOT NULL DEFAULT '',
    related_terms TEXT NOT NULL DEFAULT '',
    example_usage TEXT,
    source TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS term_relations (
    term_id TEXT NOT NULL REFERENCES terms(id) ON DELETE CASCADE,
    related_term_id TEXT NOT NULL REFERENCES terms(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (term_id, related_term_id),
    CHECK (term_id <> related_term_id)
);

CREATE INDEX IF NOT EXISTS idx_terms_category_id ON terms(category_id);
CREATE INDEX IF NOT EXISTS idx_terms_term ON terms(term);
CREATE INDEX IF NOT EXISTS idx_term_relations_term_id ON term_relations(term_id);
CREATE INDEX IF NOT EXISTS idx_term_relations_related_term_id ON term_relations(related_term_id);
