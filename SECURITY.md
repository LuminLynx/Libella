# Security Policy

Libella is a learning app: an Android client, a FastAPI/Postgres
backend, and a regression/grading pipeline that calls an LLM
provider. It handles an authentication layer and provider
credentials, so it has a real security surface and we take
reports seriously.

## Reporting a vulnerability

Please use **GitHub's private vulnerability reporting** for this
repository:

https://github.com/LuminLynx/Libella/security/advisories/new

Do **not** open a public issue or pull request for a security
problem — that discloses it before a fix is available. The
private advisory channel keeps the report confidential until
it's resolved.

If private reporting is unavailable to you, open a regular issue
that says only "security issue, requesting a private channel"
with **no details**, and we'll follow up.

## Do not include live secrets in a report

If your finding involves an exposed credential — an
`AI_PROVIDER_API_KEY`, `DATABASE_URL`, `JWT_SECRET`, or similar —
describe **where** it is exposed rather than pasting the value.
Any credential disclosed to us is treated as compromised and
rotated; pasting it into a report only widens the exposure.

## Scope

In scope:

- The backend (`backend/`) — API, auth, the grading pipeline.
- The Android app (`app/`).
- Credential or secret handling across the repo and its CI.

Out of scope:

- The published curriculum content itself (units, regression
  sets) — these are learning material, not an attack surface.
- Volumetric or rate-limit findings against any demo deployment.
- Issues that require a compromised developer machine or
  already-leaked credentials to exploit.

## Supported versions

Libella is pre-1.0 and ships from `main`. Security fixes land on
`main`; there is no back-port matrix. There are no
version-pinned releases to support yet.
