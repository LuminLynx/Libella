"""CompletionRepository — writes/reads against the `completions` table.

A completion records that a user finished a unit on its path. Idempotent
on (user_id, unit_id) — re-submission returns the existing row instead
of creating a duplicate (the table also enforces this with a UNIQUE
constraint, but we surface the existing row rather than raising).

Per-criterion grader output lives in the `grades` table and is written
by the grader service in Phase 2; this repository is intentionally
narrow to the completion event itself.
"""
from __future__ import annotations

from typing import Any

from ..db import get_connection


class UnitNotFoundError(Exception):
    """Raised when record_completion is called with an unknown unit_id."""


def _map_completion_row(row: Any) -> dict[str, Any]:
    return {
        "id": row["id"],
        "userId": row["user_id"],
        "pathId": row["path_id"],
        "unitId": row["unit_id"],
        "completedAt": row["completed_at"],
    }


def record_completion(user_id: str, unit_id: str) -> dict[str, Any]:
    """Record a completion for (user_id, unit_id). Idempotent.

    Returns {"completion": {...}, "alreadyCompleted": bool}. The path_id
    is looked up from the unit row — callers don't pass it.

    Raises UnitNotFoundError if no unit with that id exists.

    Concurrency: the INSERT uses ON CONFLICT DO NOTHING against the
    UNIQUE (user_id, unit_id) constraint from migration 019. A naive
    SELECT-then-INSERT would race under concurrent submissions for the
    same pair (both transactions see no row, both INSERT, second hits
    the unique constraint and surfaces an integrity error). The
    conflict path here re-reads the existing row and reports
    alreadyCompleted=True, matching the documented idempotent shape
    whether the duplicate came from a sequential retry or a race.
    """
    with get_connection() as connection:
        unit_row = connection.execute(
            "SELECT path_id FROM units WHERE id = %s",
            (unit_id,),
        ).fetchone()
        if unit_row is None:
            raise UnitNotFoundError(unit_id)
        path_id = unit_row["path_id"]

        row = connection.execute(
            """
            INSERT INTO completions (user_id, path_id, unit_id)
            VALUES (%s, %s, %s)
            ON CONFLICT (user_id, unit_id) DO NOTHING
            RETURNING *
            """,
            (user_id, path_id, unit_id),
        ).fetchone()

        if row is None:
            # Either a sequential re-submit or a concurrent insert just
            # won the race. The conflicting row is committed by the time
            # ON CONFLICT fires, so READ COMMITTED can see it now.
            row = connection.execute(
                "SELECT * FROM completions WHERE user_id = %s AND unit_id = %s",
                (user_id, unit_id),
            ).fetchone()
            connection.commit()
            return {
                "completion": _map_completion_row(row),
                "alreadyCompleted": True,
            }

        connection.commit()

    return {
        "completion": _map_completion_row(row),
        "alreadyCompleted": False,
    }


def list_completions(user_id: str) -> list[dict[str, Any]]:
    """Return every completion for `user_id`, newest first."""
    with get_connection() as connection:
        rows = connection.execute(
            """
            SELECT * FROM completions
            WHERE user_id = %s
            ORDER BY completed_at DESC, id DESC
            """,
            (user_id,),
        ).fetchall()
    return [_map_completion_row(row) for row in rows]
