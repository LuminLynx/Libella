"""PathRepository — reads over the `paths` and `units` tables.

Path is the canonical curriculum track ("LLM Systems for PMs" in v1).
A path is a sequenced list of units; the unit list returned here is a
manifest (id/slug/title/position/status), not the full 9-slot payload —
that lives in UnitRepository.get_unit.
"""
from __future__ import annotations

from typing import Any

from ..db import get_connection


def _map_path_row(row: Any) -> dict[str, Any]:
    return {
        "id": row["id"],
        "slug": row["slug"],
        "title": row["title"],
        "description": row["description"],
        "createdAt": row["created_at"],
        "updatedAt": row["updated_at"],
    }


def _map_unit_manifest_row(row: Any) -> dict[str, Any]:
    return {
        "id": row["id"],
        "slug": row["slug"],
        "title": row["title"],
        "position": row["position"],
        "status": row["status"],
    }


def get_path(path_id: str) -> dict[str, Any] | None:
    """Return the path with its sequenced unit manifest, or None."""
    with get_connection() as connection:
        path_row = connection.execute(
            "SELECT * FROM paths WHERE id = %s",
            (path_id,),
        ).fetchone()
        if path_row is None:
            return None
        unit_rows = connection.execute(
            """
            SELECT id, slug, title, position, status
            FROM units
            WHERE path_id = %s
            ORDER BY position ASC
            """,
            (path_id,),
        ).fetchall()

    path = _map_path_row(path_row)
    path["units"] = [_map_unit_manifest_row(row) for row in unit_rows]
    return path


def list_paths() -> list[dict[str, Any]]:
    """Return every path, ordered by title (case-insensitive)."""
    with get_connection() as connection:
        rows = connection.execute(
            "SELECT * FROM paths ORDER BY LOWER(title) ASC",
        ).fetchall()
    return [_map_path_row(row) for row in rows]


def next_unit_for_user(user_id: str, path_id: str) -> dict[str, Any] | None:
    """Return the next uncompleted unit on `path_id` for `user_id`.

    "Next" = lowest `position` whose unit id is not yet in `completions`
    for this user. Returns the manifest shape (id/slug/title/position/status).
    Returns None when the user has completed every unit on the path (or the
    path has no units).
    """
    query = """
        SELECT u.id, u.slug, u.title, u.position, u.status
        FROM units u
        WHERE u.path_id = %s
          AND NOT EXISTS (
              SELECT 1 FROM completions c
              WHERE c.user_id = %s AND c.unit_id = u.id
          )
        ORDER BY u.position ASC
        LIMIT 1
    """
    with get_connection() as connection:
        row = connection.execute(query, (path_id, user_id)).fetchone()
    return _map_unit_manifest_row(row) if row else None
