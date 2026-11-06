# GitHub Actions Documentation for Panther Project

## Overview

Panther uses five focused workflows. Duplicate workflows were removed so the same
build is not executed twice per push. Every workflow uses `concurrency` groups and
least-privilege `permissions`.

## Workflows

### 1. `ci.yml` — Continuous Integration
- **Trigger:** push to `main`, `feature/*`, `fix/*`, `refactor/*`, `docs/*`;
  pull requests to `main`; manual dispatch.
- **Jobs:** `lint-and-unit-tests` → `build-debug-apk` → `instrumentation-tests`.
- **Artifacts:** `panther-debug-apk`, `android-check-reports`, `instrumentation-reports`
  (retention: 7 days).

### 2. `security.yml` — Security Scan
- **Trigger:** push/PR to `main`, weekly schedule (Monday 03:00 UTC), manual dispatch.
- **Jobs:** `dependency-vulnerability-check` using the official
  `dependency-check/DependencyCheck@v6` action (OWASP), SARIF output published to
  GitHub code scanning via `github/codeql-action/upload-sarif@v3`.
- **Fails** the build on CVSS ≥ 7.

### 3. `build.yml` — Release Candidate
- **Trigger:** push to `main` (Markdown/docs-only changes ignored), manual dispatch.
- **Purpose:** produce an unsigned release APK and verify its package/version metadata.

### 4. `release.yml` — Official Release
- **Trigger:** tag push matching `v*.*.*`, or manual dispatch with an existing tag.
- **Jobs:** `validate-tag` → `build-and-sign-release` → `publish-release`.
- **Tag drives versioning:** `vX.Y.Z` becomes `versionName=X.Y.Z` and
  `versionCode=X*10000+Y*100+Z`, passed to Gradle as `-PreleaseVersionName` /
  `-PreleaseVersionCode`.
- **Signing:** `app/signing.properties` and `app/release.jks` are generated from secrets at
  build time only; `app/build.gradle.kts` wires them into a `release` signing config.
- **Secrets:** `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
- The GitHub Release is created as a **draft** and must be published manually by the owner.

### 5. `cleanup.yml` — Artifact Cleanup
- **Trigger:** daily at 00:00 UTC, manual dispatch.
- Deletes artifacts older than the retention window (720 hours / 30 days) and always
  skips `panther-official-release-*`.

## Branch Strategy

`AGENTS.md` section 6 is authoritative:

- `main`: stable, approved code.
- `feature/*`: new functionality.
- `fix/*`: bug fixes.
- `refactor/*`: internal improvements.
- `docs/*`: documentation-only changes.

There is no `develop` branch; the earlier documentation and workflows referencing it were
incorrect and have been corrected.

## Release Procedure

1. Ensure `ci.yml` is green on `main`.
2. Update `CHANGELOG.md` and `docs/RELEASES`-related entries.
3. Create and push the tag: `git tag vX.Y.Z && git push origin vX.Y.Z`.
4. `release.yml` builds, signs and verifies the APK, then creates a draft release.
5. Review the draft, then publish it manually (owner approval is required by
   `AGENTS.md` section 17).

## Troubleshooting

- **`setup-android` fails:** this action accepts `packages`, not `api-level`/`build-tools`;
  SDK components are installed with an explicit `sdkmanager` step.
- **Signing failure:** verify the four secrets exist and `KEYSTORE_BASE64` is valid Base64.
- **Release candidate fails:** inspect the `assembleRelease` output; minification is enabled
  in release builds, so keep ProGuard rules current.

Language: English.
