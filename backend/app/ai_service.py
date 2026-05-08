"""F4 LLM grader — Anthropic Claude Sonnet 4.6 with prompt caching.

Implements the grader contract from docs/STRATEGY.md § T2:

  * **T2-A** Per-criterion checklist (Met/Not Met). No holistic score.
  * **T2-B** Flagged-or-graded confidence. If the grader is uncertain
    anywhere (any criterion under CONFIDENCE_FLAG_THRESHOLD), the answer
    is flagged "review needed" — surfaced in the response so the UI can
    show the canonical answer instead of pass/fail.
  * **T2-D** All four hallucination guardrails:
      1. Strict tool-call schema (forced via `tool_choice`).
      2. Source-grounding — the prompt cache contains the unit's bite,
         depth, sources, and the rubric criteria themselves; the grader
         never grades against memorized prior knowledge.
      3. Answer-quote requirement — the schema requires `answer_quote`
         per criterion. Empty string is allowed only when the answer
         doesn't address the criterion at all (and `met` must then be
         false).
      4. Structured tool-call output — we ignore any free-text content
         and parse only the tool_use block.
  * **T2-E** Anthropic Claude Sonnet 4.6 with prompt caching on the
    rubric + unit content so re-grading the same unit reuses cached
    tokens. Model id comes from config (`AI_MODEL`); change at deploy
    time, not in code.

Streaming the rationale (TT2 in STRATEGY.md) is deferred to a follow-up
PR — the gate criteria don't require it.

The Anthropic client is module-level so tests can monkey-patch
`_get_client()` without importing the SDK in tests that don't need it.
"""
from __future__ import annotations

import json
import logging
from dataclasses import dataclass
from typing import Any

from .config import AI_MODEL, AI_PROVIDER, AI_PROVIDER_API_KEY

LOGGER = logging.getLogger(__name__)

# Below this confidence on any criterion, we set `flagged = true` so the
# UI surfaces the canonical answer instead of pass/fail. Tuned at the
# Phase 2 gate; not load-bearing here.
CONFIDENCE_FLAG_THRESHOLD = 0.6

GRADER_SYSTEM_PROMPT = """You are an evaluator. You grade a learner's open-ended decision-making answer against a per-criterion rubric.

Rules:
- Grade each criterion independently as Met or Not Met.
- For each criterion, return a `confidence` between 0.0 and 1.0 reflecting how sure you are about the Met/Not Met determination.
- For each criterion, return a `rationale` (1–3 sentences) explaining the determination.
- For each criterion, return an `answer_quote` — the specific span from the learner's answer that you used to make the determination. If the answer does not address the criterion at all, set met=false and answer_quote to an empty string.
- Set the top-level `flagged` to true if you are uncertain about any criterion (typically confidence < 0.6) or if the answer is too short / off-topic to grade fairly.
- Return your output exclusively via the `submit_grades` tool call. Do not produce free-text after the tool call.
- Ground every judgement in the unit content and sources provided in the system context. Do not rely on outside knowledge."""


GRADE_TOOL_SCHEMA = {
    "name": "submit_grades",
    "description": "Submit per-criterion Met/Not Met grades for the learner's answer.",
    "input_schema": {
        "type": "object",
        "properties": {
            "grades": {
                "type": "array",
                "description": "One grade per rubric criterion, in any order.",
                "items": {
                    "type": "object",
                    "properties": {
                        "criterion_id": {
                            "type": "integer",
                            "description": "The criterion's id from the rubric provided in the system context.",
                        },
                        "met": {"type": "boolean"},
                        "confidence": {"type": "number", "minimum": 0.0, "maximum": 1.0},
                        "rationale": {"type": "string"},
                        "answer_quote": {
                            "type": "string",
                            "description": "Verbatim span from the learner's answer used for this judgement, or '' if the answer does not address this criterion.",
                        },
                    },
                    "required": ["criterion_id", "met", "confidence", "rationale", "answer_quote"],
                },
            },
            "flagged": {
                "type": "boolean",
                "description": "True if any criterion's confidence is low or the answer is unsuitable for fair grading.",
            },
        },
        "required": ["grades", "flagged"],
    },
}


class AIServiceError(RuntimeError):
    def __init__(self, message: str, code: str = "AI_REQUEST_FAILED") -> None:
        super().__init__(message)
        self.code = code


class AIUnavailableError(AIServiceError):
    def __init__(self, message: str = "AI provider is unavailable.") -> None:
        super().__init__(message=message, code="AI_UNAVAILABLE")


@dataclass
class GraderOutput:
    grades: list[dict[str, Any]]
    flagged: bool


def ai_service_metadata() -> dict[str, str | None]:
    return {
        "provider": AI_PROVIDER,
        "model": AI_MODEL,
    }


def _get_client():
    """Return an Anthropic client. Imported lazily so tests can stub
    `grade_decision_answer` without the SDK side-effects firing at
    import time (and so `AI_PROVIDER_API_KEY` only needs to be set in
    environments that actually call the grader).
    """
    if not AI_PROVIDER_API_KEY:
        raise AIUnavailableError("AI_PROVIDER_API_KEY is not configured.")
    import anthropic

    return anthropic.Anthropic(api_key=AI_PROVIDER_API_KEY)


def _build_cached_context(unit: dict[str, Any]) -> str:
    """The portion of the prompt that's stable per unit and worth caching.

    Concatenates the unit's content slots, sources, and rubric criteria
    into a single text block. Anthropic's `cache_control` is applied to
    this block so subsequent grading calls for the same unit reuse the
    cached tokens (T2-E).
    """
    sources_lines = []
    for s in unit.get("sources", []) or []:
        primary = " (primary)" if s.get("primarySource") else ""
        date = s.get("date", "")
        sources_lines.append(f"- {s.get('title', '')} — {s.get('url', '')} [{date}{primary}]")
    sources_text = "\n".join(sources_lines) if sources_lines else "(none)"

    criteria_lines = []
    rubric = unit.get("rubric") or {}
    for c in rubric.get("criteria", []) or []:
        criteria_lines.append(f"- id={c['id']}: {c['text']}")
    criteria_text = "\n".join(criteria_lines) if criteria_lines else "(none)"

    decision_prompt = (unit.get("decisionPrompt") or {}).get("promptMd", "")

    return (
        f"# Unit: {unit.get('title', '')}\n\n"
        f"## Definition\n{unit.get('definition', '')}\n\n"
        f"## Trade-off framing\n{unit.get('tradeOffFraming', '')}\n\n"
        f"## 90-second bite\n{unit.get('biteMd', '')}\n\n"
        f"## Depth\n{unit.get('depthMd', '')}\n\n"
        f"## Sources\n{sources_text}\n\n"
        f"## Decision prompt\n{decision_prompt}\n\n"
        f"## Rubric criteria\n{criteria_text}\n"
    )


def _validate_grader_output(payload: dict[str, Any], expected_criterion_ids: set[int]) -> GraderOutput:
    """Enforce the T2-D guardrails on what the model returned.

    Raises AIServiceError on schema violations: the endpoint catches and
    surfaces the failure rather than persisting partial / suspicious
    grades.
    """
    if not isinstance(payload, dict):
        raise AIServiceError("Grader returned a non-object payload.")

    grades = payload.get("grades")
    flagged = payload.get("flagged")
    if not isinstance(grades, list) or not isinstance(flagged, bool):
        raise AIServiceError("Grader payload missing 'grades' (list) or 'flagged' (bool).")

    seen_ids: set[int] = set()
    cleaned: list[dict[str, Any]] = []
    for entry in grades:
        if not isinstance(entry, dict):
            raise AIServiceError("Grader returned a non-object grade entry.")
        cid = entry.get("criterion_id")
        met = entry.get("met")
        confidence = entry.get("confidence")
        rationale = entry.get("rationale")
        answer_quote = entry.get("answer_quote")

        if not isinstance(cid, int) or cid not in expected_criterion_ids:
            raise AIServiceError(f"Grader returned grade for unknown criterion_id {cid!r}.")
        if cid in seen_ids:
            raise AIServiceError(f"Grader returned duplicate grade for criterion_id {cid}.")
        seen_ids.add(cid)
        if not isinstance(met, bool):
            raise AIServiceError(f"Grade for criterion {cid} missing 'met' boolean.")
        if not isinstance(confidence, (int, float)) or not 0.0 <= float(confidence) <= 1.0:
            raise AIServiceError(f"Grade for criterion {cid} has invalid confidence {confidence!r}.")
        if not isinstance(rationale, str) or not rationale.strip():
            raise AIServiceError(f"Grade for criterion {cid} missing rationale.")
        if not isinstance(answer_quote, str):
            raise AIServiceError(f"Grade for criterion {cid} missing answer_quote (string).")
        if met and not answer_quote.strip():
            # T2-D answer-quote guardrail: a Met determination must point
            # at the span of the answer that supports it.
            raise AIServiceError(
                f"Grade for criterion {cid} is met=true but answer_quote is empty."
            )
        cleaned.append(
            {
                "criterion_id": cid,
                "met": met,
                "confidence": float(confidence),
                "rationale": rationale.strip(),
                "answer_quote": answer_quote.strip(),
            }
        )

    missing = expected_criterion_ids - seen_ids
    if missing:
        raise AIServiceError(f"Grader did not return grades for criteria: {sorted(missing)}.")

    # Override the model's `flagged` if any criterion came back below the
    # threshold. The model sometimes under-reports its own uncertainty;
    # we err on the side of flagging.
    if any(g["confidence"] < CONFIDENCE_FLAG_THRESHOLD for g in cleaned):
        flagged = True

    return GraderOutput(grades=cleaned, flagged=flagged)


def grade_decision_answer(unit: dict[str, Any], answer: str) -> GraderOutput:
    """Run the grader for one (unit, answer) pair.

    Raises AIServiceError on any failure (network, schema violation,
    guardrail breach). Callers persist nothing on failure.
    """
    answer = (answer or "").strip()
    if not answer:
        raise AIServiceError("Answer is empty.", code="ANSWER_EMPTY")

    rubric = unit.get("rubric") or {}
    criteria = rubric.get("criteria") or []
    expected_ids = {int(c["id"]) for c in criteria}
    if not expected_ids:
        raise AIServiceError("Unit has no rubric criteria to grade against.", code="UNIT_NOT_GRADABLE")

    cached_context = _build_cached_context(unit)

    try:
        client = _get_client()
        response = client.messages.create(
            model=AI_MODEL,
            max_tokens=4000,
            system=[
                {"type": "text", "text": GRADER_SYSTEM_PROMPT},
                {
                    "type": "text",
                    "text": cached_context,
                    "cache_control": {"type": "ephemeral"},
                },
            ],
            tools=[GRADE_TOOL_SCHEMA],
            tool_choice={"type": "tool", "name": "submit_grades"},
            messages=[
                {
                    "role": "user",
                    "content": f"Learner's answer:\n\n{answer}",
                }
            ],
        )
    except AIServiceError:
        raise
    except Exception as exc:
        LOGGER.exception("grader call failed")
        raise AIUnavailableError(f"Grader provider error: {exc}") from exc

    tool_use_payload: dict[str, Any] | None = None
    for block in getattr(response, "content", []) or []:
        if getattr(block, "type", None) == "tool_use" and getattr(block, "name", None) == "submit_grades":
            raw_input = getattr(block, "input", None)
            if isinstance(raw_input, dict):
                tool_use_payload = raw_input
            elif isinstance(raw_input, str):
                try:
                    tool_use_payload = json.loads(raw_input)
                except json.JSONDecodeError as exc:
                    raise AIServiceError(f"Grader tool input was not valid JSON: {exc}") from exc
            break

    if tool_use_payload is None:
        raise AIServiceError("Grader did not produce a submit_grades tool call.")

    return _validate_grader_output(tool_use_payload, expected_ids)
