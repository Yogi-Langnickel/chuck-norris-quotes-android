# Chuck Norris App TODO

Track small follow-ups that should not block the current quote and cat fact feature branch, but should be handled before the app grows much further.

## Current Branch Follow-Ups

- [x] Make Quote Battle scoring user-selected: a loaded round does not update scores until the user chooses Chuck or Cat, and each round records only once.
- [x] Keep the battle winner in place, swipe the loser away, and refresh only the losing side as the next challenger.
- [x] Replace battle select buttons with swipe-away selection and bring the refreshed challenger in from the opposite side.
- [x] Split the app into Battle Mode, Chuck Facts, and Cat Facts tabs.
- [x] Add per-stream quote/fact rate limiting and a latest-release update entry point.
- [x] Add the generated simplified action-hero head as the launcher icon.
- [x] Remove fact-power details from standalone Chuck and Cat fact cards.
- [x] Rotate cat facts between Cat Fact Ninja and MeowFacts.
- [x] Add a Dog Facts tab and include dog facts as an eligible battle challenger stream.
- [x] Refresh battle challengers from a random non-winner stream with fallback when an eligible API fails.
- [x] Smooth battle swipe gestures by preserving accepted drag offset into the loser exit animation and spring-settling rejected swipes.
- [x] Remove battle card tilt/rotation so accepted swipes slide out cleanly without a brief flip.
- [x] Add a top-bar update action that opens the latest GitHub release page for manual APK updates.
- [x] Add source-specific Chuck, Cat, and Dog victory animations on top of the winning battle card.
- [x] Replace quote-only `LiveData<Quote>` state with an explicit UI state model for loading, success, and error states.
- [x] Surface Chuck Norris API and Cat Fact API failures as clear retryable error UI instead of treating fallback text as content.
- [x] Disable or guard copy/share actions while no successful quote or fact is available.
- [x] Add a share intent safety guard and user-visible failure state for devices without a matching share activity.
- [x] Add focused ViewModel tests for loading, success, and API failure transitions for both Chuck Norris quotes and cat facts.
- [x] Run a second persona review after the error-state refactor, incorporate relevant findings, and rerun unit/build checks before release.

## Release Hygiene

- [x] Update `docs/agent-memory.md` after each meaningful app change with the decision, files touched, and checks run.
- [x] Keep README and Fastlane metadata aligned whenever a new third-party endpoint or user-visible feature is added.
- [x] Re-run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug` before publishing another APK.

## Future Fun Features

- [x] Develop Quote Battle further as a possible standalone mode/app with a chaos champion streak and richer tournament framing.
- [ ] Add richer animation polish beyond the current winner celebration and loser swipe-away.
- [ ] Add Compose UI smoke coverage for Battle tab headline removal, battle selection guidance, and the light/dark theme toggle once UI test infrastructure is introduced.
- [ ] Add a share-card generator that renders the current quote/fact as a themed image card, with themes such as classified file, retro action poster, and cat fact interruption.
- [ ] Add a lightweight daily challenge around one quote/fact, such as rating the power level, guessing the category, or deciding whether a cat survives the fact.
- [ ] Revisit Chuck source rotation if a reliable free/no-auth Chuck Norris API besides `api.chucknorris.io` becomes available.
