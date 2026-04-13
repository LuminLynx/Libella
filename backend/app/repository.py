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
        ORDER BY term COLLATE NOCASE ASC
    """
    with get_connection() as connection:
        rows = connection.execute(query).fetchall()
    return [_map_term_row(row) for row in rows]


def get_term_by_id(term_id: str) -> dict[str, Any] | None:
    query = """
        SELECT id, term, short_definition, full_explanation, category_id,
               tags, related_terms, example_usage, source, created_at, updated_at
        FROM terms
        WHERE id = ?
    """
    with get_connection() as connection:
        row = connection.execute(query, (term_id,)).fetchone()
    return _map_term_row(row) if row else None


def list_categories() -> list[dict[str, Any]]:
    query = """
        SELECT id, name, description
        FROM categories
        ORDER BY name COLLATE NOCASE ASC
    """
    with get_connection() as connection:
        rows = connection.execute(query).fetchall()
    return [dict(row) for row in rows]


def list_terms_by_category(category_id: str) -> list[dict[str, Any]]:
    query = """
        SELECT id, term, short_definition, full_explanation, category_id,
               tags, related_terms, example_usage, source, created_at, updated_at
        FROM terms
        WHERE category_id = ?
        ORDER BY term COLLATE NOCASE ASC
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
        WHERE term LIKE ? COLLATE NOCASE
           OR short_definition LIKE ? COLLATE NOCASE
           OR full_explanation LIKE ? COLLATE NOCASE
           OR tags LIKE ? COLLATE NOCASE
        ORDER BY term COLLATE NOCASE ASC
    """
    params = (search_value, search_value, search_value, search_value)
    with get_connection() as connection:
        rows = connection.execute(sql, params).fetchall()
    return [_map_term_row(row) for row in rows]


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
