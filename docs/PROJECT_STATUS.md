# Project status — Panther

Updated: 2026-11-19 · Version: `v1.2.0` (released) + merged editor work (PR #41) and the unreleased panther-photo icon · Baseline: `AGENTS.md` 1.0.0
Template: `AGENTS.md` §29.3 · Dated claims only (`[DOC-06]`).

## Current state

Panther is an offline-first Android application for planning bodybuilding workout routines, with a
Persian RTL interface, Vazirmatn typography and Jalali dates. `v1.1.0` is the latest published
release; `v1.2.0` is tagged from `main` after the audit-remediation set (PR #39, merge `a830943`) was
merged. The project adopted the *Agent Operating Baseline* (`AGENTS.md` 1.0.0) in commit `7775485`,
which replaced the numbered-rule document; project-specific rules now live in `AGENTS.project.md`.

## Completed

- 2026-11-07 — first stable release, full CI/CD (`v1.0.0`).
- 2026-11-08 — RTL redesign, Jalali dates, Room/backup hardening, signed direct publication (`v1.1.0`).
- 2026-11-11 — adopted `AGENTS.md` 1.0.0 as the agent baseline (commit `7775485`, direct push by owner).
- 2026-11-11 — audit remediation set merged as PR #39 (`a830943`): font licence, protected-file guard,
  restore snapshot, atomic exports, duplicate-release guard, string-resource duration labels,
  `editor/{id}` not-found screen, 57 + 16 tests, JaCoCo coverage and CI build annotations.
- 2026-11-11 — `v1.2.0` release documentation finalized; `PD-002` decided in favour of Persian.
- 2026-11-19 — editor change set merged as PR #41 (`b88fb53`); CI green on `4c8f8f2` (70 unit tests,
  20 instrumented tests, 0 blocking lint issues).
- 2026-11-19 — application icon replaced by the owner-supplied panther photograph across the adaptive
  icon, legacy and round launcher rasters, themed (monochrome) layer and the in-app brand mark
  (`ADR-0004`).

## In progress

| item | owner | expected | blocker |
|---|---|---|---|
| ~~Unreleased editor change set (ten owner-requested items, `ADR-0003`)~~ — **merged 2026-11-19** as PR #41 (`b88fb53`) | agent on the session branch | done | — |
| Panther-photo app icon (`ADR-0004`) | agent on the session branch | owner review | resource-only change: not compiled in the sandbox (no JDK/Android SDK), so the first `lintDebug`/`assembleDebug` happens in CI |
| Room schema cleanup and build-toolchain upgrade | owner / agent on a workstation | next minor cycle | needs a machine with a JDK and the Android SDK; a schema or toolchain change cannot be built or diagnosed in the agent sandbox |

## Confirmed defects

| id | symptom | priority | since |
|---|---|---|---|
| D-01 | ~~Bundled Vazirmatn fonts shipped without the SIL OFL licence~~ — **fixed 2026-11-11**: the upstream OFL 1.1 text now ships inside the APK at `app/src/main/assets/licenses/vazirmatn-OFL-1.1.txt` and is referenced from `README.md`. Note: `v1.0.0`/`v1.1.0` artifacts published earlier still lack it | P1 | `v1.1.0` |
| D-02 | Documentation pointed at rule numbers (`Rule N`, `section 19.1`) that no longer exist in `AGENTS.md` — 45 references across 13 files. Fixed by this change set: 44 repointed to rule IDs or `AGENTS.project.md`; the one inside the `v1.1.0` changelog entry is kept as history | P2 | since `V1.1.0` adoption (`4867f70`) |

## Next tasks

1. Decide the two remaining pending items, `PD-001` (APK artifact name) and `PD-003` (Persian README
   path); `PD-002` was decided on 2026-11-11 in favour of Persian tag descriptions.
3. Turn on branch protection for `main` in repository settings — the remaining half of `[PROT-06]`;
   `CODEOWNERS` and the `protected-files.yml` guard are in place, branch protection is still
   **unverified** because the API returns `403` for the available token.
4. Enable dependency-graph alerts, or record the decision to keep them disabled (`docs/REQUIREMENTS.md`).
5. Audit follow-ups still open, both blocked on tooling rather than on decisions:
   - Room schema cleanup (`L-03`/`L-04`, the dead `profileImageUri`/`isArchived` columns) needs a
     version-2 schema plus a `2.json` export, and Room computes that file's identity hash during
     a Gradle/KSP build. Hand-writing it would ship an unverifiable schema, so it requires a
     machine with the Android SDK.
   - The staged dependency upgrade (`L-13`) is half done: the runtime libraries were refreshed
     (core 1.16.0, activity 1.10.1, navigation 2.8.9, coroutines 1.10.2, DataStore 1.1.7, org.json
     20250107). The toolchain itself (Kotlin 2.0.21, AGP 8.10.1, KSP, Compose BOM 2024.12.01, Hilt
     2.52, Room 2.6.1) still needs a machine with a JDK and the Android SDK: a compiler or Room
     schema change cannot be built — let alone diagnosed — in this sandbox.
   Automated coverage of the SAF write path is **done**: `SettingsViewModelTest` now exports a
   backup and a PDF into a real `content://` Downloads document, and the `editor/{id}` route has an
   explicit "not found" screen. Coverage is measured with JaCoCo (`jacocoDebugUnitTestReport`) and
   uploaded by `ci.yml`.

## Pending decisions

| id | decision | options | who decides | by when |
|---|---|---|---|---|
| PD-001 | APK artifact name (baseline `[REL-07]` says `<project>-<version>-<platform>.<ext>`; project ships `Panther Vx.y.z.apk`) | keep current name with a recorded waiver, or rename and update `release.yml`, `app/build.gradle.kts`, docs | owner | before the next tag |
| PD-002 | ~~Annotated tag message language~~ — **decided 2026-11-11: Persian**, deviation from `[REL-09]` recorded in `RELEASES.md` and `AGENTS.project.md` | — | owner | done |
| PD-003 | Persian README location (baseline `[DOC-17]` wants `README.fa.md`; project uses `.github/README.md`) | keep `.github/README.md` (it is what GitHub renders), or rename and lose the homepage rendering | owner | before the next release |

## Validation status

| check | last run | result |
|---|---|---|
| Unreleased editor change set (2026-11-19) | 2026-11-19 | **CI green on PR #41** (`4c8f8f2`): unit tests 70/70, instrumentation 20/20, `lintDebug` 0 blocking issues (18 total), debug APK built, `AGENTS.md` guard passed. Not built locally — the sandbox has no JDK/Android SDK |
| Panther-photo app icon (2026-11-19) | 2026-11-19 | **not built**: resource assets only, rendered and reviewed offline at every density (108–432 px layers, 48–192 px legacy icons); safe-zone clearance measured at ~24% of the 66 dp mask radius. `lintDebug` and the APK build run in CI on the pull request |
| `app/schemas/…/2.json` | 2026-11-19 | hand-written from `1.json` plus the new `exercise_catalog` entity. CI's *Verify Room schema baseline* step passed on a clean build and `MigrationTestHelper` validated the v1→v2 upgrade against it, so the file matches what Room generates. The `identityHash` value itself could not be read back from the run: artifact and log downloads are blocked from this sandbox |
| New unit tests (`ExerciseNameCatalogTest`, extended `ProgramsViewModelTest`) | 2026-11-19 | executed by CI: 70 unit tests, 0 failures. Two earlier red runs were real defects (compile errors, then a fake-repository gap) and are fixed in `a04fbc7` and `4c8f8f2` |
| New instrumented tests (migration 1→2, catalogue, insert order) | 2026-11-19 | executed by CI on the API 29 emulator: 20 tests, 0 failures |
| `git status` / diff review of this change | 2026-11-11 | clean tree before the change; reviewed after |
| stale rule-reference grep (`Rule N`, `19.1`) | 2026-11-11 | 45 stale citations before; 0 after the fix |
| Markdown link existence check for touched docs | 2026-11-11 | all relative links resolve |
| `./gradlew :app:testDebugUnitTest` | never in this environment | **not run** — no JDK/Android SDK in the agent sandbox |
| `./gradlew :app:jacocoDebugUnitTestReport` | never in this environment | **not run** locally; executed by `ci.yml` in the `lint-and-unit-tests` job on every pull request |
| `./gradlew :app:connectedDebugAndroidTest` | never in this environment | **not run** locally; executed by the `instrumentation-tests` job on pull requests that touch source |
| `ci.yml` on PR #39 (`820b9ec`) | 2026-11-11 | Lint and unit tests pass (57 tests, 0 blocking lint issues); Instrumentation tests pass (16 tests, 0 skipped, emulator API 29); `AGENTS.md` guard pass |
| PR #39 merge | 2026-11-11 | merged on owner instruction as `a830943` (31 files) |
| Exported-schema baseline (`app/schemas`) | 2026-11-19 | `1.json` plus a hand-written `2.json` for `exercise_catalog`; the dead-column cleanup still needs a real build, and `2.json` needs its identity hash regenerated |
| `./gradlew :app:lintDebug` | never in this environment | **not run** — same reason |
| `./gradlew :app:connectedDebugAndroidTest` | never in this environment | **not run** — needs an emulator |
| `ci.yml` on a pull request | not triggered (no PR) | pending owner approval to push |
| `protected-files.yml` guard logic | 2026-11-11 | reviewed by reading; not executed — no push/PR yet |
| OFL text integrity | 2026-11-11 | fetched from `rastikerdar/vazirmatn` (`OFL.txt`) and stored verbatim; contains `SIL OPEN FONT LICENSE Version 1.1 - 26 February 2007` |
| unit test inventory | 2026-11-11 | 52 unit tests in 7 classes (27 new: `DurationCatalogTest`, `BmiCalculatorTest`); **not executed locally** — CI is the first execution |
| `release.yml` duplicate-release guard | 2026-11-11 | added; not exercised until a tag is pushed |
| Persian literals in `app/src/main` | 2026-11-11 | `grep` → 0 matches; all UI wording comes from `strings.xml` |

## Handoff notes

- The editor work merged as PR #41 follows `docs/decisions/ADR-0003-exercise-catalogue-and-program-flow.md`; the owner's four answers of 2026-11-19 are recorded there.
- The icon set follows `docs/decisions/ADR-0004-panther-photo-app-icon.md`: one photograph, `60/108` of the adaptive canvas, feathered edge, solid `#010202` background, per-density rasters. Regenerate from the source image; do not paste a trimmed rectangle on a different colour.


- `AGENTS.md` is protected and read-only (`[PROT-01]`); never edit, reformat or regenerate it, and never
  include it in bulk edits.
- Project rules, commands and off-limits paths are in `AGENTS.project.md`; the baseline itself carries no
  project-specific content.
- The working branch is the Arena session branch; `main` receives changes only through an owner-approved
  merge. No push or pull request happened without approval.
- Historical `CHANGELOG.md` entries keep their original `Rule N` / `section 19.1` wording on purpose:
  rewriting release history would falsify the record.

Language: English (`[DOC-13]`).
