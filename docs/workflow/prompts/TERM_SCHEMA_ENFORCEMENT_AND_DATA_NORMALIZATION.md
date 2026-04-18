# AI-101 — Term Schema Enforcement and Data Normalization

## Milestone
Term Schema Enforcement and Data Normalization.

## Task Type
Large implementation pass.

This is a backend/data integrity milestone for AI-101.

## Objective
Make the database-backed glossary conform to the intended canonical term format so the app only receives clean, normalized, display-ready term data.

## Product Context
- App name: AI-101
- Product: AI Terms Glossary
- Source of truth is backend/database, not YAML
- The app is currently displaying terms that do not conform to the intended canonical term format
- The intended term structure includes:
  - slug
  - term
  - definition
  - explanation
  - humor
  - see_also
  - tags
  - controversy_level
- The app should only receive normalized, display-ready term data from the backend

## Working Style Requirements
- Do not use MVP framing
- Do not propose tiny cleanup tasks
- Use large capability-based implementation
- Prioritize production readiness
- Optimize for data integrity and merge-ready product quality

## Mission
Enforce a canonical term schema across database, backend validation, backend serialization, and content lifecycle flows so malformed term content no longer leaks into the app.

## Required Outcomes
1. Audit the current database-backed term representation against the intended canonical format
2. Fix backend serialization so API responses are consistent and display-ready
3. Add or strengthen backend validation for term writes/updates/imports
4. Normalize malformed persisted data where needed
5. Ensure tags are normalized consistently
6. Ensure related-term / see_also relationships are valid and safe
7. Ensure controversy_level is constrained correctly
8. Prevent future malformed term writes from entering the DB
9. Keep Android as a pure consumer of already-clean backend data
10. Avoid unrelated architecture churn

## Execution Rules
- Treat this as a backend/data integrity milestone
- Do not solve malformed data in the Android client
- Prefer canonical server-side normalization and validation
- Include any necessary migration/data-fix logic
- Preserve working app flows while improving data quality

## Deliverables
1. Backend/data-layer schema enforcement implementation
2. Data normalization/migration changes if needed
3. Updated API behavior returning normalized term data
4. Summary of files changed
5. Remaining risks, if any
6. Merge readiness assessment

## Important
This milestone exists to make the database-backed product honor the intended glossary term contract everywhere.

## Codex Prompt
```text
Implement the next major milestone for AI-101: Term Schema Enforcement and Data Normalization.

Project context:
- App name: AI-101
- Product: AI Terms Glossary
- Source of truth is backend/database, not YAML
- The app is currently displaying terms that do not conform to the intended canonical term format
- The intended term structure includes:
  - slug
  - term
  - definition
  - explanation
  - humor
  - see_also
  - tags
  - controversy_level
- The app should only receive normalized, display-ready term data from the backend

Working style requirements:
- Do not use MVP framing
- Do not propose tiny cleanup tasks
- Use large capability-based implementation
- Prioritize production readiness
- Optimize for data integrity and merge-ready product quality

Your mission:
Enforce a canonical term schema across database, backend validation, backend serialization, and content lifecycle flows so malformed term content no longer leaks into the app.

Required outcomes:
1. Audit the current database-backed term representation against the intended canonical format
2. Fix backend serialization so API responses are consistent and display-ready
3. Add or strengthen backend validation for term writes/updates/imports
4. Normalize malformed persisted data where needed
5. Ensure tags are normalized consistently
6. Ensure related-term / see_also relationships are valid and safe
7. Ensure controversy_level is constrained correctly
8. Prevent future malformed term writes from entering the DB
9. Keep Android as a pure consumer of already-clean backend data
10. Avoid unrelated architecture churn

Execution rules:
- Treat this as a backend/data integrity milestone
- Do not solve malformed data in the Android client
- Prefer canonical server-side normalization and validation
- Include any necessary migration/data-fix logic
- Preserve working app flows while improving data quality

Deliverables:
1. Backend/data-layer schema enforcement implementation
2. Data normalization/migration changes if needed
3. Updated API behavior returning normalized term data
4. Summary of files changed
5. Remaining risks, if any
6. Merge readiness assessment

Important:
This milestone exists to make the database-backed product honor the intended glossary term contract everywhere.
```
