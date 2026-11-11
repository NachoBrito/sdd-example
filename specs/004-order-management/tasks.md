# Tasks: Order Management

**Input**: Design documents from `specs/004-order-management/`
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/orders-api.yaml`

## Format: `[ID] [P?] [Story?] Description with file path`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (`[US1]`, `[US2]`, `[US3]`, `[US4]`)
- Every task includes the exact file path and quoted constraints

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Database schema creation and migration registration

- [X] T001 Create Liquibase change set `src/main/resources/db/changelog/003-create-orders-tables.xml` for table `orders` and table `order_lines` with columns and foreign key constraints specified in `specs/004-order-management/data-model.md`
- [X] T002 Register `003-create-orders-tables.xml` in master change log `src/main/resources/db/changelog/db.changelog-master.xml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core domain model, ports, persistence base, and error handling

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T003 [P] Create `OrderStatus` enum with values `PENDING`, `PAID`, `SHIPPED`, `DELIVERED`, `CANCELLED` in `src/main/java/es/nachobrito/sdd/example/domain/model/OrderStatus.java`
- [X] T004 [P] Create domain exceptions `OrderNotFoundException`, `InvalidOrderDataException`, `DuplicateOrderIdentifierException`, and `OrderDeletionConflictException` in `src/main/java/es/nachobrito/sdd/example/domain/model/`
- [X] T005 [P] Create `DeliveryAddress` value object in `src/main/java/es/nachobrito/sdd/example/domain/model/DeliveryAddress.java` with required fields `name`, `telephone`, `street`, `city`, `state`, `zipCode`, `country` and optional `complement`
- [X] T006 [P] Create `InvoiceAddress` value object in `src/main/java/es/nachobrito/sdd/example/domain/model/InvoiceAddress.java` with required fields `name`, `vatNumber`, `telephone`, `street`, `city`, `state`, `zipCode`, `country` and optional `complement`
- [X] T007 [P] Create `OrderLine` value object in `src/main/java/es/nachobrito/sdd/example/domain/model/OrderLine.java` with constraints `quantity > 0`, `unitPriceInCents >= 0`, and calculated `lineTotalInCents = quantity * unitPriceInCents`
- [X] T008 [P] Create `Order` domain aggregate root in `src/main/java/es/nachobrito/sdd/example/domain/model/Order.java` enforcing invariants: `id` non-null UUID, `customerName` non-blank max 255 chars, `customerEmail` valid email, `date` non-null UTC Instant, `status` non-null `OrderStatus`, `totalAmountInCents` equals sum of line totals, `currency` 3-letter ISO-4217 code, `deliveryAddress` non-null, `invoiceAddress` non-null, `orderLines` non-empty list
- [X] T009 [P] Create unit tests in `src/test/java/es/nachobrito/sdd/example/domain/model/OrderTest.java` verifying invariants: total calculation, negative price rejection, quantity zero or negative rejection, invalid email rejection, invalid currency rejection, empty lines rejection
- [X] T010 [P] Create outbound port `OrderRepositoryPort` in `src/main/java/es/nachobrito/sdd/example/application/port/out/OrderRepositoryPort.java`
- [X] T011 [P] Create persistence entities `OrderEntity` and `OrderLineEntity` in `src/main/java/es/nachobrito/sdd/example/adapter/out/persistence/` mapping tables `orders` and `order_lines`
- [X] T012 [P] Create Micronaut Data JDBC repositories `OrderJdbcRepository` and `OrderLineJdbcRepository` in `src/main/java/es/nachobrito/sdd/example/adapter/out/persistence/`
- [X] T013 Create persistence adapter `OrderPersistenceAdapter` in `src/main/java/es/nachobrito/sdd/example/adapter/out/persistence/OrderPersistenceAdapter.java` implementing `OrderRepositoryPort` with transactional persistence
- [X] T014 [P] Create exception handler `OrderExceptionHandler` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/OrderExceptionHandler.java` mapping `OrderNotFoundException` to 404, `DuplicateOrderIdentifierException` and `OrderDeletionConflictException` to 409, and `InvalidOrderDataException` to 400

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Create and Retrieve Order (Priority: P1) 🎯 MVP

**Goal**: Enable clients to create an order with addresses and order lines using a client-provided UUID, and retrieve the order by identifier.

**Independent Test**: `POST /v1/orders` with a valid UUID and valid lines returns `201 Created` with `Location` header and server-calculated `totalAmountInCents`; subsequent `GET /v1/orders/{id}` returns `200 OK` with the stored order record.

### Tests for User Story 1

- [X] T015 [P] [US1] Create integration test `OrderControllerIT` in `src/test/java/es/nachobrito/sdd/example/adapter/in/rest/OrderControllerIT.java` testing `POST /v1/orders` (201 Created, 400 Bad Request, 409 Conflict) and `GET /v1/orders/{id}` (200 OK, 404 Not Found)
- [X] T016 [P] [US1] Create unit tests in `src/test/java/es/nachobrito/sdd/example/application/service/OrderServiceTest.java` for order creation and retrieval

### Implementation for User Story 1

- [X] T017 [P] [US1] Create inbound use case ports `CreateOrderUseCase` and `GetOrderUseCase` in `src/main/java/es/nachobrito/sdd/example/application/port/in/`
- [X] T018 [P] [US1] Create DTOs `CreateOrderRequest`, `DeliveryAddressDto`, `InvoiceAddressDto`, `OrderLineRequest`, `OrderLineResponse`, and `OrderResponse` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/dto/` with validation annotations
- [X] T019 [US1] Implement `CreateOrderUseCase` and `GetOrderUseCase` in `src/main/java/es/nachobrito/sdd/example/application/service/OrderService.java`
- [X] T020 [US1] Implement `POST /v1/orders` and `GET /v1/orders/{id}` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/OrderController.java` with OpenAPI documentation annotations

**Checkpoint**: User Story 1 is functional and testable independently (MVP ready)

---

## Phase 4: User Story 2 - Search and Filter Orders (Priority: P2)

**Goal**: Enable clients to filter orders by status, customer email, and date range, with sorting by date or status.

**Independent Test**: `GET /v1/orders?status=PENDING&customerEmail=alice@example.com&sort=date&direction=desc` returns matching orders.

### Tests for User Story 2

- [X] T021 [P] [US2] Add integration tests in `src/test/java/es/nachobrito/sdd/example/adapter/in/rest/OrderControllerIT.java` testing `GET /v1/orders` with filters (status, customerEmail, date range) and sorting (date, status)
- [X] T022 [P] [US2] Add unit tests in `src/test/java/es/nachobrito/sdd/example/application/service/OrderServiceTest.java` for order listing, filtering, and sorting

### Implementation for User Story 2

- [X] T023 [P] [US2] Create inbound use case port `ListOrdersUseCase` in `src/main/java/es/nachobrito/sdd/example/application/port/in/ListOrdersUseCase.java`
- [X] T024 [US2] Add query methods in `src/main/java/es/nachobrito/sdd/example/adapter/out/persistence/OrderJdbcRepository.java` and `OrderPersistenceAdapter.java` for dynamic filtering by status, customer email, date range, and sorting
- [X] T025 [US2] Implement `ListOrdersUseCase` in `src/main/java/es/nachobrito/sdd/example/application/service/OrderService.java`
- [X] T026 [US2] Implement `GET /v1/orders` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/OrderController.java` with query parameters (`status`, `customerEmail`, `startDate`, `endDate`, `sort`, `direction`)

**Checkpoint**: User Stories 1 and 2 are functional and testable independently

---

## Phase 5: User Story 3 - Update Order (Priority: P3)

**Goal**: Enable clients to update details, lines, addresses, or status of an existing order.

**Independent Test**: `PUT /v1/orders/{id}` with updated lines or status returns `200 OK` with recalculated totals and refreshed `updatedAt` timestamp.

### Tests for User Story 3

- [X] T027 [P] [US3] Add integration tests in `src/test/java/es/nachobrito/sdd/example/adapter/in/rest/OrderControllerIT.java` testing `PUT /v1/orders/{id}` (200 OK, 400 Bad Request, 404 Not Found)
- [X] T028 [P] [US3] Add unit tests in `src/test/java/es/nachobrito/sdd/example/application/service/OrderServiceTest.java` for order updates

### Implementation for User Story 3

- [X] T029 [P] [US3] Create inbound use case port `UpdateOrderUseCase` in `src/main/java/es/nachobrito/sdd/example/application/port/in/UpdateOrderUseCase.java`
- [X] T030 [P] [US3] Create DTO `UpdateOrderRequest` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/dto/UpdateOrderRequest.java` with validation annotations
- [X] T031 [US3] Implement `UpdateOrderUseCase` in `src/main/java/es/nachobrito/sdd/example/application/service/OrderService.java` updating attributes, lines, and recalculating totals
- [X] T032 [US3] Implement `PUT /v1/orders/{id}` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/OrderController.java`

**Checkpoint**: User Stories 1, 2, and 3 are functional and testable independently

---

## Phase 6: User Story 4 - Remove Order (Priority: P4)

**Goal**: Enable clients to permanently delete orders with PENDING or CANCELLED status while rejecting deletion for PAID, SHIPPED, or DELIVERED orders.

**Independent Test**: `DELETE /v1/orders/{id}` for a PENDING or CANCELLED order returns `204 No Content` and removes the order; attempting deletion for a PAID order returns `409 Conflict`.

### Tests for User Story 4

- [X] T033 [P] [US4] Add integration tests in `src/test/java/es/nachobrito/sdd/example/adapter/in/rest/OrderControllerIT.java` testing `DELETE /v1/orders/{id}` (204 No Content for PENDING/CANCELLED, 409 Conflict for PAID/SHIPPED/DELIVERED, 404 Not Found)
- [X] T034 [P] [US4] Add unit tests in `src/test/java/es/nachobrito/sdd/example/application/service/OrderServiceTest.java` for order deletion with status verification

### Implementation for User Story 4

- [X] T035 [P] [US4] Create inbound use case port `DeleteOrderUseCase` in `src/main/java/es/nachobrito/sdd/example/application/port/in/DeleteOrderUseCase.java`
- [X] T036 [US4] Implement `DeleteOrderUseCase` in `src/main/java/es/nachobrito/sdd/example/application/service/OrderService.java` verifying status eligibility and deleting the order via `OrderRepositoryPort`
- [X] T037 [US4] Implement `DELETE /v1/orders/{id}` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/OrderController.java`

**Checkpoint**: All user stories are complete and independently functional

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Verification against quality gates, architecture checks, and documentation

- [X] T038 [P] Verify OpenAPI annotations on `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/OrderController.java` align with `specs/004-order-management/contracts/orders-api.yaml`
- [X] T039 Execute ArchUnit verification in `src/test/java/es/nachobrito/sdd/example/architecture/HexagonalArchitectureTest.java`
- [X] T040 Execute static analysis and mutation coverage with `./gradlew check` to verify thresholds (80% line coverage, 90% mutation score)
- [X] T041 Execute manual verification scenarios from `specs/004-order-management/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Can start immediately.
- **Foundational (Phase 2)**: Depends on Phase 1 completion. Blocks all user stories.
- **User Stories (Phases 3 to 6)**: Depend on Phase 2 completion.
  - Can proceed sequentially (P1 → P2 → P3 → P4) or in parallel.
- **Polish (Phase 7)**: Depends on completion of all user stories.

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2). No dependency on other user stories.
- **User Story 2 (P2)**: Can start after Foundational (Phase 2). Independent from US1/US3/US4.
- **User Story 3 (P3)**: Can start after Foundational (Phase 2). Independent from US2/US4.
- **User Story 4 (P4)**: Can start after Foundational (Phase 2). Independent from US2/US3.

### Parallel Opportunities

- **Phase 1**: T001, T002 can run sequentially or in parallel.
- **Phase 2**: T003, T004, T005, T006, T007, T008, T009, T010, T011, T012, T014 can run in parallel.
- **User Story 1**: T015, T016, T017, T018 can run in parallel.
- **User Story 2**: T021, T022, T023 can run in parallel.
- **User Story 3**: T027, T028, T029, T030 can run in parallel.
- **User Story 4**: T033, T034, T035 can run in parallel.
- **Phase 7**: T038 can run in parallel with verification runs.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001 - T002).
2. Complete Phase 2: Foundational (T003 - T014).
3. Complete Phase 3: User Story 1 (T015 - T020).
4. **VALIDATE**: Run `OrderControllerIT` to verify creation and retrieval. Deliver MVP.

### Incremental Delivery

1. Foundation ready (Phases 1 and 2).
2. Add User Story 1 → Test independently → Deliver MVP.
3. Add User Story 2 → Test order searching and filtering.
4. Add User Story 3 → Test order updates and total recalculations.
5. Add User Story 4 → Test conditional order deletion.
6. Run Phase 7 quality gates.

