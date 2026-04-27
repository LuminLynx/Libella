from __future__ import annotations

import pytest

from app.ai_service import DEFAULT_PRESET, PRESETS, resolve_preset


def test_default_preset_is_in_presets() -> None:
    assert DEFAULT_PRESET in PRESETS


def test_resolve_preset_falls_back_on_unknown() -> None:
    assert resolve_preset("not-a-real-preset") == PRESETS[DEFAULT_PRESET]
    assert resolve_preset(None) == PRESETS[DEFAULT_PRESET]
    assert resolve_preset("") == PRESETS[DEFAULT_PRESET]


def test_each_preset_has_required_keys() -> None:
    required = {"label", "difficulty", "tone", "scenario_focus", "challenge_focus"}
    for key, bundle in PRESETS.items():
        missing = required - bundle.keys()
        assert not missing, f"preset {key!r} missing keys: {missing}"


@pytest.mark.parametrize(
    "preset_key,expected_difficulty",
    [
        ("quick_recap", "beginner"),
        ("interview_prep", "intermediate"),
        ("hands_on_coding", "intermediate"),
        ("conceptual_deep_dive", "advanced"),
    ],
)
def test_preset_difficulty_mapping(preset_key: str, expected_difficulty: str) -> None:
    bundle = resolve_preset(preset_key)
    assert bundle["difficulty"] == expected_difficulty


def test_all_four_presets_present() -> None:
    expected_keys = {
        "quick_recap",
        "interview_prep",
        "hands_on_coding",
        "conceptual_deep_dive",
    }
    assert set(PRESETS.keys()) == expected_keys
