CREATE OR REPLACE FUNCTION ai101_before_term_draft_write()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.term := REGEXP_REPLACE(TRIM(COALESCE(NEW.term, '')), '\\s+', ' ', 'g');
    IF NEW.term = '' THEN
        RAISE EXCEPTION 'term draft term must not be blank';
    END IF;

    NEW.slug := ai101_slugify(COALESCE(NULLIF(TRIM(NEW.slug), ''), NEW.term));
    IF NEW.slug = '' THEN
        RAISE EXCEPTION 'term draft slug must not be blank';
    END IF;

    NEW.definition := TRIM(COALESCE(NEW.definition, ''));
    IF NEW.definition = '' THEN
        RAISE EXCEPTION 'term draft definition must not be blank';
    END IF;

    NEW.explanation := TRIM(COALESCE(NEW.explanation, ''));
    IF NEW.explanation = '' THEN
        RAISE EXCEPTION 'term draft explanation must not be blank';
    END IF;

    NEW.humor := TRIM(COALESCE(NEW.humor, ''));
    NEW.source_type := TRIM(COALESCE(NEW.source_type, 'manual'));
    IF NEW.source_type = '' THEN
        NEW.source_type := 'manual';
    END IF;

    NEW.status := LOWER(TRIM(COALESCE(NEW.status, 'submitted')));
    IF NEW.status NOT IN ('submitted', 'approved', 'rejected', 'published') THEN
        RAISE EXCEPTION 'invalid term draft status: %', NEW.status;
    END IF;

    NEW.controversy_level := COALESCE(NEW.controversy_level, 0);
    IF NEW.controversy_level < 0 OR NEW.controversy_level > 3 THEN
        RAISE EXCEPTION 'term draft controversy_level must be between 0 and 3';
    END IF;

    NEW.see_also := COALESCE(NEW.see_also, '[]'::jsonb);
    NEW.tags := COALESCE(NEW.tags, '[]'::jsonb);

    NEW.updated_at := NOW();
    RETURN NEW;
END
$$;
