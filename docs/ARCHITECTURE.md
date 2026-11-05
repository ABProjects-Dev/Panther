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
- **Kotlin**: Version 17
- **Compose**: Enabled
- **SDK**: Compile/Target 36, Min 26

## GitHub Actions Architecture
- **CI**: Build, Test, Lint (ci.yml)
- **Security**: Dependency/License scan (security.yml)
- **Build**: Release Candidate (build.yml)
- **Release**: Signed Official Release (release.yml)
- **Cleanup**: Artifact management (cleanup.yml)

## Security (Rule 13)
- Keystore and Secrets managed via GitHub Secrets.
- No sensitive data in repository.

## Maintenance (Rule 16)
- Artifacts cleaned daily (cleanup.yml).
- Workspace cleanup required after tasks.

Last Updated: 2026-11-05
Language: English (Rule 3)
