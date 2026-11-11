# Release Guide for Panther Project

## Versioning & APK Naming Strategy
- **Semantic Versioning (SemVer)**: `MAJOR.MINOR.PATCH` (e.g., `v1.0.0`, `v1.0.1`, `v1.0.2`), with `MINOR` and `PATCH` in `0..99`.
- **versionCode**: Derived automatically from the release tag in CI (`MAJOR * 10000 + MINOR * 100 + PATCH`); local default is `1`.
- **versionName**: Derived automatically from the release tag (`v1.2.3` -> `1.2.3`).
- **APK Naming Convention (`AGENTS.project.md`)**: Output APKs follow `[AppName] Vx.y.z.apk` — specifically `Panther Vx.y.z.apk` (e.g., `Panther V1.0.2.apk` and `Panther V1.0.2.apk.sha256`).

## Release Process

### Step 1: Pre-Release Documentation & Merge (`AGENTS.project.md`, `[SYNC-03]`)
1. Update `CHANGELOG.md`, `RELEASES.md`, `README.md`, `.github/README.md`, and affected files in `docs/` for the upcoming release version **before** tagging.
2. Include these documentation updates in the release Pull Request, verify `ci.yml` passes if code/build/workflow files changed (documentation-only PRs skip `ci.yml` via `paths-ignore`), and merge the Pull Request into `main` upon explicit owner approval so the commit to be tagged on `main` already contains finalized documentation.
3. Ensure the four signing secrets (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) are configured in GitHub Secrets (see `docs/SECRETS_SETUP.md`). For local release builds outside CI, either set `RELEASE_STORE_FILE` / `RELEASE_STORE_PASSWORD` / `RELEASE_KEY_ALIAS` / `RELEASE_KEY_PASSWORD` in your shell environment or provide an untracked `app/signing.properties` file.

### Step 2: Tagging (`AGENTS.project.md`, `[REL-04]`)
1. When the project owner sends `tag`, present the next patch, minor, and major version options based on the latest tag and wait for explicit selection.
2. Once the owner selects a version (and after ensuring Step 1's documentation updates for that version are merged into `main`), create and push an annotated tag pointing to the target commit on `main`, writing the tag description in **Persian**:
   ```bash
   git tag -a v1.0.2 -m "انتشار نسخه ۱.۰.۲ — بهبود پایداری، امنیت و تجربهٔ کاربری"
   git push origin refs/tags/v1.0.2
   ```

### Step 3: Automated Build, Verification & Direct Publication
`release.yml` triggers automatically and performs:
1. SemVer format validation (`MINOR, PATCH <= 99`) and verification that the tag commit is an ancestor of `origin/main`.
2. Checkout of `refs/tags/<tag>` and execution of `:app:testDebugUnitTest :app:lintDebug`.
3. Signed release assembly (`:app:assembleRelease`) producing `Panther Vx.y.z.apk` with environment-variable credentials and guaranteed keystore cleanup.
4. Badging check via `aapt2`, APK Signature Scheme v2/v3 verification via `apksigner verify`, and generation + verification of `Panther Vx.y.z.apk.sha256`.
5. **Direct publication** (`draft: false`) of the GitHub Release named `Panther Vx.y.z`, including the Persian annotated tag description and attaching `Panther Vx.y.z.apk` and `Panther Vx.y.z.apk.sha256`.

## Artifact Retention
- Official Release APKs (`Panther Vx.y.z.apk`) and `.sha256` checksums are retained permanently in GitHub Releases.
- Workflow artifacts expire automatically (`1` day for CI debug APKs, `3` days for CI reports, `30` days for `panther-official-release-*`).

Language: English.
