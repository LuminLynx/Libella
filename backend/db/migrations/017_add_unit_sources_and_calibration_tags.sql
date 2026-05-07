-- Unit anatomy slots 6 (calibration tags) and 7 (sources) per docs/AUDIT.md §2.7.
--
-- Calibration tags surface confidence per claim ("settled", "contested",
-- "unsettled") — the operational expression of P2 ("calibrate, don't
-- bluff"). Strategy publishes "we don't know yet" as a valid answer.
--
-- Sources back every claim, primary preferred, dated. P2's evidentiary
-- side: no claim ships without a source.

CREATE TABLE IF NOT EXISTS unit_sources (
    id BIGSERIAL PRIMARY KEY,
    unit_id TEXT NOT NULL REFERENCES units(id) ON DELETE CASCADE,
    url TEXT NOT NULL,
    title TEXT NOT NULL,
    source_date DATE NOT NULL,
    primary_source BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (LENGTH(TRIM(url)) > 0),
    CHECK (LENGTH(TRIM(title)) > 0),
    UNIQUE (unit_id, url)
);

CREATE INDEX IF NOT EXISTS idx_unit_sources_unit ON unit_sources(unit_id);

CREATE TABLE IF NOT EXISTS calibration_tags (
    id BIGSERIAL PRIMARY KEY,
    unit_id TEXT NOT NULL REFERENCES units(id) ON DELETE CASCADE,
    claim TEXT NOT NULL,
    tier TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (LENGTH(TRIM(claim)) > 0),
    CHECK (tier IN ('settled', 'contested', 'unsettled'))
);

CREATE INDEX IF NOT EXISTS idx_calibration_tags_unit ON calibration_tags(unit_id);
CREATE INDEX IF NOT EXISTS idx_calibration_tags_tier ON calibration_tags(tier);
