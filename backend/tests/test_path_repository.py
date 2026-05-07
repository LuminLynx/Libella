"""Integration tests for PathRepository (Postgres-gated on TEST_DATABASE_URL)."""
from __future__ import annotations

from .conftest import seed_path_with_units


def test_get_path_returns_path_with_unit_manifest(gated_db) -> None:
    seed = seed_path_with_units(gated_db)

    from app.repositories import path_repository

    path = path_repository.get_path(seed["path_id"])

    assert path is not None
    assert path["id"] == "path-llm-pms"
    assert path["slug"] == "llm-systems-for-pms"
    assert path["title"] == "LLM Systems for PMs"
    units = path["units"]
    assert [u["id"] for u in units] == ["unit-a", "unit-b"]
    assert [u["position"] for u in units] == [1, 2]
    # Manifest only — no bite/depth/sources.
    assert "biteMd" not in units[0]
    assert set(units[0].keys()) == {"id", "slug", "title", "position", "status"}


def test_get_path_returns_none_for_unknown_id(gated_db) -> None:
    from app.repositories import path_repository

    assert path_repository.get_path("does-not-exist") is None


def test_list_paths_returns_every_path(gated_db) -> None:
    seed_path_with_units(gated_db)

    from app.repositories import path_repository

    paths = path_repository.list_paths()
    assert len(paths) == 1
    assert paths[0]["id"] == "path-llm-pms"


def test_next_unit_for_user_returns_first_uncompleted(gated_db) -> None:
    seed = seed_path_with_units(gated_db)

    from app.repositories import completion_repository, path_repository

    # No completions yet -> next is unit-a (position 1).
    nxt = path_repository.next_unit_for_user(seed["user_id"], seed["path_id"])
    assert nxt is not None
    assert nxt["id"] == "unit-a"

    # After completing unit-a, next is unit-b.
    completion_repository.record_completion(seed["user_id"], "unit-a")
    nxt = path_repository.next_unit_for_user(seed["user_id"], seed["path_id"])
    assert nxt is not None
    assert nxt["id"] == "unit-b"

    # After completing both, next is None.
    completion_repository.record_completion(seed["user_id"], "unit-b")
    assert path_repository.next_unit_for_user(seed["user_id"], seed["path_id"]) is None


def test_next_unit_for_user_returns_none_for_path_with_no_units(gated_db) -> None:
    seed = seed_path_with_units(gated_db)

    with gated_db() as conn:
        conn.execute(
            "INSERT INTO paths (id, slug, title) VALUES ('empty', 'empty-path', 'Empty')"
        )
        conn.commit()

    from app.repositories import path_repository

    assert path_repository.next_unit_for_user(seed["user_id"], "empty") is None
