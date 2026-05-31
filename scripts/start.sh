#!/usr/bin/env bash
# ┌─────────────────────────────────────────────────────────────────┐
# │  Tshepo — developer console                                     │
# │  Interactive startup script for local development               │
# │                                                                 │
# │  Usage:  ./scripts/start.sh                                     │
# └─────────────────────────────────────────────────────────────────┘
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# ── Colours ─────────────────────────────────────────────────────────────────
CYAN=$'\033[0;36m'; GREEN=$'\033[0;32m'; YELLOW=$'\033[1;33m'
RED=$'\033[0;31m'; BOLD=$'\033[1m'; DIM=$'\033[2m'; NC=$'\033[0m'

ok()     { printf "  ${GREEN}✓${NC}  %s\n" "$*"; }
warn()   { printf "  ${YELLOW}⚠${NC}  %s\n" "$*"; }
fail()   { printf "  ${RED}✗${NC}  %s\n" "$*"; }
info()   { printf "  ${DIM}→${NC}  %s\n" "$*"; }
header() { printf "\n${CYAN}${BOLD}%s${NC}\n" "$*"; }
rule()   { printf "${DIM}%s${NC}\n" "────────────────────────────────────────────────────────────────"; }

# ── Banner ───────────────────────────────────────────────────────────────────
clear
printf "\n"
printf "${CYAN}${BOLD}"
printf "  ████████╗███████╗██╗  ██╗███████╗██████╗  ██████╗ \n"
printf "     ██╔══╝██╔════╝██║  ██║██╔════╝██╔══██╗██╔═══██╗\n"
printf "     ██║   ███████╗███████║█████╗  ██████╔╝██║   ██║\n"
printf "     ██║   ╚════██║██╔══██║██╔══╝  ██╔═══╝ ██║   ██║\n"
printf "     ██║   ███████║██║  ██║███████╗██║     ╚██████╔╝\n"
printf "     ╚═╝   ╚══════╝╚═╝  ╚═╝╚══════╝╚═╝      ╚═════╝ \n"
printf "${NC}\n"
printf "  ${DIM}Bank-attested proof-of-funds credentials${NC}\n"
printf "  ${DIM}Powered by Investec Programmable Banking${NC}\n"
printf "\n"
rule

# ── Prerequisite checks ──────────────────────────────────────────────────────
header "Checking prerequisites"
printf "\n"

PREREQS_OK=true

# Java
if command -v java &>/dev/null; then
  JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
  if [ "${JAVA_VER:-0}" -ge 21 ] 2>/dev/null; then
    ok "Java ${JAVA_VER}  ${DIM}(required: 21+)${NC}"
  else
    fail "Java ${JAVA_VER} found — need Java 21 or later"
    info "Download: https://adoptium.net"
    PREREQS_OK=false
  fi
else
  fail "Java not found"
  info "Download: https://adoptium.net"
  PREREQS_OK=false
fi

# Node
if command -v node &>/dev/null; then
  NODE_VER=$(node --version | tr -d 'v' | cut -d'.' -f1)
  if [ "${NODE_VER:-0}" -ge 18 ] 2>/dev/null; then
    ok "Node $(node --version)  ${DIM}(required: 18+)${NC}"
  else
    warn "Node $(node --version) — recommended 22+"
  fi
else
  fail "Node.js not found"
  info "Download: https://nodejs.org"
  PREREQS_OK=false
fi

# npm dependencies
if [ -d "$ROOT/node_modules" ]; then
  ok "npm dependencies installed"
else
  warn "node_modules not found — will run npm install"
fi

# Docker (optional — only needed for sandbox mode)
if command -v docker &>/dev/null && docker info &>/dev/null 2>&1; then
  ok "Docker  ${DIM}(needed for sandbox mode)${NC}"
  DOCKER_OK=true
else
  warn "Docker not running  ${DIM}(only needed for sandbox mode)${NC}"
  DOCKER_OK=false
fi

if [ "$PREREQS_OK" = false ]; then
  printf "\n"
  fail "Fix the issues above, then run this script again."
  printf "\n"
  exit 1
fi

# ── Install npm deps if missing ───────────────────────────────────────────────
if [ ! -d "$ROOT/node_modules" ]; then
  printf "\n"
  header "Installing npm dependencies"
  printf "\n"
  npm install --silent
  ok "Dependencies installed"
fi

# ── Environment mode ─────────────────────────────────────────────────────────
printf "\n"
rule
header "Select environment"
printf "\n"
printf "  ${BOLD}[1]${NC}  Fixture mode   ${GREEN}● Recommended for first run${NC}\n"
printf "       Uses built-in SA banking sample data.\n"
printf "       ${DIM}No credentials or Docker needed.${NC}\n"
printf "\n"
printf "  ${BOLD}[2]${NC}  Sandbox mode\n"
printf "       Connects to the Investec Docker simulator\n"
printf "       running on localhost:3000.\n"
printf "       ${DIM}Requires Docker + sandbox container.${NC}\n"
printf "\n"
printf "  ${BOLD}[3]${NC}  Live account\n"
printf "       Connects to your real Investec account\n"
printf "       via openapi.investec.com.\n"
printf "       ${DIM}Requires credentials in .env.${NC}\n"
printf "\n"

# If .env has live credentials as default, pre-select accordingly
ENV_MODE=1
SPRING_PROFILES="dev"
MODE_LABEL="Fixture"

read -r -p "  Enter choice [1]: " CHOICE
CHOICE="${CHOICE:-1}"

case "$CHOICE" in
  2)
    if [ "$DOCKER_OK" = false ]; then
      printf "\n"
      warn "Docker is not running. Start Docker Desktop first."
      printf "\n"
      printf "  ${DIM}To start the Investec sandbox container:${NC}\n"
      printf "  ${CYAN}  docker run -p 3000:3000 ghcr.io/devinpearson/programmable-banking-sim:latest${NC}\n"
      printf "\n"
      read -r -p "  Docker is now running? Press Enter to continue or Ctrl+C to exit: "
    fi

    # Check if sandbox container is up
    if curl -sf http://localhost:3000 &>/dev/null; then
      ok "Investec sandbox container is running on :3000"
    else
      printf "\n"
      warn "Cannot reach localhost:3000"
      printf "\n"
      printf "  Start the sandbox container first:\n"
      printf "\n"
      printf "  ${CYAN}  docker run -p 3000:3000 ghcr.io/devinpearson/programmable-banking-sim:latest${NC}\n"
      printf "\n"
      read -r -p "  Container is now running? Press Enter to continue or Ctrl+C to exit: "
    fi

    SPRING_PROFILES="dev,investec-live"
    MODE_LABEL="Sandbox (localhost:3000)"
    if [ -f "$ROOT/.env" ]; then
      set -a; source "$ROOT/.env"; set +a
    fi
    ;;

  3)
    if [ -f "$ROOT/.env" ]; then
      set -a; source "$ROOT/.env"; set +a
    fi
    if [ -z "${INVESTEC_CLIENT_ID:-}" ] || [ -z "${INVESTEC_CLIENT_SECRET:-}" ]; then
      printf "\n"
      fail "Live mode requires credentials in .env"
      printf "\n"
      printf "  Create a ${BOLD}.env${NC} file in the project root with:\n"
      printf "\n"
      printf "  ${CYAN}  INVESTEC_CLIENT_ID='your-client-id'\n"
      printf "  ${CYAN}  INVESTEC_CLIENT_SECRET='your-client-secret'\n"
      printf "  ${CYAN}  INVESTEC_API_KEY='your-api-key'\n"
      printf "  ${CYAN}  INVESTEC_API_BASE='https://openapi.investec.com'\n"
      printf "  ${CYAN}  INVESTEC_TOKEN_URI='https://openapi.investec.com/identity/v2/oauth2/token'${NC}\n"
      printf "\n"
      printf "  ${DIM}Get credentials at: developer.investec.com${NC}\n"
      printf "\n"
      exit 1
    fi
    SPRING_PROFILES="dev,investec-live"
    MODE_LABEL="Live (${INVESTEC_CLIENT_ID:0:8}…)"
    ;;

  *)
    SPRING_PROFILES="dev"
    MODE_LABEL="Fixture"
    ;;
esac

# ── Log file setup ───────────────────────────────────────────────────────────
mkdir -p "$ROOT/logs"
SPRING_LOG="$ROOT/logs/spring.log"
WEBPACK_LOG="$ROOT/logs/webpack.log"
: > "$SPRING_LOG"
: > "$WEBPACK_LOG"

# ── Kill stale processes ─────────────────────────────────────────────────────
for port in 8080 9060; do
  pid=$(lsof -ti ":$port" 2>/dev/null || true)
  if [ -n "$pid" ]; then
    kill -9 $pid 2>/dev/null || true
  fi
done

# ── Cleanup on exit ──────────────────────────────────────────────────────────
cleanup() {
  printf "\n\n"
  rule
  printf "\n"
  info "Stopping services…"
  kill "$SPRING_PID" "$WEBPACK_PID" 2>/dev/null || true
  wait "$SPRING_PID" "$WEBPACK_PID" 2>/dev/null || true
  printf "\n"
  ok "All services stopped. Goodbye."
  printf "\n"
  rule
  printf "\n"
}
trap cleanup INT TERM

# ── Start Spring Boot ────────────────────────────────────────────────────────
printf "\n"
rule
header "Starting services"
printf "\n"

info "Spring Boot starting on :8080  ${DIM}(logging to logs/spring.log)${NC}"

SPRING_BOOT_OPTS="-DskipTests -Dspring-boot.run.profiles=$SPRING_PROFILES -q"
./mvnw spring-boot:run $SPRING_BOOT_OPTS > "$SPRING_LOG" 2>&1 &
SPRING_PID=$!

# Animate while waiting
printf "  ${DIM}Waiting for Spring Boot${NC}"
BOOT_SECONDS=0
while ! curl -sf http://localhost:8080/management/health 2>/dev/null | grep -q '"status":"UP"'; do
  printf "."
  sleep 2
  BOOT_SECONDS=$((BOOT_SECONDS + 2))
  if ! kill -0 "$SPRING_PID" 2>/dev/null; then
    printf "\n\n"
    fail "Spring Boot failed to start"
    printf "\n"
    info "Last 20 lines from logs/spring.log:"
    printf "\n"
    tail -20 "$SPRING_LOG" | sed 's/^/    /'
    printf "\n"
    exit 1
  fi
  if [ "$BOOT_SECONDS" -gt 120 ]; then
    printf "\n\n"
    fail "Spring Boot timed out after 120 seconds"
    info "Check logs/spring.log for details"
    exit 1
  fi
done
printf "\n"
ok "Spring Boot is ready  ${DIM}(${BOOT_SECONDS}s)${NC}"

# ── Start webpack ────────────────────────────────────────────────────────────
printf "\n"
info "Webpack dev server starting on :9060  ${DIM}(logging to logs/webpack.log)${NC}"

npm start > "$WEBPACK_LOG" 2>&1 &
WEBPACK_PID=$!

printf "  ${DIM}Waiting for webpack${NC}"
WEBPACK_SECONDS=0
while ! curl -sf http://localhost:9060 &>/dev/null; do
  printf "."
  sleep 2
  WEBPACK_SECONDS=$((WEBPACK_SECONDS + 2))
  if ! kill -0 "$WEBPACK_PID" 2>/dev/null; then
    printf "\n\n"
    fail "Webpack failed to start"
    printf "\n"
    info "Last 20 lines from logs/webpack.log:"
    printf "\n"
    tail -20 "$WEBPACK_LOG" | sed 's/^/    /'
    printf "\n"
    exit 1
  fi
  if [ "$WEBPACK_SECONDS" -gt 60 ]; then
    printf "\n\n"
    warn "Webpack is taking longer than expected — continuing anyway"
    break
  fi
done
printf "\n"
ok "Webpack dev server is ready  ${DIM}(${WEBPACK_SECONDS}s)${NC}"

# ── Ready screen ─────────────────────────────────────────────────────────────
printf "\n"
rule
printf "\n"
printf "  ${GREEN}${BOLD}Tshepo is running!${NC}\n"
printf "\n"
printf "  ${BOLD}Open in browser:${NC}\n"
printf "  ${CYAN}  http://localhost:9060${NC}\n"
printf "\n"
printf "  ${BOLD}Mode:${NC}  ${MODE_LABEL}\n"
printf "\n"
rule
printf "\n"
printf "  ${BOLD}What you can do:${NC}\n"
printf "\n"
printf "  ${GREEN}●${NC}  ${BOLD}Connect${NC}   Link your Investec account at ${CYAN}/connect${NC}\n"
printf "  ${GREEN}●${NC}  ${BOLD}Issue${NC}     Create a selectively-disclosable SD-JWT credential\n"
printf "  ${GREEN}●${NC}  ${BOLD}Present${NC}   Choose which claims to reveal — others are cryptographically hidden\n"
printf "  ${GREEN}●${NC}  ${BOLD}Verify${NC}    Anyone can verify a presentation at ${CYAN}/verify${NC}  (no login needed)\n"
printf "\n"
rule
printf "\n"
printf "  ${BOLD}Useful endpoints:${NC}\n"
printf "\n"
printf "  ${DIM}App          ${NC}  http://localhost:9060\n"
printf "  ${DIM}API health   ${NC}  http://localhost:8080/management/health\n"
printf "  ${DIM}API docs     ${NC}  http://localhost:8080/swagger-ui/index.html\n"
printf "  ${DIM}JWKS         ${NC}  http://localhost:8080/.well-known/jwks.json\n"
if [ "$CHOICE" = "2" ]; then
printf "  ${DIM}Sandbox UI   ${NC}  http://localhost:3000\n"
fi
printf "\n"
rule
printf "\n"
printf "  ${BOLD}Logs:${NC}\n"
printf "\n"
printf "  ${DIM}Spring Boot  ${NC}  tail -f logs/spring.log\n"
printf "  ${DIM}Webpack      ${NC}  tail -f logs/webpack.log\n"
printf "\n"
rule
printf "\n"
printf "  ${DIM}Press Ctrl+C to stop all services${NC}\n"
printf "\n"

# ── Tail spring log for errors only ─────────────────────────────────────────
# Show ERROR lines from Spring in real time so the developer notices problems
tail -f "$SPRING_LOG" 2>/dev/null | grep --line-buffered -i " ERROR \| WARN " | while IFS= read -r line; do
  printf "  ${YELLOW}[log]${NC} %s\n" "$line"
done &
TAIL_PID=$!

wait "$SPRING_PID" "$WEBPACK_PID" 2>/dev/null || true
kill "$TAIL_PID" 2>/dev/null || true
