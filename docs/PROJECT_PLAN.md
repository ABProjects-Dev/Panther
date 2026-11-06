# Panther Project Plan

## Current Phase: Stabilization and CI/CD Implementation
- **Status**: In progress (2026-11-06) — CI workflow defects fixed, first green run pending
- **Actions**:
  - Updated AGENTS.md (Rule 1).
  - Fixed code errors and security issues (Rule 10, 13, 14).
  - Implemented GitHub Actions workflows (Rule 11, 12).
  - Created documentation (Rule 3, 4).

## Next Phase: Feature Development and Release
- **Goal**: Implement new features in `feature/*` branches.
- **Process**:
  1. Develop the feature on a `feature/*` branch.
  2. Run CI and security checks; both must be green.
  3. Open a pull request to `main` and merge after review.
  4. `build.yml` produces a release candidate from `main`.
  5. Tag and release (owner approval required).

## Long-term Goals
- Maintain code quality and stability (Rule 14).
- Ensure security and sensitive information protection (Rule 13).
- Preserve project history and data (Rule 16, 20).
- Regular cleanup of artifacts (Rule 16).

## Branch Strategy

`AGENTS.md` section 6 is authoritative:

- `main`: stable, approved code.
- `feature/*`: new features.
- `fix/*`: bug fixes.
- `refactor/*`: internal improvements.
- `docs/*`: documentation-only changes.

There is no `develop` or `hotfix/*` branch; earlier references to them were incorrect.

## Documentation Policy (Rule 3)
- All technical docs in English.
- Progress reports in Persian/RTL (Rule 2).

Last Updated: 2026-11-05
Language: English (Rule 3)
