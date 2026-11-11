# Tasks: Refactor Controller Error Management

**Input**: Design documents from `specs/005-refactor-error-management/`
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/error-api.yaml`

## Format: `[ID] [P?] [Story?] Description with file path`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (`[US1]`, `[US2]`, `[US3]`, `[US4]`)
- Every task includes the exact file path and clear action

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Baseline verification of the current codebase and test suite

- [X] T001 Verify the baseline build and test execution using `./gradlew test`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Create domain base exceptions for bounded contexts

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T002 [P] Create abstract domain base exception `ProductException` in `src/main/java/es/nachobrito/sdd/example/domain/model/ProductException.java`
- [X] T003 Update product domain exceptions in `src/main/java/es/nachobrito/sdd/example/domain/model/` to extend `ProductException`
- [X] T004 [P] Create abstract domain base exception `OrderException` in `src/main/java/es/nachobrito/sdd/example/domain/model/OrderException.java`
- [X] T005 Update order domain exceptions in `src/main/java/es/nachobrito/sdd/example/domain/model/` to extend `OrderException`

**Checkpoint**: Base domain exceptions established - user story implementation can now begin

---

## Phase 3: User Story 1 - Standardized Error Responses for Product Controller (Priority: P1) 🎯 MVP

**Goal**: Replace the product error handling controller with a dedicated Micronaut `ExceptionHandler` class.

**Independent Test**: Submit invalid, missing, and conflicting product requests. Verify that responses return HTTP 400, 404, and 409 status codes with standardized `ErrorResponse` bodies.

### Tests for User Story 1

- [X] T006 [P] [US1] Update unit tests in `src/test/java/es/nachobrito/sdd/example/adapter/in/rest/ProductExceptionHandlerTest.java` for `ExceptionHandler` implementation

### Implementation for User Story 1

- [X] T007 [US1] Refactor `ProductExceptionHandler` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/ProductExceptionHandler.java` to implement `ExceptionHandler<ProductException, HttpResponse<ErrorResponse>>`

**Checkpoint**: User Story 1 is functional and testable independently (MVP ready)

---

## Phase 4: User Story 2 - Standardized Error Responses for Order Controller (Priority: P2)

**Goal**: Replace the order error handling controller with a dedicated Micronaut `ExceptionHandler` class.

**Independent Test**: Submit invalid, missing, and conflicting order requests. Verify that responses return HTTP 400, 404, and 409 status codes with standardized `ErrorResponse` bodies.

### Tests for User Story 2

- [X] T008 [P] [US2] Update unit tests in `src/test/java/es/nachobrito/sdd/example/adapter/in/rest/OrderExceptionHandlerTest.java` for `ExceptionHandler` implementation

### Implementation for User Story 2

- [X] T009 [US2] Refactor `OrderExceptionHandler` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/OrderExceptionHandler.java` to implement `ExceptionHandler<OrderException, HttpResponse<ErrorResponse>>`

**Checkpoint**: User Stories 1 and 2 are functional and testable independently

---

## Phase 5: User Story 3 - Elimination of Error Controller Endpoints (Priority: P3)

**Goal**: Eliminate `/v1/errors` and `/v1/order-errors` from HTTP routing and OpenAPI documentation.

**Independent Test**: Send HTTP GET requests to `/v1/errors` and `/v1/order-errors`. Verify that the server returns HTTP 404 Not Found.

### Tests for User Story 3

- [X] T010 [P] [US3] Create integration tests in `src/test/java/es/nachobrito/sdd/example/adapter/in/rest/ErrorRouteRemovalTestCase.java` verifying 404 for removed error paths

**Checkpoint**: User Stories 1, 2, and 3 are functional and verified

---

## Phase 6: User Story 4 - Consistent Handler Architecture for Future Features (Priority: P4)

**Goal**: Enforce and document the 1:1 controller-to-exception-handler pattern.

**Independent Test**: Verify that each REST controller is paired with exactly one exception handler class in `adapter.in.rest`.

### Tasks for User Story 4

- [X] T011 [P] [US4] Add architecture test in `src/test/java/es/nachobrito/sdd/example/architecture/HexagonalArchitectureTest.java` verifying exception handler package rules
- [X] T012 [P] [US4] Document the 1:1 controller exception handler pattern in `docs/project-setup.md`

**Checkpoint**: All user stories are complete and architecturally verified

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Full regression testing and static analysis compliance

- [X] T013 [P] Execute manual validation scenarios from `specs/005-refactor-error-management/quickstart.md`
- [X] T014 Run full verification suite with `./gradlew build` including Pitest mutation testing

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User Story 1 (P1) and User Story 2 (P2) can proceed in parallel or sequentially
  - User Story 3 (P3) depends on US1 and US2 completion
  - User Story 4 (P4) depends on US1 and US2 completion
- **Polish (Phase 7)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Depends on `ProductException` base classes from Phase 2
- **User Story 2 (P2)**: Depends on `OrderException` base classes from Phase 2
- **User Story 3 (P3)**: Depends on removal of `@Controller` annotations in US1 and US2
- **User Story 4 (P4)**: Depends on US1 and US2 handler implementations

### Parallel Opportunities

- T002 and T004 (creating base exceptions) can run in parallel
- T006 and T008 (test updates) can run in parallel
- T010, T011, and T012 can run in parallel
- T013 and T014 can run sequentially after all implementations

---

## Parallel Example: Base Exceptions

```bash
# Launch base exceptions creation in parallel:
Task: "T002 [P] Create abstract domain base exception ProductException in src/main/java/es/nachobrito/sdd/example/domain/model/ProductException.java"
Task: "T004 [P] Create abstract domain base exception OrderException in src/main/java/es/nachobrito/sdd/example/domain/model/OrderException.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001)
2. Complete Phase 2: Foundational (T002 - T005)
3. Complete Phase 3: User Story 1 (T006 - T007)
4. **STOP and VALIDATE**: Verify product error handling independently

### Incremental Delivery

1. Complete Setup + Foundational
2. Add User Story 1 (Product Exception Handler) → Test independently (MVP)
3. Add User Story 2 (Order Exception Handler) → Test independently
4. Add User Story 3 (Route Removal Verification) → Test independently
5. Add User Story 4 (Architecture Rule and Documentation) → Verify compliance
6. Run Phase 7 Polish (`./gradlew build`) for full static analysis and mutation checks

