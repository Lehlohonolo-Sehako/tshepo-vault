#!/usr/bin/env bash
# Seed script — registers a demo user and issues a proof-of-funds credential.
# Requires the app to be running on localhost:8080 (dev profile with fixture data).
set -euo pipefail

BASE="http://localhost:8080"

echo "==> Registering demo user"
curl -sf -X POST "$BASE/api/register" \
  -H "Content-Type: application/json" \
  -d '{
    "login": "demo",
    "email": "demo@tshepo.local",
    "password": "Demo1234!",
    "langKey": "en"
  }' || echo "(already registered)"

echo ""
echo "==> Authenticating"
TOKEN=$(curl -sf -X POST "$BASE/api/authenticate" \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"Demo1234!","rememberMe":false}' \
  | grep -o '"id_token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "ERROR: Authentication failed." >&2
  exit 1
fi
echo "Got JWT token."

echo ""
echo "==> Connecting fixture Investec account"
curl -sf -X POST "$BASE/api/bank/connect" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"authorizationCode":"fixture-code","redirectUri":"http://localhost:8080/callback"}' \
  | jq .

echo ""
echo "==> Fetching available claims"
curl -sf "$BASE/api/bank/claims" \
  -H "Authorization: Bearer $TOKEN" \
  | jq .

echo ""
echo "==> Issuing proof-of-income credential"
CRED=$(curl -sf -X POST "$BASE/api/credentials/issue" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Proof of income — demo",
    "purpose": "Seeded for demonstration",
    "claims": [
      {"type":"INFLOW","operator":"GTE","threshold":30000,"currency":"ZAR","periodMonths":6},
      {"type":"TENURE","operator":"GTE","threshold":6,"periodMonths":12}
    ]
  }')
echo "$CRED" | jq .

CRED_ID=$(echo "$CRED" | jq -r '.id')
echo ""
echo "==> Generating presentation (disclosing INFLOW only)"
curl -sf -X POST "$BASE/api/credentials/$CRED_ID/present" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"disclosedClaimTypes":["INFLOW"]}' \
  | jq .

echo ""
echo "Done. Demo credential id: $CRED_ID"
