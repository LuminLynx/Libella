# AI-101 Backend MVP

Minimal backend MVP for glossary data using FastAPI + SQLite.

## Endpoints

- `GET /health`
- `GET /api/v1/terms`
- `GET /api/v1/terms/{term_id}`
- `GET /api/v1/categories`
- `GET /api/v1/categories/{category_id}/terms`
- `GET /api/v1/terms/search?q=<query>`

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

## Local run

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r backend/requirements.txt
uvicorn backend.app.main:app --reload
```

## Notes

- Scope is MVP-only (no auth, accounts, admin panel, chat, or analytics).
- Seed data is AI-first and supports browse, categories, search, and details.

## Database initialization and seeding

- On application startup, the backend automatically initializes the schema and seeds data when `categories` or `terms` is empty.
- This startup seeding is idempotent and safe on redeploys.
- To force a full reseed locally, run `python -m backend.scripts.seed_db --reset`.
