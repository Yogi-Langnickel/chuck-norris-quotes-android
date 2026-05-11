# Agent Instructions

Read this file first before inspecting or changing the project.

Canonical memory and durable docs now live in the central workspace memory repo:

- Workspace memory: `/Users/yogi/Coding/docs/workspace/agent-memory.md`
- Chuck Norris memory: `/Users/yogi/Coding/docs/projects/chuck-norris/agent-memory.md`

Repo-local `docs/` files are compatibility copies for existing scripts and historical links. Prefer the central docs above for new durable memory; update repo-local copies only when a repo script, CI check, or in-repo reference still requires it.

Do not rely on generated context snapshot files for this repo. Agents have direct repository access and should inspect source files with `rg`, `rg --files`, and focused reads.

## Project Context

Chuck_Norris is a small Android app built with Kotlin, Jetpack Compose, Material 3, MVVM, and Ktor. It fetches random quotes from `https://api.chucknorris.io/jokes/random`, displays them, supports copying the current quote, and lets the user close the app.

## Do Not Edit

Do not edit generated, local, dependency, or build-output files unless explicitly required:

- `.gradle/`
- `.kotlin/`
- `app/build/`
- generated KSP output under `build/generated/`
- APKs and other compiled artifacts
- `local.properties`
- Gradle wrapper binary files unless the task is specifically about Gradle wrapper maintenance

Never invent secrets, credentials, signing keys, API keys, Play Store credentials, or real release configuration.

## Local Workflow

- Do not make code, documentation, memory, dependency, or configuration changes directly on `master` or `main`.
- If a session starts on `master` or `main`, create or switch to a scoped `feature/`, `fix/`, `chore/`, or `docs/` branch before editing.
- Completed deliverables target `develop`; `master` is release/promotion only and requires explicit user direction.
- Use `rg` and `rg --files` for search.
- Inspect focused files before editing.
- Prefer existing Kotlin, Compose, MVVM, Ktor, and Material 3 patterns.
- Keep app source changes under `app/src/main/java/com/yogi/chucknorris` unless build/config/test work requires another location.
- Keep UI in composables, state in `QuoteViewModel`, data operations in `QuoteRepository`, and HTTP calls in `ApiService`.
- Keep changes scoped to the requested behavior and avoid unrelated refactors.
- Do not revert unrelated user changes.

## Security Baseline

- Never commit `local.properties`, signing keys, keystores, Play Store credentials, API keys, private release config, APK/AAB artifacts, or generated build output.
- Keep networking HTTPS-only and treat remote quote text as untrusted display text.
- Do not add analytics, crash reporting, tracking SDKs, ads, or new network calls without privacy/security review.
- Read `SECURITY.md` and `/Users/yogi/Coding/docs/projects/chuck-norris/memory/security.md` before changing networking, dependencies, release packaging, signing, or Play Store workflow.

## Pre-Develop Review Gate

- Before asking for, approving, or performing substantial or high-risk merges into `develop`, request a manual pre-merge persona review from the workspace master assistant.
- Use the workspace prompt at `/Users/yogi/Coding/Subagent-Prompts/personas/pre-merge-review-gate.md` when available.
- Select personas by risk area. For this Android app, common personas are Senior UX/UI Designer, QA And Test Architect, Security And Privacy Engineer, Product And Operations Reviewer, and Senior Solution Architect for architecture changes.
- Do not merge to `master` unless the user explicitly asks for a release/promotion flow.

## Validation

This is an Android/Gradle repository, not a Node/npm project. Do not run `npm audit`, `npm test`, or other Node-default checks here unless a future Node toolchain is explicitly added and documented.

Use focused validation first:

- `./gradlew test` for JVM unit tests.
- `./gradlew assembleDebug` for a local debug build.
- `./gradlew connectedAndroidTest` only when an emulator/device is available and instrumentation behavior matters.
- `./gradlew assembleRelease` only when release packaging or CI parity matters.

The Gradle config currently targets Java/JVM 24. Verify the local JDK before treating build failures as code failures.

## Memory Maintenance

Update memory when a task creates stable knowledge about architecture, recurring commands, build requirements, API behavior, testing, release workflow, or known pitfalls.

Every incident review and qualifying non-incident bug learning must include transferability classification: `local-only`, `workspace-general`, `family/cross-repo`, or `named repo targets`. Bugs or defects that reach `develop` are QA/test incidents unless explicitly waived with rationale.

Use:

- `/Users/yogi/Coding/docs/projects/chuck-norris/agent-memory.md` for compact current truth.
- `/Users/yogi/Coding/docs/projects/chuck-norris/memory/` for focused durable knowledge.
- `/Users/yogi/Coding/docs/projects/chuck-norris/memory/operations.md` for dated maintenance notes and workflow learnings.

Keep `/Users/yogi/Coding/docs/projects/chuck-norris/agent-memory.md` under 150 lines. Do not store raw logs, secrets, transient debugging notes, or speculative plans.

If a future assistant wants a broad context snapshot, prefer creating a temporary local artifact outside durable docs, then delete it when done. Durable repo memory should stay compact and source-linked.
