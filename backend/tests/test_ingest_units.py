"""Tests for backend/scripts/ingest_units.py.

The DB-write tests are Postgres-gated via the `gated_db` fixture in
conftest.py — they skip cleanly when TEST_DATABASE_URL is unset. The
`--check` test is non-gated; it runs in any environment.
"""
from __future__ import annotations

import textwrap
from pathlib import Path
from typing import Callable, Any

import pytest


_LINT_CLEAN_UNIT = textwrap.dedent(
    """\
    ---
    id: ingest-test-unit-0
    slug: ingest-test
    path_id: llm-systems-for-pms
    position: 1
    prereq_unit_ids: []
    status: draft
    definition: A test unit used by the ingest tests.
    calibration_tags:
      - claim: "First settled claim."
        tier: settled
      - claim: "An open question."
        tier: contested
    sources:
      - url: "https://example.com/a"
        title: "Source A"
        date: 2024-01-15
        primary_source: true
      - url: "https://example.com/b"
        title: "Source B"
        date: 2024-02-20
        primary_source: false
    rubric:
      - text: "Names the trade-off."
      - text: "Cites a concrete scenario."
    ---

    # Ingest Test Unit

    ## Trade-off framing

    - **When this matters:** matters here.
    - **When this breaks:** breaks here.
    - **What it costs:** costs here.

    ## 90-second bite

    Bite content for the ingest test unit.

    ## Depth

    Depth content for the ingest test unit.

    ## Decision prompt

    What would you measure first, and why?
    """
)


def _write_unit(dir: Path, body: str) -> Path:
    dir.mkdir(parents=True, exist_ok=True)
    path = dir / "ingest-test-unit-0.md"
    path.write_text(body, encoding="utf-8")
    return path


def _row_counts(get_connection: Callable[[], Any], unit_id: str) -> dict[str, int]:
    counts: dict[str, int] = {}
    with get_connection() as conn:
        for table, where in (
            ("paths", ""),
            ("units", f"WHERE id = '{unit_id}'"),
            ("unit_sources", f"WHERE unit_id = '{unit_id}'"),
            ("calibration_tags", f"WHERE unit_id = '{unit_id}'"),
            ("decision_prompts", f"WHERE unit_id = '{unit_id}'"),
        ):
            row = conn.execute(f"SELECT COUNT(*) AS n FROM {table} {where}").fetchone()
            counts[table] = row["n"]
        rubric_row = conn.execute(
            """
            SELECT COUNT(*) AS n FROM rubrics r
            JOIN decision_prompts dp ON dp.id = r.decision_prompt_id
            WHERE dp.unit_id = %s
            """,
            (unit_id,),
        ).fetchone()
        counts["rubrics"] = rubric_row["n"]
        crit_row = conn.execute(
            """
            SELECT COUNT(*) AS n FROM rubric_criteria rc
            JOIN rubrics r ON r.id = rc.rubric_id
            JOIN decision_prompts dp ON dp.id = r.decision_prompt_id
            WHERE dp.unit_id = %s
            """,
            (unit_id,),
        ).fetchone()
        counts["rubric_criteria"] = crit_row["n"]
    return counts


def test_check_mode_does_not_touch_db(tmp_path: Path) -> None:
    """--check parses + lints + validates, does not require a DB."""
    from scripts.ingest_units import ingest

    _write_unit(tmp_path / "units", _LINT_CLEAN_UNIT)
    rc = ingest([tmp_path / "units"], check_only=True)
    assert rc == 0


def test_check_rejects_lint_violations(tmp_path: Path) -> None:
    from scripts.ingest_units import ingest

    bad = _LINT_CLEAN_UNIT.replace("status: draft", "status: bogus")
    _write_unit(tmp_path / "units", bad)
    rc = ingest([tmp_path / "units"], check_only=True)
    assert rc == 1


def test_check_rejects_wrong_path_id(tmp_path: Path) -> None:
    from scripts.ingest_units import ingest

    bad = _LINT_CLEAN_UNIT.replace(
        "path_id: llm-systems-for-pms", "path_id: some-other-path"
    )
    _write_unit(tmp_path / "units", bad)
    rc = ingest([tmp_path / "units"], check_only=True)
    assert rc == 1


def test_clean_ingest_writes_full_subtree(
    gated_db: Callable[[], Any], tmp_path: Path
) -> None:
    from scripts.ingest_units import ingest

    _write_unit(tmp_path / "units", _LINT_CLEAN_UNIT)
    rc = ingest([tmp_path / "units"], check_only=False)
    assert rc == 0

    counts = _row_counts(gated_db, "ingest-test-unit-0")
    assert counts == {
        "paths": 1,
        "units": 1,
        "unit_sources": 2,
        "calibration_tags": 2,
        "decision_prompts": 1,
        "rubrics": 1,
        "rubric_criteria": 2,
    }

    with gated_db() as conn:
        path = conn.execute(
            "SELECT id, slug, title FROM paths WHERE id = 'llm-systems-for-pms'"
        ).fetchone()
        assert path["slug"] == "llm-systems-for-pms"
        assert path["title"] == "LLM Systems for PMs"

        unit = conn.execute(
            "SELECT * FROM units WHERE id = 'ingest-test-unit-0'"
        ).fetchone()
        assert unit["title"] == "Ingest Test Unit"
        assert unit["status"] == "draft"
        assert "Bite content" in unit["bite_md"]
        assert "Depth content" in unit["depth_md"]
        assert "When this matters" in unit["trade_off_framing"]


def test_ingest_is_idempotent(
    gated_db: Callable[[], Any], tmp_path: Path
) -> None:
    from scripts.ingest_units import ingest

    _write_unit(tmp_path / "units", _LINT_CLEAN_UNIT)
    assert ingest([tmp_path / "units"], check_only=False) == 0
    first = _row_counts(gated_db, "ingest-test-unit-0")

    assert ingest([tmp_path / "units"], check_only=False) == 0
    second = _row_counts(gated_db, "ingest-test-unit-0")

    assert first == second


def test_source_edit_replaces_cleanly(
    gated_db: Callable[[], Any], tmp_path: Path
) -> None:
    from scripts.ingest_units import ingest

    _write_unit(tmp_path / "units", _LINT_CLEAN_UNIT)
    assert ingest([tmp_path / "units"], check_only=False) == 0

    edited = _LINT_CLEAN_UNIT.replace(
        "https://example.com/a", "https://example.com/a-revised"
    )
    _write_unit(tmp_path / "units", edited)
    assert ingest([tmp_path / "units"], check_only=False) == 0

    with gated_db() as conn:
        rows = conn.execute(
            "SELECT url FROM unit_sources WHERE unit_id = 'ingest-test-unit-0' ORDER BY url"
        ).fetchall()

    urls = [r["url"] for r in rows]
    assert "https://example.com/a-revised" in urls
    assert "https://example.com/a" not in urls
    assert len(urls) == 2
