# Implementation Plan: Complete Telemetry Dashboard

**Branch**: `011-telemetry-dashboard` | **Date**: 2026-11-14 | **Spec**: [spec.md](file:///home/nacho/github/sdd-example/specs/011-telemetry-dashboard/spec.md)

**Input**: Feature specification from `/specs/011-telemetry-dashboard/spec.md`

## Summary

Rename the Grafana dashboard `"SDD Example - HTTP Service Metrics"` to `"SDD Example - Telemetry"` and add diagnostic panels for Database, RabbitMQ, and SMTP telemetry. Use existing HikariCP Micrometer metrics for database pool gauges, Micronaut AOP `MethodInterceptor` instrumentation for database query duration and SMTP delivery metrics, and RabbitMQ's built-in `rabbitmq_prometheus` plugin scraped by Prometheus for broker queue and message metrics.

## Technical Context

**Language/Version**: Java 25 (Micronaut Platform 5.2.5)

**Primary Dependencies**: Micronaut Core, Micronaut Data JDBC, Micronaut JDBC Hikari, Micronaut RabbitMQ, Micronaut Micrometer Core & OTLP Registry

**Storage**: MariaDB 11.4 (via HikariCP connection pool)

**Testing**: JUnit 5, Micronaut Test, Mockito, ArchUnit, Checkstyle, PMD, SpotBugs, Pitest (`./gradlew build`)

**Target Platform**: Linux / Docker Compose (`sdd-app`, `mariadb`, `rabbitmq`, `mailpit`, `otel-collector`, `prometheus`, `grafana`)

**Project Type**: Hexagonal architecture web service with Docker Compose observability stack

**Performance Goals**: Metrics collection and OTLP/Prometheus scraping add negligible overhead (< 1 ms per instrumented call) and update dashboard panels within 30 seconds

**Constraints**: Do not modify `build.gradle.kts`, `gradle/libs.versions.toml`, or static analysis configs (`AGENTS.md` Rule 2); use Micronaut AOP for crosscutting metrics (`AGENTS.md` Rule 3); write documentation and comments in ASD-STE100 (`AGENTS.md` Rule 4)

**Scale/Scope**: 1 Grafana dashboard JSON (`config/grafana/dashboards/`), Prometheus & Docker Compose configs, and 2 Micronaut AOP metric interceptors in `adapter/out/`

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

1. **Principle 1 (Minimum Viable Diff)**: PASS. Changes are limited to renaming/extending the dashboard, enabling `rabbitmq_prometheus`, scraping it in Prometheus, and adding AOP metric interceptors for DB query duration and SMTP delivery.
2. **Principle 2 (Ambiguity Halts Execution)**: PASS. All requirements have unambiguous technical implementations using existing project infrastructure.
3. **Principle 3 (Do Not Distribute by Default)**: PASS. No new services or processes are introduced; only existing containers (`rabbitmq`, `prometheus`, `grafana`) are configured.
4. **Principle 4 (Optimize for Deletion, Not Extension)**: PASS. Small, self-contained AOP interceptors without speculative frameworks.
5. **Principle 5 (Make Dependencies Explicit)**: PASS. `MeterRegistry` is injected via constructor in the AOP interceptors.
6. **Principle 6 (Contract at the Boundary, Not in the Middle)**: PASS. Instrumentation sits strictly at outbound adapters (`adapter/out/persistence`, `adapter/out/smtp`).
7. **Principle 7 (Test the Transformation, Not the Plumbing)**: PASS. Unit/integration tests verify that DB query duration and SMTP sent/failed counters and timers record expected measurements.
8. **Principle 8 (Emit Structured Events, Derive Everything Else)**: PASS. Uses existing Micrometer/OTLP pipeline; no new telemetry SDKs added.
9. **Principle 9 (Recovery Over Prevention)**: PASS. Dashboard and config changes are immediately revertible.
10. **Principle 10 (Attention Is Finite)**: PASS. Dashboard panels are curated into four diagnostic sections (HTTP & Runtime, Database, RabbitMQ, SMTP) answering concrete incident questions.
11. **Principle 11 (Value Is Realized at the User, Not at Merge)**: PASS. Quickstart validates end-to-end flow from app/broker to Prometheus to Grafana.
12. **Principle 12 (Commands Are Discoverable; Local Dev Matches CI)**: PASS. Uses standard `./gradlew build` and `docker compose up`.

## Project Structure

### Documentation (this feature)

```text
specs/011-telemetry-dashboard/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/
│   └── metrics-contract.md
└── tasks.md             # Phase 2 output (/speckit-tasks command)
```

### Source Code (repository root)

```text
config/
├── grafana/dashboards/
│   └── http-service.json            # Update title to "SDD Example - Telemetry" & add DB/RabbitMQ/SMTP panels
└── prometheus/
    └── prometheus.yml               # Add scrape job for rabbitmq:15692

docker-compose.yml                   # Enable rabbitmq_prometheus plugin & expose/scrape port 15692

src/main/java/es/nachobrito/sdd/example/
└── adapter/out/
    ├── persistence/                 # Add Micronaut AOP @Around interceptor for db_query_duration
    └── smtp/                        # Add Micronaut AOP @Around interceptor for smtp_messages_total & smtp_send_duration

src/test/java/es/nachobrito/sdd/example/
└── adapter/out/
    ├── persistence/                 # Tests for DB query duration metrics interceptor
    └── smtp/                        # Tests for SMTP metrics interceptor
```

**Structure Decision**: Keep changes localized to `config/grafana/dashboards/`, `config/prometheus/prometheus.yml`, `docker-compose.yml`, and outbound adapter AOP interceptors under `src/main/java/.../adapter/out/`. Keep `domain/` and `application/` untouched.

## Complexity Tracking

No constitution violations.
