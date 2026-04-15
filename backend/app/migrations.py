from pathlib import Path

import psycopg

from .config import BASE_DIR
from .db import get_connection

MIGRATIONS_DIR = BASE_DIR / "db" / "migrations"


def _ensure_migrations_table(connection: psycopg.Connection) -> None:
    connection.execute(
        """
        CREATE TABLE IF NOT EXISTS schema_migrations (
            version TEXT PRIMARY KEY,
            applied_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
        )
        """
    )


def _applied_versions(connection: psycopg.Connection) -> set[str]:
    rows = connection.execute("SELECT version FROM schema_migrations").fetchall()
    return {row["version"] for row in rows}


def discover_migration_files() -> list[Path]:
    if not MIGRATIONS_DIR.exists():
        return []
    return sorted(path for path in MIGRATIONS_DIR.glob("*.sql") if path.is_file())


def run_migrations() -> list[str]:
    applied_now: list[str] = []

    with get_connection() as connection:
        _ensure_migrations_table(connection)
        connection.commit()

        applied = _applied_versions(connection)

        for migration_path in discover_migration_files():
            version = migration_path.name
            if version in applied:
                continue

            sql = migration_path.read_text(encoding="utf-8")
            try:
                connection.execute(sql)
                connection.execute(
                    "INSERT INTO schema_migrations (version) VALUES (%s)",
                    (version,),
                )
                connection.commit()
            except Exception:
                connection.rollback()
                raise

            applied.add(version)
            applied_now.append(version)

    return applied_now
