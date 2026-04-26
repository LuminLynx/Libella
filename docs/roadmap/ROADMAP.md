# AI-101 Project Roadmap

## 1. Project Overview

### 1.1 Project name
**AI-101**

### 1.2 Product vision
Build a native Android glossary app that helps users learn AI terms in a clear, searchable, beginner-friendly way.

### 1.3 Target users
- Beginners learning AI, machine learning, and LLM concepts
- Students and self-learners
- Curious users who want simple explanations of AI terminology
- Developers who want quick reference material for AI-related terms

### 1.4 Core value proposition
The app should make it easy to:
- Browse AI terms
- Explore AI term categories
- Search for terms quickly
- Read short definitions and fuller explanations
- grow later into a richer AI-learning product if needed

---

## 2. Product Direction

### 2.1 Glossary direction
The glossary is now:
- **AI-first**
- focused on AI, ML, LLM, data, model, inference, and related concepts

Supporting adjacent technical terms are allowed when useful to explain AI concepts clearly.

### 2.2 Naming rule for now
The product direction and visible app wording should use:
- **AI-101**
- **AI Terms Glossary**

For now, the project does **not** need to rename:
- repository name
- folder names
- file names
- Kotlin package names

Those internal names can stay unchanged unless explicitly requested later.

---

## 3. Project Principles

These principles guide the order of work:

1. **Build foundation before polish**
2. **Keep scope disciplined**
3. **Use reusable components instead of one-off UI**
4. **Prefer clean architecture that supports backend integration**
5. **Use larger bounded implementation bundles for agents**
6. **Keep Android validation in Android Studio**
7. **Delay broad cleanup or renaming unless it directly helps app progress**

---

## 4. Version Scope

### 4.1 MVP features
These are the features that belong in the first usable version:

1. Home screen
2. Browse Terms screen
3. Categories screen
4. Search screen
5. Term Details screen
6. Settings screen
7. Backend API
8. Database-backed glossary content

### 4.2 Extension features (approved, post-core)
These features are approved and either already implemented or planned. They extend the core glossary experience and should not be blocked once the MVP core is stable:

1. AI Learning Layer — Ask Glossary, AI Scenario, AI Challenge (implemented)
2. Term Draft contribution flow — users can submit missing terms for review (implemented)
3. Trend Watcher — AI Terms Trend of the week (placeholder in place, to be implemented)

### 4.3 Explicitly deferred features
These are not planned for the current development cycle and should not be added unless explicitly approved:

1. Favorites / bookmarks
2. History / recently viewed
3. User accounts
4. Admin panel

---

## 5. Current Android State

The Android app already has a strong MVP foundation in place.

### 5.1 Implemented and validated
The following are already working and validated on device:

- Home
- Browse Terms
- Categories
- Search
- Term Details
- Settings access
- Theme wrapper
- Shared repository provider
- Lightweight ViewModel introduction for key MVP flows

### 5.2 Current app architecture direction
The Android app should continue moving toward:

- `GlossaryRepository` as the abstraction
- `MockGlossaryRepository` for temporary development
- `ApiGlossaryRepository` for real backend integration
- ViewModel-managed UI state where it meaningfully helps
- centralized navigation
- reusable UI components
- backend-ready screen state

---

## 6. Recommended Build Order From This Point

The project should now be executed in this order:

1. Update active source-of-truth docs to AI-101 / AI Terms Glossary
2. Refresh mock/sample glossary content toward AI terms
3. Complete remaining Android MVP alignment
4. Prepare Android for backend integration
5. Build backend MVP
6. Connect Android to real backend data
7. Polish MVP usability and consistency
8. Stabilize and validate end-to-end

---

## 7. Roadmap by Phase

## Phase 1 — Source-of-Truth Alignment

### Goal
Make sure all active project docs and coding instructions point to:
- AI-101
- AI Terms Glossary
- the current app direction

### Required outcome
Codex and other agents should stop receiving mixed signals about:
- app naming
- glossary scope
- current priorities

---

## Phase 2 — Android MVP Completion

### Goal
Finish the Android-side MVP in a clean, backend-ready way.

### Required outcome
The Android app should have:
- stable MVP flows
- consistent state handling
- clean repository usage
- consistent reusable UI patterns
- no major architecture drift

### Key work
1. complete remaining ViewModel/state alignment where useful
2. standardize loading / empty / error states
3. keep Home, Browse, Categories, Search, Details, and Settings consistent
4. refresh mock glossary content so it reflects AI terms instead of the earlier mixed/FOSS direction

---

## Phase 3 — Backend MVP

### Goal
Build the backend and database required for the glossary core.

### Required backend capabilities
1. list terms
2. return term details
3. list categories
4. filter by category
5. search terms

### Required database capabilities
1. store AI glossary terms
2. store categories
3. store related-term relationships if used
4. support the backend as the system of record

---

## Phase 4 — Android Remote Integration

### Goal
Connect the Android app to the real backend without changing the core UX unexpectedly.

### Required outcome
- Android reads real glossary data from backend APIs
- repository swap path is clean
- Browse, Categories, Search, and Details work end-to-end with backend data
- loading and error states behave predictably

---

## Phase 5 — MVP Quality Pass

### Goal
Improve usability and finish the MVP as a coherent product.

### Focus areas
- typography consistency
- spacing consistency
- button/card consistency
- empty/loading/error state quality
- accessibility basics
- dark mode readiness
- icon pass if useful

---

## Phase 6 — Stabilization

### Goal
Make the MVP release-ready.

### Required outcome
- app builds cleanly
- backend works cleanly
- glossary flows work end-to-end
- docs match implementation
- no stale placeholder logic remains in the active user flow

---

## 8. Immediate Execution Priority

The highest-priority work from this point is:

1. update source-of-truth docs for AI-101 / AI Terms Glossary
2. refresh mock glossary dataset and categories to AI terms
3. finish Android MVP consistency work where still needed
4. begin backend MVP implementation

---

## 9. Definition of Done for MVP

The MVP is considered done when all of the following are true:

1. The app has working Home, Browse, Categories, Search, Details, and Settings flows
2. The glossary content direction is AI-first
3. The backend can serve terms, details, categories, category filtering, and search
4. The database stores the glossary content correctly
5. Android consumes backend data successfully
6. The UI is consistent and usable
7. The project builds and runs cleanly
8. The repository and source-of-truth docs are aligned

---

## 10. Current Recommended Next Step

**Next step:**
Finish updating the active source-of-truth docs for AI-101 / AI Terms Glossary, then move directly into the next large implementation bundle instead of returning to micro-tasks.
