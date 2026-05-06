from __future__ import annotations

from .config import AI_MODEL, AI_PROVIDER

# Scaffold for the F4 LLM grader (Phase 2). The previous OpenAI-flavored
# `ask_glossary` and `generate_artifact` methods, the LearningPreset registry,
# and the chat/completions plumbing have been removed per docs/AUDIT.md §2.5
# ("RESHAPE → rewrite ... Keep the file only as scaffolding for the rewrite —
# its current shape is wrong end-to-end.")
#
# Phase 2 will rebuild this module against Anthropic Claude Sonnet 4.6 with
# prompt caching on the rubric, structured tool-call output, all four T2-D
# hallucination guardrails, and streaming rationale (TT2 from STRATEGY.md).


class AIServiceError(RuntimeError):
    def __init__(self, message: str, code: str = "AI_REQUEST_FAILED") -> None:
        super().__init__(message)
        self.code = code


class AIUnavailableError(AIServiceError):
    def __init__(self, message: str = "AI provider is unavailable.") -> None:
        super().__init__(message=message, code="AI_UNAVAILABLE")


def ai_service_metadata() -> dict[str, str | None]:
    return {
        "provider": AI_PROVIDER,
        "model": AI_MODEL,
    }
