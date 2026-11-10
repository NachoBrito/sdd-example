# Tasks: Product Management

**Input**: Design documents from `specs/003-product-management/`
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/products-api.yaml`

## Format: `[ID] [P?] [Story?] Description with file path`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (`[US1]`, `[US2]`, `[US3]`, `[US4]`)
- Every task includes the exact file path and quoted constraints

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Database schema creation and migration registration

- [X] T001 Create directory layout for hexagonal architecture in `src/main/java/es/nachobrito/sdd/example/` and `src/test/java/es/nachobrito/sdd/example/`
- [X] T002 Create Liquibase change set `src/main/resources/db/changelog/002-create-products-table.xml` for table `products` with columns: `id VARCHAR(36) NOT NULL PRIMARY KEY`, `sku VARCHAR(100) NOT NULL UNIQUE`, `name VARCHAR(255) NOT NULL`, `description TEXT NULL`, `price_in_cents INT NOT NULL CHECK (price_in_cents >= 0)`, `currency CHAR(3) NOT NULL`, `status VARCHAR(20) NOT NULL`, `created_at TIMESTAMP(6) NOT NULL`, `updated_at TIMESTAMP(6) NOT NULL`
- [X] T003 Register `002-create-products-table.xml` in master change log `src/main/resources/db/changelog/db.changelog-master.xml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core domain model, ports, persistence base, and error infrastructure

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T004 [P] Create `ProductStatus` enum with values `DRAFT`, `ACTIVE`, `ARCHIVED` in `src/main/java/es/nachobrito/sdd/example/domain/model/ProductStatus.java`
- [X] T005 [P] Create domain exceptions `ProductNotFoundException`, `DuplicateIdentifierException`, `DuplicateSkuException`, and `InvalidProductDataException` in `src/main/java/es/nachobrito/sdd/example/domain/model/`
- [X] T006 [P] Create `Product` domain aggregate root in `src/main/java/es/nachobrito/sdd/example/domain/model/Product.java` enforcing invariants: `id` non-null UUID, `sku` non-blank max 100 chars, `name` non-blank max 255 chars, `description` nullable max 4000 chars, `priceInCents` integer >= 0, `currency` 3-letter ISO-4217 code, `status` non-null `ProductStatus`, `createdAt` and `updatedAt` non-null `Instant`
- [X] T007 [P] Create unit tests in `src/test/java/es/nachobrito/sdd/example/domain/model/ProductTest.java` verifying invariants: zero cents allowed, negative price rejected, invalid currency rejected, blank name/SKU rejected
- [X] T008 [P] Create outbound port `ProductRepositoryPort` in `src/main/java/es/nachobrito/sdd/example/application/port/out/ProductRepositoryPort.java`
- [X] T009 [P] Create persistence entity `ProductEntity` in `src/main/java/es/nachobrito/sdd/example/adapter/out/persistence/ProductEntity.java` mapping columns `id`, `sku`, `name`, `description`, `price_in_cents`, `currency`, `status`, `created_at`, `updated_at` with client-assigned `@Id UUID id`
- [X] T010 Create Micronaut Data JDBC repository `ProductJdbcRepository` in `src/main/java/es/nachobrito/sdd/example/adapter/out/persistence/ProductJdbcRepository.java`
- [X] T011 Create persistence adapter `ProductPersistenceAdapter` in `src/main/java/es/nachobrito/sdd/example/adapter/out/persistence/ProductPersistenceAdapter.java` implementing `ProductRepositoryPort`
- [X] T012 [P] Create error response DTO `ErrorResponse` and exception handler `ProductExceptionHandler` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/` mapping domain exceptions to HTTP 400, 404, and 409

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Create and Retrieve Product (Priority: P1) 🎯 MVP

**Goal**: Enable clients to create a product using a client-generated UUID and retrieve the product by identifier.

**Independent Test**: `POST /v1/products` with a valid UUID and valid attributes returns `201 Created`; subsequent `GET /v1/products/{id}` returns `200 OK` with the exact product record.

### Tests for User Story 1

- [X] T013 [P] [US1] Create integration test `ProductControllerIT` in `src/test/java/es/nachobrito/sdd/example/adapter/in/rest/ProductControllerIT.java` testing `POST /v1/products` (201 Created, 400 Bad Request, 409 Conflict) and `GET /v1/products/{id}` (200 OK, 404 Not Found)
- [X] T014 [P] [US1] Create unit tests in `src/test/java/es/nachobrito/sdd/example/application/service/ProductServiceTest.java` for product creation and retrieval

### Implementation for User Story 1

- [X] T015 [P] [US1] Create inbound use case ports `CreateProductUseCase` and `GetProductUseCase` in `src/main/java/es/nachobrito/sdd/example/application/port/in/`
- [X] T016 [P] [US1] Create DTOs `CreateProductRequest` and `ProductResponse` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/dto/` with validation annotations (`@NotNull`, `@PositiveOrZero`, `@Pattern(regexp = "^[A-Z]{3}$")`)
- [X] T017 [US1] Implement `CreateProductUseCase` and `GetProductUseCase` in `src/main/java/es/nachobrito/sdd/example/application/service/ProductService.java`
- [X] T018 [US1] Implement `POST /v1/products` and `GET /v1/products/{id}` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/ProductController.java` with OpenAPI documentation annotations

**Checkpoint**: User Story 1 is functional and testable independently (MVP ready)

---

## Phase 4: User Story 2 - Search and Filter Products (Priority: P2)

**Goal**: Enable clients to filter products by SKU, name regular expression, and status, with sorting by name, status, or creation timestamp.

**Independent Test**: `GET /v1/products?status=ACTIVE&nameRegex=Gaming.*&sort=name&direction=asc` returns matching products ordered by name.

### Tests for User Story 2

- [X] T019 [P] [US2] Add integration tests in `src/test/java/es/nachobrito/sdd/example/adapter/in/rest/ProductControllerIT.java` testing `GET /v1/products` filtering by SKU, name regex, status, and sorting by name, status, and createdAt
- [X] T020 [P] [US2] Add unit tests in `src/test/java/es/nachobrito/sdd/example/application/service/ProductServiceTest.java` for product search and filtering

### Implementation for User Story 2

- [X] T021 [P] [US2] Create inbound use case port `ListProductsUseCase` in `src/main/java/es/nachobrito/sdd/example/application/port/in/ListProductsUseCase.java`
- [X] T022 [US2] Add filtering query methods with MariaDB `REGEXP` support in `src/main/java/es/nachobrito/sdd/example/adapter/out/persistence/ProductJdbcRepository.java`
- [X] T023 [US2] Implement filter and sort mapping in `src/main/java/es/nachobrito/sdd/example/adapter/out/persistence/ProductPersistenceAdapter.java` and `src/main/java/es/nachobrito/sdd/example/application/port/out/ProductRepositoryPort.java`
- [X] T024 [US2] Implement `ListProductsUseCase` in `src/main/java/es/nachobrito/sdd/example/application/service/ProductService.java`
- [X] T025 [US2] Implement `GET /v1/products` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/ProductController.java` with query parameters (`sku`, `nameRegex`, `status`, `sort`, `direction`)

**Checkpoint**: User Stories 1 and 2 are functional and testable independently

---

## Phase 5: User Story 3 - Update Product Information (Priority: P3)

**Goal**: Enable clients to update product attributes by identifier and refresh the last update timestamp.

**Independent Test**: `PUT /v1/products/{id}` with updated attributes returns `200 OK` with refreshed `updatedAt` timestamp.

### Tests for User Story 3

- [X] T026 [P] [US3] Add integration tests in `src/test/java/es/nachobrito/sdd/example/adapter/in/rest/ProductControllerIT.java` testing `PUT /v1/products/{id}` (200 OK, 400 Bad Request, 404 Not Found, 409 Conflict)
- [X] T027 [P] [US3] Add unit tests in `src/test/java/es/nachobrito/sdd/example/application/service/ProductServiceTest.java` for product updates

### Implementation for User Story 3

- [X] T028 [P] [US3] Create inbound use case port `UpdateProductUseCase` in `src/main/java/es/nachobrito/sdd/example/application/port/in/UpdateProductUseCase.java`
- [X] T029 [P] [US3] Create DTO `UpdateProductRequest` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/dto/UpdateProductRequest.java` with validation annotations (`@NotNull`, `@PositiveOrZero`, `@Pattern(regexp = "^[A-Z]{3}$")`)
- [X] T030 [US3] Implement `UpdateProductUseCase` in `src/main/java/es/nachobrito/sdd/example/application/service/ProductService.java` updating attributes and setting `updatedAt` to current UTC timestamp
- [X] T031 [US3] Implement `PUT /v1/products/{id}` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/ProductController.java`

**Checkpoint**: User Stories 1, 2, and 3 are functional and testable independently

---

## Phase 6: User Story 4 - Remove Product (Priority: P4)

**Goal**: Enable clients to permanently remove a product from the database.

**Independent Test**: `DELETE /v1/products/{id}` returns `204 No Content`; subsequent `GET /v1/products/{id}` returns `404 Not Found`.

### Tests for User Story 4

- [X] T032 [P] [US4] Add integration tests in `src/test/java/es/nachobrito/sdd/example/adapter/in/rest/ProductControllerIT.java` testing `DELETE /v1/products/{id}` (204 No Content, 404 Not Found)
- [X] T033 [P] [US4] Add unit tests in `src/test/java/es/nachobrito/sdd/example/application/service/ProductServiceTest.java` for product deletion

### Implementation for User Story 4

- [X] T034 [P] [US4] Create inbound use case port `DeleteProductUseCase` in `src/main/java/es/nachobrito/sdd/example/application/port/in/DeleteProductUseCase.java`
- [X] T035 [US4] Implement `DeleteProductUseCase` in `src/main/java/es/nachobrito/sdd/example/application/service/ProductService.java` calling repository port deletion
- [X] T036 [US4] Implement `DELETE /v1/products/{id}` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/ProductController.java`

**Checkpoint**: All user stories are complete and independently functional

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Verification against quality gates, architecture checks, and documentation

- [X] T037 [P] Verify OpenAPI annotations on `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/ProductController.java` align with `specs/003-product-management/contracts/products-api.yaml`
- [X] T038 Execute ArchUnit verification in `src/test/java/es/nachobrito/sdd/example/architecture/HexagonalArchitectureTest.java`
- [X] T039 Execute static analysis and mutation coverage with `./gradlew check` to verify thresholds (80% coverage, 90% mutation)
- [X] T040 Execute manual verification scenarios from `specs/003-product-management/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Can start immediately.
- **Foundational (Phase 2)**: Depends on Phase 1 completion. Blocks all user stories.
- **User Stories (Phases 3 to 6)**: Depend on Phase 2 completion.
  - Can proceed sequentially (P1 → P2 → P3 → P4) or in parallel.
- **Polish (Phase 7)**: Depends on completion of all user stories.

### Parallel Opportunities

- **Phase 1**: T001, T002 can start in parallel.
- **Phase 2**: T004, T005, T006, T007, T008, T009, T012 can run in parallel.
- **User Story 1**: T013, T014, T015, T016 can run in parallel.
- **User Story 2**: T019, T020, T021 can run in parallel.
- **User Story 3**: T026, T027, T028, T029 can run in parallel.
- **User Story 4**: T032, T033, T034 can run in parallel.
- **Phase 7**: T037, T038, T039, T040 can execute in sequence or parallel as appropriate.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001 - T003).
2. Complete Phase 2: Foundational (T004 - T012).
3. Complete Phase 3: User Story 1 (T013 - T018).
4. **VALIDATE**: Run `ProductControllerIT` to verify creation and retrieval. Deliver MVP.

### Incremental Delivery

1. Foundation ready (Phases 1 and 2).
2. Add User Story 1 → Test independently → Deploy/Demo (MVP).
3. Add User Story 2 → Test search and filtering.
4. Add User Story 3 → Test product updates.
5. Add User Story 4 → Test product deletion.
6. Run Phase 7 quality gates.

