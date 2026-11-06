# Panther Project Releases

## Official Releases (Rule 12)
Releases are managed via GitHub Actions (`release.yml`) and tagged with Semantic Versioning (Rule 9).

### v1.0.0 (Planned)
- **Date**: TBD
- **Tag**: `v1.0.0`
- **Artifact**: `panther-official-release-v1.0.0.apk`
- **Notes**: First stable release with full CI/CD support.
- **Signature**: Verified via GitHub Secrets (Rule 13).

### No published releases yet
- No Git tags and no GitHub Releases exist at the time of writing.
- `versionName` is `0.1.0` in `app/build.gradle.kts`, but this is a pre-release baseline that
  has never been tagged or built with a green CI run.
- The first published release will be created by `release.yml` and must be approved by the
  project owner (AGENTS.md section 17).

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
