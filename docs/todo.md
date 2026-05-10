# Chuck Norris App TODO

Track small follow-ups that should not block the current quote and cat fact feature branch, but should be handled before the app grows much further.

## Current Branch Follow-Ups

- [ ] Replace quote-only `LiveData<Quote>` state with an explicit UI state model for loading, success, and error states.
- [ ] Surface Chuck Norris API and Cat Fact API failures as clear retryable error UI instead of treating fallback text as content.
- [ ] Disable or guard copy/share actions while no successful quote or fact is available.
- [ ] Add a share intent safety guard and user-visible failure state for devices without a matching share activity.
- [ ] Add focused ViewModel tests for loading, success, and API failure transitions for both Chuck Norris quotes and cat facts.
- [ ] Run a second persona review after the error-state refactor, incorporate relevant findings, and rerun unit/build checks before release.

## Release Hygiene

- [ ] Update `docs/agent-memory.md` after each meaningful app change with the decision, files touched, and checks run.
- [ ] Keep README and Fastlane metadata aligned whenever a new third-party endpoint or user-visible feature is added.
- [ ] Re-run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug` before publishing another APK.
