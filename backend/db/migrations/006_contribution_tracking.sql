ALTER TABLE term_drafts
    ADD COLUMN IF NOT EXISTS contributor_id TEXT NOT NULL DEFAULT 'anonymous';

CREATE INDEX IF NOT EXISTS idx_term_drafts_contributor_id
    ON term_drafts(contributor_id, created_at DESC);

CREATE TABLE IF NOT EXISTS contribution_events (
    id BIGSERIAL PRIMARY KEY,
    contributor_id TEXT NOT NULL,
    draft_id BIGINT REFERENCES term_drafts(id) ON DELETE SET NULL,
    event_type TEXT NOT NULL,
    points_awarded INTEGER NOT NULL DEFAULT 0,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (
        event_type IN (
            'draft_submitted',
            'draft_approved',
            'draft_rejected',
            'draft_published'
        )
    )
);

CREATE INDEX IF NOT EXISTS idx_contribution_events_contributor_created
    ON contribution_events(contributor_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_contribution_events_type_created
    ON contribution_events(event_type, created_at DESC);

CREATE UNIQUE INDEX IF NOT EXISTS idx_contribution_events_unique_draft_event
    ON contribution_events(draft_id, event_type)
    WHERE draft_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS contributor_scores (
    contributor_id TEXT PRIMARY KEY,
    total_score INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
