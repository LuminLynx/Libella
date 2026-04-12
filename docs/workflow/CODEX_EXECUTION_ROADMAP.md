# CODEX_EXECUTION_ROADMAP.md

## Purpose

This roadmap defines the major Codex-sized implementation milestones for the AI-101 project.

It is not the same as the product roadmap.
It exists to answer:

- what Codex should build
- in what order
- in milestone-sized bundles
- until the app MVP is finished

This roadmap assumes:
- product direction: **AI-101**
- glossary direction: **AI Terms Glossary**
- no repo / folder / file / package renames for now
- Android validation happens manually in Android Studio
- backend validation happens in its own runtime environment

---

## Phase 0 — Source-of-Truth Alignment
**Status:** Done

1. Update `docs/workflow/AGENTS.md`
2. Update `docs/roadmap/ROADMAP.md`
3. Update `docs/architecture/BACKEND_DATABASE_SCOPE.md`
4. Update `docs/workflow/TASKS.md`
5. Update `docs/workflow/GEMINI_AGENT_ROADMAP.md`

---

## Phase 1 — Complete the Android MVP
**Status:** In Progress

### Already completed
1. Home MVP flow
2. Browse Terms MVP flow
3. Categories MVP flow
4. Search MVP flow
5. Term Details MVP flow
6. Home alignment and theme wrapper
7. Shared repository provider
8. Lightweight ViewModel introduction for key MVP screens
9. Non-MVP entry points mostly removed from the active user path

### Remaining milestone work
10. Refresh mock glossary content to AI-first terms and categories
11. Complete Categories state / ViewModel alignment
12. Standardize loading, empty, and error states across active MVP screens
13. Finalize Settings screen structure for MVP
14. Verify non-MVP routes remain isolated from the active user flow
15. Keep all working MVP flows stable during the above changes

---

## Phase 2 — Prepare Android for Backend Integration
**Status:** Pending

16. Define Android remote DTO models for glossary content
17. Define Android API service interfaces for glossary endpoints
18. Prepare repository swap path between mock and API-backed data
19. Align active screen state for backend-ready loading / error handling
20. Keep current visible behavior stable while making Android remote-ready

---

## Phase 3 — Build the Backend MVP
**Status:** Pending

21. Create backend project skeleton
22. Create database schema for AI glossary categories
23. Create database schema for AI glossary terms
24. Create related-term schema if needed
25. Create backend seed data for AI glossary content
26. Implement terms list endpoint
27. Implement term details endpoint
28. Implement categories list endpoint
29. Implement category-filtered terms endpoint
30. Implement term search endpoint
31. Standardize backend response shapes
32. Standardize backend error handling

---

## Phase 4 — Connect Android to the Real Backend
**Status:** Pending

33. Add Android network client layer
34. Create `ApiGlossaryRepository`
35. Connect Browse flow to backend data
36. Connect Details flow to backend data
37. Connect Categories flow to backend data
38. Connect Search flow to backend data
39. Finalize loading and error behavior for remote data
40. Preserve mock fallback path if still useful for development

---

## Phase 5 — MVP Product Quality Pass
**Status:** Pending

41. Improve typography consistency
42. Improve spacing consistency
43. Improve card and button consistency
44. Add icon pass if useful
45. Improve accessibility basics
46. Review dark mode readiness
47. Review empty / loading / error state quality as product polish

---

## Phase 6 — Stabilization and Release Readiness
**Status:** Pending

48. Review navigation flow end-to-end
49. Review Browse flow end-to-end
50. Review Details flow end-to-end
51. Review Categories flow end-to-end
52. Review Search flow end-to-end
53. Review Settings flow end-to-end
54. Remove stale placeholder logic
55. Review docs for architecture drift
56. Prepare MVP release-ready state

---

## Recommended Task Size Rule

Codex tasks should usually be:
- one meaningful milestone
- one coherent PR
- broad enough to justify deep repo work
- bounded enough to review safely

Avoid returning to tiny patch tasks unless a specific blocker requires one.

---

## Immediate Next Milestone

**Complete the remaining Android MVP in one bundle**

That bundle should include:
- AI-first mock glossary content refresh
- Categories state / ViewModel alignment
- loading / empty / error state consistency
- Settings MVP finalization
- non-MVP isolation verification
- preservation of all currently working flows

---

## Milestone After That

**Prepare Android for backend integration in one bundle**

That bundle should include:
- remote DTO models
- API service interfaces
- clean repository swap path
- backend-ready screen state
- stable current visible behavior

---

## Milestone After That

**Build the full backend MVP in one bundle**

That bundle should include:
- backend skeleton
- schema
- seed data
- endpoints
- response shape
- error handling

---

## Summary

The next broad execution order is:

1. Complete remaining Android MVP
2. Prepare Android for backend integration
3. Build backend MVP
4. Connect Android to backend
5. Run MVP quality pass
6. Stabilize and prepare release-ready state
