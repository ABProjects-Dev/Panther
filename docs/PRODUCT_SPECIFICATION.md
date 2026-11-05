# Product specification

## Purpose

Panther is a Persian-first, offline-first Android application for creating, organizing, editing and managing bodybuilding workout plans. It is not a workout tracking application.

## MVP scope

- **Profile:** name, image, age, height in centimeters and weight in kilograms; editable locally.
- **Programs:** create multiple programs; edit existing programs, delete with confirmation, archive, activate and deactivate. Store creation or received date and configure training days.
- **Training days and exercises:** customize day names; reorder days and exercises; add, edit and remove exercises. Each exercise has a name, set count and repetition or duration target type. Every set has an independent target; durations support minutes and seconds.
- **Dashboard:** user name, active program, training day and exercise counts, recent programs and quick program creation.
- **Navigation:** Home, Programs, History and Profile, with settings accessible from an appropriate screen.
- **Backup/export:** JSON export and validated import with safe, confirmed restoration; PDF program export; user-selected locations through the Storage Access Framework.

## Rules and constraints

Only one program may be active; activation deactivates the prior one without deleting it. Editing does not create versions. Archived programs remain distinguishable from active and inactive programs. Significant deletion requires confirmation. Persist relationships and ordering locally using Room, transactions and migrations without destructive migrations. Import must validate the entire backup before changing data, with atomic restoration whenever possible.

The interface is dark-first, supports light mode, Persian and RTL, and uses the Panther design system. No registration, network connection, online authentication or cloud storage is required.

## Exclusions

No workout session tracking, completed sets, actual repetitions, lifted weights, rest timers, workout start/finish, performance analytics, progress charts, exercise video/tutorial image libraries, coach or athlete accounts, online accounts, cloud sync or social networking in the MVP. Set targets are plans, not recorded performance.

Authoritative project guidance: [AGENTS.md](../AGENTS.md).
