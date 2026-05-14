#!/usr/bin/env python3
"""Diagnostic — dump full per-criterion grader output for specific
regression-set pairs.

Throwaway. Used for triaging Unit 9 gate disagreements (p016 ERROR
isolation, plus per-criterion detail for the partial-FAIL pairs).
Not part of the regular runner. **Delete after the gate triage is
complete.**

Usage:
    python -m backend.scripts._inspect_pairs <yaml_path> <pair_id> [<pair_id>...]

Same env vars as run_regression_set.py:
    DATABASE_URL — points at the prod DB (so the unit + rubric come
                   from the same place the live grader endpoint reads).
    AI_PROVIDER_API_KEY — Anthropic key.

Each call to the grader costs ~$0.01 (one call per pair).
"""
from __future__ import annotations

import sys
from pathlib import Path


def main(argv: list[str]) -> int:
    if len(argv) < 3:
        print(f"usage: {argv[0]} <yaml_path> <pair_id> [<pair_id>...]", file=sys.stderr)
        return 2

    try:
        from backend.scripts.run_regression_set import parse_regression_set
    except ModuleNotFoundError:
        from scripts.run_regression_set import parse_regression_set  # type: ignore

    try:
        from app.repositories import unit_repository
        from app.ai_service import grade_decision_answer
    except ModuleNotFoundError:
        from backend.app.repositories import unit_repository  # type: ignore
        from backend.app.ai_service import grade_decision_answer  # type: ignore

    yaml_path = Path(argv[1])
    target_ids = list(dict.fromkeys(argv[2:]))

    unit_id, pairs = parse_regression_set(yaml_path.read_text(encoding="utf-8"))
    unit = unit_repository.get_unit(unit_id)
    if unit is None:
        print(f"unit '{unit_id}' not found in DB", file=sys.stderr)
        return 1

    pos_to_id: dict[int, int] = {}
    id_to_pos: dict[int, int] = {}
    for c in (unit.get("rubric") or {}).get("criteria") or []:
        pos = int(c["position"])
        cid = int(c["id"])
        pos_to_id[pos] = cid
        id_to_pos[cid] = pos

    by_id = {p.id: p for p in pairs}
    missing = [pid for pid in target_ids if pid not in by_id]
    if missing:
        print(f"warning: pair IDs not found in YAML: {missing}", file=sys.stderr)

    for pid in target_ids:
        if pid not in by_id:
            continue
        pair = by_id[pid]
        print("=" * 72)
        print(f"PAIR: {pair.id} — {pair.label}")
        print("-" * 72)
        print("EXPECTED (YAML):")
        for c in pair.expected_criteria:
            print(f"  position {c.position}: met={c.met}")
        print(f"  flagged={pair.expected_flagged}")
        print()
        print("GRADER OUTPUT:")
        try:
            output = grade_decision_answer(unit, pair.answer)
        except Exception as exc:
            print(f"  GRADER ERROR: {type(exc).__name__}: {exc}")
            print()
            continue

        actual_by_pos: dict[int, dict] = {}
        for grade in output.grades:
            cid = int(grade["criterion_id"])
            pos = id_to_pos.get(cid, -1)
            actual_by_pos[pos] = grade

        for c in pair.expected_criteria:
            grade = actual_by_pos.get(c.position)
            if grade is None:
                print(f"  position {c.position}: NO GRADE RETURNED")
                continue
            actual_met = bool(grade["met"])
            agree = "✓" if actual_met == c.met else "✗ DISAGREE"
            confidence = grade.get("confidence")
            print(
                f"  position {c.position}: met={actual_met} "
                f"(expected {c.met}) [{agree}], confidence={confidence}"
            )
            quote = grade.get("answer_quote") or ""
            rationale = grade.get("rationale") or ""
            if quote:
                print(f"    answer_quote: {quote!r}")
            if rationale:
                print(f"    rationale: {rationale}")
        print(f"  flagged={output.flagged} (expected {pair.expected_flagged})")
        usage = getattr(output, "usage", None) or {}
        print(
            f"  usage: input={usage.get('input_tokens', 0)} "
            f"cache_reads={usage.get('cache_read_input_tokens', 0)} "
            f"output={usage.get('output_tokens', 0)}"
        )
        print()

    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
