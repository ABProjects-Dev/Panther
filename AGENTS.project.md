# Project rules — Panther

Baseline: `AGENTS.md` **1.0.0** (*Agent Operating Baseline*, adopted 2026-11-11 in commit `7775485`).
Domain profile: **Mobile** (`AGENTS.md` §28.2).
This file states only what differs from, or is stricter than, the baseline (`[ADOPT-04]`, `[PREC-05]`).
It never weakens a baseline `MUST`; where it would, the item is listed under *Pending decisions* and the
baseline wins until the owner decides (`[PREC-02]`).

## Stack

| Layer | Choice | Version |
|---|---|---|
| Language / runtime | Kotlin, JVM target 17 | 2.0.21 |
| Build | Gradle wrapper, Android Gradle Plugin | 9.8.2 / 8.10.1 |
| UI | Jetpack Compose (BOM), Material 3, Navigation Compose | 2024.12.01 / 2.8.5 |
| DI / codegen | Hilt, KSP | 2.52 / 2.0.21-1.0.28 |
| Persistence | Room, DataStore | 2.6.1 / 1.1.1 |
| SDK levels | `compileSdk` / `targetSdk` / `minSdk` | 36 / 36 / 26 |
| Application ID | `com.panther.app` — changing it needs owner approval (`[APPR-04]`) | — |
| Platform | Android only; offline-first; **no runtime permissions declared** | — |
| Product languages | UI Persian, RTL, Vazirmatn, Jalali dates; technical documents English (`[DOC-13]`) | — |

## Commands (authoritative)

| Purpose | Command |
|---|---|
| Debug build | `./gradlew :app:assembleDebug` |
| Unit tests | `./gradlew :app:testDebugUnitTest` |
| Android Lint | `./gradlew :app:lintDebug` |
| Instrumented tests | `./gradlew :app:connectedDebugAndroidTest` (emulator, API 29) |
| Android-test APK only | `./gradlew :app:assembleDebugAndroidTest` |
| Room schema check | build, then `git status --porcelain app/schemas` must print nothing |
| Release | owner-approved `vX.Y.Z` tag → `release.yml`; never a manual local publish |

Requires JDK 17 and Android SDK 36. Details, test inventory and known limitations: `docs/TESTING.md`.

## Workflow (owner's standing rules)

1. Keep one working branch — `dev`, or the fixed Arena session branch. Do not create extra branches
   (`[GIT-02]`).
2. Do **not** open a pull request unless the owner asks for one (`[REMOTE-05]`).
3. Do **not** merge unless the owner asks for that exact merge (`[GIT-04]`, `[REMOTE-01]`).
4. `ci.yml` runs only on pull requests that touch non-documentation paths; documentation-only pull
   requests skip it through `paths-ignore` and need no CI run.
5. Do not add automation that creates branches, commits or pull requests (Dependabot and similar) —
   dependency and action-SHA updates are manual (`[REMOTE-01]`).
6. Before a release tag, merge every release document (`CHANGELOG.md`, `RELEASES.md`, `README.md`,
   `.github/README.md`, `docs/*`) so the tagged commit already carries final documentation
   (`[SYNC-03]`).
7. Start a release only when the owner sends `tag`: offer patch/minor/major, wait for the selection,
   then tag from `main` (`[REL-04]`).
8. Keep the English `README.md` and the Persian `.github/README.md` in sync (`[DOC-18]`).

## Conventions

- **Commits**: Conventional Commits, one logical change per commit, English subject (`[GIT-02]`).
- **Branches**: `main` (stable, owner-approved) plus the single working branch; no `feat/`, `fix/`
  prefixes.
- **Tags**: annotated `vX.Y.Z` on `main`; `versionCode = MAJOR*10000 + MINOR*100 + PATCH`, with
  `MINOR` and `PATCH` in `0..99` (`[REL-03]`, `[REL-04]`).
- **Documents**: English only, except `README` translations (`[DOC-13]`, `[DOC-19]`).
- **Reports to the owner**: Persian, RTL (`[REP-05]`, `[REP-06]`).
- **Release artifacts**: signed APK plus `.sha256` checksum, built from the tagged commit
  (`[REL-07]`).

## Stricter rules

- **MUST NOT** commit signing material: `*.jks`, `*.keystore`, `signing.properties`, or decoded
  keystores (`[SEC-07]`).
- **MUST** keep `app/schemas/*.json` committed and matching the Room schema; an intended change ships
  with its migration and test (`[DATA-03]`, `[MOB-05]`).
- **MUST** keep the app permission-free (`AndroidManifest.xml` declares no `<uses-permission>`); adding
  one is a product decision that needs owner approval (`[SEC-02]`).
- **MUST** keep every third-party dependency and action pinned — actions by immutable commit SHA, as
  `ci.yml` and `release.yml` do (`[CI-01]`, `[DEP-02]`).
- **MUST** stop and wait for owner approval when `ci.yml` is green on a release pull request
  (`[APPR-01]`).

## Paths

| Path | Meaning |
|---|---|
| `app/src/main/java/com/panther/app/` | Application code (data, domain, feature, ui, designsystem) |
| `app/src/main/res/` | Resources; `res/font/` holds the bundled Vazirmatn files |
| `app/src/test/java/` | JVM unit tests |
| `app/src/androidTest/java/` | Instrumented tests (emulator) |
| `app/schemas/` | Committed Room schema baseline (`1.json`) |
| `docs/` | Architecture, requirements, plan, status, testing, API, release, secrets, decisions |
| `.github/workflows/` | `ci.yml`, `release.yml` |
| `.github/README.md` | Persian README shown on the GitHub repository page |

## Off-limits without approval

- `AGENTS.md` — protected, read-only for every agent (`[PROT-01]`).
- `main` — no direct pushes; merges only on owner request (`[GIT-04]`).
- Release tags and GitHub Releases (`[REL-01]`, `[REL-02]`).
- Signing keystore, signing passwords, GitHub Secrets (`[SEC-01]`).
- `applicationId` / package name (`[APPR-04]`).
- Repository settings: branch protection, collaborators, secrets.

## Pending decisions

These conflict with a baseline `MUST`; none of them may be resolved by an agent.
Tracked as `PD-001`–`PD-003` in `docs/PROJECT_STATUS.md`.

- **PD-001** — APK naming: current `Panther Vx.y.z.apk` vs baseline `[REL-07]`
  `<project>-<version>-<platform>.<ext>`.
- **PD-002** — annotated tag message language: current Persian vs baseline `[REL-09]` (English).
- **PD-003** — Persian README path: current `.github/README.md` vs baseline `[DOC-17]` (`README.fa.md`).

Language: English (`[DOC-13]`).
