CREATE TABLE IF NOT EXISTS term_search_events (
    id BIGSERIAL PRIMARY KEY,
    query TEXT NOT NULL,
    normalized_query TEXT NOT NULL,
    matched_term_id TEXT REFERENCES terms(id) ON DELETE SET NULL,
    had_exact_match BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_term_search_events_created_at
    ON term_search_events(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_term_search_events_normalized_query
    ON term_search_events(normalized_query);
CREATE INDEX IF NOT EXISTS idx_term_search_events_missing
    ON term_search_events(normalized_query, created_at DESC)
    WHERE had_exact_match = FALSE;

CREATE TABLE IF NOT EXISTS term_drafts (
    id BIGSERIAL PRIMARY KEY,
    slug TEXT NOT NULL,
    term TEXT NOT NULL,
    definition TEXT NOT NULL,
    explanation TEXT NOT NULL,
    humor TEXT NOT NULL DEFAULT '',
    see_also JSONB NOT NULL DEFAULT '[]'::jsonb,
    tags JSONB NOT NULL DEFAULT '[]'::jsonb,
    controversy_level SMALLINT NOT NULL DEFAULT 0,
    source_type TEXT NOT NULL DEFAULT 'manual',
    source_reference TEXT,
    status TEXT NOT NULL DEFAULT 'submitted',
    category_id TEXT REFERENCES categories(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$'),
    CHECK (LENGTH(TRIM(term)) > 0),
    CHECK (LENGTH(TRIM(definition)) > 0),
    CHECK (LENGTH(TRIM(explanation)) > 0),
    CHECK (controversy_level BETWEEN 0 AND 3),
    CHECK (status IN ('submitted', 'approved', 'rejected', 'published')),
    CHECK (jsonb_typeof(see_also) = 'array'),
    CHECK (jsonb_typeof(tags) = 'array')
);

CREATE INDEX IF NOT EXISTS idx_term_drafts_status ON term_drafts(status);
CREATE INDEX IF NOT EXISTS idx_term_drafts_slug ON term_drafts(slug);
CREATE INDEX IF NOT EXISTS idx_term_drafts_updated_at ON term_drafts(updated_at DESC);

CREATE OR REPLACE FUNCTION ai101_before_term_draft_write()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.term := REGEXP_REPLACE(TRIM(COALESCE(NEW.term, '')), '\\s+', ' ', 'g');
    IF NEW.term = '' THEN
        RAISE EXCEPTION 'term draft term must not be blank';
    END IF;

    NEW.slug := ai101_slugify(COALESCE(NULLIF(TRIM(NEW.slug), ''), NEW.term));
    IF NEW.slug = '' THEN
        RAISE EXCEPTION 'term draft slug must not be blank';
    END IF;

    NEW.definition := TRIM(COALESCE(NEW.definition, ''));
    IF NEW.definition = '' THEN
        RAISE EXCEPTION 'term draft definition must not be blank';
    END IF;

    NEW.explanation := TRIM(COALESCE(NEW.explanation, ''));
    IF NEW.explanation = '' THEN
        RAISE EXCEPTION 'term draft explanation must not be blank';
    END IF;

    NEW.humor := TRIM(COALESCE(NEW.humor, ''));
    NEW.source_type := TRIM(COALESCE(NEW.source_type, 'manual'));
    IF NEW.source_type = '' THEN
        NEW.source_type := 'manual';
    END IF;

    NEW.status := LOWER(TRIM(COALESCE(NEW.status, 'submitted')));
    IF NEW.status NOT IN ('submitted', 'approved', 'rejected', 'published') THEN
        RAISE EXCEPTION 'invalid term draft status: %', NEW.status;
    END IF;

    NEW.controversy_level := COALESCE(NEW.controversy_level, 0);
    IF NEW.controversy_level < 0 OR NEW.controversy_level > 3 THEN
        RAISE EXCEPTION 'term draft controversy_level must be between 0 and 3';
    END IF;

    NEW.see_also := COALESCE(NEW.see_also, '[]'::jsonb);
    NEW.tags := COALESCE(NEW.tags, '[]'::jsonb);

    NEW.updated_at := NOW();
    RETURN NEW;
END
$$;

DROP TRIGGER IF EXISTS trg_ai101_before_term_draft_write ON term_drafts;
CREATE TRIGGER trg_ai101_before_term_draft_write
BEFORE INSERT OR UPDATE ON term_drafts
FOR EACH ROW
EXECUTE FUNCTION ai101_before_term_draft_write();
