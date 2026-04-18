from __future__ import annotations

from backend.app.db import get_connection
from backend.app.migrations import run_migrations


def run_audit() -> dict[str, int]:
    run_migrations()

    checks = {
        "missing_slug": "SELECT COUNT(*) AS count FROM terms WHERE slug IS NULL OR slug = ''",
        "invalid_slug_format": "SELECT COUNT(*) AS count FROM terms WHERE slug !~ '^[a-z0-9]+(?:-[a-z0-9]+)*$'",
        "blank_term": "SELECT COUNT(*) AS count FROM terms WHERE LENGTH(TRIM(term)) = 0",
        "blank_definition": "SELECT COUNT(*) AS count FROM terms WHERE LENGTH(TRIM(definition)) = 0",
        "blank_explanation": "SELECT COUNT(*) AS count FROM terms WHERE LENGTH(TRIM(explanation)) = 0",
        "invalid_controversy_level": "SELECT COUNT(*) AS count FROM terms WHERE controversy_level < 0 OR controversy_level > 3",
        "orphan_term_relations": """
            SELECT COUNT(*) AS count
            FROM term_relations relation
            LEFT JOIN terms src ON src.id = relation.term_id
            LEFT JOIN terms target ON target.id = relation.related_term_id
            WHERE src.id IS NULL OR target.id IS NULL OR relation.term_id = relation.related_term_id
        """,
    }

    results: dict[str, int] = {}
    with get_connection() as connection:
        for name, sql in checks.items():
            row = connection.execute(sql).fetchone()
            results[name] = int(row["count"]) if row else -1

    return results


if __name__ == "__main__":
    audit = run_audit()
    print("Term schema audit summary:")
    failing = False
    for key, count in audit.items():
        print(f"- {key}: {count}")
        if count > 0:
            failing = True

    if failing:
        raise SystemExit("Audit failed: canonical term schema violations were found.")

    print("Audit passed: canonical term schema is enforced and clean.")
