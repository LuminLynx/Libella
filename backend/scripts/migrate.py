"""Apply pending DB migrations as a one-shot, outside the web request path.

Recommended deploy pattern (Railway / Heroku / etc.):

    # release / pre-deploy command
    python -m backend.scripts.migrate

The web process's on_startup hook also calls run_migrations() — it's
race-safe via a Postgres advisory lock — but running this script as a
release command means web instances boot with a no-op verification,
and any migration failure is observed BEFORE serving traffic.

Idempotent: skips migrations already recorded in `schema_migrations`.

Exit codes:
    0 — migrations applied (or nothing to apply).
    1 — migration failure (DB error, syntax error in a migration file).
"""
from __future__ import annotations

import sys


def main(argv: list[str]) -> int:
    try:
        from app.migrations import run_migrations
    except ModuleNotFoundError:
        from backend.app.migrations import run_migrations  # type: ignore

    try:
        applied = run_migrations()
    except Exception as exc:
        print(f"migration failed: {exc}", file=sys.stderr)
        return 1

    if applied:
        print(f"applied {len(applied)} migration(s):")
        for version in applied:
            print(f"  - {version}")
    else:
        print("schema up to date; no migrations applied.")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
