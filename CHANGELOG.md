# Panther Project Changelog

## [Unreleased]

### Added
- `AGENTS.project.md` — project rules (stack, commands, conventions, stricter rules, paths, off-limits)
  that pin the agent baseline `AGENTS.md` 1.0.0 and declare the Mobile profile.
- `docs/PROJECT_STATUS.md` — dated project state, confirmed defects, next tasks, pending decisions and
  validation status.
- `docs/TESTING.md` — authoritative test commands, suite inventory and known limitations.
- `docs/decisions/ADR-0001-adopt-agent-operating-baseline.md` and
  `ADR-0002-defer-baseline-policy-conflicts.md`.
- `CONTRIBUTING.md` — contribution workflow, change hygiene and documentation-language rules.
- Unit coverage for previously untested pure logic: `DurationCatalogTest` (18 tests) and
  `BmiCalculatorTest` (9 tests).

### Changed
- Documentation now cites the operating baseline by stable rule ID (`[SEC-07]`, `[REL-03]`, …) instead
  of the removed `Rule N` / `section 19.1` numbering; 44 references across 13 files were repointed,
  historical entries below were left unchanged on purpose.
- `README.md` gained a documentation index pointing at the new files.
- Backup and PDF exports render into a cache file first and are copied into the chosen document
  afterwards, so a failure during generation cannot leave a truncated file.
- Duration labels ("۳۰ ثانیه (۰۰:۳۰)") are now built from `strings.xml` in the UI layer; the domain
  model carries data only and `DurationCatalog.search` takes an optional label resolver, so no
  user-facing Persian text lives in the domain anymore ([UI-03]).

### Fixed
- Readers following a documentation link no longer land on rule numbers that do not exist in
  `AGENTS.md` (documentation-only defect, no app behaviour change).
- Restoring a backup now writes a best-effort snapshot of the current data to
  `filesDir/pre-restore/backup-before-restore.json` first, so an accidental restore can still be
  undone. The snapshot never blocks a restore the user confirmed.
- Debug builds report `0.0.0-local` instead of `0.1.0`, so a local APK can no longer be mistaken for
  a released version.

### Removed
- `app/src/main/res/values-fa/strings.xml` — byte-identical duplicate of `values/strings.xml`; one
  resource set is now the single source of UI text.

### Security
- The Vazirmatn SIL OFL 1.1 text now ships inside the APK
  (`app/src/main/assets/licenses/vazirmatn-OFL-1.1.txt`) and is referenced from `README.md`.
- `.github/CODEOWNERS` and a `Protected file guard` workflow fail any pull request that modifies
  `AGENTS.md`.
- `release.yml` refuses to publish when a GitHub Release already exists for the tag, keeping published
  versions and their artifacts immutable.
- Nothing changed in the app's runtime behaviour. Note for the next release: `PD-001`–`PD-003` in
  `docs/PROJECT_STATUS.md` must be decided before tagging.

## [v1.1.0] - 2026-11-08

Feature, UX, accessibility, data-integrity, and CI/release hardening release tagged on `main`
with a Persian annotated tag description and published directly as `Panther V1.1.0.apk`.

### Added
- Bundled official `Vazirmatn` font family (`Regular`, `Medium`, `SemiBold`, `Bold`) across
  Compose Material 3 typography, Android theme, and PDF export, alongside a custom fitness
  launcher and header icon.
- Dashboard BMI calculation and classification (`BmiCalculator`) derived from user height and
  weight.
- Tabbed training-day views with day reordering controls, drag-and-drop exercise reordering
  persisted in Room (with edge auto-scroll and Persian `TalkBack` custom reorder actions),
  visual square set boxes (`ExerciseSetsSquares`), and a searchable duration select picker
  (`DurationCatalog`).
- Exact Gregorian-to-Jalali (Solar Hijri) calendar formatting in `PantherDateFormat` with unit
  tests, preserving epoch-millisecond database storage.
- Persian GitHub landing-page README (`.github/README.md`) alongside the root English
  `README.md`, plus permanent owner operating rules in `AGENTS.md` section 19.1.
- Expanded JVM unit and Android emulator instrumentation regression test suites (including
  `ProgramsViewModelTest`, `MigrationTestHelper` Room schema baseline check, single-program
  `%PDF-` snapshot check, and bottom-navigation active-tab pop-to-root check).

### Changed
- Renamed application display title to `Panther` across launcher, headers, dashboard, and
  onboarding, and standardized generated APK filenames to `Panther Vx.y.z.apk` (with companion
  `Panther Vx.y.z.apk.sha256` checksums).
- Converted `HomeScreen` to a read-only dashboard highlighting the active program in green;
  redesigned `ProgramsScreen` with compact cards, active toggle switches, and an accessible
  bottom-left creation Extended FAB; moved program rename and delete actions inside
  `ProgramEditorScreen`.
- Configured `ci.yml` to run strictly on `pull_request` targeting `main` and configured
  `release.yml` to publish GitHub Releases directly (`draft: false`) with Persian annotated tag
  descriptions, full 40-char Action SHA pinning, environment-variable signing, `apksigner`
  v2/v3 verification, and `SHA-256` checksums.
- Improved visual hierarchy and keep-with-next heading pagination in `ProgramPdfExporter` and
  hardened DataStore error recovery in `ThemePreferences`.

### Fixed
- Compacted `sortOrder` atomically when adding or deleting training days and exercises and
  normalized backup ordering in `RoomPantherRepository`, preventing duplicate `sortOrder`
  values from breaking `PantherBackupJson.encode`.
- Enforced `MAX_PROGRAMS`, `MAX_DAYS_PER_PROGRAM`, `MAX_EXERCISES_PER_DAY`, and
  `MAX_SETS_PER_EXERCISE` symmetrically across `WorkoutValidation.sets`,
  `RoomPantherRepository`, and editor UI controls.
- Preserved `editingExerciseId` and exercise dialog state via `rememberSaveable` across screen
  rotation and activity recreation so editing an exercise never creates a duplicate entry.
- Memoized Room `Flow` bindings in Compose screens and added a transactional `ExerciseWithSets`
  Room relation query to eliminate per-frame recomposition query storms during drag-and-drop.
- Added single-program `createProgramBackup(programId)` for PDF export and `SafeUriOutput`
  fallback from `"wt"` to `"w"` with channel truncation for third-party Android SAF providers.
- Synchronized edge-to-edge status and navigation bar icon contrast with the selected in-app
  theme (`DARK`, `LIGHT`, `SYSTEM`) on `targetSdk 36`, synchronized `values-fa/strings.xml`,
  and enabled active bottom-navigation tabs to pop nested screens back to their tab root.

### Removed
- Removed the History screen/navigation route, program archiving, Home "New Program" button,
  Home "Recent Programs" section, card-level Edit/Delete buttons, profile photo upload/display,
  unused repository mutation methods, `.github/dependabot.yml`, and the auxiliary `build.yml`,
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

## Semantic Versioning (`[REL-03]`)
- `MAJOR.MINOR.PATCH`
- `v1.0.0`: First stable release.
- `v1.1.0`: Backward-compatible feature addition.
- `v1.1.1`: Backward-compatible bug fix.
- `v2.0.0`: Incompatible changes.

Language: English (`[DOC-13]`)
