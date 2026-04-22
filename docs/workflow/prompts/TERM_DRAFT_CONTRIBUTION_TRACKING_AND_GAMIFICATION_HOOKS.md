# AI-101 — Term Draft Contribution Tracking and Gamification Hooks

## Milestone
Term Draft Contribution Tracking and Gamification Hooks.

## Task Type
Large implementation pass.

This is a backend/product-systems milestone for AI-101.

## Objective
Add the first contribution-tracking layer behind Term Draft submissions so the system can later reward users with score and badges, while keeping the current implementation practical and controlled.

## Product Context
- App name: AI-101
- Product: AI Terms Glossary
- The Term Draft feature now works end to end
- The backend already supports search logging, term drafts, draft status, and publishing
- The next layer is to track user contributions in a structured way
- Full gamification UI is not required yet, but the system should be built so score and badge features can be added cleanly later

## Mission
Implement contribution tracking for draft-term activity and expose enough backend/app structure so user contribution score can exist as a real system rather than a vague future idea.

## Working Style Requirements
- Do not use MVP framing
- Do not propose tiny cleanup tasks
- Use a production-minded capability pass
- Keep the system extensible for future badges and contribution ranking
- Avoid overbuilding a flashy UI before the data model and event model are solid

## Required Outcomes
1. Track which user created a term draft
2. Track important draft lifecycle events relevant to contribution value
3. Introduce a contribution score model or event model that supports later gamification
4. Make draft approval/publish events attributable to the original contributor
5. Expose user contribution summary data in a clean way
6. Keep the design extensible for future badges, achievements, and leaderboards
7. Avoid unrelated architecture churn

## System Design Requirements
### Contribution events
Track events such as:
- draft_submitted
- draft_reviewed
- draft_approved
- draft_published

You may choose an event-log design, a score-table design, or a hybrid, but it must be durable and extensible.

### Attribution
Each draft should be attributable to a user identity or contributor identity supported by the current app/account model.

If the current app does not yet have full user accounts, implement the cleanest contributor identity strategy available in the project without derailing the milestone.

### Scoring
Introduce contribution scoring rules for at least:
- draft submitted
- draft approved
- draft published

The exact point values can be simple and configurable, but they must be persisted and derivable from real contribution data.

### Future extensibility
The design should support later features like:
- badges
- milestone achievements
- contribution history
- contributor profile summary
- leaderboard or ranking

## Backend Requirements
1. Add the necessary data model changes for contribution attribution and event/score tracking
2. Ensure draft lifecycle transitions can emit or persist contribution events
3. Make it possible to query contribution summary for a contributor
4. Keep publishing flow connected to contributor attribution
5. Add verification coverage for contribution tracking behavior

## App/Product Requirements
1. Add the minimum user-visible support needed to acknowledge contribution state if appropriate
2. Do not overbuild the gamification UI yet
3. If a lightweight contribution confirmation or summary is useful, keep it simple and production-ready
4. Preserve current Term Draft flow behavior

## Deliverables
1. Contribution tracking data model and backend implementation
2. Attribution wiring from Term Draft creation to later lifecycle events
3. Score/event persistence for contribution milestones
4. Query path for contribution summary
5. Summary of files changed
6. Remaining risks, if any
7. Merge readiness assessment

## Acceptance Criteria
- A draft submission is attributable to a contributor
- Important lifecycle events are tracked durably
- Contribution score or equivalent gamification-ready state is persisted
- Draft approval/publish can be connected back to the original contributor
- The system can produce a contribution summary for a contributor
- Existing Term Draft flow continues working
- The design clearly supports later badge/achievement work

## Validation Expectations
- Run migrations successfully
- Create a draft term and confirm contributor attribution exists
- Transition a draft through relevant lifecycle stages
- Confirm contribution events or score updates are recorded
- Query contribution summary successfully
- Verify no regression to current Term Draft functionality
- Provide concrete verification notes

## Important
This milestone is the systems foundation for gamification.
The goal is not to ship shiny badges immediately, but to make contribution tracking real so future score and badge features are based on durable product behavior.

## Codex Prompt
```text
Implement the next major milestone for AI-101: Term Draft Contribution Tracking and Gamification Hooks.

Project context:
- App name: AI-101
- Product: AI Terms Glossary
- The Term Draft feature now works end to end
- The backend already supports search logging, term drafts, draft status, and publishing
- The next layer is to track user contributions in a structured way
- Full gamification UI is not required yet, but the system should be built so score and badge features can be added cleanly later

Mission:
Implement contribution tracking for draft-term activity and expose enough backend/app structure so user contribution score can exist as a real system rather than a vague future idea.

Working style requirements:
- Do not use MVP framing
- Do not propose tiny cleanup tasks
- Use a production-minded capability pass
- Keep the system extensible for future badges and contribution ranking
- Avoid overbuilding a flashy UI before the data model and event model are solid

Required outcomes:
1. Track which user created a term draft
2. Track important draft lifecycle events relevant to contribution value
3. Introduce a contribution score model or event model that supports later gamification
4. Make draft approval/publish events attributable to the original contributor
5. Expose user contribution summary data in a clean way
6. Keep the design extensible for future badges, achievements, and leaderboards
7. Avoid unrelated architecture churn

System design requirements:
Contribution events
- Track events such as:
  - draft_submitted
  - draft_reviewed
  - draft_approved
  - draft_published

You may choose an event-log design, a score-table design, or a hybrid, but it must be durable and extensible.

Attribution
- Each draft should be attributable to a user identity or contributor identity supported by the current app/account model.
- If the current app does not yet have full user accounts, implement the cleanest contributor identity strategy available in the project without derailing the milestone.

Scoring
- Introduce contribution scoring rules for at least:
  - draft submitted
  - draft approved
  - draft published
- The exact point values can be simple and configurable, but they must be persisted and derivable from real contribution data.

Future extensibility
- The design should support later features like:
  - badges
  - milestone achievements
  - contribution history
  - contributor profile summary
  - leaderboard or ranking

Backend requirements:
1. Add the necessary data model changes for contribution attribution and event/score tracking
2. Ensure draft lifecycle transitions can emit or persist contribution events
3. Make it possible to query contribution summary for a contributor
4. Keep publishing flow connected to contributor attribution
5. Add verification coverage for contribution tracking behavior

App/Product requirements:
1. Add the minimum user-visible support needed to acknowledge contribution state if appropriate
2. Do not overbuild the gamification UI yet
3. If a lightweight contribution confirmation or summary is useful, keep it simple and production-ready
4. Preserve current Term Draft flow behavior

Deliverables:
1. Contribution tracking data model and backend implementation
2. Attribution wiring from Term Draft creation to later lifecycle events
3. Score/event persistence for contribution milestones
4. Query path for contribution summary
5. Summary of files changed
6. Remaining risks, if any
7. Merge readiness assessment

Acceptance criteria:
- A draft submission is attributable to a contributor
- Important lifecycle events are tracked durably
- Contribution score or equivalent gamification-ready state is persisted
- Draft approval/publish can be connected back to the original contributor
- The system can produce a contribution summary for a contributor
- Existing Term Draft flow continues working
- The design clearly supports later badge/achievement work

Validation expectations:
- Run migrations successfully
- Create a draft term and confirm contributor attribution exists
- Transition a draft through relevant lifecycle stages
- Confirm contribution events or score updates are recorded
- Query contribution summary successfully
- Verify no regression to current Term Draft functionality
- Provide concrete verification notes

Important:
This milestone is the systems foundation for gamification.
The goal is not to ship shiny badges immediately, but to make contribution tracking real so future score and badge features are based on durable product behavior.
```
