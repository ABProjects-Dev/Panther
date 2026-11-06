# Security Policy

## Supported versions

Only the latest published release receives security fixes.

## Reporting a vulnerability

Do **not** open a public issue for security problems. Report them privately to the
project owner through GitHub's "Report a vulnerability" advisory feature
(Security -> Advisories -> New draft security advisory).

Please include: affected version, Android version, reproduction steps, and impact.

## Secret handling

Signing keys, `signing.properties`, keystore passwords, and API tokens must never be
committed. They are supplied to CI exclusively through GitHub Actions secrets.
See [docs/SECRETS_SETUP.md](docs/SECRETS_SETUP.md).
