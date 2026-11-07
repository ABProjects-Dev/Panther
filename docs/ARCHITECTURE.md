# Panther Project Architecture

## Overview
The Panther project is an Android application built with Kotlin, Jetpack Compose, and Hilt. It follows a clean architecture pattern with Domain, Data, and Presentation layers.

## Layers

### 1. Presentation Layer
- **Components**: Composable screens, ViewModels, Navigation.
- **Technology**: Jetpack Compose, Hilt, Kotlin Coroutines.
- **Location**: `app/src/main/java/com/panther/app/feature/`, `app/src/main/java/com/panther/app/core/`.

### 2. Domain Layer
- **Components**: Use cases, Models, Validation logic.
- **Technology**: Kotlin, Coroutines.
- **Location**: `app/src/main/java/com/panther/app/domain/`.

### 3. Data Layer
- **Components**: Repository, DAO (Room), Backup JSON codec, DataStore.
- **Technology**: Room, KSP, DataStore, Android SDK 36.
- **Location**: `app/src/main/java/com/panther/app/data/`.

## Build System
- **Gradle**: Version 8.11.1
- **Android Gradle Plugin**: Integrated via `libs.plugins`
- **Kotlin**: 2.0.21 (JVM target 17)
- **Compose**: Enabled
- **SDK**: Compile/Target 36, Min 26

## GitHub Actions Architecture
- **CI**: Lint, unit tests, debug APK, Room schema baseline check and instrumentation tests (ci.yml)
- **Release**: Signed official release on `v*.*.*` tags, published as a draft GitHub Release (release.yml)

The release candidate, security scan and artifact cleanup workflows were removed on 2026-11-07; see
[docs/GITHUB_ACTIONS.md](GITHUB_ACTIONS.md) for the consequences.

## Security (Rule 13)
- Keystore and Secrets managed via GitHub Secrets.
- No sensitive data in repository.

## Maintenance
- Artifacts expire automatically at their retention (7 days for CI artifacts, 30 days for release
  artifacts); no scheduled cleanup workflow is needed.
- Room schemas are exported to `app/schemas`; keep them committed and add migrations on change.

Last Updated: 2026-11-05
Language: English (Rule 3)
