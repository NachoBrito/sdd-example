# Research & Technical Decisions: Load Testing and Performance Validation

**Feature Branch**: `009-load-testing`  
**Date**: 2026-11-12  
**Spec**: [spec.md](spec.md)

This document records the technical evaluations, architectural decisions, and trade-offs for the load testing and performance validation suite.

---

## 1. Tooling Selection: k6 vs. Alternatives

### Decision
Select **Grafana k6** as the load testing engine.

### Rationale
- **Performance and Resource Footprint**: k6 is written in Go and compiles to a native binary. It handles high concurrency (hundreds to thousands of virtual users) with minimal CPU and memory overhead compared to JVM-based tools.
- **Zero Host Installation Requirement**: The official Docker image `grafana/k6:latest` allows developers and CI runners to execute load tests without installing local runtimes or compilers.
- **Developer-Friendly Scripting**: Scenarios are written in standard modern JavaScript (ES6). Developers can write expressive logic, custom payload generation, and assertions without learning domain-specific languages.
- **Built-in Quality Gates (Thresholds)**: k6 natively supports declarative performance thresholds (e.g., `http_req_duration: ['p(95)<500']`, `http_req_failed: ['rate<0.01']`). If thresholds fail, k6 exits automatically with code 99. This satisfies requirement FR-011 without custom parsing scripts.
- **Zero Impact on Application Build**: JVM load testing tools (like Gatling) typically require Gradle build plugins and dependencies. Modifying `build.gradle.kts` would violate AGENTS.md Rule 2. k6 operates strictly at the HTTP boundary and requires no changes to the build configuration.
- **Telemetry Integration**: k6 metrics can export directly to Prometheus or OpenTelemetry Collector, which already exist in `docker-compose.yml`.

### Alternatives Considered
- *Gatling*: Requires Scala/Java knowledge and build configuration changes in `build.gradle.kts` (prohibited by AGENTS.md Rule 2). Slower startup and higher JVM memory consumption.
- *Locust*: Python-based; requires Python runtime and `pip` dependency management on the developer machine. Lower requests-per-second performance per CPU core.
- *Apache JMeter*: XML test plans are difficult to maintain in version control and lack modern developer ergonomics. Heavy memory footprint.
- *wrk / hey / autocannon*: Excellent for raw single-endpoint throughput, but lack multi-step stateful workflows (e.g., creating a product, capturing its generated identifier, and using it in an order).

---

## 2. Execution Architecture: Docker and Host Execution

### Decision
Provide an executable shell script `load-tests/run.sh` that supports two execution modes:
1. **Containerized Mode (Default)**: Executes `grafana/k6:latest` via Docker. Uses the host network (`--network host` on Linux, or `host.docker.internal` on macOS/Windows) to communicate with the local Docker Compose environment.
2. **Native CLI Mode**: Automatically detects if `k6` is installed on the host `PATH`. If present and requested, runs directly on the host for maximum execution speed.

### Rationale
- Complies with Constitution Principle 12: "Commands Are Discoverable; Local Dev Matches CI". A single script `./load-tests/run.sh` runs identically on a local developer machine and in automated test environments.
- Ensures zero prerequisite friction: any developer with Docker installed can run load tests immediately without installing Go, Python, or k6 binaries.
- Supports flexible host overrides for developers running the service in hybrid mode (`./gradlew run` on host) or full container mode (`docker compose up`).

### Alternatives Considered
- *Docker Compose Service Definition only*: Running load tests as a permanent service in `docker-compose.yml` is inconvenient for ad-hoc, repeatable load runs with varying CLI arguments.
- *Host binary only*: Forces every developer to install `k6` locally before running tests, increasing onboarding friction.

---

## 3. Workload Modeling and Scenario Architecture

### Decision
Structure load tests into modular JavaScript files under `load-tests/scenarios/` with a shared helper module `load-tests/helpers/payloads.js`:

1. **`products.js` (Catalog Scenario - P1)**:
   - 70% Product retrieval (`GET /v1/products?limit=10&offset=0`, `GET /v1/products/{id}`).
   - 30% Product creation (`POST /v1/products`) with unique UUIDs, realistic SKUs, names, descriptions, and Euro prices.

2. **`orders.js` (Order Scenario - P2)**:
   - Setup phase / Dynamic seeding: Queries existing products or creates a test product to acquire a valid product identifier.
   - Order placement (`POST /v1/orders`) referencing the valid product identifier with realistic customer details and postal addresses.
   - Order retrieval (`GET /v1/orders/{id}`) and listing (`GET /v1/orders`).

3. **`mixed.js` (Realistic E-Commerce Scenario - P3)**:
   - Simulates a full user funnel: 60% catalog browsing, 20% product detail lookups, 10% new product additions, 10% order checkouts.
   - Accurately tests database read/write concurrency and queue publishing pressure.

4. **`stability.js` (Sustained Load Validation - P4)**:
   - Executes steady-state traffic for an extended duration (default 5 minutes, configurable).
   - Validates memory stability, connection pool health, and absence of HTTP 5xx errors under continuous operations.

### Rationale
- Bugfix 008 established strict validation that orders must reference existing active products (`ProductNotFoundException` / HTTP 404 on invalid IDs). The order scenario must supply valid product IDs to simulate legitimate traffic and test happy-path transaction throughput.
- Realistic payloads (realistic names, addresses, and prices) ensure serialization, validation, and database storage execute realistic execution paths.

---

## 4. Configuration and Parameter Control

### Decision
Expose operational parameters through command-line options and environment variables with sensible defaults:

| Parameter | CLI Flag | Environment Variable | Default Value | Description |
|:---|:---|:---|:---|:---|
| **Scenario** | `-s`, `--scenario` | `SCENARIO` | `mixed` | Scenario to run (`products`, `orders`, `mixed`, `stability`) |
| **Virtual Users** | `-u`, `--users` | `VUS` | `10` | Number of concurrent simulated users |
| **Duration** | `-d`, `--duration` | `DURATION` | `30s` (`5m` for stability) | Test execution duration |
| **Ramp-Up** | `-r`, `--ramp-up` | `RAMP_UP` | `5s` | Duration to scale from 0 to target users |
| **Target URL** | `-t`, `--target` | `TARGET_URL` | `http://localhost:8080` | Base URL of the target application |

### Rationale
- Allows operators to run quick sanity checks (`./load-tests/run.sh -s products -u 5 -d 10s`) or thorough stress evaluations (`./load-tests/run.sh -s stability -u 50 -d 10m`) without modifying code.
- Compatible with CI execution where parameters are injected via environment variables.

---

## 5. Thresholds and Success Criteria Verification

### Decision
Define k6 thresholds across all scenarios:
```javascript
export const options = {
  thresholds: {
    http_req_failed: ['rate<0.01'], // Error rate must remain under 1%
    http_req_duration: ['p(95)<500', 'p(99)<1000'], // 95% under 500ms, 99% under 1s
  },
};
```

### Rationale
- Directly maps to Success Criteria SC-003 (p95 < 500ms) and SC-004 (error rate < 1%).
- Automatically signals build and test failure when the system degrades under load.

