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

The MVP includes the four-destination shell and dashboard; local profile editing with Photo Picker; program creation, editing, activation, archive and confirmed deletion; training-day and exercise editing/reordering; independent repetition or duration targets per set; persisted theme selection; validated JSON backup/restore; and per-program PDF export. Program history is a list of programs, not workout tracking. Backup files are chosen through Android's Storage Access Framework; importing validates the complete file and asks before replacing local data. Profile images are included in backup files with a 5 MB image limit. Android automatic backup remains disabled.

**Verification status:** the CI pipeline is green on
`arena/acfba818-panther`: `:app:testDebugUnitTest`, `:app:lintDebug` and
`:app:assembleDebug` pass (16 unit tests, 0 failures), the debug APK is produced, and the
instrumentation tests run on an API 29 emulator. `app/schemas` holds the Room schema
baseline (version 1), and CI fails if a schema change is not committed.

## GitHub Actions

Android CI runs on pull requests, pushes to any branch, and manual dispatch. It builds a debug APK, runs unit tests and lint, and runs instrumented tests on an API 29 emulator. Download `panther-debug-apk` from the build run's artifacts; it is a debug-signed build, not a release APK. Test reports are uploaded when available.

Five workflows are defined: `ci.yml` (lint, unit tests, debug APK, instrumentation),
`security.yml` (dependency review and dependency-graph submission), `build.yml` (release candidate from `main`),
`release.yml` (signed release on `v*.*.*` tags) and `cleanup.yml` (artifact retention).
See [docs/GITHUB_ACTIONS.md](docs/GITHUB_ACTIONS.md) for the authoritative description.

Artifact cleanup runs daily at 00:00 UTC and can also be triggered manually. It deletes
artifacts older than 30 days (matching the longest workflow retention) and never deletes
`panther-official-release-*` artifacts. Scheduled workflows only run from the default
branch.

## License

MIT — see [LICENSE](LICENSE). Security policy: [SECURITY.md](SECURITY.md).
