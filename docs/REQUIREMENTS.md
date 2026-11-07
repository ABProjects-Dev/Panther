# Panther Project Requirements Specification

## Document History
- **Version**: 1.1.0-draft
- **Date**: 2026-11-07
- **Status**: Active

## System Requirements
- **Android SDK**: 36 (Compile/Target), 26 (Min)
- **Kotlin**: 2.0.21 (JVM target 17)
- **Gradle Version**: 8.11.1
- **Java Version**: 17 (Temurin)
- **Compose**: Enabled

## Functional Requirements (Rule 4)
1. **Workout Plan Management**
   - Programs, tabbed Training Days, drag-and-drop reorderable Exercises, and per-set Targets (repetitions or searchable duration dropdown).
   - Single-active-program invariant controlled via toggle switches and highlighted in green.
   - Read-only Home dashboard with square visual set indicators.
   - Uniform Persian Jalali calendar formatting over epoch-millisecond persistence.
2. **Profile & Dashboard Metrics**
   - Local profile data (name, age, height, weight) without profile photos, plus theme preferences.
   - Automatic BMI calculation and classification on the dashboard.
3. **Backup, Restore & PDF Export**
   - JSON codec with version and structural validation (Rule 13, 14), backward-compatible with v1 backups.
   - Per-program RTL PDF export rendered with Vazirmatn and Jalali dates.
4. **GitHub Actions**
   - CI workflow (`ci.yml`) and Release workflow (`release.yml`).

## Non-Functional Requirements
1. **Security**
   - Keystore outside repo (Rule 13).
   - Secrets in GitHub Actions (Rule 13).
   - Vulnerability alerts come from GitHub's dependency graph, which an owner must
     enable under *Settings → Code security → Dependency graph*; it is currently
     disabled, so no automated advisory gate is active.
   - Dependency and Action version updates are performed manually and validated by
     `ci.yml`; no automated update bot is configured.
2. **Performance**
   - Efficient flows and coroutines.
   - Bounded artifact retention (7 days for CI artifacts, 30 days for release
     artifacts) (Rule 16).
3. **Maintainability**
   - Clean architecture (Rule 14).
   - English technical docs, Persian reports (Rule 2, 3).
4. **Compliance**
   - Follow AGENTS.md rules (Rule 1).
   - Semantic Versioning (Rule 9).

## Change Log References
- See `CHANGELOG.md` for version history.
- See `RELEASES.md` for official releases.

Last Updated: 2026-11-07
Language: English (Rule 3)
