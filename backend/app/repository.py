from __future__ import annotations

import json
import re
import secrets
from datetime import datetime, timedelta, timezone
from typing import Any

from .db import get_connection

TERM_SELECT_BASE = """
    SELECT
        t.id,
        t.slug,
        t.term,
        t.definition,
        t.explanation,
        t.humor,
        t.controversy_level,
        t.short_definition,
        t.full_explanation,
        t.category_id,
        t.tags,
        t.related_terms,
        t.example_usage,
        t.source,
        t.created_at,
        t.updated_at,
        COALESCE(
            ARRAY_AGG(related.slug ORDER BY related.term)
            FILTER (WHERE related.slug IS NOT NULL),
            '{}'::TEXT[]
        ) AS see_also,
        COALESCE(
            ARRAY_AGG(related.id ORDER BY related.term)
            FILTER (WHERE related.id IS NOT NULL),
            '{}'::TEXT[]
        ) AS related_term_ids
    FROM terms t
    LEFT JOIN term_relations relation ON relation.term_id = t.id
    LEFT JOIN terms related ON related.id = relation.related_term_id
"""

TERM_GROUP_BY = """
    GROUP BY
        t.id,
        t.slug,
        t.term,
        t.definition,
        t.explanation,
        t.humor,
        t.controversy_level,
        t.short_definition,
        t.full_explanation,
        t.category_id,
        t.tags,
        t.related_terms,
        t.example_usage,
        t.source,
        t.created_at,
        t.updated_at
"""

DRAFT_STATUSES = {"submitted", "approved", "rejected", "published"}
ALLOWED_STATUS_TRANSITIONS: dict[str, set[str]] = {
    "submitted": {"approved", "rejected"},
    "approved": {"published"},
    "rejected": set(),
    "published": set(),
}
CONTRIBUTION_EVENT_POINTS = {
    "draft_submitted": 5,
    "draft_approved": 15,
    "draft_rejected": 0,
    "draft_published": 25,
    "scenario_completed": 10,
    "challenge_completed": 15,
}
STATUS_TO_EVENT_TYPE = {
    "approved": "draft_approved",
    "rejected": "draft_rejected",
}


def _parse_csv(value: str | None) -> list[str]:
    if not value:
        return []
    return [item.strip() for item in value.split(",") if item.strip()]


def _to_list(value: Any) -> list[str]:
    if value is None:
        return []
    if isinstance(value, list):
        return [str(item).strip() for item in value if str(item).strip()]
    return _parse_csv(str(value))


def _normalize_spaces(value: str) -> str:
    return re.sub(r"\s+", " ", value.strip())


def normalize_query(value: str) -> str:
    return _normalize_spaces(value).lower()


def slugify(value: str) -> str:
    slug = re.sub(r"[^a-z0-9]+", "-", value.lower()).strip("-")
    if not slug:
        raise ValueError("slug must contain at least one alphanumeric character")
    return slug


def _normalize_tag(value: str) -> str:
    token = re.sub(r"[^a-z0-9]+", "-", value.strip().lower()).strip("-")
    return token


def normalize_tags(values: list[str]) -> list[str]:
    normalized = sorted({_normalize_tag(value) for value in values if _normalize_tag(value)})
    return normalized


def _term_id_from_slug(slug: str) -> str:
    return slug if slug.startswith("term-") else f"term-{slug}"


def _validate_controversy_level(controversy_level: int) -> None:
    if controversy_level < 0 or controversy_level > 3:
        raise ValueError("controversy_level must be between 0 and 3")


def validate_draft_payload(draft: dict[str, Any]) -> dict[str, Any]:
    term = _normalize_spaces(str(draft.get("term", "")))
    definition = _normalize_spaces(str(draft.get("definition", "")))
    explanation = _normalize_spaces(str(draft.get("explanation", "")))
    humor = _normalize_spaces(str(draft.get("humor", "")))

    if not term:
        raise ValueError("term is required")
    if not definition:
        raise ValueError("definition is required")
    if not explanation:
        raise ValueError("explanation is required")

    slug_input = str(draft.get("slug") or term)
    normalized_slug = slugify(slug_input)

    controversy_level = int(draft.get("controversy_level", 0))
    _validate_controversy_level(controversy_level)

    see_also = [slugify(item) for item in _to_list(draft.get("see_also"))]
    tags = normalize_tags(_to_list(draft.get("tags")))

    category_id = str(draft.get("category_id", "")).strip()
    if not category_id:
        raise ValueError("category_id is required")

    contributor_id = _normalize_spaces(str(draft.get("contributor_id", "anonymous"))) or "anonymous"
    contributor_metadata_raw = draft.get("contributor_metadata")
    contributor_metadata = contributor_metadata_raw if isinstance(contributor_metadata_raw, dict) else {}
    missing_search_event_id = draft.get("missing_search_event_id")
    if missing_search_event_id is not None:
        missing_search_event_id = int(missing_search_event_id)

    status = str(draft.get("status", "submitted")).strip().lower()
    if status not in DRAFT_STATUSES:
        raise ValueError(f"status must be one of: {', '.join(sorted(DRAFT_STATUSES))}")

    return {
        "slug": normalized_slug,
        "term": term,
        "definition": definition,
        "explanation": explanation,
        "humor": humor,
        "see_also": sorted(set(see_also)),
        "tags": tags,
        "controversy_level": controversy_level,
        "source_type": str(draft.get("source_type", "manual")).strip() or "manual",
        "source_reference": str(draft.get("source_reference", "")).strip() or None,
        "status": status,
        "category_id": category_id,
        "contributor_id": contributor_id,
        "contributor_metadata": contributor_metadata,
        "missing_search_event_id": missing_search_event_id,
    }


def list_terms() -> list[dict[str, Any]]:
    query = f"""
        {TERM_SELECT_BASE}
        {TERM_GROUP_BY}
        ORDER BY LOWER(t.term) ASC
    """
    with get_connection() as connection:
        rows = connection.execute(query).fetchall()
    return [_map_term_row(row) for row in rows]


def get_term_by_id(term_id: str) -> dict[str, Any] | None:
    query = f"""
        {TERM_SELECT_BASE}
        WHERE t.id = %s
        {TERM_GROUP_BY}
    """
    with get_connection() as connection:
        row = connection.execute(query, (term_id,)).fetchone()
    return _map_term_row(row) if row else None


def list_categories() -> list[dict[str, Any]]:
    query = """
        SELECT id, name, description
        FROM categories
        ORDER BY LOWER(name) ASC
    """
    with get_connection() as connection:
        rows = connection.execute(query).fetchall()
    return [dict(row) for row in rows]


def list_terms_by_category(category_id: str) -> list[dict[str, Any]]:
    query = f"""
        {TERM_SELECT_BASE}
        WHERE t.category_id = %s
        {TERM_GROUP_BY}
        ORDER BY LOWER(t.term) ASC
    """
    with get_connection() as connection:
        rows = connection.execute(query, (category_id,)).fetchall()
    return [_map_term_row(row) for row in rows]


def search_terms(raw_query: str) -> list[dict[str, Any]]:
    query = raw_query.strip()
    search_value = f"%{query}%"
    sql = f"""
        {TERM_SELECT_BASE}
        WHERE t.term ILIKE %s
           OR t.definition ILIKE %s
           OR t.explanation ILIKE %s
           OR t.tags ILIKE %s
           OR t.slug ILIKE %s
        {TERM_GROUP_BY}
        ORDER BY LOWER(t.term) ASC
    """
    params = (search_value, search_value, search_value, search_value, search_value)

    with get_connection() as connection:
        rows = connection.execute(sql, params).fetchall()

        exact_match_row = connection.execute(
            """
            SELECT id
            FROM terms
            WHERE LOWER(term) = LOWER(%s)
               OR slug = %s
            LIMIT 1
            """,
            (query, slugify(query) if query else ""),
        ).fetchone()

        had_exact_match = exact_match_row is not None
        matched_term_id = exact_match_row["id"] if exact_match_row else None

        connection.execute(
            """
            INSERT INTO term_search_events (
                query,
                normalized_query,
                matched_term_id,
                had_exact_match
            ) VALUES (%s, %s, %s, %s)
            """,
            (raw_query, normalize_query(raw_query), matched_term_id, had_exact_match),
        )
        connection.commit()

    return [_map_term_row(row) for row in rows]


def get_top_missing_queries(*, days: int = 30, limit: int = 20) -> list[dict[str, Any]]:
    bounded_days = max(1, min(days, 365))
    bounded_limit = max(1, min(limit, 100))

    with get_connection() as connection:
        rows = connection.execute(
            """
            SELECT
                normalized_query,
                COUNT(*)::INT AS search_count,
                MAX(created_at) AS last_seen_at,
                MIN(created_at) AS first_seen_at,
                ARRAY_AGG(query ORDER BY created_at DESC) FILTER (WHERE query IS NOT NULL) AS recent_queries
            FROM term_search_events
            WHERE had_exact_match = FALSE
              AND normalized_query <> ''
              AND created_at >= NOW() - (%s * INTERVAL '1 day')
            GROUP BY normalized_query
            ORDER BY search_count DESC, last_seen_at DESC
            LIMIT %s
            """,
            (bounded_days, bounded_limit),
        ).fetchall()

    return [
        {
            "normalizedQuery": row["normalized_query"],
            "searchCount": row["search_count"],
            "firstSeenAt": row["first_seen_at"],
            "lastSeenAt": row["last_seen_at"],
            "sampleQueries": [item for item in (row["recent_queries"] or [])[:3]],
        }
        for row in rows
    ]


def create_term_draft(payload: dict[str, Any]) -> dict[str, Any]:
    draft = validate_draft_payload(payload)
    with get_connection() as connection:
        row = connection.execute(
            """
            INSERT INTO term_drafts (
                slug,
                term,
                definition,
                explanation,
                humor,
                see_also,
                tags,
                controversy_level,
                source_type,
                source_reference,
                status,
                category_id,
                contributor_id,
                contributor_metadata,
                missing_search_event_id
            ) VALUES (%s, %s, %s, %s, %s, %s::jsonb, %s::jsonb, %s, %s, %s, %s, %s, %s, %s::jsonb, %s)
            RETURNING *
            """,
            (
                draft["slug"],
                draft["term"],
                draft["definition"],
                draft["explanation"],
                draft["humor"],
                json.dumps(draft["see_also"]),
                json.dumps(draft["tags"]),
                draft["controversy_level"],
                draft["source_type"],
                draft["source_reference"],
                draft["status"],
                draft["category_id"],
                draft["contributor_id"],
                json.dumps(draft["contributor_metadata"]),
                draft["missing_search_event_id"],
            ),
        ).fetchone()
        _record_contribution_event(
            connection=connection,
            contributor_id=draft["contributor_id"],
            draft_id=row["id"],
            event_type="draft_submitted",
            metadata={"status": row["status"]},
        )
        connection.commit()

    return _map_draft_row(row)


def update_draft_status(draft_id: int, status: str) -> dict[str, Any] | None:
    normalized_status = status.strip().lower()
    if normalized_status not in DRAFT_STATUSES:
        raise ValueError(f"status must be one of: {', '.join(sorted(DRAFT_STATUSES))}")

    with get_connection() as connection:
        current_draft = connection.execute(
            """
            SELECT id, status, contributor_id
            FROM term_drafts
            WHERE id = %s
            FOR UPDATE
            """,
            (draft_id,),
        ).fetchone()
        if not current_draft:
            return None

        previous_status = current_draft["status"]
        if normalized_status == previous_status:
            raise ValueError(f"draft is already '{normalized_status}'")

        allowed_transitions = ALLOWED_STATUS_TRANSITIONS.get(previous_status, set())
        if normalized_status not in allowed_transitions:
            raise ValueError(f"transition '{previous_status}' -> '{normalized_status}' is not allowed")

        row = connection.execute(
            """
            UPDATE term_drafts
            SET status = %s,
                updated_at = NOW()
            WHERE id = %s
            RETURNING *
            """,
            (normalized_status, draft_id),
        ).fetchone()

        event_type = STATUS_TO_EVENT_TYPE.get(normalized_status)
        if event_type:
            _record_contribution_event(
                connection=connection,
                contributor_id=current_draft["contributor_id"],
                draft_id=draft_id,
                event_type=event_type,
                metadata={
                    "previousStatus": previous_status,
                    "newStatus": normalized_status,
                },
            )
        connection.commit()

    return _map_draft_row(row) if row else None


def publish_term_draft(draft_id: int) -> dict[str, Any] | None:
    with get_connection() as connection:
        draft_row = connection.execute(
            "SELECT * FROM term_drafts WHERE id = %s FOR UPDATE",
            (draft_id,),
        ).fetchone()

        if not draft_row:
            return None

        draft = validate_draft_payload(dict(draft_row))
        if draft_row["status"] == "published":
            raise ValueError("draft has already been published")
        if draft_row["status"] != "approved":
            raise ValueError("draft must have status 'approved' before publishing")

        existing_term = connection.execute(
            "SELECT id, category_id, created_at FROM terms WHERE slug = %s",
            (draft["slug"],),
        ).fetchone()

        category_id = draft["category_id"] or (existing_term["category_id"] if existing_term else None)
        if not category_id:
            raise ValueError("category_id is required to publish a new term draft")

        category_exists = connection.execute(
            "SELECT 1 FROM categories WHERE id = %s",
            (category_id,),
        ).fetchone()
        if not category_exists:
            raise ValueError(f"category_id '{category_id}' does not exist")

        term_id = existing_term["id"] if existing_term else _term_id_from_slug(draft["slug"])
        related_rows = connection.execute(
            """
            SELECT id, slug
            FROM terms
            WHERE slug = ANY(%s)
            """,
            (draft["see_also"],),
        ).fetchall()
        related_term_ids = sorted(
            {row["id"] for row in related_rows if row["id"] != term_id}
        )

        source = draft["source_type"]
        if draft["source_reference"]:
            source = f"{source}:{draft['source_reference']}"

        connection.execute(
            """
            INSERT INTO terms (
                id,
                slug,
                term,
                definition,
                explanation,
                humor,
                controversy_level,
                short_definition,
                full_explanation,
                category_id,
                tags,
                related_terms,
                source,
                source_draft_id,
                created_at,
                updated_at
            ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, NOW(), NOW())
            ON CONFLICT (slug)
            DO UPDATE SET
                term = EXCLUDED.term,
                definition = EXCLUDED.definition,
                explanation = EXCLUDED.explanation,
                humor = EXCLUDED.humor,
                controversy_level = EXCLUDED.controversy_level,
                short_definition = EXCLUDED.short_definition,
                full_explanation = EXCLUDED.full_explanation,
                category_id = EXCLUDED.category_id,
                tags = EXCLUDED.tags,
                related_terms = EXCLUDED.related_terms,
                source = EXCLUDED.source,
                source_draft_id = EXCLUDED.source_draft_id,
                updated_at = NOW()
            """,
            (
                term_id,
                draft["slug"],
                draft["term"],
                draft["definition"],
                draft["explanation"],
                draft["humor"],
                draft["controversy_level"],
                draft["definition"],
                draft["explanation"],
                category_id,
                ",".join(draft["tags"]),
                ",".join(related_term_ids),
                source,
                draft_id,
            ),
        )

        connection.execute("DELETE FROM term_relations WHERE term_id = %s", (term_id,))
        for related_id in related_term_ids:
            connection.execute(
                """
                INSERT INTO term_relations(term_id, related_term_id)
                VALUES (%s, %s)
                ON CONFLICT (term_id, related_term_id) DO NOTHING
                """,
                (term_id, related_id),
            )

        published_draft_row = connection.execute(
            """
            UPDATE term_drafts
            SET status = 'published',
                category_id = %s,
                published_term_id = %s,
                updated_at = NOW()
            WHERE id = %s
            RETURNING *
            """,
            (category_id, term_id, draft_id),
        ).fetchone()
        _record_contribution_event(
            connection=connection,
            contributor_id=draft_row["contributor_id"],
            draft_id=draft_id,
            event_type="draft_published",
            metadata={"termId": term_id, "termSlug": draft["slug"]},
        )
        connection.commit()

    term = get_term_by_id(term_id)
    if term is None:
        raise RuntimeError("published term could not be loaded")

    return {
        "draft": _map_draft_row(published_draft_row),
        "term": term,
    }


def get_contributor_summary(contributor_id: str, *, recent_limit: int = 10) -> dict[str, Any]:
    normalized_contributor_id = _normalize_spaces(contributor_id)
    if not normalized_contributor_id:
        raise ValueError("contributor_id is required")

    bounded_limit = max(1, min(recent_limit, 50))

    with get_connection() as connection:
        score_row = connection.execute(
            """
            SELECT total_score, updated_at
            FROM contributor_scores
            WHERE contributor_id = %s
            """,
            (normalized_contributor_id,),
        ).fetchone()

        event_rows = connection.execute(
            """
            SELECT event_type, COUNT(*)::INT AS count, COALESCE(SUM(points_awarded), 0)::INT AS points
            FROM contribution_events
            WHERE contributor_id = %s
            GROUP BY event_type
            ORDER BY event_type ASC
            """,
            (normalized_contributor_id,),
        ).fetchall()

        draft_stats = connection.execute(
            """
            SELECT
                COUNT(*)::INT AS total_drafts,
                COUNT(*) FILTER (WHERE status = 'published')::INT AS published_drafts,
                MAX(updated_at) AS last_draft_activity_at
            FROM term_drafts
            WHERE contributor_id = %s
            """,
            (normalized_contributor_id,),
        ).fetchone()

        recent_events = connection.execute(
            """
            SELECT id, event_type, points_awarded, draft_id, metadata, created_at
            FROM contribution_events
            WHERE contributor_id = %s
            ORDER BY created_at DESC
            LIMIT %s
            """,
            (normalized_contributor_id, bounded_limit),
        ).fetchall()

    events = [
        {
            "eventType": row["event_type"],
            "count": row["count"],
            "points": row["points"],
        }
        for row in event_rows
    ]

    return {
        "contributorId": normalized_contributor_id,
        "totalScore": score_row["total_score"] if score_row else 0,
        "scoreUpdatedAt": score_row["updated_at"] if score_row else None,
        "draftStats": {
            "totalDrafts": draft_stats["total_drafts"],
            "publishedDrafts": draft_stats["published_drafts"],
            "lastDraftActivityAt": draft_stats["last_draft_activity_at"],
        },
        "eventBreakdown": events,
        "recentEvents": [
            {
                "id": row["id"],
                "eventType": row["event_type"],
                "points": row["points_awarded"],
                "draftId": row["draft_id"],
                "metadata": row["metadata"],
                "createdAt": row["created_at"],
            }
            for row in recent_events
        ],
    }


def build_term_context(term_id: str) -> dict[str, Any] | None:
    term = get_term_by_id(term_id)
    if term is None:
        return None

    related_terms = [get_term_by_id(related_id) for related_id in term["relatedTerms"]]
    return {
        "term": term,
        "relatedTerms": [item for item in related_terms if item is not None],
    }


def get_cached_generated_content(
    *,
    term_id: str,
    content_type: str,
    max_age: timedelta,
) -> dict[str, Any] | None:
    query = """
        SELECT content_json, updated_at
        FROM ai_generated_content
        WHERE term_id = %s AND content_type = %s
    """
    with get_connection() as connection:
        row = connection.execute(query, (term_id, content_type)).fetchone()
    if not row:
        return None

    updated_at = row["updated_at"]
    now = datetime.now(timezone.utc)
    if now - updated_at > max_age:
        return None
    return dict(row["content_json"])


def upsert_generated_content(
    *,
    term_id: str,
    content_type: str,
    content_json: dict[str, Any],
    provider: str,
    model_name: str | None,
) -> None:
    query = """
        INSERT INTO ai_generated_content(term_id, content_type, content_json, provider, model_name)
        VALUES (%s, %s, %s::jsonb, %s, %s)
        ON CONFLICT (term_id, content_type)
        DO UPDATE SET
            content_json = EXCLUDED.content_json,
            provider = EXCLUDED.provider,
            model_name = EXCLUDED.model_name,
            updated_at = NOW()
    """
    with get_connection() as connection:
        connection.execute(
            query,
            (term_id, content_type, json.dumps(content_json), provider, model_name),
        )
        connection.commit()

def _get_term_title_by_id_or_slug(reference: str) -> str | None:
    if not reference:
        return None

    query = """
        SELECT term
        FROM terms
        WHERE id = %s OR slug = %s
        LIMIT 1
    """
    with get_connection() as connection:
        row = connection.execute(query, (reference, reference)).fetchone()
        return row["term"] if row else None


def _resolve_see_also_display_names(references: list[str]) -> list[str]:
    resolved: list[str] = []
    for reference in references:
        title = _get_term_title_by_id_or_slug(reference)
        resolved.append(title if title else reference)
    return resolved


def _map_term_row(row: Any) -> dict[str, Any]:
    normalized_tags = _parse_csv(row["tags"])
    normalized_see_also_refs = _to_list(row["see_also"])
    normalized_legacy_related_refs = _parse_csv(row["related_terms"])
    normalized_related_ids = _to_list(row["related_term_ids"])

    # Prefer relation-table slugs; if absent, fallback to legacy related_terms references.
    see_also_refs = normalized_see_also_refs if normalized_see_also_refs else normalized_legacy_related_refs
    resolved_see_also = _resolve_see_also_display_names(see_also_refs)

    return {
        "id": row["id"],
        "slug": row["slug"],
        "term": row["term"],
        "definition": row["definition"],
        "explanation": row["explanation"],
        "humor": row["humor"],
        "seeAlso": resolved_see_also,
        "tags": normalized_tags,
        "controversyLevel": row["controversy_level"],
        # Backward-compatible aliases for existing Android consumers.
        "shortDefinition": row["definition"],
        "fullExplanation": row["explanation"],
        "relatedTerms": normalized_related_ids,
        "categoryId": row["category_id"],
        "exampleUsage": row["example_usage"],
        "source": row["source"],
        "createdAt": row["created_at"],
        "updatedAt": row["updated_at"],
    }

def _map_draft_row(row: Any) -> dict[str, Any]:
    return {
        "id": row["id"],
        "slug": row["slug"],
        "term": row["term"],
        "definition": row["definition"],
        "explanation": row["explanation"],
        "humor": row["humor"],
        "seeAlso": _to_list(row["see_also"]),
        "tags": _to_list(row["tags"]),
        "controversyLevel": row["controversy_level"],
        "sourceType": row["source_type"],
        "sourceReference": row["source_reference"],
        "status": row["status"],
        "categoryId": row["category_id"],
        "contributorId": row["contributor_id"],
        "contributorMetadata": row["contributor_metadata"] or {},
        "missingSearchEventId": row["missing_search_event_id"],
        "publishedTermId": row["published_term_id"],
        "createdAt": row["created_at"],
        "updatedAt": row["updated_at"],
    }


def _record_contribution_event(
    *,
    connection: Any,
    contributor_id: str,
    draft_id: int | None,
    event_type: str,
    metadata: dict[str, Any] | None = None,
    learning_completion_id: int | None = None,
) -> int:
    """
    Insert a contribution_event row and increment contributor_scores.
    Returns the points awarded by THIS call (0 if the event was deduplicated).
    """
    points = CONTRIBUTION_EVENT_POINTS.get(event_type)
    if points is None:
        raise ValueError(f"unsupported contribution event_type '{event_type}'")

    inserted_row = connection.execute(
        """
        INSERT INTO contribution_events (
            contributor_id,
            draft_id,
            event_type,
            points_awarded,
            metadata,
            learning_completion_id
        ) VALUES (%s, %s, %s, %s, %s::jsonb, %s)
        ON CONFLICT DO NOTHING
        RETURNING points_awarded
        """,
        (
            contributor_id,
            draft_id,
            event_type,
            points,
            json.dumps(metadata or {}),
            learning_completion_id,
        ),
    ).fetchone()
    if inserted_row is None:
        return 0

    connection.execute(
        """
        INSERT INTO contributor_scores (contributor_id, total_score, updated_at)
        VALUES (%s, %s, NOW())
        ON CONFLICT (contributor_id)
        DO UPDATE SET
            total_score = contributor_scores.total_score + EXCLUDED.total_score,
            updated_at = NOW()
        """,
        (contributor_id, inserted_row["points_awarded"]),
    )
    return int(inserted_row["points_awarded"])


# ----- Users / Auth -----


def _user_id() -> str:
    return f"u-{secrets.token_urlsafe(12)}"


def _map_user_row(row: Any) -> dict[str, Any]:
    return {
        "id": row["id"],
        "email": row["email"],
        "displayName": row["display_name"],
        "createdAt": row["created_at"],
    }


def create_user(*, email: str, password_hash: str, display_name: str) -> dict[str, Any]:
    user_id = _user_id()
    with get_connection() as connection:
        row = connection.execute(
            """
            INSERT INTO users (id, email, password_hash, display_name)
            VALUES (%s, %s, %s, %s)
            RETURNING *
            """,
            (user_id, email, password_hash, display_name),
        ).fetchone()
        connection.commit()
    return _map_user_row(row)


def get_user_by_email(email: str) -> dict[str, Any] | None:
    with get_connection() as connection:
        row = connection.execute(
            "SELECT * FROM users WHERE LOWER(email) = LOWER(%s)",
            (email,),
        ).fetchone()
    return row if row is None else dict(row)


def get_user_by_id(user_id: str) -> dict[str, Any] | None:
    with get_connection() as connection:
        row = connection.execute(
            "SELECT * FROM users WHERE id = %s",
            (user_id,),
        ).fetchone()
    if row is None:
        return None
    return _map_user_row(row)


# ----- Learning completions (scenario / challenge) -----

VALID_ARTIFACT_TYPES = ("scenario", "challenge")
VALID_CONFIDENCE = ("low", "medium", "high")
ARTIFACT_TO_EVENT_TYPE = {
    "scenario": "scenario_completed",
    "challenge": "challenge_completed",
}


def _map_completion_row(row: Any) -> dict[str, Any]:
    return {
        "id": row["id"],
        "userId": row["user_id"],
        "termId": row["term_id"],
        "artifactType": row["artifact_type"],
        "confidence": row["confidence"],
        "reflectionNotes": row["reflection_notes"],
        "completedAt": row["completed_at"],
    }


def record_learning_completion(
    *,
    user_id: str,
    term_id: str,
    artifact_type: str,
    confidence: str,
    reflection_notes: str | None,
) -> dict[str, Any]:
    """
    Record a scenario/challenge completion. Idempotent on (user_id, term_id, artifact_type):
    a second call returns the existing row and awards no additional points.

    Returns: {"completion": {...}, "pointsAwarded": int, "alreadyCompleted": bool}
    """
    if artifact_type not in VALID_ARTIFACT_TYPES:
        raise ValueError(f"artifact_type must be one of {VALID_ARTIFACT_TYPES}")
    if confidence not in VALID_CONFIDENCE:
        raise ValueError(f"confidence must be one of {VALID_CONFIDENCE}")

    cleaned_notes = (reflection_notes or "").strip() or None
    event_type = ARTIFACT_TO_EVENT_TYPE[artifact_type]

    with get_connection() as connection:
        existing = connection.execute(
            """
            SELECT * FROM learning_completions
            WHERE user_id = %s AND term_id = %s AND artifact_type = %s
            """,
            (user_id, term_id, artifact_type),
        ).fetchone()

        if existing is not None:
            return {
                "completion": _map_completion_row(existing),
                "pointsAwarded": 0,
                "alreadyCompleted": True,
            }

        row = connection.execute(
            """
            INSERT INTO learning_completions (
                user_id, term_id, artifact_type, confidence, reflection_notes
            ) VALUES (%s, %s, %s, %s, %s)
            RETURNING *
            """,
            (user_id, term_id, artifact_type, confidence, cleaned_notes),
        ).fetchone()

        points = _record_contribution_event(
            connection=connection,
            contributor_id=user_id,
            draft_id=None,
            event_type=event_type,
            metadata={
                "termId": term_id,
                "confidence": confidence,
            },
            learning_completion_id=row["id"],
        )
        connection.commit()

    return {
        "completion": _map_completion_row(row),
        "pointsAwarded": points,
        "alreadyCompleted": False,
    }
