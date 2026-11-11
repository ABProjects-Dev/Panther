# Contributing to Panther

Panther is an offline-first Android workout-planning app with a Persian RTL interface. This file covers
the practical path from a checkout to a reviewable change; the normative rules live in `AGENTS.md`
(agent baseline) and `AGENTS.project.md` (project rules).

## Before you start

- Read `AGENTS.md` — the operating baseline (`[R-01]`). It is protected and must never be edited.
- Read `AGENTS.project.md` for the stack, commands, conventions and off-limits paths.
- Check `docs/PROJECT_STATUS.md` for current work, confirmed defects and pending decisions.

## Set up

Requirements: JDK 17, Android SDK 36, and the repository's Gradle wrapper (9.8.2).

```bash
./gradlew :app:assembleDebug          # build
./gradlew :app:testDebugUnitTest      # unit tests
./gradlew :app:lintDebug              # lint
```

Instrumented tests need an emulator (API 29): `./gradlew :app:connectedDebugAndroidTest`. Full details:
`docs/TESTING.md`.

## Workflow

1. Work on a single branch — `dev`, or the fixed Arena session branch. Do not create extra branches.
2. Commit in logical, verified steps using Conventional Commits, in English.
3. Open a pull request **only when the owner asks for one**; merge **only when the owner asks for that
   merge**.
4. `ci.yml` runs on pull requests that touch non-documentation paths; documentation-only pull requests
   skip it. Wait for the owner's approval after CI is green.
5. Never push a release tag or publish a release without the owner's explicit `tag` request.

## Change hygiene

- Update the documents the change invalidates in the same commit (`AGENTS.md` `[SYNC-01]`,
  `[SYNC-02]`): `CHANGELOG.md` under *Unreleased*, plus `docs/REQUIREMENTS.md`,
  `docs/ARCHITECTURE.md`, `docs/API.md` or `docs/TESTING.md` where they apply.
- Add or update tests for behaviour changes; never weaken a test to make it pass (`[VAL-03]`).
- Keep English `README.md` and Persian `.github/README.md` in sync.
- Never commit signing material (`*.jks`, `*.keystore`, `signing.properties`) or secrets.
- Keep `app/schemas/*.json` in step with the Room database; ship the migration with the schema change.

## Documentation language

Technical documents, comments, commit messages and pull-request text are English (`[DOC-13]`). The
README may be Persian, and this project keeps its Persian README at `.github/README.md` (see `PD-003` in
`docs/PROJECT_STATUS.md`).

## Reporting a problem

Security issues: follow `SECURITY.md`, not a public issue. Functional bugs: include the version, the
device/Android level, reproduction steps and what you expected (`[DOC-14]`).
