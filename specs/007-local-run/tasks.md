# Tasks: Local Environment and Execution Documentation

**Input**: Design documents from `specs/007-local-run/`  
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/`, `quickstart.md`

## Format: `[ID] [P?] [Story?] Description with file path`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (`[US1]`, `[US2]`, `[US3]`)
- Every task includes the exact file path and quoted parameters

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create directory structure for configuration, documentation, and request suites

- [X] T001 Create configuration directories for telemetry and dashboards: `config/otel/`, `config/prometheus/`, `config/grafana/provisioning/datasources/`, `config/grafana/provisioning/dashboards/`, and `config/grafana/dashboards/`
- [X] T002 Create documentation and request directories: `docs/running/` and `docs/running/requests/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Configuration assets for telemetry collectors, metrics scraper, and dashboard provisioning

**⚠️ CRITICAL**: Must complete before starting User Story 1

- [X] T003 [P] Create OpenTelemetry Collector configuration in `config/otel/otel-collector-config.yaml` with OTLP HTTP receiver on port `4318`, batch processor, and Prometheus exporter on port `8889`
- [X] T004 [P] Create Prometheus scrape configuration in `config/prometheus/prometheus.yml` with scrape interval `5s` targeting `otel-collector:8889`
- [X] T005 [P] Create Grafana datasource provisioning file in `config/grafana/provisioning/datasources/datasource.yml` registering Prometheus at `http://prometheus:9090` as default datasource
- [X] T006 [P] Create Grafana dashboard provider configuration in `config/grafana/provisioning/dashboards/dashboards.yml` loading dashboard definitions from `/var/lib/grafana/dashboards`
- [X] T007 [P] Create HTTP metrics dashboard definition in `config/grafana/dashboards/http-service.json` with panels for request rate, response duration p95, HTTP status codes, and JVM heap usage

**Checkpoint**: Foundation ready - container definitions and documentation can now proceed

---

## Phase 3: User Story 1 - Local Environment Execution (Priority: P1) 🎯 MVP

**Goal**: Run the application and all supporting services locally with a single Docker Compose command.

**Independent Test**: Execute `docker compose up -d` and verify via `docker compose ps` that all containers start and report healthy states.

### Implementation for User Story 1

- [X] T008 [P] [US1] Create multi-stage `Dockerfile` in `Dockerfile` using Eclipse Temurin JRE 25 base image and configuring application port `8080`
- [X] T009 [US1] Create Docker Compose orchestration file in `docker-compose.yml` defining services `app`, `mariadb`, `rabbitmq`, `mailpit`, `otel-collector`, `prometheus`, and `grafana` with network `sdd-network`, persistent volumes, and health checks matching `specs/007-local-run/contracts/docker-compose-spec.yaml`
- [X] T010 [US1] Validate Docker Compose configuration syntax and container dependencies with `docker compose config`

**Checkpoint**: User Story 1 is functional and testable independently (MVP complete)

---

## Phase 4: User Story 2 - Execution Documentation (Priority: P2)

**Goal**: Document procedures to start, verify, use, and stop the service in `docs/running/README.md`, linked from the root `README.md`.

**Independent Test**: A contributor navigates from `README.md` to `docs/running/README.md` and successfully starts, inspects, and stops the environment.

### Implementation for User Story 2

- [X] T011 [P] [US2] Create operational guide in `docs/running/README.md` documenting prerequisites, containerized execution, hybrid local execution with Gradle, health verification, ports table, web interfaces, and shutdown commands in ASD-STE100
- [X] T012 [US2] Update root `README.md` to add link to `docs/running/README.md` under the Quick Start and Documentation sections

**Checkpoint**: User Stories 1 and 2 are functional and verifiable

---

## Phase 5: User Story 3 - Example HTTP Request Suite (Priority: P3)

**Goal**: Provide executable `.http` request files covering all product and order API use cases.

**Independent Test**: Execute the requests in `docs/running/requests/products.http` and `docs/running/requests/orders.http` against `http://localhost:8080`, verifying expected status codes.

### Implementation for User Story 3

- [X] T013 [P] [US3] Create product request suite in `docs/running/requests/products.http` with requests for create product (`POST /v1/products`), get product (`GET /v1/products/{id}`), update product (`PUT /v1/products/{id}`), list products (`GET /v1/products`), delete product (`DELETE /v1/products/{id}`), and error cases matching `specs/007-local-run/contracts/http-requests-spec.md`
- [X] T014 [P] [US3] Create order request suite in `docs/running/requests/orders.http` with requests for create order (`POST /v1/orders`), get order (`GET /v1/orders/{id}`), update order (`PUT /v1/orders/{id}`), list orders (`GET /v1/orders`), delete order (`DELETE /v1/orders/{id}`), and error cases matching `specs/007-local-run/contracts/http-requests-spec.md`

**Checkpoint**: All user stories are complete and independently verifiable

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: End-to-end validation and documentation verification

- [X] T015 Verify documentation and request links against `specs/007-local-run/quickstart.md`
- [X] T016 Review all created documentation files for ASD-STE100 compliance and sentence length limits

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - executes first.
- **Foundational (Phase 2)**: Depends on Setup (Phase 1) - blocks all user stories.
- **User Story 1 (Phase 3)**: Depends on Foundational completion.
- **User Story 2 (Phase 4)**: Depends on User Story 1 configuration paths.
- **User Story 3 (Phase 5)**: Depends on Foundational completion; can proceed in parallel with US2.
- **Polish (Phase 6)**: Depends on completion of all user stories.

### User Story Dependencies

- **User Story 1 (P1)**: Independent after Foundational phase.
- **User Story 2 (P2)**: Documents the environment created in User Story 1.
- **User Story 3 (P3)**: Provides requests to test the environment; independent of documentation.

### Parallel Opportunities

- Tasks `T003`, `T004`, `T005`, `T006`, and `T007` in Phase 2 can execute in parallel.
- Tasks `T013` and `T014` in Phase 5 can execute in parallel.
- User Story 2 (`T011`) and User Story 3 (`T013`, `T014`) can execute in parallel once User Story 1 finishes.

---

## Parallel Example: Foundational Phase

```bash
# Launch telemetry configuration tasks in parallel:
Task: "Create OpenTelemetry Collector configuration in config/otel/otel-collector-config.yaml"
Task: "Create Prometheus scrape configuration in config/prometheus/prometheus.yml"
Task: "Create Grafana datasource provisioning file in config/grafana/provisioning/datasources/datasource.yml"
Task: "Create Grafana dashboard provider configuration in config/grafana/provisioning/dashboards/dashboards.yml"
Task: "Create HTTP metrics dashboard definition in config/grafana/dashboards/http-service.json"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Execute Phase 1: Setup (`T001`, `T002`).
2. Execute Phase 2: Foundational (`T003` - `T007`).
3. Execute Phase 3: User Story 1 (`T008` - `T010`).
4. **STOP and VALIDATE**: Verify container startup with `docker compose config`.
5. Deploy MVP environment.

### Incremental Delivery

1. Setup + Foundational → Telemetry and configuration foundation ready.
2. User Story 1 → Docker Compose and application container (MVP functional).
3. User Story 2 → Operational documentation in `docs/running` and root `README.md`.
4. User Story 3 → Executable `.http` request suites for products and orders.
5. Polish → End-to-end quickstart validation and ASD-STE100 review.
