"""Phase 2 — F4 grader unit tests.

Two layers:
  * `test_grader_validation_*`: pure-Python tests on `_validate_grader_output`,
    enforcing T2-D guardrails on the grader's tool-call payload.
  * `test_post_grade_*`: endpoint tests with `grade_decision_answer`
    monkey-patched. No Anthropic API calls, no DB unless `gated_db`.

Live grader-vs-Anthropic tests live under the regression-set runner
(P2.2), not here — those calls cost real money and run on demand.
"""
from __future__ import annotations

from typing import Any

import pytest
from fastapi.testclient import TestClient

from app import ai_service
from app.ai_service import (
    AIServiceError,
    GraderOutput,
    _validate_grader_output,
)
from app.auth import create_access_token
from app.main import app
from app.repositories import (
    completion_repository,
    grade_repository,
    unit_repository,
)


@pytest.fixture
def client() -> TestClient:
    return TestClient(app)


@pytest.fixture
def auth_header() -> dict[str, str]:
    token = create_access_token("u-grader-test")
    return {"Authorization": f"Bearer {token}"}


# ---------------------------------------------------------------------------
# Validator: T2-D guardrails on grader payload
# ---------------------------------------------------------------------------


def _grade(criterion_id: int, **overrides: Any) -> dict[str, Any]:
    base = {
        "criterion_id": criterion_id,
        "met": True,
        "confidence": 0.9,
        "rationale": "Quote covers the criterion.",
        "answer_quote": "the quoted span",
    }
    base.update(overrides)
    return base


def test_validator_accepts_well_formed_payload() -> None:
    payload = {"grades": [_grade(1), _grade(2)], "flagged": False}
    out = _validate_grader_output(payload, expected_criterion_ids={1, 2})
    assert isinstance(out, GraderOutput)
    assert out.flagged is False
    assert {g["criterion_id"] for g in out.grades} == {1, 2}


def test_validator_overrides_flagged_when_any_confidence_low() -> None:
    payload = {
        "grades": [_grade(1, confidence=0.95), _grade(2, met=False, confidence=0.4, answer_quote="")],
        "flagged": False,
    }
    out = _validate_grader_output(payload, expected_criterion_ids={1, 2})
    assert out.flagged is True, "below-threshold confidence must force flagged=true"


def test_validator_rejects_missing_criterion() -> None:
    payload = {"grades": [_grade(1)], "flagged": False}
    with pytest.raises(AIServiceError, match="did not return grades"):
        _validate_grader_output(payload, expected_criterion_ids={1, 2})


def test_validator_rejects_unknown_criterion() -> None:
    payload = {"grades": [_grade(99)], "flagged": False}
    with pytest.raises(AIServiceError, match="unknown criterion_id"):
        _validate_grader_output(payload, expected_criterion_ids={1})


def test_validator_rejects_duplicate_criterion() -> None:
    payload = {"grades": [_grade(1), _grade(1)], "flagged": False}
    with pytest.raises(AIServiceError, match="duplicate grade"):
        _validate_grader_output(payload, expected_criterion_ids={1})


def test_validator_rejects_met_without_answer_quote() -> None:
    # T2-D answer-quote guardrail: a Met grade must point at the supporting span.
    payload = {"grades": [_grade(1, met=True, answer_quote="")], "flagged": False}
    with pytest.raises(AIServiceError, match="answer_quote is empty"):
        _validate_grader_output(payload, expected_criterion_ids={1})


def test_validator_rejects_out_of_range_confidence() -> None:
    payload = {"grades": [_grade(1, confidence=1.5)], "flagged": False}
    with pytest.raises(AIServiceError, match="invalid confidence"):
        _validate_grader_output(payload, expected_criterion_ids={1})


def test_validator_rejects_missing_rationale() -> None:
    payload = {"grades": [_grade(1, rationale="")], "flagged": False}
    with pytest.raises(AIServiceError, match="missing rationale"):
        _validate_grader_output(payload, expected_criterion_ids={1})


def test_validator_rejects_top_level_missing_flagged() -> None:
    payload = {"grades": [_grade(1)]}
    with pytest.raises(AIServiceError, match="missing 'grades'"):
        _validate_grader_output(payload, expected_criterion_ids={1})


def test_extract_usage_pulls_known_fields() -> None:
    """Real Anthropic responses carry usage on a SDK Pydantic model.
    `_extract_usage` must read the cache-related fields too; the
    Phase 2 gate uses them as cost-relevance signal.
    """
    from app.ai_service import _extract_usage

    class _Usage:
        input_tokens = 1500
        output_tokens = 200
        cache_creation_input_tokens = 0
        cache_read_input_tokens = 1300

    class _Response:
        usage = _Usage()

    out = _extract_usage(_Response())
    assert out == {
        "input_tokens": 1500,
        "output_tokens": 200,
        "cache_creation_input_tokens": 0,
        "cache_read_input_tokens": 1300,
    }


def test_extract_usage_returns_empty_when_response_lacks_usage() -> None:
    from app.ai_service import _extract_usage

    class _ResponseNoUsage:
        pass

    assert _extract_usage(_ResponseNoUsage()) == {}


# ---------------------------------------------------------------------------
# Endpoint
# ---------------------------------------------------------------------------


def test_grade_endpoint_requires_auth(client: TestClient) -> None:
    response = client.post("/api/v1/units/u-1/grade", json={"answer": "any"})
    assert response.status_code == 401
    assert response.json()["detail"]["code"] == "AUTH_REQUIRED"


def test_grade_endpoint_returns_404_for_unknown_unit(
    client: TestClient, monkeypatch: pytest.MonkeyPatch, auth_header: dict[str, str]
) -> None:
    monkeypatch.setattr(unit_repository, "get_unit", lambda unit_id: None)
    response = client.post(
        "/api/v1/units/no-such-unit/grade",
        json={"answer": "anything"},
        headers=auth_header,
    )
    assert response.status_code == 404
    assert response.json()["error"]["code"] == "UNIT_NOT_FOUND"


def test_grade_endpoint_returns_409_when_unit_has_no_rubric(
    client: TestClient, monkeypatch: pytest.MonkeyPatch, auth_header: dict[str, str]
) -> None:
    monkeypatch.setattr(
        unit_repository,
        "get_unit",
        lambda unit_id: {"id": unit_id, "rubric": {"criteria": []}},
    )
    response = client.post(
        "/api/v1/units/u-1/grade",
        json={"answer": "anything"},
        headers=auth_header,
    )
    assert response.status_code == 409
    assert response.json()["error"]["code"] == "UNIT_NOT_GRADABLE"


def test_grade_endpoint_returns_502_when_grader_fails(
    client: TestClient, monkeypatch: pytest.MonkeyPatch, auth_header: dict[str, str]
) -> None:
    monkeypatch.setattr(
        unit_repository,
        "get_unit",
        lambda unit_id: {
            "id": unit_id,
            "rubric": {"criteria": [{"id": 1, "text": "c1"}]},
        },
    )

    def _raise(*_args: Any, **_kwargs: Any) -> None:
        raise AIServiceError("synthetic failure", code="AI_REQUEST_FAILED")

    monkeypatch.setattr("app.main.grade_decision_answer", _raise)

    response = client.post(
        "/api/v1/units/u-1/grade",
        json={"answer": "anything"},
        headers=auth_header,
    )
    assert response.status_code == 502
    assert response.json()["error"]["code"] == "AI_REQUEST_FAILED"


def test_grade_endpoint_persists_completion_and_grades(
    client: TestClient, monkeypatch: pytest.MonkeyPatch, auth_header: dict[str, str]
) -> None:
    captured: dict[str, Any] = {}

    monkeypatch.setattr(
        unit_repository,
        "get_unit",
        lambda unit_id: {
            "id": unit_id,
            "title": "Tokenization",
            "rubric": {"criteria": [{"id": 11, "text": "c1"}, {"id": 12, "text": "c2"}]},
        },
    )

    def _grader(unit: dict[str, Any], answer: str) -> GraderOutput:
        captured["answer"] = answer
        return GraderOutput(
            grades=[
                {
                    "criterion_id": 11,
                    "met": True,
                    "confidence": 0.9,
                    "rationale": "good quote",
                    "answer_quote": "tokens not characters",
                },
                {
                    "criterion_id": 12,
                    "met": False,
                    "confidence": 0.85,
                    "rationale": "missed",
                    "answer_quote": "",
                },
            ],
            flagged=False,
        )

    def _record_completion(user_id: str, unit_id: str) -> dict[str, Any]:
        captured["completion_user"] = user_id
        return {
            "completion": {
                "id": 42,
                "userId": user_id,
                "pathId": "p1",
                "unitId": unit_id,
                "completedAt": None,
            },
            "alreadyCompleted": False,
        }

    def _upsert(completion_id: int, grades: list[dict[str, Any]], flagged: bool) -> list[dict[str, Any]]:
        captured["completion_id"] = completion_id
        captured["flagged"] = flagged
        return [
            {
                "id": i,
                "completionId": completion_id,
                "criterionId": g["criterion_id"],
                "met": g["met"],
                "confidence": g["confidence"],
                "rationale": g["rationale"],
                "flagged": flagged,
                "createdAt": None,
            }
            for i, g in enumerate(grades, start=1)
        ]

    monkeypatch.setattr("app.main.grade_decision_answer", _grader)
    monkeypatch.setattr(completion_repository, "record_completion", _record_completion)
    monkeypatch.setattr(grade_repository, "upsert_grades", _upsert)

    response = client.post(
        "/api/v1/units/u-1/grade",
        json={"answer": "tokens not characters or words"},
        headers=auth_header,
    )
    assert response.status_code == 200
    body = response.json()["data"]
    assert body["completion"]["id"] == 42
    assert body["flagged"] is False
    assert {g["criterionId"] for g in body["grades"]} == {11, 12}
    assert {q["criterionId"] for q in body["answerQuotes"]} == {11, 12}
    assert captured["answer"] == "tokens not characters or words"
    assert captured["completion_id"] == 42


def test_grade_request_rejects_empty_answer(
    client: TestClient, auth_header: dict[str, str]
) -> None:
    response = client.post(
        "/api/v1/units/u-1/grade",
        json={"answer": ""},
        headers=auth_header,
    )
    # Pydantic Field min_length=1 → 422 before any handler runs.
    assert response.status_code == 422


# ---------------------------------------------------------------------------
# Repository — Postgres-gated
# ---------------------------------------------------------------------------


def test_upsert_drops_grades_for_criteria_not_in_submission(gated_db) -> None:
    """A re-grade with a different criterion set must not leave stale rows.

    Rubric versioning (chunk 6) means the criterion ids the grader sees
    today may differ from the ids it saw on a previous attempt. The
    repository must DELETE prior rows whose criterion id isn't in the
    incoming submission, atomically with the UPSERT.
    """
    from .conftest import seed_path_with_units

    seed = seed_path_with_units(gated_db)
    user_id = seed["user_id"]
    unit_id = seed["unit_a_id"]
    crit_a, crit_b = seed["criterion_ids"]

    # First grade — both criteria present.
    completion = completion_repository.record_completion(user_id=user_id, unit_id=unit_id)["completion"]
    grade_repository.upsert_grades(
        completion_id=completion["id"],
        grades=[
            {"criterion_id": crit_a, "met": True, "confidence": 0.9, "rationale": "ok", "answer_quote": "x"},
            {"criterion_id": crit_b, "met": False, "confidence": 0.7, "rationale": "no", "answer_quote": ""},
        ],
        flagged=False,
    )

    first = grade_repository.list_grades_for_completion(completion["id"])
    assert {g["criterionId"] for g in first} == {crit_a, crit_b}

    # Second grade — criterion B is no longer in the submission. The
    # repository must remove the stale (completion_id, crit_b) row.
    grade_repository.upsert_grades(
        completion_id=completion["id"],
        grades=[
            {"criterion_id": crit_a, "met": True, "confidence": 0.95, "rationale": "still ok", "answer_quote": "y"},
        ],
        flagged=False,
    )

    second = grade_repository.list_grades_for_completion(completion["id"])
    assert {g["criterionId"] for g in second} == {crit_a}, (
        "stale criterion row should be deleted on re-grade"
    )
    assert second[0]["confidence"] == pytest.approx(0.95)
