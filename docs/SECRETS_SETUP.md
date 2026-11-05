# GitHub Secrets Setup for Panther Project

## Required Secrets
The following secrets must be configured in the repository settings (Settings -> Secrets and variables -> Actions) for the `release.yml` workflow to function correctly.

### 1. KEYSTORE_PASSWORD
- **Description**: The password for the Android Keystore file.
- **Security**: Never commit this to the repository.

### 2. KEY_ALIAS
- **Description**: The alias of the signing key inside the Keystore.
- **Example**: `panther_release_key`

### 3. KEY_PASSWORD
- **Description**: The password for the specific key alias (if different from Keystore password).
- **Security**: Never commit this to the repository.

### 4. KEYSTORE_BASE64
- **Description**: The Base64 encoded content of the `.jks` or `.keystore` file.
- **How to generate**:
  ```bash
  base64 release.jks
  ```
- **Security**: The Keystore file must be kept outside the repository (Rule 13 of AGENTS.md).

## Permissions
- The `release.yml` workflow requires `contents: write`, `packages: write`, `actions: write`, and `deployments: write` permissions.
- The `cleanup.yml` workflow requires `actions: write` and `contents: read`.
- Other workflows use `contents: read` only.

## Security Best Practices (Rule 13)
- Never hardcode passwords or keys in workflow files.
- Use `base64` encoding for binary files like Keystore.
- Limit permissions to the minimum required for each job.
- Use `concurrency` to prevent overlapping runs.
- Do not print Secrets in logs (GitHub Actions masks them automatically, but avoid logging variable names).

## Maintenance
- Review Secrets periodically.
- Rotate Keystore passwords if compromised (requires new Keystore and release process).
- Ensure `cleanup.yml` is running daily to manage storage.
