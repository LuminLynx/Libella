"""FastAPI endpoint tests for the path-centric surface.

The 401/404 tests run without a database by stubbing repository functions
and overriding the auth dependency. The end-to-end happy-path test is
gated on TEST_DATABASE_URL via the `gated_db` fixture in conftest.
"""
from __future__ import annotations

from typing import Any

import pytest
from fastapi.testclient import TestClient

from app.auth import create_access_token
from app.main import app
from app.repositories import (
    completion_repository,
    path_repository,
    unit_repository,
)

from .conftest import seed_path_with_units


@pytest.fixture
def client() -> TestClient:
    return TestClient(app)


@pytest.fixture
def auth_header() -> dict[str, str]:
    token = create_access_token("u-endpoint-test")
    return {"Authorization": f"Bearer {token}"}


# ----- 401 (auth required) -----


def test_units_endpoint_requires_auth(client: TestClient) -> None:
    response = client.get("/api/v1/units/any-id")
    assert response.status_code == 401
    assert response.json()["detail"]["code"] == "AUTH_REQUIRED"


def test_completions_endpoint_requires_auth(client: TestClient) -> None:
    response = client.post("/api/v1/completions", json={"unitId": "any-id"})
    assert response.status_code == 401
    assert response.json()["detail"]["code"] == "AUTH_REQUIRED"


def test_list_completions_endpoint_requires_auth(client: TestClient) -> None:
    response = client.get("/api/v1/completions")
    assert response.status_code == 401
    assert response.json()["detail"]["code"] == "AUTH_REQUIRED"


def test_list_completions_returns_user_completions(
    client: TestClient, monkeypatch: pytest.MonkeyPatch, auth_header: dict[str, str]
) -> None:
    captured: dict[str, Any] = {}

    def _list(user_id: str) -> list[dict[str, Any]]:
        captured["user_id"] = user_id
        return [
            {"id": 2, "userId": user_id, "pathId": "p1", "unitId": "u2", "completedAt": None},
            {"id": 1, "userId": user_id, "pathId": "p1", "unitId": "u1", "completedAt": None},
        ]

    monkeypatch.setattr(completion_repository, "list_completions", _list)

    response = client.get("/api/v1/completions", headers=auth_header)
    assert response.status_code == 200
    body = response.json()
    assert [row["unitId"] for row in body["data"]] == ["u2", "u1"]
    assert "user_id" in captured


# ----- 404 (resource missing) — repository monkeypatched, no DB needed -----


def test_get_path_returns_404_envelope(
    client: TestClient, monkeypatch: pytest.MonkeyPatch
) -> None:
    monkeypatch.setattr(path_repository, "get_path", lambda _id: None)
    response = client.get("/api/v1/paths/missing")
    assert response.status_code == 404
    body = response.json()
    assert body["data"] is None
    assert body["error"]["code"] == "PATH_NOT_FOUND"
    assert "missing" in body["error"]["message"]


def test_get_unit_returns_404_envelope(
    client: TestClient, monkeypatch: pytest.MonkeyPatch, auth_header: dict[str, str]
) -> None:
    monkeypatch.setattr(unit_repository, "get_unit", lambda _id: None)
    response = client.get("/api/v1/units/missing", headers=auth_header)
    assert response.status_code == 404
    body = response.json()
    assert body["data"] is None
    assert body["error"]["code"] == "UNIT_NOT_FOUND"


def test_post_completion_returns_404_for_unknown_unit(
    client: TestClient, monkeypatch: pytest.MonkeyPatch, auth_header: dict[str, str]
) -> None:
    def _raise(*_args: Any, **_kwargs: Any) -> Any:
        raise completion_repository.UnitNotFoundError("missing")

    monkeypatch.setattr(completion_repository, "record_completion", _raise)
    response = client.post(
        "/api/v1/completions",
        json={"unitId": "missing"},
        headers=auth_header,
    )
    assert response.status_code == 404
    assert response.json()["error"]["code"] == "UNIT_NOT_FOUND"


# ----- Happy paths via stubbed repositories (no DB) -----


def test_get_path_returns_data_envelope(
    client: TestClient, monkeypatch: pytest.MonkeyPatch
) -> None:
    fake_path = {
        "id": "p1",
        "slug": "s",
        "title": "T",
        "description": "",
        "createdAt": None,
        "updatedAt": None,
        "units": [
            {"id": "u1", "slug": "x", "title": "X", "position": 1, "status": "published"},
        ],
    }
    monkeypatch.setattr(path_repository, "get_path", lambda _id: fake_path)
    response = client.get("/api/v1/paths/p1")
    assert response.status_code == 200
    body = response.json()
    assert body["error"] is None
    assert body["data"]["units"][0]["id"] == "u1"


def test_post_completion_returns_201_for_new_and_200_for_repeat(
    client: TestClient, monkeypatch: pytest.MonkeyPatch, auth_header: dict[str, str]
) -> None:
    state = {"called": 0}

    def _record(user_id: str, unit_id: str) -> dict[str, Any]:
        state["called"] += 1
        return {
            "completion": {
                "id": 1,
                "userId": user_id,
                "pathId": "p1",
                "unitId": unit_id,
                "completedAt": None,
            },
            "alreadyCompleted": state["called"] > 1,
        }

    monkeypatch.setattr(completion_repository, "record_completion", _record)

    first = client.post("/api/v1/completions", json={"unitId": "u1"}, headers=auth_header)
    assert first.status_code == 201
    assert first.json()["data"]["alreadyCompleted"] is False

    second = client.post("/api/v1/completions", json={"unitId": "u1"}, headers=auth_header)
    assert second.status_code == 200
    assert second.json()["data"]["alreadyCompleted"] is True


# ----- DB-gated end-to-end -----


def test_end_to_end_flow_against_real_db(
    client: TestClient, gated_db, auth_header
) -> None:
    seed = seed_path_with_units(gated_db)
    # Auth identity must match the seeded user so the completion FK resolves.
    auth_header = {"Authorization": f"Bearer {create_access_token(seed['user_id'])}"}

    path_resp = client.get(f"/api/v1/paths/{seed['path_id']}")
    assert path_resp.status_code == 200
    assert [u["id"] for u in path_resp.json()["data"]["units"]] == ["unit-a", "unit-b"]

    unit_resp = client.get(f"/api/v1/units/{seed['unit_a_id']}", headers=auth_header)
    assert unit_resp.status_code == 200
    unit = unit_resp.json()["data"]
    assert unit["title"] == "Tokenization"
    assert len(unit["sources"]) == 2
    assert unit["rubric"]["version"] == 1

    post_resp = client.post(
        "/api/v1/completions",
        json={"unitId": "unit-a"},
        headers=auth_header,
    )
    assert post_resp.status_code == 201
    assert post_resp.json()["data"]["completion"]["unitId"] == "unit-a"

    repeat = client.post(
        "/api/v1/completions",
        json={"unitId": "unit-a"},
        headers=auth_header,
    )
    assert repeat.status_code == 200
    assert repeat.json()["data"]["alreadyCompleted"] is True
