# Tshepo — Bank-attested proof-of-funds credentials

> **"Tshepo"** means _trust_ and _hope_ in Sesotho and Setswana.

Tshepo turns an Investec account into a private, bank-attested Verifiable Credential.
A holder proves **"average monthly inflow ≥ R30,000"** to a landlord or lender — without revealing a single transaction.

---

## Table of contents

1. [What it does](#what-it-does)
2. [Architecture overview](#architecture-overview)
3. [Quick start](#quick-start)
4. [Developer guide](#developer-guide)
   - [Prerequisites](#prerequisites)
   - [Running locally](#running-locally)
   - [Environment configuration](#environment-configuration)
   - [Switching between fixture / sandbox / live](#switching-environments)
5. [User flows](#user-flows)
   - [Holder — connect, issue, present](#holder-flow)
   - [Verifier — verify a presentation](#verifier-flow)
6. [API reference](#api-reference)
7. [Credential format](#credential-format)
8. [Privacy by design](#privacy-by-design)
9. [Project layout](#project-layout)
10. [Stack](#stack)
11. [Deployment](#deployment)

---

## What it does

| Step        | What happens                                                                                                                                                                                                                                     |
| ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **Connect** | Holder links their Investec account via read-only OAuth2. Tshepo fetches up to 12 months of statements.                                                                                                                                          |
| **Issue**   | Backend computes claim predicates entirely in memory (avg inflow, balance, tenure, salary continuity, overdraft-free). Raw transactions are discarded immediately. A selectively-disclosable SD-JWT VC is signed with the issuer key and stored. |
| **Present** | Holder chooses exactly which claims to reveal. Non-selected claims are cryptographically stripped — the verifier cannot even count them.                                                                                                         |
| **Verify**  | Any verifier pastes the token at `/verify` (browser) or calls `POST /v1/verify` (API). They receive a `valid/invalid` verdict showing only the disclosed claims.                                                                                 |

---

## Architecture overview

```
Browser (React 19 SPA)
    │
    │  JWT (JHipster)
    ▼
Spring Boot 4 API  ─────────────────────────────────────────┐
    │                                                         │
    ├── InvestecClient (interface)                           │
    │       ├── FixtureInvestecClient  (dev profile)         │
    │       └── LiveInvestecClient     (investec-live profile)│
    │                │                                        │
    │                └──▶  Investec OpenAPI / Docker Sandbox  │
    │                                                        │
    ├── PredicateService  (in-memory, discards raw data)     │
    ├── CredentialIssuanceService                            │
    ├── SdJwtService  (Nimbus JOSE+JWT)                      │
    ├── VerificationService                                  │
    └── H2 (dev) / PostgreSQL (prod)  ───────────────────────┘
```

**Trust model note:** In the ideal production deployment, Investec would act as the credential issuer — countersigning or directly issuing the SD-JWT from the bank's own key. The current build uses Tshepo's issuer key (`did:web`) to sign claims computed from live Investec data. This demonstrates the full technical stack while acknowledging that bank-direct issuance would provide stronger cryptographic attestation.

---

## Quick start

```bash
# 1. Clone and install
git clone <repo-url>
cd tshepo-vault
npm install

# 2. Start the developer console (recommended)
./scripts/start.sh
# → interactive mode selection, prerequisite checks, clean status output

# 3. Open the app
open http://localhost:9060
```

---

## Developer guide

### Prerequisites

| Tool   | Version            | Notes                            |
| ------ | ------------------ | -------------------------------- |
| Java   | 21                 | [Adoptium](https://adoptium.net) |
| Node   | 22+                | [nodejs.org](https://nodejs.org) |
| Docker | Any recent         | Only needed for sandbox mode     |
| Maven  | Bundled (`./mvnw`) | No separate install needed       |

### Running locally

There are three ways to run the application:

#### Option 1 — Developer console (recommended for new developers)

Interactive startup with prerequisite checks, mode selection, and clean terminal output. Spring Boot and webpack logs are written to `logs/` — only errors surface in the terminal.

```bash
./scripts/start.sh
```

The console will ask:

- `[1] Fixture mode` — no credentials needed, uses built-in SA banking sample data
- `[2] Sandbox mode` — connects to the Investec Docker simulator on `localhost:3000`
- `[3] Live account` — connects to your real Investec account via `openapi.investec.com`

#### Option 2 — Script (for CI or automation)

```bash
# Fixture mode (default)
./scripts/dev.sh

# Investec sandbox or live account
./scripts/dev.sh --live
```

#### Option 3 — Manual (two terminals)

```bash
# Terminal 1 — Spring Boot
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 2 — React dev server (hot reload)
npm start
```

App → `http://localhost:9060`  
API → `http://localhost:8080`

#### Seed demo data

After starting in fixture mode, run the seed script to pre-populate the database with a demo user (Puseletso Lesenyane) and two credentials:

```bash
./scripts/seed.sh
```

### Environment configuration

Create a `.env` file in the project root (gitignored). The app reads it automatically when started via `./scripts/dev.sh --live` or `./scripts/start.sh`.

```bash
# Required for sandbox or live mode
INVESTEC_CLIENT_ID='your-client-id'
INVESTEC_CLIENT_SECRET='your-client-secret'
INVESTEC_API_KEY='your-api-key'

# For Docker sandbox (localhost:3000)
INVESTEC_API_BASE='http://localhost:3000'
INVESTEC_TOKEN_URI='http://localhost:3000/identity/v2/oauth2/token'

# For live account (openapi.investec.com)
# INVESTEC_API_BASE='https://openapi.investec.com'
# INVESTEC_TOKEN_URI='https://openapi.investec.com/identity/v2/oauth2/token'
```

Get credentials from the [Investec Developer Portal](https://developer.investec.com).

### Switching environments

Comment/uncomment the relevant block in `.env`:

```bash
# ── Sandbox ──────────────────────────────────────────────
INVESTEC_API_BASE='http://localhost:3000'
INVESTEC_TOKEN_URI='http://localhost:3000/identity/v2/oauth2/token'

# ── Live ─────────────────────────────────────────────────
# INVESTEC_API_BASE='https://openapi.investec.com'
# INVESTEC_TOKEN_URI='https://openapi.investec.com/identity/v2/oauth2/token'
```

No code changes required. Restart with `./scripts/dev.sh --live` after editing `.env`.

---

## User flows

### Holder flow

A _holder_ is an Investec account owner who wants to prove something about their finances without revealing their statements.

**Step 1 — Land on the app**

Open `http://localhost:9060`. The landing page explains the concept. Click **"Connect with Investec"** in the top-right nav or hero CTA.

**Step 2 — Connect screen**

You land on `/connect`. This screen explains what Tshepo will access (read-only) and what it will do with the data (compute predicates and discard raw data). Click **"Connect Investec account"** to start the OAuth flow.

The button animates through the connection steps:

1. Authorising read-only access via Investec OAuth
2. Pulling 12 months of statements
3. Computing claims in memory
4. Discarding raw transactions — privacy by design

**Step 3 — My credentials hub**

After connecting, you land on the hub. Your account details (name, masked account number, connection date) appear at the top. From here you can:

- **Issue a new credential** — click "New credential" or "Issue first credential"
- **Present a credential** — click any existing credential card to open the presentation flow

**Step 4 — Issue a credential**

Select which claims to certify. Only claims your account _meets_ are selectable (unmet claims are greyed out). Add a title, optional purpose, then click **"Issue credential"**.

Available claim types:

| Claim                            | What it certifies                                  |
| -------------------------------- | -------------------------------------------------- |
| Average monthly inflow ≥ R30,000 | Mean monthly credit over the past 6 months         |
| Current balance ≥ R50,000        | Account balance as of today                        |
| Account open ≥ 6 months          | Account tenure                                     |
| Account open ≥ 12 months         | Account tenure                                     |
| 3+ months consistent salary      | Consecutive months with a large credit (≥ R20,000) |
| No overdraft in 6 months         | No negative running balance in the window          |

**Step 5 — Present a credential**

Click a credential card. Select which claims to reveal using the toggles — hidden claims are cryptographically removed from the presentation token. The verifier will not be able to see, or even know the count of, unrevealed claims.

Copy the SD-JWT token or scan the QR code to share with a verifier.

### Verifier flow

A _verifier_ is a landlord, lender, visa officer, or anyone else who needs to confirm a financial fact.

**Browser verification (public, no login)**

1. Open `http://localhost:9060/verify`
2. Paste the SD-JWT presentation token
3. Click **Verify**
4. See the result: `VALID` or `INVALID`, with only the disclosed claims shown

**API verification (metered, for integrations)**

```bash
POST /v1/verify
X-API-Key: your-api-key
Content-Type: application/json

{
  "token": "<sd-jwt-presentation-token>"
}
```

Response:

```json
{
  "valid": true,
  "holderDid": "did:tshepo:puseletso",
  "issuerDid": "did:web:tshepo.app",
  "disclosedClaims": [
    {
      "type": "INFLOW",
      "operator": "GTE",
      "threshold": 30000,
      "currency": "ZAR",
      "met": true
    }
  ],
  "verifiedAt": "2026-05-31T08:00:00Z"
}
```

**Managing API keys**

```bash
# Create a key
POST /api/verifier-keys
Authorization: Bearer <jwt>
{"label": "my-integration"}

# List keys
GET /api/verifier-keys
Authorization: Bearer <jwt>

# Revoke a key
DELETE /api/verifier-keys/{id}
Authorization: Bearer <jwt>
```

---

## API reference

Full OpenAPI 3.1 spec: `src/main/resources/swagger/api.yml`
Interactive docs (when running): `http://localhost:8080/swagger-ui/index.html`

### Bank connection

| Method | Path                           | Auth | Description                              |
| ------ | ------------------------------ | ---- | ---------------------------------------- |
| `GET`  | `/api/auth/investec/authorize` | —    | Initiate Investec OAuth connect flow     |
| `POST` | `/api/bank/connect`            | JWT  | Exchange auth code for bank connection   |
| `GET`  | `/api/bank/status`             | JWT  | Connection status + masked account       |
| `GET`  | `/api/bank/claims`             | JWT  | Available claims with met/not-met status |

### Credentials

| Method | Path                            | Auth | Description                                  |
| ------ | ------------------------------- | ---- | -------------------------------------------- |
| `POST` | `/api/credentials/issue`        | JWT  | Issue a new SD-JWT VC                        |
| `GET`  | `/api/credentials`              | JWT  | List all credentials for the holder          |
| `GET`  | `/api/credentials/{id}`         | JWT  | Get a single credential                      |
| `POST` | `/api/credentials/{id}/present` | JWT  | Generate a selective-disclosure presentation |
| `POST` | `/api/credentials/{id}/revoke`  | JWT  | Revoke a credential                          |

### Verification

| Method | Path          | Auth                  | Description                              |
| ------ | ------------- | --------------------- | ---------------------------------------- |
| `POST` | `/api/verify` | Public (rate-limited) | Verify a presentation (browser-friendly) |
| `POST` | `/v1/verify`  | `X-API-Key` (metered) | Verify at scale                          |

### Verifier API keys

| Method   | Path                      | Auth | Description       |
| -------- | ------------------------- | ---- | ----------------- |
| `GET`    | `/api/verifier-keys`      | JWT  | List API keys     |
| `POST`   | `/api/verifier-keys`      | JWT  | Create an API key |
| `DELETE` | `/api/verifier-keys/{id}` | JWT  | Revoke an API key |

### Well-known endpoints

| Method | Path                     | Auth   | Description                            |
| ------ | ------------------------ | ------ | -------------------------------------- |
| `GET`  | `/.well-known/did.json`  | Public | DID document for `did:web` resolution  |
| `GET`  | `/.well-known/jwks.json` | Public | JWKS for SD-JWT signature verification |

### Example — issue a credential

```bash
# 1. Connect (opens browser redirect flow — returns JWT via /oauth-callback)
GET http://localhost:8080/api/auth/investec/authorize

# 2. Issue a credential
curl -X POST http://localhost:8080/api/credentials/issue \
  -H "Authorization: Bearer <jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Proof of income",
    "purpose": "Rental application",
    "claims": [
      {"type": "INFLOW", "operator": "GTE", "threshold": 30000, "currency": "ZAR", "periodMonths": 6}
    ],
    "validityDays": 180
  }'

# 3. Present (disclose only INFLOW)
curl -X POST http://localhost:8080/api/credentials/{id}/present \
  -H "Authorization: Bearer <jwt>" \
  -H "Content-Type: application/json" \
  -d '{"disclosedClaimTypes": ["INFLOW"]}'

# 4. Verify
curl -X POST http://localhost:8080/api/verify \
  -H "Content-Type: application/json" \
  -d '{"token": "<sd-jwt-presentation>"}'
```

---

## Credential format

Tshepo implements **SD-JWT VC** ([IETF draft-ietf-oauth-selective-disclosure-jwt](https://datatracker.ietf.org/doc/draft-ietf-oauth-selective-disclosure-jwt/)) using [Nimbus JOSE+JWT](https://connect2id.com/products/nimbus-jose-jwt).

| Property          | Value                                                                              |
| ----------------- | ---------------------------------------------------------------------------------- |
| Signing algorithm | ES256 (EC P-256)                                                                   |
| Issuer identity   | `did:web` — JWKS at `/.well-known/jwks.json`                                       |
| Disclosure format | `BASE64URL(["<salt>","<claim_name>",<claim_value>])`                               |
| JWT payload       | `_sd` array of SHA-256 hashes — raw claim values never appear in the JWT body      |
| Presentation      | `<issuer-jwt>~<disclosure-1>~<disclosure-2>~` — only selected disclosures included |

---

## Privacy by design

These are enforced in code, not just policy:

| Rule                                  | Enforcement                                                                                           |
| ------------------------------------- | ----------------------------------------------------------------------------------------------------- |
| Raw transactions never persisted      | `PredicateService` processes in-memory; no transaction entity exists in the domain model              |
| Credentials certify thresholds only   | Claims store `"≥ R30,000"`, never the actual figure                                                   |
| Selective disclosure is cryptographic | Non-revealed disclosures are stripped before the token is returned; verifier cannot infer their count |
| Read-only bank access                 | Investec OAuth scope is `accounts` only — Tshepo can never initiate a payment                         |
| Tokens not stored at rest             | `BankConnection` stores only connection metadata (masked account, timestamp)                          |

---

## Project layout

```
scripts/
  start.sh          Developer console — interactive startup with status output
  dev.sh            CI/automation startup script
  seed.sh           Pre-populate database with demo user + credentials

design/
  landing.html      Marketing landing page prototype (open in browser)
  index.html        Holder app + verifier prototype (open in browser)

src/main/resources/
  swagger/api.yml                OpenAPI 3.1 contract (api-first)
  config/liquibase/              Database migrations (Liquibase)
  config/application-dev.yml     Dev config (H2, fixture profile)
  config/application-prod.yml    Prod config (PostgreSQL)

src/main/java/app/tshepo/
  integration/investec/
    InvestecClient.java          Adapter interface
    FixtureInvestecClient.java   Hardcoded SA fixture data (default dev)
    LiveInvestecClient.java      HTTP client against Investec OpenAPI (investec-live)
    InvestecProperties.java      Configuration binding
  service/
    PredicateService.java        In-memory claim computation — discards raw data
    CredentialIssuanceService.java  SD-JWT issuance + claim storage
    SdJwtService.java            SD-JWT build / selective disclosure
    VerificationService.java     Presentation verification
    BankConnectService.java      Bank connection lifecycle
    ApiKeyService.java           Verifier API key management
  web/rest/
    InvestecOAuthController.java OAuth connect + JWT issuance
    CredentialsController.java   Credential CRUD + presentation
    VerifyController.java        Public + metered verify endpoints
  domain/                        JPA entities (Credential, IssuedClaim, BankConnection, …)

src/main/webapp/app/tshepo/
  HolderApp.tsx    Connect / Hub / Issue / Present screens
  LandingPage.tsx  Marketing landing page (React)
  VerifierPage.tsx Public /verify page
  OAuthCallback.tsx  JWT storage + session bootstrap
  api.ts           Generated OpenAPI client wrappers

logs/               Runtime logs (gitignored)
  spring.log        Spring Boot output
  webpack.log       Webpack dev server output
```

---

## Stack

| Concern         | Technology                           |
| --------------- | ------------------------------------ |
| Backend         | Spring Boot 4.0.3 · Java 21 · Maven  |
| Auth            | JWT (JHipster scaffold)              |
| Database        | PostgreSQL (prod) · H2 on-disk (dev) |
| Migrations      | Liquibase                            |
| VC layer        | SD-JWT VC via Nimbus JOSE+JWT        |
| Issuer identity | `did:web` — JWKS served over HTTPS   |
| Frontend        | React 19 · TypeScript · Webpack 5    |
| API contract    | OpenAPI 3.1 (api-first)              |
| Scaffold        | JHipster 9.0.0                       |

---

## Deployment

> Deployment is planned for a future iteration.

**What you need:**

| Requirement                                          | Notes                                                                             |
| ---------------------------------------------------- | --------------------------------------------------------------------------------- |
| PostgreSQL instance                                  | Railway, Supabase, Render, or self-hosted                                         |
| `JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET` | Generate a strong base64 secret — never use the dev default                       |
| `INVESTEC_CLIENT_ID / SECRET / API_KEY`              | From the Investec Developer Portal                                                |
| HTTPS endpoint                                       | Required for `did:web` and live Investec OAuth                                    |
| Investec redirect URI                                | Register `https://your-domain/api/auth/investec/callback` in the Developer Portal |

**Build the production JAR:**

```bash
./mvnw -Pprod clean verify -DskipTests
java -jar target/tshepo-vault-*.jar
```

**Or with Docker (PostgreSQL):**

```bash
docker compose -f src/main/docker/postgresql.yml up -d
./mvnw -Pprod
```

---

## License

MIT — see [LICENSE](LICENSE).
