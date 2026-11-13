# Load Testing and Performance Validation

This document describes the procedures to execute and configure performance load tests for the service.
The tooling evaluates system stability, throughput, and response latency under concurrent traffic.

---

## 1. Prerequisites

Verify that the host system satisfies the following requirements:

- Docker version 24.0 or newer.
- Bash shell environment.
- The service running in Docker Compose (`docker compose up -d`).

---

## 2. Load Testing Architecture

The load testing suite uses **Grafana k6** to simulate virtual users.
The runner script `./load-tests/run.sh` operates in two execution modes:

1. **Containerized Mode (Default)**:
   The script runs the official `grafana/k6:latest` container.
   No local installation of k6 is necessary on the host.

2. **Native CLI Mode**:
   The script detects if the `k6` binary is on the system PATH.
   If present, the script executes k6 directly on the host.

---

## 3. Available Scenarios

The suite includes four performance scenarios in `load-tests/scenarios/`:

| Scenario | File | Description | Traffic Mix |
|:---|:---|:---|:---|
| **`products`** | `products.js` | Product catalog throughput and latency | Browse (50%), lookup (20%), create (30%) |
| **`orders`** | `orders.js` | Transactional order processing | Order placement (50%), lookup (30%), list (20%) |
| **`mixed`** | `mixed.js` | Realistic end-to-end shopping journey | Browse (60%), detail (20%), add product (10%), order (10%) |
| **`stability`** | `stability.js` | Sustained traffic for endurance validation | Continuous mixed traffic with periodic health checks |

---

## 4. Run Scenarios

Execute scenarios with the runner script:

```bash
./load-tests/run.sh [OPTIONS]
```

### Option A: Run Default Mixed Scenario

Run the mixed workload scenario with 10 virtual users for 30 seconds:

```bash
./load-tests/run.sh
```

### Option B: Run Product Catalog Scenario

Run the product catalog scenario with 20 virtual users for 45 seconds:

```bash
./load-tests/run.sh --scenario products --users 20 --duration 45s
```

### Option C: Run Order Processing Scenario

Run the order transaction scenario with 15 virtual users for 30 seconds:

```bash
./load-tests/run.sh --scenario orders --users 15 --duration 30s
```

### Option D: Run Stability and Endurance Scenario

Run the sustained stability scenario for 5 minutes:

```bash
./load-tests/run.sh --scenario stability --users 20 --duration 5m
```

---

## 5. Configuration Options

The runner script accepts command-line arguments and environment variable fallbacks:

| Flag | Environment Variable | Default Value | Description |
|:---|:---|:---|:---|
| `-s`, `--scenario` | `SCENARIO` | `mixed` | Scenario name (`products`, `orders`, `mixed`, `stability`) |
| `-u`, `--users` | `VUS` | `10` | Number of concurrent virtual users |
| `-d`, `--duration` | `DURATION` | `30s` | Active test duration (e.g., `30s`, `1m`, `5m`) |
| `-r`, `--ramp-up` | `RAMP_UP` | `5s` | Ramp-up duration to target user count |
| `-t`, `--target` | `TARGET_URL` | `http://localhost:8080` | Target service base URL |

---

## 6. Performance Thresholds and Exit Codes

Each scenario enforces strict performance thresholds:

- **Error Rate**: Less than 1 percent failed HTTP requests (`http_req_failed < 0.01`).
- **95th Percentile Latency**: Less than 500 milliseconds (`http_req_duration p(95) < 500ms`).
- **99th Percentile Latency**: Less than 1000 milliseconds (`http_req_duration p(99) < 1000ms`).

The script returns the following exit codes:

| Exit Code | Meaning | Condition |
|:---|:---|:---|
| `0` | Success | All requests completed and all thresholds passed. |
| `1` | Configuration Error | An invalid argument or missing scenario file was provided. |
| `2` | Connectivity Error | The target service is unreachable or reports an unhealthy state. |
| `99` | Threshold Failure | The test executed, but a performance threshold was violated. |

---

## 7. Inspect Telemetry During Load Tests

1. Open `http://localhost:3000` in your web browser.
2. Sign in with username `admin` and password `admin`.
3. Open the **SDD Example - HTTP Service Metrics** dashboard.
4. Observe real-time request volume, latency percentiles, and JVM memory utilization during test execution.

