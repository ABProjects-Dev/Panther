# Panther

Panther is an offline-first Android application for creating and managing personal bodybuilding workout plans. It is not a workout tracking application. Persian is the primary language.

## Technology

Kotlin, Jetpack Compose and Material 3, Clean Architecture with MVVM, Navigation Compose, Hilt, Room, Kotlin coroutines and Flow, and Preferences DataStore. The project uses a single app module, Gradle Kotlin DSL, and a version catalog. Minimum Android version is API 26; compile and target SDK are API 36.

## Planned MVP

- Edit a local profile (name, photo, age, height and weight).
- Create, edit, delete, activate, deactivate and archive multiple programs, with at most one active at a time.
- Organize and reorder training days and exercises, with independent repetition or duration targets for every set.
- View a dashboard and access Home, Programs, History and Profile; access settings from an appropriate screen.
- Export/import validated JSON backups and export programs as PDF using user-selected locations.

No workout sessions, recorded performance, timers, accounts or cloud synchronization are in scope. See [product specification](docs/PRODUCT_SPECIFICATION.md).

## Architecture and structure

One Android application module with logical presentation, domain and data layers. Dependencies point toward domain; Hilt connects implementations. See [architecture](docs/ARCHITECTURE.md) and [roadmap](docs/ROADMAP.md).

```text
app/src/main/java/com/panther/app/
├── PantherApplication.kt
├── MainActivity.kt
└── core/
    ├── designsystem/theme/  # colors, typography, shapes and spacing
    └── navigation/          # initial navigation host
```

Data, domain, feature and DI packages will be introduced when they contain real implementations rather than empty placeholders.

## Prerequisites

- Android Studio supporting AGP 8.10.1 and Android SDK 36.
- JDK 17 and Android SDK Platform 36 installed via SDK Manager.
- Android device/emulator running API 26 or newer.
- Network access for the initial Gradle distribution and dependency download. The app itself requires no internet.

The Gradle wrapper uses Gradle 8.11.1. Configure the SDK through Android Studio or `ANDROID_HOME`/local `local.properties` (never commit it).

## Build, test and install

From the repository root:

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest  # requires a running device or emulator
./gradlew :app:installDebug               # requires a connected device or emulator
```

On Windows use `gradlew.bat`. Alternatively open the root directory in Android Studio, sync Gradle, select the app configuration, and run it on a device. The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Current status

Repository and initial application foundation only: launcher, Persian-first RTL presentation, a basic dark/light theme API, Hilt setup, and a single foundation navigation destination. There is no theme picker or persistence yet; dark is the default. Room, DataStore and the other libraries are configured but product data and features are not yet implemented. Android automatic backup is disabled until a deliberate backup strategy is implemented. See the roadmap for subsequent work.
