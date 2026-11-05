# Architecture

## Approach

Panther uses a single Android application module with logical boundaries intended to support later extraction when warranted. Keep dependencies pointing inward: presentation depends on domain contracts; data implements those contracts. Do not create layers or modules without a concrete use.

## Layers and data flow

- **Presentation:** Compose screens and reusable design-system components; screen ViewModels expose immutable UI state via StateFlow and handle events. Collect flows with lifecycle-aware APIs. Navigation is separate from business logic.
- **Domain:** models, repository interfaces and focused use cases for business rules, independent of Android UI and persistence details.
- **Data:** Room entities/DAOs and mappers implement domain repository interfaces; DataStore holds preferences. Expose observable data using Flow. All essential data remains local.

A user event goes from a composable to a ViewModel, through a use case or domain repository contract to the data implementation. Repository Flow updates return through the ViewModel to the UI. Suspend functions and coroutines handle asynchronous work.

## Dependency injection

Hilt is initialized by `PantherApplication` and `MainActivity` is an Android entry point. Hilt binds the Room repository and provides a singleton database. Use cases are constructor-injected; the activity owns the current feature ViewModels.

## Database strategy

Room version 1 defines profile, program, training day, exercise and per-set target entities, with foreign keys, cascading deletion and indexes. The repository uses transactions for activation, reordering and replacement of sets; it validates names and targets before writing. Schema export is configured at app/schemas; the first schema must be generated and committed by a successful build. Future schema versions need explicit migrations; never use destructive migration. Database tests cover activation, ordering, profile and relational behavior but have not yet run in this environment.

## Navigation and localization

A Navigation Compose host owns the four primary destinations, program details and settings. Bottom navigation uses saved state and single-top navigation. The current UI contains Persian resources and explicitly uses RTL because Persian is the only supported UI language for now. Revisit direction selection when additional languages are introduced. The theme defaults to dark and supports light and system settings persisted in DataStore.
