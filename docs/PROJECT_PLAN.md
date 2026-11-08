# Panther Project Plan

## Current Phase: Stabilization, Hardening, and Quality Assurance
- **Status**: Active (2026-11-08) — `v1.0.0` and `v1.1.0` published directly on GitHub Releases.
- **Completed Milestones**:
  - Delivered core offline workout planning, BMI dashboard, Vazirmatn RTL design system, Jalali date formatting, JSON backup/restore, and PDF export.
  - Hardened data ordering invariants, symmetric validation limits, configuration-change state preservation, edge-to-edge system bar contrast, and TalkBack accessibility.
  - Secured `ci.yml` and `release.yml` with commit-SHA pinning, `apksigner` v2/v3 verification, `SHA-256` checksums, and bounded artifact retention.

## Development & Release Workflow (`AGENTS.md` Section 19.1)
1. Commit logical, verified changes to the single development branch (`dev`, or the platform-assigned Arena session branch).
2. Open a pull request targeting `main` **only when explicitly requested by the project owner**.
3. Verify `ci.yml` passes (unit tests, Android Lint, debug APK, Room schema baseline check, and API 29 emulator instrumentation tests), then stop and wait for explicit merge approval.
4. Merge into `main` **only when explicitly requested by the project owner**.
5. Initiate a release **only when the project owner sends `tag`**: present patch, minor, and major version options, wait for explicit owner selection, create the selected `vX.Y.Z` tag on `main` with a Persian annotated tag description, and verify `release.yml` publishes `Panther Vx.y.z` directly (`draft: false`).

## Long-term Goals
- Maintain offline-first reliability, strict data integrity, and zero-permission privacy (Rule 13, 14).
- Keep Room schema migrations tested via `MigrationTestHelper` whenever the database version advances.
- Keep GitHub Actions artifact storage bounded (`1` day for CI debug APKs, `3` days for CI reports, `30` days for release workflow artifacts).

## Branch Strategy
Per `AGENTS.md` section 19.1 (superseding the general prefixes in section 6):
- `main`: stable, owner-approved code.
- `dev` (or the fixed Arena session branch when running inside Arena Agent Mode): single active working branch; do not create extra branches without explicit owner direction.

## Documentation Policy (Rule 3 & Section 19.1)
- All source-controlled technical documentation, code comments, commits, and PRs in English (except `.github/README.md` and annotated release tag descriptions in Persian per `AGENTS.md` section 19.1).
- All user-facing progress and final reports in Persian (RTL) (Rule 2).

Last Updated: 2026-11-08
Language: English (Rule 3)
