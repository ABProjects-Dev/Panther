# Panther Project Changelog

## [Unreleased]

### Fixed
- **2026-11-07**: Compacted `sortOrder` atomically when adding or deleting training days and
  exercises and normalized backup ordering in `RoomPantherRepository`, preventing duplicate
  `sortOrder` values from breaking `PantherBackupJson.encode` (`L1-01`).
- **2026-11-07**: Enforced `MAX_PROGRAMS`, `MAX_DAYS_PER_PROGRAM`, `MAX_EXERCISES_PER_DAY`,
  and `MAX_SETS_PER_EXERCISE` symmetrically across `WorkoutValidation.sets`,
  `RoomPantherRepository`, and editor UI controls (`L1-02`).
- **2026-11-07**: Preserved `editingExerciseId` and exercise dialog state via
  `rememberSaveable` across screen rotation and activity recreation so editing an exercise
  never creates a duplicate entry (`L1-05`).
- **2026-11-07**: Hardened `release.yml` to check out `refs/tags/<tag>` explicitly on manual
  dispatch, verify `origin/main` tag ancestry and `0..99` SemVer minor/patch bounds, run unit
  tests and lint before signing, pass signing credentials via environment variables with
  `always()` keystore cleanup, verify APK Signature Scheme v2/v3 with `apksigner`, attach
  `.apk.sha256` checksums, scope `contents: write` to the publish job only, and pin all
  GitHub Actions in `ci.yml` and `release.yml` to full commit SHAs (`L1-03`, `L1-04`).
- **2026-11-07**: Memoized Room `Flow` bindings in Compose screens and added a transactional
  `ExerciseWithSets` Room relation query to eliminate per-frame recomposition query storms
  during drag-and-drop (`L2-01`).
- **2026-11-07**: Added single-program `createProgramBackup(programId)` for PDF export and
  eliminated intermediate UTF-8 `ByteArray` duplication during backup JSON decoding (`L2-02`).
- **2026-11-07**: Synchronized edge-to-edge status and navigation bar icon contrast with the
  selected in-app theme (`DARK`, `LIGHT`, `SYSTEM`) on `targetSdk 36` and removed deprecated
  bar color attributes from `styles.xml` (`L2-03`).
- **2026-11-07**: Deduplicated backup status/error messages in `SettingsScreen`, surfaced PDF
  export feedback directly in `ProgramEditorScreen`, added auto-clearing error resets, and
  enabled active bottom-navigation tabs to pop nested screens back to their tab root (`L2-04`).
- **2026-11-07**: Wired training-day reordering controls in `ProgramEditorScreen`, added
  `LazyColumn` edge auto-scroll during exercise drag-and-drop, expanded drag handle touch
  targets to `48.dp`, and added Persian `TalkBack` labels and custom reorder actions (`L2-05`).
- **2026-11-07**: Added `SafeUriOutput` fallback from `"wt"` to `"w"` with channel truncation
  for third-party Android SAF document providers (`L2-06`).

### Changed
- **2026-11-07**: Exposed `app/schemas` to `androidTest` assets for `MigrationTestHelper`,
  ensured `ci.yml` runs instrumentation tests when `ci.yml` itself changes, reduced CI
  artifact retention (`1` day for debug APKs, `3` days for test/lint reports), and expanded
  JVM unit and instrumented test suites (`L2-07`, `L2-08`).
- **2026-11-07**: Improved visual hierarchy and keep-with-next heading pagination in
  `ProgramPdfExporter` and hardened DataStore error recovery in `ThemePreferences`
  (`L3-07`, `L3-08`).
- **2026-11-07**: Updated `.gitattributes` binary rules (`*.ttf`, `*.jks`, `*.keystore`),
  added feature-request and security-link issue templates, trimmed unnecessary
  `kotlin.Metadata` keep rules from `proguard-rules.pro`, and synchronized all technical
  documentation with Gradle `9.8.2` (`L3-03`, `L3-05`, `L3-06`).

### Removed
- **2026-11-07**: Removed obsolete Persian `AUDIT_REPORT_FA.html` from repository root and
  removed unused `addExercise`, `renameExercise`, and `replaceSets` repository methods
  (`L3-01`, `L3-02`).

## [v1.0.1] - 2026-11-07

Feature and UX overhaul tagged on `main` and built as a signed draft GitHub Release.

### Added
- **2026-11-07**: Bundled official `Vazirmatn` font family (`Regular`, `Medium`,
  `SemiBold`, `Bold`) across Compose Material 3 typography, Android theme, and PDF export,
  alongside a custom fitness launcher and header icon.
- **2026-11-07**: Dashboard BMI calculation and classification (`BmiCalculator`) derived
  from user height and weight.
- **2026-11-07**: Tabbed training-day views, drag-and-drop exercise reordering persisted in
  Room, visual square set boxes (`ExerciseSetsSquares`), and a searchable duration select
  picker (`DurationCatalog`).
- **2026-11-07**: Exact Gregorian-to-Jalali (Solar Hijri) calendar formatting in
  `PantherDateFormat` with unit tests, preserving epoch-millisecond database storage.
- **2026-11-07**: Owner operating rules recorded in `AGENTS.md` section 19.1.

### Changed
- **2026-11-07**: Renamed application display title to `Panther` across launcher, headers,
  dashboard, and onboarding.
- **2026-11-07**: Converted `HomeScreen` to a read-only dashboard highlighting the active
  program in green; redesigned `ProgramsScreen` with compact cards, active toggle switches,
  and a bottom-left creation FAB; moved program rename and delete actions inside
  `ProgramEditorScreen`.
- **2026-11-07**: Configured `ci.yml` to run strictly on `pull_request` targeting `main`
  (owner rule, `AGENTS.md` section 19.1).

### Removed
- **2026-11-07**: Removed the History screen/navigation route, program archiving, Home
  "New Program" button, Home "Recent Programs" section, card-level Edit/Delete buttons,
  profile photo upload/display, `.github/dependabot.yml`, and the auxiliary `build.yml`,
  `security.yml`, and `cleanup.yml` workflows.

## [v1.0.0] - 2026-11-07

First stable release. The application builds, is signed, and ships with a green
CI pipeline (unit tests, lint, debug APK, and instrumentation tests on an emulator).

### Fixed
- **2026-11-06**: Restored `PantherBackupJson.kt`, aligned program `id` JSON key across
  encode/decode, fixed atomic `restorePendingBackup`, unified Persian date formatting,
  enabled edge-to-edge handling for `targetSdk 36`, and enforced byte-level backup size caps.

### Changed
- **2026-11-06**: Enabled R8 minification, resource shrinking, and release signing configuration;
  consolidated CI workflows and aligned documentation.

### Added
- **2026-11-06**: Adaptive launcher icon, `LICENSE`, `SECURITY.md`, `.gitattributes`, backup
  codec/limit unit tests, and Room testing dependencies.

## [v0.1.0] - Initial Release (Baseline)
- Initial project setup with Android SDK 36, Kotlin 2.0.21 (JVM target 17), and Jetpack Compose.
- Basic workout management, profile management, and backup/restore features.

## Semantic Versioning (Rule 9)
- `MAJOR.MINOR.PATCH`
- `v1.0.0`: First stable release.
- `v1.1.0`: Backward-compatible feature addition.
- `v1.1.1`: Backward-compatible bug fix.
- `v2.0.0`: Incompatible changes.

Language: English (Rule 3)
