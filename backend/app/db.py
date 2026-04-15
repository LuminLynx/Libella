import sqlite3
from contextlib import contextmanager

from .config import DATA_DIR, DB_PATH, SCHEMA_PATH, SEED_PATH


def _connect() -> sqlite3.Connection:
    connection = sqlite3.connect(DB_PATH)
    connection.row_factory = sqlite3.Row
    connection.execute("PRAGMA foreign_keys = ON;")
    return connection


def initialize_database() -> None:
    DATA_DIR.mkdir(parents=True, exist_ok=True)
    with _connect() as connection:
        schema_sql = SCHEMA_PATH.read_text(encoding="utf-8")
        connection.executescript(schema_sql)
        connection.commit()


def _table_row_count(connection: sqlite3.Connection, table_name: str) -> int:
    row = connection.execute(f"SELECT COUNT(1) AS total FROM {table_name}").fetchone()
    return int(row["total"]) if row is not None else 0


def seed_database(force: bool = False) -> bool:
    """Seed categories and terms.

    Returns True when seed SQL was executed, False when skipped.
    """
    initialize_database()

    with _connect() as connection:
        categories_count = _table_row_count(connection, "categories")
        terms_count = _table_row_count(connection, "terms")

        should_seed = force or categories_count == 0 or terms_count == 0
        if not should_seed:
            return False

        seed_sql = SEED_PATH.read_text(encoding="utf-8")
        connection.executescript(seed_sql)
        connection.commit()
        return True


@contextmanager
def get_connection() -> sqlite3.Connection:
    connection = _connect()
    try:
        yield connection
    finally:
        connection.close()
