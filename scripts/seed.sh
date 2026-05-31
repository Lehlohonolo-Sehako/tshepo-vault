#!/usr/bin/env bash
# Seed the Tshepo dev database with a demo user and pre-issued credentials.
# Run AFTER ./scripts/dev.sh (fixture mode) has fully started.
#
# Usage: ./scripts/seed.sh

set -euo pipefail

API="http://localhost:8080"
CYAN='\033[0;36m'; GREEN='\033[0;32m'; RED='\033[0;31m'; NC='\033[0m'

log()  { printf "${CYAN}[seed]${NC} %s\n" "$*"; }
ok()   { printf "${GREEN}[seed]${NC} ✓ %s\n" "$*"; }
fail() { printf "${RED}[seed]${NC} ✗ %s\n" "$*" >&2; exit 1; }

# ── 1. Wait for backend ──────────────────────────────────────────────────────
log "Waiting for Spring Boot..."
until curl -sf "$API/management/health" 2>/dev/null | grep -q '"status":"UP"'; do
  sleep 2
done
ok "Backend is up."

# ── 2. Connect via fixture OAuth (creates Puseletso Lesenyane if not exists) ────────
log "Running fixture OAuth connect..."
LOCATION=$(curl -si "$API/api/auth/investec/authorize" 2>/dev/null \
  | grep -i "^location:" | tr -d '\r' | sed 's/[Ll]ocation: //')

JWT=$(echo "$LOCATION" | grep -oE 'token=[^& ]+' | sed 's/token=//')

[ -z "$JWT" ] && fail "Could not extract JWT. Is the app running in fixture mode?"
ok "Fixture user provisioned (Puseletso Lesenyane)."

AUTH="-H \"Authorization: Bearer $JWT\""

# ── 3. Issue proof-of-income credential ──────────────────────────────────────
log "Issuing 'Proof of Income' credential..."
CRED1=$(curl -sf -X POST "$API/api/credentials/issue" \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Proof of Income",
    "purpose": "Rental application — monthly inflow ≥ R30,000",
    "claims": [
      {"type": "INFLOW",  "operator": "GTE", "threshold": 30000, "currency": "ZAR", "periodMonths": 6},
      {"type": "BALANCE", "operator": "GTE", "threshold": 50000, "currency": "ZAR", "periodMonths": 1},
      {"type": "NO_OVERDRAFT", "operator": "EQ", "threshold": 1, "periodMonths": 6}
    ],
    "validityDays": 180
  }')
ID1=$(echo "$CRED1" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null || echo "?")
ok "Proof of Income issued (id=$ID1)."

# ── 4. Issue account-tenure credential ───────────────────────────────────────
log "Issuing 'Account Standing' credential..."
CRED2=$(curl -sf -X POST "$API/api/credentials/issue" \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Account Standing",
    "purpose": "Visa application — 12+ months tenure, no overdraft",
    "claims": [
      {"type": "TENURE",         "operator": "GTE", "threshold": 12, "periodMonths": 12},
      {"type": "SALARY_CONTINUITY", "operator": "GTE", "threshold": 3, "periodMonths": 6}
    ],
    "validityDays": 180
  }')
ID2=$(echo "$CRED2" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null || echo "?")
ok "Account Standing issued (id=$ID2)."

# ── 5. Generate a sample presentation ────────────────────────────────────────
log "Generating sample presentation (INFLOW only)..."
curl -sf -X POST "$API/api/credentials/$ID1/present" \
  -H "Authorization: Bearer $JWT" \
  -H "Content-Type: application/json" \
  -d '{"disclosedClaimTypes": ["INFLOW"]}' > /dev/null
ok "Presentation generated."

# ── Done ─────────────────────────────────────────────────────────────────────
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
ok "Seed complete — 2 credentials in the database."
ok "Open http://localhost:9060 and click"
ok "'Connect with Investec' to log in as Puseletso Lesenyane."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
