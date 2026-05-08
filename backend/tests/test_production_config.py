"""Tests for backend/app/config.py:validate_production_config.

Pure-Python: no DB, no SDKs. Covers the fail-fast gate that refuses to
boot under APP_ENV=production with default secrets in place.
"""
from __future__ import annotations

import pytest

from app import config
from app.config import ProductionConfigError, validate_production_config


@pytest.fixture(autouse=True)
def _restore_env(monkeypatch: pytest.MonkeyPatch) -> None:
    """Each test starts from a clean slate of the module-level config
    constants. We monkeypatch the module attributes directly because
    `validate_production_config` reads them via lambdas that close over
    the module namespace.
    """
    # No setup-time mutation; tests set what they need.
    yield


def test_noop_when_app_env_is_development(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(config, "APP_ENV", "development")
    monkeypatch.setattr(config, "JWT_SECRET", "change-me-in-production")
    monkeypatch.setattr(config, "POSTGRES_PASSWORD", "postgres")
    monkeypatch.setattr(config, "AI_PROVIDER_API_KEY", "")
    # Should not raise — the gate explicitly tolerates default values
    # outside production so local dev and CI runs aren't blocked.
    validate_production_config()


def test_passes_in_production_with_real_values(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(config, "APP_ENV", "production")
    monkeypatch.setattr(config, "JWT_SECRET", "an-actual-strong-secret-from-deploy-env")
    monkeypatch.setattr(config, "POSTGRES_PASSWORD", "real-prod-password")
    monkeypatch.setattr(config, "AI_PROVIDER_API_KEY", "sk-ant-real-key")
    validate_production_config()


def test_fails_in_production_when_jwt_secret_is_default(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(config, "APP_ENV", "production")
    monkeypatch.setattr(config, "JWT_SECRET", "change-me-in-production")
    monkeypatch.setattr(config, "POSTGRES_PASSWORD", "real")
    monkeypatch.setattr(config, "AI_PROVIDER_API_KEY", "sk-ant-real")

    with pytest.raises(ProductionConfigError, match="JWT_SECRET"):
        validate_production_config()


def test_fails_in_production_when_postgres_password_is_default(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(config, "APP_ENV", "production")
    monkeypatch.setattr(config, "JWT_SECRET", "real")
    monkeypatch.setattr(config, "POSTGRES_PASSWORD", "postgres")
    monkeypatch.setattr(config, "AI_PROVIDER_API_KEY", "sk-ant-real")

    with pytest.raises(ProductionConfigError, match="POSTGRES_PASSWORD"):
        validate_production_config()


def test_fails_in_production_when_ai_provider_api_key_is_empty(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(config, "APP_ENV", "production")
    monkeypatch.setattr(config, "JWT_SECRET", "real")
    monkeypatch.setattr(config, "POSTGRES_PASSWORD", "real")
    monkeypatch.setattr(config, "AI_PROVIDER_API_KEY", "")

    with pytest.raises(ProductionConfigError, match="AI_PROVIDER_API_KEY"):
        validate_production_config()


def test_reports_all_problems_at_once(monkeypatch: pytest.MonkeyPatch) -> None:
    """The gate should surface every misconfiguration in one message,
    not bisect — operators shouldn't have to fix-redeploy three times.
    """
    monkeypatch.setattr(config, "APP_ENV", "production")
    monkeypatch.setattr(config, "JWT_SECRET", "change-me-in-production")
    monkeypatch.setattr(config, "POSTGRES_PASSWORD", "postgres")
    monkeypatch.setattr(config, "AI_PROVIDER_API_KEY", "")

    with pytest.raises(ProductionConfigError) as exc_info:
        validate_production_config()

    message = str(exc_info.value)
    assert "JWT_SECRET" in message
    assert "POSTGRES_PASSWORD" in message
    assert "AI_PROVIDER_API_KEY" in message


def test_staging_env_is_treated_as_non_production(monkeypatch: pytest.MonkeyPatch) -> None:
    """Only the literal string 'production' triggers the gate. A
    'staging' env can run with whatever its operator chose — the
    intent is to protect the launch surface, not force every
    environment through prod-grade secret hygiene.
    """
    monkeypatch.setattr(config, "APP_ENV", "staging")
    monkeypatch.setattr(config, "JWT_SECRET", "change-me-in-production")
    monkeypatch.setattr(config, "POSTGRES_PASSWORD", "postgres")
    monkeypatch.setattr(config, "AI_PROVIDER_API_KEY", "")
    validate_production_config()  # should not raise
