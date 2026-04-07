# TASKS.md

## 1. Purpose

This file lists the current next implementation tasks for FOSS-101.

These tasks are intentionally small and suitable for AI-agent execution followed by manual validation.

---

## 2. Current Priority

The current priority is **architecture before UI polish**.

That means the project should focus on:
1. structure
2. data models
3. repository abstraction
4. backend/API baseline
5. reusable UI foundations
6. layout planning later

Do not prioritize icons or visual polish yet.

---

## 3. Active Task List

### Task 01 — Organize repository docs
**Goal:** standardize project documentation under `docs/`.

**Expected result:**
- `docs/roadmap/ROADMAP.md`
- `docs/architecture/BACKEND_DATABASE_SCOPE.md`
- `docs/workflow/AGENTS.md`
- `docs/workflow/TASKS.md`

**Notes:**
- keep `README.md` in the project root
- avoid scattering markdown files across the repo

---

### Task 02 — Review Android package structure
**Goal:** inspect the current Android package organization and compare it to the target structure.

**Target structure:**
- `data/`
- `data/remote/`
- `data/repository/`
- `model/`
- `ui/`
- `ui/screens/`
- `ui/components/`
- `navigation/`
- `viewmodel/`
- `theme/`

**Expected result:**
- list what already exists
- list what should be added or cleaned up
- avoid unnecessary changes until the structure is agreed

---

### Task 03 — Define glossary domain models
**Goal:** create the initial domain models for glossary content.

**Expected models:**
- `GlossaryTerm`
- `Category`

**Suggested `GlossaryTerm` fields:**
- `id`
- `term`
- `shortDefinition`
- `fullExplanation`
- `categoryId`
- `tags`
- `relatedTerms`
- `exampleUsage`
- `source`

**Suggested `Category` fields:**
- `id`
- `name`
- `description`

---

### Task 04 — Create repository abstraction
**Goal:** define a repository interface for glossary data access.

**Expected result:**
- `GlossaryRepository` interface

**Suggested responsibilities:**
- get all terms
- get term by ID
- get categories
- search terms
- filter terms by category

**Rule:**
UI should depend on this abstraction, not directly on mock data or API code.

---

### Task 05 — Create mock repository
**Goal:** enable Android UI work without waiting for backend completion.

**Expected result:**
- `MockGlossaryRepository`

**Requirements:**
- returns sample terms
- returns sample categories
- supports simple search behavior
- supports category filtering

---

### Task 06 — Create starter dataset
**Goal:** create a small sample dataset for UI development.

**Expected size:**
- 20 to 30 glossary terms
- 4 to 6 categories

**Requirements:**
- terms should be realistic
- category assignment should be consistent
- content should be clean enough to test browse/search/details flows

---

### Task 07 — Review navigation routes
**Goal:** standardize the current navigation routes.

**Expected routes:**
- `home`
- `browse`
- `categories`
- `search`
- `details/{termId}`
- `settings`

**Expected result:**
- document current routes
- align naming if needed
- avoid adding future routes prematurely

---

### Task 08 — Verify theme and dependency foundation
**Goal:** make sure the Android project foundation is stable before further implementation.

**Check for:**
- Compose setup
- Material 3
- Navigation Compose
- Lifecycle/ViewModel support
- stable theme organization

**Expected result:**
- list any missing or unnecessary dependencies
- confirm the current foundation is adequate for the next tasks

---

### Task 09 — List reusable UI components
**Goal:** define the first reusable UI building blocks before more screen work.

**Likely components:**
- app top bar
- primary action button
- section header
- glossary list item
- category card
- search bar
- empty state view
- loading state view
- detail content block

**Expected result:**
- a prioritized reusable component list
- no implementation yet unless explicitly chosen as the next task

---

### Task 10 — Draft MVP API contract
**Goal:** convert the approved backend scope into a first API contract draft.

**Expected MVP endpoints:**
- `GET /terms`
- `GET /terms/{id}`
- `GET /categories`
- `GET /terms?categoryId={id}`
- `GET /terms?search={query}`

**Expected result:**
- endpoint list
- response-shape draft
- error response outline

---

## 4. Execution Order

The preferred near-term order is:

1. Task 01 — Organize repository docs
2. Task 02 — Review Android package structure
3. Task 03 — Define glossary domain models
4. Task 04 — Create repository abstraction
5. Task 05 — Create mock repository
6. Task 06 — Create starter dataset
7. Task 07 — Review navigation routes
8. Task 08 — Verify theme and dependency foundation
9. Task 09 — List reusable UI components
10. Task 10 — Draft MVP API contract

---

## 5. Agent Execution Rules

When an AI agent works on a task in this file, it should:
- pick one task at a time
- keep the diff small
- avoid unrelated changes
- avoid adding out-of-scope features
- leave validation to the correct environment

For Android tasks:
- validate in Android Studio

For backend tasks:
- validate in the backend runtime

---

## 6. Not Current Priorities

These are explicitly not current priorities:
- custom icons
- AI chat
- trend features
- user accounts
- admin panel
- large-scale UI polish
- major refactors without task justification

---

## 7. Completion Rule

When a task is finished and validated, it should either:
- be marked complete in this file, or
- be moved into a completed section later

Until then, treat all tasks above as pending.
