-- Retire term_search_events per docs/AUDIT.md §5 #3.
--
-- Background: this table logged search queries against the glossary's
-- now-deleted "search as front-door" surface. With search demoted into
-- the in-unit side-door (PR #50) and the contribution flow's
-- missing-query consumer deleted in PR #48, the telemetry has no
-- reader and no writer.
--
-- The original FK from term_drafts.missing_search_event_id pointed at
-- this table. That FK is gone now: the prior migration (014) dropped
-- term_drafts entirely, which automatically dropped its FK to here.
-- This migration is therefore safe to apply after 014 in the standard
-- numeric order the runner uses.
--
-- Founder confirmed synthetic data only on 2026-05-04 (AUDIT.md §5 #8
-- option (a)); no pg_dump archive is required before drop.

DROP TABLE IF EXISTS term_search_events;
