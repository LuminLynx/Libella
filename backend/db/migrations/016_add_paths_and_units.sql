-- Path-centric spine per docs/STRATEGY.md (Path / Unit) and docs/AUDIT.md §2.7.
--
-- A path is the canonical curriculum track ("LLM Systems for PMs" in v1).
-- A unit is the smallest thing worth opening the app for: one concept,
-- taught through one trade-off, with a 90-second bite + depth on tap
-- (the F2 spine in EXECUTION.md).
--
-- Slot mapping to STRATEGY.md "9-slot unit anatomy":
--   1. Title             -> units.title
--   2. Single-sentence definition -> units.definition
--   3. Trade-off framing -> units.trade_off_framing
--   4. 90-second bite    -> units.bite_md
--   5. Depth             -> units.depth_md
--   6. Calibration tags  -> calibration_tags table (migration 017)
--   7. Sources           -> unit_sources table       (migration 017)
--   8. Decision prompt + rubric -> decision_prompts + rubrics (migration 018)
--   9. Prereq pointers   -> units.prereq_unit_ids (TEXT[] on this table)
--
-- prereq_unit_ids is a TEXT[] of unit ids per AUDIT §2.7 ("smallest viable
-- forward step each time"). FK enforcement on array elements is not
-- supported by Postgres; validation lives in the path-centric
-- repositories. A future migration can normalize this into a unit_prereqs
-- join table if FK enforcement becomes load-bearing.

CREATE TABLE IF NOT EXISTS paths (
    id TEXT PRIMARY KEY,
    slug TEXT NOT NULL UNIQUE,
    title TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$'),
    CHECK (LENGTH(TRIM(title)) > 0)
);

CREATE TABLE IF NOT EXISTS units (
    id TEXT PRIMARY KEY,
    path_id TEXT NOT NULL REFERENCES paths(id) ON DELETE CASCADE,
    slug TEXT NOT NULL,
    position INTEGER NOT NULL,
    title TEXT NOT NULL,
    definition TEXT NOT NULL,
    trade_off_framing TEXT NOT NULL,
    bite_md TEXT NOT NULL,
    depth_md TEXT NOT NULL DEFAULT '',
    prereq_unit_ids TEXT[] NOT NULL DEFAULT '{}',
    status TEXT NOT NULL DEFAULT 'draft',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$'),
    CHECK (LENGTH(TRIM(title)) > 0),
    CHECK (LENGTH(TRIM(definition)) > 0),
    CHECK (LENGTH(TRIM(trade_off_framing)) > 0),
    CHECK (LENGTH(TRIM(bite_md)) > 0),
    CHECK (status IN ('draft', 'published', 'archived')),
    UNIQUE (path_id, slug),
    UNIQUE (path_id, position)
);

CREATE INDEX IF NOT EXISTS idx_units_path_position ON units(path_id, position);
CREATE INDEX IF NOT EXISTS idx_units_status ON units(status);
