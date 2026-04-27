from __future__ import annotations

import time

import pytest

from app.auth import (
    AuthError,
    create_access_token,
    decode_access_token,
    hash_password,
    normalize_email,
    validate_display_name,
    validate_email,
    validate_password,
    verify_password,
)


def test_password_hash_round_trip() -> None:
    password = "Sup3rSecret!"
    hashed = hash_password(password)
    assert hashed != password
    assert verify_password(password, hashed)
    assert not verify_password("wrong-password", hashed)


def test_jwt_round_trip() -> None:
    token = create_access_token("u-abc123")
    payload = decode_access_token(token)
    assert payload["sub"] == "u-abc123"
    assert "iat" in payload and "exp" in payload
    assert payload["exp"] > payload["iat"]


def test_jwt_invalid_token_raises() -> None:
    with pytest.raises(AuthError):
        decode_access_token("not-a-jwt-token")


def test_validate_email_normalizes_and_rejects_invalid() -> None:
    assert validate_email("  USER@Example.COM ") == "user@example.com"
    assert normalize_email("Foo@Bar.io") == "foo@bar.io"
    with pytest.raises(AuthError):
        validate_email("not-an-email")
    with pytest.raises(AuthError):
        validate_email("missing@dot")


def test_validate_password_min_length() -> None:
    validate_password("longenough")
    with pytest.raises(AuthError):
        validate_password("short")


def test_validate_display_name_bounds() -> None:
    assert validate_display_name("  Ada  ") == "Ada"
    with pytest.raises(AuthError):
        validate_display_name("A")
    with pytest.raises(AuthError):
        validate_display_name("X" * 100)
