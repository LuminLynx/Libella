from fastapi import FastAPI, Query
from fastapi.encoders import jsonable_encoder
from fastapi.responses import JSONResponse

from .repository import (
    get_term_by_id,
    list_categories,
    list_terms,
    list_terms_by_category,
    search_terms,
)

app = FastAPI(title="AI-101 Backend MVP", version="0.1.0")


def _envelope_response(*, data, error=None, status_code: int = 200) -> JSONResponse:
    payload = {"data": data, "error": error}
    return JSONResponse(content=jsonable_encoder(payload), status_code=status_code)


@app.get("/health")
def health() -> dict:
    return {"status": "ok"}


@app.get("/api/v1/terms")
def get_terms() -> JSONResponse:
    return _envelope_response(data=list_terms())


@app.get("/api/v1/terms/{term_id}")
def get_term_details(term_id: str) -> JSONResponse:
    term = get_term_by_id(term_id)
    if term is None:
        return _envelope_response(
            status_code=404,
            data=None,
            error={
                "code": "TERM_NOT_FOUND",
                "message": f"No term found for id '{term_id}'.",
            },
        )
    return _envelope_response(data=term)


@app.get("/api/v1/categories")
def get_categories() -> JSONResponse:
    return _envelope_response(data=list_categories())


@app.get("/api/v1/categories/{category_id}/terms")
def get_terms_for_category(category_id: str) -> JSONResponse:
    return _envelope_response(data=list_terms_by_category(category_id))


@app.get("/api/v1/search/terms")
def get_term_search_results(q: str = Query(default="", min_length=0)) -> JSONResponse:
    return _envelope_response(data=search_terms(q))
