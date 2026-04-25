ALTER TABLE term_drafts
    ADD COLUMN IF NOT EXISTS contributor_metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    ADD COLUMN IF NOT EXISTS missing_search_event_id BIGINT REFERENCES term_search_events(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS published_term_id TEXT REFERENCES terms(id) ON DELETE SET NULL;

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
