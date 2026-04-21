# AI-101 — Term Draft User Flow

## Milestone
Term Draft User Flow.

## Task Type
Large implementation pass.

This is an Android/product-flow milestone for AI-101.

## Objective
Implement the first user-facing missing-term contribution flow so users can create a draft term when search does not find what they need, using the backend draft-term pipeline that already exists.

## Product Context
- App name: AI-101
- Product: AI Terms Glossary
- Backend support for search logging, missing-query aggregation, term drafts, draft status, and draft publishing already exists
- The glossary should grow through controlled contributions, not massive live AI generation
- User-created terms must go into draft state, not directly into the live glossary
- This feature will later connect naturally to gamification, score, and badges

## Canonical Term Schema
Drafts and published terms should preserve this structure:
- slug
- term
- definition
- explanation
- humor
- see_also
- tags
- controversy_level

## Mission
Add the first app-side Term Draft flow so a user who cannot find a term can submit a canonical-structured draft to the backend.

## Working Style Requirements
- Do not use MVP framing
- Do not propose tiny cleanup tasks
- Use a production-ready feature pass
- Keep the user flow simple, clear, and controlled
- Preserve editorial control by drafting rather than auto-publishing
- Keep future gamification extensibility in mind, but do not implement gamification in this milestone

## Required Outcomes
1. Extend the search experience so missing-term scenarios have a clear next action
2. Add a visible “Create Term Draft” entry point when no exact term is found
3. Implement a draft submission screen or flow
4. Submit the draft to the backend draft-term API
5. Preserve canonical structure as much as possible in the UI model
6. Handle validation errors cleanly
7. Provide success/failure states that make sense to the user
8. Keep the glossary search experience primary; Term Draft is a fallback contribution path
9. Avoid unrelated architecture churn

## User Flow Requirements
### Search flow
When a search does not find an exact term:
- continue showing any partial/similar results
- show a clear option to create a missing term draft

### Draft submission flow
The user should be able to submit at least:
- term name
- definition or short description
- optional explanation
- optional humor
- optional tags

If category is already part of your UI/domain model and easy to support cleanly, it may be included, but do not let it derail the milestone.

### Submission behavior
- The app should send the draft to the backend draft endpoint
- The draft should be created in draft state, not published
- The app should show a success message that makes clear the term was submitted as a draft

## UX Requirements
- Keep the flow simple and low-friction
- Make it clear that the user is contributing a draft, not editing the live glossary directly
- Do not make Ask Glossary the default alternative in this flow
- Keep the UI clean and production-ready
- Support empty/loading/error/success states properly

## Backend Integration Requirements
Assume backend support exists for:
- creating term drafts
- draft validation
- status lifecycle
- publishing later by separate review flow

The Android app should integrate with the relevant backend endpoint(s) rather than faking local success.

## Deliverables
1. Updated search UX with missing-term draft entry point
2. Term Draft submission UI
3. Networking/repository support for submitting drafts
4. Validation/error handling in the app
5. Summary of files changed
6. Remaining risks, if any
7. Merge readiness assessment

## Acceptance Criteria
- A user can search for a term that does not exist and see a Create Term Draft option
- A user can open the draft flow and submit a draft term
- The app sends the draft to the backend successfully
- Validation failures are shown clearly
- Success state makes clear the term was saved as a draft, not published live
- Existing glossary search and browsing still work
- The implementation remains narrowly scoped to this feature

## Validation Expectations
- Run the Android app manually
- Search for a missing term
- Confirm the Create Term Draft action appears
- Submit a valid draft
- Confirm backend accepts it
- Submit an invalid draft and confirm validation errors surface correctly
- Confirm normal term search still works
- Provide concrete verification notes

## Important
This milestone is the user-facing layer of the glossary growth system.
The goal is to let users contribute missing terms safely, without turning the app into a direct live-publishing tool or a massive AI dependency.

## Codex Prompt
```text
Implement the next major milestone for AI-101: Term Draft User Flow.

Project context:
- App name: AI-101
- Product: AI Terms Glossary
- Backend support for search logging, missing-query aggregation, term drafts, draft status, and draft publishing already exists
- The glossary should grow through controlled contributions, not massive live AI generation
- User-created terms must go into draft state, not directly into the live glossary
- This feature will later connect naturally to gamification, score, and badges

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
Add the first app-side Term Draft flow so a user who cannot find a term can submit a canonical-structured draft to the backend.

Working style requirements:
- Do not use MVP framing
- Do not propose tiny cleanup tasks
- Use a production-ready feature pass
- Keep the user flow simple, clear, and controlled
- Preserve editorial control by drafting rather than auto-publishing
- Keep future gamification extensibility in mind, but do not implement gamification in this milestone

Required outcomes:
1. Extend the search experience so missing-term scenarios have a clear next action
2. Add a visible “Create Term Draft” entry point when no exact term is found
3. Implement a draft submission screen or flow
4. Submit the draft to the backend draft-term API
5. Preserve canonical structure as much as possible in the UI model
6. Handle validation errors cleanly
7. Provide success/failure states that make sense to the user
8. Keep the glossary search experience primary; Term Draft is a fallback contribution path
9. Avoid unrelated architecture churn

User flow requirements:
Search flow
- When a search does not find an exact term:
  - continue showing any partial/similar results
  - show a clear option to create a missing term draft

Draft submission flow
- The user should be able to submit at least:
  - term name
  - definition or short description
  - optional explanation
  - optional humor
  - optional tags

If category is already part of your UI/domain model and easy to support cleanly, it may be included, but do not let it derail the milestone.

Submission behavior
- The app should send the draft to the backend draft endpoint
- The draft should be created in draft state, not published
- The app should show a success message that makes clear the term was submitted as a draft

UX requirements:
- Keep the flow simple and low-friction
- Make it clear that the user is contributing a draft, not editing the live glossary directly
- Do not make Ask Glossary the default alternative in this flow
- Keep the UI clean and production-ready
- Support empty/loading/error/success states properly

Backend integration requirements:
Assume backend support exists for:
- creating term drafts
- draft validation
- status lifecycle
- publishing later by separate review flow

The Android app should integrate with the relevant backend endpoint(s) rather than faking local success.

Deliverables:
1. Updated search UX with missing-term draft entry point
2. Term Draft submission UI
3. Networking/repository support for submitting drafts
4. Validation/error handling in the app
5. Summary of files changed
6. Remaining risks, if any
7. Merge readiness assessment

Acceptance criteria:
- A user can search for a term that does not exist and see a Create Term Draft option
- A user can open the draft flow and submit a draft term
- The app sends the draft to the backend successfully
- Validation failures are shown clearly
- Success state makes clear the term was saved as a draft, not published live
- Existing glossary search and browsing still work
- The implementation remains narrowly scoped to this feature

Validation expectations:
- Run the Android app manually
- Search for a missing term
- Confirm the Create Term Draft action appears
- Submit a valid draft
- Confirm backend accepts it
- Submit an invalid draft and confirm validation errors surface correctly
- Confirm normal term search still works
- Provide concrete verification notes

Important:
This milestone is the user-facing layer of the glossary growth system.
The goal is to let users contribute missing terms safely, without turning the app into a direct live-publishing tool or a massive AI dependency.
```
