from __future__ import annotations

import json
import re
import secrets
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

    return [_map_term_row(row) for row in rows]


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
#
# RESHAPE per docs/AUDIT.md §2.3: this becomes the path-centric Completion +
# Grade shape during Phase 1 reshape work. Kept here in current shape so the
# existing /api/v1/learning-completions endpoint still functions until the
# reshape PR replaces it. The contribution_events sidecar that used to dual-
# write here has been removed (contribution flow is cut per §5 #4).

VALID_ARTIFACT_TYPES = ("scenario", "challenge")
VALID_CONFIDENCE = ("low", "medium", "high")

SCENARIO_BASE_POINTS = 5
SCENARIO_MAX_POINTS = 10
CHALLENGE_MAX_POINTS = 15


def _map_completion_row(row: Any) -> dict[str, Any]:
    return {
        "id": row["id"],
        "userId": row["user_id"],
        "termId": row["term_id"],
        "artifactType": row["artifact_type"],
        "confidence": row["confidence"],
        "reflectionNotes": row["reflection_notes"],
        "taskStates": row["task_states"] if "task_states" in row.keys() else None,
        "challengeResponse": row["challenge_response"] if "challenge_response" in row.keys() else None,
        "criteriaGrades": row["criteria_grades"] if "criteria_grades" in row.keys() else None,
        "earnedPoints": row["earned_points"] if "earned_points" in row.keys() else 0,
        "completedAt": row["completed_at"],
    }


def _normalize_task_states(raw: Any) -> list[dict[str, Any]]:
    if not isinstance(raw, list):
        raise ValueError("taskStates must be a list")
    cleaned: list[dict[str, Any]] = []
    seen_indices: set[int] = set()
    for item in raw:
        if not isinstance(item, dict):
            raise ValueError("each task state must be an object")
        try:
            index = int(item["index"])
        except (KeyError, TypeError, ValueError) as error:
            raise ValueError("each task state requires an integer index") from error
        if index in seen_indices:
            raise ValueError(f"duplicate task index {index}")
        seen_indices.add(index)
        checked = bool(item.get("checked", False))
        note_raw = item.get("note")
        note = (str(note_raw).strip() or None) if note_raw is not None else None
        cleaned.append({"index": index, "checked": checked, "note": note})
    return cleaned


def _normalize_criteria_grades(raw: Any) -> list[dict[str, Any]]:
    if not isinstance(raw, list):
        raise ValueError("criteriaGrades must be a list")
    cleaned: list[dict[str, Any]] = []
    seen_indices: set[int] = set()
    for item in raw:
        if not isinstance(item, dict):
            raise ValueError("each criterion grade must be an object")
        try:
            index = int(item["index"])
        except (KeyError, TypeError, ValueError) as error:
            raise ValueError("each criterion grade requires an integer index") from error
        if index in seen_indices:
            raise ValueError(f"duplicate criterion index {index}")
        seen_indices.add(index)
        met = bool(item.get("met", False))
        note_raw = item.get("note")
        note = (str(note_raw).strip() or None) if note_raw is not None else None
        cleaned.append({"index": index, "met": met, "note": note})
    return cleaned


def compute_scenario_points(task_states: list[dict[str, Any]]) -> int:
    checked_count = sum(1 for state in task_states if state.get("checked"))
    return min(SCENARIO_MAX_POINTS, SCENARIO_BASE_POINTS + checked_count)


def compute_challenge_points(criteria_grades: list[dict[str, Any]]) -> int:
    if not criteria_grades:
        return 0
    met_count = sum(1 for grade in criteria_grades if grade.get("met"))
    raw = met_count / len(criteria_grades) * CHALLENGE_MAX_POINTS
    return int(round(raw))


def record_learning_completion(
    *,
    user_id: str,
    term_id: str,
    artifact_type: str,
    confidence: str,
    reflection_notes: str | None,
    task_states: list[dict[str, Any]] | None = None,
    challenge_response: str | None = None,
    criteria_grades: list[dict[str, Any]] | None = None,
) -> dict[str, Any]:
    """
    Record a scenario/challenge completion. Idempotent on (user_id, term_id, artifact_type):
    a second call returns the existing row and awards no additional points.

    Server computes earned_points from the engagement signals:
      - scenario: 5 base + 1 per checked task, capped at 10
      - challenge: round(criteria_met / total_criteria * 15)

    Returns: {"completion": {...}, "pointsAwarded": int, "alreadyCompleted": bool}
    """
    if artifact_type not in VALID_ARTIFACT_TYPES:
        raise ValueError(f"artifact_type must be one of {VALID_ARTIFACT_TYPES}")
    if confidence not in VALID_CONFIDENCE:
        raise ValueError(f"confidence must be one of {VALID_CONFIDENCE}")

    cleaned_notes = (reflection_notes or "").strip() or None

    if artifact_type == "scenario":
        if task_states is None:
            raise ValueError("scenario completions require taskStates")
        normalized_tasks = _normalize_task_states(task_states)
        if not any(t["checked"] for t in normalized_tasks):
            raise ValueError("scenario completions require at least one checked task")
        earned_points = compute_scenario_points(normalized_tasks)
        normalized_criteria: list[dict[str, Any]] | None = None
        cleaned_response: str | None = None
    else:  # challenge
        cleaned_response = (challenge_response or "").strip()
        if not cleaned_response:
            raise ValueError("challenge completions require a non-empty challengeResponse")
        if criteria_grades is None:
            raise ValueError("challenge completions require criteriaGrades")
        normalized_criteria = _normalize_criteria_grades(criteria_grades)
        if not normalized_criteria:
            raise ValueError("challenge completions require at least one criterion grade")
        earned_points = compute_challenge_points(normalized_criteria)
        normalized_tasks = None

    task_states_json = json.dumps(normalized_tasks) if normalized_tasks is not None else None
    criteria_grades_json = json.dumps(normalized_criteria) if normalized_criteria is not None else None

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
                user_id, term_id, artifact_type, confidence, reflection_notes,
                task_states, challenge_response, criteria_grades, earned_points
            ) VALUES (%s, %s, %s, %s, %s, %s::jsonb, %s, %s::jsonb, %s)
            RETURNING *
            """,
            (
                user_id, term_id, artifact_type, confidence, cleaned_notes,
                task_states_json, cleaned_response, criteria_grades_json, earned_points,
            ),
        ).fetchone()

        connection.commit()

    return {
        "completion": _map_completion_row(row),
        "pointsAwarded": earned_points,
        "alreadyCompleted": False,
    }
