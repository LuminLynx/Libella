"""Integration tests for CompletionRepository (Postgres-gated on TEST_DATABASE_URL)."""
from __future__ import annotations

import pytest

from .conftest import seed_path_with_units


def test_record_completion_inserts_new_row(gated_db) -> None:
    seed = seed_path_with_units(gated_db)

    from app.repositories import completion_repository

    result = completion_repository.record_completion(seed["user_id"], "unit-a")

    assert result["alreadyCompleted"] is False
    assert result["completion"]["userId"] == "u-test"
    assert result["completion"]["unitId"] == "unit-a"
    assert result["completion"]["pathId"] == "path-llm-pms"


def test_record_completion_is_idempotent(gated_db) -> None:
    seed = seed_path_with_units(gated_db)

    from app.repositories import completion_repository

    first = completion_repository.record_completion(seed["user_id"], "unit-a")
    second = completion_repository.record_completion(seed["user_id"], "unit-a")

    assert first["alreadyCompleted"] is False
    assert second["alreadyCompleted"] is True
    assert second["completion"]["id"] == first["completion"]["id"]
    assert second["completion"]["completedAt"] == first["completion"]["completedAt"]

    # And only one row exists in the underlying table.
    with gated_db() as conn:
        rows = conn.execute(
            "SELECT * FROM completions WHERE user_id = %s AND unit_id = %s",
            (seed["user_id"], "unit-a"),
        ).fetchall()
    assert len(rows) == 1


def test_record_completion_raises_for_unknown_unit(gated_db) -> None:
    seed = seed_path_with_units(gated_db)

    from app.repositories import completion_repository

    with pytest.raises(completion_repository.UnitNotFoundError):
        completion_repository.record_completion(seed["user_id"], "no-such-unit")


def test_list_completions_returns_newest_first(gated_db) -> None:
    seed = seed_path_with_units(gated_db)

    from app.repositories import completion_repository

    completion_repository.record_completion(seed["user_id"], "unit-a")
    completion_repository.record_completion(seed["user_id"], "unit-b")

    rows = completion_repository.list_completions(seed["user_id"])
    assert [r["unitId"] for r in rows] == ["unit-b", "unit-a"]


def test_list_completions_returns_empty_for_no_completions(gated_db) -> None:
    seed = seed_path_with_units(gated_db)

    from app.repositories import completion_repository

    assert completion_repository.list_completions(seed["user_id"]) == []
