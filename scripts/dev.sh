#!/usr/bin/env bash
# Start Tshepo in development mode.
#
# Usage:
#   ./scripts/dev.sh           # fixture mode (no real Investec credentials needed)
#   ./scripts/dev.sh --live    # Investec sandbox (requires env vars below)
#
# Required env vars for --live:
#   INVESTEC_CLIENT_ID
#   INVESTEC_CLIENT_SECRET
#
# The redirect URI you must register in the Investec Developer Portal:
#   http://localhost:8080/api/auth/investec/callback
#
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# ── Load .env if present (never committed — add credentials here) ─────────────
if [ -f "$ROOT/.env" ]; then
  set -a
  # shellcheck source=/dev/null
  source "$ROOT/.env"
  set +a
fi

# ── Parse flags ──────────────────────────────────────────────────────────────
LIVE=false
for arg in "$@"; do
  case $arg in
    --live) LIVE=true ;;
    *) echo "Unknown flag: $arg"; exit 1 ;;
  esac
done

if $LIVE; then
  if [ -z "${INVESTEC_CLIENT_ID:-}" ] || [ -z "${INVESTEC_CLIENT_SECRET:-}" ]; then
    echo ""
    echo "ERROR: --live requires INVESTEC_CLIENT_ID and INVESTEC_CLIENT_SECRET to be set."
    echo ""
    echo "  export INVESTEC_CLIENT_ID=your-client-id"
    echo "  export INVESTEC_CLIENT_SECRET=your-client-secret"
    echo "  ./scripts/dev.sh --live"
    echo ""
    exit 1
  fi
  SPRING_PROFILES="dev,investec-live"
  echo "Mode: Investec sandbox (client ID: ${INVESTEC_CLIENT_ID:0:8}…)"
else
  SPRING_PROFILES="dev"
  echo "Mode: fixture (no real Investec credentials needed)"
fi

# ── Clear stale processes ────────────────────────────────────────────────────
for port in 8080 9060; do
  pid=$(lsof -ti ":$port" 2>/dev/null || true)
  if [ -n "$pid" ]; then
    echo "Killing process on port $port (PID $pid)"
    kill -9 $pid 2>/dev/null || true
  fi
done

# ── Trap Ctrl-C → kill both children ────────────────────────────────────────
cleanup() {
  echo ""
  echo "Stopping..."
  kill "$SPRING_PID" "$WEBPACK_PID" 2>/dev/null || true
  wait "$SPRING_PID" "$WEBPACK_PID" 2>/dev/null || true
  echo "Done."
}
trap cleanup INT TERM

# ── Spring Boot ──────────────────────────────────────────────────────────────
echo "Starting Spring Boot (port 8080, profiles: $SPRING_PROFILES)..."
./mvnw spring-boot:run -DskipTests -Dspring-boot.run.profiles="$SPRING_PROFILES" -q 2>&1 | sed 's/^/[spring] /' &
SPRING_PID=$!

# ── Wait for backend to be healthy ──────────────────────────────────────────
echo "Waiting for Spring Boot to be ready..."
until curl -sf http://localhost:8080/management/health 2>/dev/null | grep -q '"status":"UP"'; do
  sleep 2
  if ! kill -0 "$SPRING_PID" 2>/dev/null; then
    echo "Spring Boot failed to start. Check output above."
    exit 1
  fi
done
echo "Spring Boot is up."

# ── Webpack dev server ───────────────────────────────────────────────────────
echo "Starting webpack dev server (port 9060)..."
npm start 2>&1 | sed 's/^/[webpack] /' &
WEBPACK_PID=$!

# ── Done ─────────────────────────────────────────────────────────────────────
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  App:      http://localhost:9060"
echo "  API:      http://localhost:8080"
echo "  Sandbox:  http://localhost:3000 (Investec Docker)"
echo "  Verify:   http://localhost:9060/verify"
if $LIVE; then
  echo "  Mode:     Investec sandbox"
  echo "  Redirect: http://localhost:8080/api/auth/investec/callback"
else
  echo "  Mode:     Fixture (auto-login as Puseletso Lesenyane)"
fi
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Press Ctrl-C to stop."

wait
