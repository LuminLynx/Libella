from __future__ import annotations

import pytest

from app.repository import (
    CHALLENGE_MAX_POINTS,
    SCENARIO_BASE_POINTS,
    SCENARIO_MAX_POINTS,
    _normalize_criteria_grades,
    _normalize_task_states,
    compute_challenge_points,
    compute_scenario_points,
)


# ---- Scenario scoring: 5 base + 1 per checked task, capped at 10 ----


def _task(index: int, checked: bool = False, note: str | None = None) -> dict:
    return {"index": index, "checked": checked, "note": note}


def test_scenario_zero_tasks_checked_is_base() -> None:
    tasks = [_task(0), _task(1), _task(2)]
    # zero checked → base only (note: endpoint validation rejects this; pure formula here)
    assert compute_scenario_points(tasks) == SCENARIO_BASE_POINTS


def test_scenario_partial_tasks() -> None:
    tasks = [_task(0, True), _task(1, True), _task(2)]
    assert compute_scenario_points(tasks) == SCENARIO_BASE_POINTS + 2


def test_scenario_caps_at_max() -> None:
    # 8 tasks all checked → would be 5 + 8 = 13 → capped at 10
    tasks = [_task(i, True) for i in range(8)]
    assert compute_scenario_points(tasks) == SCENARIO_MAX_POINTS


def test_scenario_three_checked_three_total() -> None:
    tasks = [_task(0, True), _task(1, True), _task(2, True)]
    assert compute_scenario_points(tasks) == 8


# ---- Challenge scoring: round(criteria_met / total_criteria * 15) ----


def _criterion(index: int, met: bool = False, note: str | None = None) -> dict:
    return {"index": index, "met": met, "note": note}


def test_challenge_zero_criteria_is_zero() -> None:
    assert compute_challenge_points([]) == 0


def test_challenge_all_met_is_max() -> None:
    grades = [_criterion(i, True) for i in range(3)]
    assert compute_challenge_points(grades) == CHALLENGE_MAX_POINTS


def test_challenge_none_met_is_zero() -> None:
    grades = [_criterion(i, False) for i in range(3)]
    assert compute_challenge_points(grades) == 0


def test_challenge_partial_three_criteria() -> None:
    # 2 of 3 met → 2/3 * 15 = 10
    grades = [_criterion(0, True), _criterion(1, True), _criterion(2, False)]
    assert compute_challenge_points(grades) == 10


def test_challenge_one_of_four_met() -> None:
    # 1/4 * 15 = 3.75 → round to 4
    grades = [_criterion(0, True), _criterion(1), _criterion(2), _criterion(3)]
    assert compute_challenge_points(grades) == 4


def test_challenge_caps_naturally_at_max() -> None:
    # 5 of 5 met → 15 (no overflow even with more criteria)
    grades = [_criterion(i, True) for i in range(5)]
    assert compute_challenge_points(grades) == CHALLENGE_MAX_POINTS


# ---- Normalizers ----


def test_normalize_task_states_strips_notes_and_dedups() -> None:
    raw = [
        {"index": 0, "checked": True, "note": "  hello  "},
        {"index": 1, "checked": False, "note": ""},
    ]
    normalized = _normalize_task_states(raw)
    assert normalized[0] == {"index": 0, "checked": True, "note": "hello"}
    assert normalized[1] == {"index": 1, "checked": False, "note": None}


def test_normalize_task_states_rejects_duplicate_index() -> None:
    with pytest.raises(ValueError):
        _normalize_task_states([{"index": 0, "checked": True}, {"index": 0, "checked": False}])


def test_normalize_task_states_rejects_non_list() -> None:
    with pytest.raises(ValueError):
        _normalize_task_states("not a list")  # type: ignore[arg-type]


def test_normalize_task_states_rejects_non_int_index() -> None:
    with pytest.raises(ValueError):
        _normalize_task_states([{"index": "zero", "checked": True}])


def test_normalize_criteria_grades_strips_and_dedups() -> None:
    raw = [
        {"index": 0, "met": True, "note": "  yes  "},
        {"index": 1, "met": False},
    ]
    normalized = _normalize_criteria_grades(raw)
    assert normalized[0] == {"index": 0, "met": True, "note": "yes"}
    assert normalized[1] == {"index": 1, "met": False, "note": None}


def test_normalize_criteria_grades_rejects_duplicate_index() -> None:
    with pytest.raises(ValueError):
        _normalize_criteria_grades([{"index": 0, "met": True}, {"index": 0, "met": False}])
