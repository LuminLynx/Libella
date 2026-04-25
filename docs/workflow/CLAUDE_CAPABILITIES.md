# CLAUDE_CAPABILITIES.md

## 1. Purpose

This document describes what Claude (Anthropic's coding agent, including Claude Code) can do when working on the AI-101 project, and what it deliberately will not do.

It is a **descriptive companion** to `docs/workflow/AGENTS.md` — not a replacement.
`AGENTS.md` defines the rules that apply to every agent.
This file records Claude's specific execution-environment capabilities so other contributors and the project owner know what Claude is realistically able to verify before pushing.

If anything in this file conflicts with `AGENTS.md`, the rules in `AGENTS.md` win.

---

## 2. Project Context

AI-101 is a native Android glossary app with an Android client, a backend API, and a database.
Full project context, MVP constraints, and product direction live in `AGENTS.md` §2 — Claude follows that section exactly.

---

## 3. What Claude Can Do in This Repo

### 3.1 Code and repository operations
- Read any file in the repository
- Search the codebase (file globs, ripgrep-style content search)
- Edit existing files and create new files
- Run shell commands in a sandboxed working directory
- Stage, commit, and push to its **designated branch** (see `AGENTS.md` workflow expectations and the per-task branch given to Claude)
- Use parallel subagents (Explore, Plan, general-purpose) for research-heavy tasks without polluting the main context

### 3.2 Backend (FastAPI / Python)
- Read and edit Python sources under the backend tree
- Run `pytest` and other Python-based checks in Claude's environment
- Validate backend behavior against the backend runtime as required by `AGENTS.md` §5.2

### 3.3 Android (Kotlin / Jetpack Compose / Gradle)
Claude's execution environment **does have a working Gradle toolchain**, so Claude can:
- read and edit Android sources, Gradle scripts, manifests, and resources
- run `./gradlew <task>` via the project wrapper for static and compile-level feedback when network access permits

Project Gradle facts Claude follows:
- Project Gradle version (per `gradle/wrapper/gradle-wrapper.properties`): **8.7**
- Android Gradle Plugin (per `app/build.gradle.kts`): **8.5.2**
- Always use `./gradlew` (the wrapper) rather than any system-installed Gradle, so the project's pinned version is honored.

Known network constraint:
- Some sandboxed runs cannot reach `dl.google.com/dl/android/maven2`, which means the Android Gradle Plugin and other Google-hosted artifacts may fail to resolve. When that happens, full Android builds will not complete in Claude's environment.
- In those cases, Claude falls back to static review and reasoning, and explicitly says so rather than claiming a green build.

Authoritative validation for Android still happens in **Android Studio** per `AGENTS.md` §5.1. Anything Claude runs locally is supplementary.

### 3.4 Research and reasoning
- Spawn parallel subagents for codebase exploration or implementation planning
- Use web search and web fetch when allowed by the harness
- Read large amounts of source quickly without context bloat

### 3.5 GitHub (scoped)
Claude's GitHub access in this repo is restricted to `luminlynx/foss-101`. Within that scope, Claude can:
- read issues and pull requests
- create issues and pull requests **only when the user explicitly asks**
- comment, review, and check CI status
- subscribe to PR activity events (CI results, review comments) when the user opts in

---

## 4. What Claude Will Not Do Without an Explicit Ask

These rules align with `AGENTS.md` §4.2, §11, and the project's git-safety expectations.

- Push to any branch other than the one designated for the task
- Force-push, run `git reset --hard`, or amend commits that have already been pushed
- Open a pull request unprompted (per session policy)
- Add features outside MVP scope (auth, chat, admin tooling, analytics, bookmarks, AI generation inside the app, etc. — see `AGENTS.md` §4.2 and §7.4)
- Rename `FOSS` → `AI` in folders, file paths, package names, or repo names (see `AGENTS.md` §2.1)
- Skip git hooks (`--no-verify`) or bypass signing
- Perform repo-wide refactors, doc cleanups, or speculative architecture changes that the active task did not request

When a task would require any of the above, Claude defers and asks — the same posture as `AGENTS.md` §11.

---

## 5. Validation Expectations

### 5.1 Android
- Claude may run `./gradlew` tasks (e.g. `help`, `tasks`, `assembleDebug`, `lint`) for static and compile-level feedback when its environment can resolve the required artifacts.
- Android Studio remains the **authoritative** validation environment for Android changes (`AGENTS.md` §5.1).
- Claude does not treat a successful local Gradle run as final acceptance — it is supplementary signal only (`AGENTS.md` §4.4).

### 5.2 Backend
- Claude validates backend changes by running tests and exercising the backend runtime in its own environment, as `AGENTS.md` §5.2 expects.

### 5.3 Reporting
- If a build, test, or check could not be completed (for example, AGP plugin resolution failed due to network restrictions), Claude states this explicitly in the conversation rather than claiming success.

---

## 6. Working Style

Claude follows the bounded-bundle posture in `AGENTS.md` §4.1:
- prefer one meaningful, reviewable bundle per task
- no unrelated cleanup, no speculative refactors
- focused diffs with clear rationale (`AGENTS.md` §9)
- conservative dependency and import changes (`AGENTS.md` §6, §10)

For small documentation or single-file fixes, Claude will produce equally small diffs rather than padding the change.

---

## 7. Source-of-Truth Hierarchy

This file is descriptive. The authoritative documents remain those listed in `AGENTS.md` §3:

1. `docs/roadmap/ROADMAP.md`
2. `docs/architecture/BACKEND_DATABASE_SCOPE.md`
3. `docs/workflow/AGENTS.md`
4. `docs/workflow/TASKS.md`
5. `docs/workflow/GEMINI_AGENT_ROADMAP.md`

If `CLAUDE_CAPABILITIES.md` ever conflicts with any of those, the source-of-truth doc wins and this file should be updated.

---

## 8. Escalation

Same rule as `AGENTS.md` §11: if a task would require changing approved scope, renaming internal project structure, altering product direction, or going beyond the source-of-truth docs, Claude does not guess. The task is deferred until the project docs are updated.
