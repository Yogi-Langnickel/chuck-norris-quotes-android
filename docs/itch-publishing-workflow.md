# itch.io Publishing Workflow

Status: active draft
Last updated: 2026-05-23

Use this workflow for the no-cost itch.io distribution path for Quote Battle
Royal. Keep credentials, session cookies, signing keys, screenshots from private
dashboards, and generated APK files out of Git.

## Project

- Page: `https://yogi-langnickel.itch.io/quote-battle-royal`
- Edit URL: `https://itch.io/game/edit/4602818`
- Classification: `Games`
- Kind: `Downloadable`
- Release status: `Released`
- Pricing: `No payments`
- Visibility before final review: `Draft`
- Android package/application id: `com.yogi.quotebattleroyal`

## Current Listing Copy

Short description:

```text
Pick the funnier fact, build streaks, and climb daily, weekly, and monthly scores.
```

Description:

```html
<p>Quote Battle Royal is a lightweight Android game where quick facts face off in tiny battles. Pick your favourite, build streaks, and keep your daily, weekly, and monthly scores moving.</p>
<p>This early Android build is free while the game grows. Feedback from testers is welcome.</p>
<p><strong>Android package:</strong> com.yogi.quotebattleroyal</p>
```

Install instructions:

```text
Download the Android APK on your device. If Android asks for permission, allow installs from your browser or file manager, then open Quote Battle Royal.
```

Tags currently saved by itch.io:

```text
casual, funny, no-ai
```

## Manual Browser Setup

Use the Playwright-controlled browser when the user wants the assistant to edit
the itch.io page directly.

1. Open `https://itch.io/login` in the Playwright browser.
1. The user logs in manually and completes any 2FA/captcha.
1. Verify with a snapshot from `https://itch.io/dashboard`.
1. Open the Quote Battle edit page.
1. Do not publish or switch visibility to public without explicit user approval.

## Release Artifact Checklist

Before uploading files to itch.io:

1. Confirm the Android package id is `com.yogi.quotebattleroyal`.
1. Confirm the version in `app/build.gradle.kts`.
1. Build from a release-signing environment outside repo-stored credentials.
1. Produce:
   - signed APK
   - SHA256 checksum
   - release notes
   - 3 to 5 screenshots
   - optional cover image, minimum `315x250`, recommended `630x500`
1. Run:

```sh
./gradlew test
./gradlew assembleDebug
git diff --check
```

Use `./gradlew assembleRelease` only when signing is configured safely outside
the repo. Never upload a debug APK for public distribution unless the user
explicitly accepts that temporary tester-only tradeoff.

## Publishing Steps

1. Keep page visibility as `Draft`.
1. Upload the signed APK.
1. Upload screenshots and a cover image.
1. Save the draft.
1. View the page as owner and check:
   - title
   - short description
   - install instructions
   - no payments
   - package id
   - download file name/version
1. Only after explicit user approval, set visibility to `Public`.

## Repeatable Automation Target

Later, prefer itch.io `butler` for repeat uploads after the first manual release
is proven:

```sh
butler push <signed-apk-path> yogi-langnickel/quote-battle-royal:android
```

Do not store butler API keys in the repo.
