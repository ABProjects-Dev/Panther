# Roadmap

1. **Repository and project foundation:** Gradle, app entry point, dependencies and documentation.
2. **Design system, brand identity and navigation:** `Panther` branding, bundled `Vazirmatn` typography, custom fitness launcher/header icons, three-destination shell (`Home`, `Programs`, `Profile`), Persian RTL resources and dark/light/system themes.
3. **Room database and domain models:** version 1 schema preserved without destructive migrations, relational entities, transactional repository operations, BMI calculation, searchable duration catalog, and business validation.
4. **User profile:** local editing for name, age, height and weight (profile photo feature removed by design).
5. **Workout program management:** compact program cards, bottom-left floating creation action, toggle switch activation with single-active-program green highlighting, and edit/delete actions inside each program's dedicated page (archive removed by design).
6. **Tabbed training days and drag-and-drop exercise editor:** separate tabs per training day, drag-and-drop exercise reordering persisted in Room, and independent repetition or searchable-dropdown duration targets per set.
7. **Read-only dashboard:** BMI summary card, green-highlighted active program with tabbed training days and square per-set visual boxes (recent programs and Home creation action removed by design).
8. **Jalali calendar formatting:** uniform Solar Hijri date display across UI screens and PDF export while preserving epoch-millisecond database storage.
9. **Settings and theme management:** DataStore-backed dark, light and system selection.
10. **JSON backup and restore:** Storage Access Framework export/import, version and data validation, replacement confirmation and transactional database restoration.
11. **PDF export:** per-program RTL PDF export with Vazirmatn typography and Jalali dates.
12. **Testing, optimization and release:** run the full Android build, unit tests, Room/UI instrumentation tests and device checks; continue accessibility, localization and release verification.

Each phase should include relevant tests, migrations where applicable, and documentation updates. Implementation status does not imply that unrun build or device verification has passed.
