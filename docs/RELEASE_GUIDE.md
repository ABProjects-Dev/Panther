# Release Guide for Panther Project

## Versioning Strategy
- **Semantic Versioning (SemVer)**: `MAJOR.MINOR.PATCH` (e.g., `v1.0.0`).
- **versionCode**: Incremented automatically by build system or manually in `app/build.gradle.kts`.
- **versionName**: Matches the Tag (e.g., `0.1.0` -> `v1.0.0`).

## Release Process

### Step 1: Preparation
1. Ensure all features and fixes are merged into `develop` or `main`.
2. Run CI checks (`ci.yml`) and ensure they pass.
3. Update `versionCode` and `versionName` in `app/build.gradle.kts` if necessary (though Tag usually drives the release name).
4. Ensure GitHub Secrets are configured (see `SECRETS_SETUP.md`).

### Step 2: Tagging
1. Create a tag from the commit you want to release:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
2. **Note**: The tag must follow the format `v*.*.*`.

### Step 3: Automated Release
1. The `release.yml` workflow will trigger automatically on tag push.
2. It will:
   - Validate the tag format.
   - Build the signed Release APK using GitHub Secrets.
   - Verify the signature.
   - Create a GitHub Release and attach the APK.

### Step 4: Verification
1. Visit the GitHub Releases page.
2. Download the APK and verify the version and signature.
3. Check the release notes.

## Troubleshooting
- **Invalid Tag Format**: Ensure the tag starts with `v` and follows `X.Y.Z`.
- **Signing Error**: Check if Secrets (`KEYSTORE_PASSWORD`, etc.) are correct and the Keystore is valid.
- **Build Failure**: Review the logs in the `release.yml` workflow run.

## Artifact Retention
- Official Release APKs are kept permanently in GitHub Releases.
- Temporary build artifacts are cleaned up daily by `cleanup.yml`.
