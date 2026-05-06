from typing import Any

from fastapi import Depends, FastAPI, Query
from fastapi.encoders import jsonable_encoder
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field

from .ai_service import ai_service_metadata
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
from .repository import (
    create_user,
    get_term_by_id,
    get_user_by_email,
    get_user_by_id,
    list_categories,
    list_terms,
    list_terms_by_category,
    record_learning_completion,
    search_terms,
)

app = FastAPI(title="AI-101 Backend", version="0.3.0")


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
