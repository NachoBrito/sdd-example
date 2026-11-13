#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# Default parameter values
SCENARIO="${SCENARIO:-mixed}"
VUS="${VUS:-10}"
DURATION="${DURATION:-30s}"
RAMP_UP="${RAMP_UP:-5s}"
TARGET_URL="${TARGET_URL:-http://localhost:8080}"

print_help() {
  cat <<'EOF'
Usage: ./load-tests/run.sh [OPTIONS]

Runs load testing and performance validation scenarios against the SDD example service.

Options:
  -s, --scenario SCENARIO   Scenario to run: auth, products, orders, mixed, stability (default: mixed)
  -u, --users VUS           Number of concurrent virtual users (default: 10)
  -d, --duration DURATION   Active test duration, e.g. 30s, 1m, 5m (default: 30s)
  -r, --ramp-up DURATION    Ramp-up duration, e.g. 5s, 15s (default: 5s)
  -t, --target URL          Target application base URL (default: http://localhost:8080)
  -h, --help                Show this help message and exit

Environment Variables:
  SCENARIO                  Scenario name fallback
  VUS                       Virtual users fallback
  DURATION                  Duration fallback
  RAMP_UP                   Ramp-up duration fallback
  TARGET_URL                Target URL fallback

Exit Codes:
  0                         Success (all thresholds passed)
  1                         Configuration error
  2                         Connectivity error (target service unreachable)
  99                        Performance threshold violation
EOF
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
  case "$1" in
    -s|--scenario)
      if [[ -z "${2:-}" || "${2:-}" == -* ]]; then
        echo "Error: --scenario requires a value" >&2
        exit 1
      fi
      SCENARIO="$2"
      shift 2
      ;;
    -u|--users)
      if [[ -z "${2:-}" || "${2:-}" == -* ]]; then
        echo "Error: --users requires a value" >&2
        exit 1
      fi
      VUS="$2"
      shift 2
      ;;
    -d|--duration)
      if [[ -z "${2:-}" || "${2:-}" == -* ]]; then
        echo "Error: --duration requires a value" >&2
        exit 1
      fi
      DURATION="$2"
      shift 2
      ;;
    -r|--ramp-up)
      if [[ -z "${2:-}" || "${2:-}" == -* ]]; then
        echo "Error: --ramp-up requires a value" >&2
        exit 1
      fi
      RAMP_UP="$2"
      shift 2
      ;;
    -t|--target)
      if [[ -z "${2:-}" || "${2:-}" == -* ]]; then
        echo "Error: --target requires a value" >&2
        exit 1
      fi
      TARGET_URL="$2"
      shift 2
      ;;
    -h|--help)
      print_help
      exit 0
      ;;
    *)
      echo "Error: Unknown option: $1" >&2
      print_help >&2
      exit 1
      ;;
  esac
done

# Strip trailing slash from TARGET_URL if present
TARGET_URL="${TARGET_URL%/}"

# Validate scenario file existence
SCENARIO_FILE="${SCRIPT_DIR}/scenarios/${SCENARIO}.js"
if [[ ! -f "${SCENARIO_FILE}" ]]; then
  echo "Error: Scenario file not found: ${SCENARIO_FILE}" >&2
  echo "Available scenarios:" >&2
  for file in "${SCRIPT_DIR}/scenarios"/*.js; do
    if [[ -f "${file}" ]]; then
      basename "${file}" .js >&2
    fi
  done
  exit 1
fi

# Pre-run service connectivity and health check
echo "==> Checking target service health at ${TARGET_URL}/health..."
if ! curl -s -f -m 5 "${TARGET_URL}/health" >/dev/null 2>&1; then
  echo "Error: Target service at ${TARGET_URL} is unreachable or unhealthy." >&2
  echo "Make sure the service is running (e.g., via 'docker compose up -d')." >&2
  exit 2
fi
echo "==> Target service is healthy."

# Determine relative path of scenario file from repository root
REL_SCENARIO="load-tests/scenarios/${SCENARIO}.js"

echo "=========================================================="
echo " Starting Load Test: ${SCENARIO}"
echo " Target:     ${TARGET_URL}"
echo " VUs:        ${VUS}"
echo " Duration:   ${DURATION}"
echo " Ramp-up:    ${RAMP_UP}"
echo "=========================================================="

K6_EXIT_CODE=0

if command -v k6 >/dev/null 2>&1; then
  echo "==> Running k6 natively on host..."
  k6 run \
    --vus "${VUS}" \
    --duration "${DURATION}" \
    -e TARGET_URL="${TARGET_URL}" \
    -e VUS="${VUS}" \
    -e DURATION="${DURATION}" \
    -e RAMP_UP="${RAMP_UP}" \
    "${REPO_ROOT}/${REL_SCENARIO}" || K6_EXIT_CODE=$?
elif command -v docker >/dev/null 2>&1; then
  echo "==> Running k6 container (grafana/k6:latest)..."

  DOCKER_TARGET_URL="${TARGET_URL}"
  # On macOS/Windows Docker, map localhost to host.docker.internal
  if [[ "${OSTYPE}" == "darwin"* ]]; then
    DOCKER_TARGET_URL="${DOCKER_TARGET_URL/localhost/host.docker.internal}"
    DOCKER_TARGET_URL="${DOCKER_TARGET_URL/127.0.0.1/host.docker.internal}"
  fi

  docker run --rm -i \
    --network=host \
    -v "${REPO_ROOT}:/workspace" \
    -w /workspace \
    grafana/k6:latest run \
    --vus "${VUS}" \
    --duration "${DURATION}" \
    -e TARGET_URL="${DOCKER_TARGET_URL}" \
    -e VUS="${VUS}" \
    -e DURATION="${DURATION}" \
    -e RAMP_UP="${RAMP_UP}" \
    "${REL_SCENARIO}" || K6_EXIT_CODE=$?
else
  echo "Error: Neither 'k6' nor 'docker' was found on PATH." >&2
  exit 1
fi

if [[ ${K6_EXIT_CODE} -eq 0 ]]; then
  echo "==> Load test completed successfully. All thresholds passed."
elif [[ ${K6_EXIT_CODE} -eq 99 ]]; then
  echo "==> Load test completed with threshold failure (exit code 99)." >&2
else
  echo "==> Load test failed with exit code ${K6_EXIT_CODE}." >&2
fi

exit ${K6_EXIT_CODE}

