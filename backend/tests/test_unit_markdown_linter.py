"""Tests for backend/scripts/lint_unit_markdown.py.

Each broken fixture exercises one named failure mode from
docs/EXECUTION.md Phase 1:
  - missing slot
  - malformed calibration tier
  - undated source
  - empty rubric
  - prereq pointing at unknown unit
"""

from __future__ import annotations

from pathlib import Path

from scripts.lint_unit_markdown import lint_paths

FIXTURES = Path(__file__).parent / "fixtures" / "units"


def _violations_for(*relative_paths: str) -> list[str]:
    paths = [FIXTURES / rel for rel in relative_paths]
    return [v.render() for v in lint_paths(paths)]


def test_clean_fixture_lints_with_zero_violations() -> None:
    violations = _violations_for("clean")
    assert violations == [], f"expected clean fixture to lint clean, got: {violations}"


def test_missing_slot_is_flagged() -> None:
    rendered = _violations_for("broken/missing-slot.md")
    assert any("missing required section '## Depth'" in line for line in rendered), rendered


def test_malformed_tier_is_flagged() -> None:
    rendered = _violations_for("broken/malformed-tier.md")
    assert any(
        "tier must be one of" in line and "'maybe'" in line for line in rendered
    ), rendered


def test_undated_source_is_flagged() -> None:
    rendered = _violations_for("broken/undated-source.md")
    assert any("missing 'date'" in line for line in rendered), rendered


def test_empty_rubric_is_flagged() -> None:
    rendered = _violations_for("broken/empty-rubric.md")
    assert any(
        "'rubric' must have at least one criterion" in line for line in rendered
    ), rendered


def test_unknown_prereq_is_flagged() -> None:
    rendered = _violations_for("broken/unknown-prereq.md")
    assert any(
        "references unknown unit id 'this-unit-does-not-exist'" in line for line in rendered
    ), rendered


def test_prereq_referencing_sibling_in_same_pass_is_accepted() -> None:
    # The clean fixture has id 'sample-clean'; if a second file declared it as
    # a prereq within the same lint pass, it should resolve cleanly.
    import tempfile

    with tempfile.TemporaryDirectory() as tmp:
        tmp_path = Path(tmp)
        (tmp_path / "a.md").write_text(
            (FIXTURES / "clean" / "sample-clean.md").read_text(encoding="utf-8"),
            encoding="utf-8",
        )
        (tmp_path / "b.md").write_text(
            """---
id: depends-on-clean
slug: depends-on-clean
path_id: llm-systems-for-pms
position: 2
prereq_unit_ids:
  - sample-clean
status: draft
definition: A unit that depends on the clean fixture in the same pass.
calibration_tags:
  - claim: "A claim."
    tier: settled
sources:
  - url: "https://example.com/source"
    title: "Source Title"
    date: 2024-01-15
rubric:
  - text: "A rubric criterion."
---

# Depends On Clean

## Trade-off framing

Trade-off.

## 90-second bite

Bite.

## Depth

Depth.

## Decision prompt

Prompt.
""",
            encoding="utf-8",
        )
        violations = [v.render() for v in lint_paths([tmp_path])]
        assert violations == [], violations


def test_template_files_are_skipped() -> None:
    # Files starting with '_' are authoring scaffolding (e.g. _TEMPLATE.md).
    # The template intentionally has placeholder values that would fail real
    # validation; the linter must skip it when scanning a directory.
    import tempfile

    with tempfile.TemporaryDirectory() as tmp:
        tmp_path = Path(tmp)
        (tmp_path / "_TEMPLATE.md").write_text(
            "this file is malformed on purpose\n", encoding="utf-8"
        )
        assert lint_paths([tmp_path]) == []
