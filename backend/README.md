# Backend

FastAPI + PostgreSQL backend for the FOSS-101 / Libella project.

> **Status:** transitional. The original term-glossary backend has been
> stripped of cut features (contribution flow, AI Learning Layer cache,
> ask-glossary, scenario/challenge generators) per `docs/AUDIT.md`. The
> path-centric data model and grader (per `docs/STRATEGY.md` +
> `docs/EXECUTION.md`) land in subsequent Phase 1 / Phase 2 PRs.

## Stable API endpoints

Term / category / search reads (kept as the S1 glossary side-door):

- `GET /health`
- `GET /api/v1/terms`
- `GET /api/v1/terms/{term_id}`
- `GET /api/v1/categories`
- `GET /api/v1/categories/{category_id}/terms`
- `GET /api/v1/search/terms?q=<query>`

Auth (F7):

- `POST /api/v1/auth/signup`
- `POST /api/v1/auth/login`
- `GET /api/v1/auth/me`

Learning completion (RESHAPE — replaced by the path-centric Completion
endpoint in a later Phase 1 PR):

- `POST /api/v1/learning-completions`

All API responses use:

```json
{
  "data": {},
  "error": null
}
```

Not found shape:

```json
{
  "data": null,
  "error": {
    "code": "TERM_NOT_FOUND",
    "message": "..."
  }
}
```

## Canonical term contract

Term payloads returned by term endpoints are normalized server-side and include canonical fields:

- `slug`
- `term`
- `definition`
- `explanation`
- `humor`
- `seeAlso`
- `tags`
- `controversyLevel` (constrained to 0-3)

Legacy compatibility aliases are still present for existing Android consumers (`shortDefinition`, `fullExplanation`, `relatedTerms`).

## Runtime configuration

Configuration is environment-driven.

### Primary database variable

- `DATABASE_URL` (recommended)
  - Example: `postgresql://postgres:postgres@localhost:5432/ai101`

### Fallback database variables

If `DATABASE_URL` is not set, the backend builds it from:

- `POSTGRES_HOST` (default: `localhost`)
- `POSTGRES_PORT` (default: `5432`)
- `POSTGRES_DB` (default: `ai101`)
- `POSTGRES_USER` (default: `postgres`)
- `POSTGRES_PASSWORD` (default: `postgres`)

### App runtime variables

- `APP_HOST` (default: `0.0.0.0`)
- `APP_PORT` (default: `8000`)

### AI provider variables

Defaults flipped to Anthropic Claude Sonnet 4.6 in Phase 0 (configuration
only; the empirical Phase 2 provider lock comes after the grader runs
against the regression set).

- `AI_PROVIDER` (default: `anthropic`)
- `AI_PROVIDER_BASE_URL` (default: `https://api.anthropic.com/v1`)
- `AI_PROVIDER_API_KEY` (env-provided; no default)
- `AI_MODEL` (default: `claude-sonnet-4-6`)

### Auth (JWT)

- `JWT_SECRET` (default: `change-me-in-production`)
- `JWT_ALGORITHM` (default: `HS256`)
- `JWT_EXPIRATION_DAYS` (default: `30`)

## Local run

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r backend/requirements.txt
export DATABASE_URL=postgresql://postgres:postgres@localhost:5432/ai101
python -m backend.scripts.migrate_db
python -m backend.scripts.seed_db --reset
uvicorn backend.app.main:app --host 0.0.0.0 --port 8000 --reload
```

## Migration strategy (production-safe)

- The API runtime does **not** create schema on startup. The startup hook
  applies pending migrations idempotently via `schema_migrations`; it does
  not seed data.
- Schema changes are applied through forward SQL migration files in
  `backend/db/migrations/`. Existing applied migrations are immutable
  history — never edit them. New changes ship as new numbered files.
- Apply migrations explicitly:

```bash
python -m backend.scripts.migrate_db
```

## Seed strategy

- Seeding is operator-invoked only; it does not run automatically in production startup.

```bash
python -m backend.scripts.seed_db
python -m backend.scripts.seed_db --reset
```

## PostgreSQL API verification

Run smoke verification against a real PostgreSQL instance:

```bash
python -m backend.scripts.verify_postgres_api
```

This checks browse, categories, details, category-filtered browse, search (`/api/v1/search/terms`), and not-found behavior using the stable `{ data, error }` envelope, plus canonical-term data integrity guards.

## Railway deployment steps

1. Provision PostgreSQL in Railway.
2. Set service env var `DATABASE_URL`.
3. Run migration command on deploy (pre-deploy or release command):

```bash
python -m backend.scripts.migrate_db
```

4. (Optional one-time bootstrap) run seed command:

```bash
python -m backend.scripts.seed_db
```

5. Start web service:

```bash
uvicorn backend.app.main:app --host 0.0.0.0 --port ${PORT:-8000}
```

## Notes

- `term_relations` powers normalized `seeAlso`/`relatedTerms` serialization in term responses.
- Do not commit real credentials.
