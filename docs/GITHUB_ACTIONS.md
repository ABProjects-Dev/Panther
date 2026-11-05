# GitHub Actions Documentation for Panther Project

## Overview
The Panther project uses a standardized, secure, and maintainable GitHub Actions architecture for Android development with Kotlin.

## Workflows

### 1. ci.yml (Continuous Integration)
- **Trigger**: Push/PR to `main`, `develop`, `feature/*`, `fix/*`, `hotfix/*`; Manual dispatch.
- **Purpose**:
  - Check structure and Lint (`check-structure-and-lint`).
  - Run Unit Tests (`unit-tests`).
  - Build Debug APK (`build-debug-apk`).
  - Run Instrumentation Tests (`instrumentation-tests`).
- **Artifacts**: `panther-debug-apk`, `unit-test-reports`, `android-check-reports`, `instrumentation-reports` (Retention: 1 day).
- **Branch Protection**: CI success is a prerequisite for merging to `main` and `develop`.

### 2. security.yml (Security Scan)
- **Trigger**: Push/PR to `main`, `develop`; Weekly schedule (Monday).
- **Purpose**:
  - Dependency Vulnerability Check (OWASP Dependency Check).
  - License Compliance Check.
- **Artifacts**: SARIF reports, License reports (Retention: 1 day).

### 3. build.yml (Build Release Candidate)
- **Trigger**: Push to `develop`; Manual dispatch.
- **Purpose**:
  - Build Release APK (Unsigned/Test) for internal testing.
  - Verify APK version and package name.
- **Artifacts**: `panther-release-candidate-apk` (Retention: 1 day).

### 4. release.yml (Official Release)
- **Trigger**: Git Tag matching `v*.*.*` (Semantic Versioning).
- **Purpose**:
  - Validate Tag format.
  - Build and Sign Release APK using GitHub Secrets.
  - Verify signature.
  - Create GitHub Release and attach APK.
- **Secrets Required**:
  - `KEYSTORE_PASSWORD`
  - `KEY_ALIAS`
  - `KEY_PASSWORD`
  - `KEYSTORE_BASE64` (Encoded Keystore file)
- **Artifacts**: Official APK attached to Release (Permanent).

### 5. cleanup.yml (Cleanup)
- **Trigger**: Daily schedule (00:00 UTC); Manual dispatch.
- **Purpose**:
  - Delete temporary artifacts older than 24 hours.
  - Preserve official release artifacts.

## Branch Strategy
- `main`: Stable versions (Manual merge only).
- `develop`: Development branch (CI/Build triggers).
- `feature/*`: New functionality.
- `fix/*`: Bug fixes.
- `hotfix/*`: Immediate fixes for stable versions.

## Secrets Setup
1. Go to Repository Settings -> Secrets and variables -> Actions.
2. Add the following secrets:
   - `KEYSTORE_PASSWORD`: Password for the keystore.
   - `KEY_ALIAS`: Alias of the signing key.
   - `KEY_PASSWORD`: Password for the key alias.
   - `KEYSTORE_BASE64`: Base64 encoded content of the `.jks` or `.keystore` file.

## Release Guide
1. Ensure all CI checks pass on `develop` or `main`.
2. Create a Git Tag: `git tag vX.Y.Z` (e.g., `v1.0.0`).
3. Push the tag: `git push origin vX.Y.Z`.
4. The `release.yml` workflow will automatically trigger, build, sign, and publish the official release.
5. Verify the release on GitHub Releases page.

## Troubleshooting
- **Build Failure**: Check `android-ci.yml` or `ci.yml` logs for compilation errors.
- **Security Failure**: Check `security.yml` SARIF reports for vulnerability details.
- **Release Failure**: Ensure Secrets are correctly set and Tag format is `vX.Y.Z`.

## Storage Policy
- Temporary Artifacts: 1 day retention (cleaned by `cleanup.yml`).
- Official Releases: Permanent (stored in GitHub Releases).
