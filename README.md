# Panther

> [نسخهٔ فارسی (Persian README)](.github/README.md)

Panther is an offline-first Android application for creating and managing personal bodybuilding workout plans. It is not a workout tracking application. Persian is the primary interface language, rendered RTL with the Vazirmatn typeface and Jalali (Solar Hijri) calendar.

## Technology

Kotlin 2.0.21, Jetpack Compose and Material 3, Clean Architecture with MVVM, Navigation Compose, Hilt, Room, Kotlin coroutines and Flow, and Preferences DataStore. The project uses a single app module, Gradle Kotlin DSL, and a version catalog. Minimum Android version is API 26; compile and target SDK are API 36.

## Core capabilities

- Edit a local profile (name, age, height and weight) with automatic BMI calculation and classification on the dashboard.
- View a read-only Home dashboard displaying BMI, active workout program (highlighted in green), tabbed training days, and per-set visual square targets.
- Create, edit, delete, and toggle active status across multiple workout programs via a compact Programs list, with at most one active program at any time.
- Organize and reorder training days and exercises: manage days in dedicated tabs with left/right day reordering, add/edit/delete exercises, select duration targets from a searchable dropdown list, and reorder exercise cards via Drag & Drop (with edge auto-scroll and TalkBack accessibility actions) persisted in Room.
- Render all displayed dates uniformly in the Persian Jalali calendar (`YYYY/MM/DD`) while storing epoch timestamps in SQLite.
- Export/import validated JSON backups and export individual programs as RTL PDF documents using user-selected locations.

No workout sessions, recorded performance, history, archive, profile photos, timers, accounts or cloud synchronization are in scope. See [product specification](docs/PRODUCT_SPECIFICATION.md).

## Architecture and structure

One Android application module with Compose presentation, domain models/use cases and a Room-backed repository. Hilt provides dependencies; DataStore stores theme choice. See [architecture](docs/ARCHITECTURE.md) and [roadmap](docs/ROADMAP.md).

```text
app/src/main/java/com/panther/app/
├── core/          # design system (Vazirmatn, colors, components), Jalali date format, navigation, theme preferences
├── data/          # Room entities, DAO, database, backup codec, SAF helper, PDF exporter and repository
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

The Gradle wrapper uses Gradle 9.8.2. Configure the SDK through Android Studio or `ANDROID_HOME`/local `local.properties` (never commit it).

## Build, test and install

From the repository root:

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest  # requires a running device or emulator
./gradlew :app:installDebug               # requires a connected device or emulator
```

On Windows use `gradlew.bat`. Alternatively open the root directory in Android Studio, sync Gradle, select the app configuration, and run it on a device. Generated APKs follow the `Panther Vx.y.z.apk` naming convention (for example, `app/build/outputs/apk/debug/Panther V0.1.0.apk` for local debug builds and `app/build/outputs/apk/release/Panther Vx.y.z.apk` for tagged releases).

## Current status

The application features a three-destination navigation shell (`Home`, `Programs`, `Profile`, with `Settings` reachable from `Profile` and active-tab pop-to-root); a read-only Home dashboard with BMI calculation, green active-program highlighting, tabbed day views, and square per-set indicators; compact program cards with toggle switches and a bottom-left floating creation button; dedicated program pages with reorderable tabbed training days, drag-and-drop exercise reordering, and searchable duration dropdowns; uniform Jalali date rendering; bundled Vazirmatn fonts; custom fitness launcher/header icons; dynamic edge-to-edge system bar icon contrast; persisted theme selection; validated JSON backup/restore; and per-program PDF export. Android automatic backup remains disabled.

## GitHub Actions

Android CI runs on pull requests targeting `main` only; pushes to `main` and manual dispatch do not start a run (owner rule, `AGENTS.md` section 19.1). It builds the debug and androidTest APKs, runs unit tests and lint, verifies the committed Room schema baseline, and runs instrumented tests on an API 29 emulator (documentation-only changes skip the emulator).

Two workflows are defined (with actions pinned to immutable commit SHAs):

- `ci.yml` — lint, unit tests, debug & androidTest APK assembly, Room schema baseline check and instrumentation tests (1-day retention for debug APK, 3-day retention for reports).
- `release.yml` — validates SemVer tag format and `origin/main` ancestry, runs unit tests and lint, builds and signs `Panther Vx.y.z.apk` from the exact tag commit, verifies APK Signature Scheme v2/v3 via `apksigner`, generates `Panther Vx.y.z.apk.sha256`, and publishes the GitHub Release directly (`draft: false`) including the Persian annotated tag description.

See [docs/GITHUB_ACTIONS.md](docs/GITHUB_ACTIONS.md) for the authoritative description.

## License

MIT — see [LICENSE](LICENSE). Security policy: [SECURITY.md](SECURITY.md).
