# Architecture

## Approach

Panther uses a single Android application module with logical boundaries intended to support later extraction when warranted. Keep dependencies pointing inward: presentation depends on domain contracts; data implements those contracts. Do not create layers or modules without a concrete use.

## Layers and data flow

- **Presentation:** Compose screens and reusable design-system components; screen ViewModels expose immutable UI state via StateFlow and handle events. Collect flows with lifecycle-aware APIs. Navigation is separate from business logic.
- **Domain:** models, repository interfaces and focused use cases for business rules, independent of Android UI and persistence details.
- **Data:** Room entities/DAOs and mappers implement domain repository interfaces; DataStore holds preferences. Expose observable data using Flow. All essential data remains local.

A user event goes from a composable to a ViewModel, through a use case or domain repository contract to the data implementation. Repository Flow updates return through the ViewModel to the UI. Suspend functions and coroutines handle asynchronous work.

## Dependency injection

Hilt is initialized by `PantherApplication` and `MainActivity` is an Android entry point. Future DI modules will bind repository implementations and provide database and other dependencies with appropriate lifetimes; no empty bindings are needed today.

## Database strategy

Room dependencies and KSP are configured; no database or schema is created at this stage. Introduce normalized profile, program, training day, exercise and per-set target entities with stable keys, indexes and foreign keys when the data layer is implemented. Use transactions for related writes and single-active-program enforcement. Export schemas and provide explicit migrations as the schema evolves; never use destructive migration in production. Test DAO and transactional behavior.

## Navigation and localization

A Navigation Compose host owns the initial foundation destination. Future feature destinations and the four primary navigation destinations will be added as real screens are developed, with stable routes and preserved state where appropriate. The current UI contains Persian resources and explicitly uses RTL because Persian is the only supported UI language for now. Revisit direction selection when additional languages are introduced. The theme is dark by default and provides a light scheme; a persisted theme choice is future work.
