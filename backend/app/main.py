from __future__ import annotations

from datetime import timedelta
from typing import Any

from fastapi import FastAPI, Query
from fastapi.encoders import jsonable_encoder
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field

from .ai_service import AIServiceError, AIUnavailableError, ai_service, ai_service_metadata
from .migrations import run_migrations
from .repository import (
    build_term_context,
    create_term_draft,
    get_cached_generated_content,
    get_term_by_id,
    get_top_missing_queries,
    list_categories,
    list_terms,
    list_terms_by_category,
    publish_term_draft,
    search_terms,
    update_draft_status,
    upsert_generated_content,
)

app = FastAPI(title="AI-101 Backend", version="0.3.0")


class AskGlossaryRequest(BaseModel):
    question: str = Field(min_length=3, max_length=2000)
    termId: str | None = None


class GenerateArtifactRequest(BaseModel):
    forceRefresh: bool = False


class TermDraftRequest(BaseModel):
    slug: str | None = None
    term: str = Field(min_length=1)
    definition: str = Field(min_length=1)
    explanation: str = Field(min_length=1)
    humor: str = ""
    seeAlso: list[str] = Field(default_factory=list)
    tags: list[str] = Field(default_factory=list)
    controversyLevel: int = Field(default=0, ge=0, le=3)
    sourceType: str = "manual"
    sourceReference: str | None = None
    status: str = "draft"
    categoryId: str | None = None


class DraftStatusRequest(BaseModel):
    status: str


def _envelope_response(*, data, error=None, status_code: int = 200) -> JSONResponse:
    payload = {"data": data, "error": error}
    return JSONResponse(content=jsonable_encoder(payload), status_code=status_code)


@app.on_event("startup")
def on_startup() -> None:
    run_migrations()


@app.get("/health")
def health() -> dict[str, Any]:
    return {
        "status": "ok",
        "ai": {
            "provider": ai_service_metadata()["provider"],
            "model": ai_service_metadata()["model"],
        },
    }


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


@app.get("/api/v1/search/missing-queries")
def get_missing_queries(days: int = Query(default=30, ge=1, le=365), limit: int = Query(default=20, ge=1, le=100)) -> JSONResponse:
    return _envelope_response(data=get_top_missing_queries(days=days, limit=limit))


@app.post("/api/v1/term-drafts")
def post_term_draft(request: TermDraftRequest) -> JSONResponse:
    payload = {
        "slug": request.slug,
        "term": request.term,
        "definition": request.definition,
        "explanation": request.explanation,
        "humor": request.humor,
        "see_also": request.seeAlso,
        "tags": request.tags,
        "controversy_level": request.controversyLevel,
        "source_type": request.sourceType,
        "source_reference": request.sourceReference,
        "status": request.status,
        "category_id": request.categoryId,
    }

    try:
        draft = create_term_draft(payload)
    except ValueError as error:
        return _envelope_response(
            status_code=400,
            data=None,
            error={"code": "INVALID_DRAFT", "message": str(error)},
        )

    return _envelope_response(status_code=201, data=draft)


@app.post("/api/v1/term-drafts/{draft_id}/status")
def post_term_draft_status(draft_id: int, request: DraftStatusRequest) -> JSONResponse:
    try:
        draft = update_draft_status(draft_id, request.status)
    except ValueError as error:
        return _envelope_response(
            status_code=400,
            data=None,
            error={"code": "INVALID_DRAFT_STATUS", "message": str(error)},
        )

    if draft is None:
        return _envelope_response(
            status_code=404,
            data=None,
            error={"code": "DRAFT_NOT_FOUND", "message": f"No draft found for id '{draft_id}'."},
        )

    return _envelope_response(data=draft)


@app.post("/api/v1/term-drafts/{draft_id}/publish")
def post_publish_term_draft(draft_id: int) -> JSONResponse:
    try:
        result = publish_term_draft(draft_id)
    except ValueError as error:
        return _envelope_response(
            status_code=400,
            data=None,
            error={"code": "PUBLISH_VALIDATION_FAILED", "message": str(error)},
        )

    if result is None:
        return _envelope_response(
            status_code=404,
            data=None,
            error={"code": "DRAFT_NOT_FOUND", "message": f"No draft found for id '{draft_id}'."},
        )

    return _envelope_response(data=result)


@app.post("/api/v1/ai/ask-glossary")
def post_ask_glossary(request: AskGlossaryRequest) -> JSONResponse:
    term_context = None
    if request.termId:
        term_context = build_term_context(request.termId)
        if term_context is None:
            return _envelope_response(
                status_code=404,
                data=None,
                error={"code": "TERM_NOT_FOUND", "message": "Term context was not found."},
            )

    try:
        result = ai_service.ask_glossary(question=request.question, term_context=term_context)
    except AIUnavailableError as error:
        return _envelope_response(
            status_code=503,
            data=None,
            error={"code": error.code, "message": str(error)},
        )
    except AIServiceError as error:
        return _envelope_response(
            status_code=502,
            data=None,
            error={"code": error.code, "message": str(error)},
        )

    return _envelope_response(data=result)


@app.post("/api/v1/ai/terms/{term_id}/scenario")
def post_generate_scenario(term_id: str, request: GenerateArtifactRequest) -> JSONResponse:
    return _generate_term_artifact(term_id=term_id, artifact_type="scenario", force_refresh=request.forceRefresh)


@app.post("/api/v1/ai/terms/{term_id}/challenge")
def post_generate_challenge(term_id: str, request: GenerateArtifactRequest) -> JSONResponse:
    return _generate_term_artifact(term_id=term_id, artifact_type="challenge", force_refresh=request.forceRefresh)


def _generate_term_artifact(term_id: str, artifact_type: str, force_refresh: bool) -> JSONResponse:
    term = get_term_by_id(term_id)
    if term is None:
        return _envelope_response(
            status_code=404,
            data=None,
            error={"code": "TERM_NOT_FOUND", "message": "Term context was not found."},
        )

    if not force_refresh:
        cached = get_cached_generated_content(
            term_id=term_id,
            content_type=artifact_type,
            max_age=timedelta(days=14),
        )
        if cached is not None:
            return _envelope_response(data={"artifact": cached, "cached": True})

    try:
        artifact = ai_service.generate_artifact(term=term, artifact_type=artifact_type)  # type: ignore[arg-type]
    except AIUnavailableError as error:
        return _envelope_response(
            status_code=503,
            data=None,
            error={"code": error.code, "message": str(error)},
        )
    except AIServiceError as error:
        return _envelope_response(
            status_code=502,
            data=None,
            error={"code": error.code, "message": str(error)},
        )

    metadata = ai_service_metadata()
    upsert_generated_content(
        term_id=term_id,
        content_type=artifact_type,
        content_json=artifact,
        provider=metadata["provider"] or "unknown",
        model_name=metadata["model"],
    )
    return _envelope_response(data={"artifact": artifact, "cached": False})
