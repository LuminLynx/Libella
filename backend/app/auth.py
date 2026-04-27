from __future__ import annotations

import re
from datetime import datetime, timedelta, timezone
from typing import Any

import jwt
from fastapi import Header, HTTPException
from passlib.context import CryptContext

from .config import JWT_ALGORITHM, JWT_EXPIRATION_DAYS, JWT_SECRET

EMAIL_PATTERN = re.compile(r"^[^@\s]+@[^@\s]+\.[^@\s]+$")
MIN_PASSWORD_LENGTH = 8
MIN_DISPLAY_NAME_LENGTH = 2
MAX_DISPLAY_NAME_LENGTH = 50

_pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


class AuthError(Exception):
    def __init__(self, message: str, code: str, status_code: int = 401) -> None:
        super().__init__(message)
        self.code = code
        self.status_code = status_code


def hash_password(password: str) -> str:
    return _pwd_context.hash(password)


def verify_password(password: str, password_hash: str) -> bool:
    try:
        return _pwd_context.verify(password, password_hash)
    except ValueError:
        return False


def normalize_email(email: str) -> str:
    return email.strip().lower()


def validate_email(email: str) -> str:
    normalized = normalize_email(email)
    if not EMAIL_PATTERN.match(normalized):
        raise AuthError("Invalid email format.", code="INVALID_EMAIL", status_code=400)
    return normalized


def validate_password(password: str) -> str:
    if not isinstance(password, str) or len(password) < MIN_PASSWORD_LENGTH:
        raise AuthError(
            f"Password must be at least {MIN_PASSWORD_LENGTH} characters.",
            code="WEAK_PASSWORD",
            status_code=400,
        )
    return password


def validate_display_name(display_name: str) -> str:
    cleaned = display_name.strip()
    if not (MIN_DISPLAY_NAME_LENGTH <= len(cleaned) <= MAX_DISPLAY_NAME_LENGTH):
        raise AuthError(
            f"Display name must be {MIN_DISPLAY_NAME_LENGTH}-{MAX_DISPLAY_NAME_LENGTH} characters.",
            code="INVALID_DISPLAY_NAME",
            status_code=400,
        )
    return cleaned


def create_access_token(user_id: str) -> str:
    now = datetime.now(timezone.utc)
    payload = {
        "sub": user_id,
        "iat": int(now.timestamp()),
        "exp": int((now + timedelta(days=JWT_EXPIRATION_DAYS)).timestamp()),
    }
    return jwt.encode(payload, JWT_SECRET, algorithm=JWT_ALGORITHM)


def decode_access_token(token: str) -> dict[str, Any]:
    try:
        return jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
    except jwt.ExpiredSignatureError as error:
        raise AuthError("Token has expired.", code="TOKEN_EXPIRED") from error
    except jwt.InvalidTokenError as error:
        raise AuthError("Invalid token.", code="INVALID_TOKEN") from error


def _extract_token(authorization: str | None) -> str | None:
    if not authorization:
        return None
    parts = authorization.split(" ", 1)
    if len(parts) != 2 or parts[0].lower() != "bearer":
        return None
    token = parts[1].strip()
    return token or None


def optional_user_id(authorization: str | None = Header(default=None)) -> str | None:
    """FastAPI dependency: returns the authenticated user_id, or None if no/invalid token."""
    token = _extract_token(authorization)
    if token is None:
        return None
    try:
        payload = decode_access_token(token)
    except AuthError:
        return None
    sub = payload.get("sub")
    return sub if isinstance(sub, str) else None


def required_user_id(authorization: str | None = Header(default=None)) -> str:
    """FastAPI dependency: raises 401 if no valid token is present."""
    token = _extract_token(authorization)
    if token is None:
        raise HTTPException(
            status_code=401,
            detail={"code": "AUTH_REQUIRED", "message": "Authentication required."},
        )
    try:
        payload = decode_access_token(token)
    except AuthError as error:
        raise HTTPException(
            status_code=401,
            detail={"code": error.code, "message": str(error)},
        ) from error
    sub = payload.get("sub")
    if not isinstance(sub, str):
        raise HTTPException(
            status_code=401,
            detail={"code": "INVALID_TOKEN", "message": "Token payload missing subject."},
        )
    return sub
