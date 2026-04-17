# AI-101 — AI Learning Layer

## Milestone
AI Learning Layer.

## Task Type
Large implementation pass.

This is the first broad AI product milestone for AI-101.

## Objective
Turn AI-101 into an AI-assisted learning product by implementing the first full vertical slice of AI-powered user value.

## Product Context
- App name: AI-101
- Product: AI Terms Glossary
- Backend is live, connected to PostgreSQL, and seeded
- Android app is working end to end
- Android production polish milestone is complete
- The source of truth is backend/database, not YAML or terminal scripts
- Existing terminal scripts define the intended AI workflows:
  - Ask Glossary
  - Generate Scenario
  - Generate Challenge
- Those scripts are references for capability, not the runtime architecture

## Working Style Requirements
- Do not use MVP framing
- Do not propose tiny cleanup tasks
- Use large capability-based implementation
- Prioritize production readiness
- Prioritize real AI integration
- Optimize for merge-ready product quality

## Mission
Implement the full AI Learning Layer as one cohesive product milestone.

## Required Outcomes
1. Backend AI capability for Ask Glossary
2. Backend AI capability for term-specific Scenario generation
3. Backend AI capability for term-specific Challenge generation
4. Android UI integration for Ask Glossary
5. Android UI integration for Scenario generation
6. Android UI integration for Challenge generation
7. Proper states for all three:
   - loading
   - error
   - empty/unavailable
8. Keep backend secrets and provider calls server-side only
9. Keep the Android app as a pure client of backend APIs
10. Use the database-backed glossary as the source of truth
11. Use structured outputs where appropriate for generated artifacts
12. Add persistence or caching for generated scenario/challenge content where appropriate

## Execution Rules
- Do not shell out to JS scripts in production
- Re-implement the capabilities in the backend architecture
- Do not drift into unrelated backend or Android churn
- Do not stop after backend-only or UI-only work; complete the vertical slice
- Treat this as a product milestone, not an experiment

## Deliverables
1. Backend implementation for Ask Glossary, Scenario, and Challenge
2. Android UI integration for all three
3. Summary of files changed
4. Any remaining runtime/product risks
5. Merge readiness assessment

## Important
This is the first broad AI product milestone.
The outcome should make AI-101 feel like an AI-assisted learning app, not just a glossary with static content.

## Codex Prompt
```text
Implement the next major milestone for AI-101: the AI Learning Layer.

Project context:
- App name: AI-101
- Product: AI Terms Glossary
- Backend is live, connected to PostgreSQL, and seeded
- Android app is working end to end
- Android production polish milestone is complete
- The source of truth is now backend/database, not YAML or terminal scripts
- Existing terminal scripts already define the intended AI workflows:
  - Ask Glossary
  - Generate Scenario
  - Generate Challenge
- These scripts are references for capability, not runtime architecture

Working style requirements:
- Do not use MVP framing
- Do not propose tiny cleanup tasks
- Use large capability-based implementation
- Prioritize production readiness
- Prioritize real AI integration
- Optimize for merge-ready product quality

Your mission:
Implement the full AI Learning Layer as one cohesive product milestone.

Required outcomes:
1. Backend AI capability for Ask Glossary
2. Backend AI capability for term-specific Scenario generation
3. Backend AI capability for term-specific Challenge generation
4. Android UI integration for Ask Glossary
5. Android UI integration for Scenario generation
6. Android UI integration for Challenge generation
7. Proper states for all three:
   - loading
   - error
   - empty/unavailable
8. Keep backend secrets and provider calls server-side only
9. Keep the Android app as a pure client of backend APIs
10. Use the database-backed glossary as the source of truth
11. Use structured outputs where appropriate for generated artifacts
12. Add persistence or caching for generated scenario/challenge content where appropriate

Execution rules:
- Do not shell out to JS scripts in production
- Re-implement the capabilities in the backend architecture
- Do not drift into unrelated backend or Android churn
- Do not stop after backend-only or UI-only work; complete the vertical slice
- Treat this as a product milestone, not an experiment

Deliverables:
1. Backend implementation for Ask Glossary, Scenario, and Challenge
2. Android UI integration for all three
3. Summary of files changed
4. Any remaining runtime/product risks
5. Merge readiness assessment

Important:
This is the first broad AI product milestone.
The outcome should make AI-101 feel like an AI-assisted learning app, not just a glossary with static content.
```
