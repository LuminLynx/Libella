import argparse

from backend.app.config import SEED_PATH, masked_database_url
from backend.app.db import get_connection
from backend.app.migrations import run_migrations
from backend.scripts.audit_term_schema import run_audit


def seed(reset: bool) -> None:
    run_migrations()

    with get_connection() as connection:
        if reset:
            connection.execute(
                "TRUNCATE TABLE term_search_events, term_drafts, ai_generated_content, term_relations, terms, categories RESTART IDENTITY CASCADE"
            )

        seed_sql = SEED_PATH.read_text(encoding="utf-8")
        connection.execute(seed_sql)
        connection.commit()

    audit_result = run_audit()
    violations = {name: count for name, count in audit_result.items() if count > 0}
    if violations:
        violation_text = ", ".join(f"{key}={value}" for key, value in violations.items())
        raise RuntimeError(f"Seed produced invalid canonical term data: {violation_text}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Seed AI-101 backend PostgreSQL database")
    parser.add_argument("--reset", action="store_true", help="Clear existing rows before seeding")
    args = parser.parse_args()

    seed(args.reset)
    print(f"Database seeded at: {masked_database_url()}")
