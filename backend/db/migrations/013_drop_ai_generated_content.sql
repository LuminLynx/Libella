-- Retire ai_generated_content per docs/AUDIT.md §2.3 + §2.6.
--
-- Background: this table cached scenario/challenge artifact JSON keyed by
-- (term_id, content_type) for the "AI Learning Layer" feature. PR #48
-- removed every code path that read or wrote it. The new strategy uses
-- provider-side prompt caching on the rubric (T2-E in docs/STRATEGY.md),
-- not row-cached artifacts, so this table has no future use.
--
-- Founder confirmed synthetic data only on 2026-05-04 (AUDIT.md §5 #8
-- option (a)); no pg_dump archive is required before drop.

DROP TABLE IF EXISTS ai_generated_content;
