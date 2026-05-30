#!/usr/bin/env bash
# Start Tshepo in development mode.
# Runs Spring Boot (:8080) and the webpack dev server (:9060) side by side.
# Press Ctrl-C once to stop both.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

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
echo "Starting Spring Boot (port 8080)..."
./mvnw spring-boot:run -DskipTests -q 2>&1 | sed 's/^/[spring] /' &
SPRING_PID=$!

# ── Wait for backend to be healthy ──────────────────────────────────────────
echo "Waiting for Spring Boot to be ready..."
until curl -sf http://localhost:8080/management/health 2>/dev/null | grep -q '"status":"UP"'; do
  sleep 2
  # Exit early if Spring Boot died
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
echo "  Verify:   http://localhost:9060/verify"
echo "  Health:   http://localhost:8080/management/health"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Press Ctrl-C to stop."

wait
