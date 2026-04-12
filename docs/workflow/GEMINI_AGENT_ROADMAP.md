# GEMINI_AGENT_ROADMAP.md

## Purpose

This document is the execution roadmap for Gemini Agent work on AI-101.

It is derived from:
- `docs/roadmap/ROADMAP.md`
- `docs/architecture/BACKEND_DATABASE_SCOPE.md`
- `docs/workflow/AGENTS.md`
- `docs/workflow/TASKS.md`

It contains:
- agent-executable task bundles
- recommended order
- rough time estimates
- validation checkpoints

It does not replace the main project roadmap.

---

## Status Legend

- Pending
- In Progress
- Done
- Blocked

---

## Phase 1 — Source-of-Truth Alignment

1. Align active project docs to AI-101 / AI Terms Glossary — 30 to 60 min — In Progress
2. Confirm no mixed product wording remains in source-of-truth docs — 15 to 30 min — Pending
3. Confirm no folder/file/package renames are required for now — 10 to 20 min — Pending

### Validation checkpoint
- active docs consistently say AI-101
- active docs consistently say AI Terms Glossary
- internal technical names remain unchanged unless explicitly requested

---

## Phase 2 — Android MVP Completion

4. Refresh mock glossary content to AI-first sample data — 30 to 90 min — Pending
5. Complete MVP screen consistency across Home, Browse, Categories, Search, and Details — 45 to 120 min — Pending
6. Finish Categories state/ViewModel alignment if still needed — 30 to 90 min — Pending
7. Standardize loading, empty, and error state patterns across active MVP flows — 45 to 120 min — Pending
8. Finalize Settings screen structure for MVP — 20 to 60 min — Pending

### Validation checkpoint
- active MVP screens compile
- active MVP screens remain stable on device
- mock content direction is AI-first
- no non-MVP features are pulled into the active user flow

---

## Phase 3 — Android Backend Preparation

9. Define Android remote DTO models for glossary content — 30 to 60 min — Pending
10. Define Android API service interfaces for glossary endpoints — 30 to 60 min — Pending
11. Prepare repository swap path between mock and API-backed data — 30 to 60 min — Pending
12. Align active screen state for backend-ready loading/error handling — 45 to 90 min — Pending

### Validation checkpoint
- Android code remains compile-safe
- repository abstraction stays clean
- app is clearly prepared for backend integration

---

## Phase 4 — Backend MVP

13. Create backend project skeleton — 45 to 120 min — Pending
14. Create database schema for AI glossary categories — 30 to 60 min — Pending
15. Create database schema for AI glossary terms — 30 to 60 min — Pending
16. Create related-term schema if needed — 20 to 40 min — Pending
17. Create backend seed data for AI glossary content — 30 to 90 min — Pending
18. Implement terms list endpoint — 30 to 60 min — Pending
19. Implement term details endpoint — 30 to 60 min — Pending
20. Implement categories list endpoint — 20 to 40 min — Pending
21. Implement category-filtered terms endpoint — 30 to 60 min — Pending
22. Implement term search endpoint — 30 to 60 min — Pending
23. Standardize backend response shapes — 20 to 40 min — Pending
24. Standardize backend error handling — 20 to 40 min — Pending

### Validation checkpoint
- backend runs in its own runtime
- API shape matches the approved glossary scope
- seed data is usable by Android integration

---

## Phase 5 — Android Remote Integration

25. Add Android network client layer — 30 to 90 min — Pending
26. Create `ApiGlossaryRepository` — 30 to 60 min — Pending
27. Connect Browse flow to backend data — 30 to 60 min — Pending
28. Connect Details flow to backend data — 30 to 60 min — Pending
29. Connect Categories flow to backend data — 30 to 60 min — Pending
30. Connect Search flow to backend data — 30 to 60 min — Pending
31. Finalize loading and error behavior for remote data — 30 to 60 min — Pending
32. Preserve mock fallback path if still useful — 20 to 40 min — Pending

### Validation checkpoint
- Android builds with remote integration
- Browse, Categories, Search, and Details work end-to-end with backend data
- loading and error behavior is stable

---

## Phase 6 — MVP Quality Pass

33. Improve typography consistency across active MVP screens — 20 to 40 min — Pending
34. Improve spacing consistency across active MVP screens — 20 to 40 min — Pending
35. Improve card and button consistency — 20 to 40 min — Pending
36. Add icon pass if useful to the MVP — 30 to 60 min — Pending
37. Improve accessibility basics — 30 to 60 min — Pending
38. Review dark mode readiness — 20 to 40 min — Pending
39. Review empty/loading/error states as a product-quality pass — 20 to 40 min — Pending

### Validation checkpoint
- app looks coherent
- accessibility is improved
- no polish work expands scope beyond the glossary MVP

---

## Phase 7 — Stabilization and Release Readiness

40. Review navigation flow end-to-end — 20 to 40 min — Pending
41. Review Browse flow end-to-end — 20 to 40 min — Pending
42. Review Details flow end-to-end — 20 to 40 min — Pending
43. Review Categories flow end-to-end — 20 to 40 min — Pending
44. Review Search flow end-to-end — 20 to 40 min — Pending
45. Review Settings flow end-to-end — 15 to 30 min — Pending
46. Remove stale placeholder logic from active flows — 20 to 40 min — Pending
47. Review docs for architecture drift — 20 to 40 min — Pending
48. Prepare MVP release-ready state — 30 to 60 min — Pending

### Validation checkpoint
- app builds cleanly
- glossary flows work end-to-end
- backend works cleanly
- docs match implementation
- repo is clean and release-ready

---

## Phase 8 — Recurring Validation Rules

These are recurring operational tasks and should continue throughout the roadmap.

49. Review agent diff before accepting — 10 to 20 min — Recurring
50. Sync project after meaningful Android changes — 5 to 15 min — Recurring
51. Build selected run configuration after meaningful Android changes — 5 to 15 min — Recurring
52. Resolve compile or sync errors introduced by agent changes — 10 to 60 min — Recurring
53. Commit validated changes in focused units — 10 to 20 min — Recurring
54. Pull with rebase before push when needed — 5 to 15 min — Recurring
55. Push validated changes to GitHub — 5 to 10 min — Recurring

---

## Current Immediate Next Bundles

1. Finish source-of-truth AI-101 alignment
2. Refresh mock glossary content to AI terms
3. Complete remaining Android MVP consistency work
4. Begin backend MVP implementation

---

## Operating Rule

Gemini Agent should be used for:
- bounded implementation bundles
- multi-file milestone work
- repetitive UI/component work
- repository and backend-readiness work
- backend scaffolding
- doc-aligned refactors

Human review should remain responsible for:
- scope decisions
- Android Studio validation
- backend runtime validation
- Git acceptance and push decisions
- architecture corrections
