# Tshepo — Bank-attested proof-of-funds credentials

> **"Tshepo"** means _trust_ and _hope_ in Sesotho and Setswana.

Tshepo turns an Investec account into a private, bank-attested Verifiable Credential.
A holder proves "average monthly inflow ≥ R30,000" to a landlord or lender — without revealing a single transaction. Privacy-by-design is the core selling point.

---

## What it does

1. **Connect** — holder links their Investec account (read-only OAuth2).
2. **Issue** — backend computes predicates in memory (avg inflow, balance, tenure…), builds a selectively-disclosable SD-JWT VC signed by the issuer key, and persists only the boolean results. Raw transactions are never stored.
3. **Present** — holder picks exactly which claims to reveal; a QR/token is generated. Claims not selected are cryptographically hidden — the verifier cannot even tell they exist.
4. **Verify** — verifier pastes the token (or calls the metered REST API) and receives a clear valid/invalid verdict showing only the disclosed claims.

---

## Who it's for

| Actor                                         | Surface                                                   |
| --------------------------------------------- | --------------------------------------------------------- |
| **Holder** (Investec customer)                | Authenticated SPA — hub-and-spoke around "My credentials" |
| **Verifier** (landlord, lender, visa officer) | Public `/verify` page + metered REST endpoint             |

---

## Monetisation

- **Issuance fee** — charged per credential issued.
- **Metered verify API** — pay-as-you-go `POST /v1/verify` endpoint; call count tracked per API key.

---

## How Investec Programmable Banking is used

Tshepo uses Investec's [Programmable Banking](https://developer.investec.com/za/home) OAuth2 + Accounts API to:

- Authenticate the holder with read-only scope.
- Pull up to 12 months of account transactions.
- Compute claim predicates in memory (e.g. mean monthly credit ≥ R30,000).
- Immediately discard all raw transaction data after predicate evaluation.

No money movement, no write access, no raw data at rest — ever.

---

## Stack

| Layer           | Technology                                         |
| --------------- | -------------------------------------------------- |
| Backend         | Spring Boot 4.0.3 · Java 21 · Maven                |
| Auth            | JWT (JHipster)                                     |
| Database        | PostgreSQL (prod) · H2 on-disk (dev)               |
| Migrations      | Liquibase                                          |
| VC layer        | SD-JWT VC via Nimbus JOSE+JWT                      |
| Issuer identity | `did:web` — JWKS served over HTTPS                 |
| Frontend        | React 19 · TypeScript · Webpack                    |
| API contract    | OpenAPI 3.1 (`src/main/resources/swagger/api.yml`) |
| E2E tests       | Cypress                                            |
| Scaffold        | JHipster 9.0.0                                     |

---

## Project layout

```
design/               UI prototypes (source of truth for the React frontend)
  landing.html        Marketing landing page — open in browser to preview
  index.html          Holder app + verifier interactive demo
  tshepo-*.jsx        Shared icon/UI/card/holder/verifier/app/page components

src/main/resources/
  swagger/api.yml     OpenAPI 3.1 contract — authored before implementation (api-first)
  config/liquibase/   Database migrations

src/main/java/app/tshepo/
  web/rest/           Controllers (implement generated OpenAPI interfaces)
  service/            Core business logic
  domain/             JPA entities
  repository/         Spring Data repositories

src/main/webapp/app/  React 19 SPA (matches design/ artefacts)
tshepo-app.jdl        JDL used to generate the scaffold
```

---

## Setup

### Prerequisites

| Tool     | Version                             |
| -------- | ----------------------------------- |
| Java     | 21                                  |
| Node     | 22+                                 |
| npm      | bundled with Node                   |
| JHipster | 9.0.0                               |
| Docker   | any recent version (for PostgreSQL) |

### Install dependencies

```bash
npm install
```

### Run in development (H2 in-memory, fixture Investec data)

Open two terminals:

```bash
# Terminal 1 — Spring Boot
./mvnw

# Terminal 2 — React dev server (hot reload)
npm start
```

App is available at <http://localhost:8080>.

The `fixtureinvestec` Spring profile is active by default in dev — no real Investec credentials needed.

### Run with a real Investec sandbox

```bash
# Set credentials in environment (never in code/git)
export INVESTEC_CLIENT_ID=...
export INVESTEC_CLIENT_SECRET=...
export INVESTEC_API_BASE=https://openapi.investec.com

./mvnw -Dspring.profiles.active=dev,investec-live
```

### Production build

```bash
./mvnw -Pprod clean verify
java -jar target/*.jar
```

### Docker (PostgreSQL)

```bash
docker compose -f src/main/docker/postgresql.yml up -d
./mvnw -Pprod
```

---

## API contract

The full OpenAPI 3.1 spec is at `src/main/resources/swagger/api.yml`.

Key custom endpoints:

| Method | Path                            | Auth                  | Description                                |
| ------ | ------------------------------- | --------------------- | ------------------------------------------ |
| `POST` | `/api/bank/connect`             | JWT                   | Exchange Investec OAuth code               |
| `GET`  | `/api/bank/status`              | JWT                   | Connection status                          |
| `GET`  | `/api/bank/claims`              | JWT                   | Available claims with met/not-met          |
| `POST` | `/api/credentials/issue`        | JWT                   | Issue a new SD-JWT VC                      |
| `POST` | `/api/credentials/{id}/present` | JWT                   | Generate selective-disclosure presentation |
| `POST` | `/api/verify`                   | public (rate-limited) | Verify a presentation                      |
| `POST` | `/v1/verify`                    | API key (metered)     | Verify at scale                            |
| `GET`  | `/.well-known/did.json`         | public                | DID document for did:web                   |
| `GET`  | `/.well-known/jwks.json`        | public                | JWKS for signature verification            |

---

## Testing

```bash
# Backend unit + integration tests
./mvnw verify

# Frontend unit tests
npm test

# E2E (Cypress) — requires running app
npm run app:start        # terminal 1
npm run e2e              # terminal 2
```

---

## Privacy by design — rules enforced in code

- Raw transactions are **never persisted** or logged — only boolean predicate results.
- Credentials certify thresholds ("≥ R30,000"), never exact figures.
- Presentations hide non-selected claims cryptographically; verifiers cannot infer hidden claim count.
- Bank connection stores only metadata (account mask, connection timestamp) — no tokens at rest.

---

## Phases

| #   | Phase                             | Status  |
| --- | --------------------------------- | ------- |
| 0   | Prerequisites & JHipster scaffold | ✅ Done |
| 1   | Domain model JDL                  | ✅ Done |
| 2   | OpenAPI contract review           | ✅ Done |
| 3   | Investec adapter (fixture + live) | ✅ Done |
| 4   | Core services                     | ✅ Done |
| 5   | React frontend                    | ✅ Done |
| 6   | Submission polish                 | ✅ Done |

---

## License

MIT — see [LICENSE](LICENSE).
