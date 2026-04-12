# TASKS.md

## 1. Purpose

This file lists the current next implementation tasks for AI-101.

These tasks are intentionally organized as **larger bounded implementation bundles** suitable for AI-agent execution followed by manual validation.

---

## 2. Current Priority

The current priority is:

1. keep the Android app moving
2. keep source-of-truth docs aligned
3. avoid mixed signals about product direction
4. finish the app logically in milestone-sized bundles
5. prepare for backend integration and real data

Do not return to trivial micro-tasks unless a specific blocker requires it.

---

## 3. Current Product Direction

The product direction is now:

- **AI-101**
- **AI Terms Glossary**

Important:
- visible app wording and content direction should follow AI-101 / AI Terms Glossary
- supporting adjacent technical terms are allowed when helpful
- internal folder names, file names, repository name, and package names do not need renaming for now

---

## 4. Current Implementation State

Already completed and validated:
- Home
- Browse Terms
- Categories
- Search
- Term Details
- Settings access
- theme wrapper
- shared repository provider
- lightweight ViewModel introduction for key MVP flows

That means the next tasks should focus on:
- source-of-truth alignment
- AI-focused content direction
- backend preparation
- backend integration
- MVP quality and stabilization

---

## 5. Active Task Bundles

### Task 01 — Align source-of-truth docs to AI-101
**Goal:** update the active project docs so Codex and other agents build toward AI-101 / AI Terms Glossary.

**Required files:**
- `docs/roadmap/ROADMAP.md`
- `docs/architecture/BACKEND_DATABASE_SCOPE.md`
- `docs/workflow/AGENTS.md`
- `docs/workflow/TASKS.md`
- `docs/workflow/GEMINI_AGENT_ROADMAP.md`

**Expected result:**
- no mixed project direction
- AI-101 is the visible app direction
- AI Terms Glossary is the active glossary scope

---

### Task 02 — Refresh mock glossary content for AI terms
**Goal:** replace the current sample/mock glossary direction with AI-focused terms and categories.

**Expected result:**
- AI-first mock dataset
- category set aligned to AI learning
- term content good enough for Browse, Search, Categories, and Details
- current MVP flows still work

**Notes:**
- this is not a database task yet
- this is the local/mock content baseline used by the Android app

---

### Task 03 — Complete MVP Android consistency alignment
**Goal:** finish remaining internal consistency work across active MVP screens without changing the visible product unexpectedly.

**Expected result:**
- state handling is consistent across active MVP flows
- loading / empty / error states are reasonable
- no obvious architecture drift remains in active screens
- user-visible behavior remains stable unless explicitly requested

---

### Task 04 — Prepare Android for backend integration
**Goal:** make the Android app fully ready to swap from mock data to backend data.

**Expected result:**
- remote DTO model plan or implementation
- API service interface plan or implementation
- repository swap path is clean
- active MVP flows are backend-ready

**Notes:**
- this task should stay focused on Android-side preparation
- backend implementation itself belongs to the next task bundle

---

### Task 05 — Build backend MVP
**Goal:** implement the backend and database required for the glossary core.

**Expected backend capabilities:**
- list terms
- return term details
- list categories
- filter by category
- search terms

**Expected result:**
- backend skeleton
- database schema
- seed data
- endpoints
- stable response shape
- stable error handling

---

### Task 06 — Connect Android to the backend
**Goal:** switch active MVP flows from mock-first to backend-driven data while preserving app usability.

**Expected result:**
- `ApiGlossaryRepository`
- Browse connected to backend
- Details connected to backend
- Categories connected to backend
- Search connected to backend
- loading/error behavior works end-to-end

---

### Task 07 — MVP product quality pass
**Goal:** make the app feel coherent and release-worthy.

**Focus areas:**
- typography consistency
- spacing consistency
- card/button consistency
- icon pass if useful
- accessibility basics
- dark mode review
- empty/loading/error quality

**Expected result:**
- a cleaner, more consistent MVP product

---

### Task 08 — MVP stabilization
**Goal:** clean and validate the product end-to-end.

**Expected result:**
- no stale placeholder logic in active flows
- docs match implementation
- app builds cleanly
- backend works cleanly
- glossary flows work end-to-end
- repo is in a clean state

---

## 6. Recommended Execution Order

The preferred order from this point is:

1. Task 01 — Align source-of-truth docs to AI-101
2. Task 02 — Refresh mock glossary content for AI terms
3. Task 03 — Complete MVP Android consistency alignment
4. Task 04 — Prepare Android for backend integration
5. Task 05 — Build backend MVP
6. Task 06 — Connect Android to the backend
7. Task 07 — MVP product quality pass
8. Task 08 — MVP stabilization

---

## 7. Agent Execution Rules

When an AI agent works on a task in this file, it should:
- execute one **bounded implementation bundle** at a time
- keep the diff focused
- avoid unrelated changes
- avoid out-of-scope features
- leave validation to the correct environment

For Android tasks:
- validate in Android Studio

For backend tasks:
- validate in the backend runtime

For Codex:
- do not run Gradle commands in this repository

---

## 8. Not Current Priorities

These are explicitly not current priorities:
- user accounts
- admin panel
- chat inside the app
- AI answer generation inside the app
- trend features
- large-scale repo renaming from FOSS to AI
- broad documentation cleanup outside source-of-truth docs
- speculative refactors without milestone value

---

## 9. Completion Rule

When a task bundle is finished and validated, it should either:
- be marked complete in this file, or
- be replaced by the next higher-priority active bundle

Until then, treat all task bundles above as active roadmap items.
