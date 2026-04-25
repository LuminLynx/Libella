# AGENTS.md

## 1. Purpose

This document defines how AI agents should work on the AI-101 project.

It applies to:
- Codex
- GitHub Copilot Agent
- any future coding agent used in this repository

This file exists to keep implementation aligned with the project roadmap, backend/database scope, and Android Studio validation workflow.

---

## 2. Project Context

AI-101 is a native Android glossary app with:
- an Android client
- a backend API
- a database

The app is focused on AI glossary functionality.

Approved MVP constraints:
1. No user accounts
2. No chat / AI tools inside the app MVP
3. No admin panel
4. Online-first app
5. Content managed manually at first

Agents must respect these constraints unless the project docs are explicitly updated.

### 2.1 Product direction
The product direction is now:
- **AI Terms Glossary**
- temporary app name: **AI-101**

Important:
- do **not** rename folders, file paths, package names, or repository names from `FOSS` to `AI` unless explicitly requested
- content, wording, and app-facing product direction should follow **AI Terms Glossary**
- internal technical names may remain unchanged for now

---

## 3. Source-of-Truth Documents

Agents should use these documents as source of truth:

1. `docs/roadmap/ROADMAP.md`
2. `docs/architecture/BACKEND_DATABASE_SCOPE.md`
3. `docs/workflow/AGENTS.md`
4. `docs/workflow/TASKS.md`
5. `docs/workflow/GEMINI_AGENT_ROADMAP.md`

If implementation suggestions conflict with these documents, the docs take priority.

---

## 4. Core Working Rules

### 4.1 Prefer bounded implementation bundles
Agents should work in **bounded, reviewable implementation bundles**, not tiny patch tasks.

Preferred task shape:
- one meaningful milestone or app slice
- multiple related files allowed
- clear scope boundaries
- easy PR review
- no unrelated cleanup

Examples of good task sizes:
- complete one MVP flow end-to-end
- complete one architectural milestone
- complete one backend slice
- complete one UI/system alignment pass

Avoid:
- trivial micro-edits as standalone tasks
- large repo-wide rewrites without boundaries
- speculative refactors unrelated to the active task
- mixing unrelated architecture and feature work in one PR

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
- ViewModel-managed UI state where appropriate
- centralized navigation
- clear package structure

### 4.4 Do not treat generated code as validated
Code proposed by agents is not automatically accepted.
All meaningful changes must be validated in the correct environment.

### 4.5 Keep app behavior stable unless the task explicitly changes it
For internal cleanup tasks:
- preserve visible user behavior
- preserve navigation behavior
- preserve working MVP flows

Only change visible behavior when the task explicitly calls for it.

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

### 5.4 Codex build and validation restriction
For this repository, Codex must not run Gradle commands.

Examples of commands that must not be run by Codex:
- `./gradlew`
- `gradle`
- `./gradlew build`
- `./gradlew assemble`
- `./gradlew test`
- `./gradlew lint`

Reason:
Codex's execution environment does not have a working Gradle toolchain for this project, so Gradle invocations there are unreliable and produce noise rather than validation. Authoritative Android build validation is performed manually in Android Studio after PR review and pull.

For Android tasks, Codex should:
- make code changes only
- keep changes compile-oriented and minimal
- rely on static code reasoning and file consistency checks
- avoid attempting Android build validation in its own environment

Note on other agents:
Other agents (e.g. Claude) whose execution environment includes a working Gradle toolchain may run `./gradlew` for static and compile-level feedback when network access permits. This is supplementary only — Android Studio remains the authoritative validation environment for all agents (see §5.1). Per-agent capabilities are documented in their own files (e.g. `docs/workflow/CLAUDE_CAPABILITIES.md`).

---

## 6. Android-Specific Guidance

Agents should assume:
- Android Studio is the real Android validation environment
- Gradle/emulator validation must not be attempted by Codex for this repository
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
When building Android UI, agents should generally follow this order:
1. models and repository abstraction
2. reusable components
3. screen structure
4. screen logic
5. backend-readiness
6. polish later

Do not jump to icons or visual polish before layout and structure are settled unless the task explicitly requests a visible UI alignment pass.

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

### 7.3 Content direction
The glossary content direction is:
- AI terms first
- supporting adjacent technical terms allowed when useful to explain AI concepts

Do not steer the content back toward a FOSS-focused glossary unless explicitly requested.

### 7.4 Out of scope for MVP
Do not add unless explicitly approved:
- accounts
- auth
- chat
- AI answer generation inside the app
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
- changed product wording that affects active implementation

Do not silently change architecture without reflecting it in docs.

For now:
- update only the docs explicitly treated as source-of-truth for active implementation
- do not perform broad repo-wide documentation cleanup unless explicitly requested

---

## 9. Commit and Diff Expectations

Preferred output from agents:
- focused diffs
- clear rationale
- no unrelated file churn
- meaningful PR-ready units

Good examples:
- implement one complete MVP flow
- align one architecture slice across multiple screens
- implement one backend slice
- standardize one UI system pass

Bad examples:
- rewrite multiple unrelated parts of the app at once
- add polish, features, and refactors in one step without a bounded task
- include unrelated cleanup in a feature PR

---

## 10. Coding Style Expectations

Agents should prefer:
- readable names
- focused classes/files
- minimal duplication
- explicit models
- small composables where practical
- predictable state flow

Avoid:
- overly clever abstractions
- unnecessary frameworks
- hidden magic behavior
- tight coupling between UI and data access

---

## 11. Escalation Rule

If a task would require changing approved scope, renaming internal project structure, or altering product direction beyond the current source-of-truth docs, the agent should not guess.
Instead, the task should be deferred until the project docs are updated.

---

## 12. Summary Rule

For AI-101:
- use AI agents heavily
- prefer larger bounded implementation bundles
- keep scope disciplined
- validate Android work in Android Studio
- validate backend work in the backend environment
- follow repo docs as source of truth
- keep internal names/paths unchanged unless explicitly told to rename them
