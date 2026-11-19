# ADR-0003: Exercise-name catalogue and the new program/ day flow

Status: accepted · Date: 2026-11-19

## Context

The owner asked for ten behaviour changes to the program editor (default 12 repetitions, square set
fields with an `R`/`S` marker, a searchable exercise-name database, `R`/`S` labels, room-before-name
creation, numbered exercises, opt-in drag-and-drop, one screen per training day, icon back buttons and
page-level delete buttons). Four of them change the stored shape or the creation flow, so they are
recorded here:

1. exercise names must be searchable, which needs a persisted catalogue;
2. a program must exist before it is named, but must not be saved without a name;
3. exercise numbers must follow the persisted order;
4. dragging must be off by default.

Two questions had more than one safe reading and were answered by the owner on 2026-11-19:

- the name is chosen **in the program page**, not in a dialog after creation;
- the catalogue stays **device-only** and never enters the backup format;
- the set-count stepper **stays**, each set becomes a small numbered square;
- tasks 9 and 10 apply to **nested pages** (program, day, settings) and to the program, day and
  exercise delete actions.

## Options

| option | pros | cons | risk |
|---|---|---|---|
| Derive the name catalogue from existing exercises only | no schema change | cannot add a name before it is used, no "add to database" action | medium |
| Persist a device-only catalogue table (`exercise_catalog`) | searchable, addable, survives app restarts | Room schema bump and a migration | low |
| Persist the catalogue *and* include it in backups | one restore recreates suggestions | grows the format, duplicates data that `exercises` already carries | medium |
| Create the program row first with a placeholder name | the room exists immediately, one code path | needs a discard path when the user leaves | low |
| Keep the name dialog before creation | no placeholder | contradicts the explicit request (task 5) | low |

## Decision

Add `exercise_catalog` in Room schema 2 through the additive `MIGRATION_1_2` and keep it out of
`PantherBackup`. Create a program row with the `PENDING_PROGRAM_NAME` placeholder, let the program
page ask for the name, and delete the row when the user leaves before naming it. Derive every
displayed exercise/day number from the dense persisted `sortOrder`, and gate reordering behind the
`PantherReorderToggle` (off by default).

## Rationale

Weights correctness and reversibility: the migration only adds a table, the placeholder is removed by
the same screen that created it, and the numbering has a single source of truth (`sortOrder`) that
already existed and was already validated by `WorkoutValidation`. Backups stay backward compatible,
so no user has to migrate a file.

## Consequences

easier: adding suggestions, "add to database", and renumbering without extra counters · harder:
the program table can briefly hold a placeholder row, so every reader treats
`PENDING_PROGRAM_NAME` as "unnamed" · now costly to reverse: the schema version (reverting needs its
own migration) · follow-up: confirm the committed `2.json` identity hash on a machine that can run
the Room compiler, and add Compose UI tests for the reorder toggle and the name field.
