import argparse

from backend.app.config import DB_PATH, SEED_PATH
from backend.app.db import get_connection, initialize_database


def seed(reset: bool) -> None:
    initialize_database()

    with get_connection() as connection:
        if reset:
            connection.execute("DELETE FROM terms")
            connection.execute("DELETE FROM categories")

        seed_sql = SEED_PATH.read_text(encoding="utf-8")
        connection.executescript(seed_sql)
        connection.commit()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Seed AI-101 backend database")
    parser.add_argument("--reset", action="store_true", help="Clear existing rows before seeding")
    args = parser.parse_args()

    seed(args.reset)
    print(f"Database seeded at: {DB_PATH}")
