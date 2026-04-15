import argparse

from backend.app.config import DB_PATH
from backend.app.db import get_connection, initialize_database, seed_database


def seed(reset: bool) -> None:
    initialize_database()

    if reset:
        with get_connection() as connection:
            connection.execute("DELETE FROM terms")
            connection.execute("DELETE FROM categories")
            connection.commit()

    seed_database(force=reset)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Seed AI-101 backend database")
    parser.add_argument("--reset", action="store_true", help="Clear existing rows before seeding")
    args = parser.parse_args()

    seed(args.reset)
    print(f"Database seeded at: {DB_PATH}")
