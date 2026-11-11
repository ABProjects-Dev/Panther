# Product specification

## Purpose

Panther is a Persian-first, offline-first Android application for creating, organizing, editing and managing bodybuilding workout plans. It is not a workout tracking application.

## Scope

- **Visual identity & typography:** Application branded as `Panther` across launcher, headers, dashboard and onboarding, with a custom fitness dumbbell/crest icon and bundled `Vazirmatn` font across all UI and PDF output.
- **Profile & BMI:** Local name, age, height in centimeters and weight in kilograms (no profile photos). Height and weight drive automatic BMI calculation and classification on the Home dashboard.
- **Dashboard (Home):** Strictly read-only overview showing BMI and the active workout program (highlighted in green), with tabbed training days and individual square boxes for every exercise set. No "New Program" button or "Recent Programs" list on Home.
- **Programs:** Compact program cards showing name, counts, Jalali creation date and an active/inactive toggle switch, with a floating "New Program" button anchored at the bottom-left corner. Only one program may be active at a time; activating one automatically deactivates the prior active program. Editing and confirmed deletion live exclusively inside each program's dedicated page.
- **Training days and exercises:** Training days are separated into tabs so selecting a tab displays only that day's exercises. Exercises support adding, editing, deleting, drag-and-drop reordering (persisted in Room), and independent per-set targets (repetitions or searchable duration dropdown).
- **Dates & calendar:** Stored as epoch milliseconds in Room; formatted uniformly in the Persian Jalali (Solar Hijri) calendar (`YYYY/MM/DD`) across all screens and PDF exports.
- **Navigation:** Three main destinations (`Home`, `Programs`, `Profile`), with `Settings` accessible from `Profile`. No History or Archive features.
- **Backup/export:** JSON export and validated import with safe, confirmed restoration; PDF program export; user-selected locations through the Storage Access Framework.

## Rules and constraints

Only one program may be active; activation deactivates the prior one without deleting it. Significant deletion requires confirmation. Persist relationships and ordering locally using Room and transactions without destructive migrations (schema version 1 is preserved). Import must validate the entire backup before changing data, with atomic restoration.

The interface is dark-first, supports light mode, Persian and RTL, and uses the Panther design system. No registration, network connection, online authentication or cloud storage is required.

## Exclusions

No workout session tracking, completed sets, actual repetitions, lifted weights, workout history, program archiving, profile image uploads, rest timers, workout start/finish, performance analytics, progress charts, exercise video/tutorial image libraries, coach or athlete accounts, online accounts, cloud sync or social networking. Set targets are plans, not recorded performance.

Authoritative project guidance: [AGENTS.md](../AGENTS.md) (operating baseline) and [AGENTS.project.md](../AGENTS.project.md) (project rules).
