"""PostgreSQL-backed smoke verification for stable API flows.

Requires DATABASE_URL (or POSTGRES_* fallback env vars) pointing at a reachable PostgreSQL instance.
"""

from fastapi.testclient import TestClient

from backend.app.main import app
from backend.scripts.seed_db import seed


EXPECTED_CANONICAL_FIELDS = {
    "slug",
    "term",
    "definition",
    "explanation",
    "humor",
    "seeAlso",
    "tags",
    "controversyLevel",
}


def assert_envelope(payload: dict) -> None:
    assert "data" in payload
    assert "error" in payload


def assert_canonical_term_shape(term: dict) -> None:
    assert EXPECTED_CANONICAL_FIELDS.issubset(term.keys())
    assert isinstance(term["slug"], str) and term["slug"]
    assert isinstance(term["tags"], list)
    assert term["tags"] == sorted(set(term["tags"]))
    assert isinstance(term["seeAlso"], list)
    assert all(isinstance(item, str) and item for item in term["seeAlso"])
    assert isinstance(term["controversyLevel"], int)
    assert 0 <= term["controversyLevel"] <= 3


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
        assert_canonical_term_shape(sample_term)
        term_id = sample_term["id"]
        category_id = sample_term["categoryId"]

        details_resp = client.get(f"/api/v1/terms/{term_id}")
        assert details_resp.status_code == 200
        details_payload = details_resp.json()
        assert_envelope(details_payload)
        assert details_payload["data"]["id"] == term_id
        assert_canonical_term_shape(details_payload["data"])

        by_category_resp = client.get(f"/api/v1/categories/{category_id}/terms")
        assert by_category_resp.status_code == 200
        by_category_payload = by_category_resp.json()
        assert_envelope(by_category_payload)
        assert isinstance(by_category_payload["data"], list)
        assert len(by_category_payload["data"]) > 0
        assert all(term["categoryId"] == category_id for term in by_category_payload["data"])

        term_token = sample_term["term"].split()[0]
        search_resp = client.get("/api/v1/search/terms", params={"q": term_token})
        assert search_resp.status_code == 200
        search_payload = search_resp.json()
        assert_envelope(search_payload)
        assert isinstance(search_payload["data"], list)
        assert any(item["id"] == term_id for item in search_payload["data"])
        assert all("slug" in item for item in search_payload["data"])

        # Log repeated missing queries and verify aggregation.
        for query in ["Mixture of Experts", "mixture    of experts", "Agentic Retrieval"]:
            resp = client.get("/api/v1/search/terms", params={"q": query})
            assert resp.status_code == 200

        missing_resp = client.get("/api/v1/search/missing-queries", params={"days": 90, "limit": 5})
        assert missing_resp.status_code == 200
        missing_payload = missing_resp.json()
        assert_envelope(missing_payload)
        missing_rows = missing_payload["data"]
        assert any(row["normalizedQuery"] == "mixture of experts" for row in missing_rows)
        moe_row = next(row for row in missing_rows if row["normalizedQuery"] == "mixture of experts")
        assert moe_row["searchCount"] >= 2

        draft_payload = {
            "term": "Mixture of Experts",
            "definition": "A sparse architecture where only selected sub-models are active per input.",
            "explanation": "Mixture-of-experts routes each token or input through a small subset of specialist expert networks, improving scale efficiency.",
            "humor": "Like calling only the right specialists into the meeting.",
            "seeAlso": ["transformer", "attention mechanism"],
            "tags": ["Architecture", "Sparse Models", "architecture"],
            "controversyLevel": 1,
            "status": "draft",
            "categoryId": "cat-ml-foundations",
            "sourceType": "manual",
            "sourceReference": "verification",
            "contributorId": "verify-user-1",
        }
        create_draft_resp = client.post("/api/v1/term-drafts", json=draft_payload)
        assert create_draft_resp.status_code == 201
        create_draft_payload = create_draft_resp.json()
        assert_envelope(create_draft_payload)
        draft = create_draft_payload["data"]
        assert draft["slug"] == "mixture-of-experts"
        assert draft["tags"] == ["architecture", "sparse-models"]
        assert draft["status"] == "draft"
        assert draft["contributorId"] == "verify-user-1"

        approve_resp = client.post(f"/api/v1/term-drafts/{draft['id']}/status", json={"status": "approved"})
        assert approve_resp.status_code == 200
        approve_payload = approve_resp.json()
        assert approve_payload["data"]["status"] == "approved"

        publish_resp = client.post(f"/api/v1/term-drafts/{draft['id']}/publish")
        assert publish_resp.status_code == 200
        publish_payload = publish_resp.json()
        assert_envelope(publish_payload)
        assert publish_payload["data"]["draft"]["status"] == "published"
        published_term = publish_payload["data"]["term"]
        assert published_term["slug"] == "mixture-of-experts"
        assert_canonical_term_shape(published_term)
        assert published_term["tags"] == ["architecture", "sparse-models"]
        assert "transformer" in published_term["seeAlso"]

        contributor_summary_resp = client.get("/api/v1/contributors/verify-user-1/summary")
        assert contributor_summary_resp.status_code == 200
        contributor_summary_payload = contributor_summary_resp.json()
        assert_envelope(contributor_summary_payload)
        summary = contributor_summary_payload["data"]
        assert summary["contributorId"] == "verify-user-1"
        assert summary["totalScore"] == 45
        assert summary["draftStats"]["totalDrafts"] >= 1
        assert summary["draftStats"]["publishedDrafts"] >= 1
        event_types = {item["eventType"]: item for item in summary["eventBreakdown"]}
        assert event_types["draft_submitted"]["count"] >= 1
        assert event_types["draft_approved"]["count"] >= 1
        assert event_types["draft_published"]["count"] >= 1

        search_new_term_resp = client.get("/api/v1/search/terms", params={"q": "Mixture of Experts"})
        assert search_new_term_resp.status_code == 200
        search_new_term_payload = search_new_term_resp.json()
        assert any(item["slug"] == "mixture-of-experts" for item in search_new_term_payload["data"])

        not_found_resp = client.get("/api/v1/terms/term-does-not-exist")
        assert not_found_resp.status_code == 404
        not_found_payload = not_found_resp.json()
        assert_envelope(not_found_payload)
        assert not_found_payload["data"] is None
        assert not_found_payload["error"]["code"] == "TERM_NOT_FOUND"


if __name__ == "__main__":
    run_verification()
    print("PostgreSQL API verification passed.")
