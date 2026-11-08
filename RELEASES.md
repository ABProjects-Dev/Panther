# Panther Project Releases

## Official Releases (Rule 12)
Releases are managed via GitHub Actions (`release.yml`) and tagged with Semantic Versioning (Rule 9).

### v1.0.1
- **Date**: 2026-11-07
- **Tag**: `v1.0.1`
- **Workflow Artifact**: `panther-official-release-v1.0.1`
- **Release Asset**: `app-release.apk` (`1,799,505` bytes)
- **Notes**: UI/UX overhaul with bundled Vazirmatn typography, Jalali dates, read-only active-program dashboard with BMI card, tabbed training days, drag-and-drop exercise ordering, and searchable duration picker.
- **Signature**: Signed and verified in `release.yml` via GitHub Secrets (Rule 13).
- **Status**: Draft GitHub Release created (`draft: true`); awaiting manual owner review and publication per `AGENTS.md` section 17.

### v1.0.0
- **Date**: 2026-11-07
- **Tag**: `v1.0.0`
- **Workflow Artifact**: `panther-official-release-v1.0.0`
- **Release Asset**: `app-release.apk`
- **Notes**: First stable release with full CI/CD support.
- **Signature**: Verified via GitHub Secrets (Rule 13).
- **Status**: Published on GitHub Releases.

### Release history
- `v1.0.0` is the first tagged release (`v0.1.0` was an untagged pre-release baseline).
- Starting after `v1.0.1`, `release.yml` verifies APK Signature Scheme v2/v3 via `apksigner` and attaches both `app-release.apk` and `app-release.apk.sha256` to each draft GitHub Release.

## Release Policy (Rule 12)
- Use Git tags on `main` for releases.
- Never reuse an existing release tag.
- Preserve historical releases.
- Upload only verified build artifacts and SHA-256 checksums.
- Never upload signing keys or passwords.

## Maintenance
- Build artifacts expire automatically at their configured retention (`1` day for CI debug APKs, `3` days for CI reports, `30` days for `panther-official-release-*`).
- Official releases are kept permanently in GitHub Releases.

Language: English (Rule 3)
