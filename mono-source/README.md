# MONO Android build (separate from the Nourotour website)

This directory contains the user's MONO v1.9.0 Android project. It was added with the user's explicit permission to publish the source on this public repository and build a fresh-install APK here. Existing website files are not part of this Android project and are not changed by its build.

## Source provenance

- Original archive: `MONO-v1.9.0-source.zip` (105,205 bytes, 57 project files).
- Archive SHA-256: `f7ed0f12c38a16b29e84da4cce64b14f947faca9ab0dc1d3da438f54814daf9c`.
- Project directory: `MONO-v1.9.0/`.
- Application ID: `com.mono.app`.
- Version: `1.9.0`, versionCode `9`.
- Minimum Android: 8.0 / API 26; compile and target SDK: 35.
- Build toolchain: JDK 17, Gradle 8.10.2, Android Gradle Plugin 8.7.3.

## Cloud build

`.github/workflows/build-mono-apk.yml` builds this project on GitHub Actions when the MONO files or its workflow are pushed to `arena/2d3419e0-nourotour`. There is no website deployment step in this workflow.

The workflow validates the Gradle wrapper, runs `:app:assembleDebug`, verifies the resulting APK's signature and application identity, checks that no INTERNET permission is declared, and uploads `MONO-v1.9.0-APK`. That artifact contains:

- `MONO-v1.9.0-debug.apk` — a signed, installable APK for personal testing/use.
- `SHA256SUMS.txt` — APK SHA-256 checksum.
- `apk-metadata.txt` and `signature-verification.txt` — verification results.

Artifacts are retained for 14 days. Download and keep the APK if you want to install it later. The app itself does not expire after 14 days.

This is a debug-signed build for a **fresh installation**, not an app-store release. It is not guaranteed to update an existing installation signed with another key. No previous signing key or personal credentials are included. Future cloud builds can use a different debug signing key, so do not assume they will install as updates without additional signing setup. Back up any data before uninstalling an existing version.

Building and inspecting the APK does not replace testing the application on a physical Android device.
