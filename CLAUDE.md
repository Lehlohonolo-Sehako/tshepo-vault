# CLAUDE.md — Tshepo project spec

This file is read by Claude Code at the start of every session. It is the single source of truth for how to work on this codebase.

---

## What this project is

**Tshepo** is a bank-attested proof-of-funds Verifiable Credential app built on Investec Programmable Banking.
Holders connect their Investec account (read-only), issue selectively-disclosable SD-JWT VCs, and present only chosen claims to verifiers. Raw transactions are **never persisted** — privacy by design.

All 6 phases are complete. The app builds a clean production JAR (`./mvnw verify -DskipTests`).

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
| Frontend        | React 19 · TypeScript · Webpack 5    |
| Scaffold        | JHipster 9.0.0 (`tshepo-app.jdl`)    |
| Package         | `app.tshepo`                         |
| Base name       | `tshepoVault`                        |
| App port        | 9060 (webpack) · 8080 (Spring Boot)  |

---

## Repository layout

```
scripts/
  start.sh          Developer console (interactive — recommended for new devs)
  dev.sh            CI/automation startup
  seed.sh           Pre-populate database with demo user + credentials

design/
  landing.html      Marketing landing page prototype
  index.html        Holder app + verifier prototype

src/main/resources/
  swagger/api.yml              OpenAPI 3.1 contract (api-first)
  config/liquibase/            Database migrations
  config/application-dev.yml  Dev config (H2, fixture profile, port 9060)
  config/application-prod.yml Prod config (PostgreSQL)

src/main/java/app/tshepo/
  integration/investec/
    InvestecClient.java          Adapter interface
    FixtureInvestecClient.java   Hard-coded SA fixture data (default dev)
    LiveInvestecClient.java      HTTP client vs Investec OpenAPI (investec-live profile)
    InvestecProperties.java      Config binding for INVESTEC_* env vars
  service/
    PredicateService.java          In-memory claim computation; raw data discarded after
    CredentialIssuanceService.java SD-JWT build + IssuedClaim persistence
    SdJwtService.java              Selective disclosure token build/strip
    VerificationService.java       Presentation verification
    BankConnectService.java        Bank connection lifecycle + precomputed claims cache
    HolderProvisioningService.java OAuth user provisioning
    ApiKeyService.java             Verifier API key CRUD
    IssuerKeyService.java          EC P-256 key management + DID document
  web/rest/
    InvestecOAuthController.java   GET /api/auth/investec/authorize (OAuth + JWT issuance)
    CredentialsController.java     Credential CRUD + presentation
    VerifyController.java          Public + metered verify endpoints
  domain/                          JPA entities
  repository/                      Spring Data repositories

src/main/webapp/app/tshepo/
  HolderApp.tsx      Connect / Hub / Issue / Present screens
  LandingPage.tsx    Marketing landing page
  VerifierPage.tsx   Public /verify page
  OAuthCallback.tsx  JWT storage + session bootstrap
  api.ts             Generated OpenAPI client wrappers

logs/                Runtime logs (gitignored) — written by start.sh
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

- Always behind the `InvestecClient` adapter interface.
- Profile `default/dev` → `FixtureInvestecClient` (hard-coded Puseletso Lesenyane fixture data).
- Profile `investec-live` → `LiveInvestecClient` (HTTP/1.1, `SimpleClientHttpRequestFactory` for token, `JdkClientHttpRequestFactory` for API calls).
- Token response is read as `byte[]` to bypass content-type negotiation (sandbox returns `application/octet-stream`).
- Secrets go in `.env` (gitignored), never in code or git.

### Environment switching

Edit `.env` — comment/uncomment the sandbox or live block. No code changes needed. Restart with `./scripts/dev.sh --live` or `./scripts/start.sh`.

---

## Phases

| #   | Phase                    | Status  | Key deliverable                                                                                                |
| --- | ------------------------ | ------- | -------------------------------------------------------------------------------------------------------------- |
| 0   | Prerequisites & scaffold | ✅ Done | JHipster monolith generated                                                                                    |
| 1   | Domain model (JDL)       | ✅ Done | `entities.jdl` — Credential, IssuedClaim, VerifierApiKey, VerificationEvent, BankConnection                    |
| 2   | API contract             | ✅ Done | `swagger/api.yml` reviewed; openapi-generator wired; interfaces in `target/generated-sources/openapi`          |
| 3   | Investec adapter         | ✅ Done | `InvestecClient` + `FixtureInvestecClient` + `LiveInvestecClient` (sandbox + live tested)                      |
| 4   | Core services            | ✅ Done | PredicateService, CredentialIssuanceService, SdJwtService, VerificationService, ApiKeyService                  |
| 5   | React frontend           | ✅ Done | LandingPage → ConnectScreen (animated) → HubScreen (account info) → IssueScreen → PresentScreen + VerifierPage |
| 6   | Submission polish        | ✅ Done | README (full dev guide), LICENSE, `knowledge` file, seed script, developer console (`start.sh`)                |
| 7   | Deployment               | 🔜 Next | PostgreSQL + Railway/Render hosting + HTTPS + Investec OAuth redirect URI registration                         |

---

## Key known behaviours

- **Token endpoint**: sandbox returns `application/octet-stream`; `LiveInvestecClient` reads as `byte[]` + manual Jackson parse.
- **HTTP/1.1**: Local Docker sandbox does not support HTTP/2. `JdkClientHttpRequestFactory` with `HTTP_1_1` is set for all API calls.
- **Account ID resolution**: `CredentialIssuanceService.resolveAccountId()` calls `investecClient.getAccounts()` to get the real account ID — never hardcoded.
- **Fixture account**: `FixtureInvestecClient` returns "Puseletso Lesenyane", Private Bank Account •••• 4821, ~R42,300/month salary.
- **Error handling**: `InvestecOAuthController.authorize()` catches all exceptions and redirects to `/connect?error=<code>` — no raw JSON 500s shown to users.

---

## Domain model

| Entity              | Key fields                                                                                                                  |
| ------------------- | --------------------------------------------------------------------------------------------------------------------------- |
| `Credential`        | id, holderLogin, title, purpose, status (ACTIVE/REVOKED/EXPIRED), issuedAt, expiresAt, issuerDid, sdJwt TEXT, claimsSummary |
| `IssuedClaim`       | id, type (INFLOW/BALANCE/TENURE/…), operator, threshold, currency, met boolean → many-to-one Credential                     |
| `VerifierApiKey`    | id, label, keyHash, active, callCount, createdAt                                                                            |
| `VerificationEvent` | id, verifiedAt, result, disclosedClaims, apiKeyId (nullable)                                                                |
| `BankConnection`    | id, holderLogin, connectedAt, status, maskedAccount, accessToken, preComputedClaimsJson                                     |

---

## Secrets — never in code or git

```bash
# .env (gitignored) — Investec credentials
INVESTEC_CLIENT_ID=...
INVESTEC_CLIENT_SECRET=...
INVESTEC_API_KEY=...
INVESTEC_API_BASE=https://openapi.investec.com
INVESTEC_TOKEN_URI=https://openapi.investec.com/identity/v2/oauth2/token

# Production JWT secret
JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET=...
```

---

## Design system

| Token                | Value                                    |
| -------------------- | ---------------------------------------- |
| Charcoal (nav/brand) | `#1A1A1A`                                |
| Brand blue           | `#00A9E0`                                |
| Hover blue           | `#0089B8`                                |
| Success              | `#1D9E75`                                |
| Warning              | `#BA7517`                                |
| Danger               | `#b91c1c`                                |
| Page bg              | `#F7F7F6`                                |
| Border               | `#ECECEA`                                |
| Muted text           | `#6B6B6B`                                |
| Font                 | Plus Jakarta Sans 400/500 only           |
| Radii                | 12–16px                                  |
| Shadows              | soft only (`0 1px 3px rgba(0,0,0,0.06)`) |
