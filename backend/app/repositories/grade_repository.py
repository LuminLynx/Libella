"""GradeRepository — writes/reads against the `grades` table.

Per docs/STRATEGY.md § T2-A, grades are per-criterion (Met / Not Met
with confidence + rationale + answer-quote), never holistic. The
schema lives in migration 019:

    grades (
        id BIGSERIAL PK,
        completion_id BIGINT FK -> completions.id ON DELETE CASCADE,
        criterion_id BIGINT FK -> rubric_criteria.id ON DELETE CASCADE,
        met BOOLEAN, confidence REAL, rationale TEXT,
        flagged BOOLEAN, created_at TIMESTAMPTZ,
        UNIQUE (completion_id, criterion_id)
    )

`flagged` is repeated on every grade row in this table by design — the
schema doesn't carry a top-level "this submission was flagged" column,
so we write the same flagged value on every row in a single submission.
A higher-level read can decide whether to surface "any criterion
flagged" or "all criteria flagged"; today we set the same value
across, since the grader emits one top-level flagged per call.

Every submission UPSERTs by (completion_id, criterion_id): re-grading
the same completion replaces the prior row in place. Older grades for
that pair are not retained — the unique constraint enforces a single
current grade per criterion. If history is needed later, it can be
added via an `attempts` table without touching this repository.

`answer_quote` is captured by the grader (T2-D guardrail) but not
persisted on the row today — the column doesn't exist in migration
019. Storing it requires a future migration; for the Phase 2 gate we
return it in the API response from the grader output but don't save
it. Tracked as a follow-up.
"""
from __future__ import annotations

from typing import Any

from ..db import get_connection


def _map_grade_row(row: Any) -> dict[str, Any]:
    return {
        "id": row["id"],
        "completionId": row["completion_id"],
        "criterionId": row["criterion_id"],
        "met": row["met"],
        "confidence": float(row["confidence"]),
        "rationale": row["rationale"],
        "flagged": row["flagged"],
        "createdAt": row["created_at"],
    }


def upsert_grades(
    completion_id: int,
    grades: list[dict[str, Any]],
    flagged: bool,
) -> list[dict[str, Any]]:
    """Replace this completion's grades atomically.

    Each grade is { criterion_id, met, confidence, rationale, ... }.
    Returns the persisted rows in the order grades were submitted.
    """
    if not grades:
        return []

    persisted: list[dict[str, Any]] = []
    with get_connection() as connection:
        for grade in grades:
            row = connection.execute(
                """
                INSERT INTO grades (completion_id, criterion_id, met, confidence, rationale, flagged)
                VALUES (%s, %s, %s, %s, %s, %s)
                ON CONFLICT (completion_id, criterion_id) DO UPDATE SET
                    met = EXCLUDED.met,
                    confidence = EXCLUDED.confidence,
                    rationale = EXCLUDED.rationale,
                    flagged = EXCLUDED.flagged
                RETURNING *
                """,
                (
                    completion_id,
                    grade["criterion_id"],
                    grade["met"],
                    grade["confidence"],
                    grade["rationale"],
                    flagged,
                ),
            ).fetchone()
            persisted.append(_map_grade_row(row))
        connection.commit()
    return persisted


def list_grades_for_completion(completion_id: int) -> list[dict[str, Any]]:
    """Return all grades for a completion, ordered by criterion position."""
    with get_connection() as connection:
        rows = connection.execute(
            """
            SELECT g.*
            FROM grades g
            JOIN rubric_criteria rc ON rc.id = g.criterion_id
            WHERE g.completion_id = %s
            ORDER BY rc.position ASC
            """,
            (completion_id,),
        ).fetchall()
    return [_map_grade_row(row) for row in rows]
