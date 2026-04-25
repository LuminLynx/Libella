ALTER TABLE term_drafts
    ADD COLUMN IF NOT EXISTS contributor_metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    ADD COLUMN IF NOT EXISTS missing_search_event_id BIGINT REFERENCES term_search_events(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS published_term_id TEXT REFERENCES terms(id) ON DELETE SET NULL;

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

ALTER TABLE term_drafts
    DROP CONSTRAINT IF EXISTS term_drafts_status_check;

ALTER TABLE term_drafts
    ADD CONSTRAINT term_drafts_status_check
    CHECK (status IN ('draft', 'reviewed', 'submitted', 'approved', 'rejected', 'published'));

UPDATE term_drafts
SET status = 'submitted'
WHERE status IN ('draft', 'reviewed');

ALTER TABLE term_drafts
    DROP CONSTRAINT IF EXISTS term_drafts_status_check;

ALTER TABLE term_drafts
    ADD CONSTRAINT term_drafts_status_check
    CHECK (status IN ('submitted', 'approved', 'rejected', 'published'));

ALTER TABLE terms
    ADD COLUMN IF NOT EXISTS source_draft_id BIGINT REFERENCES term_drafts(id) ON DELETE SET NULL;

ALTER TABLE contribution_events
    DROP CONSTRAINT IF EXISTS contribution_events_event_type_check;

ALTER TABLE contribution_events
    ADD CONSTRAINT contribution_events_event_type_check
    CHECK (event_type IN ('draft_submitted', 'draft_approved', 'draft_rejected', 'draft_published'));

CREATE INDEX IF NOT EXISTS idx_term_drafts_missing_search_event
    ON term_drafts(missing_search_event_id);
CREATE INDEX IF NOT EXISTS idx_term_drafts_published_term_id
    ON term_drafts(published_term_id);
CREATE INDEX IF NOT EXISTS idx_terms_source_draft_id
    ON terms(source_draft_id);
