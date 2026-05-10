"""One-off diagnostic — Unit 4 gate triage.

Throwaway script (underscore prefix). Pulls full per-criterion grader
output for a list of pair IDs from a regression-set YAML, so triage
can decide YAML-vs-grader on borderlines and inspect surprise FAILs.

Usage:
    python -m backend.scripts._inspect_pairs <yaml_path> <pair_id> [<pair_id> ...]

Reads from the live deployed grader (production env vars must be
exported in the shell — DATABASE_URL, AI_PROVIDER_API_KEY).

Deleted post-triage along with its branch.
"""
from __future__ import annotations

import sys
from pathlib import Path

import yaml


def main(argv: list[str]) -> int:
    if len(argv) < 3:
        print("usage: python -m backend.scripts._inspect_pairs <yaml_path> <pair_id> [<pair_id> ...]", file=sys.stderr)
        return 2

    yaml_path = Path(argv[1])
    pair_ids = set(argv[2:])

    if not yaml_path.exists():
        print(f"file not found: {yaml_path}", file=sys.stderr)
        return 2

    data = yaml.safe_load(yaml_path.read_text(encoding="utf-8"))
    unit_id = data["unit_id"]
    pairs_by_id = {p["id"]: p for p in data["pairs"]}

    missing = pair_ids - pairs_by_id.keys()
    if missing:
        print(f"pair id(s) not found in YAML: {sorted(missing)}", file=sys.stderr)
        return 2

    try:
        from app.repositories import unit_repository
        from app.ai_service import grade_decision_answer
    except ModuleNotFoundError:
        from backend.app.repositories import unit_repository  # type: ignore
        from backend.app.ai_service import grade_decision_answer  # type: ignore

    unit = unit_repository.get_unit(unit_id)
    if unit is None:
        print(f"unit '{unit_id}' not found in DB.", file=sys.stderr)
        return 1

    pos_to_id: dict[int, int] = {}
    id_to_pos: dict[int, int] = {}
    for c in (unit.get("rubric") or {}).get("criteria") or []:
        pos_to_id[int(c["position"])] = int(c["id"])
        id_to_pos[int(c["id"])] = int(c["position"])

    for pid in argv[2:]:
        pair = pairs_by_id[pid]
        print("=" * 72)
        print(f"PAIR: {pid} — {pair['label']}")
        print("-" * 72)

        expected_by_pos = {int(c["position"]): bool(c["met"]) for c in pair["expected"]["criteria"]}
        expected_flagged = bool(pair["expected"]["flagged"])

        print("EXPECTED (YAML):")
        for pos in sorted(expected_by_pos):
            print(f"  position {pos}: met={expected_by_pos[pos]}")
        print(f"  flagged={expected_flagged}")
        print()

        try:
            output = grade_decision_answer(unit, pair["answer"])
        except Exception as exc:
            print(f"GRADER ERROR: {type(exc).__name__}: {exc}")
            print()
            continue

        print("GRADER OUTPUT:")
        actual_by_pos: dict[int, dict] = {}
        for g in output.grades:
            cid = int(g["criterion_id"])
            pos = id_to_pos.get(cid)
            if pos is not None:
                actual_by_pos[pos] = g

        for pos in sorted(set(expected_by_pos) | set(actual_by_pos)):
            actual = actual_by_pos.get(pos)
            expected_met = expected_by_pos.get(pos)
            if actual is None:
                print(f"  position {pos}: GRADER DID NOT RETURN")
                continue
            actual_met = bool(actual["met"])
            agree = "✓" if actual_met == expected_met else "✗ DISAGREE"
            print(f"  position {pos}: met={actual_met} (expected {expected_met}) [{agree}], confidence={actual.get('confidence')}")
            quote = actual.get("answer_quote", "")
            if quote:
                print(f"    answer_quote: {quote!r}")
            rationale = actual.get("rationale", "")
            if rationale:
                print(f"    rationale: {rationale}")

        flagged_agree = "✓" if output.flagged == expected_flagged else "✗ DISAGREE"
        print(f"  flagged={output.flagged} (expected {expected_flagged}) [{flagged_agree}]")

        usage = output.usage or {}
        in_t = usage.get("input_tokens", 0)
        out_t = usage.get("output_tokens", 0)
        cache_t = usage.get("cache_read_input_tokens", 0)
        print(f"  usage: input={in_t} cache_reads={cache_t} output={out_t}")
        print()

    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
