# Panther Project Releases

## Official Releases (Rule 12)
Releases are managed via GitHub Actions (`release.yml`) and tagged with Semantic Versioning (Rule 9).

### v1.0.0 (Planned)
- **Date**: TBD
- **Tag**: `v1.0.0`
- **Artifact**: `panther-official-release-v1.0.0.apk`
- **Notes**: First stable release with full CI/CD support.
- **Signature**: Verified via GitHub Secrets (Rule 13).

### v0.1.0 (Baseline)
- **Date**: 2026-11-05
- **Tag**: `v0.1.0` (Initial state)
- **Artifact**: N/A (Pre-GitHub Actions)
- **Notes**: Initial project structure and core features.

## Release Policy (Rule 12)
- Use Git tags for releases.
- Never reuse an existing release tag.
- Preserve historical releases.
- Upload only verified build artifacts.
- Never upload signing keys or passwords.

## Maintenance (Rule 16)
- Temporary artifacts cleaned daily by `cleanup.yml`.
- Official releases kept permanently in GitHub Releases.

Language: English (Rule 3)
