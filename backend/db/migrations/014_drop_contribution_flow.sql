-- Retire the contribution flow per docs/AUDIT.md §2.3 + §5 #4.
--
-- Background: PR #48 removed every endpoint, repository helper, and
-- Android UI tied to the contribution flow (term-draft submit / approve /
-- publish, contributor scoring, contribution events). The tables created
-- by migrations 005, 006, 007, 008 are now callerless. They retire here
-- via this forward migration; existing migrations 005/006/007/008 stay
-- untouched as immutable history (AUDIT.md §5 #5).
--
-- Drop order is FK-aware:
--   1. contribution_events depends on term_drafts (draft_id FK) and on
--      learning_completions (learning_completion_id FK from migration
--      011). learning_completions is KEEP for now (RESHAPE later in the
--      path-centric completions PR), so we drop the child here without
--      touching the parent.
--   2. terms.source_draft_id (added by migration 007) is a column on the
--      KEEP terms table whose FK points at term_drafts. The column itself
--      was added solely for the contribution flow and has no future
--      consumer. Drop the column (and its index) before dropping the
--      term_drafts parent.
--   3. term_drafts then drops cleanly. Dropping the table also drops the
--      ai101_before_term_draft_write BEFORE-INSERT-OR-UPDATE trigger
--      that migration 005 attached to it (008 only redefined the
--      function body, not the trigger object).
--   4. contributor_scores has no inbound or outbound FKs.
--   5. The plpgsql function `ai101_before_term_draft_write()` is now
--      orphaned (its only trigger was on term_drafts). Drop it last.
--
-- Founder confirmed synthetic data only on 2026-05-04 (AUDIT.md §5 #8
-- option (a)); no pg_dump archive is required before drop.

DROP TABLE IF EXISTS contribution_events;

ALTER TABLE terms
    DROP COLUMN IF EXISTS source_draft_id;
DROP INDEX IF EXISTS idx_terms_source_draft_id;

DROP TABLE IF EXISTS term_drafts;

DROP TABLE IF EXISTS contributor_scores;

DROP FUNCTION IF EXISTS ai101_before_term_draft_write();
