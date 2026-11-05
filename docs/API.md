# Panther Project API Reference

## Overview
This document provides technical details for the Panther Android application APIs, including internal domain models, repository interfaces, and GitHub Actions integration.

## Domain Models (Rule 3)
- **UserProfile**: Name, age, height, weight, profile image URI.
- **ProgramBackup**: Name, creation time, active/archived status, days.
- **DayBackup**: Name, order, exercises.
- **ExerciseBackup**: Name, order, sets.
- **SetBackup**: Number, type (Reps/Duration/Seconds), values.

## Repository Interfaces
- **PantherRepository**:
  - `observePrograms()`, `observeDays()`, `observeExercises()`, `observeSets()`, `observeProfile()`.
  - `saveProfile()`, `createProgram()`, `updateProgram()`, `deleteProgram()`, `activateProgram()`, `archiveProgram()`.
  - `addDay()`, `renameDay()`, `deleteDay()`, `reorderDays()`.
  - `saveExercise()`, `deleteExercise()`, `reorderExercises()`.

## ViewModels
- **ProgramsViewModel**: Manage workout data flows and operations.
- **ProfileViewModel**: Manage profile and image operations.
- **SettingsViewModel**: Manage theme and backup/restore operations.

## GitHub Actions API (Rule 11)
- **ci.yml**: Triggers on Push/PR; runs tests, lint, and builds debug APK.
- **security.yml**: Triggers on Push/PR/Schedule; scans dependencies and licenses.
- **build.yml**: Triggers on `develop` push; builds release candidate APK.
- **release.yml**: Triggers on Tag `v*.*.*`; signs and publishes official release.
- **cleanup.yml**: Triggers daily; deletes temporary artifacts.

## Secrets (Rule 13)
- `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`, `KEYSTORE_BASE64`.

Last Updated: 2026-11-05
Language: English (Rule 3)
