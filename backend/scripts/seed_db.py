import argparse

from backend.app.config import SEED_PATH, masked_database_url
from backend.app.db import get_connection
from backend.app.migrations import run_migrations


def seed(reset: bool) -> None:
    run_migrations()

    with get_connection() as connection:
        if reset:
            connection.execute(
                "TRUNCATE TABLE term_relations, terms, categories RESTART IDENTITY CASCADE"
            )

        seed_sql = SEED_PATH.read_text(encoding="utf-8")
        connection.execute(seed_sql)
        connection.commit()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Seed AI-101 backend PostgreSQL database")
    parser.add_argument("--reset", action="store_true", help="Clear existing rows before seeding")
    args = parser.parse_args()

    seed(args.reset)
    print(f"Database seeded at: {masked_database_url()}")
