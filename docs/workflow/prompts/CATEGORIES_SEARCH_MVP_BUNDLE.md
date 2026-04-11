# CATEGORIES_SEARCH_MVP_BUNDLE.md

## Task

Implement the MVP Categories + Search flow for the FOSS-101 Android app using the existing mock repository.

---

## Project Context

Follow these repo docs as source of truth:

- `docs/roadmap/ROADMAP.md`
- `docs/architecture/BACKEND_DATABASE_SCOPE.md`
- `docs/workflow/AGENTS.md`
- `docs/workflow/TASKS.md`
- `docs/workflow/GEMINI_AGENT_ROADMAP.md`

### Constraints
- MVP only
- Do not add accounts, chat features, AI tools, trend logic, admin features, bookmarks, or backend code
- Keep the implementation compile-safe and Android Studio-friendly
- Use a bounded, focused diff

---

## Goal

Implement working mock-data Categories and Search flows, both leading to the existing Term Details screen.

---

## Requirements

1. Use the existing repository abstraction:
   - `GlossaryRepository`
   - `MockGlossaryRepository`

2. Update `CategoriesScreen` so it:
   - reads categories from `MockGlossaryRepository`
   - displays a list or grid of categories
   - shows at least category name and description
   - allows tapping a category

3. Category selection must:
   - show the glossary terms filtered by that category
   - keep the implementation simple and MVP-safe
   - reuse existing Browse/list presentation if appropriate

4. Update `SearchScreen` so it:
   - allows entering a search query
   - searches glossary terms from `MockGlossaryRepository`
   - displays matching terms in a scrollable list
   - shows at least `term` and `shortDefinition` for each result

5. Clicking a category-filtered term or search result must navigate to:
   - `details/{termId}`

6. Add simple fallback states where appropriate:
   - no categories available
   - no terms for selected category
   - no search results

7. Use existing reusable components where appropriate, but do not over-engineer.

8. Keep UI simple and MVP-safe.

9. Do not refactor unrelated screens.

10. Do not touch backend code.

11. Do not expand non-MVP routes except where strictly required for compilation.

---

## Expected Files Likely Involved

- `app/src/main/java/com/example/foss101/ui/categories/CategoriesScreen.kt`
- `app/src/main/java/com/example/foss101/ui/search/SearchScreen.kt`
- `app/src/main/java/com/example/foss101/navigation/AppNav.kt`
- optionally one or two small reusable UI components if helpful

Only touch other files if truly required.

---

## Acceptance Criteria

- `CategoriesScreen` compiles
- It uses `MockGlossaryRepository` data
- It shows categories clearly
- Selecting a category shows filtered glossary terms
- `SearchScreen` compiles
- It can search mock glossary terms
- It shows search results clearly
- Clicking a filtered term or search result navigates using `details/{termId}`
- Simple empty-result states are handled
- Project builds successfully
- Diff remains focused on Categories + Search MVP flow only

---

## Deliverable

Provide a small, focused implementation for the Categories + Search MVP flow.
