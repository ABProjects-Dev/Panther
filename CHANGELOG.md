# Panther Project Changelog

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
