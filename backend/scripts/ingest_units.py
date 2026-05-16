"""Ingest authored unit markdown into the path-centric Postgres schema.

Loads every `.md` under the given paths into `paths` / `units` /
`unit_sources` / `calibration_tags` / `decision_prompts` / `rubrics` /
`rubric_criteria` (migrations 016–018). Idempotent: re-running with no
changes is a no-op; authoring edits (e.g. a corrected source URL)
propagate cleanly without leaving orphan rows.

Usage:
    python -m backend.scripts.ingest_units content/units/
    python -m backend.scripts.ingest_units content/units/ --check

Exit codes:
    0 — clean ingest (or clean --check)
    1 — linter violations, parse error, or DB error
    2 — usage error

`--check` parses every file, runs the linter, and validates that every
unit's `path_id` matches the canonical Phase 1 path. It does not open a
DB connection — useful as a CI guardrail in the no-DB matrix.

Write semantics per child table:
  - `unit_sources`, `calibration_tags`: replace-on-write (delete + insert).
    Nothing FKs into these, so churn is safe.
  - `decision_prompts`: UPSERT on `unit_id` (UNIQUE), preserving id.
  - `rubrics` + `rubric_criteria`: append-only versions. Authored
    criteria are diffed against the latest stored version; if
    identical, no-op; if different, a new `rubrics` row at
    `version = max+1` is inserted with a fresh criteria set. Old
    versions stay because `grades.criterion_id` FKs into
    `rubric_criteria` (migration 019) — deleting criteria would
    cascade-delete grade history, which we never want during a
    routine re-ingest.
  - `paths`, `units`: UPSERT on `id`, so external FKs
    (`completions.unit_id`, etc.) survive.
"""
from __future__ import annotations

import argparse
import datetime as _datetime
import json
import re
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Iterable

from .lint_unit_markdown import lint_paths

CANONICAL_PATH_ID = "llm-systems-for-pms"
CANONICAL_PATH_SLUG = "llm-systems-for-pms"
CANONICAL_PATH_TITLE = "LLM Systems for PMs"
CANONICAL_PATH_DESCRIPTION = (
    "The canonical curriculum: the LLM concepts a product manager "
    "actually has to reason about — from tokenization and context "
    "windows to evals, RAG, and the production trade-offs beyond — "
    "taught one trade-off at a time."
)

_SECTION_HEADER_RE = re.compile(r"^##\s+(.+?)\s*$")
_TITLE_HEADER_RE = re.compile(r"^#\s+(.+?)\s*$")


@dataclass
class UnitData:
    path: Path
    unit_id: str
    slug: str
    path_id: str
    position: int
    title: str
    definition: str
    trade_off_framing: str
    bite_md: str
    depth_md: str
    prereq_unit_ids: list[str]
    status: str
    sources: list[dict[str, Any]]
    calibration_tags: list[dict[str, str]]
    decision_prompt_md: str
    rubric_criteria: list[str]


def _extract_body_sections(body: str) -> tuple[str | None, dict[str, str]]:
    """Return (title, {section_name: section_body}). Section bodies are stripped."""
    title: str | None = None
    sections: dict[str, list[str]] = {}
    current: str | None = None
    for line in body.splitlines():
        if title is None:
            m = _TITLE_HEADER_RE.match(line)
            if m and not line.startswith("##"):
                title = m.group(1).strip()
                continue
        sm = _SECTION_HEADER_RE.match(line)
        if sm:
            current = sm.group(1).strip()
            sections.setdefault(current, [])
            continue
        if current is not None:
            sections[current].append(line)
    return title, {name: "\n".join(lines).strip() for name, lines in sections.items()}


def _split_front_matter(text: str) -> tuple[str, str]:
    """Return (front_matter_yaml, body). Assumes file already lints clean."""
    lines = text.splitlines()
    if not lines or lines[0].strip() != "---":
        raise ValueError("missing front matter")
    closing = next(
        (i for i in range(1, len(lines)) if lines[i].strip() == "---"),
        None,
    )
    if closing is None:
        raise ValueError("unterminated front matter")
    return "\n".join(lines[1:closing]), "\n".join(lines[closing + 1 :])


def _parse_source_date(value: Any) -> _datetime.date:
    if isinstance(value, _datetime.date):
        return value
    if isinstance(value, str):
        return _datetime.date.fromisoformat(value.strip())
    raise ValueError(f"unsupported source date type: {type(value).__name__}")


def parse_unit(path: Path) -> UnitData:
    """Parse an already-lint-clean unit into the ingest-ready shape."""
    import yaml

    text = path.read_text(encoding="utf-8")
    fm_yaml, body = _split_front_matter(text)
    fm = yaml.safe_load(fm_yaml) or {}

    title, sections = _extract_body_sections(body)
    if title is None or not title.strip():
        raise ValueError(f"{path}: missing H1 title")
    for required in ("Trade-off framing", "90-second bite", "Depth", "Decision prompt"):
        if required not in sections:
            raise ValueError(f"{path}: missing section '## {required}'")

    sources = [
        {
            "url": s["url"].strip(),
            "title": s["title"].strip(),
            "date": _parse_source_date(s["date"]),
            "primary_source": bool(s.get("primary_source", False)),
        }
        for s in fm["sources"]
    ]
    calibration_tags = [
        {"claim": t["claim"].strip(), "tier": t["tier"].strip()}
        for t in fm["calibration_tags"]
    ]
    rubric_criteria = [c["text"].strip() for c in fm["rubric"]]

    return UnitData(
        path=path,
        unit_id=str(fm["id"]).strip(),
        slug=str(fm["slug"]).strip(),
        path_id=str(fm["path_id"]).strip(),
        position=int(fm["position"]),
        title=title.strip(),
        definition=str(fm["definition"]).strip(),
        trade_off_framing=sections["Trade-off framing"],
        bite_md=sections["90-second bite"],
        depth_md=sections["Depth"],
        prereq_unit_ids=[str(p).strip() for p in fm.get("prereq_unit_ids", [])],
        status=str(fm["status"]).strip(),
        sources=sources,
        calibration_tags=calibration_tags,
        decision_prompt_md=sections["Decision prompt"],
        rubric_criteria=rubric_criteria,
    )


def _collect_files(roots: Iterable[Path]) -> list[Path]:
    files: list[Path] = []
    for root in roots:
        if root.is_file():
            if root.name.startswith("_"):
                continue
            files.append(root)
            continue
        if not root.exists():
            raise FileNotFoundError(root)
        for candidate in sorted(root.rglob("*.md")):
            if candidate.name.startswith("_"):
                continue
            files.append(candidate)
    return files


def _validate_path_ids(units: list[UnitData]) -> list[str]:
    errs: list[str] = []
    for u in units:
        if u.path_id != CANONICAL_PATH_ID:
            errs.append(
                f"{u.path}: path_id {u.path_id!r} does not match canonical "
                f"path {CANONICAL_PATH_ID!r}"
            )
    return errs


def _upsert_path(connection: Any) -> None:
    connection.execute(
        """
        INSERT INTO paths (id, slug, title, description)
        VALUES (%s, %s, %s, %s)
        ON CONFLICT (id) DO UPDATE SET
            slug = EXCLUDED.slug,
            title = EXCLUDED.title,
            description = EXCLUDED.description,
            updated_at = NOW()
        """,
        (
            CANONICAL_PATH_ID,
            CANONICAL_PATH_SLUG,
            CANONICAL_PATH_TITLE,
            CANONICAL_PATH_DESCRIPTION,
        ),
    )


def _upsert_unit(connection: Any, unit: UnitData) -> None:
    connection.execute(
        """
        INSERT INTO units (
            id, path_id, slug, position, title, definition,
            trade_off_framing, bite_md, depth_md, prereq_unit_ids, status
        )
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        ON CONFLICT (id) DO UPDATE SET
            path_id = EXCLUDED.path_id,
            slug = EXCLUDED.slug,
            position = EXCLUDED.position,
            title = EXCLUDED.title,
            definition = EXCLUDED.definition,
            trade_off_framing = EXCLUDED.trade_off_framing,
            bite_md = EXCLUDED.bite_md,
            depth_md = EXCLUDED.depth_md,
            prereq_unit_ids = EXCLUDED.prereq_unit_ids,
            status = EXCLUDED.status,
            updated_at = NOW()
        """,
        (
            unit.unit_id,
            unit.path_id,
            unit.slug,
            unit.position,
            unit.title,
            unit.definition,
            unit.trade_off_framing,
            unit.bite_md,
            unit.depth_md,
            unit.prereq_unit_ids,
            unit.status,
        ),
    )


def _sync_decision_prompt_and_rubric(connection: Any, unit: UnitData) -> None:
    """UPSERT the decision prompt; insert a new rubric version only on change.

    Rubric versions and rubric_criteria rows are append-only. `grades.criterion_id`
    FKs into `rubric_criteria` (migration 019), so deleting criteria would
    cascade-delete grades. When the authored criteria match the latest
    stored version verbatim, we no-op; otherwise we insert a new
    `rubrics` row at `version = max+1` with a fresh set of criteria, leaving
    the prior version intact for audit + grade history.
    """
    if not unit.decision_prompt_md.strip():
        return

    prompt_row = connection.execute(
        """
        INSERT INTO decision_prompts (unit_id, prompt_md)
        VALUES (%s, %s)
        ON CONFLICT (unit_id) DO UPDATE SET
            prompt_md = EXCLUDED.prompt_md,
            updated_at = NOW()
        RETURNING id
        """,
        (unit.unit_id, unit.decision_prompt_md),
    ).fetchone()
    prompt_id = prompt_row["id"]

    if not unit.rubric_criteria:
        return

    latest = connection.execute(
        """
        SELECT id, version
        FROM rubrics
        WHERE decision_prompt_id = %s
        ORDER BY version DESC
        LIMIT 1
        """,
        (prompt_id,),
    ).fetchone()

    if latest is not None:
        existing_criteria = connection.execute(
            """
            SELECT position, criterion_text
            FROM rubric_criteria
            WHERE rubric_id = %s
            ORDER BY position ASC
            """,
            (latest["id"],),
        ).fetchall()
        existing = [(r["position"], r["criterion_text"]) for r in existing_criteria]
        new = [(i + 1, t) for i, t in enumerate(unit.rubric_criteria)]
        if existing == new:
            return
        next_version = latest["version"] + 1
    else:
        next_version = 1

    rubric_json = json.dumps(
        {
            "criteria": [
                {"position": i + 1, "text": t}
                for i, t in enumerate(unit.rubric_criteria)
            ]
        }
    )
    rubric_row = connection.execute(
        """
        INSERT INTO rubrics (decision_prompt_id, version, rubric_json)
        VALUES (%s, %s, %s::jsonb)
        RETURNING id
        """,
        (prompt_id, next_version, rubric_json),
    ).fetchone()
    for i, criterion in enumerate(unit.rubric_criteria, start=1):
        connection.execute(
            """
            INSERT INTO rubric_criteria (rubric_id, position, criterion_text)
            VALUES (%s, %s, %s)
            """,
            (rubric_row["id"], i, criterion),
        )


def _replace_unit_children(connection: Any, unit: UnitData) -> None:
    """Replace unit_sources + calibration_tags (no FKs into them); version
    decision_prompt + rubric so grade history survives.
    """
    connection.execute("DELETE FROM unit_sources WHERE unit_id = %s", (unit.unit_id,))
    for s in unit.sources:
        connection.execute(
            """
            INSERT INTO unit_sources (unit_id, url, title, source_date, primary_source)
            VALUES (%s, %s, %s, %s, %s)
            """,
            (unit.unit_id, s["url"], s["title"], s["date"], s["primary_source"]),
        )

    connection.execute(
        "DELETE FROM calibration_tags WHERE unit_id = %s", (unit.unit_id,)
    )
    for t in unit.calibration_tags:
        connection.execute(
            "INSERT INTO calibration_tags (unit_id, claim, tier) VALUES (%s, %s, %s)",
            (unit.unit_id, t["claim"], t["tier"]),
        )

    _sync_decision_prompt_and_rubric(connection, unit)


def ingest(roots: list[Path], *, check_only: bool = False) -> int:
    try:
        files = _collect_files(roots)
    except FileNotFoundError as exc:
        print(f"path does not exist: {exc}", file=sys.stderr)
        return 1
    if not files:
        print("no unit files found", file=sys.stderr)
        return 1

    violations = lint_paths(roots)
    if violations:
        for v in violations:
            print(v.render(), file=sys.stderr)
        return 1

    units = [parse_unit(path) for path in files]

    path_id_errs = _validate_path_ids(units)
    if path_id_errs:
        for err in path_id_errs:
            print(err, file=sys.stderr)
        return 1

    if check_only:
        print(f"--check ok: {len(units)} unit(s) parsed and validated")
        return 0

    try:
        from app.db import get_connection  # type: ignore
    except ModuleNotFoundError:
        from backend.app.db import get_connection  # type: ignore

    try:
        with get_connection() as connection:
            try:
                _upsert_path(connection)
                for unit in sorted(units, key=lambda u: u.position):
                    _upsert_unit(connection, unit)
                    _replace_unit_children(connection, unit)
                connection.commit()
            except Exception:
                connection.rollback()
                raise
    except Exception as exc:
        print(f"ingest failed: {exc}", file=sys.stderr)
        return 1

    print(f"ingested 1 path + {len(units)} unit(s)")
    return 0


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(
        prog="ingest_units",
        description="Ingest authored unit markdown into Postgres.",
    )
    parser.add_argument("paths", nargs="+", help="files or directories under content/units/")
    parser.add_argument(
        "--check",
        action="store_true",
        help="parse + validate without writing to the database",
    )
    args = parser.parse_args(argv[1:])
    return ingest([Path(p) for p in args.paths], check_only=args.check)


if __name__ == "__main__":
    sys.exit(main(sys.argv))
