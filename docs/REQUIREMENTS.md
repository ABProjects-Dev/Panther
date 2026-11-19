# Panther Project Requirements Specification

## Document History
- **Version**: 1.2.0
- **Date**: 2026-11-19
- **Status**: Active

## System Requirements
- **Android SDK**: 36 (Compile/Target), 26 (Min)
- **Kotlin**: 2.0.21 (JVM target 17)
- **Gradle Version**: 9.8.2
- **Java Version**: 17 (Temurin)
- **Compose**: Enabled

## Functional Requirements (`[DOC-15]`)
1. **Workout Plan Management**
   - Programs whose name is chosen on the program page after the room is created (leaving without a
     name discards the room), one independent card and screen per Training Day, and per-set Targets
     (repetitions or searchable duration) shown as small numbered squares marked `R` or `S`.
   - A new exercise pre-fills three sets of 12 repetitions (`WorkoutDefaults`).
   - Exercise ordering is locked by default; a "reorder mode" toggle enables drag-and-drop, and moved
     or inserted exercises are renumbered from the persisted dense `sortOrder`.
   - A device-only exercise-name catalogue backs the name field: typing filters it, and a new name has
     an inline "add to catalogue" action. The catalogue is excluded from backups.
   - Symmetric capacity limits enforced across creation and backup (`100` programs, `50` days/program, `50` exercises/day, `30` sets/exercise).
   - Single-active-program invariant controlled via toggle switches and highlighted in green.
   - Read-only Home dashboard with square visual set indicators and the same per-day cards/screens.
   - Uniform Persian Jalali calendar formatting over epoch-millisecond persistence.
2. **Profile & Dashboard Metrics**
   - Local profile data (name, age, height, weight) without profile photos, plus theme preferences (`DARK`, `LIGHT`, `SYSTEM`) synchronized with system bar icon contrast.
   - Automatic BMI calculation and classification on the dashboard.
3. **Navigation and Screen Chrome**
   - Every nested page carries an icon-only back action at the top-right (RTL top end).
   - Destructive actions are full-width buttons at the end of their page, behind a confirmation step.

4. **Backup, Restore & PDF Export**
   - JSON codec (`PantherBackupJson`) with version, byte-size (`8 MB`), and structural validation (`[SEC-02]`, `[DATA-03]`), backward-compatible with v1 backups.
   - Single-program RTL PDF export rendered with Vazirmatn typography and Jalali dates.
5. **GitHub Actions**
   - Pull-request CI workflow (`ci.yml`) and tag-verified Release workflow (`release.yml`) with SHA-pinned actions.

## Non-Functional Requirements
1. **Security**
   - Keystore outside repo and credentials supplied via GitHub Secrets (`[SEC-07]`).
   - All GitHub Actions pinned to immutable 40-character commit SHAs.
   - Official APKs verified with Android `apksigner` (APK Signature Scheme v2/v3) and published with `SHA-256` checksums.
   - Vulnerability alerts come from GitHub's dependency graph (*Settings → Code security → Dependency graph*, currently disabled). Dependency and Action version updates are performed manually in accordance with `AGENTS.project.md`.
2. **Performance**
   - Memoized reactive Room `Flow` subscriptions and `@Relation` day-exercise-set queries without N+1 UI overhead.
   - Bounded artifact retention (`1` day for CI debug APKs, `3` days for CI reports, `30` days for release artifacts) (`AGENTS.project.md` — artifact retention).
3. **Maintainability**
   - Clean layered architecture (`[DOC-08]`).
   - English technical docs, Persian RTL user reports (`[REP-05]`, `[DOC-13]`).
4. **Compliance**
   - Follow `AGENTS.md` and `AGENTS.project.md` (`[R-01]`).
   - Semantic Versioning (`[REL-03]`).

## Change Log References
- See `CHANGELOG.md` for version history.
- See `RELEASES.md` for official releases.

Last Updated: 2026-11-19
Language: English (`[DOC-13]`)
