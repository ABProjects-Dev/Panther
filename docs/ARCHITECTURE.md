# Panther Project Architecture

## Overview
Panther is an offline-first Android application built with Kotlin, Jetpack Compose (Material 3), Room, DataStore, and Hilt. It follows a clean layered architecture separating Presentation, Domain, and Data concerns.

## Layers

### 1. Presentation Layer
- **Components**: Composable screens (`HomeScreen`, `ProgramsScreen`, `ProgramEditorScreen`, `TrainingDayScreen`, `ProfileScreen`, `SettingsScreen`), ViewModels (`ProgramsViewModel`, `ProfileViewModel`, `SettingsViewModel`), Navigation (`PantherNavigation`), Design System (`Vazirmatn` typography, `PantherExtendedColors`, reusable components), and Jalali date formatting (`PantherDateFormat`).
- **Technology**: Jetpack Compose, Material 3, Hilt, Kotlin Coroutines & StateFlow, Edge-to-Edge `SystemBarStyle`.
- **Location**: `app/src/main/java/com/panther/app/feature/`, `app/src/main/java/com/panther/app/core/`.

### 2. Domain Layer
- **Components**: Immutable domain models (`WorkoutProgram`, `TrainingDay`, `Exercise`, `ExerciseSet`, `ExerciseWithSets`, `UserProfile`, `PantherBackup`), `BmiCalculator`, `DurationCatalog`, `WorkoutValidation`, `ExerciseNameCatalog` / `WorkoutDefaults` (pure catalogue matching and the shared defaults), `PantherRepository` interface, and use cases.
- **Technology**: Pure Kotlin and Coroutines Flow.
- **Location**: `app/src/main/java/com/panther/app/domain/`.

### 3. Data Layer
- **Components**: `RoomPantherRepository`, `PantherDatabase` & `PantherDao` (Room schema v2 with foreign-key cascades, transactional `@Relation` queries, and the additive `MIGRATION_1_2`), `PantherBackupJson` codec, `ProgramPdfExporter` (RTL A4 PDF generator with Vazirmatn fonts), `SafeUriOutput` (SAF stream helper), and `ThemePreferences` (DataStore).
- **Technology**: Room 2.6.1, KSP, Preferences DataStore, Android `PdfDocument`.
- **Location**: `app/src/main/java/com/panther/app/data/`, `app/src/main/java/com/panther/app/core/settings/`.

## Build System
- **Gradle Wrapper**: Version 9.8.2
- **Android Gradle Plugin**: 8.10.1 (via `gradle/libs.versions.toml`)
- **Kotlin**: 2.0.21 (JVM target 17)
- **Compose**: Enabled (BOM `2024.12.01`)
- **SDK**: Compile/Target 36, Min 26

## GitHub Actions Architecture
- **CI (`ci.yml`)**: Runs on pull requests targeting `main` (excluding documentation-only pull requests via `paths-ignore`) — unit tests, Android Lint, debug APK assembly, Room schema baseline verification, and API 29 emulator instrumentation tests.
- **Release (`release.yml`)**: Validates SemVer tags on `origin/main`, runs unit tests and lint, builds and signs `Panther Vx.y.z.apk` via environment-variable secrets, verifies APK Signature Scheme v2/v3 with `apksigner`, generates `Panther Vx.y.z.apk.sha256` checksums, and publishes the GitHub Release directly (`draft: false`) including the Persian annotated tag description.
- All workflow actions are pinned to full 40-character commit SHAs.

See [docs/GITHUB_ACTIONS.md](GITHUB_ACTIONS.md) for full pipeline details.

## Security (`[SEC-02]`)
- Keystore and signing credentials are stored exclusively in GitHub Secrets and passed via process environment variables during CI release builds.
- No network permissions, analytics SDKs, or sensitive credentials exist in the repository.

## Screen structure and navigation
- **Destinations**: `home`, `programs`, `profile`, `settings`, `editor/{id}` (one program) and `day/{programId}/{dayId}` (one training day). Every training day is its own screen, reached from its own card on the program page or from the read-only home dashboard; the program page no longer hosts tabs.
- **Numbering**: exercise and day numbers are derived from the persisted dense `sortOrder` (`sortOrder + 1`), so a reorder or a mid-list insert renumbers the list without a separate counter.
- **Reordering**: disabled by default. `PantherReorderToggle` on the day screen enables the drag handles and the move actions; `RoomPantherRepository.saveExercise(order = …)` inserts at a position and shifts the following exercises down.
- **Exercise-name catalogue**: `exercise_catalog` is device-only (`name`, normalized lowercase `nameKey`, `createdAt`) and is deliberately excluded from `PantherBackup`; the name field filters it as the user types and offers an inline "add" action for a new name.

## Brand assets
- One source photograph (owner-supplied panther artwork) renders every icon variant; `ADR-0004` records the sizes and the safe-zone rule.
- `<adaptive-icon>` files in `res/mipmap-anydpi-v26/` reference `@drawable/ic_launcher_background` (solid `#010202` vector), `@drawable/ic_launcher_foreground` (per-density `drawable-*dpi` PNG, artwork at 60 of the 108 dp canvas) and `@drawable/ic_launcher_monochrome` (per-density PNG whose alpha is the photo's luminance for themed icons).
- `res/mipmap-*dpi/ic_launcher.png` and `ic_launcher_round.png` are the legacy pre-API-26 rasters (artwork at 84 of 108; the round one is circular-masked).
- `res/drawable-*dpi/ic_panther_logo.png` is the in-app brand mark drawn by `PantherBrandHeader` (`28 dp`) — it has no vector counterpart.

## Repository layout notes
- `docs/decisions/` holds one ADR per consequential decision (`ADR-NNNN-<slug>.md`).
- `app/src/main/assets/licenses/` carries the verbatim third-party licence texts shipped inside the APK.
- `.github/workflows/protected-files.yml` guards `AGENTS.md`; `.github/CODEOWNERS` routes its changes to the owner.

## Maintenance
- Artifacts expire automatically (`1` day for CI debug APKs, `3` days for CI reports, `30` days for release workflow artifacts).
- Room schemas are exported to `app/schemas` and exposed to `androidTest` assets for `MigrationTestHelper`; every future schema version bump must commit its exported JSON and explicit migration.

Last Updated: 2026-11-19
Language: English (`[DOC-13]`)
