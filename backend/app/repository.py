from __future__ import annotations

import json
from datetime import datetime, timedelta, timezone
from typing import Any

from .db import get_connection


def _parse_csv(value: str | None) -> list[str]:
    if not value:
        return []
    return [item.strip() for item in value.split(",") if item.strip()]


def list_terms() -> list[dict[str, Any]]:
    query = """
        SELECT id, term, short_definition, full_explanation, category_id,
               tags, related_terms, example_usage, source, created_at, updated_at
        FROM terms
        ORDER BY LOWER(term) ASC
    """
    with get_connection() as connection:
        rows = connection.execute(query).fetchall()
    return [_map_term_row(row) for row in rows]


def get_term_by_id(term_id: str) -> dict[str, Any] | None:
    query = """
        SELECT id, term, short_definition, full_explanation, category_id,
               tags, related_terms, example_usage, source, created_at, updated_at
        FROM terms
        WHERE id = %s
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
    query = """
        SELECT id, term, short_definition, full_explanation, category_id,
               tags, related_terms, example_usage, source, created_at, updated_at
        FROM terms
        WHERE category_id = %s
        ORDER BY LOWER(term) ASC
    """
    with get_connection() as connection:
        rows = connection.execute(query, (category_id,)).fetchall()
    return [_map_term_row(row) for row in rows]


def search_terms(raw_query: str) -> list[dict[str, Any]]:
    query = raw_query.strip()
    if not query:
        return []

    search_value = f"%{query}%"
    sql = """
        SELECT id, term, short_definition, full_explanation, category_id,
               tags, related_terms, example_usage, source, created_at, updated_at
        FROM terms
        WHERE term ILIKE %s
           OR short_definition ILIKE %s
           OR full_explanation ILIKE %s
           OR tags ILIKE %s
        ORDER BY LOWER(term) ASC
    """
    params = (search_value, search_value, search_value, search_value)
    with get_connection() as connection:
        rows = connection.execute(sql, params).fetchall()
    return [_map_term_row(row) for row in rows]


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


def _map_term_row(row: Any) -> dict[str, Any]:
    return {
        "id": row["id"],
        "term": row["term"],
        "shortDefinition": row["short_definition"],
        "fullExplanation": row["full_explanation"],
        "categoryId": row["category_id"],
        "tags": _parse_csv(row["tags"]),
        "relatedTerms": _parse_csv(row["related_terms"]),
        "exampleUsage": row["example_usage"],
        "source": row["source"],
        "createdAt": row["created_at"],
        "updatedAt": row["updated_at"],
    }
