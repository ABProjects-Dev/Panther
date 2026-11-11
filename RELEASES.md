# Panther Project Releases

## Official Releases (`[REL-08]`)
Releases are managed via GitHub Actions (`release.yml`) and tagged with Semantic Versioning (`[REL-03]`) using Persian annotated tag descriptions (`AGENTS.project.md`; conflicts with `[REL-09]`, see `PD-002` in `docs/PROJECT_STATUS.md`).

### v1.1.0
- **Date**: 2026-11-08
- **Tag**: `v1.1.0` (annotated in Persian)
- **Workflow Artifact**: `panther-official-release-v1.1.0`
- **Release Assets**: `Panther V1.1.0.apk` (`1,788,609` bytes) and `Panther V1.1.0.apk.sha256` (`85` bytes)
- **Notes**: Complete RTL UI/UX redesign with bundled Vazirmatn typography, Jalali calendar dates, read-only active-program Home dashboard with BMI calculation, reorderable tabbed training days, drag-and-drop exercise ordering with TalkBack actions, searchable duration picker, Room data/backup integrity hardening, SAF fallback, single-program PDF export refinements, and hardened CI/release automation.
- **Signature**: Signed and verified with `apksigner` (APK Signature Scheme v2/v3) in `release.yml` via GitHub Secrets (`[SEC-07]`).
- **Status**: Published directly on GitHub Releases (`draft: false`).

### v1.0.0
- **Date**: 2026-11-07
- **Tag**: `v1.0.0`
- **Workflow Artifact**: `panther-official-release-v1.0.0`
- **Release Asset**: `app-release.apk` (`1,439,269` bytes)
- **Notes**: First stable release with full CI/CD support.
- **Signature**: Verified via GitHub Secrets (`[SEC-07]`).
- **Status**: Published on GitHub Releases.

### Release history & current standard
- `v1.0.0` is the first tagged release (`v0.1.0` was an untagged pre-release baseline).
- Starting with `v1.1.0` (per `AGENTS.project.md`):
  - Annotated release tags (`git tag -a vX.Y.Z`) carry **Persian** tag descriptions.
  - Signed release APKs follow the `[AppName] Vx.y.z` naming convention (`Panther Vx.y.z.apk` and `Panther Vx.y.z.apk.sha256`), verified with `apksigner` (APK Signature Scheme v2/v3).
  - Once the owner approves the release tag, `release.yml` publishes the GitHub Release **directly** (`draft: false`).

## Release Policy (`[REL-02]`, `[REL-04]`, `AGENTS.project.md`)
- Initiate releases only after explicit owner approval (`tag` workflow).
- Use annotated Git tags on `main` with Persian descriptions.
- Never reuse an existing release tag without explicit owner instruction.
- Preserve historical releases unless the owner explicitly orders removal.
- Upload only verified `Panther Vx.y.z.apk` artifacts and `.sha256` checksums.
- Never upload signing keys or passwords.

## Maintenance
- Build artifacts expire automatically at their configured retention (`1` day for CI debug APKs, `3` days for CI reports, `30` days for `panther-official-release-*`).
- Official releases are kept permanently in GitHub Releases.

Language: English (`[DOC-13]`)
