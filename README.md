# Libella

![Status](https://img.shields.io/badge/status-Phase%203%20in%20progress-blue)
![Units](https://img.shields.io/badge/units-9%20of%2020%20published-purple)
![Platform](https://img.shields.io/badge/platform-Android-green)
![Backend](https://img.shields.io/badge/backend-FastAPI-teal)
![Database](https://img.shields.io/badge/database-PostgreSQL-blue)
![License](https://img.shields.io/badge/license-GPL--3.0-orange)

Libella is a mobile-native learning product for product-side professionals who need to become AI-fluent enough to make AI product decisions, not become machine-learning engineers.

The product teaches LLM systems through trade-offs: what a concept changes, what it costs, when it breaks, and how a product decision-maker should reason about it.

> Development note: this repository began as **FOSS-101 / AI-101**, a glossary-oriented Android app. The canonical strategy now reshapes it into **Libella**, a path-centric learning product. Some package names, app labels, legacy glossary files, and repo history still reflect that transition.

---

## Product vision

Libella helps product professionals become AI-fluent enough to lead the decisions their teams now have to make.

The intended audience is product-side professionals with real stakes in AI literacy:

- Product managers
- Product marketing managers
- Founders
- Design leads
- Business-development leads
- Executives and product-adjacent decision-makers

The goal is **decision-grade competence**: enough fluency to make build / buy / skip decisions, talk credibly with engineers, and recognize trade-offs and failure modes in AI-backed products.

Libella is not a math-first ML course, a code-first engineering curriculum, a glossary-first reference app, or a hype-feed about AI news.

---

## Current development status

Libella is currently in **Phase 3 — Full v1**.

Completed:

- **Phase 1: Foundation** — the core path-loop spine is in place.
- **Phase 2: Unit-1 pilot** — the LLM grader was proven and the gate passed.

In progress:

- **Phase 3: Full v1** — authoring the complete 20-unit path and completing the remaining spine / sidewall features.

Current state, per the project docs:

- 9 of 20 units are published.
- Units 10–20 remain to be authored.
- F5 Spaced Review is not started.
- F6 Path Overview is not started.
- Authoring is the pacing constraint.
- Phase 4 public-launch readiness has not started.

This is an in-development product, not a finished public release.

Canonical status docs:

- `docs/STRATEGY.md` — product strategy and locked decisions.
- `docs/EXECUTION.md` — phase plan and sequencing.
- `docs/PHASE_3_4_ROADMAP.md` — current Phase 3 / Phase 4 roadmap.
- `docs/curriculum/v1-path-outline.md` — the canonical 20-unit path outline.

---

## Core learning loop

The app is organized around a path, not a catalog.

The intended session loop is:

1. **Continue** — open the app and see the next unit in the path.
2. **Bite** — read a short, trade-off-first explanation.
3. **Decide** — answer an open-ended decision prompt.
4. **Calibrate** — see how the answer maps to a rubric, sources, and confidence.
5. **Progress** — complete the unit and advance through the path.
6. **Return** — revisit older units through spaced review.

The glossary exists as supporting reference material, not as the primary product surface.

---

## v1 path: LLM Systems for PMs

The canonical v1 path is **LLM Systems for PMs**.

The path is designed to teach product professionals how to reason about LLM-backed products through concrete product trade-offs.

Published units:

1. Tokenization
2. Context Window
3. Latency
4. Evals
5. Model selection
6. Prompt design basics
7. Hallucination + reliability
8. Cost dynamics at scale
9. Fine-tuning vs. prompting vs. RAG

Locked / planned units include:

10. Vector search / RAG fundamentals
11. Streaming UX
12. Tool use / function calling
13. Multimodal basics
14. Agents / multi-step reasoning
15. Safety + content moderation
16–20. Operating-phase units, to be locked from real-user signal

See `docs/curriculum/v1-path-outline.md` for the maintained source of truth.

---

## Product principles

Libella is guided by five product principles:

1. **Decisions before mechanism** — teach what to do with a concept before diving into how it works.
2. **Calibrate, don't bluff** — claims should be sourced, confidence-tagged, and honest about uncertainty.
3. **Path, not catalog** — the home experience is continuing the learning path, not browsing a glossary.
4. **Bite first, depth on tap** — every unit should be understandable quickly, with depth available when needed.
5. **Quality ceiling, not content scale** — better to ship fewer excellent units than many mediocre ones.

The primary wedge is the combination of trade-off-first pedagogy and calibrated reliability.

---

## Architecture

The repo contains an Android client, a FastAPI backend, PostgreSQL migrations, authored curriculum content, regression sets, and project documentation.

### Android

- Kotlin
- Jetpack Compose
- Material 3
- JWT-backed auth state
- Encrypted token storage
- Path home, unit reader, auth, settings, and supporting glossary surfaces

### Backend

- FastAPI
- PostgreSQL
- psycopg
- JWT auth
- Migration runner
- Path / unit / completion APIs
- LLM grading service
- Regression-set discipline for grader calibration

### Deployment / operations

- Railway-oriented backend deployment
- Production config validation through `APP_ENV=production`
- PostgreSQL migration discipline
- Prompt-caching strategy for grader unit economics

Backend decisions are documented in `docs/BACKEND_BEST_PRACTICES.md`.
Android decisions are documented in `docs/ANDROID_BEST_PRACTICES.md`.

---

## Repository structure

```text
app/                         Android client
backend/                     FastAPI backend, migrations, scripts, tests
content/units/               Authored learning units
content/regression-sets/     Ground-truth answer/grade regression sets
docs/                        Canonical strategy, execution, audit, and roadmap docs
gradle/                      Android Gradle wrapper files
scripts/                     Project utility scripts
```

Important docs:

```text
docs/STRATEGY.md                         Product strategy
docs/EXECUTION.md                        Phase plan
docs/AUDIT.md                            Phase 0 cleanup audit and keep/reshape/delete map
docs/PHASE_3_4_ROADMAP.md                Phase 3/4 roadmap
docs/ANDROID_BEST_PRACTICES.md           Android implementation decisions
docs/BACKEND_BEST_PRACTICES.md           Backend implementation decisions
docs/curriculum/v1-path-outline.md       Canonical v1 unit sequence
```

---

## Local development

This section is intentionally minimal until the setup flow is stabilized.

### Backend

Typical backend workflow:

```bash
python3 -m venv .venv
source .venv/bin/activate
pip install -r backend/requirements.txt
python3 -m backend.scripts.migrate
python3 -m backend.scripts.seed_db
uvicorn backend.app.main:app --host 0.0.0.0 --port 8000
```

Environment variables are expected for production-like runs, especially:

```text
DATABASE_URL
JWT_SECRET
AI_PROVIDER_API_KEY
APP_ENV
```

Production deployments should set `APP_ENV=production` so weak defaults are rejected at startup.

### Android

Typical Android workflow:

```bash
./gradlew assembleDebug
```

The app is developed in Android Studio and currently targets Android-first v1 development.

---

## Grader and calibration discipline

The product's credibility depends on the grader being trustworthy.

Each published unit is expected to ship with a ground-truth regression set. The grader is evaluated against authored expected outcomes before a unit is considered published.

The grading model is per-criterion, not holistic. The intended user-facing behavior is:

- show which criteria were met or not met;
- expose confidence;
- flag uncertain answers instead of pretending certainty;
- ground grading in the unit content, sources, rubric, and quoted user answer text.

This is part of the product's reliability moat, not an optional test harness.

---

## Legacy / transition notes

Some old glossary-oriented features are intentionally being demoted or removed as the product moves from FOSS-101 / AI-101 toward Libella.

Examples of legacy or demoted surfaces:

- Browse / Categories / Search as primary navigation
- Ask Glossary as a front door
- Term Draft contribution flows
- AI Learning Layer style-picker flows
- Glossary-first home screen patterns

The current source of truth for what survives, changes, or gets removed is `docs/AUDIT.md`.

---

## Public launch status

Libella has not reached public launch readiness.

Phase 4 is expected to cover:

- trademark review for the Libella name;
- read-only web preview;
- landing-page and marketing copy;
- beta cohort recruitment;
- cost dashboards;
- app-store preparation.

Until then, this repo should be understood as an active product-development repository.

---

## License

This project is licensed under the **GNU General Public License v3.0**.

See `LICENSE` for the full license text.
