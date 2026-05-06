import os
from pathlib import Path
from urllib.parse import urlsplit, urlunsplit

BASE_DIR = Path(__file__).resolve().parent.parent
SEED_PATH = BASE_DIR / "db" / "seed.sql"

POSTGRES_HOST = os.getenv("POSTGRES_HOST", "localhost")
POSTGRES_PORT = int(os.getenv("POSTGRES_PORT", "5432"))
POSTGRES_DB = os.getenv("POSTGRES_DB", "ai101")
POSTGRES_USER = os.getenv("POSTGRES_USER", "postgres")
POSTGRES_PASSWORD = os.getenv("POSTGRES_PASSWORD", "postgres")

DATABASE_URL = os.getenv("DATABASE_URL")
if DATABASE_URL:
    RESOLVED_DATABASE_URL = DATABASE_URL
else:
    RESOLVED_DATABASE_URL = (
        f"postgresql://{POSTGRES_USER}:{POSTGRES_PASSWORD}@"
        f"{POSTGRES_HOST}:{POSTGRES_PORT}/{POSTGRES_DB}"
    )

APP_HOST = os.getenv("APP_HOST", "0.0.0.0")
APP_PORT = int(os.getenv("APP_PORT", "8000"))

AI_PROVIDER = os.getenv("AI_PROVIDER", "anthropic")
AI_PROVIDER_BASE_URL = os.getenv("AI_PROVIDER_BASE_URL", "https://api.anthropic.com/v1")
AI_PROVIDER_API_KEY = os.getenv("AI_PROVIDER_API_KEY", "")
AI_MODEL = os.getenv("AI_MODEL", "claude-sonnet-4-6")

JWT_SECRET = os.getenv("JWT_SECRET", "change-me-in-production")
JWT_ALGORITHM = os.getenv("JWT_ALGORITHM", "HS256")
JWT_EXPIRATION_DAYS = int(os.getenv("JWT_EXPIRATION_DAYS", "30"))


def masked_database_url() -> str:
    split = urlsplit(RESOLVED_DATABASE_URL)
    if split.password is None:
        return RESOLVED_DATABASE_URL

    username = split.username or ""
    host = split.hostname or ""
    port = f":{split.port}" if split.port else ""
    safe_netloc = f"{username}:***@{host}{port}"
    return urlunsplit((split.scheme, safe_netloc, split.path, split.query, split.fragment))
