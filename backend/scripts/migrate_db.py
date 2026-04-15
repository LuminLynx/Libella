from backend.app.config import masked_database_url
from backend.app.migrations import run_migrations


if __name__ == "__main__":
    applied = run_migrations()
    print(f"Migrations database: {masked_database_url()}")
    if applied:
        print("Applied migrations:")
        for version in applied:
            print(f"- {version}")
    else:
        print("No pending migrations.")
