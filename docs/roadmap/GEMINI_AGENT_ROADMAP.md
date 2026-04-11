# GEMINI_AGENT_ROADMAP.md

## Purpose

This document is the execution roadmap for Gemini Agent work on FOSS-101.

It is derived from:
- `docs/roadmap/ROADMAP.md`
- `docs/architecture/BACKEND_DATABASE_SCOPE.md`
- `docs/workflow/AGENTS.md`
- `docs/workflow/TASKS.md`

It contains:
- agent-executable task titles
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

## Phase 1 — Documentation and Scope Alignment

1. Review roadmap, backend scope, agents, and tasks documents — 15 to 30 min — Pending
2. Confirm MVP scope against current codebase — 15 to 30 min — Pending
3. Confirm non-MVP features remain out of implementation scope — 10 to 20 min — Pending
4. Organize documentation under `docs/` and verify final file locations — 15 to 30 min — Pending
5. Record current architecture assumptions in repo docs if needed — 15 to 30 min — Pending

### Validation checkpoint
- Docs are present in the correct folders
- Scope matches approved MVP decisions
- No out-of-scope work is being planned

---

## Phase 2 — Backend and Database Definition

6. Confirm backend MVP responsibilities — 15 to 30 min — Pending
7. Confirm database MVP responsibilities — 15 to 30 min — Pending
8. Confirm online-first MVP strategy — 10 to 20 min — Pending
9. Confirm no accounts/chat/admin in MVP — 10 to 20 min — Pending
10. Draft MVP API endpoint list — 30 to 60 min — Pending
11. Draft API response-shape baseline — 30 to 60 min — Pending
12. Draft error response baseline — 20 to 40 min — Pending
13. Draft database schema baseline for terms and categories — 30 to 60 min — Pending
14. Draft related-terms storage approach — 20 to 40 min — Pending
15. Record backend stack decision once chosen — 10 to 20 min — Pending

### Validation checkpoint
- Backend scope matches approved MVP
- Database scope matches approved MVP
- API baseline is documented
- Schema baseline is documented

---

## Phase 3 — Android Architecture Foundation

16. Review current Android package structure — 15 to 30 min — Done
17. Create missing architecture packages — 10 to 20 min — Done
18. Define glossary domain models — 15 to 30 min — Done
19. Create glossary repository abstraction — 15 to 30 min — Done
20. Create mock glossary repository — 30 to 60 min — Done
21. Expand mock glossary dataset to MVP-safe sample size — 30 to 60 min — In Progress
22. Review and normalize navigation routes — 20 to 40 min — In Progress
23. Scaffold reusable UI components baseline — 30 to 60 min — In Progress
24. Review theme foundation — 20 to 40 min — In Progress
25. Review dependency foundation — 20 to 40 min — Done
26. Add missing ViewModel-related dependency only when needed — 10 to 20 min — Pending
27. Create app theme package and wrapper if missing — 30 to 60 min — Pending

### Validation checkpoint
- Project syncs successfully
- Selected run configuration builds successfully
- New files compile without red errors
- Architecture packages remain clean and focused

---

## Phase 4 — Mock-First MVP Screen Flows

28. Implement Browse Terms screen with mock repository data — 45 to 90 min — Pending
29. Implement term list item presentation for Browse Terms — 30 to 60 min — Pending
30. Implement click navigation from Browse Terms to term details — 20 to 40 min — Pending
31. Implement Term Details screen using `termId` route argument — 45 to 90 min — Pending
32. Load selected term from repository in Term Details screen — 30 to 60 min — Pending
33. Add basic empty/missing-term state in Term Details screen — 20 to 40 min — Pending
34. Implement Categories screen with mock repository data — 45 to 90 min — Pending
35. Implement category selection flow — 30 to 60 min — Pending
36. Implement filtered terms by category flow — 45 to 90 min — Pending
37. Implement Search screen with mock repository data — 45 to 90 min — Pending
38. Implement search query handling — 30 to 60 min — Pending
39. Implement search results state — 30 to 60 min — Pending
40. Improve Home screen for MVP navigation only — 30 to 60 min — Pending
41. Review Settings screen for MVP readiness — 20 to 40 min — Pending

### Validation checkpoint
- Home, Browse, Categories, Search, Details, and Settings screens compile
- Browse, Search, Categories, and Details work with mock data
- Navigation to `details/{termId}` works
- No non-MVP features are expanded

---

## Phase 5 — Reusable UI Expansion

42. Extract reusable glossary list item component — 30 to 60 min — Pending
43. Extract reusable category card component — 30 to 60 min — Pending
44. Extract reusable search bar component — 20 to 40 min — Pending
45. Add empty state component — 20 to 40 min — Pending
46. Add loading state component — 20 to 40 min — Pending
47. Align MVP screens with reusable components — 45 to 90 min — Pending
48. Normalize spacing and section header usage across MVP screens — 30 to 60 min — Pending

### Validation checkpoint
- Components are reusable and compile-safe
- Screen code becomes simpler, not more complex
- No unnecessary abstraction is introduced

---

## Phase 6 — Layout and Theme Alignment

49. Plan Home screen layout against MVP scope — 20 to 40 min — Pending
50. Plan Browse Terms layout against real content needs — 20 to 40 min — Pending
51. Plan Categories layout against filtering flow — 20 to 40 min — Pending
52. Plan Search layout against results flow — 20 to 40 min — Pending
53. Plan Term Details layout against repository-driven data — 20 to 40 min — Pending
54. Create custom app theme package if still missing — 30 to 60 min — Pending
55. Wrap app content with custom theme in `MainActivity` — 15 to 30 min — Pending
56. Align typography and spacing with Material 3 foundation — 30 to 60 min — Pending

### Validation checkpoint
- Theme wrapper exists and compiles
- MVP screens still build after theme introduction
- Layout changes remain within MVP scope

---

## Phase 7 — Backend Project Preparation

57. Create backend project skeleton — 45 to 90 min — Pending
58. Create database schema for categories — 30 to 60 min — Pending
59. Create database schema for terms — 30 to 60 min — Pending
60. Create related-terms schema if included in MVP — 20 to 40 min — Pending
61. Create seed data for categories — 20 to 40 min — Pending
62. Create seed data for glossary terms — 30 to 60 min — Pending
63. Add backend route for listing terms — 30 to 60 min — Pending
64. Add backend route for term details — 30 to 60 min — Pending
65. Add backend route for categories — 20 to 40 min — Pending
66. Add backend route for category filtering — 30 to 60 min — Pending
67. Add backend route for term search — 30 to 60 min — Pending
68. Standardize backend response shapes — 20 to 40 min — Pending
69. Standardize backend error responses — 20 to 40 min — Pending

### Validation checkpoint
- Backend runs in its own environment
- Endpoints respond with agreed shapes
- Seed data is usable for Android integration

---

## Phase 8 — Android Remote Integration

70. Add Android remote data models if needed — 30 to 60 min — Pending
71. Add API client layer — 45 to 90 min — Pending
72. Scaffold remote service interface — 30 to 60 min — Pending
73. Create `ApiGlossaryRepository` — 30 to 60 min — Pending
74. Connect Browse Terms screen to repository abstraction cleanly — 30 to 60 min — Pending
75. Connect Term Details screen to repository abstraction cleanly — 30 to 60 min — Pending
76. Connect Categories screen to repository abstraction cleanly — 30 to 60 min — Pending
77. Connect Search screen to repository abstraction cleanly — 30 to 60 min — Pending
78. Add loading states for remote data — 30 to 60 min — Pending
79. Add error states for remote data — 30 to 60 min — Pending
80. Preserve mock repository path for development/testing if useful — 20 to 40 min — Pending

### Validation checkpoint
- Android builds with remote integration
- App can read real backend data
- Browse, Search, Categories, and Details work end-to-end

---

## Phase 9 — ViewModel and State Cleanup

81. Add ViewModel support to Browse Terms flow — 30 to 60 min — Pending
82. Add ViewModel support to Term Details flow — 30 to 60 min — Pending
83. Add ViewModel support to Categories flow — 30 to 60 min — Pending
84. Add ViewModel support to Search flow — 30 to 60 min — Pending
85. Move UI state out of composables where appropriate — 30 to 60 min — Pending
86. Normalize state handling across MVP screens — 30 to 60 min — Pending

### Validation checkpoint
- UI state is clearer and less coupled
- No unnecessary ViewModel complexity is introduced
- Project still builds cleanly

---

## Phase 10 — MVP Polish

87. Add icons to MVP screens — 30 to 60 min — Pending
88. Improve typography consistency — 20 to 40 min — Pending
89. Improve spacing consistency — 20 to 40 min — Pending
90. Improve button and card consistency — 20 to 40 min — Pending
91. Add accessibility labels/content descriptions — 30 to 60 min — Pending
92. Improve touch target consistency — 20 to 40 min — Pending
93. Review dark mode readiness — 20 to 40 min — Pending
94. Review empty states across MVP screens — 20 to 40 min — Pending
95. Review loading states across MVP screens — 20 to 40 min — Pending

### Validation checkpoint
- MVP screens look consistent
- Accessibility is improved
- No polish work expands scope beyond MVP

---

## Phase 11 — Stabilization and Validation

96. Review navigation flow end-to-end — 20 to 40 min — Pending
97. Review Browse Terms flow end-to-end — 20 to 40 min — Pending
98. Review Term Details flow end-to-end — 20 to 40 min — Pending
99. Review Categories flow end-to-end — 20 to 40 min — Pending
100. Review Search flow end-to-end — 20 to 40 min — Pending
101. Review Settings flow end-to-end — 15 to 30 min — Pending
102. Fix compile-safe architecture issues — 30 to 60 min — Pending
103. Fix minor UI consistency issues — 30 to 60 min — Pending
104. Clean stale or placeholder code left from scaffolding — 20 to 40 min — Pending
105. Review docs for architecture drift — 20 to 40 min — Pending
106. Update task status documents — 15 to 30 min — Pending
107. Prepare MVP release-candidate state — 30 to 60 min — Pending

### Validation checkpoint
- App builds cleanly
- End-to-end MVP flows work
- Docs still match implementation
- Repo is clean and commit-ready

---

## Phase 12 — Git and Validation Checkpoints

These are recurring operational tasks and should happen throughout the roadmap, not only at the end.

108. Review agent diff before accepting — 10 to 20 min — Recurring
109. Sync project after meaningful Android changes — 5 to 15 min — Recurring
110. Build selected run configuration after meaningful Android changes — 5 to 15 min — Recurring
111. Resolve compile or sync errors introduced by agent changes — 10 to 60 min — Recurring
112. Commit validated changes in focused units — 10 to 20 min — Recurring
113. Pull with rebase before push when needed — 5 to 15 min — Recurring
114. Push validated changes to GitHub — 5 to 10 min — Recurring

---

## Current Immediate Next Tasks

1. Finish expanding mock glossary dataset
2. Implement Browse Terms screen with mock data
3. Implement term click navigation to details
4. Implement Term Details screen using `termId`
5. Continue with Categories and Search mock-first flows

---

## Operating Rule

Gemini Agent should be used for:
- bounded implementation tasks
- scaffold generation
- repetitive UI/component work
- repository/integration work
- backend scaffolding
- doc-aligned refactors

Human review should remain responsible for:
- scope decisions
- Android Studio validation
- backend runtime validation
- Git acceptance and push decisions
- architecture corrections
