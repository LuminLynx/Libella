from fastapi import FastAPI, Query
from fastapi.responses import JSONResponse

from .db import seed_database
from .repository import (
    get_term_by_id,
    list_categories,
    list_terms,
    list_terms_by_category,
    search_terms,
)

app = FastAPI(title="AI-101 Backend MVP", version="0.1.0")


@app.on_event("startup")
def startup_event() -> None:
    seed_database()


@app.get("/health")
def health() -> dict:
    return {"status": "ok"}


@app.get("/api/v1/terms")
def get_terms() -> JSONResponse:
    return JSONResponse({"data": list_terms(), "error": None})


@app.get("/api/v1/terms/{term_id}")
def get_term_details(term_id: str) -> JSONResponse:
    term = get_term_by_id(term_id)
    if term is None:
        return JSONResponse(
            status_code=404,
            content={
                "data": None,
                "error": {
                    "code": "TERM_NOT_FOUND",
                    "message": f"No term found for id '{term_id}'.",
                },
            },
        )
    return JSONResponse({"data": term, "error": None})


@app.get("/api/v1/categories")
def get_categories() -> JSONResponse:
    return JSONResponse({"data": list_categories(), "error": None})


@app.get("/api/v1/categories/{category_id}/terms")
def get_terms_for_category(category_id: str) -> JSONResponse:
    return JSONResponse({"data": list_terms_by_category(category_id), "error": None})


@app.get("/api/v1/terms/search")
def get_term_search_results(q: str = Query(default="", min_length=0)) -> JSONResponse:
    return JSONResponse({"data": search_terms(q), "error": None})
