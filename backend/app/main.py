from typing import Any

from fastapi import Depends, FastAPI, Query
from fastapi.encoders import jsonable_encoder
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field

from .ai_service import AIServiceError, ai_service_metadata, grade_decision_answer
from .auth import (
    AuthError,
    create_access_token,
    hash_password,
    required_user_id,
    validate_display_name,
    validate_email,
    validate_password,
    verify_password,
)
from .migrations import run_migrations
from .repositories import (
    completion_repository,
    grade_repository,
    path_repository,
    unit_repository,
)
from .repository import (
    create_user,
    get_term_by_id,
    get_user_by_email,
    get_user_by_id,
    list_categories,
    list_terms,
    list_terms_by_category,
    search_terms,
)

app = FastAPI(title="AI-101 Backend", version="0.3.0")


class CompletionRequest(BaseModel):
    unitId: str = Field(min_length=1)


class GradeRequest(BaseModel):
    answer: str = Field(min_length=1, max_length=8000)


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


@app.get("/api/v1/paths/{path_id}")
def get_path(path_id: str) -> JSONResponse:
    path = path_repository.get_path(path_id)
    if path is None:
        return _envelope_response(
            status_code=404,
            data=None,
            error={"code": "PATH_NOT_FOUND", "message": f"No path found for id '{path_id}'."},
        )
    return _envelope_response(data=path)


@app.get("/api/v1/units/{unit_id}")
def get_unit(
    unit_id: str,
    current_user_id: str = Depends(required_user_id),  # noqa: ARG001 — auth gate
) -> JSONResponse:
    unit = unit_repository.get_unit(unit_id)
    if unit is None:
        return _envelope_response(
            status_code=404,
            data=None,
            error={"code": "UNIT_NOT_FOUND", "message": f"No unit found for id '{unit_id}'."},
        )
    return _envelope_response(data=unit)


@app.post("/api/v1/completions")
def post_completion(
    request: CompletionRequest,
    current_user_id: str = Depends(required_user_id),
) -> JSONResponse:
    try:
        result = completion_repository.record_completion(
            user_id=current_user_id,
            unit_id=request.unitId,
        )
    except completion_repository.UnitNotFoundError:
        return _envelope_response(
            status_code=404,
            data=None,
            error={"code": "UNIT_NOT_FOUND", "message": f"No unit found for id '{request.unitId}'."},
        )

    status_code = 200 if result["alreadyCompleted"] else 201
    return _envelope_response(status_code=status_code, data=result)


@app.get("/api/v1/completions")
def list_completions(
    current_user_id: str = Depends(required_user_id),
) -> JSONResponse:
    """Return every completion for the authenticated user, newest first.

    Lets clients seed their local completion cache after sign-in or when
    moving to a new device, so per-user completion state survives across
    installs.
    """
    completions = completion_repository.list_completions(user_id=current_user_id)
    return _envelope_response(data=completions)


@app.post("/api/v1/units/{unit_id}/grade")
def post_grade(
    unit_id: str,
    request: GradeRequest,
    current_user_id: str = Depends(required_user_id),
) -> JSONResponse:
    """F4 — grade the user's open-ended decision-prompt answer.

    Per docs/STRATEGY.md § Loop step 4 + T2: per-criterion Met/Not Met
    with confidence + rationale + answer-quote. The unit's rubric and
    decision prompt must already be authored (chunk 6 ingest).
    """
    unit = unit_repository.get_unit(unit_id)
    if unit is None:
        return _envelope_response(
            status_code=404,
            data=None,
            error={"code": "UNIT_NOT_FOUND", "message": f"No unit found for id '{unit_id}'."},
        )
    rubric = unit.get("rubric") or {}
    if not (rubric.get("criteria") or []):
        return _envelope_response(
            status_code=409,
            data=None,
            error={
                "code": "UNIT_NOT_GRADABLE",
                "message": f"Unit '{unit_id}' has no rubric criteria; nothing to grade.",
            },
        )

    try:
        grader_output = grade_decision_answer(unit, request.answer)
    except AIServiceError as exc:
        return _envelope_response(
            status_code=502,
            data=None,
            error={"code": exc.code, "message": str(exc)},
        )

    # Only commit a completion + grades if the grader call succeeded.
    try:
        completion_result = completion_repository.record_completion(
            user_id=current_user_id,
            unit_id=unit_id,
        )
    except completion_repository.UnitNotFoundError:
        # Race: unit deleted between the lookup above and now.
        return _envelope_response(
            status_code=404,
            data=None,
            error={"code": "UNIT_NOT_FOUND", "message": f"No unit found for id '{unit_id}'."},
        )

    completion = completion_result["completion"]
    grades = grade_repository.upsert_grades(
        completion_id=completion["id"],
        grades=grader_output.grades,
        flagged=grader_output.flagged,
    )

    return _envelope_response(
        data={
            "completion": completion,
            "grades": grades,
            "flagged": grader_output.flagged,
            # answer_quote isn't persisted (no column in migration 019);
            # return it inline so the UI can surface it without a schema
            # change. Tracked as a follow-up.
            "answerQuotes": [
                {"criterionId": g["criterion_id"], "quote": g["answer_quote"]}
                for g in grader_output.grades
            ],
        }
    )
