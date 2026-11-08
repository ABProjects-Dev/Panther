# Panther Project Architecture

## Overview
Panther is an offline-first Android application built with Kotlin, Jetpack Compose (Material 3), Room, DataStore, and Hilt. It follows a clean layered architecture separating Presentation, Domain, and Data concerns.

## Layers

### 1. Presentation Layer
- **Components**: Composable screens (`HomeScreen`, `ProgramsScreen`, `ProgramEditorScreen`, `ProfileScreen`, `SettingsScreen`), ViewModels (`ProgramsViewModel`, `ProfileViewModel`, `SettingsViewModel`), Navigation (`PantherNavigation`), Design System (`Vazirmatn` typography, `PantherExtendedColors`, reusable components), and Jalali date formatting (`PantherDateFormat`).
- **Technology**: Jetpack Compose, Material 3, Hilt, Kotlin Coroutines & StateFlow, Edge-to-Edge `SystemBarStyle`.
- **Location**: `app/src/main/java/com/panther/app/feature/`, `app/src/main/java/com/panther/app/core/`.

### 2. Domain Layer
- **Components**: Immutable domain models (`WorkoutProgram`, `TrainingDay`, `Exercise`, `ExerciseSet`, `ExerciseWithSets`, `UserProfile`, `PantherBackup`), `BmiCalculator`, `DurationCatalog`, `WorkoutValidation`, `PantherRepository` interface, and use cases.
- **Technology**: Pure Kotlin and Coroutines Flow.
- **Location**: `app/src/main/java/com/panther/app/domain/`.

### 3. Data Layer
- **Components**: `RoomPantherRepository`, `PantherDatabase` & `PantherDao` (Room schema v1 with foreign-key cascades and transactional `@Relation` queries), `PantherBackupJson` codec, `ProgramPdfExporter` (RTL A4 PDF generator with Vazirmatn fonts), `SafeUriOutput` (SAF stream helper), and `ThemePreferences` (DataStore).
- **Technology**: Room 2.6.1, KSP, Preferences DataStore, Android `PdfDocument`.
- **Location**: `app/src/main/java/com/panther/app/data/`, `app/src/main/java/com/panther/app/core/settings/`.

## Build System
- **Gradle Wrapper**: Version 9.8.2
- **Android Gradle Plugin**: 8.10.1 (via `gradle/libs.versions.toml`)
- **Kotlin**: 2.0.21 (JVM target 17)
- **Compose**: Enabled (BOM `2024.12.01`)
- **SDK**: Compile/Target 36, Min 26

## GitHub Actions Architecture
- **CI (`ci.yml`)**: Runs on pull requests targeting `main` — unit tests, Android Lint, debug APK assembly, Room schema baseline verification, and API 29 emulator instrumentation tests.
- **Release (`release.yml`)**: Validates SemVer tags on `origin/main`, runs unit tests and lint, builds and signs `Panther Vx.y.z.apk` via environment-variable secrets, verifies APK Signature Scheme v2/v3 with `apksigner`, generates `Panther Vx.y.z.apk.sha256` checksums, and publishes the GitHub Release directly (`draft: false`) including the Persian annotated tag description.
- All workflow actions are pinned to full 40-character commit SHAs.

See [docs/GITHUB_ACTIONS.md](GITHUB_ACTIONS.md) for full pipeline details.

## Security (Rule 13)
- Keystore and signing credentials are stored exclusively in GitHub Secrets and passed via process environment variables during CI release builds.
- No network permissions, analytics SDKs, or sensitive credentials exist in the repository.

## Maintenance
- Artifacts expire automatically (`1` day for CI debug APKs, `3` days for CI reports, `30` days for release workflow artifacts).
- Room schemas are exported to `app/schemas` and exposed to `androidTest` assets for `MigrationTestHelper`; every future schema version bump must commit its exported JSON and explicit migration.

Last Updated: 2026-11-07
Language: English (Rule 3)
