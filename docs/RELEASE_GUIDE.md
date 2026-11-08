# Release Guide for Panther Project

## Versioning Strategy
- **Semantic Versioning (SemVer)**: `MAJOR.MINOR.PATCH` (e.g., `v1.0.0`, `v1.0.1`), with `MINOR` and `PATCH` in `0..99`.
- **versionCode**: Derived automatically from the release tag in CI (`MAJOR * 10000 + MINOR * 100 + PATCH`); local default is `1`.
- **versionName**: Derived automatically from the release tag (`v1.2.3` -> `1.2.3`).

## Release Process

### Step 1: Preparation
1. Ensure all approved changes are merged into `main` after passing `ci.yml`.
2. Update `CHANGELOG.md` and `RELEASES.md`.
3. Ensure the four signing secrets (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) are configured in GitHub Secrets (see `docs/SECRETS_SETUP.md`). For local release builds outside CI, either set `RELEASE_STORE_FILE` / `RELEASE_STORE_PASSWORD` / `RELEASE_KEY_ALIAS` / `RELEASE_KEY_PASSWORD` in your shell environment or provide an untracked `app/signing.properties` file.

### Step 2: Tagging (`AGENTS.md` Section 19.1)
1. When the project owner sends `tag`, present the next patch, minor, and major version options based on the latest tag.
2. After the owner selects a version, create and push an annotated tag pointing to the target commit on `main`:
   ```bash
   git tag -a v1.0.2 -m "Release v1.0.2"
   git push origin refs/tags/v1.0.2
   ```

### Step 3: Automated Build, Verification & Draft Release
`release.yml` triggers automatically and performs:
1. SemVer format validation (`MINOR, PATCH <= 99`) and verification that the tag commit is an ancestor of `origin/main`.
2. Checkout of `refs/tags/<tag>` and execution of `:app:testDebugUnitTest :app:lintDebug`.
3. Signed release assembly (`:app:assembleRelease`) with environment-variable credentials and guaranteed keystore cleanup.
4. Badging check via `aapt2`, APK Signature Scheme v2/v3 verification via `apksigner verify`, and generation of `app-release.apk.sha256`.
5. Creation of a **draft** GitHub Release containing `app-release.apk` and `app-release.apk.sha256`.

### Step 4: Owner Review & Publication
1. Inspect the draft release on GitHub Releases and verify `sha256sum --check app-release.apk.sha256`.
2. Publish the draft release manually **only after explicit owner approval** (`AGENTS.md` section 17).

## Artifact Retention
- Official Release APKs and `.sha256` checksums are retained permanently in GitHub Releases.
- Workflow artifacts expire automatically (`1` day for CI debug APKs, `3` days for CI reports, `30` days for `panther-official-release-*`).

Language: English.
