# Panther Project Releases

## Official Releases (`[REL-08]`)
Releases are managed via GitHub Actions (`release.yml`) and tagged with Semantic Versioning (`[REL-03]`) using Persian annotated tag descriptions (`AGENTS.project.md`). `PD-002` — the conflict with `[REL-09]`, which
asks for English — was decided by the owner on 2026-11-11 in favour of Persian, so the deviation is
recorded here rather than pending.

### v1.2.0
- **Date**: 2026-11-11
- **Tag**: `v1.2.0` (annotated in Persian)
- **Workflow Artifact**: `panther-official-release-v1.2.0`
- **Release Assets**: built, verified (`aapt2` badging, `apksigner` v2/v3) and published by `release.yml` from the tagged commit as `Panther V1.2.0.apk` and `Panther V1.2.0.apk.sha256`
- **Notes**: Restore now writes a safety snapshot to `filesDir/pre-restore` before replacing data; backup and single-program PDF exports render into a cache file and only then copy to the picked document; `release.yml` refuses to reuse a tag that already has a release; the Vazirmatn SIL OFL 1.1 text ships inside the APK; duration labels come from `strings.xml` so no user-facing Persian text lives in the domain; `editor/{id}` shows a dedicated "program not found" screen for a deleted or malformed id; the test suite is split into 57 unit and 16 instrumented tests with JaCoCo coverage and build-failure annotations in `ci.yml`; runtime dependencies refreshed.
- **Signature**: Signed and verified with `apksigner` (APK Signature Scheme v2/v3) in `release.yml` via GitHub Secrets (`[SEC-07]`).
- **Status**: Published directly on GitHub Releases (`draft: false`) by `release.yml` once the tag is pushed.

### v1.1.0
- **Date**: 2026-11-08
- **Tag**: `v1.1.0` (annotated in Persian)
- **Workflow Artifact**: `panther-official-release-v1.1.0`
- **Release Assets**: built as `Panther V1.1.0.apk` (`1,788,609` bytes) and `Panther V1.1.0.apk.sha256` (`85` bytes); GitHub serves them as `Panther.V1.1.0.apk` and `Panther.V1.1.0.apk.sha256` because it replaces spaces with dots in uploaded asset names
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
