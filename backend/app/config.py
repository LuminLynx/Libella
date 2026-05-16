import os
from pathlib import Path
from urllib.parse import urlsplit, urlunsplit

BASE_DIR = Path(__file__).resolve().parent.parent
REPO_ROOT = BASE_DIR.parent
SEED_PATH = BASE_DIR / "db" / "seed.sql"

# Load a local .env before reading any environment variables, so local
# development doesn't depend on hand-exporting secrets per shell. Real
# platform env vars (Railway, CI) take precedence — override=False — so
# production behaviour is unchanged and a missing .env is a no-op.
try:
    from dotenv import load_dotenv

    for _env_file in (REPO_ROOT / ".env", BASE_DIR / ".env"):
        if _env_file.is_file():
            load_dotenv(_env_file, override=False)
except ModuleNotFoundError:
    pass

# `production` triggers the strict-secrets / strict-config gate below.
# Set via Railway / your hosting platform's env vars; defaults to
# `development` for local work and CI.
APP_ENV = os.getenv("APP_ENV", "development")

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

# Default kept for local dev; production is required to override (see
# validate_production_config).
JWT_SECRET = os.getenv("JWT_SECRET", "change-me-in-production")
JWT_ALGORITHM = os.getenv("JWT_ALGORITHM", "HS256")
JWT_EXPIRATION_DAYS = int(os.getenv("JWT_EXPIRATION_DAYS", "30"))


# Default values that must NOT appear in a production deployment.
# POSTGRES_PASSWORD is intentionally absent from this tuple — it's
# only consulted when DATABASE_URL is unset (see the conditional in
# validate_production_config below). The Railway-style deploy pattern
# is to set DATABASE_URL directly with strong credentials inside, in
# which case the POSTGRES_* fallbacks are dead code; gating on them
# unconditionally would refuse perfectly valid deploys.
_PRODUCTION_FORBIDDEN_DEFAULTS = (
    ("JWT_SECRET", lambda: JWT_SECRET, "change-me-in-production"),
)


class ProductionConfigError(RuntimeError):
    """Raised when the process is configured as APP_ENV=production but
    has insecure defaults still in place. Caught at app startup so a
    misconfigured deploy fails fast and visibly instead of silently
    running with weak credentials.
    """


def validate_production_config() -> None:
    """Refuse to start in production with default secrets.

    Called from `main.py`'s startup hook. In any non-production
    environment (development, test, ci, staging — anything other than
    the literal string "production") this is a no-op so local work and
    test runs aren't blocked.
    """
    if APP_ENV != "production":
        return

    problems: list[str] = []
    for name, current, forbidden_default in _PRODUCTION_FORBIDDEN_DEFAULTS:
        if current() == forbidden_default:
            problems.append(
                f"{name} is still the development default ({forbidden_default!r}); "
                f"set it via the deploy environment."
            )

    # POSTGRES_PASSWORD is only consulted when DATABASE_URL is unset
    # (see RESOLVED_DATABASE_URL above). Skip the gate when the
    # operator has provided a full DATABASE_URL — its credentials are
    # the operator's explicit choice and the fallback never runs.
    if not DATABASE_URL and POSTGRES_PASSWORD == "postgres":
        problems.append(
            "POSTGRES_PASSWORD is still the development default ('postgres') "
            "and DATABASE_URL is unset; set DATABASE_URL to a complete "
            "connection string (recommended) or set POSTGRES_PASSWORD."
        )

    # AI_PROVIDER_API_KEY's default is empty; in production we require
    # any non-empty value. Per-provider validity is checked at first
    # call (the grader returns 502 AI_UNAVAILABLE if the key is bad).
    if not AI_PROVIDER_API_KEY:
        problems.append(
            "AI_PROVIDER_API_KEY is empty; set it via the deploy environment."
        )

    if problems:
        joined = "\n  - ".join(problems)
        raise ProductionConfigError(
            f"Refusing to start: APP_ENV=production but config has weak defaults:\n  - {joined}"
        )


def masked_database_url() -> str:
    split = urlsplit(RESOLVED_DATABASE_URL)
    if split.password is None:
        return RESOLVED_DATABASE_URL

    username = split.username or ""
    host = split.hostname or ""
    port = f":{split.port}" if split.port else ""
    safe_netloc = f"{username}:***@{host}{port}"
    return urlunsplit((split.scheme, safe_netloc, split.path, split.query, split.fragment))
