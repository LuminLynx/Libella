"""Regression-set runner — Phase 2 gate evidence.

Per docs/STRATEGY.md § T2-C and EXECUTION.md § Phase 2 exit criteria:
every published unit ships with a regression set (>= 20 pairs for
flagship) used to verify the grader stays calibrated. This script
loads a YAML regression set, runs each pair through the live grader,
and reports per-criterion agreement, overall agreement, and a usage
summary so the operator can judge cost.

Usage:
    python -m backend.scripts.run_regression_set <path-to-yaml>

    python -m backend.scripts.run_regression_set <path-to-yaml> --check
        Parse + validate the YAML schema only. No DB lookup, no
        grader calls. Useful as a CI guardrail.

Exit codes:
    0 — runner completed (pairs may still have failed; agreement is
        reported and the operator decides whether to ship).
    1 — schema validation failed, or the live run hit an unrecoverable
        error before any pair was scored.
    2 — usage error.

The runner does not gate on a specific agreement threshold; STRATEGY.md
suggests >= 80% as a recommended bar at the Phase 2 decision gate, but
that's a human call, not a script-enforced one. The script reports the
numbers; the operator decides.
"""
from __future__ import annotations

import argparse
import dataclasses
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Iterable

import yaml


@dataclass
class ExpectedCriterion:
    position: int
    met: bool


@dataclass
class RegressionPair:
    id: str
    label: str
    answer: str
    expected_criteria: list[ExpectedCriterion]
    expected_flagged: bool


@dataclass
class PairOutcome:
    pair_id: str
    label: str
    matched_criteria: int
    total_criteria: int
    flagged_match: bool
    actual_flagged: bool
    expected_flagged: bool
    error: str | None = None
    input_tokens: int = 0
    output_tokens: int = 0
    cache_read_tokens: int = 0


# ---------------------------------------------------------------------------
# YAML schema validation
# ---------------------------------------------------------------------------


class RegressionSetError(Exception):
    """Raised when the YAML doesn't match the required schema."""


def parse_regression_set(yaml_text: str) -> tuple[str, list[RegressionPair]]:
    """Parse and validate the YAML. Returns (unit_id, pairs)."""
    try:
        data = yaml.safe_load(yaml_text)
    except yaml.YAMLError as exc:
        raise RegressionSetError(f"YAML is not valid: {exc}") from exc

    if not isinstance(data, dict):
        raise RegressionSetError("Top-level YAML must be a mapping.")

    unit_id = data.get("unit_id")
    if not isinstance(unit_id, str) or not unit_id.strip():
        raise RegressionSetError("Missing or empty 'unit_id'.")

    raw_pairs = data.get("pairs")
    if not isinstance(raw_pairs, list) or not raw_pairs:
        raise RegressionSetError("'pairs' must be a non-empty list.")

    pairs: list[RegressionPair] = []
    seen_ids: set[str] = set()
    for index, raw in enumerate(raw_pairs, start=1):
        if not isinstance(raw, dict):
            raise RegressionSetError(f"pairs[{index}] must be a mapping.")

        pair_id = raw.get("id")
        label = raw.get("label", "")
        answer = raw.get("answer")
        expected = raw.get("expected")

        if not isinstance(pair_id, str) or not pair_id.strip():
            raise RegressionSetError(f"pairs[{index}] missing non-empty 'id'.")
        if pair_id in seen_ids:
            raise RegressionSetError(f"pair id {pair_id!r} appears more than once.")
        seen_ids.add(pair_id)

        if not isinstance(answer, str) or not answer.strip():
            raise RegressionSetError(f"pair {pair_id} missing non-empty 'answer'.")
        if len(answer) > 8000:
            raise RegressionSetError(
                f"pair {pair_id} 'answer' exceeds 8000 characters "
                f"(matches the grade endpoint's request limit)."
            )

        if not isinstance(expected, dict):
            raise RegressionSetError(f"pair {pair_id} missing 'expected' mapping.")

        flagged = expected.get("flagged")
        if not isinstance(flagged, bool):
            raise RegressionSetError(f"pair {pair_id} 'expected.flagged' must be a boolean.")

        criteria_list = expected.get("criteria")
        if not isinstance(criteria_list, list) or not criteria_list:
            raise RegressionSetError(
                f"pair {pair_id} 'expected.criteria' must be a non-empty list."
            )
        criteria: list[ExpectedCriterion] = []
        seen_positions: set[int] = set()
        for j, raw_c in enumerate(criteria_list, start=1):
            if not isinstance(raw_c, dict):
                raise RegressionSetError(
                    f"pair {pair_id} criteria[{j}] must be a mapping."
                )
            position = raw_c.get("position")
            met = raw_c.get("met")
            if not isinstance(position, int) or position < 1:
                raise RegressionSetError(
                    f"pair {pair_id} criteria[{j}] 'position' must be a positive integer."
                )
            if position in seen_positions:
                raise RegressionSetError(
                    f"pair {pair_id} criteria[{j}] duplicates position {position}."
                )
            seen_positions.add(position)
            if not isinstance(met, bool):
                raise RegressionSetError(
                    f"pair {pair_id} criteria[{j}] 'met' must be a boolean."
                )
            criteria.append(ExpectedCriterion(position=position, met=met))

        pairs.append(
            RegressionPair(
                id=pair_id,
                label=str(label),
                answer=answer,
                expected_criteria=criteria,
                expected_flagged=flagged,
            )
        )

    return unit_id, pairs


# ---------------------------------------------------------------------------
# Live runner
# ---------------------------------------------------------------------------


def _position_to_criterion_id(unit: dict[str, Any]) -> dict[int, int]:
    rubric = unit.get("rubric") or {}
    out: dict[int, int] = {}
    for c in rubric.get("criteria") or []:
        out[int(c["position"])] = int(c["id"])
    return out


def score_pair(
    pair: RegressionPair,
    unit: dict[str, Any],
    grader,  # callable: (unit, answer) -> ai_service.GraderOutput
) -> PairOutcome:
    """Run one pair through the grader and compare to expected.

    `grader` is injected so tests can substitute a stub without
    importing the SDK. In production it's `ai_service.grade_decision_answer`.
    """
    pos_to_id = _position_to_criterion_id(unit)
    missing_positions = [c.position for c in pair.expected_criteria if c.position not in pos_to_id]
    if missing_positions:
        return PairOutcome(
            pair_id=pair.id,
            label=pair.label,
            matched_criteria=0,
            total_criteria=len(pair.expected_criteria),
            flagged_match=False,
            actual_flagged=False,
            expected_flagged=pair.expected_flagged,
            error=(
                f"unit's rubric has no criterion at position(s) {missing_positions} — "
                f"either the rubric was re-authored or the regression-set YAML is stale."
            ),
        )

    try:
        output = grader(unit, pair.answer)
    except Exception as exc:
        return PairOutcome(
            pair_id=pair.id,
            label=pair.label,
            matched_criteria=0,
            total_criteria=len(pair.expected_criteria),
            flagged_match=False,
            actual_flagged=False,
            expected_flagged=pair.expected_flagged,
            error=f"grader error: {exc}",
        )

    actual_by_id: dict[int, bool] = {int(g["criterion_id"]): bool(g["met"]) for g in output.grades}
    matched = 0
    for expected in pair.expected_criteria:
        cid = pos_to_id[expected.position]
        if cid in actual_by_id and actual_by_id[cid] == expected.met:
            matched += 1

    flagged_match = output.flagged == pair.expected_flagged

    usage = getattr(output, "usage", None) or {}
    return PairOutcome(
        pair_id=pair.id,
        label=pair.label,
        matched_criteria=matched,
        total_criteria=len(pair.expected_criteria),
        flagged_match=flagged_match,
        actual_flagged=output.flagged,
        expected_flagged=pair.expected_flagged,
        input_tokens=int(usage.get("input_tokens", 0)),
        output_tokens=int(usage.get("output_tokens", 0)),
        cache_read_tokens=int(usage.get("cache_read_input_tokens", 0)),
    )


def render_report(outcomes: list[PairOutcome]) -> str:
    total_pairs = len(outcomes)
    if total_pairs == 0:
        return "No pairs scored."

    total_criteria = sum(o.total_criteria for o in outcomes)
    matched_criteria = sum(o.matched_criteria for o in outcomes)
    fully_passed = sum(1 for o in outcomes if o.error is None and o.matched_criteria == o.total_criteria and o.flagged_match)
    flagged_matches = sum(1 for o in outcomes if o.flagged_match and o.error is None)
    errored = sum(1 for o in outcomes if o.error is not None)

    input_tokens = sum(o.input_tokens for o in outcomes)
    output_tokens = sum(o.output_tokens for o in outcomes)
    cache_tokens = sum(o.cache_read_tokens for o in outcomes)

    lines = []
    lines.append("Regression-set run")
    lines.append("=" * 60)
    lines.append(f"Pairs scored:               {total_pairs}")
    lines.append(f"Errored (no score):         {errored}")
    lines.append(f"Fully passed (all crit + flagged):  {fully_passed} ({fully_passed * 100 // total_pairs}%)")
    if total_criteria:
        lines.append(
            f"Per-criterion agreement:    {matched_criteria}/{total_criteria} "
            f"({matched_criteria * 100 // total_criteria}%)"
        )
    lines.append(f"Flagged-correct:            {flagged_matches}/{total_pairs}")
    lines.append("")
    lines.append("Token usage (cost-relevant):")
    lines.append(f"  input tokens:     {input_tokens:>8}")
    lines.append(f"  cache reads:      {cache_tokens:>8}")
    lines.append(f"  output tokens:    {output_tokens:>8}")
    lines.append("")
    lines.append("Per-pair detail:")
    for o in outcomes:
        if o.error:
            lines.append(f"  [ERROR] {o.pair_id} ({o.label}) — {o.error}")
            continue
        crit_status = f"{o.matched_criteria}/{o.total_criteria}"
        flag_status = "ok" if o.flagged_match else f"want={o.expected_flagged} got={o.actual_flagged}"
        verdict = "PASS" if o.matched_criteria == o.total_criteria and o.flagged_match else "FAIL"
        lines.append(f"  [{verdict}] {o.pair_id} crit={crit_status} flagged={flag_status} — {o.label}")

    return "\n".join(lines)


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(prog="run_regression_set")
    parser.add_argument("yaml_path", help="path to the regression-set YAML")
    parser.add_argument(
        "--check",
        action="store_true",
        help="parse + validate the YAML schema only; do not call the grader.",
    )
    args = parser.parse_args(argv[1:])

    path = Path(args.yaml_path)
    if not path.exists():
        print(f"file not found: {path}", file=sys.stderr)
        return 2

    try:
        unit_id, pairs = parse_regression_set(path.read_text(encoding="utf-8"))
    except RegressionSetError as exc:
        print(f"schema error: {exc}", file=sys.stderr)
        return 1

    if args.check:
        print(f"--check ok: {len(pairs)} pair(s) for unit '{unit_id}'.")
        return 0

    # Live run — needs the unit from the database and the live grader.
    try:
        from app.repositories import unit_repository
        from app.ai_service import grade_decision_answer
    except ModuleNotFoundError:
        from backend.app.repositories import unit_repository  # type: ignore
        from backend.app.ai_service import grade_decision_answer  # type: ignore

    unit = unit_repository.get_unit(unit_id)
    if unit is None:
        print(f"unit '{unit_id}' not found in the database.", file=sys.stderr)
        return 1

    outcomes = [score_pair(p, unit, grade_decision_answer) for p in pairs]
    print(render_report(outcomes))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
