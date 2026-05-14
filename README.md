# Quote Battle Royal Android

A small Kotlin Android app that fetches random Chuck Norris quotes, cat facts, and dog facts from public APIs, then displays them with a Jetpack Compose interface.

## Install On Android

Quick download: [latest Quote Battle Royal APK](https://github.com/Yogi-Langnickel/chuck-norris-quotes-android/releases/latest/download/quote-battle-royal-latest.apk)

The easiest way to install the app on your own phone is through the latest GitHub release:

1. Open the quick download link above on your Android phone.
2. Download the release APK asset.
3. If Android asks, allow your browser or file manager to install unknown apps.
4. Open the downloaded APK and confirm installation.

Android may show a warning because the app is not distributed through Google Play.

## Install With ADB

From a machine with Android platform tools installed:

```bash
adb install -r quote-battle-royal-latest.apk
```

To install a downloaded release asset:

```bash
adb install -r quote-battle-royal-latest.apk
```

## Build Locally

Requirements:

- Android Studio with the Android SDK installed.
- JDK 21.
- An Android device or emulator running Android 8.0 or newer.

If VS Code or the Gradle extension reports `Unsupported class file major version 69`, it is running Gradle analysis with Java 25. Configure the IDE/Gradle JVM to JDK 21. On this Mac, Homebrew installs it at `/usr/local/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home`.

Commands:

```bash
./gradlew testDebugUnitTest
./gradlew assembleRelease
```

The generated APK is written to:

```text
app/build/outputs/apk/release/app-release-unsigned.apk
```

## Features

- Fetches random Chuck Norris quotes from `api.chucknorris.io`.
- Rotates random cat facts between `catfact.ninja` and MeowFacts.
- Fetches random dog facts from Dog API.
- Runs a battle-first mode where the user swipes away the loser, the winner scores, and a new challenger from another fact stream slides in.
- Uses spring settling and straight slide-out motion for smoother battle swipes.
- Shows source-specific Chuck, Cat, and Dog victory animations with confetti when a winner is picked.
- Highlights the current Battle Mode champion streak as the same source keeps winning.
- Tracks personal daily, weekly, and monthly winners locally.
- Separates Battle Mode, Chuck Facts, Cat Facts, and Dog Facts into dedicated tabs.
- Limits Chuck, Cat, and Dog refresh streams to 10 requests per minute each.
- Links to the latest GitHub release from a top-bar action inside the app.
- Supports a persistent light/dark theme toggle.
- Shows a local-only fact power profile for the current quote or fact.
- Supports copying and sharing the current quote or fact.
- Uses Kotlin and Jetpack Compose.
- Keeps API access behind a repository layer.
- Includes a GitHub Actions workflow that runs unit tests.

## Privacy

The app requests internet access so it can fetch quotes, cat facts, and dog facts from public third-party APIs. It does not require login, payments, contacts, location, or local account access.

## Current Release

- Version: `1.3.8`
- APK SHA-256: `1e1e497165d5dfe7f923437b13802e04893720698cf34e74d4e8f02f71c22b36`

## License

MIT. See [LICENSE](LICENSE).
