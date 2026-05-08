# Security Policy

Chuck_Norris is a small Android app with no current secrets, user accounts, payments, or private backend. Security expectations still apply because Android release signing, Play Store credentials, and generated artifacts are sensitive.

## Reporting

Do not file public issues with signing keys, Play Store credentials, private release config, local machine paths, or exploit details. Report sensitive findings privately to the project owner.

## Baseline Rules

- Never commit `local.properties`, signing keys, keystores, Play Store credentials, API keys, private release config, APK/AAB artifacts, or generated build output.
- Keep release signing material outside the repository.
- Treat third-party API responses as untrusted input.
- Keep networking simple and HTTPS-only.
- Validate release workflow changes with the relevant Gradle command before shipping.

## Before Release Or Merge To `main`

- Run focused tests and a debug or release build as appropriate.
- Review `git diff` for signing material, local machine paths, generated artifacts, and private release config.
- Request the workspace manual pre-merge persona review gate for `main`.
