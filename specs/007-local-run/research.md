# Research & Technical Decisions: Local Environment and Execution Documentation

**Feature Branch**: `007-local-run`  
**Date**: 2026-11-11  
**Spec**: [spec.md](spec.md)

This document records technical decisions, architectures, and trade-offs for the local Docker Compose environment.

---

## 1. Docker Compose Architecture and Service Selection

### Decision
Implement a multi-container Docker Compose file (`docker-compose.yml`) in the repository root. The configuration defines the following services:
- `app`: The `sdd-example` application service running on port `8080`.
- `mariadb`: MariaDB 11.4 relational database running on port `3306`.
- `rabbitmq`: RabbitMQ with management plugin running on ports `5672` (AMQP) and `15672` (Management UI).
- `mailpit`: Axllent Mailpit mock SMTP server on ports `1025` (SMTP internal, mapped to host `2525`) and `8025` (Web UI).
- `otel-collector`: OpenTelemetry Collector receiving OTLP HTTP metrics on port `4318` and exporting Prometheus metrics on port `8889`.
- `prometheus`: Prometheus server scraping the collector and serving metrics on port `9090`.
- `grafana`: Grafana server on port `3000` with automated datasource and dashboard provisioning.

### Rationale
- The application configuration in `application.yml` requires MariaDB, RabbitMQ, SMTP, and an OTLP metrics endpoint.
- Mailpit is lightweight and provides a web interface to inspect sent notification emails.
- Health checks ensure that dependent containers report healthy status before the application starts.
- Docker Compose v2 is the standard container orchestration tool for local development.

### Alternatives Considered
- *Single monolith container*: Violates container separation principles and makes debugging difficult.
- *Mailhog*: Unmaintained for years; Mailpit is active, faster, and provides a modern web interface.
- *Cloud dependencies*: Requires network access, accounts, and credentials, violating local developer autonomy.

---

## 2. Application Containerization

### Decision
Provide a multi-stage `Dockerfile` in the repository root based on Eclipse Temurin JRE 25.
The Dockerfile builds the application JAR or uses pre-built artifacts and sets default runtime configuration via environment variables.
The Docker Compose file supports both:
1. Full container execution: `docker compose up --build`.
2. Hybrid execution: developers start backing services with `docker compose up mariadb rabbitmq mailpit otel-collector prometheus grafana` and run `./gradlew run` on the host.

### Rationale
- Multi-stage Docker builds ensure reproducible container creation without local JDK setup requirements.
- The hybrid option allows rapid code iteration and debugging in IDEs without rebuilding containers for each code change.
- Eclipse Temurin JRE 25 aligns with `build.gradle.kts` toolchain configuration.

### Alternatives Considered
- *Host-only execution*: Requires manual installation of database, message broker, and mail server on developer machines.
- *Container-only execution without hybrid mode*: Slows down code-edit-test cycles during daily development.

---

## 3. Telemetry Pipeline and Dashboard Auto-Provisioning

### Decision
Configure the telemetry path as follows:
1. The application exports Micrometer metrics to OpenTelemetry Collector via OTLP HTTP at `http://otel-collector:4318/v1/metrics`.
2. The OpenTelemetry Collector transforms OTLP metrics and exposes a Prometheus scrape endpoint on port `8889`.
3. Prometheus scrapes the collector on port `8889` every 5 seconds.
4. Grafana provisions Prometheus as the default datasource automatically via a file in `grafana/provisioning/datasources/`.
5. Grafana loads a pre-configured dashboard JSON file automatically via a file in `grafana/provisioning/dashboards/`.
6. The dashboard displays HTTP request rates, response times, HTTP status code distributions, and JVM memory usage.

### Rationale
- Zero manual steps required from the developer. The dashboard is visible immediately upon starting Grafana.
- Adheres to AGENTS.md Rule 2: avoids changing `build.gradle.kts` by utilizing existing `micronaut-micrometer-registry-otlp` dependencies.
- OpenTelemetry Collector bridges OTLP metrics into Prometheus format cleanly.

### Alternatives Considered
- *Manual dashboard import*: Requires developers to copy JSON files and configure datasources manually via the UI.
- *Direct Prometheus scraping*: Would require adding Prometheus client dependencies to `build.gradle.kts`, violating build stability rules.

---

## 4. Documentation Structure and Navigation

### Decision
Create a dedicated documentation directory `docs/running/` containing:
- `README.md`: The complete operational guide for starting, verifying, and stopping the environment.
- `requests/products.http`: Executable HTTP requests for the product catalog.
- `requests/orders.http`: Executable HTTP requests for order management.
Update the repository root `README.md` to link directly to `docs/running/README.md`.

### Rationale
- Concentrates all local execution resources and sample requests in one discoverable location.
- Keeps the root `README.md` clean while providing a clear entry point for new contributors.
- Satisfies requirements FR-008, FR-009, FR-010, and FR-011.

### Alternatives Considered
- *Inline documentation in root README.md*: Bloats the main README file and scatters operational instructions.
- *Separate wiki*: Disconnects documentation from version-controlled repository changes.

---

## 5. HTTP Request Suite Design

### Decision
Write example requests using the standard RFC 7230 `.http` file format supported by IntelliJ IDEA, VS Code REST Client, and command-line tools.
Requests include:
- Base URL variables (`@host = http://localhost:8080`).
- Valid payload examples for every CRUD use case.
- Sequential scenarios (create entity, retrieve entity, update entity, list entities, delete entity).
- Error validation examples (missing fields, duplicate keys, non-existent identifiers).

### Rationale
- Developers can execute requests directly inside their IDE with a single click.
- Avoids external dependencies such as Postman collections that require proprietary account logins.
- Satisfies requirements FR-014, FR-015, and FR-016.

### Alternatives Considered
- *cURL shell scripts*: Harder to read, inspect, and maintain than declarative `.http` files.
- *Postman collections*: Requires binary application installations and external file formats.

