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
    category_id TEXT NOT NULL,
    tags TEXT DEFAULT '',
    related_terms TEXT DEFAULT '',
    example_usage TEXT,
    source TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_terms_category_id ON terms(category_id);
CREATE INDEX IF NOT EXISTS idx_terms_term ON terms(term);
