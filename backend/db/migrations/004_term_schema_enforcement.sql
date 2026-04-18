ALTER TABLE terms
    ADD COLUMN IF NOT EXISTS slug TEXT,
    ADD COLUMN IF NOT EXISTS definition TEXT,
    ADD COLUMN IF NOT EXISTS explanation TEXT,
    ADD COLUMN IF NOT EXISTS humor TEXT,
    ADD COLUMN IF NOT EXISTS controversy_level SMALLINT;

CREATE OR REPLACE FUNCTION ai101_slugify(input_text TEXT)
RETURNS TEXT
LANGUAGE SQL
IMMUTABLE
AS $$
    SELECT TRIM(BOTH '-' FROM REGEXP_REPLACE(LOWER(COALESCE(input_text, '')), '[^a-z0-9]+', '-', 'g'))
$$;

CREATE OR REPLACE FUNCTION ai101_normalize_csv(input_text TEXT, lowercase_tokens BOOLEAN DEFAULT TRUE)
RETURNS TEXT
LANGUAGE SQL
IMMUTABLE
AS $$
    SELECT COALESCE(
        STRING_AGG(token, ',' ORDER BY token),
        ''
    )
    FROM (
        SELECT DISTINCT
            CASE
                WHEN lowercase_tokens THEN LOWER(TRIM(raw_value))
                ELSE TRIM(raw_value)
            END AS token
        FROM UNNEST(STRING_TO_ARRAY(COALESCE(input_text, ''), ',')) AS raw_value
        WHERE TRIM(raw_value) <> ''
    ) normalized
$$;

UPDATE terms
SET
    term = REGEXP_REPLACE(TRIM(term), '\\s+', ' ', 'g'),
    slug = COALESCE(NULLIF(slug, ''), ai101_slugify(term)),
    definition = COALESCE(NULLIF(TRIM(definition), ''), short_definition),
    explanation = COALESCE(NULLIF(TRIM(explanation), ''), full_explanation),
    humor = COALESCE(TRIM(humor), ''),
    controversy_level = COALESCE(controversy_level, 0),
    tags = ai101_normalize_csv(tags, TRUE),
    related_terms = ai101_normalize_csv(related_terms, TRUE),
    short_definition = COALESCE(NULLIF(TRIM(definition), ''), short_definition),
    full_explanation = COALESCE(NULLIF(TRIM(explanation), ''), full_explanation),
    updated_at = NOW();

UPDATE terms
SET slug = ai101_slugify(term)
WHERE slug IS NULL OR slug = '';

ALTER TABLE terms
    ALTER COLUMN slug SET NOT NULL,
    ALTER COLUMN definition SET NOT NULL,
    ALTER COLUMN explanation SET NOT NULL,
    ALTER COLUMN humor SET DEFAULT '',
    ALTER COLUMN humor SET NOT NULL,
    ALTER COLUMN controversy_level SET DEFAULT 0,
    ALTER COLUMN controversy_level SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'terms_slug_unique'
    ) THEN
        ALTER TABLE terms ADD CONSTRAINT terms_slug_unique UNIQUE (slug);
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'terms_term_not_blank'
    ) THEN
        ALTER TABLE terms
            ADD CONSTRAINT terms_term_not_blank CHECK (LENGTH(TRIM(term)) > 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'terms_slug_format'
    ) THEN
        ALTER TABLE terms
            ADD CONSTRAINT terms_slug_format CHECK (slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$');
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'terms_definition_not_blank'
    ) THEN
        ALTER TABLE terms
            ADD CONSTRAINT terms_definition_not_blank CHECK (LENGTH(TRIM(definition)) > 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'terms_explanation_not_blank'
    ) THEN
        ALTER TABLE terms
            ADD CONSTRAINT terms_explanation_not_blank CHECK (LENGTH(TRIM(explanation)) > 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'terms_controversy_level_range'
    ) THEN
        ALTER TABLE terms
            ADD CONSTRAINT terms_controversy_level_range CHECK (controversy_level BETWEEN 0 AND 3);
    END IF;
END
$$;

CREATE OR REPLACE FUNCTION ai101_before_term_write()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.term := REGEXP_REPLACE(TRIM(COALESCE(NEW.term, '')), '\\s+', ' ', 'g');
    IF NEW.term = '' THEN
        RAISE EXCEPTION 'term must not be blank';
    END IF;

    NEW.slug := ai101_slugify(COALESCE(NULLIF(TRIM(NEW.slug), ''), NEW.term));
    IF NEW.slug = '' THEN
        RAISE EXCEPTION 'slug must not be blank';
    END IF;

    NEW.definition := TRIM(COALESCE(NULLIF(NEW.definition, ''), NEW.short_definition, ''));
    IF NEW.definition = '' THEN
        RAISE EXCEPTION 'definition must not be blank';
    END IF;

    NEW.explanation := TRIM(COALESCE(NULLIF(NEW.explanation, ''), NEW.full_explanation, ''));
    IF NEW.explanation = '' THEN
        RAISE EXCEPTION 'explanation must not be blank';
    END IF;

    NEW.humor := TRIM(COALESCE(NEW.humor, ''));
    NEW.controversy_level := COALESCE(NEW.controversy_level, 0);

    NEW.tags := ai101_normalize_csv(NEW.tags, TRUE);
    NEW.related_terms := ai101_normalize_csv(NEW.related_terms, TRUE);

    NEW.short_definition := NEW.definition;
    NEW.full_explanation := NEW.explanation;

    NEW.updated_at := NOW();
    RETURN NEW;
END
$$;

DROP TRIGGER IF EXISTS trg_ai101_before_term_write ON terms;
CREATE TRIGGER trg_ai101_before_term_write
BEFORE INSERT OR UPDATE ON terms
FOR EACH ROW
EXECUTE FUNCTION ai101_before_term_write();

CREATE OR REPLACE FUNCTION ai101_sync_term_relations_from_terms()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM term_relations WHERE term_id = NEW.id;

    INSERT INTO term_relations (term_id, related_term_id)
    SELECT NEW.id, related.id
    FROM UNNEST(STRING_TO_ARRAY(NEW.related_terms, ',')) AS rel_id
    JOIN terms AS related ON related.id = TRIM(rel_id)
    WHERE TRIM(rel_id) <> ''
      AND related.id <> NEW.id
    ON CONFLICT (term_id, related_term_id) DO NOTHING;

    RETURN NEW;
END
$$;

DROP TRIGGER IF EXISTS trg_ai101_sync_term_relations_from_terms ON terms;
CREATE TRIGGER trg_ai101_sync_term_relations_from_terms
AFTER INSERT OR UPDATE OF related_terms, id ON terms
FOR EACH ROW
EXECUTE FUNCTION ai101_sync_term_relations_from_terms();

DELETE FROM term_relations;
INSERT INTO term_relations (term_id, related_term_id)
SELECT t.id, related.id
FROM terms t
JOIN LATERAL UNNEST(STRING_TO_ARRAY(t.related_terms, ',')) AS rel_id ON TRUE
JOIN terms AS related ON related.id = TRIM(rel_id)
WHERE TRIM(rel_id) <> ''
  AND t.id <> related.id
ON CONFLICT (term_id, related_term_id) DO NOTHING;
