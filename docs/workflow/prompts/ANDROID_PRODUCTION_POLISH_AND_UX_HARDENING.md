# AI-101 — Android Production Polish and UX Hardening Task

## Milestone
Android production polish and UX hardening.

## Task Type
Large implementation pass.

This is a finish-the-product milestone, not MVP work.

## Objective
Turn the Android app's core experience into a production-grade, visually coherent product by implementing a strong design system and applying it across the main user flows.

## Product Context
- App name: AI-101
- Product: AI Terms Glossary
- Android app is already live and connected to the Railway backend
- Core backend-backed flows are working:
  - Browse
  - Categories
  - Details
  - Search
- PostgreSQL backend hardening has already been completed and validated
- The current focus is now product finish and Android UX quality

## Working Style Requirements
- Do not use MVP framing
- Do not propose tiny cleanup tasks
- Use large capability-based implementation
- Prioritize production readiness
- Prioritize polished Android UI/theme
- Optimize for merge-ready product quality

## Mission
Build and apply a production-grade Android design system across the core app flows.

This should not be treated as cosmetic cleanup. It is the implementation pass that turns the app from functionally working into shipping-quality.

## Required Outcomes
1. Establish or refine a real app-wide visual system:
   - color usage
   - typography
   - spacing
   - shapes
   - cards
   - chips
   - top bars
   - buttons
   - list and item styling
2. Apply that system consistently across:
   - Browse screen
   - Categories screen
   - Details screen
   - Search screen
3. Improve UX states:
   - loading
   - empty
   - error
   - no-results
4. Improve visual hierarchy and readability on the Details screen
5. Improve search UX and result presentation
6. Eliminate rough or dev-looking UI patterns and obvious placeholder presentation
7. Keep backend integration intact and do not regress working flows
8. Keep the implementation realistic for a production Android app, not a concept redesign

## Execution Rules
- Focus on the real shipped Android UI
- Do not drift into backend work
- Do not introduce unrelated architectural churn
- Do not stop after one screen; complete the whole core flow set
- Prefer a cohesive implementation pass over isolated tweaks

## Deliverables
1. Production-ready Android UI/theme improvements across the core flows
2. Concise summary of what changed visually and structurally
3. Files changed
4. Any remaining visual or UX risks
5. Merge readiness assessment

## Done Criteria
This task is done only if:
- the four main flows look visually consistent
- the app reads as intentional and branded, not generic or dev-like
- loading, error, empty, and no-results states are polished
- the UI has no obvious placeholder styling
- the core screens are ready to be shown as a real product

## Codex Prompt
```text
Implement the next major milestone for AI-101: Android production polish and UX hardening.

Project context:
- App name: AI-101
- Product: AI Terms Glossary
- Android app is already live and connected to the Railway backend
- Core backend-backed flows are working:
  - Browse
  - Categories
  - Details
  - Search
- The PostgreSQL backend hardening milestone has been completed and validated
- We are now moving to product-finish work, not MVP work

Working style requirements:
- Do not use MVP framing
- Do not propose tiny cleanup tasks
- Use large capability-based implementation
- Prioritize production readiness
- Prioritize polished Android UI/theme
- Optimize for merge-ready product quality

Your mission:
Turn the Android app’s core experience into a production-grade, visually coherent product by implementing a strong design system and applying it across the main flows.

Required outcomes:
1. Establish or refine a real app-wide visual system:
   - color usage
   - typography
   - spacing
   - shapes
   - cards
   - chips
   - top bars
   - buttons
   - list/item styling
2. Apply that system consistently across:
   - Browse screen
   - Categories screen
   - Details screen
   - Search screen
3. Improve UX states:
   - loading
   - empty
   - error
   - no-results
4. Improve visual hierarchy and readability on the Details screen
5. Improve search UX and result presentation
6. Eliminate rough/dev-looking UI patterns and obvious placeholder presentation
7. Keep backend integration intact and do not regress working flows
8. Keep the implementation realistic for a production Android app, not a concept redesign

Execution rules:
- Focus on the real shipped Android UI
- Do not drift into backend work
- Do not introduce unrelated architectural churn
- Do not stop after one screen; complete the whole core flow set
- Prefer a cohesive implementation pass over isolated tweaks

Deliverables:
1. Production-ready Android UI/theme improvements across the core flows
2. Concise summary of what changed visually and structurally
3. Files changed
4. Any remaining visual or UX risks
5. Merge readiness assessment

Important:
This is a finish-the-product milestone.
Treat this as a large implementation pass that materially upgrades the app experience.
```
