# Panther Project Releases

## Official Releases (Rule 12)
Releases are managed via GitHub Actions (`release.yml`) and tagged with Semantic Versioning (Rule 9).

### v1.0.0
- **Date**: 2026-11-07
- **Tag**: `v1.0.0`
- **Artifact**: `panther-official-release-v1.0.0.apk`
- **Notes**: First stable release with full CI/CD support.
- **Signature**: Verified via GitHub Secrets (Rule 13).
- **Status**: Built and signed by `release.yml`; the GitHub Release is created as a
  draft and published manually by the project owner.

### Release history
- `v1.0.0` is the first tagged release. Earlier commits had no tags and no GitHub
  Releases; `v0.1.0` was a pre-release baseline that was never tagged.

## Release Policy (Rule 12)
- Use Git tags for releases.
- Never reuse an existing release tag.
- Preserve historical releases.
- Upload only verified build artifacts.
- Never upload signing keys or passwords.

## Maintenance
- Temporary artifacts are cleaned by `cleanup.yml` after the retention window.
- Artifacts named `panther-official-release-*` are always preserved.
- Official releases are kept permanently in GitHub Releases.

Language: English (Rule 3)
