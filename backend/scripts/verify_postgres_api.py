"""PostgreSQL-backed smoke verification for stable API flows.

Requires DATABASE_URL (or POSTGRES_* fallback env vars) pointing at a reachable PostgreSQL instance.
"""

from fastapi.testclient import TestClient

from backend.app.main import app
from backend.scripts.seed_db import seed


def assert_envelope(payload: dict) -> None:
    assert "data" in payload
    assert "error" in payload


def run_verification() -> None:
    seed(reset=True)

    with TestClient(app) as client:
        browse_resp = client.get("/api/v1/terms")
        assert browse_resp.status_code == 200
        browse_payload = browse_resp.json()
        assert_envelope(browse_payload)
        assert isinstance(browse_payload["data"], list)
        assert len(browse_payload["data"]) > 0

        categories_resp = client.get("/api/v1/categories")
        assert categories_resp.status_code == 200
        categories_payload = categories_resp.json()
        assert_envelope(categories_payload)
        assert isinstance(categories_payload["data"], list)
        assert len(categories_payload["data"]) > 0

        sample_term = browse_payload["data"][0]
        term_id = sample_term["id"]
        category_id = sample_term["categoryId"]

        details_resp = client.get(f"/api/v1/terms/{term_id}")
        assert details_resp.status_code == 200
        details_payload = details_resp.json()
        assert_envelope(details_payload)
        assert details_payload["data"]["id"] == term_id

        by_category_resp = client.get(f"/api/v1/categories/{category_id}/terms")
        assert by_category_resp.status_code == 200
        by_category_payload = by_category_resp.json()
        assert_envelope(by_category_payload)
        assert isinstance(by_category_payload["data"], list)
        assert len(by_category_payload["data"]) > 0

        term_token = sample_term["term"].split()[0]
        search_resp = client.get("/api/v1/search/terms", params={"q": term_token})
        assert search_resp.status_code == 200
        search_payload = search_resp.json()
        assert_envelope(search_payload)
        assert isinstance(search_payload["data"], list)
        assert any(item["id"] == term_id for item in search_payload["data"])

        not_found_resp = client.get("/api/v1/terms/term-does-not-exist")
        assert not_found_resp.status_code == 404
        not_found_payload = not_found_resp.json()
        assert_envelope(not_found_payload)
        assert not_found_payload["data"] is None
        assert not_found_payload["error"]["code"] == "TERM_NOT_FOUND"


if __name__ == "__main__":
    run_verification()
    print("PostgreSQL API verification passed.")
