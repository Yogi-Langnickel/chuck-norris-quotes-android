# Release Readiness

Status: draft operations checklist
Last updated: 2026-05-16

Use this document to coordinate Google Play, App Store, and tester-readiness
work without storing credentials, signing keys, private account identifiers, or
submission-only artifacts in Git.

## Guardrails

- Do not commit signing keys, keystores, provisioning profiles, App Store
  Connect API keys, Play Console credentials, service-account JSON, screenshots
  from private consoles, generated APK/AAB/IPA archives, or `local.properties`.
- Keep Android and iOS release credentials outside the repository.
- Treat all store policy requirements as time-sensitive; verify them in the
  relevant console before submission.
- Continue without store credentials by preparing docs, metadata, validation
  commands, and tester coordination templates.

## Android Play Checklist

- [ ] Confirm the active release version in `README.md`, Gradle config, and
  release notes.
- [ ] Review `fastlane/metadata/android/en-US/` for title, short description,
  and full description alignment with current app behavior.
- [ ] Verify privacy copy still matches the app: internet access only, no login,
  payments, contacts, location, analytics, ads, or private backend.
- [ ] Run `./gradlew testDebugUnitTest`.
- [ ] Run `./gradlew assembleRelease` when preparing a release artifact.
- [ ] Run `git diff --check`.
- [ ] Inspect `git status --short` for generated artifacts and private files
  before committing.
- [ ] Build or upload signed release artifacts only from a credentialed release
  environment outside this repo.
- [ ] If the Play account is a personal developer account that requires closed
  testing, recruit at least 12 testers and keep them opted in for at least 14
  continuous days before applying for production access. This reflects the
  official Play Console Help requirement checked on 2026-05-16; verify again in
  Play Console before submission.
- [ ] Capture final store-console decisions outside Git if they include private
  account data.

## App Store Checklist

- [ ] Decide whether the first iOS delivery is a native SwiftUI app or a shared
  Kotlin Multiplatform extraction.
- [ ] Keep the iOS bundle id, Team ID, provisioning profiles, certificates, and
  App Store Connect API keys out of Git.
- [ ] Mirror current Android product behavior before adding iOS-only features:
  battle-first experience, Chuck/Cat/Dog tabs, local-only scoring, share/copy,
  update/store link, light/dark theme, and retryable API errors.
- [ ] Verify third-party API wording and privacy answers for Chuck, Cat, and Dog
  fact sources.
- [ ] Prepare TestFlight tester groups and beta test information in App Store
  Connect, including beta description, features to test, and feedback email.
- [ ] Run iOS unit/UI checks from the future iOS project before TestFlight.
- [ ] Archive and submit only from a credentialed Mac/Xcode environment outside
  repo-stored credentials.

## Tester Recruitment Tracker

Copy the table into a private tracker when real names or email addresses are
needed. Keep this in-repo version anonymous.

| Tester code | Platform | Channel | Invite sent | Opted in | First build tested | Feedback received | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| T01 | Android | Play closed test |  |  |  |  |  |
| T02 | Android | Play closed test |  |  |  |  |  |
| T03 | Android | Play closed test |  |  |  |  |  |
| T04 | Android | Play closed test |  |  |  |  |  |
| T05 | Android | Play closed test |  |  |  |  |  |
| T06 | Android | Play closed test |  |  |  |  |  |
| T07 | Android | Play closed test |  |  |  |  |  |
| T08 | Android | Play closed test |  |  |  |  |  |
| T09 | Android | Play closed test |  |  |  |  |  |
| T10 | Android | Play closed test |  |  |  |  |  |
| T11 | Android | Play closed test |  |  |  |  |  |
| T12 | Android | Play closed test |  |  |  |  |  |
| I01 | iOS | TestFlight |  |  |  |  |  |
| I02 | iOS | TestFlight |  |  |  |  |  |
| I03 | iOS | TestFlight |  |  |  |  |  |

## iOS Scaffold Decision Record

Decision: start with a native SwiftUI scaffold when iOS implementation begins.

Rationale:

- The existing Android app is compact and UI-heavy; duplicating the first iOS
  surface in SwiftUI is lower risk than introducing Kotlin Multiplatform before
  shared domain logic is proven valuable.
- Store readiness requires iOS-specific signing, TestFlight, review metadata,
  privacy answers, screenshots, and device QA regardless of sharing strategy.
- The current data model is small enough to reimplement around HTTPS API clients
  and local-only score persistence without forcing shared build complexity.

Deferred option:

- Revisit Kotlin Multiplatform only if Android and iOS logic diverge or battle
  scoring, source fallback, API parsing, or local persistence grows enough that
  duplication becomes a real maintenance cost.

Non-goals for the scaffold:

- No signing material, provisioning profiles, App Store Connect credentials, or
  private release config in Git.
- No analytics, crash reporting, ads, tracking, login, payments, or private
  backend without a separate privacy/security review.
- No Android app source changes just to prepare the iOS scaffold.
