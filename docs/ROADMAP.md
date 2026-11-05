# Roadmap

1. **Repository and project foundation:** Gradle, app entry point, dependencies and documentation.
2. **Design system and application navigation:** reusable components, four-destination shell, Persian RTL resources and dark/light/system themes are implemented; visual accessibility checks on devices remain.
3. **Room database and domain models:** version 1 schema, relational entities, transactional repository operations and business validation are implemented. Run tests, generate and commit the Room schema, and add explicit migrations when the schema changes.
4. **User profile:** local editing and Photo Picker are implemented; replaced app-managed profile images are cleaned up after successful saves/restores. Device verification remains.
5. **Workout program management:** create, rename/edit, confirmed deletion, activate/deactivate and archive are implemented; device verification remains.
6. **Training days and exercise editor:** add/edit/delete/reorder training days and exercises, and configure independent repetition or duration targets for each set, are implemented.
7. **Dashboard:** active program summary, recent programs and quick creation are implemented; device verification remains.
8. **History and archive:** program history and archive actions are implemented; this is program history only, not workout tracking.
9. **Settings and theme management:** DataStore-backed dark, light and system selection is implemented.
10. **JSON backup and restore:** Storage Access Framework export/import, version and data validation, profile-image packaging, replacement confirmation and transactional database restoration are implemented.
11. **PDF export:** per-program PDF export through a user-selected location is implemented.
12. **Testing, optimization and release:** run the full Android build, unit tests, Room/UI instrumentation tests and device checks; continue accessibility, localization and release verification.

Each phase should include relevant tests, migrations where applicable, and documentation updates. Implementation status does not imply that unrun build or device verification has passed.
