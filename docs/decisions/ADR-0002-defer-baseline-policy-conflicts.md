# ADR-0002: Defer the three baseline policy conflicts to the owner

Status: proposed · Date: 2026-11-11

## Context

Adopting the *Agent Operating Baseline* 1.0.0 (ADR-0001) exposes three places where the project's
published behaviour conflicts with a baseline `MUST`. Each one changes something users, stores or
release consumers can observe, so an agent may not resolve it alone (`[SCOPE-03]`, `[APPR-01]`), and
the baseline itself forbids treating a conflict with a `MUST` as an implicit waiver (`[R-04]`).

| id | baseline rule | current project behaviour | evidence |
|---|---|---|---|
| PD-001 | `[REL-07]`: artifacts named `<project>-<version>-<platform>.<ext>` | `Panther Vx.y.z.apk` (+ `.sha256`) | `app/build.gradle.kts:87`, `docs/RELEASE_GUIDE.md:7`, `RELEASES.md` v1.1.0 entry |
| PD-002 | `[REL-09]`: annotated tag message in English | Persian tag descriptions | `docs/RELEASE_GUIDE.md:16`, `RELEASES.md:4`, `CHANGELOG.md` v1.1.0 entry |
| PD-003 | `[DOC-17]`: translated README as `README.fa.md` beside the canonical file | Persian README at `.github/README.md` | `.github/README.md`, `README.md:3` |

## Options

| option | pros | cons | risk |
|---|---|---|---|
| Rename/reword everything now to satisfy the baseline literally | zero open conflicts | changes published artifact names and tag history; breaks the GitHub homepage rendering of the Persian README; contradicts the owner's recorded intent | high |
| Keep current behaviour and record a deviation | no user-visible change | deviation is only legitimate for `SHOULD` (`[R-03]`), not for a `MUST`; the conflict stays open | medium |
| Defer to the owner with an explicit, dated decision request | honest, reversible, blocks releases until decided | `PD-001`–`PD-003` remain open | low |

## Decision

Record the three conflicts as `PD-001`–`PD-003` in `docs/PROJECT_STATUS.md`, change nothing about the
current published behaviour, and require an owner decision before the next tagged release.

## Rationale

The conflicts are about outward-facing format, not internal correctness. Changing an artifact name or
a tag message retroactively has costs (download links, checksum records, release history) that exceed
the benefit of literal compliance, while the decision itself belongs to the owner (`[D-01]`).

## Consequences

- easier: no published behaviour changes without the owner's word; the conflict is documented instead
  of silently drifting.
- harder: a release cannot be tagged while these remain undecided; review checklists must carry them.
- now costly to reverse: once a release ships under the current naming, changing it later needs a
  migration note in `CHANGELOG.md` for consumers.
- follow-up: on decision, update `app/build.gradle.kts`, `release.yml`, `docs/RELEASE_GUIDE.md`,
  `RELEASES.md`, `README.md`, `.github/README.md` in one change (`[SYNC-01]`).

Language: English (`[DOC-13]`).
