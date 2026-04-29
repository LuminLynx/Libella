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
PresetKey = Literal["quick_recap", "interview_prep", "hands_on_coding", "conceptual_deep_dive"]

DEFAULT_PRESET: PresetKey = "quick_recap"

# Each preset is a small bundle of generation hints injected into the prompt.
# Keep these short and concrete — verbose presets dilute the model's focus.
PRESETS: dict[str, dict[str, str]] = {
    "quick_recap": {
        "label": "Quick recap",
        "difficulty": "beginner",
        "tone": "friendly and concise",
        "scenario_focus": (
            "Build a simple, contextual scenario the learner can finish in about 5 minutes. "
            "Use 2 to 3 short tasks. Keep the context to 2 sentences."
        ),
        "challenge_focus": (
            "Make a short, low-pressure challenge that reinforces the core idea. "
            "Use 2 to 3 success criteria. Provide a gentle, encouraging hint."
        ),
    },
    "interview_prep": {
        "label": "Interview prep",
        "difficulty": "intermediate",
        "tone": "probing, in the style of a senior interviewer talking to a junior candidate",
        "scenario_focus": (
            "Frame the scenario as an interview probe. Tasks should require the learner to explain "
            "the concept, justify trade-offs, and connect it to a realistic engineering decision. "
            "Reflection questions must dig into edge cases and failure modes."
        ),
        "challenge_focus": (
            "Frame the prompt as an interview question. Success criteria should reward clear reasoning, "
            "naming trade-offs, and articulating when NOT to use the concept. Hint should be a Socratic nudge, "
            "not a giveaway."
        ),
    },
    "hands_on_coding": {
        "label": "Hands-on coding",
        "difficulty": "intermediate",
        "tone": "technical and example-driven",
        "scenario_focus": (
            "Tasks must include a small, runnable code snippet (Python preferred, pseudocode acceptable) "
            "that the learner can paste into a notebook. Keep snippets under 15 lines. "
            "Reflection should ask the learner to predict or modify the snippet's behavior."
        ),
        "challenge_focus": (
            "The challenge should produce a concrete artifact: a function signature, a code completion, "
            "or a small refactor. Success criteria must be objectively checkable. The hint may include "
            "a starter line of code."
        ),
    },
    "conceptual_deep_dive": {
        "label": "Conceptual deep-dive",
        "difficulty": "advanced",
        "tone": "rigorous and reflective",
        "scenario_focus": (
            "Push the learner past surface understanding: ask why the concept exists, when it breaks, "
            "and how it contrasts with related ideas. Reflection questions should require comparative analysis "
            "with at least one related concept (use the term's seeAlso when available)."
        ),
        "challenge_focus": (
            "Pose a question that cannot be answered by definition recall alone. Success criteria should "
            "demand the learner argue a position, pick between alternatives, or identify when the concept "
            "fails. The hint should point to a key tension, not a fact."
        ),
    },
}


def resolve_preset(key: str | None) -> dict[str, str]:
    return PRESETS.get(key or DEFAULT_PRESET) or PRESETS[DEFAULT_PRESET]


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

    def generate_artifact(
        self,
        term: dict[str, Any],
        artifact_type: ArtifactType,
        preset: str | None = None,
    ) -> dict[str, Any]:
        serialized_term = json.dumps(jsonable_encoder(term))
        preset_bundle = resolve_preset(preset)

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
            focus = preset_bundle["scenario_focus"]
            user_prompt = (
                f"Create a hands-on learning scenario for this glossary term.\n"
                f"Target difficulty: {preset_bundle['difficulty']}.\n"
                f"Tone: {preset_bundle['tone']}.\n"
                f"Focus instructions: {focus}\n"
                f"Term JSON: {serialized_term}"
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
            focus = preset_bundle["challenge_focus"]
            user_prompt = (
                f"Create a practical learner challenge for this glossary term.\n"
                f"Target difficulty: {preset_bundle['difficulty']}.\n"
                f"Tone: {preset_bundle['tone']}.\n"
                f"Focus instructions: {focus}\n"
                f"Term JSON: {serialized_term}"
            )

        return self._chat_json(
            system_prompt=(
                "You create concise, high-quality learning content for AI-101 learners. "
                "Use only the provided term context; do not invent facts. "
                "Keep prose tight: no preamble, no meta-commentary, no apologies."
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
