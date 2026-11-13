# CLI Contract: Load Test Execution Script

**Feature Branch**: `009-load-testing`  
**Date**: 2026-11-12  
**Contract Type**: Command-Line Interface

This document specifies the contract for the load test runner script `./load-tests/run.sh`.

---

## 1. Command Syntax

```bash
./load-tests/run.sh [OPTIONS]
```

---

## 2. Options and Flags

| Flag | Long Option | Value Type | Description | Default |
|:---|:---|:---|:---|:---|
| `-s` | `--scenario` | String | Scenario name (`products`, `orders`, `mixed`, `stability`) | `mixed` |
| `-u` | `--users` | Integer | Number of concurrent virtual users | `10` |
| `-d` | `--duration` | String | Active duration (e.g., `30s`, `1m`, `5m`) | `30s` |
| `-r` | `--ramp-up` | String | Ramp-up duration (e.g., `5s`, `15s`) | `5s` |
| `-t` | `--target` | String | Target service base URL | `http://localhost:8080` |
| `-h` | `--help` | None | Display usage instructions and exit | N/A |

---

## 3. Environment Variable Fallbacks

The script reads environment variables when flags are omitted:

- `SCENARIO` (maps to `--scenario`)
- `VUS` (maps to `--users`)
- `DURATION` (maps to `--duration`)
- `RAMP_UP` (maps to `--ramp-up`)
- `TARGET_URL` (maps to `--target`)

Explicit CLI flags override environment variables.

---

## 4. Exit Codes

| Exit Code | Meaning | Condition |
|:---|:---|:---|
| `0` | Success | All scenarios completed and all performance thresholds passed |
| `1` | Configuration Error | Invalid command-line argument, missing scenario file, or malformed parameter |
| `2` | Connectivity Error | Target service is unreachable or health check endpoint returns non-200 status |
| `99` | Threshold Failure | Load test executed successfully, but one or more k6 performance thresholds failed |

