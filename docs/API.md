# Panther Project API Reference

## Overview
This document provides technical details for the Panther Android application APIs, including internal domain models, repository interfaces, ViewModels, backup codec, and GitHub Actions integration.

## Domain Models
- **UserProfile**: Name, age, height (cm), weight (kg).
- **BmiCalculator / BmiResult**: Calculates BMI and classifies into `UNDERWEIGHT`, `NORMAL`, `OVERWEIGHT`, or `OBESE`.
- **DurationCatalog / DurationOption**: Preset and custom `mm:ss` durations. `DurationOption` is data only
  (`minutes`, `seconds`, `key`, `totalSeconds`); the localized label is produced by
  `core/format/DurationLabels.kt` from `strings.xml`, and `DurationCatalog.search` accepts an optional
  `labelOf` resolver so the wording participates in matching without living in the domain layer.
- **WorkoutProgram**: ID, name, creation timestamp, active status, day count, exercise count.
- **TrainingDay**: ID, program ID, name, dense `sortOrder`.
- **Exercise / ExerciseSet / ExerciseWithSets**: Exercise metadata paired with ordered per-set repetition or duration targets.
- **ProgramBackup / DayBackup / ExerciseBackup / SetBackup / PantherBackup**: Versioned backup snapshot models with bounded structural limits (`MAX_PROGRAMS = 100`, `MAX_DAYS_PER_PROGRAM = 50`, `MAX_EXERCISES_PER_DAY = 50`, `MAX_SETS_PER_EXERCISE = 30`, `MAX_BACKUP_BYTES = 8_000_000`).

## Repository Interface

`PantherRepository` (`app/src/main/java/com/panther/app/domain/repository/PantherRepository.kt`):

- **Observation:** `observeProfile()`, `observePrograms()`, `observeDays(programId)`,
  `observeExercises(dayId)`, `observeExercisesWithSets(dayId)`, `observeSets(exerciseId)`.
- **Profile:** `saveProfile(profile)`.
- **Programs:** `createProgram(name)`, `renameProgram(id, name)`, `deleteProgram(id)`,
  `activateProgram(id)`, `deactivateProgram(id)`.
- **Days:** `addDay(programId, name, order)`, `renameDay(id, name)`, `deleteDay(id)`,
  `reorderDays(programId, ids)`.
- **Exercises and sets:** `saveExercise(dayId, exerciseId, name, order, sets)`,
  `deleteExercise(id)`, `reorderExercises(dayId, ids)`.
- **Backup & PDF snapshot:** `createProgramBackup(programId)`, `createBackup()`,
  `restoreBackup(backup)`.

## ViewModels
- **ProgramsViewModel**: Manages workout program/day/exercise flows, activation toggles, day and exercise reordering, and error state (`clearError()`).
- **ProfileViewModel**: Manages local user profile persistence, validation, and transient save/error states (`clearSaved()`, `clearError()`).
- **SettingsViewModel**: Manages persisted theme selection, single-program PDF export (`pdfMessage`), and validated JSON backup/restore operations (`operationMessage`).

## Backup JSON Format

```json
{
  "format": "panther-backup",
  "version": 1,
  "profile": { "name": "…", "age": 28, "heightCm": 175.5, "weightKg": 80.0 },
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
Decoding accepts either a `ByteArray` or `String`, validates UTF-8 byte limits without extra
heap copies, enforces single-active-program and ordering invariants before any database write
occurs, and remains backward-compatible with legacy v1 backups containing `profileImageBase64`
or `archived`.

## GitHub Actions

See [GITHUB_ACTIONS.md](GITHUB_ACTIONS.md) for the authoritative workflow documentation.

## Secrets (`[SEC-01]`)
- `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`, `KEYSTORE_BASE64`.

Last Updated: 2026-11-07
Language: English (`[DOC-13]`)
