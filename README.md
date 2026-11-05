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

One Android application module with Compose presentation, domain models/use cases and a Room-backed repository. Hilt provides dependencies; DataStore stores theme choice. See [architecture](docs/ARCHITECTURE.md) and [roadmap](docs/ROADMAP.md).

```text
app/src/main/java/com/panther/app/
├── core/          # design system, navigation and theme preferences
├── data/          # Room entities, DAO, database and repository
├── di/            # Hilt bindings
├── domain/        # models, validation, repository contract, use cases
├── feature/       # Compose screens and ViewModels
├── MainActivity.kt
└── PantherApplication.kt
```

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

The initial implementation includes the four-destination shell, dashboard, program creation/rename/activation/deactivation/archive/delete and basic details, local profile editing with Photo Picker, and persisted theme selection. Room models support days, exercises and independent per-set targets; day/exercise editing UI is not implemented yet. Program history is a list of programs, not workout tracking. Backup, import and PDF export are not implemented. Android automatic backup remains disabled pending a deliberate backup strategy.

**Verification is pending:** the current development environment has no JDK or Android SDK, so the implementation, build and tests have not been verified. Room schema export is configured; commit the generated schema after the first successful build. See the roadmap for follow-up work.
