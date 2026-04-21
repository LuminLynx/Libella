-- Legacy schema snapshot.
-- Active schema changes must be added via backend/db/migrations/*.sql.

CREATE TABLE IF NOT EXISTS categories (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    description TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS terms (
    id TEXT PRIMARY KEY,
    slug TEXT NOT NULL UNIQUE,
    term TEXT NOT NULL UNIQUE,
    definition TEXT NOT NULL,
    explanation TEXT NOT NULL,
    humor TEXT NOT NULL DEFAULT '',
    controversy_level SMALLINT NOT NULL DEFAULT 0 CHECK (controversy_level BETWEEN 0 AND 3),
    short_definition TEXT NOT NULL,
    full_explanation TEXT NOT NULL,
    category_id TEXT NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    tags TEXT NOT NULL DEFAULT '',
    related_terms TEXT NOT NULL DEFAULT '',
    example_usage TEXT,
    source TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CHECK (LENGTH(TRIM(term)) > 0),
    CHECK (slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$')
);

CREATE TABLE IF NOT EXISTS term_relations (
    term_id TEXT NOT NULL REFERENCES terms(id) ON DELETE CASCADE,
    related_term_id TEXT NOT NULL REFERENCES terms(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (term_id, related_term_id),
    CHECK (term_id <> related_term_id)
);

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

CREATE INDEX IF NOT EXISTS idx_terms_category_id ON terms(category_id);
CREATE INDEX IF NOT EXISTS idx_terms_term ON terms(term);
CREATE INDEX IF NOT EXISTS idx_terms_slug ON terms(slug);
CREATE INDEX IF NOT EXISTS idx_term_relations_term_id ON term_relations(term_id);
CREATE INDEX IF NOT EXISTS idx_term_relations_related_term_id ON term_relations(related_term_id);
CREATE INDEX IF NOT EXISTS idx_ai_generated_term_type ON ai_generated_content(term_id, content_type);


CREATE TABLE IF NOT EXISTS term_search_events (
    id BIGSERIAL PRIMARY KEY,
    query TEXT NOT NULL,
    normalized_query TEXT NOT NULL,
    matched_term_id TEXT REFERENCES terms(id) ON DELETE SET NULL,
    had_exact_match BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS term_drafts (
    id BIGSERIAL PRIMARY KEY,
    slug TEXT NOT NULL,
    term TEXT NOT NULL,
    definition TEXT NOT NULL,
    explanation TEXT NOT NULL,
    humor TEXT NOT NULL DEFAULT '',
    see_also JSONB NOT NULL DEFAULT '[]'::jsonb,
    tags JSONB NOT NULL DEFAULT '[]'::jsonb,
    controversy_level SMALLINT NOT NULL DEFAULT 0 CHECK (controversy_level BETWEEN 0 AND 3),
    source_type TEXT NOT NULL DEFAULT 'manual',
    source_reference TEXT,
    status TEXT NOT NULL DEFAULT 'draft' CHECK (status IN ('draft', 'reviewed', 'approved', 'rejected', 'published')),
    category_id TEXT REFERENCES categories(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (LENGTH(TRIM(term)) > 0),
    CHECK (slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$'),
    CHECK (jsonb_typeof(see_also) = 'array'),
    CHECK (jsonb_typeof(tags) = 'array')
);

CREATE INDEX IF NOT EXISTS idx_term_search_events_created_at ON term_search_events(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_term_search_events_normalized_query ON term_search_events(normalized_query);
CREATE INDEX IF NOT EXISTS idx_term_search_events_missing ON term_search_events(normalized_query, created_at DESC) WHERE had_exact_match = FALSE;
CREATE INDEX IF NOT EXISTS idx_term_drafts_status ON term_drafts(status);
CREATE INDEX IF NOT EXISTS idx_term_drafts_slug ON term_drafts(slug);
CREATE INDEX IF NOT EXISTS idx_term_drafts_updated_at ON term_drafts(updated_at DESC);
