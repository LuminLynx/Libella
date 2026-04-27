-- Extend contribution_events.event_type CHECK constraint to allow learning completions.
-- Postgres CHECK constraints are not directly modifiable; drop and recreate.

ALTER TABLE contribution_events
    DROP CONSTRAINT IF EXISTS contribution_events_event_type_check;

ALTER TABLE contribution_events
    ADD CONSTRAINT contribution_events_event_type_check
    CHECK (
        event_type IN (
            'draft_submitted',
            'draft_approved',
            'draft_rejected',
            'draft_published',
            'scenario_completed',
            'challenge_completed'
        )
    );

-- Add a learning_completion_id column so we can link events to completion rows.
ALTER TABLE contribution_events
    ADD COLUMN IF NOT EXISTS learning_completion_id BIGINT
        REFERENCES learning_completions(id) ON DELETE SET NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_contribution_events_unique_completion_event
    ON contribution_events(learning_completion_id, event_type)
    WHERE learning_completion_id IS NOT NULL;
