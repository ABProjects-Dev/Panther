# Panther

Panther is an offline-first Android application for creating and managing personal bodybuilding workout plans. It is not a workout tracking application. Persian is the primary interface language, rendered RTL with the Vazirmatn typeface and Jalali (Solar Hijri) calendar.

## Technology

Kotlin, Jetpack Compose and Material 3, Clean Architecture with MVVM, Navigation Compose, Hilt, Room, Kotlin coroutines and Flow, and Preferences DataStore. The project uses a single app module, Gradle Kotlin DSL, and a version catalog. Minimum Android version is API 26; compile and target SDK are API 36.

## Core capabilities

- Edit a local profile (name, age, height and weight) with automatic BMI calculation and classification on the dashboard.
- View a read-only Home dashboard displaying BMI, active workout program (highlighted in green), tabbed training days, and per-set visual square targets.
- Create, edit, delete, and toggle active status across multiple workout programs via a compact Programs list, with at most one active program at any time.
- Manage training days in separate tabs, add/edit/delete exercises, select duration targets from a searchable dropdown list, and reorder exercise cards via Drag & Drop with persistent Room storage.
- Render all displayed dates uniformly in the Persian Jalali calendar (`YYYY/MM/DD`) while storing epoch timestamps in SQLite.
- Export/import validated JSON backups and export programs as PDF using user-selected locations.

No workout sessions, recorded performance, history, archive, profile photos, timers, accounts or cloud synchronization are in scope. See [product specification](docs/PRODUCT_SPECIFICATION.md).

## Architecture and structure

One Android application module with Compose presentation, domain models/use cases and a Room-backed repository. Hilt provides dependencies; DataStore stores theme choice. See [architecture](docs/ARCHITECTURE.md) and [roadmap](docs/ROADMAP.md).

```text
app/src/main/java/com/panther/app/
├── core/          # design system (Vazirmatn, colors, components), Jalali date format, navigation, theme preferences
├── data/          # Room entities, DAO, database, backup codec, PDF exporter and repository
├── di/            # Hilt bindings
├── domain/        # models, BMI & duration catalog, validation, repository contract, use cases
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

The application features a three-destination navigation shell (`Home`, `Programs`, `Profile`, with `Settings` reachable from `Profile`); a read-only Home dashboard with BMI calculation, green active-program highlighting, tabbed day views, and square per-set indicators; compact program cards with toggle switches and a bottom-left floating creation button; dedicated program pages with tabbed training days, drag-and-drop exercise reordering, and searchable duration dropdowns; uniform Jalali date rendering; bundled Vazirmatn fonts; custom fitness launcher/header icons; persisted theme selection; validated JSON backup/restore; and per-program PDF export. Android automatic backup remains disabled.

## GitHub Actions

Android CI runs on pull requests targeting `main` only; pushes to `main` and manual dispatch do not start a run (owner rule, `AGENTS.md` section 19.1). It builds a debug APK, runs unit tests and lint, and runs instrumented tests on an API 29 emulator (documentation-only changes skip the emulator). Download `panther-debug-apk` from the CI run's artifacts; it is a debug-signed build, not a release APK. Test reports and the Room schema are uploaded when available.

Two workflows are defined:

- `ci.yml` — lint, unit tests, debug APK, Room schema baseline check and instrumentation tests.
- `release.yml` — builds, signs and verifies the official APK for `v*.*.*` tags, then creates a draft GitHub Release that the owner publishes manually.

See [docs/GITHUB_ACTIONS.md](docs/GITHUB_ACTIONS.md) for the authoritative description, including the workflows that were removed on 2026-11-07 and the consequences of their removal.

Artifacts are retained for 7 days (CI) and 30 days (release) and GitHub expires them automatically; no scheduled cleanup job is needed.

## License

MIT — see [LICENSE](LICENSE). Security policy: [SECURITY.md](SECURITY.md).
