# Chuck Norris App TODO

Track small follow-ups that should not block the current quote and cat fact feature branch, but should be handled before the app grows much further.

## Current Branch Follow-Ups

- [x] Make Quote Battle scoring user-selected: a loaded round does not update scores until the user chooses Chuck or Cat, and each round records only once.
- [x] Replace quote-only `LiveData<Quote>` state with an explicit UI state model for loading, success, and error states.
- [x] Surface Chuck Norris API and Cat Fact API failures as clear retryable error UI instead of treating fallback text as content.
- [x] Disable or guard copy/share actions while no successful quote or fact is available.
- [x] Add a share intent safety guard and user-visible failure state for devices without a matching share activity.
- [x] Add focused ViewModel tests for loading, success, and API failure transitions for both Chuck Norris quotes and cat facts.
- [x] Run a second persona review after the error-state refactor, incorporate relevant findings, and rerun unit/build checks before release.

## Release Hygiene

- [ ] Update `docs/agent-memory.md` after each meaningful app change with the decision, files touched, and checks run.
- [ ] Keep README and Fastlane metadata aligned whenever a new third-party endpoint or user-visible feature is added.
- [ ] Re-run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug` before publishing another APK.

## Future Fun Features

- [ ] Develop Quote Battle further as a possible standalone mode/app with a chaos champion streak and richer tournament framing.
- [ ] Add a richer animated battle presentation for quote contests beyond the current animated contender cards.
- [ ] Add a share-card generator that renders the current quote/fact as a themed image card, with themes such as classified file, retro action poster, and cat fact interruption.
- [ ] Add a lightweight daily challenge around one quote/fact, such as rating the power level, guessing the category, or deciding whether a cat survives the fact.
