"""Schema linter for authored unit markdown.

Validates every authored unit against the 9-slot anatomy from
STRATEGY.md § Unit anatomy. Run as:

    python -m backend.scripts.lint_unit_markdown content/units/

Exit codes:
    0 — every unit lints clean
    1 — at least one violation; each printed as "<path>:<line>: <reason>"
    2 — usage error (no path provided, path doesn't exist)

Files whose name begins with an underscore (_TEMPLATE.md, _DRAFT.md, ...)
are skipped — they're authoring scaffolding, not real units.

The linter is intentionally strict and self-contained. It does not import
the rest of the backend; it can run in CI before any database is up.
"""

from __future__ import annotations

import datetime as _datetime
import re
import sys
from dataclasses import dataclass, field
from pathlib import Path
from typing import Iterable

import yaml

_ISO_DATE_RE = re.compile(r"^\d{4}-\d{2}-\d{2}$")

VALID_TIERS = ("settled", "contested", "unsettled")
VALID_STATUSES = ("draft", "published", "archived")

REQUIRED_FRONT_MATTER_FIELDS = (
    "id",
    "slug",
    "path_id",
    "position",
    "prereq_unit_ids",
    "status",
    "definition",
    "calibration_tags",
    "sources",
    "rubric",
)

REQUIRED_BODY_SECTIONS = (
    "Trade-off framing",
    "90-second bite",
    "Depth",
    "Decision prompt",
)


@dataclass
class Violation:
    path: Path
    line: int
    reason: str

    def render(self) -> str:
        return f"{self.path}:{self.line}: {self.reason}"


@dataclass
class ParsedUnit:
    path: Path
    front_matter: dict | None
    front_matter_end_line: int  # 1-indexed line where '---' closes
    title: str | None
    title_line: int
    sections: dict[str, int]  # section name -> 1-indexed line of '## name'
    unit_id: str | None = None
    violations: list[Violation] = field(default_factory=list)


def _split_front_matter(path: Path, text: str) -> tuple[dict | None, int, str, list[Violation]]:
    """Return (front_matter, end_line, body_text, violations).

    end_line is the 1-indexed line of the closing '---'. If front matter is
    missing or malformed, front_matter is None and end_line is 0.
    """
    violations: list[Violation] = []
    lines = text.splitlines()
    if not lines or lines[0].strip() != "---":
        violations.append(
            Violation(path, 1, "missing YAML front matter (file must start with '---')")
        )
        return None, 0, text, violations

    closing = None
    for i in range(1, len(lines)):
        if lines[i].strip() == "---":
            closing = i
            break
    if closing is None:
        violations.append(
            Violation(path, 1, "unterminated YAML front matter (no closing '---')")
        )
        return None, 0, text, violations

    fm_text = "\n".join(lines[1:closing])
    try:
        data = yaml.safe_load(fm_text) or {}
    except yaml.YAMLError as exc:
        violations.append(Violation(path, 1, f"YAML front matter is not valid YAML: {exc}"))
        return None, closing + 1, "\n".join(lines[closing + 1 :]), violations

    if not isinstance(data, dict):
        violations.append(
            Violation(path, 1, "YAML front matter must be a mapping (key: value pairs)")
        )
        return None, closing + 1, "\n".join(lines[closing + 1 :]), violations

    return data, closing + 1, "\n".join(lines[closing + 1 :]), violations


def _scan_body(path: Path, body_text: str, body_offset: int) -> tuple[str | None, int, dict[str, int]]:
    """Return (title, title_line, sections).

    body_offset is the line number (1-indexed) the body's first line maps to
    in the original file, so reported line numbers point at the real file.
    """
    title: str | None = None
    title_line = 0
    sections: dict[str, int] = {}
    for idx, raw in enumerate(body_text.splitlines()):
        absolute_line = body_offset + idx
        stripped = raw.strip()
        if title is None and stripped.startswith("# ") and not stripped.startswith("## "):
            title = stripped[2:].strip()
            title_line = absolute_line
            continue
        if stripped.startswith("## "):
            name = stripped[3:].strip()
            if name and name not in sections:
                sections[name] = absolute_line
    return title, title_line, sections


def parse_unit(path: Path) -> ParsedUnit:
    text = path.read_text(encoding="utf-8")
    front_matter, fm_end_line, body, fm_violations = _split_front_matter(path, text)
    body_offset = fm_end_line + 1 if fm_end_line else 1
    title, title_line, sections = _scan_body(path, body, body_offset)
    parsed = ParsedUnit(
        path=path,
        front_matter=front_matter,
        front_matter_end_line=fm_end_line,
        title=title,
        title_line=title_line,
        sections=sections,
        unit_id=front_matter.get("id") if isinstance(front_matter, dict) else None,
    )
    parsed.violations.extend(fm_violations)
    return parsed


def _validate_front_matter(parsed: ParsedUnit) -> None:
    fm = parsed.front_matter
    if fm is None:
        return
    path = parsed.path

    for key in REQUIRED_FRONT_MATTER_FIELDS:
        if key not in fm:
            parsed.violations.append(
                Violation(path, 1, f"front matter missing required field '{key}'")
            )

    status = fm.get("status")
    if status is not None and status not in VALID_STATUSES:
        parsed.violations.append(
            Violation(
                path,
                1,
                f"front matter 'status' must be one of {VALID_STATUSES}, got {status!r}",
            )
        )

    position = fm.get("position")
    if "position" in fm and not isinstance(position, int):
        parsed.violations.append(
            Violation(path, 1, "front matter 'position' must be an integer")
        )

    definition = fm.get("definition")
    if "definition" in fm:
        if not isinstance(definition, str) or not definition.strip():
            parsed.violations.append(
                Violation(path, 1, "front matter 'definition' must be a non-empty string (slot 2)")
            )
        else:
            # Strip trailing whitespace and trailing closing quotes so that
            # legitimate definitions ending in `..."writing tips."` are
            # treated as ending in a period.
            stripped = definition.strip().rstrip("\"'")
            if not stripped.endswith("."):
                parsed.violations.append(
                    Violation(
                        path,
                        1,
                        "front matter 'definition' must end with a period (slot 2 — one sentence)",
                    )
                )
            # Strip the trailing period before scanning for internal sentence
            # breaks, so the legitimate end-of-string terminator doesn't match.
            # Heuristic: a sentence break is terminator (.?!) + optional
            # closing quote + whitespace + ASCII letter (any case). Allowing
            # lowercase catches a second sentence that starts lowercase or
            # with a token like "e.g.," — a bypass of the case-sensitive
            # form. Embedded abbreviations like "e.g.," that stay inside
            # one sentence don't trigger because the next character after
            # the abbreviating period is a comma, not whitespace.
            body = stripped[:-1] if stripped.endswith(".") else stripped
            if re.search(r"[.?!]['\"]?\s+[A-Za-z]", body):
                parsed.violations.append(
                    Violation(
                        path,
                        1,
                        "front matter 'definition' must be a single sentence (slot 2)",
                    )
                )

    prereqs = fm.get("prereq_unit_ids")
    if "prereq_unit_ids" in fm:
        if not isinstance(prereqs, list):
            parsed.violations.append(
                Violation(path, 1, "front matter 'prereq_unit_ids' must be a list (slot 9)")
            )
        else:
            for entry in prereqs:
                if not isinstance(entry, str) or not entry.strip():
                    parsed.violations.append(
                        Violation(
                            path,
                            1,
                            "front matter 'prereq_unit_ids' entries must be non-empty strings",
                        )
                    )


def _validate_calibration_tags(parsed: ParsedUnit) -> None:
    fm = parsed.front_matter
    if fm is None or "calibration_tags" not in fm:
        return
    path = parsed.path
    tags = fm["calibration_tags"]
    if not isinstance(tags, list) or not tags:
        parsed.violations.append(
            Violation(path, 1, "'calibration_tags' must be a non-empty list (slot 6)")
        )
        return
    for index, tag in enumerate(tags, start=1):
        if not isinstance(tag, dict):
            parsed.violations.append(
                Violation(path, 1, f"calibration_tags[{index}] must be a mapping with claim+tier")
            )
            continue
        claim = tag.get("claim")
        tier = tag.get("tier")
        if not isinstance(claim, str) or not claim.strip():
            parsed.violations.append(
                Violation(path, 1, f"calibration_tags[{index}] missing non-empty 'claim'")
            )
        if tier not in VALID_TIERS:
            parsed.violations.append(
                Violation(
                    path,
                    1,
                    f"calibration_tags[{index}] tier must be one of {VALID_TIERS}, got {tier!r}",
                )
            )


def _validate_sources(parsed: ParsedUnit) -> None:
    fm = parsed.front_matter
    if fm is None or "sources" not in fm:
        return
    path = parsed.path
    sources = fm["sources"]
    if not isinstance(sources, list) or not sources:
        parsed.violations.append(
            Violation(path, 1, "'sources' must be a non-empty list (slot 7)")
        )
        return
    for index, source in enumerate(sources, start=1):
        if not isinstance(source, dict):
            parsed.violations.append(
                Violation(path, 1, f"sources[{index}] must be a mapping with url+title+date")
            )
            continue
        url = source.get("url")
        title = source.get("title")
        date = source.get("date")
        if not isinstance(url, str) or not url.strip():
            parsed.violations.append(
                Violation(path, 1, f"sources[{index}] missing non-empty 'url'")
            )
        if not isinstance(title, str) or not title.strip():
            parsed.violations.append(
                Violation(path, 1, f"sources[{index}] missing non-empty 'title'")
            )
        if date is None or (isinstance(date, str) and not date.strip()):
            parsed.violations.append(
                Violation(path, 1, f"sources[{index}] missing 'date' (YYYY-MM-DD)")
            )
        elif isinstance(date, _datetime.date):
            pass
        elif isinstance(date, str):
            if not _ISO_DATE_RE.match(date.strip()):
                parsed.violations.append(
                    Violation(
                        path,
                        1,
                        f"sources[{index}] 'date' must be YYYY-MM-DD, got {date!r}",
                    )
                )
            else:
                try:
                    _datetime.date.fromisoformat(date.strip())
                except ValueError:
                    parsed.violations.append(
                        Violation(
                            path,
                            1,
                            f"sources[{index}] 'date' must be a valid YYYY-MM-DD date, got {date!r}",
                        )
                    )
        else:
            parsed.violations.append(
                Violation(
                    path,
                    1,
                    f"sources[{index}] 'date' must be YYYY-MM-DD, got {type(date).__name__}",
                )
            )


def _validate_rubric(parsed: ParsedUnit) -> None:
    fm = parsed.front_matter
    if fm is None or "rubric" not in fm:
        return
    path = parsed.path
    rubric = fm["rubric"]
    if not isinstance(rubric, list) or not rubric:
        parsed.violations.append(
            Violation(path, 1, "'rubric' must have at least one criterion (slot 8)")
        )
        return
    for index, criterion in enumerate(rubric, start=1):
        if not isinstance(criterion, dict):
            parsed.violations.append(
                Violation(path, 1, f"rubric[{index}] must be a mapping with non-empty 'text'")
            )
            continue
        text = criterion.get("text")
        if not isinstance(text, str) or not text.strip():
            parsed.violations.append(
                Violation(path, 1, f"rubric[{index}] missing non-empty 'text'")
            )


def _validate_body(parsed: ParsedUnit) -> None:
    if parsed.title is None or not parsed.title.strip():
        parsed.violations.append(
            Violation(parsed.path, 1, "missing slot 1: H1 title (e.g. '# Tokenization')")
        )
    for section in REQUIRED_BODY_SECTIONS:
        if section not in parsed.sections:
            parsed.violations.append(
                Violation(
                    parsed.path,
                    parsed.front_matter_end_line + 1 if parsed.front_matter_end_line else 1,
                    f"missing required section '## {section}'",
                )
            )


def _validate_prereq_references(parsed_units: list[ParsedUnit]) -> None:
    known_ids: set[str] = set()
    for parsed in parsed_units:
        fm = parsed.front_matter
        if isinstance(fm, dict) and isinstance(fm.get("id"), str):
            known_ids.add(fm["id"])

    for parsed in parsed_units:
        fm = parsed.front_matter
        if not isinstance(fm, dict):
            continue
        prereqs = fm.get("prereq_unit_ids")
        if not isinstance(prereqs, list):
            continue
        for entry in prereqs:
            if not isinstance(entry, str) or not entry.strip():
                continue
            if entry not in known_ids:
                parsed.violations.append(
                    Violation(
                        parsed.path,
                        1,
                        f"prereq_unit_ids references unknown unit id '{entry}'",
                    )
                )


def lint_paths(paths: Iterable[Path]) -> list[Violation]:
    """Lint every .md file under each given path. Returns all violations."""
    files: list[Path] = []
    for root in paths:
        if root.is_file():
            if root.name.startswith("_"):
                continue
            files.append(root)
            continue
        if not root.exists():
            return [Violation(root, 0, "path does not exist")]
        for candidate in sorted(root.rglob("*.md")):
            if candidate.name.startswith("_"):
                continue
            files.append(candidate)

    parsed_units: list[ParsedUnit] = []
    for path in files:
        parsed = parse_unit(path)
        _validate_front_matter(parsed)
        _validate_calibration_tags(parsed)
        _validate_sources(parsed)
        _validate_rubric(parsed)
        _validate_body(parsed)
        parsed_units.append(parsed)

    _validate_prereq_references(parsed_units)

    violations: list[Violation] = []
    for parsed in parsed_units:
        violations.extend(parsed.violations)
    return violations


def main(argv: list[str]) -> int:
    if len(argv) < 2:
        print("usage: python -m backend.scripts.lint_unit_markdown <path> [<path> ...]", file=sys.stderr)
        return 2

    paths = [Path(arg) for arg in argv[1:]]
    violations = lint_paths(paths)
    for v in violations:
        print(v.render())
    return 0 if not violations else 1


if __name__ == "__main__":
    sys.exit(main(sys.argv))
