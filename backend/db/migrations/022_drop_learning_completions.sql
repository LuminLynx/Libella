-- Retire the term-centric learning_completions table per docs/AUDIT.md §2.4.
--
-- The path-centric `completions` table (migration 019) and POST /api/v1/completions
-- replace POST /api/v1/learning-completions. Migrations 010 (initial table) and
-- 012 (payload columns) remain immutable history per AUDIT §5 #5; this forward
-- migration retires the table they created.
--
-- Anything still reading from learning_completions belongs to the term-centric
-- surface that AUDIT §2.3 marks for removal in Phase 1; the column references
-- here are intentionally none. If a future audit finds a stray reader, this
-- DROP catches it loudly rather than silently leaving stale rows.

DROP TABLE IF EXISTS learning_completions;
