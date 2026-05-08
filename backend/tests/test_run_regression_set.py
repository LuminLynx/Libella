"""Tests for backend/scripts/run_regression_set.py.

Two layers:
  * `parse_regression_set` schema validation — pure, no DB / no SDK.
  * `score_pair` and `render_report` — runner logic with a stub grader.

Live grader-vs-Anthropic verification doesn't run here. That's the
operator's job (P2.5 gate evaluation).
"""
from __future__ import annotations

from typing import Any

import pytest

from scripts.run_regression_set import (
    PairOutcome,
    RegressionPair,
    RegressionSetError,
    parse_regression_set,
    render_report,
    score_pair,
)
from app.ai_service import GraderOutput


# ---------------------------------------------------------------------------
# parse_regression_set
# ---------------------------------------------------------------------------


VALID_YAML = """
unit_id: tokenization-bundle-0
description: short note
pairs:
  - id: p001
    label: All criteria met
    answer: |
      A solid answer that addresses every criterion.
    expected:
      criteria:
        - position: 1
          met: true
        - position: 2
          met: true
        - position: 3
          met: true
      flagged: false
"""


def test_parse_accepts_well_formed_yaml() -> None:
    unit_id, pairs = parse_regression_set(VALID_YAML)
    assert unit_id == "tokenization-bundle-0"
    assert len(pairs) == 1
    assert pairs[0].id == "p001"
    assert len(pairs[0].expected_criteria) == 3
    assert pairs[0].expected_flagged is False


def test_parse_rejects_missing_unit_id() -> None:
    bad = VALID_YAML.replace("unit_id: tokenization-bundle-0", "")
    with pytest.raises(RegressionSetError, match="unit_id"):
        parse_regression_set(bad)


def test_parse_rejects_empty_pairs() -> None:
    bad = "unit_id: u\npairs: []\n"
    with pytest.raises(RegressionSetError, match="non-empty list"):
        parse_regression_set(bad)


def test_parse_rejects_duplicate_pair_id() -> None:
    bad = VALID_YAML + """
  - id: p001
    answer: |
      duplicate id
    expected:
      criteria:
        - position: 1
          met: true
      flagged: false
"""
    with pytest.raises(RegressionSetError, match="more than once"):
        parse_regression_set(bad)


def test_parse_rejects_oversized_answer() -> None:
    long_answer = "x " * 5000
    bad = f"""
unit_id: u
pairs:
  - id: p001
    answer: |
      {long_answer}
    expected:
      criteria:
        - position: 1
          met: true
      flagged: false
"""
    with pytest.raises(RegressionSetError, match="exceeds 8000 characters"):
        parse_regression_set(bad)


def test_parse_rejects_non_boolean_met() -> None:
    bad = VALID_YAML.replace("met: true", "met: yes-please")
    with pytest.raises(RegressionSetError, match="'met' must be a boolean"):
        parse_regression_set(bad)


def test_parse_rejects_duplicate_position() -> None:
    bad = """
unit_id: u
pairs:
  - id: p001
    answer: |
      x
    expected:
      criteria:
        - position: 1
          met: true
        - position: 1
          met: false
      flagged: false
"""
    with pytest.raises(RegressionSetError, match="duplicates position"):
        parse_regression_set(bad)


# ---------------------------------------------------------------------------
# score_pair
# ---------------------------------------------------------------------------


def _unit(criteria_positions: list[int]) -> dict[str, Any]:
    return {
        "id": "u-1",
        "rubric": {
            "criteria": [
                {"id": 100 + p, "position": p, "text": f"crit {p}"}
                for p in criteria_positions
            ]
        },
    }


def _pair(expected: list[tuple[int, bool]], flagged: bool = False) -> RegressionPair:
    from scripts.run_regression_set import ExpectedCriterion

    return RegressionPair(
        id="p001",
        label="test",
        answer="answer",
        expected_criteria=[ExpectedCriterion(position=p, met=m) for p, m in expected],
        expected_flagged=flagged,
    )


def test_score_pair_full_match() -> None:
    unit = _unit([1, 2])

    def grader(_unit: Any, _answer: str) -> GraderOutput:
        return GraderOutput(
            grades=[
                {"criterion_id": 101, "met": True, "confidence": 0.9, "rationale": "x", "answer_quote": "y"},
                {"criterion_id": 102, "met": False, "confidence": 0.9, "rationale": "x", "answer_quote": ""},
            ],
            flagged=False,
        )

    outcome = score_pair(_pair([(1, True), (2, False)]), unit, grader)
    assert outcome.matched_criteria == 2
    assert outcome.total_criteria == 2
    assert outcome.flagged_match is True
    assert outcome.error is None


def test_score_pair_partial_match() -> None:
    unit = _unit([1, 2])

    def grader(_unit: Any, _answer: str) -> GraderOutput:
        return GraderOutput(
            grades=[
                {"criterion_id": 101, "met": True, "confidence": 0.9, "rationale": "x", "answer_quote": "y"},
                {"criterion_id": 102, "met": True, "confidence": 0.9, "rationale": "x", "answer_quote": "y"},
            ],
            flagged=False,
        )

    outcome = score_pair(_pair([(1, True), (2, False)]), unit, grader)
    assert outcome.matched_criteria == 1, "criterion 2 expected false but grader said true"
    assert outcome.total_criteria == 2


def test_score_pair_grader_error_does_not_propagate() -> None:
    unit = _unit([1])

    def grader(_unit: Any, _answer: str) -> GraderOutput:
        raise RuntimeError("rate limit")

    outcome = score_pair(_pair([(1, True)]), unit, grader)
    assert outcome.error is not None
    assert "rate limit" in outcome.error
    assert outcome.matched_criteria == 0


def test_score_pair_propagates_provider_usage() -> None:
    """Token counts the grader reports must reach PairOutcome so the
    Phase 2 cost summary is real, not zeros.
    """
    unit = _unit([1])

    def grader(_unit: Any, _answer: str) -> GraderOutput:
        return GraderOutput(
            grades=[
                {"criterion_id": 101, "met": True, "confidence": 0.9, "rationale": "x", "answer_quote": "y"},
            ],
            flagged=False,
            usage={
                "input_tokens": 1500,
                "output_tokens": 200,
                "cache_read_input_tokens": 1300,
            },
        )

    outcome = score_pair(_pair([(1, True)]), unit, grader)
    assert outcome.input_tokens == 1500
    assert outcome.output_tokens == 200
    assert outcome.cache_read_tokens == 1300


def test_score_pair_flags_stale_yaml_when_position_missing() -> None:
    unit = _unit([1])  # rubric only has position 1
    grader_called = {"count": 0}

    def grader(_unit: Any, _answer: str) -> GraderOutput:
        grader_called["count"] += 1
        return GraderOutput(grades=[], flagged=False)

    outcome = score_pair(_pair([(1, True), (2, True)]), unit, grader)
    assert outcome.error is not None
    assert "position(s)" in outcome.error
    assert grader_called["count"] == 0, (
        "should not call the grader when the YAML references a non-existent criterion"
    )


# ---------------------------------------------------------------------------
# render_report
# ---------------------------------------------------------------------------


def test_render_report_summarizes_runs() -> None:
    outcomes = [
        PairOutcome("p001", "ok", 3, 3, True, False, False, input_tokens=100, output_tokens=50),
        PairOutcome("p002", "fail", 2, 3, True, False, False, input_tokens=100, output_tokens=50),
        PairOutcome("p003", "errored", 0, 3, False, False, False, error="rate limit"),
    ]
    report = render_report(outcomes)
    assert "Pairs scored:               3" in report
    assert "Errored (no score):         1" in report
    assert "Fully passed" in report
    assert "p003" in report and "[ERROR]" in report
    assert "p001" in report and "PASS" in report
    assert "p002" in report and "FAIL" in report
