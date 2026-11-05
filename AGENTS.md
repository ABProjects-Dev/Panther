# AGENTS.md — Panther Android Application

## 1. Project Identity

Project Name: Panther

Panther is a modern, offline-first Android application designed for creating, organizing, editing, and managing bodybuilding workout plans.

The application focuses exclusively on workout plan management.

Panther is NOT a workout tracking application.

The primary objective is to provide a clean, professional, reliable, and intuitive environment for users to create and manage their personal workout programs.

All development decisions must respect this product definition.

## 2. Technology Stack

Use the following technologies:

- Programming Language: Kotlin
- UI Framework: Jetpack Compose
- Design System: Material 3 with a custom Panther design system
- Architecture: Clean Architecture with MVVM
- Database: Room
- Navigation: Navigation Compose
- Dependency Injection: Hilt
- Asynchronous Programming: Kotlin Coroutines and Flow
- Preferences: DataStore
- Image Selection: Android Photo Picker
- Minimum Android SDK: 26 (Android 8.0)
- Target SDK: Latest stable SDK compatible with the selected development environment.

Use Gradle Kotlin DSL.

Use a centralized version catalog through `gradle/libs.versions.toml`.

Prefer stable library versions. Avoid alpha, beta, or experimental dependencies unless absolutely necessary.

## 3. Architecture Rules

Follow a modular, maintainable, and scalable architecture.

Use clear separation of concerns:

- Presentation Layer
- Domain Layer
- Data Layer

Recommended package structure:

```text
com.panther.app/
│
├── PantherApplication.kt
│
├── core/
│   ├── navigation/
│   ├── designsystem/
│   ├── theme/
│   ├── common/
│   ├── extensions/
│   └── util/
│
├── data/
│   ├── local/
│   │   ├── database/
│   │   ├── dao/
│   │   └── entity/
│   ├── repository/
│   └── mapper/
│
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
│
├── feature/
│   ├── home/
│   ├── programs/
│   ├── history/
│   ├── profile/
│   ├── programeditor/
│   └── settings/
│
└── di/
```

This is the recommended logical structure. Physical Gradle modules may be introduced when they provide meaningful architectural benefits.

Do not create unnecessary abstractions or excessive modules.

## 4. Product Scope and Restrictions

The following features are included in the MVP:

### User Profile

- Name
- Profile image
- Age
- Height in centimeters
- Weight in kilograms
- Editing profile information

### Workout Programs

- Create multiple workout programs.
- Edit existing programs.
- Delete programs.
- Archive programs.
- Activate and deactivate programs.
- Store program creation or received date.
- Configure the number of training days.
- Customize training day names.
- Reorder training days.
- Add, edit, remove, and reorder exercises.

### Exercises

Each exercise must support:

- Exercise name
- Number of sets
- Target type: Repetitions or Duration
- Individual target value for each set.

Duration must support minutes and seconds.

Each set must be represented by its own independent target value.

### Dashboard

Display:

- User name
- Active workout program
- Number of training days
- Number of exercises
- Recent workout programs
- Quick access to creating a program

### Main Navigation

The application must have four primary destinations:

1. Home
2. Programs
3. History
4. Profile

Settings can be accessed from the appropriate screen.

## 5. Explicitly Excluded Features

NEVER implement the following in the MVP unless explicitly requested:

- Workout session tracking
- Recording completed sets
- Recording actual repetitions
- Recording lifted weights
- Rest timers
- Workout start or finish functionality
- Performance analytics
- Progress charts
- Exercise video library
- Exercise tutorial images
- Coach and athlete accounts
- Online accounts
- Cloud synchronization
- Social networking

Do not introduce features that conflict with the product definition.

## 6. Business Rules

These rules must always be respected:

1. A user can create multiple workout programs.
2. Only one program can be active at a time.
3. Activating a new program must deactivate the previous active program.
4. Previous programs must remain available.
5. Editing a program modifies the existing program.
6. Automatic program versioning is not supported.
7. Archived programs must remain distinguishable from active and inactive programs.
8. Deleting important data must require user confirmation.
9. Program days and exercises must support custom ordering.
10. Set targets must be stored independently.

Do not confuse target values with actual workout performance.

## 7. Database Rules

Use Room as the local database.

The application must work completely offline.

Recommended database entities:

- UserProfileEntity
- WorkoutProgramEntity
- TrainingDayEntity
- ExerciseEntity
- ExerciseSetEntity

Use proper foreign keys and relationships.

Recommended relationships:

- One program has multiple training days.
- One training day has multiple exercises.
- One exercise has multiple sets.

Use stable primary keys and appropriate indexes.

Use database transactions for operations that modify multiple related records.

Enforce the single-active-program rule at the repository or domain level, preferably through a transactional operation.

Avoid storing relational data in unnecessarily complex serialized strings.

Database changes must be handled through proper Room migrations.

Never use destructive migrations in production code.

## 8. UI and Design Guidelines

Panther must follow a modern, professional, minimal, Dark First design language.

Primary color palette:

| Color | Hex |
| --- | --- |
| Panther Lime | #C6F432 |
| Background | #101820 |
| White | #FFFFFF |
| Surface | #252F38 |
| Secondary Gray | #8D99A6 |

Design requirements:

- Dark theme is the default.
- Light theme must also be supported.
- Preserve Panther's visual identity in both themes.
- Use consistent spacing and typography.
- Use rounded cards and clean layouts.
- Avoid unnecessary visual clutter.
- Use subtle animations.
- Provide clear visual hierarchy.
- Use accessible touch targets.
- Support Persian and RTL layouts.
- Avoid hardcoded UI strings.
- Put all user-facing text in string resources.
- Use reusable Compose components.

The UI should feel like a premium fitness management application.

Do not use generic Material 3 styling without adapting it to Panther's visual identity.

## 9. Localization

Persian is the primary application language.

Requirements:

- Proper RTL layout.
- Persian UI strings.
- Correct alignment of text, icons, and navigation.
- Appropriate Persian typography.
- Support for localized dates and numbers where applicable.

Keep the application architecture localization-ready.

Do not hardcode Persian strings directly inside composable functions.

## 10. Offline-First Requirements

The application must not require:

- Internet access
- Registration
- Login
- Online authentication
- Remote APIs
- Cloud storage

All essential data must be stored locally.

Do not add the INTERNET permission unless explicitly required and approved.

## 11. Data Backup and Export

The planned backup system must support:

- JSON export.
- JSON import.
- Backup file validation.
- Safe restoration.
- PDF export of workout programs.
- User-selected file locations using Android Storage Access Framework.

Import operations must validate the complete backup before modifying existing data.

Database restoration must be atomic whenever possible.

Never overwrite user data without appropriate confirmation.

## 12. Code Quality

Follow these standards:

- Write clean, readable Kotlin.
- Prefer immutable data structures.
- Use StateFlow for UI state where appropriate.
- Use sealed interfaces or classes for UI states and results.
- Avoid business logic inside Composable functions.
- Keep composables focused and reusable.
- Use dependency injection consistently.
- Handle errors explicitly.
- Validate user inputs.
- Avoid unnecessary dependencies.
- Avoid duplicated code.
- Avoid unexplained magic numbers.
- Use meaningful naming conventions.
- Document complex business rules.

Do not use deprecated APIs when stable alternatives exist.

## 13. Jetpack Compose Guidelines

- Prefer stateless reusable composables.
- Use ViewModels for screen-level state.
- Collect flows with lifecycle-aware APIs.
- Keep navigation separate from business logic.
- Use Material 3 components as building blocks.
- Create reusable Panther-specific components.
- Use stable keys for dynamic lists.
- Preserve UI state when navigating where appropriate.

## 14. Testing

Write tests for important business logic.

Prioritize:

- Program activation rules.
- Program creation and editing.
- Program deletion.
- Archive behavior.
- Exercise and set management.
- Input validation.
- Backup validation.
- Repository operations.

Use unit tests for domain logic and database tests for Room behavior.

Do not consider a feature complete without verifying its basic behavior.

## 15. Git and Documentation Rules

- Keep commits focused and descriptive.
- Do not commit generated build artifacts.
- Do not commit local secrets.
- Maintain a useful README.md.
- Document architectural decisions.
- Keep documentation synchronized with implementation.
- Never silently remove existing functionality.
- Never overwrite unrelated user changes.

## 16. Agent Working Rules

Before modifying the project:

1. Inspect the repository.
2. Read this AGENTS.md file.
3. Read the existing documentation.
4. Understand the current architecture.
5. Identify affected files.
6. Propose a clear implementation plan when the task is substantial.

During development:

- Make focused changes.
- Respect existing conventions.
- Avoid unnecessary refactoring.
- Do not introduce unrelated features.
- Do not generate placeholder implementations for features that should work.
- Do not claim that a build or test succeeded unless it was actually executed successfully.

After implementation:

1. Run the relevant Gradle build tasks.
2. Run relevant tests.
3. Fix compilation errors.
4. Report changed files.
5. Report completed functionality.
6. Report remaining issues and limitations.

If a requirement is ambiguous, ask before making a major architectural or product decision.

## 17. Definition of Done

A task is complete only when:

- The implementation follows this document.
- The code compiles.
- Relevant tests pass.
- Existing functionality remains intact.
- UI follows Panther's design system.
- Data is persisted correctly.
- No excluded MVP functionality has been introduced.
- Documentation is updated when necessary.

### Final Principle

Panther is a focused workout program management application.

Simplicity, reliability, offline functionality, maintainability, and user experience are more important than adding unnecessary features.

Every implementation decision must support this principle.
