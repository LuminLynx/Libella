# AGENTS.md

## 1. Purpose

This document defines how AI agents should work on the FOSS-101 project.

It applies to:
- Codex
- GitHub Copilot Agent
- any future coding agent used in this repository

This file exists to keep implementation aligned with the project roadmap, backend/database scope, and Android Studio validation workflow.

---

## 2. Project Context

FOSS-101 is a native Android glossary app with:
- an Android client
- a backend API
- a database

The MVP is focused on glossary functionality only.

Approved MVP constraints:
1. No user accounts
2. No chat / AI tools
3. No admin panel
4. Online-first app
5. Content managed manually at first

Agents must respect these constraints unless the project docs are explicitly updated.

---

## 3. Source-of-Truth Documents

Agents should use these documents as source of truth:

1. `docs/roadmap/ROADMAP.md`
2. `docs/architecture/BACKEND_DATABASE_SCOPE.md`
3. `docs/workflow/AGENTS.md`
4. `docs/workflow/TASKS.md`

If implementation suggestions conflict with these documents, the docs take priority.

---

## 4. Core Working Rules

### 4.1 Keep tasks small
Agents must work in small, reviewable tasks.

Preferred task shape:
- one focused objective
- small number of changed files
- clear intent
- easy manual review

Avoid:
- large repo-wide rewrites
- speculative refactors unrelated to the active task
- mixing architecture changes with UI polish in one step

### 4.2 Do not invent scope
Agents must not add features outside approved scope.

Examples of disallowed MVP additions unless explicitly requested:
- authentication
- bookmarks/favorites
- AI chat
- trend features
- admin tooling
- analytics systems

### 4.3 Prefer clean architecture
Agents should favor:
- separation of concerns
- repository abstraction
- reusable composables
- ViewModel-managed UI state
- centralized navigation
- clear package structure

### 4.4 Do not treat generated code as validated
Code proposed by agents is not automatically accepted.
All meaningful changes must be validated in the correct environment.

---

## 5. Validation Rules

### 5.1 Android code
Android code must be validated in Android Studio.

Validation includes:
- Gradle sync
- successful build
- navigation checks
- UI behavior checks
- runtime error checks

### 5.2 Backend code
Backend code must be validated in its backend runtime environment.

### 5.3 Database changes
Database changes must be reviewed against the approved schema direction and integration needs.

---

## 6. Android-Specific Guidance

Agents should assume:
- Android Studio is the real Android validation environment
- Gradle/emulator validation may not be available to the agent
- Compose code must be conservative and compatible with the existing project setup
- imports and dependencies should be kept minimal and intentional

### 6.1 Preferred Android architecture
- Kotlin
- Jetpack Compose
- Navigation Compose
- ViewModel
- Repository pattern

### 6.2 UI data rule
UI must not depend directly on hardcoded mock data forever.

Preferred path:
- `GlossaryRepository`
- `MockGlossaryRepository` for temporary UI development
- `ApiGlossaryRepository` for real integration

### 6.3 UI work order
When building Android UI, agents should follow this order:
1. models and repository abstraction
2. reusable components
3. screen structure
4. screen logic
5. polish later

Do not jump to icons or visual polish before layout and structure are settled.

---

## 7. Backend and Database Guidance

### 7.1 MVP backend responsibilities
The backend should support:
- list terms
- get term details
- list categories
- filter by category
- search terms

### 7.2 MVP database responsibilities
The database should store:
- glossary terms
- categories
- related-term relationships if used

### 7.3 Out of scope for MVP
Do not add unless explicitly approved:
- accounts
- auth
- chat
- AI answer generation
- admin dashboard
- advanced sync features

---

## 8. Documentation Rules

Agents should update docs when architecture or workflow changes materially.

Examples:
- new package structure
- updated API contract
- major roadmap changes
- changed MVP scope

Do not silently change architecture without reflecting it in docs.

---

## 9. Commit and Diff Expectations

Preferred output from agents:
- small diffs
- clear rationale
- no unrelated file churn
- meaningful commit-ready units

Good examples:
- create glossary term model and category model
- add repository interface
- implement mock repository
- refactor navigation routes into constants

Bad examples:
- rewrite multiple screens and architecture at once
- add polish, features, and refactors in one step

---

## 10. Coding Style Expectations

Agents should prefer:
- readable names
- focused classes/files
- minimal duplication
- explicit models
- small composables
- predictable state flow

Avoid:
- overly clever abstractions
- unnecessary frameworks
- hidden magic behavior
- tight coupling between UI and data access

---

## 11. Escalation Rule

If a task would require changing approved scope, the agent should not guess.
Instead, the task should be deferred until the project docs are updated.

---

## 12. Summary Rule

For FOSS-101:
- use AI agents heavily
- keep tasks small
- keep scope disciplined
- validate Android work in Android Studio
- validate backend work in the backend environment
- follow repo docs as source of truth
