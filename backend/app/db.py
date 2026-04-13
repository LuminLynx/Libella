import sqlite3
from contextlib import contextmanager

from .config import DATA_DIR, DB_PATH, SCHEMA_PATH


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


@contextmanager
def get_connection() -> sqlite3.Connection:
    connection = _connect()
    try:
        yield connection
    finally:
        connection.close()
