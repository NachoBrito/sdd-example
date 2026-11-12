# Implementation Plan: Local Environment and Execution Documentation

**Branch**: `007-local-run` | **Date**: 2026-11-11 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/007-local-run/spec.md`

---

## Summary

Implement a local development environment using Docker Compose that runs the service alongside all required dependencies:
- MariaDB 11.4 database with automated Liquibase migrations on application startup.
- RabbitMQ message broker with AMQP messaging and management console.
- Mailpit mock SMTP server with web UI for inspecting notification emails.
- OpenTelemetry Collector and Prometheus for application metrics collection.
- Grafana server with automated datasource and dashboard provisioning for HTTP service metrics.
- Comprehensive operational documentation in `docs/running/README.md` linked from the root `README.md`.
- Executable HTTP request suites in `docs/running/requests/` covering all product catalog and order management use cases.

---

## Technical Context

**Language/Version**: Java 25 (runtime container: Eclipse Temurin JRE 25)

**Primary Dependencies**: Docker Compose v2, MariaDB 11.4, RabbitMQ 3.13-management, Mailpit, OpenTelemetry Collector Contrib 0.114.0, Prometheus, Grafana

**Storage**: MariaDB with persistent volume `mariadb-data`

**Messaging**: RabbitMQ with persistent volume `rabbitmq-data`

**Testing**: RFC 7230 executable `.http` test files, `docker compose ps` health verification

**Target Platform**: Local developer workstation (Linux, macOS, Windows with Docker)

**Project Type**: Infrastructure orchestration, technical documentation, and API test suites

**Performance Goals**: Environment starts and reaches healthy status in under 60 seconds; single-command stop completes cleanly

**Constraints**:
- Adhere to ASD-STE100 Simplified Technical English for all documentation.
- Zero unauthorized modifications to `build.gradle.kts`, Gradle version catalogs, or static analysis rules.
- Automatic dashboard provisioning in Grafana without requiring manual UI configuration.
- RFC 7230 compatibility for all sample request files.

**Scale/Scope**:
- 1 Docker Compose file (`docker-compose.yml`)
- 1 multi-stage application `Dockerfile`
- 1 OpenTelemetry Collector configuration (`config/otel/otel-collector-config.yaml`)
- 1 Prometheus configuration (`config/prometheus/prometheus.yml`)
- 1 Grafana datasource provisioning file (`config/grafana/provisioning/datasources/datasource.yml`)
- 1 Grafana dashboard provider file (`config/grafana/provisioning/dashboards/dashboards.yml`)
- 1 Grafana dashboard definition (`config/grafana/dashboards/http-service.json`)
- 1 operational guide (`docs/running/README.md`)
- 1 updated root `README.md` with link to running guide
- 2 executable HTTP request suites (`docs/running/requests/products.http`, `docs/running/requests/orders.http`)

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Evaluation | Justification |
|:---|:---|:---|
| **1. Minimum Viable Diff** | PASS | Implements strictly what is requested: compose file, documentation in `docs/running`, root README link, HTTP files, and Grafana dashboard. Modifies zero domain code. |
| **2. Ambiguity Halts Execution** | PASS | Telemetry scope clarified to include automated Grafana dashboard displaying HTTP service metrics. |
| **3. Do Not Distribute by Default** | PASS | Local container stack mirrors required runtime dependencies; single application binary preserved. |
| **4. Optimize for Deletion, Not Extension** | PASS | Declarative compose YAML and standard configuration files without proprietary wrapper scripts. |
| **5. Make Dependencies Explicit** | PASS | Explicit environment variable bindings in `docker-compose.yml` declare all service dependencies. |
| **6. Contract at the Boundary** | PASS | Versioned contracts defined in `contracts/docker-compose-spec.yaml`, `contracts/http-requests-spec.md`, and `contracts/grafana-dashboard-spec.json`. |
| **7. Test the Transformation** | PASS | Executable `.http` files verify all API boundary use cases against the running containers. |
| **8. Emit Structured Events** | PASS | Micrometer OTLP metrics stream into OpenTelemetry Collector and Prometheus. |
| **9. Recovery Over Prevention** | PASS | Health checks gate application startup; `docker compose down -v` provides clean reset capability. |
| **10. Attention Is Finite** | PASS | Grafana dashboard visualizes high-signal metrics: request rate, p95 latency, and error rate. |
| **11. Value Is Realized at the User** | PASS | Complete validation guide in `quickstart.md` verifies end-to-end usability. |
| **12. Commands Are Discoverable** | PASS | Standard commands documented in `README.md` and `docs/running/README.md`: `docker compose up -d`, `docker compose down`. |

---

## Project Structure

### Documentation (this feature)

```text
specs/007-local-run/
├── plan.md              # This plan
├── research.md          # Technical decisions and trade-offs
├── data-model.md        # Configuration models and topologies
├── quickstart.md        # Runnable verification guide
├── contracts/           # Boundary contracts
│   ├── docker-compose-spec.yaml
│   ├── http-requests-spec.md
│   └── grafana-dashboard-spec.json
└── checklists/
    └── requirements.md  # Requirements quality checklist
```

### Source Code and Configuration Layout

```text
.
├── docker-compose.yml                           # Multi-container orchestration definition
├── Dockerfile                                   # Application container definition
├── README.md                                    # Root README updated with running link
├── config/
│   ├── otel/
│   │   └── otel-collector-config.yaml           # OpenTelemetry Collector configuration
│   ├── prometheus/
│   │   └── prometheus.yml                       # Prometheus scrape target configuration
│   └── grafana/
│       ├── provisioning/
│       │   ├── datasources/
│       │   │   └── datasource.yml               # Automated Prometheus datasource registration
│       │   └── dashboards/
│       │       └── dashboards.yml               # Automated dashboard provider registration
│       └── dashboards/
│           └── http-service.json                # Pre-configured HTTP metrics dashboard
└── docs/
    └── running/
        ├── README.md                            # Operational guide for running and stopping
        └── requests/
            ├── products.http                    # Product use cases request suite
            └── orders.http                      # Order use cases request suite
```

**Structure Decision**: Place all container configuration files in `config/` grouped by tool, operational documentation and HTTP requests in `docs/running/`, and root files (`docker-compose.yml`, `Dockerfile`) at the top level for developer familiarity.

---

## Complexity Tracking

> No constitutional violations exist; this section is empty.
