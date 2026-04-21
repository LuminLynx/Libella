# AI-101 — Missing Search Logging and Draft Term Publishing

## Milestone
Missing Search Logging and Draft Term Publishing.

## Task Type
Large implementation pass.

This is a backend/content-operations milestone for AI-101.

## Objective
Turn the glossary from a static seeded corpus into a controlled, growing system by logging user search demand, capturing missing terms, creating draft terms for review, and publishing approved drafts into the canonical glossary.

## Product Context
- App name: AI-101
- Product: AI Terms Glossary
- The app should not rely on massive runtime AI usage for glossary coverage
- Published glossary terms must remain structured, reviewed, and database-backed
- Search demand should help drive glossary expansion
- AI should be optional and limited to draft/content operations, not primary runtime glossary delivery

## Canonical Term Schema
Published terms and draft terms should preserve this structure:
- slug
- term
- definition
- explanation
- humor
- see_also
- tags
- controversy_level

## Mission
Implement the first real glossary growth pipeline:
1. log searches
2. identify missing-demand terms
3. store draft terms in canonical structure
4. publish approved drafts into the live glossary

## Working Style Requirements
- Do not use MVP framing
- Do not propose tiny cleanup tasks
- Use a production-minded capability pass
- Keep runtime glossary delivery database-backed and deterministic
- Avoid turning Ask Glossary into the default content engine
- Keep AI optional and secondary in this milestone

## Required Outcomes
1. Add persistent search logging for glossary search usage
2. Record whether a search had an exact match
3. Make it possible to identify frequently searched missing terms
4. Add a draft-term persistence layer with canonical term fields
5. Add validation for draft term schema quality
6. Add a publish flow that moves approved drafts into the canonical glossary tables
7. Ensure related-term / see_also references are handled safely on publish
8. Normalize tags and slug format on publish
9. Keep published glossary terms as the app’s source of truth
10. Avoid unrelated architecture churn

## Data Model Requirements
### New table: term_search_events
Suggested fields:
- id
- query
- normalized_query
- matched_term_id (nullable)
- had_exact_match
- created_at

### New table: term_drafts
Suggested fields:
- id
- slug
- term
- definition
- explanation
- humor
- see_also
- tags
- controversy_level
- source_type
- source_reference
- status
- created_at
- updated_at

Draft status values should support at least:
- draft
- reviewed
- approved
- rejected
- published

## Backend Behavior Requirements
### Search logging
- Log every glossary search
- Record the original query and a normalized query
- Record whether an exact match existed
- Record the matched term id when applicable

### Missing-term aggregation
- Add a backend way to retrieve the top missing queries over time
- Group repeated normalized queries together
- Prioritize high-frequency misses

### Draft creation
- Allow creating draft terms manually or programmatically
- Drafts must preserve canonical schema shape
- Drafts must validate required fields and controversy bounds

### Draft publishing
- Publishing a draft should:
  - validate schema
  - normalize slug
  - normalize tags
  - upsert into terms
  - update term_relations / see_also safely
  - mark the draft as published

## Execution Rules
- Treat this as a backend/content-operations milestone
- Do not implement massive live AI rewriting in the app
- Do not auto-publish missing searches directly into the glossary
- Do not bypass review/validation
- Preserve existing app search and term display flows
- Keep the system extensible for future AI-assisted draft enrichment

## Deliverables
1. DB migrations for new search-log and term-draft tables
2. Backend repository/service support for logging searches and managing drafts
3. Validation rules for draft schema
4. Publish flow into canonical glossary tables
5. Aggregation path for missing-term demand
6. Summary of files changed
7. Remaining risks, if any
8. Merge readiness assessment

## Acceptance Criteria
- Searches are logged persistently
- Missing search demand can be queried meaningfully
- Draft terms can be stored with canonical fields
- Draft terms can be validated before publish
- Publishing a draft produces a real canonical glossary term in the live DB
- Related-term handling remains safe
- Tags and slug are normalized on publish
- Existing glossary search and browsing continue working
- No heavy runtime AI dependency is introduced

## Validation Expectations
- Run backend migrations successfully
- Verify search logging by executing real search queries
- Verify missing-term aggregation returns expected results
- Create at least one draft term manually
- Publish at least one approved draft into the live glossary
- Confirm the published term appears in API responses
- Confirm app search can find the published term
- Provide concrete verification notes

## Important
This milestone is the first real glossary growth system.
The goal is to let the glossary expand from real demand signals while preserving strong structure, editorial control, and low AI cost.

## Codex Prompt
```text
Implement the next major milestone for AI-101: Missing Search Logging and Draft Term Publishing.

Project context:
- App name: AI-101
- Product: AI Terms Glossary
- The app should not rely on massive runtime AI usage for glossary coverage
- Published glossary terms must remain structured, reviewed, and database-backed
- Search demand should help drive glossary expansion
- AI should be optional and limited to draft/content operations, not primary runtime glossary delivery

Canonical term schema:
- slug
- term
- definition
- explanation
- humor
- see_also
- tags
- controversy_level

Mission:
Implement the first real glossary growth pipeline:
1. log searches
2. identify missing-demand terms
3. store draft terms in canonical structure
4. publish approved drafts into the live glossary

Working style requirements:
- Do not use MVP framing
- Do not propose tiny cleanup tasks
- Use a production-minded capability pass
- Keep runtime glossary delivery database-backed and deterministic
- Avoid turning Ask Glossary into the default content engine
- Keep AI optional and secondary in this milestone

Required outcomes:
1. Add persistent search logging for glossary search usage
2. Record whether a search had an exact match
3. Make it possible to identify frequently searched missing terms
4. Add a draft-term persistence layer with canonical term fields
5. Add validation for draft term schema quality
6. Add a publish flow that moves approved drafts into the canonical glossary tables
7. Ensure related-term / see_also references are handled safely on publish
8. Normalize tags and slug format on publish
9. Keep published glossary terms as the app’s source of truth
10. Avoid unrelated architecture churn

Data model requirements:
New table: term_search_events
Suggested fields:
- id
- query
- normalized_query
- matched_term_id (nullable)
- had_exact_match
- created_at

New table: term_drafts
Suggested fields:
- id
- slug
- term
- definition
- explanation
- humor
- see_also
- tags
- controversy_level
- source_type
- source_reference
- status
- created_at
- updated_at

Draft status values should support at least:
- draft
- reviewed
- approved
- rejected
- published

Backend behavior requirements:
Search logging
- Log every glossary search
- Record the original query and a normalized query
- Record whether an exact match existed
- Record the matched term id when applicable

Missing-term aggregation
- Add a backend way to retrieve the top missing queries over time
- Group repeated normalized queries together
- Prioritize high-frequency misses

Draft creation
- Allow creating draft terms manually or programmatically
- Drafts must preserve canonical schema shape
- Drafts must validate required fields and controversy bounds

Draft publishing
- Publishing a draft should:
  - validate schema
  - normalize slug
  - normalize tags
  - upsert into terms
  - update term_relations / see_also safely
  - mark the draft as published

Execution rules:
- Treat this as a backend/content-operations milestone
- Do not implement massive live AI rewriting in the app
- Do not auto-publish missing searches directly into the glossary
- Do not bypass review/validation
- Preserve existing app search and term display flows
- Keep the system extensible for future AI-assisted draft enrichment

Deliverables:
1. DB migrations for new search-log and term-draft tables
2. Backend repository/service support for logging searches and managing drafts
3. Validation rules for draft schema
4. Publish flow into canonical glossary tables
5. Aggregation path for missing-term demand
6. Summary of files changed
7. Remaining risks, if any
8. Merge readiness assessment

Acceptance criteria:
- Searches are logged persistently
- Missing search demand can be queried meaningfully
- Draft terms can be stored with canonical fields
- Draft terms can be validated before publish
- Publishing a draft produces a real canonical glossary term in the live DB
- Related-term handling remains safe
- Tags and slug are normalized on publish
- Existing glossary search and browsing continue working
- No heavy runtime AI dependency is introduced

Validation expectations:
- Run backend migrations successfully
- Verify search logging by executing real search queries
- Verify missing-term aggregation returns expected results
- Create at least one draft term manually
- Publish at least one approved draft into the live glossary
- Confirm the published term appears in API responses
- Confirm app search can find the published term
- Provide concrete verification notes

Important:
This milestone is the first real glossary growth system.
The goal is to let the glossary expand from real demand signals while preserving strong structure, editorial control, and low AI cost.
```
