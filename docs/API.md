# Panther Project API Reference

## Overview
This document provides technical details for the Panther Android application APIs, including internal domain models, repository interfaces, and GitHub Actions integration.

## Domain Models (Rule 3)
- **UserProfile**: Name, age, height, weight, profile image URI.
- **ProgramBackup**: Name, creation time, active/archived status, days.
- **DayBackup**: Name, order, exercises.
- **ExerciseBackup**: Name, order, sets.
- **SetBackup**: Number, target type (`REPS`/`DURATION`), and either a repetition value or
  minutes+seconds. Exactly one target family is set per set.

## Repository Interfaces

`PantherRepository` (`app/src/main/java/com/panther/app/domain/repository/PantherRepository.kt`):

- **Observation:** `observeProfile()`, `observePrograms()`, `observeDays(programId)`,
  `observeExercises(dayId)`, `observeSets(exerciseId)`.
- **Profile:** `saveProfile(profile)`.
- **Programs:** `createProgram(name)`, `renameProgram(id, name)`, `deleteProgram(id)`,
  `activateProgram(id)`, `deactivateProgram(id)`, `archiveProgram(id, archived)`.
- **Days:** `addDay(programId, name, order)`, `renameDay(id, name)`, `deleteDay(id)`,
  `reorderDays(programId, ids)`.
- **Exercises and sets:** `addExercise(dayId, name, order)`, `renameExercise(id, name)`,
  `saveExercise(dayId, exerciseId, name, order, sets)`, `deleteExercise(id)`,
  `reorderExercises(dayId, ids)`, `replaceSets(exerciseId, sets)`.
- **Backup:** `createBackup()`, `restoreBackup(backup)`.

## ViewModels
- **ProgramsViewModel**: Manage workout data flows and operations.
- **ProfileViewModel**: Manage profile and image operations.
- **SettingsViewModel**: Manage theme and backup/restore operations.

## Backup JSON Format

```json
{
  "format": "panther-backup",
  "version": 1,
  "profile": { "name": "…", "age": 28, "heightCm": 175.5, "weightKg": 80.0 },
  "profileImageBase64": null,
  "programs": [
    {
      "id": 7, "name": "Plan", "createdAt": 1700000000000,
      "active": true, "archived": false,
      "days": [
        { "name": "Day 1", "order": 0,
          "exercises": [
            { "name": "Squat", "order": 0,
              "sets": [ { "number": 1, "type": "REPS", "value": 8, "minutes": null, "seconds": null } ] }
          ] }
      ]
    }
  ]
}
```

`id` carries `ProgramBackup.originalId` and is written and read under the same key.
Decoding validates size limits, single-active-program invariant, ordering uniqueness and
set targets before any database write occurs.

## GitHub Actions

See [GITHUB_ACTIONS.md](GITHUB_ACTIONS.md) for the authoritative workflow documentation.

## Secrets (Rule 13)
- `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`, `KEYSTORE_BASE64`.

Last Updated: 2026-11-05
Language: English (Rule 3)
