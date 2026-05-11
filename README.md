# Chuck Norris Quotes Android

A small Kotlin Android app that fetches random Chuck Norris quotes, cat facts, and dog facts from public APIs, then displays them with a Jetpack Compose interface.

## Install On Android

The easiest way to install the app on your own phone is through the latest GitHub release:

1. Open `https://github.com/Yogi-Langnickel/chuck-norris-quotes-android/releases/latest` on your Android phone.
2. Download the `chuck-norris-quotes-android-1.3.4-debug.apk` asset.
3. If Android asks, allow your browser or file manager to install unknown apps.
4. Open the downloaded APK and confirm installation.

This is a debug-signed APK for personal testing. Android may show a warning because it is not distributed through Google Play.

## Install With ADB

From a machine with Android platform tools installed:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

To install a downloaded release asset:

```bash
adb install -r chuck-norris-quotes-android-1.3.4-debug.apk
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
./gradlew assembleDebug
```

The generated APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Features

- Fetches random Chuck Norris quotes from `api.chucknorris.io`.
- Rotates random cat facts between `catfact.ninja` and MeowFacts.
- Fetches random dog facts from Dog API.
- Runs a battle-first mode where the user swipes away the loser, the winner scores, and a new challenger from another fact stream slides in.
- Uses spring settling and straight slide-out motion for smoother battle swipes.
- Shows source-specific Chuck, Cat, and Dog victory animations with confetti when a winner is picked.
- Tracks personal daily, weekly, and monthly winners locally.
- Separates Battle Mode, Chuck Facts, Cat Facts, and Dog Facts into dedicated tabs.
- Limits Chuck, Cat, and Dog refresh streams to 10 requests per minute each.
- Links to the latest GitHub release from inside the app.
- Shows a visible Update app button that opens the latest release download page.
- Shows a local-only fact power profile for the current quote or fact.
- Supports copying and sharing the current quote or fact.
- Uses Kotlin and Jetpack Compose.
- Keeps API access behind a repository layer.
- Includes a GitHub Actions workflow that runs unit tests and uploads a debug APK artifact.

## Privacy

The app requests internet access so it can fetch quotes, cat facts, and dog facts from public third-party APIs. It does not require login, payments, contacts, location, or local account access.

## Current Release

- Version: `1.3.4`
- APK SHA-256: `d33036cf204aa387fd8e8d658117d1327dbe5dbc02b398799df10452d83bf312`

## License

MIT. See [LICENSE](LICENSE).
