# Panther Project Changelog

## [Unreleased]

### Added
- **2026-11-07**: Bundled official `Vazirmatn` font family (`Regular`, `Medium`,
  `SemiBold`, `Bold`) across the Compose Material 3 typography, Android theme and PDF
  exporter, alongside a custom fitness dumbbell/crest launcher and header icon.
- **2026-11-07**: Dashboard BMI calculation and classification (`BmiCalculator`) derived
  from user height and weight.
- **2026-11-07**: Tabbed training-day views, drag-and-drop exercise reordering persisted in
  Room, visual square set boxes (`ExerciseSetsSquares`), and a searchable duration select
  picker (`DurationCatalog`).
- **2026-11-07**: Exact Gregorian-to-Jalali (Solar Hijri) calendar formatting in
  `PantherDateFormat` with unit tests, preserving epoch-millisecond database storage.
- **2026-11-07**: Owner operating rules recorded in `AGENTS.md` section 19.1: no pull
  request and no merge without an explicit request, a single `dev` branch, CI only after
  a pull request, and a release that starts only when the owner sends the word `tag`.
  The same rules are summarised in `docs/GITHUB_ACTIONS.md`.

### Changed
- **2026-11-07**: Renamed application display title to `Panther` across launcher, headers,
  dashboard and onboarding.
- **2026-11-07**: Converted `HomeScreen` to a read-only dashboard highlighting the active
  program in green; redesigned `ProgramsScreen` with compact cards, active toggle switches,
  and a bottom-left creation FAB; moved program rename and delete actions inside
  `ProgramEditorScreen`.
- **2026-11-07**: `ci.yml` runs on the `pull_request` event only. The `push` to `main`
  trigger and `workflow_dispatch` were removed, so there is no post-merge health check
  and no manual run (owner rule, `AGENTS.md` section 19.1).
- **2026-11-07**: Closed the four unsuccessful Dependabot pull requests (#16, #17, #19,
  #26; every one of them failed CI) and deleted all extra branches, leaving only `main`
  and the current working branch. The pre-squash history stays reachable on GitHub under
  `refs/pull/28/head`.
- **2026-11-07**: Documentation updated to describe the two remaining workflows and to
  state the consequences of the removal: `docs/GITHUB_ACTIONS.md` (rewritten),
  `README.md`, `RELEASES.md`, `docs/ARCHITECTURE.md`, `docs/PROJECT_PLAN.md`,
  `docs/REQUIREMENTS.md`, `docs/RELEASE_GUIDE.md` and `docs/SECRETS_SETUP.md`.
- **2026-11-07**: Corrected the documented CI triggers: `ci.yml` runs on pull requests
  targeting `main`, on pushes to `main` and on manual dispatch, not on pushes to any
  branch.

### Removed
- **2026-11-07**: Removed the History screen/navigation route, program archiving, Home
  "New Program" button, Home "Recent Programs" section, card-level Edit/Delete buttons,
  and profile photo upload/display while keeping Room schema v1 intact.
- **2026-11-07**: Deleted `.github/dependabot.yml`. Dependency versions and GitHub
  Action versions are now updated manually. Nothing else changes: Dependabot
  security alerts were never active here because they depend on the repository
  setting *Dependency graph*, which is disabled (the SBOM endpoint answers 404),
  not on this configuration file. Re-enabling that setting would restore alerts
  even without the file.
- **2026-11-07**: Deleted the `build.yml` (release candidate), `security.yml`
  (dependency review and dependency-graph submission) and `cleanup.yml` (artifact
  retention) workflows. The pipeline is now `ci.yml` for validation and
  `release.yml` for official releases. Consequences: no per-merge unsigned release
  candidate and no scheduled artifact cleanup (GitHub expires artifacts at their own
  retention: 7 days for CI artifacts, 30 days for release artifacts). The removed
  dependency review was already advisory because the repository setting
  *Dependency graph* is disabled.

## [v1.0.0] - 2026-11-07

First stable release. The application builds, is signed, and ships with a green
CI pipeline (unit tests, lint, debug APK, and instrumentation tests on an emulator).

### Fixed
- **2026-11-06**: Restored `PantherBackupJson.kt`, which had been flattened into three
  lines with escaped newlines and no longer compiled.
- **2026-11-06**: Backup codec now reads and writes the program identifier under the same
  JSON key (`id`); previously every valid import was rejected.
- **2026-11-06**: `restorePendingBackup` no longer deletes the pending backup before the
  transaction commits and no longer leaks a copied profile image on failure.
- **2026-11-06**: Unified Persian date formatting across screens and PDF export.
- **2026-11-06**: Enabled edge-to-edge handling for `targetSdk 36`.
- **2026-11-06**: Backup size is now checked in bytes, matching the import path.

### Changed
- **2026-11-06**: Release builds enable R8 minification and resource shrinking with
  ProGuard rules; release signing is wired through `signingConfigs`.
- **2026-11-06**: Backup shape limits reduced so a maximum backup stays inside the heap
  budget alongside the 8 MB file cap.
- **2026-11-06**: Consolidated CI workflows (removed the duplicate `android-ci.yml` and
  `cleanup-artifacts.yml`), fixed invalid `setup-android` inputs, replaced the
  non-existent dependency-check action, and switched artifact cleanup to a 30-day
  window that preserves release artifacts.
- **2026-11-06**: Removed unused use case, dead UI state fields and unused dependencies.
- **2026-11-06**: Documentation corrected to match the code (Kotlin version, repository
  API, workflow triggers, release status).

### Added
- **2026-11-06**: Application launcher icon (adaptive, monochrome-capable).
- **2026-11-06**: `LICENSE`, `SECURITY.md`, `.gitattributes` and Dependabot configuration.
- **2026-11-06**: JVM unit tests for the backup codec and backup limits, plus Room
  testing dependencies for migration tests.

- **2026-11-05**: Implemented standard GitHub Actions (CI, Security, Build, Release, Cleanup) (Rule 11, 12).
- **2026-11-05**: Fixed error handling, contentResolver safety, and backup JSON decoding (Rule 10, 13, 14, 18).
- **2026-11-05**: Updated AGENTS.md with permanent project development instructions (Rule 1, 3).

## [v0.1.0] - Initial Release (Baseline)
- Initial project setup with Android SDK 36, Kotlin 17, and Jetpack Compose.
- Basic workout management, profile management, and backup/restore features.
- Initial repository structure and README.md.

## Semantic Versioning (Rule 9)
- MAJOR.MINOR.PATCH
- v1.0.0: First stable release.
- v1.1.0: Backward-compatible feature addition.
- v1.1.1: Backward-compatible bug fix.
- v2.0.0: Incompatible changes.

Language: English (Rule 3)
