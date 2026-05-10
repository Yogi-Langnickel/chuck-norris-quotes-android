# Chuck Norris Quotes Android

A small Kotlin Android app that fetches random Chuck Norris quotes and cat facts from public APIs, then displays them with a Jetpack Compose interface.

## Install On Android

The easiest way to install the app on your own phone is through the latest GitHub release:

1. Open `https://github.com/Yogi-Langnickel/chuck-norris-quotes-android/releases/latest` on your Android phone.
2. Download the `chuck-norris-quotes-android-1.0.0-debug.apk` asset.
3. If Android asks, allow your browser or file manager to install unknown apps.
4. Open the downloaded APK and confirm installation.

This first release is a debug-signed APK for personal testing. Android may show a warning because it is not distributed through Google Play.

## Install With ADB

From a machine with Android platform tools installed:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

To install a downloaded release asset:

```bash
adb install -r chuck-norris-quotes-android-1.0.0-debug.apk
```

## Build Locally

Requirements:

- Android Studio with the Android SDK installed.
- JDK 24.
- An Android device or emulator running Android 8.0 or newer.

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
- Fetches random cat facts from `catfact.ninja`.
- Shows a local-only fact power profile for the current quote or fact.
- Supports copying and sharing the current quote or fact.
- Uses Kotlin and Jetpack Compose.
- Keeps API access behind a repository layer.
- Includes a GitHub Actions workflow that runs unit tests and uploads a debug APK artifact.

## Privacy

The app requests internet access so it can fetch quotes and cat facts from public third-party APIs. It does not require login, payments, contacts, location, or local account access.

## Current Release

- Version: `1.0.0`
- APK SHA-256: `bfa81bee3aabcf567a3f28a5497141293d94171d01c406dd9a711c1434a3866c`

## License

MIT. See [LICENSE](LICENSE).
