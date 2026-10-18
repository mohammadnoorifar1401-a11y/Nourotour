# MONO v1.9.0 — Android source

MONO is a personal, offline-first Android app built with Kotlin, Jetpack Compose, Room, and Material 3. No `INTERNET` permission is declared.

## Version / identity
- Launcher name: **MONO**
- `versionCode`: **9** · `versionName`: **1.9.0**
- Android application ID: `com.mono.app`
- Minimum Android: API 26 · compile/target SDK: 35

## Build
Open this project root in Android Studio and allow Gradle Sync, or run:

```bash
# Linux/macOS
chmod +x gradlew
./gradlew :app:assembleDebug --no-daemon
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Source layout
```text
app/src/main/java/com/mono/app/
├── MonoApp.kt, MainActivity.kt
├── data/       Room models, DAOs, migrations, repositories, sample data
├── ui/         Compose screens, navigation, shared components, MONO theme
├── backup/     JSON export/restore, including folders and v1.9 fields
├── reminders/  local reminders, receivers and workers
└── util/       formatting helpers
```

## v1.9.0 features
- Charcoal/gold interface, Persian RTL, MONO branding and `MOHAMMAD NOORIFAR WORKSPACE` header.
- Emoji/gallery avatars; text-entry forms and member details.
- Favorites for members and folders; folder-specific list/detail/grid layouts and sorting.
- Room migration and JSON backup/restore for folders, favorites, view settings, member fields and tasks.
- Local reminders; offline-only operation.

## Notes
- `local.properties` is intentionally omitted from the source archive; Android Studio/Gradle will create it for the local SDK.
- `build/`, `.gradle/`, and generated APKs are omitted; Gradle recreates them.
- This is the full source project, not a prebuilt APK. The earlier APK accidentally retained YadYar branding/package identity; this source has the MONO app label and `com.mono.app` identity.
