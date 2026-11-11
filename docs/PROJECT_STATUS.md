# Project status — Panther

Updated: 2026-11-11 · Version: `v1.1.0` (latest published release) · Baseline: `AGENTS.md` 1.0.0
Template: `AGENTS.md` §29.3 · Dated claims only (`[DOC-06]`).

## Current state

Panther is an offline-first Android application for planning bodybuilding workout routines, with a
Persian RTL interface, Vazirmatn typography and Jalali dates. `v1.1.0` is published on GitHub
Releases; `main` is at `7775485`. The project adopted the *Agent Operating Baseline* (`AGENTS.md`
1.0.0) in commit `7775485`, which replaced the numbered-rule document; project-specific rules now
live in `AGENTS.project.md`.

## Completed

- 2026-11-07 — first stable release, full CI/CD (`v1.0.0`).
- 2026-11-08 — RTL redesign, Jalali dates, Room/backup hardening, signed direct publication (`v1.1.0`).
- 2026-11-11 — adopted `AGENTS.md` 1.0.0 as the agent baseline (commit `7775485`, direct push by owner).

## In progress

| item | owner | expected | blocker |
|---|---|---|---|
| Audit remediation set 1: font licence, protected-file guard, restore snapshot, doc fixes | agent session | awaiting owner review | needs approval to push/PR |

## Confirmed defects

| id | symptom | priority | since |
|---|---|---|---|
| D-01 | ~~Bundled Vazirmatn fonts shipped without the SIL OFL licence~~ — **fixed 2026-11-11**: the upstream OFL 1.1 text now ships inside the APK at `app/src/main/assets/licenses/vazirmatn-OFL-1.1.txt` and is referenced from `README.md`. Note: `v1.0.0`/`v1.1.0` artifacts published earlier still lack it | P1 | `v1.1.0` |
| D-02 | Documentation pointed at rule numbers (`Rule N`, `section 19.1`) that no longer exist in `AGENTS.md` — 45 references across 13 files. Fixed by this change set: 44 repointed to rule IDs or `AGENTS.project.md`; the one inside the `v1.1.0` changelog entry is kept as history | P2 | since `V1.1.0` adoption (`4867f70`) |

## Next tasks

1. Resolve `PD-001`–`PD-003` (below); no release may be tagged until they are decided.
2. On owner approval, push the current change set and open a pull request if requested.
3. Turn on branch protection for `main` in repository settings — the remaining half of `[PROT-06]`;
   `CODEOWNERS` and the `protected-files.yml` guard are in place, branch protection is still
   **unverified** because the API returns `403` for the available token.
4. Enable dependency-graph alerts, or record the decision to keep them disabled (`docs/REQUIREMENTS.md`).
5. Audit follow-ups still open: tests for `SettingsViewModel`/`ProgramPdfExporter`/`SafeUriOutput`,
   coverage measurement (JaCoCo), an error screen for an invalid `editor/{id}` route, Room schema
   cleanup, and the staged dependency upgrade.

## Pending decisions

| id | decision | options | who decides | by when |
|---|---|---|---|---|
| PD-001 | APK artifact name (baseline `[REL-07]` says `<project>-<version>-<platform>.<ext>`; project ships `Panther Vx.y.z.apk`) | keep current name with a recorded waiver, or rename and update `release.yml`, `app/build.gradle.kts`, docs | owner | before the next tag |
| PD-002 | Annotated tag message language (baseline `[REL-09]` says English; project uses Persian) | switch to English, or record a deviation | owner | before the next tag |
| PD-003 | Persian README location (baseline `[DOC-17]` wants `README.fa.md`; project uses `.github/README.md`) | keep `.github/README.md` (it is what GitHub renders), or rename and lose the homepage rendering | owner | before the next release |

## Validation status

| check | last run | result |
|---|---|---|
| `git status` / diff review of this change | 2026-11-11 | clean tree before the change; reviewed after |
| stale rule-reference grep (`Rule N`, `19.1`) | 2026-11-11 | 45 stale citations before; 0 after the fix |
| Markdown link existence check for touched docs | 2026-11-11 | all relative links resolve |
| `./gradlew :app:testDebugUnitTest` | never in this environment | **not run** — no JDK/Android SDK in the agent sandbox |
| `./gradlew :app:lintDebug` | never in this environment | **not run** — same reason |
| `./gradlew :app:connectedDebugAndroidTest` | never in this environment | **not run** — needs an emulator |
| `ci.yml` on a pull request | not triggered (no PR) | pending owner approval to push |
| `protected-files.yml` guard logic | 2026-11-11 | reviewed by reading; not executed — no push/PR yet |
| OFL text integrity | 2026-11-11 | fetched from `rastikerdar/vazirmatn` (`OFL.txt`) and stored verbatim; contains `SIL OPEN FONT LICENSE Version 1.1 - 26 February 2007` |
| unit test inventory | 2026-11-11 | 52 unit tests in 7 classes (27 new: `DurationCatalogTest`, `BmiCalculatorTest`); **not executed locally** — CI is the first execution |
| `release.yml` duplicate-release guard | 2026-11-11 | added; not exercised until a tag is pushed |
| Persian literals in `app/src/main` | 2026-11-11 | `grep` → 0 matches; all UI wording comes from `strings.xml` |

## Handoff notes

- `AGENTS.md` is protected and read-only (`[PROT-01]`); never edit, reformat or regenerate it, and never
  include it in bulk edits.
- Project rules, commands and off-limits paths are in `AGENTS.project.md`; the baseline itself carries no
  project-specific content.
- The working branch is the Arena session branch; `main` receives changes only through an owner-approved
  merge. No push or pull request happened without approval.
- Historical `CHANGELOG.md` entries keep their original `Rule N` / `section 19.1` wording on purpose:
  rewriting release history would falsify the record.

Language: English (`[DOC-13]`).
