# AI-101 Backend MVP

Backend MVP for glossary data using **FastAPI + PostgreSQL**.

## Stable API Endpoints

- `GET /health`
- `GET /api/v1/terms`
- `GET /api/v1/terms/{term_id}`
- `GET /api/v1/categories`
- `GET /api/v1/categories/{category_id}/terms`
- `GET /api/v1/search/terms?q=<query>`
- `GET /api/v1/contributors/{contributor_id}/summary`

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

- The API runtime does **not** create schema or seed data on startup.
- Schema changes are applied explicitly through SQL migration files in `backend/db/migrations`.
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

Run schema audit checks against persisted term data:

```bash
python -m backend.scripts.audit_term_schema
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

- Scope is MVP-only (no auth, accounts, admin panel, chat, or analytics).
- `term_relations` powers normalized `seeAlso`/`relatedTerms` serialization in term responses.
- Term draft submissions support `contributorId` (defaults to `anonymous`) so contribution events and scores can be attributed even before full account auth exists.
- Do not commit real credentials.
