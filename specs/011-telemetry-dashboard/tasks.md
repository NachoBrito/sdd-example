# Tasks: Complete Telemetry Dashboard

**Input**: Design documents from `/specs/011-telemetry-dashboard/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/metrics-contract.md`, `quickstart.md`

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US4)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Verify baseline build and existing observability configuration before making changes.

- [X] T001 Run `./gradlew build` from repository root to confirm baseline compilation, tests, and static analysis pass

---

## Phase 2: User Story 1 - Rename and Retain Core Service Telemetry (Priority: P1) 🎯 MVP

**Goal**: Rename the Grafana dashboard to `"SDD Example - Telemetry"` and group existing HTTP/JVM panels under an `"HTTP & Runtime"` section.

**Independent Test**: Open Grafana, confirm `"SDD Example - Telemetry"` appears without `"SDD Example - HTTP Service Metrics"`, and verify existing HTTP and JVM panels render.

### Implementation for User Story 1

- [X] T002 [US1] Update dashboard `title` to `"SDD Example - Telemetry"`, preserve `uid: "sdd-http-service"`, and add an `"HTTP & Runtime"` row header above existing panels in `config/grafana/dashboards/http-service.json`

**Checkpoint**: User Story 1 is complete and independently verifiable in Grafana.

---

## Phase 3: User Story 2 - Monitor Database Pool and Query Performance (Priority: P1)

**Goal**: Expose database query duration via Micronaut AOP and display HikariCP pool and query performance panels on the telemetry dashboard.

**Independent Test**: Run `DbQueryMetricsInterceptorTest`, execute DB-backed API calls, and verify Database panels in `config/grafana/dashboards/http-service.json` show pool counts, acquisition time, and query duration.

### Implementation for User Story 2

- [X] T003 [P] [US2] Create `@DbQueryMetrics` Micronaut `@Around` AOP annotation in `src/main/java/es/nachobrito/sdd/example/adapter/out/persistence/DbQueryMetrics.java`
- [X] T004 [US2] Implement `DbQueryMetricsInterceptor` (`MethodInterceptor<Object, Object>`) recording `db_query_duration` timer with tags `adapter`, `method`, and `outcome` (`success` | `error`) via `MeterRegistry` in `src/main/java/es/nachobrito/sdd/example/adapter/out/persistence/DbQueryMetricsInterceptor.java`
- [X] T005 [US2] Annotate `OrderPersistenceAdapter.java`, `ProductPersistenceAdapter.java`, `UserPersistenceAdapter.java`, and `AuthTokenPersistenceAdapter.java` with `@DbQueryMetrics` in `src/main/java/es/nachobrito/sdd/example/adapter/out/persistence/`
- [X] T006 [P] [US2] Add unit tests verifying `db_query_duration` recording on success and exception paths in `src/test/java/es/nachobrito/sdd/example/adapter/out/persistence/DbQueryMetricsInterceptorTest.java`
- [X] T007 [US2] Add `"Database"` row and panels (Connection Pool Size: `hikaricp_connections_active`, `hikaricp_connections_idle`, `hikaricp_connections_max`, `hikaricp_connections_pending`; Connection Acquisition Time; Query Execution Duration) in `config/grafana/dashboards/http-service.json`

**Checkpoint**: User Story 2 is complete and independently testable.

---

## Phase 4: User Story 3 - Monitor Message Broker Queue and Throughput (Priority: P2)

**Goal**: Enable RabbitMQ Prometheus metrics scraping and display queue size, throughput, and consumer panels on the telemetry dashboard.

**Independent Test**: Start Docker Compose stack, confirm `rabbitmq:15692` target is `UP` in Prometheus, and verify RabbitMQ panels render queue depth and message rates.

### Implementation for User Story 3

- [X] T008 [P] [US3] Enable the `rabbitmq_prometheus` plugin and expose port `15692` on the `rabbitmq` service in `docker-compose.yml`
- [X] T009 [P] [US3] Add `rabbitmq` scrape target (`rabbitmq:15692`) in `config/prometheus/prometheus.yml`
- [X] T010 [US3] Add `"RabbitMQ"` row and panels (Queue Size: `rabbitmq_queue_messages`, `rabbitmq_queue_messages_ready`, `rabbitmq_queue_messages_unacked`; Message Throughput: published, delivered, ack rates; Active Consumers: `rabbitmq_queue_consumers`) in `config/grafana/dashboards/http-service.json`

**Checkpoint**: User Story 3 is complete and independently testable.

---

## Phase 5: User Story 4 - Monitor Email Delivery Outcomes (Priority: P2)

**Goal**: Instrument `SmtpClient` with Micronaut AOP metrics for sent/failed counts and duration, and display SMTP panels on the telemetry dashboard.

**Independent Test**: Run `SmtpMetricsInterceptorTest`, trigger outbound notifications, and verify SMTP panels display sent count, failed count, and send duration.

### Implementation for User Story 4

- [X] T011 [P] [US4] Create `@SmtpMetrics` Micronaut `@Around` AOP annotation in `src/main/java/es/nachobrito/sdd/example/adapter/out/smtp/SmtpMetrics.java`
- [X] T012 [US4] Implement `SmtpMetricsInterceptor` (`MethodInterceptor<Object, Object>`) recording `smtp_messages_total` counter and `smtp_send_duration` timer with tag `outcome` (`sent` | `failed`) via `MeterRegistry` in `src/main/java/es/nachobrito/sdd/example/adapter/out/smtp/SmtpMetricsInterceptor.java`
- [X] T013 [US4] Annotate `SmtpClient.sendEmail(...)` with `@SmtpMetrics` in `src/main/java/es/nachobrito/sdd/example/adapter/out/smtp/SmtpClient.java`
- [X] T014 [P] [US4] Add unit tests verifying `smtp_messages_total` and `smtp_send_duration` recording on sent and failed outcomes in `src/test/java/es/nachobrito/sdd/example/adapter/out/smtp/SmtpMetricsInterceptorTest.java`
- [X] T015 [US4] Add `"SMTP"` row and panels (Email Delivery Rate: sent vs. failed from `smtp_messages_total`; Total Emails Sent / Failed stats; Email Send Duration from `smtp_send_duration_*`) in `config/grafana/dashboards/http-service.json`

**Checkpoint**: User Story 4 is complete and independently testable.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final validation across dashboard JSON, compose/Prometheus configs, and full Gradle build.

- [X] T016 [P] Validate JSON syntax of `config/grafana/dashboards/http-service.json` and YAML syntax of `docker-compose.yml` and `config/prometheus/prometheus.yml`
- [X] T017 Run `./gradlew build` and confirm all tests, ArchUnit, Checkstyle, PMD, SpotBugs, and Pitest checks pass

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — run first to establish clean baseline.
- **User Story 1 (Phase 2)**: Depends on Phase 1.
- **User Story 2 (Phase 3)**: Depends on Phase 1; dashboard edits (`T007`) follow `T002` to avoid JSON merge conflicts.
- **User Story 3 (Phase 4)**: `T008` and `T009` can run in parallel with US2; dashboard edits (`T010`) follow `T007`.
- **User Story 4 (Phase 5)**: `T011`–`T014` can run in parallel with US2/US3; dashboard edits (`T015`) follow `T010`.
- **Polish (Phase 6)**: Depends on all user stories completing.

### Parallel Opportunities

- Java AOP annotations and tests (`T003`, `T006`, `T011`, `T014`) and infra configs (`T008`, `T009`) touch distinct files and can be implemented in parallel.
- Edits to `config/grafana/dashboards/http-service.json` (`T002`, `T007`, `T010`, `T015`) should run sequentially or be combined cleanly.

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 (`T001`) and Phase 2 (`T002`) to rename the dashboard and preserve existing HTTP/JVM telemetry.
2. **Incremental Delivery**: Add Database (`US2`), RabbitMQ (`US3`), and SMTP (`US4`) telemetry sections one by one, validating with `./gradlew build` (`T017`).

