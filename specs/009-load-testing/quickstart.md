# Quickstart & Validation Guide: Load Testing

**Feature Branch**: `009-load-testing`  
**Date**: 2026-11-12  
**Spec**: [spec.md](spec.md)

This guide documents the procedures to run and validate the load testing suite against the local environment.

---

## 1. Prerequisites

Verify that the following tools are available on the host machine:
- Docker version 24.0 or newer.
- Bash shell (Linux, macOS, or WSL).
- Target application running and healthy via Docker Compose.

---

## 2. Start the Application Environment

Start the local service and dependencies before running load tests:

```bash
docker compose up -d --build
```

Wait until all containers report healthy status:

```bash
docker compose ps
```

Verify service connectivity:

```bash
curl -f http://localhost:8080/health || echo "Service not ready"
```

---

## 3. Run Load Test Scenarios

### Option A: Run Default Mixed Scenario

Execute the mixed e-commerce browsing and checkout scenario with default settings (10 users, 30 seconds):

```bash
./load-tests/run.sh
```

### Option B: Run Product Catalog Scenario

Run the product catalog scenario with 20 concurrent users for 45 seconds:

```bash
./load-tests/run.sh --scenario products --users 20 --duration 45s
```

### Option C: Run Order Processing Scenario

Run the order transaction scenario with 15 concurrent users for 30 seconds:

```bash
./load-tests/run.sh --scenario orders --users 15 --duration 30s
```

### Option D: Run Stability and Endurance Scenario

Run the sustained stability scenario for 5 minutes:

```bash
./load-tests/run.sh --scenario stability --users 20 --duration 5m
```

---

## 4. Expected Output and Results

A successful execution produces the standard k6 summary table:

```text
✓ status is 200/201

checks.........................: 100.00% ✓ 1520      ✗ 0
http_req_duration..............: avg=42.15ms min=12.10ms med=34.20ms max=189.40ms p(90)=68.10ms p(95)=85.30ms p(99)=142.10ms
http_req_failed................: 0.00%   ✓ 0         ✗ 1520
http_reqs......................: 1520    50.66/s
vus............................: 10      min=10      max=10
```

Validation rules:
- `http_req_failed` rate must be less than 1.00% (`rate<0.01`).
- `http_req_duration` p(95) must be less than 500ms.
- Exit code must be `0`.

---

## 5. Verify Metrics in Grafana Dashboard

1. Open `http://localhost:3000` in a web browser.
2. Sign in with username `admin` and password `admin`.
3. Open the **SDD Example - HTTP Service Metrics** dashboard.
4. Verify that HTTP request spikes, response duration percentiles, and 2xx status rates reflect the test execution.

---

## 6. Stop the Application Environment

When performance validation is complete, stop all containers:

```bash
docker compose down
```

