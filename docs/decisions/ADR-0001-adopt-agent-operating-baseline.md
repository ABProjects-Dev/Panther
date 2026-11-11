# ADR-0001: Adopt the Agent Operating Baseline 1.0.0 and move project rules into AGENTS.project.md

Status: accepted · Date: 2026-11-11

## Context

The repository's single instruction file went through three forms: the original numbered-rule
document (referenced across the project as `Rule N` and `section 19.1`), a universal baseline
`V1.0.1`, then `V1.1.0`, and finally — in commit `7775485`, pushed directly to `main` by the owner —
the *Agent Operating Baseline* `1.0.0`. The new file is deliberately **project-agnostic**: it contains
no reference to Panther, Android, Kotlin, Gradle or APK artifacts, and it requires
(`[ADOPT-01]`, `[ADOPT-04]`, §28, §29.8) that a project pin its baseline version, declare its domain
profile and keep its own rules in a separate project-rules file. It also replaces numeric section
citations with stable rule IDs (`[PROT-01]`, `[REL-07]`, …).

Consequently the project had two gaps: no file the baseline recognises as its project-rules file, and
45 documentation references to rule numbers that no longer exist.

## Options

| option | pros | cons | risk |
|---|---|---|---|
| Keep documentation as-is and treat the baseline as advisory | zero work | `[ADOPT-01]`/`[DOC-04]` unmet; every citation dangles | medium — rules become decorative |
| Re-add project rules into `AGENTS.md` | single file | violates `[PROT-01]`; the file is owner-only and read-only | high — protected-file breach |
| Add `AGENTS.project.md` + required docs, repoint citations to rule IDs | compliant, reversible, evidence-based | one larger documentation change | low |

## Decision

Adopt `AGENTS.md` 1.0.0 as the sole normative baseline, keep it untouched, and record all
project-specific content in a new `AGENTS.project.md` that pins the baseline version and declares the
Mobile profile (`§28.2`). Fill the missing documents the baseline makes mandatory for this project's
tier (`docs/PROJECT_STATUS.md`, `docs/TESTING.md`, `docs/decisions/`, `CONTRIBUTING.md`) and repoint
every stale numeric citation to the rule ID or project-rules section that now covers the same topic.

## Rationale

The baseline's own precedence ladder (`[PREC-01]`) places it above project documents, so the project
must conform to the file rather than the file to the project — and the file forbids editing itself.
A separate project-rules file is the only structure both documents allow, and it keeps one source of
truth per fact (`[DOC-11]`).

## Consequences

- easier: citations are stable (`[GIT-05]`, `[SEC-07]`) and survive future baseline revisions; a new
  session finds commands, conventions and off-limits paths in one place.
- harder: two instruction files must stay consistent; a baseline upgrade now also updates the pin in
  `AGENTS.project.md` (`[ADOPT-02]`).
- now costly to reverse: once docs cite rule IDs, reverting to numbered sections means re-editing every
  citation.
- follow-up: `PD-001`–`PD-003` (ADR-0002), a CI guard that fails on any change to `AGENTS.md`
  (`[PROT-06]`), and the missing Vazirmatn licence file.

Language: English (`[DOC-13]`).
