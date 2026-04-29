from datetime import timedelta
from typing import Any

from fastapi import Depends, FastAPI, Query
from fastapi.encoders import jsonable_encoder
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field

from .ai_service import AIServiceError, AIUnavailableError, ai_service, ai_service_metadata
from .auth import (
    AuthError,
    create_access_token,
    hash_password,
    optional_user_id,
    required_user_id,
    validate_display_name,
    validate_email,
    validate_password,
    verify_password,
)
from .migrations import run_migrations
from .repository import (
    build_term_context,
    create_term_draft,
    create_user,
    get_cached_generated_content,
    get_contributor_summary,
    get_term_by_id,
    get_top_missing_queries,
    get_user_by_email,
    get_user_by_id,
    list_categories,
    list_terms,
    list_terms_by_category,
    publish_term_draft,
    record_learning_completion,
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
    preset: str | None = None


class TaskStatePayload(BaseModel):
    index: int
    checked: bool = False
    note: str | None = None


class CriterionGradePayload(BaseModel):
    index: int
    met: bool = False
    note: str | None = None


class LearningCompletionRequest(BaseModel):
    termId: str = Field(min_length=1)
    artifactType: str
    confidence: str
    reflectionNotes: str | None = None
    taskStates: list[TaskStatePayload] | None = None
    challengeResponse: str | None = None
    criteriaGrades: list[CriterionGradePayload] | None = None


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
    contributorId: str = "anonymous"
    contributorMetadata: dict[str, Any] = Field(default_factory=dict)
    missingSearchEventId: int | None = None


class DraftStatusRequest(BaseModel):
    status: str


class SignupRequest(BaseModel):
    email: str
    password: str
    displayName: str


class LoginRequest(BaseModel):
    email: str
    password: str


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
def get_missing_queries(
    days: int = Query(default=30, ge=1, le=365),
    limit: int = Query(default=20, ge=1, le=100),
) -> JSONResponse:
    return _envelope_response(data=get_top_missing_queries(days=days, limit=limit))


@app.post("/api/v1/term-drafts")
def post_term_draft(
    request: TermDraftRequest,
    current_user_id: str | None = Depends(optional_user_id),
) -> JSONResponse:
    contributor_id = current_user_id if current_user_id else request.contributorId
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
        "contributor_id": contributor_id,
        "contributor_metadata": request.contributorMetadata,
        "missing_search_event_id": request.missingSearchEventId,
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


@app.get("/api/v1/contributors/{contributor_id}/summary")
def get_contributor_contribution_summary(
    contributor_id: str,
    recentLimit: int = Query(default=10, ge=1, le=50),
) -> JSONResponse:
    try:
        summary = get_contributor_summary(contributor_id, recent_limit=recentLimit)
    except ValueError as error:
        return _envelope_response(
            status_code=400,
            data=None,
            error={"code": "INVALID_CONTRIBUTOR", "message": str(error)},
        )

    return _envelope_response(data=summary)


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
def post_generate_scenario(
    term_id: str,
    request: GenerateArtifactRequest,
    current_user_id: str = Depends(required_user_id),
) -> JSONResponse:
    return _generate_term_artifact(
        term_id=term_id,
        artifact_type="scenario",
        force_refresh=request.forceRefresh,
        preset=request.preset,
    )


@app.post("/api/v1/ai/terms/{term_id}/challenge")
def post_generate_challenge(
    term_id: str,
    request: GenerateArtifactRequest,
    current_user_id: str = Depends(required_user_id),
) -> JSONResponse:
    return _generate_term_artifact(
        term_id=term_id,
        artifact_type="challenge",
        force_refresh=request.forceRefresh,
        preset=request.preset,
    )


def _generate_term_artifact(
    term_id: str,
    artifact_type: str,
    force_refresh: bool,
    preset: str | None = None,
) -> JSONResponse:
    term = get_term_by_id(term_id)
    if term is None:
        return _envelope_response(
            status_code=404,
            data=None,
            error={"code": "TERM_NOT_FOUND", "message": "Term context was not found."},
        )

    # Cache only when no preset is specified — different presets produce different artifacts,
    # and the cache schema does not include the preset key.
    use_cache = not force_refresh and preset is None

    if use_cache:
        cached = get_cached_generated_content(
            term_id=term_id,
            content_type=artifact_type,
            max_age=timedelta(days=14),
        )
        if cached is not None:
            return _envelope_response(data={"artifact": cached, "cached": True})

    try:
        artifact = ai_service.generate_artifact(
            term=term,
            artifact_type=artifact_type,  # type: ignore[arg-type]
            preset=preset,
        )
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

    if use_cache:
        metadata = ai_service_metadata()
        upsert_generated_content(
            term_id=term_id,
            content_type=artifact_type,
            content_json=artifact,
            provider=metadata["provider"] or "unknown",
            model_name=metadata["model"],
        )
    return _envelope_response(data={"artifact": artifact, "cached": False})


@app.post("/api/v1/auth/signup")
def post_signup(request: SignupRequest) -> JSONResponse:
    try:
        email = validate_email(request.email)
        password = validate_password(request.password)
        display_name = validate_display_name(request.displayName)
    except AuthError as error:
        return _envelope_response(
            status_code=error.status_code,
            data=None,
            error={"code": error.code, "message": str(error)},
        )

    if get_user_by_email(email) is not None:
        return _envelope_response(
            status_code=409,
            data=None,
            error={"code": "EMAIL_TAKEN", "message": "An account with this email already exists."},
        )

    user = create_user(
        email=email,
        password_hash=hash_password(password),
        display_name=display_name,
    )
    token = create_access_token(user["id"])
    return _envelope_response(
        status_code=201,
        data={"token": token, "user": user},
    )


@app.post("/api/v1/auth/login")
def post_login(request: LoginRequest) -> JSONResponse:
    try:
        email = validate_email(request.email)
    except AuthError as error:
        return _envelope_response(
            status_code=error.status_code,
            data=None,
            error={"code": error.code, "message": str(error)},
        )

    row = get_user_by_email(email)
    if row is None or not verify_password(request.password, row["password_hash"]):
        return _envelope_response(
            status_code=401,
            data=None,
            error={"code": "INVALID_CREDENTIALS", "message": "Invalid email or password."},
        )

    user = {
        "id": row["id"],
        "email": row["email"],
        "displayName": row["display_name"],
        "createdAt": row["created_at"],
    }
    token = create_access_token(user["id"])
    return _envelope_response(data={"token": token, "user": user})


@app.get("/api/v1/auth/me")
def get_me(current_user_id: str = Depends(required_user_id)) -> JSONResponse:
    user = get_user_by_id(current_user_id)
    if user is None:
        return _envelope_response(
            status_code=404,
            data=None,
            error={"code": "USER_NOT_FOUND", "message": "Authenticated user no longer exists."},
        )
    return _envelope_response(data=user)


@app.post("/api/v1/learning-completions")
def post_learning_completion(
    request: LearningCompletionRequest,
    current_user_id: str = Depends(required_user_id),
) -> JSONResponse:
    if get_term_by_id(request.termId) is None:
        return _envelope_response(
            status_code=404,
            data=None,
            error={"code": "TERM_NOT_FOUND", "message": f"No term found for id '{request.termId}'."},
        )

    task_states_payload = (
        [item.model_dump() for item in request.taskStates] if request.taskStates is not None else None
    )
    criteria_grades_payload = (
        [item.model_dump() for item in request.criteriaGrades]
        if request.criteriaGrades is not None
        else None
    )

    try:
        result = record_learning_completion(
            user_id=current_user_id,
            term_id=request.termId,
            artifact_type=request.artifactType,
            confidence=request.confidence,
            reflection_notes=request.reflectionNotes,
            task_states=task_states_payload,
            challenge_response=request.challengeResponse,
            criteria_grades=criteria_grades_payload,
        )
    except ValueError as error:
        return _envelope_response(
            status_code=400,
            data=None,
            error={"code": "INVALID_COMPLETION", "message": str(error)},
        )

    status_code = 200 if result["alreadyCompleted"] else 201
    return _envelope_response(status_code=status_code, data=result)
