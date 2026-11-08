# GitHub Actions Documentation for Panther Project

## Overview

Panther uses **two** workflows: `ci.yml` validates pull requests and
`release.yml` produces the official, signed release. All third-party and
first-party actions are pinned to full 40-character commit SHAs, and both
workflows use `concurrency` groups and job-scoped least-privilege `permissions`.

## Workflows

### 1. `ci.yml` — Continuous Integration

- **Trigger:** pull requests targeting `main` only (owner rule, `AGENTS.md`
  section 19.1). Pushes to `main` and manual dispatch do **not** start a run.
- **Jobs:** `lint-and-unit-tests` → `instrumentation-tests`.
  `lint-and-unit-tests` runs `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug --stacktrace`
  and verifies `app/schemas` matches the committed Room schema baseline.
- **Artifacts:**
  - `panther-debug-apk` (retention: `1` day)
  - `android-check-reports` (retention: `3` days)
  - `instrumentation-reports` (retention: `3` days)
- **Instrumentation gating:** the API 29 emulator runs whenever source code,
  resources, Gradle build files, or `.github/workflows/ci.yml` change. Pure
  documentation/metadata edits (`docs/`, `*.md`, `.gitattributes`, issue/PR
  templates, `release.yml`, `LICENSE`, etc.) skip the emulator.
- **Permissions:** `contents: read`. Timeouts: 30 minutes (lint and unit
  tests), 45 minutes (instrumentation tests).

### 2. `release.yml` — Official Release

- **Trigger:** push of a tag matching `v*.*.*`, or `workflow_dispatch` with an
  existing `v*.*.*` tag.
- **Jobs:** `validate-tag` → `build-and-sign-release` → `publish-release`.
- **Tag & ancestry validation (`validate-tag`):**
  - Validates `vMAJOR.MINOR.PATCH` with `MINOR` and `PATCH` in `0..99` so
    `versionCode = MAJOR*10000 + MINOR*100 + PATCH` stays strictly monotonic.
  - Verifies the tag exists and is an ancestor of `origin/main`.
- **Exact tag checkout & quality gate (`build-and-sign-release`):**
  - Checks out `refs/tags/<tag>` explicitly (ensuring `workflow_dispatch` always
    builds the tagged commit rather than `HEAD` of the default branch).
  - Runs `./gradlew :app:testDebugUnitTest :app:lintDebug` before touching
    signing secrets.
- **Signing & cleanup:**
  - Decodes `KEYSTORE_BASE64` to `app/release.jks` (`0600`) and supplies
    `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, and
    `RELEASE_KEY_PASSWORD` via step environment variables (passwords are never
    written to `signing.properties` in CI).
  - Removes `app/release.jks` in an `if: always()` cleanup step immediately
    after `assembleRelease`.
- **Verification & checksums:**
  - Inspects package/version metadata with `aapt2 dump badging`.
  - Verifies Android APK Signature Scheme v2/v3 with
    `apksigner verify --verbose --min-sdk-version 26 --print-certs`.
  - Generates `app-release.apk.sha256` and verifies it prior to release upload.
- **Artifact & draft release (`publish-release`):**
  - Uploads workflow artifact `panther-official-release-<tag>` (retention:
    `30` days).
  - Creates a **draft** GitHub Release attaching both `app-release.apk` and
    `app-release.apk.sha256` (`permissions: contents: write` is scoped strictly
    to `publish-release`). Manual publication requires explicit owner approval
    (`AGENTS.md` section 17).

## Removed workflows

| Workflow | What it did | Consequence of the removal |
| --- | --- | --- |
| `build.yml` | Built an unsigned release candidate APK on pushes to `main`. | No per-merge unsigned RC; `ci.yml` validates PRs and `release.yml` builds signed tags. |
| `security.yml` | Ran `dependency-review` on PRs. | Enable *Settings → Code security → Dependency graph* on GitHub to receive passive vulnerability alerts without extra workflow runs. |
| `cleanup.yml` | Deleted artifacts older than 30 days on a schedule. | Replaced by tight per-artifact `retention-days` (`1`, `3`, and `30` days). |

## Dependency updates

`.github/dependabot.yml` remains intentionally absent in accordance with
`AGENTS.md` section 19.1 (no automatic branches or unrequested pull requests).
Dependency and GitHub Action SHA updates are performed manually and verified
through `ci.yml`.

## Operating rules set by the owner (`AGENTS.md` section 19.1)

- Do not open a pull request unless the owner directly asks for one.
- Do not merge a pull request unless the owner directly asks for the merge.
- Commit all changes to the single development branch (`dev`, or the fixed Arena
  session branch).
- CI runs only after a pull request; when green, stop and wait for owner
  approval.
- Releases start only when the owner sends the word `tag`.

Language: English.
