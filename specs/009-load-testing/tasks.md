# Tasks: Load Testing and Performance Validation

**Input**: Design documents from `specs/009-load-testing/`  
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/`, `quickstart.md`

## Format: `[ID] [P?] [Story?] Description with file path`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (`[US1]`, `[US2]`, `[US3]`, `[US4]`)
- Every task includes the exact file path and quoted parameters

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create directory structure and shared payload utilities for load testing

- [X] T001 Create directory structure for load testing: `load-tests/helpers/` and `load-tests/scenarios/`
- [X] T002 [P] Create shared payload generator in `load-tests/helpers/payloads.js` with random UUID generation, realistic product data (`sku`, `name`, `description`, `priceInCents` > 0, `currency: "EUR"`, `status: "ACTIVE"`), and realistic order data (`customerName`, `customerEmail`, addresses, and `orderLines`) matching `specs/009-load-testing/data-model.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core execution script and service pre-flight checks

**⚠️ CRITICAL**: Must complete before implementing any user story scenario

- [X] T003 Create executable runner script in `load-tests/run.sh` implementing CLI argument parsing (`-s/--scenario`, `-u/--users`, `-d/--duration`, `-r/--ramp-up`, `-t/--target`), environment variable fallbacks (`SCENARIO`, `VUS`, `DURATION`, `RAMP_UP`, `TARGET_URL`), pre-run service health check against `${TARGET_URL}/health`, execution dispatch via Docker (`grafana/k6:latest`) or host CLI, and exit code propagation (`0`, `1`, `2`, `99`) matching `specs/009-load-testing/contracts/cli-contract.md`
- [X] T004 Grant execute permissions to `load-tests/run.sh` and verify `--help` flag displays all options, environment variables, and exit codes

**Checkpoint**: Foundation ready - scenario implementation can now begin

---

## Phase 3: User Story 1 - Product Catalog Load Scenario (Priority: P1) 🎯 MVP

**Goal**: Validate performance of product catalog endpoints under concurrent traffic with configurable virtual users and duration.

**Independent Test**: Execute `./load-tests/run.sh --scenario products --users 10 --duration 10s` against running service and verify successful HTTP 200/201 responses, metrics summary, and exit code 0.

### Implementation for User Story 1

- [X] T005 [P] [US1] Create product catalog load scenario in `load-tests/scenarios/products.js` implementing list products (`GET /v1/products`), get product by ID (`GET /v1/products/{id}`), create product (`POST /v1/products`) with realistic payload from `load-tests/helpers/payloads.js`, and thresholds `http_req_failed: ['rate<0.01']` and `http_req_duration: ['p(95)<500', 'p(99)<1000']` matching `specs/009-load-testing/contracts/scenarios-contract.yaml`
- [X] T006 [US1] Execute product load scenario against Docker Compose environment using `./load-tests/run.sh --scenario products --users 5 --duration 10s` and verify metrics and threshold compliance

**Checkpoint**: User Story 1 is functional and testable independently (MVP complete)

---

## Phase 4: User Story 2 - Order Processing Load Scenario (Priority: P2)

**Goal**: Validate transactional order processing performance with realistic payloads referencing existing active products.

**Independent Test**: Execute `./load-tests/run.sh --scenario orders --users 5 --duration 10s` and verify orders are created with HTTP 201 status, retrieved with HTTP 200 status, and metrics summary is produced.

### Implementation for User Story 2

- [X] T007 [P] [US2] Create order processing load scenario in `load-tests/scenarios/orders.js` implementing catalog lookup or setup to obtain valid product IDs, order creation (`POST /v1/orders`) with line items referencing existing products, retrieve order (`GET /v1/orders/{id}`), list orders (`GET /v1/orders`), and thresholds `http_req_failed: ['rate<0.01']` and `http_req_duration: ['p(95)<500', 'p(99)<1000']` matching `specs/009-load-testing/contracts/scenarios-contract.yaml`
- [X] T008 [US2] Execute order load scenario against Docker Compose environment using `./load-tests/run.sh --scenario orders --users 5 --duration 10s` and verify order creation success and latency percentiles

**Checkpoint**: User Stories 1 and 2 are functional and verifiable independently

---

## Phase 5: User Story 3 - Mixed Realistic E-Commerce Scenario (Priority: P3)

**Goal**: Simulate realistic end-to-end shopping journeys combining catalog browsing (80%) and order placement (20%) under concurrent load.

**Independent Test**: Execute `./load-tests/run.sh --scenario mixed --users 10 --duration 15s` and verify concurrent read and write operations execute without database deadlocks or latency degradation.

### Implementation for User Story 3

- [X] T009 [P] [US3] Create mixed e-commerce scenario in `load-tests/scenarios/mixed.js` combining catalog browsing (60%), product details lookup (20%), product creation (10%), and order placement (10%) with weighted execution paths and thresholds matching `specs/009-load-testing/contracts/scenarios-contract.yaml`
- [X] T010 [US3] Execute mixed load scenario against Docker Compose environment using `./load-tests/run.sh --scenario mixed --users 10 --duration 15s` and verify throughput and error rate thresholds

**Checkpoint**: User Stories 1, 2, and 3 are functional and verifiable

---

## Phase 6: User Story 4 - Stability and Sustained Load Validation (Priority: P4)

**Goal**: Validate system endurance and stability over extended test duration to ensure zero memory exhaustion or connection pool leaks.

**Independent Test**: Execute `./load-tests/run.sh --scenario stability --users 10 --duration 30s` (or extended 5m) and verify steady response times, zero container failures, and clean health check after completion.

### Implementation for User Story 4

- [X] T011 [P] [US4] Create stability scenario in `load-tests/scenarios/stability.js` maintaining sustained concurrent traffic across catalog and order endpoints with configurable duration (default 5m) and thresholds matching `specs/009-load-testing/contracts/scenarios-contract.yaml`
- [X] T012 [US4] Execute stability scenario sanity run using `./load-tests/run.sh --scenario stability --users 10 --duration 20s` and verify service health remains UP after execution

**Checkpoint**: All user stories are complete and independently verifiable

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Operational documentation and running guide integration

- [X] T013 [P] Create operational load testing guide in `docs/running/load-testing.md` documenting prerequisites, scenario descriptions, configuration options, execution commands, and metric interpretation in ASD-STE100
- [X] T014 Update running guide in `docs/running/README.md` adding a section and link for load testing and performance validation
- [X] T015 Run validation commands from `specs/009-load-testing/quickstart.md` to confirm end-to-end usability and verify ASD-STE100 sentence constraints across all documentation

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - executes first.
- **Foundational (Phase 2)**: Depends on Setup (Phase 1) - blocks all user stories.
- **User Story 1 (Phase 3)**: Depends on Foundational completion.
- **User Story 2 (Phase 4)**: Depends on Foundational completion. Uses payloads helper from Setup.
- **User Story 3 (Phase 5)**: Depends on Foundational completion. Leverages patterns from US1 and US2.
- **User Story 4 (Phase 6)**: Depends on Foundational completion. Sustains traffic across all endpoints.
- **Polish (Phase 7)**: Depends on completion of user stories.

### User Story Dependencies

- **User Story 1 (P1)**: Independent of other stories.
- **User Story 2 (P2)**: Independent of other stories (acquires product IDs dynamically).
- **User Story 3 (P3)**: Combines workflows from US1 and US2.
- **User Story 4 (P4)**: Runs sustained traffic across combined endpoints.

---

## Parallel Opportunities

- **Phase 1**: Task T002 can run in parallel with directory creation T001.
- **Phase 3-6 (User Stories)**: Once Phase 2 (Foundational) is complete, scenarios T005 (`products.js`), T007 (`orders.js`), T009 (`mixed.js`), and T011 (`stability.js`) can be implemented in parallel by different developers.
- **Phase 7**: Task T013 (`docs/running/load-testing.md`) can be drafted in parallel with scenario implementation.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (`load-tests/helpers/payloads.js`)
2. Complete Phase 2: Foundational (`load-tests/run.sh`)
3. Complete Phase 3: User Story 1 (`load-tests/scenarios/products.js`)
4. **STOP and VALIDATE**: Run `./load-tests/run.sh --scenario products` against local Docker environment.
5. Verify MVP deliverables.

### Incremental Delivery

1. Complete Setup + Foundational → Runner script operational.
2. Deliver User Story 1 (P1) → Product catalog load testing functional (MVP!).
3. Deliver User Story 2 (P2) → Order transaction load testing functional.
4. Deliver User Story 3 (P3) → Realistic mixed traffic journey functional.
5. Deliver User Story 4 (P4) → Prolonged stability validation functional.
6. Deliver Polish (Phase 7) → Operational guides and documentation updated.

