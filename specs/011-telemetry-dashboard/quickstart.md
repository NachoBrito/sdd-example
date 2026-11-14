# Quickstart Validation: Complete Telemetry Dashboard

**Feature**: `011-telemetry-dashboard` | **Date**: 2026-11-14

## Prerequisites

- Docker and Docker Compose are available.
- Java 25 and Gradle wrapper (`./gradlew`) work in the workspace.

## Validation Steps

1. **Run the full build and test suite**:
   ```bash
   ./gradlew build
   ```
   Expected result: Compilation, unit tests, integration tests, ArchUnit, Checkstyle, PMD, SpotBugs, and Pitest all pass.

2. **Start the local stack**:
   ```bash
   docker compose up --build -d
   ```
   Expected result: `sdd-app`, `sdd-mariadb`, `sdd-rabbitmq`, `sdd-mailpit`, `sdd-otel-collector`, `sdd-prometheus`, and `sdd-grafana` start cleanly.

3. **Verify Prometheus targets**:
   - Open `http://localhost:9090/targets`.
   - Confirm both `otel-collector` (`otel-collector:8889`) and `rabbitmq` (`rabbitmq:15692`) targets are `UP`.

4. **Generate end-to-end traffic**:
   - Register/login a user and create an order via the REST API (`http://localhost:8080`).
   - This exercises HTTP endpoints, MariaDB persistence, RabbitMQ event publishing/consuming, and SMTP email delivery to Mailpit.

5. **Verify Grafana dashboard**:
   - Open `http://localhost:3000` (default credentials: `admin` / `admin`).
   - Confirm the dashboard appears as **"SDD Example - Telemetry"** and **"SDD Example - HTTP Service Metrics"** is absent.
   - Verify all four sections (**HTTP & Runtime**, **Database**, **RabbitMQ**, **SMTP**) display live data without panel query errors.

