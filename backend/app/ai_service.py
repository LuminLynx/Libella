from __future__ import annotations

import json
from typing import Any, Literal

import httpx
from fastapi.encoders import jsonable_encoder

from .config import (
    AI_MODEL,
    AI_PROVIDER,
    AI_PROVIDER_API_KEY,
    AI_PROVIDER_BASE_URL,
)

ArtifactType = Literal["scenario", "challenge"]


class AIServiceError(RuntimeError):
    def __init__(self, message: str, code: str = "AI_REQUEST_FAILED") -> None:
        super().__init__(message)
        self.code = code


class AIUnavailableError(AIServiceError):
    def __init__(self, message: str = "AI provider is unavailable.") -> None:
        super().__init__(message=message, code="AI_UNAVAILABLE")


class AIService:
    def ask_glossary(self, question: str, term_context: dict[str, Any] | None) -> dict[str, Any]:
        context_lines = [
            "You are AI-101 Ask Glossary assistant.",
            "Answer using the glossary context. If unsure, say what is missing.",
            "Keep answer practical for learners and include a short summary.",
        ]
        if term_context:
            context_lines.append("Focused term context:")
            context_lines.append(json.dumps(jsonable_encoder(term_context)))

        response_schema = {
            "type": "object",
            "properties": {
                "answer": {"type": "string"},
                "summary": {"type": "string"},
                "relatedTermIds": {
                    "type": "array",
                    "items": {"type": "string"},
                },
            },
            "required": ["answer", "summary", "relatedTermIds"],
            "additionalProperties": False,
        }

        return self._chat_json(
            system_prompt="\n".join(context_lines),
            user_prompt=question,
            response_schema=response_schema,
            feature_name="ask_glossary",
        )

    def generate_artifact(self, term: dict[str, Any], artifact_type: ArtifactType) -> dict[str, Any]:
        serialized_term = json.dumps(jsonable_encoder(term))

        if artifact_type == "scenario":
            schema = {
                "type": "object",
                "properties": {
                    "title": {"type": "string"},
                    "difficulty": {"type": "string", "enum": ["beginner", "intermediate", "advanced"]},
                    "context": {"type": "string"},
                    "objective": {"type": "string"},
                    "tasks": {"type": "array", "items": {"type": "string"}},
                    "reflectionQuestions": {"type": "array", "items": {"type": "string"}},
                },
                "required": [
                    "title",
                    "difficulty",
                    "context",
                    "objective",
                    "tasks",
                    "reflectionQuestions",
                ],
                "additionalProperties": False,
            }
            user_prompt = (
                "Create a hands-on learning scenario for this glossary term. "
                "Term JSON: " + serialized_term
            )
        else:
            schema = {
                "type": "object",
                "properties": {
                    "title": {"type": "string"},
                    "difficulty": {"type": "string", "enum": ["beginner", "intermediate", "advanced"]},
                    "prompt": {"type": "string"},
                    "successCriteria": {"type": "array", "items": {"type": "string"}},
                    "hint": {"type": "string"},
                },
                "required": ["title", "difficulty", "prompt", "successCriteria", "hint"],
                "additionalProperties": False,
            }
            user_prompt = (
                "Create a practical learner challenge for this glossary term. "
                "Term JSON: " + serialized_term
            )

        return self._chat_json(
            system_prompt=(
                "You create concise, high-quality learning content for AI-101 learners. "
                "Use only term context and avoid fabricated claims."
            ),
            user_prompt=user_prompt,
            response_schema=schema,
            feature_name=artifact_type,
        )

    def _chat_json(
        self,
        *,
        system_prompt: str,
        user_prompt: str,
        response_schema: dict[str, Any],
        feature_name: str,
    ) -> dict[str, Any]:
        if not AI_PROVIDER_API_KEY:
            raise AIUnavailableError("AI provider key is not configured.")

        headers = {
            "Authorization": f"Bearer {AI_PROVIDER_API_KEY}",
            "Content-Type": "application/json",
        }
        payload = {
            "model": AI_MODEL,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
            "response_format": {
                "type": "json_schema",
                "json_schema": {
                    "name": f"ai101_{feature_name}",
                    "schema": response_schema,
                    "strict": True,
                },
            },
        }

        try:
            with httpx.Client(timeout=30.0) as client:
                response = client.post(
                    f"{AI_PROVIDER_BASE_URL}/chat/completions",
                    headers=headers,
                    json=payload,
                )
        except httpx.HTTPError as error:
            raise AIUnavailableError("Failed to reach AI provider.") from error

        if response.status_code >= 400:
            raise AIServiceError(
                message=f"AI provider returned HTTP {response.status_code}.",
                code="AI_PROVIDER_ERROR",
            )

        body = response.json()
        try:
            content = body["choices"][0]["message"]["content"]
        except (KeyError, IndexError, TypeError) as error:
            raise AIServiceError("AI response shape was invalid.", code="AI_RESPONSE_INVALID") from error

        try:
            parsed = json.loads(content)
            if not isinstance(parsed, dict):
                raise ValueError("json result must be an object")
            return parsed
        except (json.JSONDecodeError, ValueError) as error:
            raise AIServiceError("AI response JSON parsing failed.", code="AI_RESPONSE_INVALID") from error


ai_service = AIService()


def ai_service_metadata() -> dict[str, str | None]:
    return {
        "provider": AI_PROVIDER,
        "model": AI_MODEL,
    }
