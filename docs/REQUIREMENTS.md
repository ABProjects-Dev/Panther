# Panther Project Requirements Specification

## Document History
- **Version**: 1.1.0
- **Date**: 2026-11-07
- **Status**: Active

## System Requirements
- **Android SDK**: 36 (Compile/Target), 26 (Min)
- **Kotlin**: 2.0.21 (JVM target 17)
- **Gradle Version**: 9.8.2
- **Java Version**: 17 (Temurin)
- **Compose**: Enabled

## Functional Requirements (Rule 4)
1. **Workout Plan Management**
   - Programs, reorderable tabbed Training Days, drag-and-drop reorderable Exercises (with edge auto-scroll and TalkBack custom actions), and per-set Targets (repetitions or searchable duration dropdown).
   - Symmetric capacity limits enforced across creation and backup (`100` programs, `50` days/program, `50` exercises/day, `30` sets/exercise).
   - Single-active-program invariant controlled via toggle switches and highlighted in green.
   - Read-only Home dashboard with square visual set indicators.
   - Uniform Persian Jalali calendar formatting over epoch-millisecond persistence.
2. **Profile & Dashboard Metrics**
   - Local profile data (name, age, height, weight) without profile photos, plus theme preferences (`DARK`, `LIGHT`, `SYSTEM`) synchronized with system bar icon contrast.
   - Automatic BMI calculation and classification on the dashboard.
3. **Backup, Restore & PDF Export**
   - JSON codec (`PantherBackupJson`) with version, byte-size (`8 MB`), and structural validation (Rule 13, 14), backward-compatible with v1 backups.
   - Single-program RTL PDF export rendered with Vazirmatn typography and Jalali dates.
4. **GitHub Actions**
   - Pull-request CI workflow (`ci.yml`) and tag-verified Release workflow (`release.yml`) with SHA-pinned actions.

## Non-Functional Requirements
1. **Security**
   - Keystore outside repo and credentials supplied via GitHub Secrets (Rule 13).
   - All GitHub Actions pinned to immutable 40-character commit SHAs.
   - Official APKs verified with Android `apksigner` (APK Signature Scheme v2/v3) and published with `SHA-256` checksums.
   - Vulnerability alerts come from GitHub's dependency graph (*Settings → Code security → Dependency graph*, currently disabled). Dependency and Action version updates are performed manually in accordance with `AGENTS.md` section 19.1.
2. **Performance**
   - Memoized reactive Room `Flow` subscriptions and `@Relation` day-exercise-set queries without N+1 UI overhead.
   - Bounded artifact retention (`1` day for CI debug APKs, `3` days for CI reports, `30` days for release artifacts) (Rule 16).
3. **Maintainability**
   - Clean layered architecture (Rule 14).
   - English technical docs, Persian RTL user reports (Rule 2, 3).
4. **Compliance**
   - Follow `AGENTS.md` rules (Rule 1).
   - Semantic Versioning (Rule 9).

## Change Log References
- See `CHANGELOG.md` for version history.
- See `RELEASES.md` for official releases.

Last Updated: 2026-11-07
Language: English (Rule 3)
