# FOSS-101 Backend and Database Scope

## 1. Purpose

This document defines the approved MVP scope for the backend and database of the FOSS-101 project.

It exists to make sure the Android app, backend, database, and AI agents all work from the same assumptions.

---

## 2. Approved MVP Decisions

The following decisions are approved for the MVP:

1. **No user accounts**
2. **No chat / AI tools**
3. **No admin panel**
4. **Online-first app**
5. **Content managed manually at first**
6. **Backend and database are required parts of the system**

These decisions reduce scope and keep the MVP focused on the glossary core.

---

## 3. System Shape

FOSS-101 is a **client + backend + database** application.

### 3.1 Android client responsibilities
The Android app is responsible for:
- presenting the UI
- navigation between screens
- user interaction
- displaying glossary content
- calling backend APIs
- managing UI state

### 3.2 Backend responsibilities
The backend is responsible for:
- serving glossary terms
- serving term details
- serving categories
- supporting search
- supporting category filtering
- exposing stable API responses to the Android app

### 3.3 Database responsibilities
The database is responsible for:
- storing glossary terms
- storing categories
- storing glossary relationships if needed
- serving as the system of record for glossary content

---

## 4. MVP Feature Coverage

The backend and database must support these MVP Android features:

1. Home
2. Browse Terms
3. Categories
4. Search
5. Term Details
6. Settings

The backend/database are directly required for:
- Browse Terms
- Categories
- Search
- Term Details

---

## 5. Backend MVP Scope

The backend should support these use cases in the MVP:

1. Return the glossary term list
2. Return a single glossary term by ID
3. Return the category list
4. Return terms filtered by category
5. Return terms filtered by search query

### 5.1 Out of scope for MVP
The backend should **not** include these in MVP:
- authentication
- user profiles
- bookmarks/favorites sync
- chat
- AI-generated answers
- trend ingestion
- admin dashboard
- analytics platform features unless explicitly added later

---

## 6. Recommended API Shape

The MVP backend should expose a simple REST API.

### 6.1 Suggested endpoints

- `GET /terms`
- `GET /terms/{id}`
- `GET /categories`

### 6.2 Suggested query support on `/terms`

- `GET /terms?categoryId={id}`
- `GET /terms?search={query}`

### 6.3 API behavior expectations

- stable JSON responses
- consistent field names
- predictable empty-result behavior
- clear not-found responses
- clear error responses

---

## 7. Database MVP Scope

The database should store glossary content and category structure.

### 7.1 Terms
Each glossary term should support at least:

- `id`
- `term`
- `short_definition`
- `full_explanation`
- `category_id`
- `tags`
- `example_usage` (optional)
- `source` (optional)
- `created_at`
- `updated_at`

### 7.2 Categories
Each category should support at least:

- `id`
- `name`
- `description`

### 7.3 Optional related terms
This may be included now or later:

- `term_id`
- `related_term_id`

This is useful if the app later shows related terms on the details screen.

---

## 8. Search Decision

For the MVP, search is **backend-driven**.

### Why
- the database remains the source of truth
- Android logic stays simpler
- search behavior stays centralized
- future scaling is easier

The Android app should send the query and display the returned results.

---

## 9. Offline Strategy

The MVP is **online-first**.

### Meaning
- the app expects backend connectivity
- full offline mode is not required in MVP
- local caching can be added later if needed

This keeps the initial system simpler and aligned with the approved scope.

---

## 10. Content Management Strategy

For the MVP, glossary content will be managed manually.

### Meaning
- no admin panel in MVP
- no content editor UI in MVP
- database content can be inserted/updated through scripts, seeding, or backend tooling

This is the fastest path for the first version.

---

## 11. Recommended Database Direction

A relational database is the recommended default for this project.

### Why
- terms and categories are structured
- relationships are clear
- search and filtering fit well
- the data model is not highly irregular

A concrete database choice can be recorded separately once selected.

---

## 12. Android Integration Strategy

The Android app should not depend directly on raw mock data forever.

### 12.1 Recommended abstraction
Use a repository interface such as:

- `GlossaryRepository`

### 12.2 Recommended transition path
1. Build UI using `MockGlossaryRepository`
2. Build backend and database in parallel
3. Replace or supplement with `ApiGlossaryRepository`
4. Validate end-to-end behavior

This allows UI work to move forward without waiting for the backend to be complete.

---

## 13. Definition of MVP Success for Backend and Database

The backend/database portion of the MVP is successful when:

1. the backend can return glossary terms
2. the backend can return term details
3. the backend can return categories
4. the backend can filter by category
5. the backend can search terms
6. the database stores glossary content correctly
7. the Android app can consume the backend successfully

---

## 14. Next Documents to Create

After this document, the next repo documents should be:

1. `AGENTS.md`
2. `TASKS.md`

These should use this scope definition as a source of truth.

---

## 15. Approved Summary

The approved MVP system is:

- Android client
- backend API
- database-backed glossary content
- online-first
- no user accounts
- no chat / AI tools
- no admin panel
- manual content management initially

This scope should guide the roadmap, implementation tasks, and AI-agent instructions.
