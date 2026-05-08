from pathlib import Path

import psycopg

from .config import BASE_DIR
from .db import get_connection

MIGRATIONS_DIR = BASE_DIR / "db" / "migrations"

# Stable arbitrary 64-bit integer used as the key for `pg_advisory_lock`
# during run_migrations. Two app instances starting concurrently against
# the same database serialize on this key, so only one applies pending
# migrations at a time. The lock is held only for the duration of the
# migration apply; any other connections to the DB are unaffected.
_MIGRATION_LOCK_KEY = 0x66053510153  # "FOSS101 mig" in hex-ish, just needs to be unique


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
    """Apply any pending migrations.

    Race-safe under horizontal scale: takes a transactional Postgres
    advisory lock so two concurrent callers serialize. The recommended
    deploy pattern is still to run `python -m backend.scripts.migrate`
    as a one-shot release command — that lets this on-startup call
    exit immediately on every instance after the first. The advisory
    lock is the belt-and-suspenders that makes a misconfigured deploy
    correct rather than crashed.

    Returns the list of migration versions applied during this call.
    Empty when the schema was already up to date.
    """
    applied_now: list[str] = []

    with get_connection() as connection:
        # Acquire a transaction-scoped advisory lock. If another instance
        # already holds it we block here until it releases (typically a
        # few hundred ms — migrations are fast and idempotent). The lock
        # is automatically released when the transaction commits/rolls
        # back, so we don't need an explicit unlock.
        connection.execute("BEGIN")
        try:
            connection.execute("SELECT pg_advisory_xact_lock(%s)", (_MIGRATION_LOCK_KEY,))
            _ensure_migrations_table(connection)

            applied = _applied_versions(connection)

            for migration_path in discover_migration_files():
                version = migration_path.name
                if version in applied:
                    continue

                sql = migration_path.read_text(encoding="utf-8")
                connection.execute(sql)
                connection.execute(
                    "INSERT INTO schema_migrations (version) VALUES (%s)",
                    (version,),
                )
                applied.add(version)
                applied_now.append(version)

            connection.execute("COMMIT")
        except Exception:
            connection.execute("ROLLBACK")
            raise

    return applied_now
