# Testing — Panther

Authoritative commands, what each check covers, and what cannot be verified outside a full Android
toolchain. Rules that require this document: `[DOC-04]` (Tier 2), `[VAL-08]`, `[SYNC-02]`.

## Prerequisites

- JDK 17 (`actions/setup-java`, Temurin 17 in CI).
- Android SDK with platform 36 (`compileSdk`/`targetSdk`), build-tools, and `aapt2`/`apksigner` for the
  release checks.
- A hardware-accelerated emulator for instrumented tests (CI uses `api-level: 29`, `target: google_apis`,
  `arch: x86_64`, `profile: Nexus 6`, animations disabled).
- The Gradle wrapper (`gradlew`, Gradle 9.8.2) — never a locally installed Gradle.

## Commands

| Check | Command | Notes |
|---|---|---|
| Unit tests | `./gradlew :app:testDebugUnitTest` | JVM only, no device needed |
| Unit-test coverage | `./gradlew :app:jacocoDebugUnitTestReport` | HTML/XML under `app/build/reports/jacoco/jacocoDebugUnitTestReport/` |
| Android Lint | `./gradlew :app:lintDebug` | Report: `app/build/reports/lint-results-debug.html` |
| Compile debug APK | `./gradlew :app:assembleDebug` | Output: `app/build/outputs/apk/debug/Panther Vx.y.z.apk` |
| Compile androidTest APK | `./gradlew :app:assembleDebugAndroidTest` | Proves the instrumented sources compile |
| Instrumented tests | `./gradlew :app:connectedDebugAndroidTest` | Requires a running emulator/device |
| Room schema check | build, then `git status --porcelain app/schemas` | Empty output = schema matches the committed baseline |
| Release quality gate | `./gradlew :app:testDebugUnitTest :app:lintDebug` | What `release.yml` runs before signing |

CI shorthand — the exact command `ci.yml` uses:

```bash
./gradlew :app:testDebugUnitTest :app:jacocoDebugUnitTestReport :app:lintDebug \
  :app:assembleDebug :app:assembleDebugAndroidTest --stacktrace
```

On Windows use `gradlew.bat`. Run everything from the repository root.

## What the suites cover

Unit tests (`app/src/test/java/com/panther/app/`) — 57 tests:

| File | Tests | Covers |
|---|---|---|
| `data/PantherBackupJsonTest.kt` | 8 | Backup/restore JSON encoding, versioning, byte-size limit, structural validation |
| `domain/WorkoutValidationTest.kt` | 7 | Input validation and ordering invariants |
| `domain/BackupLimitTest.kt` | 4 | Symmetric limits and boundary values |
| `core/format/PantherDateFormatTest.kt` | 3 | Jalali date formatting |
| `feature/ProgramsViewModelTest.kt` | 3 | Program-list view-model state |
| `domain/DurationCatalogTest.kt` | 18 | Duration parsing (Persian and ASCII digits), preset ordering/uniqueness, custom-value insertion, search by label/key/seconds |
| `domain/BmiCalculatorTest.kt` | 9 | BMI guard rails (missing, non-finite, out-of-range), boundary acceptance, category thresholds, rounding |
| `feature/EditorAvailabilityTest.kt` | 5 | `editor/{id}` resolution: found, still loading, missing, `0` fallback for a malformed argument |

Instrumented tests (`app/src/androidTest/java/com/panther/app/`) — 16 tests:

| File | Tests | Covers |
|---|---|---|
| `data/PantherDatabaseTest.kt` | 9 | Room schema, DAO behavior, ordering and cascade rules |
| `MainActivityTest.kt` | 2 | Activity launch and basic navigation smoke test |
| `feature/SettingsViewModelTest.kt` | 4 | Import validation rejects malformed JSON without touching data; an accepted restore writes `filesDir/pre-restore/backup-before-restore.json` and replaces the dataset; export writes a decodable JSON document and a `%PDF-` document to a real `content://` destination |
| `feature/ProgramNotFoundScreenTest.kt` | 1 | The terminal `editor/{id}` screen shows the message and wires the back action |

## CI mapping

| Workflow | Job | Scope |
|---|---|---|
| `ci.yml` | `lint-and-unit-tests` | unit tests, lint, debug and androidTest APK assembly, Room schema check |
| `ci.yml` | `instrumentation-tests` | emulator run; skips documentation/metadata-only changes |
| `release.yml` | build-and-sign | `testDebugUnitTest` + `lintDebug`, then signed release, `apksigner` v2/v3 verification, SHA-256 checksum |

`ci.yml` runs only on pull requests touching non-documentation paths (`paths-ignore`), per the owner
rules in `AGENTS.project.md`.

## Known limitations

- **No local execution in the agent sandbox**: the container has no JDK, no Android SDK and no
  emulator, so the commands above were **not run** while this document was written (`[VAL-05]`). They
  are copied verbatim from `ci.yml` and `release.yml`, not invented.
- Instrumented tests need an emulator; they cannot run on a headless machine without KVM.
- Lint counts and unit-test counts are published by `ci.yml` to the workflow step summary; read them
  there rather than from a local run when reviewing a pull request.
- Nothing here replaces a manual check of a release APK on a real device (`[MOB-04]`).

Language: English (`[DOC-13]`).
