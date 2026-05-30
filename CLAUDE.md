# CLAUDE.md — Tshepo project spec

This file is read by Claude Code at the start of every session. It is the single source of truth for how to work on this codebase.

---

## What this project is

**Tshepo** is a bank-attested proof-of-funds Verifiable Credential app built on Investec Programmable Banking.
Holders connect their Investec account (read-only), issue selectively-disclosable SD-JWT VCs, and present only chosen claims to verifiers. Raw transactions are **never persisted** — privacy by design.

---

## Stack (pinned — do not upgrade without discussion)

| Concern         | Technology                           |
| --------------- | ------------------------------------ |
| Backend         | Spring Boot 4.0.3 · Java 21 · Maven  |
| Auth            | JWT via JHipster                     |
| Database        | PostgreSQL (prod) · H2 on-disk (dev) |
| Migrations      | Liquibase                            |
| VC layer        | SD-JWT VC via **Nimbus JOSE+JWT**    |
| Issuer identity | `did:web` — JWKS over HTTPS          |
| Frontend        | React 19 · TypeScript · Webpack      |
| Scaffold        | JHipster 9.0.0 (`tshepo-app.jdl`)    |
| Package         | `app.tshepo`                         |
| Base name       | `tshepoVault`                        |
| Port            | 8080                                 |

---

## Repository layout

```
design/                   UI prototype (source of truth for the React frontend)
  landing.html            Marketing landing page
  index.html              Holder app + verifier demo
  tshepo-*.jsx            Shared components — icons, UI primitives, cards, screens

src/main/resources/
  swagger/api.yml         OpenAPI 3.1 contract (api-first — reviewed before any impl)
  config/liquibase/       Database migrations (generated from entities.jdl)

src/main/java/app/tshepo/
  web/rest/               Controllers — implement the generated OpenAPI interfaces
  service/                Business logic (PredicateService, CredentialIssuanceService, …)
  domain/                 JPA entities
  repository/             Spring Data repositories

src/main/webapp/app/      React 19 SPA — match design/ artefacts pixel-for-pixel
tshepo-app.jdl            JDL application block (Phase 0, already applied)
entities.jdl              JDL entity model (Phase 1 — to be created)
```

---

## Development workflow rules

### API-first

`src/main/resources/swagger/api.yml` is **authored and reviewed before any controller implementation**. Controllers implement the generated interfaces — they never drift from the contract.

### Privacy by design — non-negotiable

- Never persist or log raw transactions. Ever.
- `PredicateService` processes data in memory and discards it immediately after computing the boolean result.
- Credentials certify thresholds, never exact figures.
- Presentations hide non-disclosed claims cryptographically.

### Investec integration

- Always behind an `InvestecClient` adapter interface.
- Two implementations selected by Spring profile:
  - `FixtureInvestecClient` (default/dev) — returns hard-coded SA banking sample data.
  - `LiveInvestecClient` (profile `investec-live`) — WebClient against Investec sandbox OAuth2 + API.
- Secrets go in environment variables, never in code or git.

### Phase discipline

Work in the numbered phases below. Pause after each phase for review before proceeding. Never skip ahead.

---

## Phases

| #   | Phase                    | Status  | Key deliverable                                                                                                   |
| --- | ------------------------ | ------- | ----------------------------------------------------------------------------------------------------------------- |
| 0   | Prerequisites & scaffold | ✅ Done | JHipster monolith generated                                                                                       |
| 1   | Domain model (JDL)       | ✅ Done | `entities.jdl` — Credential, IssuedClaim, VerifierApiKey, VerificationEvent, BankConnection                       |
| 2   | API contract             | ✅ Done | `swagger/api.yml` reviewed; openapi-generator wired; interfaces in `target/generated-sources/openapi`             |
| 3   | Investec adapter         | 🔜 Next | `InvestecClient` interface + `FixtureInvestecClient`; fixture drives all dev flows                                |
| 4   | Core services            | 🔜      | PredicateService, CredentialIssuanceService, PresentationService, VerificationService, ApiKeyService + unit tests |
| 5   | React frontend           | 🔜      | Match `design/` exactly; use generated OpenAPI client — no hand-written fetch calls                               |
| 6   | Submission polish        | 🔜      | README, LICENSE, `knowledge` file, seed script                                                                    |

---

## Domain model (Phase 1 target)

No raw transactions stored at any point.

| Entity              | Key fields                                                                                                                  |
| ------------------- | --------------------------------------------------------------------------------------------------------------------------- |
| `Credential`        | id, holderLogin, title, purpose, status (ACTIVE/REVOKED/EXPIRED), issuedAt, expiresAt, issuerDid, sdJwt TEXT, claimsSummary |
| `IssuedClaim`       | id, type (INFLOW/BALANCE/TENURE/…), operator, threshold, currency, met boolean → many-to-one Credential                     |
| `VerifierApiKey`    | id, label, keyHash, active, callCount, createdAt                                                                            |
| `VerificationEvent` | id, verifiedAt, result, disclosedClaims, apiKeyId (nullable)                                                                |
| `BankConnection`    | id, holderLogin, connectedAt, status — token metadata only, never raw data                                                  |

---

## API contract summary

Full spec: `src/main/resources/swagger/api.yml`

| Method            | Path                            | Auth                  |
| ----------------- | ------------------------------- | --------------------- |
| `POST`            | `/api/bank/connect`             | JWT                   |
| `GET`             | `/api/bank/status`              | JWT                   |
| `GET`             | `/api/bank/claims`              | JWT                   |
| `POST`            | `/api/credentials/issue`        | JWT                   |
| `GET`             | `/api/credentials`              | JWT                   |
| `POST`            | `/api/credentials/{id}/present` | JWT                   |
| `POST`            | `/api/credentials/{id}/revoke`  | JWT                   |
| `POST`            | `/api/verify`                   | public (rate-limited) |
| `POST`            | `/v1/verify`                    | `X-API-Key` (metered) |
| `GET/POST/DELETE` | `/api/verifier-keys`            | JWT                   |
| `GET`             | `/.well-known/did.json`         | public                |
| `GET`             | `/.well-known/jwks.json`        | public                |

---

## Design system

Source of truth for every React component: `design/` directory.

| Token                | Value                                    |
| -------------------- | ---------------------------------------- |
| Charcoal (nav/brand) | `#1A1A1A`                                |
| Brand blue           | `#00A9E0`                                |
| Hover blue           | `#0089B8`                                |
| Success              | `#1D9E75`                                |
| Warning              | `#BA7517`                                |
| Page bg              | `#F7F7F6`                                |
| Border               | `#ECECEA`                                |
| Muted text           | `#6B6B6B`                                |
| Font                 | Plus Jakarta Sans 400/500 only           |
| Radii                | 12–16px                                  |
| Shadows              | soft only (`0 1px 3px rgba(0,0,0,0.06)`) |

Two credential card variants: **Passport** (premium digital-ID) and **Minimal** (receipt-style). Hub toggles between them.

---

## Running locally

```bash
# Install JS deps (once)
npm install

# Dev: two terminals
./mvnw                    # Spring Boot on :8080
npm start                 # React dev server (HMR)

# Tests
./mvnw verify             # backend
npm test                  # frontend unit
npm run e2e               # Cypress (app must be running)
```

---

## Secrets — never in code or git

```bash
# Investec live profile
INVESTEC_CLIENT_ID=...
INVESTEC_CLIENT_SECRET=...
INVESTEC_API_BASE=https://openapi.investec.com

# Production JWT secret — override application-secret-samples.yml
JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET=...
```

---

## Key files to know

| File                                             | Purpose                                       |
| ------------------------------------------------ | --------------------------------------------- |
| `tshepo-app.jdl`                                 | JHipster application config (already applied) |
| `entities.jdl`                                   | Domain entity model (Phase 1)                 |
| `src/main/resources/swagger/api.yml`             | OpenAPI 3.1 contract                          |
| `src/main/resources/config/application-dev.yml`  | Dev config (H2, fixture profile)              |
| `src/main/resources/config/application-prod.yml` | Prod config (PostgreSQL)                      |
| `src/main/docker/postgresql.yml`                 | Docker Compose for local Postgres             |
| `design/index.html`                              | Holder app prototype (open in browser)        |
| `design/landing.html`                            | Marketing landing page prototype              |
