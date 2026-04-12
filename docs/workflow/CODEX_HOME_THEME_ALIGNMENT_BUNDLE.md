# CODEX_HOME_THEME_ALIGNMENT_BUNDLE.md

## Task

Implement the MVP Home + Theme + UI alignment bundle for the FOSS-101 Android app, and prepare the work as a focused GitHub PR.

---

## Project Context

Use these repo documents as source of truth:

- `docs/roadmap/ROADMAP.md`
- `docs/architecture/BACKEND_DATABASE_SCOPE.md`
- `docs/workflow/AGENTS.md`
- `docs/workflow/TASKS.md`
- `docs/workflow/GEMINI_AGENT_ROADMAP.md`

### Constraints
- MVP only
- Do not add accounts, chat features, AI tools, trend logic, admin features, bookmarks, or backend code
- Keep the implementation compile-safe and Android Studio-friendly
- Keep the diff focused and PR-reviewable
- Do not run Gradle commands for this task
- Do not attempt Android build validation in Codex; validation will be performed manually in Android Studio after PR review

---

## Goal

Improve the Home screen so it clearly reflects the currently working MVP flows, create the missing app theme package and theme wrapper, and normalize basic typography and spacing across the current MVP screens.

---

## Requirements

1. Improve the Home screen so it clearly reflects the currently working MVP flows:
   - Browse Terms
   - Categories
   - Search
   - Settings

2. Keep the Home screen simple and MVP-safe.

3. Do not expand or prioritize non-MVP screens.

4. Create the missing app theme package if it does not exist.

5. Add a custom app theme wrapper and use it in `MainActivity`.

6. Normalize basic typography and spacing across the MVP screens that already exist:
   - Home
   - Browse
   - Details
   - Categories
   - Search

7. Keep changes conservative and compile-oriented.

8. Do not touch backend code.

9. Do not refactor unrelated screens.

10. Use existing reusable components where appropriate, but do not over-engineer.

---

## Expected Files Likely Involved

- `app/src/main/java/com/example/foss101/MainActivity.kt`
- `app/src/main/java/com/example/foss101/ui/home/HomeScreen.kt`
- `app/src/main/java/com/example/foss101/ui/theme/*`

Optional:
- a few existing MVP screen files for spacing/typography alignment
- one or two existing reusable component files if truly needed

Only touch other files if truly required.

---

## Acceptance Criteria

- Home screen remains functional and better aligned with MVP
- A custom app theme package exists
- `MainActivity` uses the custom app theme
- MVP screens remain simple and visually more consistent
- Non-MVP screens remain untouched unless required for compilation
- Diff remains focused on Home + Theme + UI alignment only

---

## PR Requirements

- keep the PR focused
- include a short summary of changed files
- mention any assumptions made
- do not include unrelated cleanup

---

## Deliverable

Provide a focused implementation for the Home + Theme + UI alignment bundle as a GitHub PR-ready change set.
