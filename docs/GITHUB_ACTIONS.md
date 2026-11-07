# GitHub Actions Documentation for Panther Project

## Overview

Panther uses **two** workflows: `ci.yml` validates every change and
`release.yml` produces the official, signed release. Both use `concurrency`
groups and least-privilege `permissions`.

The release candidate, security scan and artifact cleanup workflows were removed
on 2026-11-07 (see `CHANGELOG.md`) so that one pipeline validates the code and
one pipeline ships it, without duplicate Gradle runs or extra scheduled jobs.
`docs/GITHUB_ACTIONS.md` is the authoritative description of the workflows.

## Workflows

### 1. `ci.yml` — Continuous Integration

- **Trigger:** push to `main`; pull requests targeting `main`; manual dispatch.
  Pushes to short-lived branches do **not** start a run, because the pull
  request already validates that branch and a second run would be identical.
- **Jobs:** `lint-and-unit-tests` → `instrumentation-tests`. Lint, unit tests,
  the debug APK and the Room schema check share one Gradle invocation:
  `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`.
- **Artifacts** (retention: 7 days): `panther-debug-apk`,
  `android-check-reports`, `room-schema`, `instrumentation-reports`.
- **Room schema gate:** the job fails when `app/schemas` changes during the
  build, which means the committed schema baseline is stale and must be
  regenerated and committed.
- **Instrumentation gating:** the emulator starts only when the change touches
  code or resources. Documentation-only changes (`docs/`, `*.md`, `.github/`,
  `README`, `LICENSE`, `SECURITY`, `CHANGELOG`, `RELEASES`) skip it, which keeps
  the slowest step off documentation pull requests.
- **Permissions:** `contents: read`. Timeouts: 30 minutes (lint and unit
  tests), 45 minutes (instrumentation tests).

### 2. `release.yml` — Official Release

- **Trigger:** push of a tag matching `v*.*.*`, or manual dispatch with an
  existing tag of the same format.
- **Jobs:** `validate-tag` → `build-and-sign-release` → `publish-release`.
- **Tag drives versioning:** `vX.Y.Z` becomes `versionName = X.Y.Z` and
  `versionCode = X*10000 + Y*100 + Z`, passed to Gradle as
  `-PreleaseVersionName` / `-PreleaseVersionCode`. An invalid tag fails the
  first job before anything is built.
- **Signing:** `app/signing.properties` and `app/release.jks` are generated
  from secrets at build time and are never committed
  (`app/build.gradle.kts` only enables signing when those files exist).
- **Secrets:** `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`,
  `KEY_PASSWORD`. See `docs/SECRETS_SETUP.md`.
- **Verification:** `aapt2 dump badging` for package, version code and version
  name, then `jarsigner -verify` for the signature.
- **Artifact:** `panther-official-release-<tag>` (retention: 30 days).
- **Publishing:** the GitHub Release is created as a **draft** and must be
  published manually by the project owner (AGENTS.md section 17).
- **Permissions:** `contents: write`. Concurrency never cancels a running
  release.

## Removed workflows

| Workflow | What it did | Consequence of the removal |
| --- | --- | --- |
| `build.yml` (Build Release Candidate) | Built an unsigned release APK on every push to `main` and verified its metadata with `aapt2`. | No per-merge release candidate. `ci.yml` still produces a debug APK; `release.yml` produces the signed release APK from a tag. |
| `security.yml` (Security Scan) | Ran `dependency-review` on pull requests and published the Gradle dependency graph. | No automated dependency review in the pipeline. Note: the gate was already advisory, because the repository setting *Settings → Code security → Dependency graph* is disabled (the SBOM endpoint returns 404). Re-enabling that setting restores Dependabot vulnerability alerts even without the workflow. |
| `cleanup.yml` (Cleanup Old Artifacts) | Deleted artifacts older than 30 days, never touching `panther-official-release-*`. | No scheduled deletion job. GitHub still expires artifacts automatically at their own retention (7 days for CI artifacts, 30 days for release artifacts), so storage stays bounded. |

## Branch Strategy

`AGENTS.md` section 6 is authoritative:

- `main`: stable, approved code.
- `feature/*`: new functionality.
- `fix/*`: bug fixes.
- `refactor/*`: internal improvements.
- `docs/*`: documentation-only changes.

There is no `develop` branch.

## Release Procedure

1. Ensure `ci.yml` is green on `main`.
2. Update `CHANGELOG.md` and the release notes.
3. Create and push the tag: `git tag vX.Y.Z && git push origin vX.Y.Z`.
4. `release.yml` builds, signs and verifies the APK, then creates a draft
   release.
5. Review the draft, then publish it manually (owner approval is required by
   `AGENTS.md` section 17).

## Troubleshooting

- **No Android SDK setup step is needed:** `ubuntu-latest` runners already ship
  the Android SDK. `android-actions/setup-android@v3` was removed because it
  fails on this runner image and masked the real Gradle failure.
- **Room schema check fails:** `app/schemas` is out of date. Run a build, then
  commit the regenerated schema as the new baseline.
- **Signing failure:** verify the four secrets exist and `KEYSTORE_BASE64` is
  valid Base64.
- **Release fails:** inspect the `assembleRelease` output; minification is
  enabled in release builds, so keep ProGuard rules current.

Language: English.
