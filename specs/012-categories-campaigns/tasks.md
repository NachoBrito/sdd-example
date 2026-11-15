---

description: "Task list for Product Categories and Discount Campaigns"
---

# Tasks: Product Categories and Discount Campaigns

**Input**: Design documents from `/specs/012-categories-campaigns/`
**Prerequisites**: [plan.md](file:///home/nacho/github/sdd-example/specs/012-categories-campaigns/plan.md), [spec.md](file:///home/nacho/github/sdd-example/specs/012-categories-campaigns/spec.md), [research.md](file:///home/nacho/github/sdd-example/specs/012-categories-campaigns/research.md), [data-model.md](file:///home/nacho/github/sdd-example/specs/012-categories-campaigns/data-model.md), [categories-api.yaml](file:///home/nacho/github/sdd-example/specs/012-categories-campaigns/contracts/categories-api.yaml), [campaigns-api.yaml](file:///home/nacho/github/sdd-example/specs/012-categories-campaigns/contracts/campaigns-api.yaml), [products-api.yaml](file:///home/nacho/github/sdd-example/specs/012-categories-campaigns/contracts/products-api.yaml), [quickstart.md](file:///home/nacho/github/sdd-example/specs/012-categories-campaigns/quickstart.md)

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., `US1`, `US2`, `US3`, `US4`)
- Exact file paths are included in every task description.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirm baseline build readiness and inspect changelog registration before database and domain changes.

- [X] T001 Verify baseline compilation and test discovery via `./gradlew testClasses` and inspect master changelog registration in `src/main/resources/db/changelog/db.changelog-master.xml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core database schema, category and extended product domain types, and outbound repository contracts required before user stories begin.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T002 Create Liquibase migration creating `categories` (`id` VARCHAR(36) PK NOT NULL, `name` VARCHAR(255) NOT NULL, `description` TEXT NULL, index `idx_categories_name`), adding nullable `products.category_id` (`VARCHAR(36)` NULL, FK `fk_products_category` referencing `categories(id)` ON DELETE RESTRICT, index `idx_products_category_id`), creating `campaigns` (`id` VARCHAR(36) PK NOT NULL, `name` VARCHAR(255) NOT NULL, `description` TEXT NULL, `status` VARCHAR(20) NOT NULL, `start_date` TIMESTAMP(6) NOT NULL, `end_date` TIMESTAMP(6) NOT NULL, `discount_percent` INT NOT NULL, `chk_campaigns_discount_percent` 0..100, `chk_campaigns_date_range` `end_date >= start_date`, index `idx_campaigns_status_dates`), and creating `campaign_predicates` (`id` VARCHAR(36) PK NOT NULL, `campaign_id` VARCHAR(36) NOT NULL FK `fk_campaign_predicates_campaign` ON DELETE CASCADE, `predicate_type` VARCHAR(32) NOT NULL, `predicate_value` VARCHAR(255) NOT NULL, index `idx_campaign_predicates_campaign_id`) with `<rollback>` blocks in `src/main/resources/db/changelog/005-add-categories-and-campaigns.xml` and register it in `src/main/resources/db/changelog/db.changelog-master.xml`
- [X] T003 [P] Create pure Java `Category` record (`UUID id` non-null, `String name` required non-blank trimmed max 255 characters, `@Nullable String description` optional trimmed max 4000 characters) and exception hierarchy (`CategoryException`, `InvalidCategoryDataException`, `CategoryNotFoundException`, `DuplicateCategoryIdentifierException`, `CategoryInUseException`) in `src/main/java/es/nachobrito/sdd/example/domain/model/`
- [X] T004 [P] Extend `Product` record with `@Nullable UUID categoryId` (zero campaign or discount fields stored on `Product`) in `src/main/java/es/nachobrito/sdd/example/domain/model/Product.java`
- [X] T005 [P] Create `CategoryRepositoryPort` outbound port (`save`, `findById`, `existsById`, `deleteById`, `findAll(name, description)`) in `src/main/java/es/nachobrito/sdd/example/application/port/out/CategoryRepositoryPort.java` and add `boolean existsByCategoryId(UUID categoryId)` to `src/main/java/es/nachobrito/sdd/example/application/port/out/ProductRepositoryPort.java`
- [X] T006 [P] Implement `CategoryEntity`, `CategoryJdbcRepository` (using `LOCATE(LOWER(:filter), LOWER(col)) > 0` for case-insensitive substring matching on `name` and `description`), and `@DbQueryMetrics` `CategoryPersistenceAdapter` in `src/main/java/es/nachobrito/sdd/example/adapter/out/persistence/`
- [X] T007 [P] Update `ProductEntity`, `ProductJdbcRepository` (adding `boolean existsByCategoryId(UUID categoryId)`), and `ProductPersistenceAdapter` to map `@Nullable @MappedProperty("category_id") UUID categoryId` and delegate `existsByCategoryId` in `src/main/java/es/nachobrito/sdd/example/adapter/out/persistence/`

**Checkpoint**: Foundation ready — user story implementation can now begin.

---

## Phase 3: User Story 1 - Manage Product Categories (Priority: P1) 🎯 MVP

**Goal**: Allow `ADMIN` users to create and delete categories (rejecting deletion when products reference the category) and allow `CUSTOMER` and `CATALOG` users to list categories with optional `name` and `description` filters.

**Independent Test**: Authenticate as `ADMIN` to create a category, authenticate as `CUSTOMER` and `CATALOG` to list and filter categories, confirm non-`ADMIN` mutation attempts fail with `403`, and confirm deleting an unused category succeeds while deleting an in-use category fails with `409`.

- [X] T008 [P] [US1] Create inbound ports `CreateCategoryUseCase` (`id`, `name`, `description`), `ListCategoriesUseCase` (`name`, `description`), `DeleteCategoryUseCase` (`id`), and composite `CategoryUseCase` in `src/main/java/es/nachobrito/sdd/example/application/port/in/`
- [X] T009 [US1] Implement `CategoryService` (constructor injection of `CategoryRepositoryPort` and `ProductRepositoryPort`; checking duplicate `id` with `DuplicateCategoryIdentifierException`, checking existence with `CategoryNotFoundException`, and blocking delete via `productRepository.existsByCategoryId(id)` with `CategoryInUseException`) in `src/main/java/es/nachobrito/sdd/example/application/service/CategoryService.java`
- [X] T010 [P] [US1] Create REST DTOs `CreateCategoryRequest` (`@NotNull UUID id`, `@NotBlank @Size(max = 255) String name`, `@Nullable @Size(max = 4000) String description`) and `CategoryResponse` (`UUID id`, `String name`, `@Nullable String description`) in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/dto/`
- [X] T011 [P] [US1] Create `CategoryExceptionHandler` mapping `CategoryNotFoundException` (`404`), `DuplicateCategoryIdentifierException` and `CategoryInUseException` (`409`), and `InvalidCategoryDataException` (`400`) to `ErrorResponse` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/CategoryExceptionHandler.java`
- [X] T012 [US1] Create `CategoryController` (`@Controller("/v1/categories")`, `@Secured({"ADMIN"})` on `POST` and `DELETE /{id}`, `@Secured({"CUSTOMER", "CATALOG"})` on `GET` with optional `name` and `description` query filters, OpenAPI annotations, no single-category GET endpoint) in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/CategoryController.java`
- [X] T013 [P] [US1] Add unit tests for `Category` domain validation, `CategoryPersistenceAdapter`, `CategoryService`, `CategoryExceptionHandler`, and `CategoryController` in `src/test/java/es/nachobrito/sdd/example/`
- [X] T014 [US1] Add REST Assured integration test `CategoryLifecycleTestCase` covering `ADMIN`-only create/delete, `CUSTOMER`/`CATALOG` list and substring filtering, `401`/`403`/`404`/`409` responses, and in-use category deletion conflict in `src/test/java/es/nachobrito/sdd/example/adapter/in/rest/CategoryLifecycleTestCase.java`

**Checkpoint**: User Story 1 is independently functional and testable (MVP).

---

## Phase 4: User Story 2 - Assign Valid Categories to Products (Priority: P1)

**Goal**: Support optional `categoryId` on `/v1/products` (backward-compatible), require non-null `categoryId` on `/v2/products`, and validate every provided `categoryId` against persisted categories.

**Independent Test**: Create a category via `/v1/categories`, list categories via `GET /v1/categories`, verify `POST /v1/products` succeeds both with and without `categoryId`, verify `POST /v2/products` and `PUT /v2/products/{id}` require a valid `categoryId`, and confirm nonexistent `categoryId` values return `400 Bad Request` on both versions.

- [X] T015 [P] [US2] Update `CreateProductCommand` and `UpdateProductCommand` with `@Nullable UUID categoryId` in `src/main/java/es/nachobrito/sdd/example/application/port/in/CreateProductUseCase.java` and `src/main/java/es/nachobrito/sdd/example/application/port/in/UpdateProductUseCase.java`
- [X] T016 [US2] Update `ProductService` (constructor injection of `CategoryRepositoryPort`; validate `if (command.categoryId() != null && !categoryRepository.existsById(command.categoryId()))` throwing `InvalidProductDataException` in `createProduct` and `updateProduct`) in `src/main/java/es/nachobrito/sdd/example/application/service/ProductService.java`
- [X] T017 [P] [US2] Update `CreateProductRequest` and `UpdateProductRequest` with optional `@Nullable UUID categoryId` and add `@Nullable UUID categoryId` to `ProductResponse` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/dto/`
- [X] T018 [P] [US2] Create `CreateProductV2Request` and `UpdateProductV2Request` requiring `@NotNull UUID categoryId` alongside validated `id`, `sku` (1..100), `name` (1..255), `description` (<=4000), `priceInCents` (>=0), `currency` (3-letter ISO-4217), and `status` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/dto/`
- [X] T019 [US2] Update `ProductController` (`/v1/products`) to pass `request.categoryId()` to `CreateProductCommand` and `UpdateProductCommand` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/ProductController.java`
- [X] T020 [US2] Create `ProductV2Controller` (`@Controller("/v2/products")`, `@Secured({"CATALOG"})` on `POST`/`PUT`/`DELETE`, `@Secured({"CUSTOMER", "CATALOG"})` on `GET`, delegating to `ProductUseCase` and sharing `ProductExceptionHandler`, with OpenAPI annotations) in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/ProductV2Controller.java`
- [X] T021 [P] [US2] Update existing product unit tests (`ProductTest`, `ProductNullValidationTest`, `ProductEntityTest`, `ProductPersistenceAdapterCommandTest`, `ProductPersistenceAdapterQueryTest`, `ProductServiceCommandTest`, `ProductServiceQueryTest`, `ProductControllerTest`, `ProductResponseTest`) and add `ProductV2LifecycleTestCase` verifying `/v1/products` backward compatibility, `/v2/products` mandatory `categoryId`, category lookup via `GET /v1/categories`, and `400` on unknown `categoryId` in `src/test/java/es/nachobrito/sdd/example/`

**Checkpoint**: User Stories 1 and 2 work independently and together without breaking `/v1/products`.

---

## Phase 5: User Story 3 - Manage Promotional Campaigns (Priority: P2)

**Goal**: Allow `ADMIN` users to create, update, list (with filters), and delete campaigns with status (`DRAFT`, `ACTIVE`, `INACTIVE`), UTC `[startDate, endDate]`, `discountPercent` (0..100), and typed predicates (`IN_CATEGORY`, `NOT_IN_CATEGORY`, `IS_CHEAPER_THAN`, `IS_MORE_EXPENSIVE_THAN`).

**Independent Test**: Authenticate as `ADMIN`, create a campaign with predicates, update its fields and predicates, list campaigns using `name`, `description`, `status`, `startDate`, and `endDate` filters, verify validation failures (`400`) and non-`ADMIN` access rejections (`403`), and delete the campaign.

- [X] T022 [P] [US3] Create `CampaignStatus` (`DRAFT`, `ACTIVE`, `INACTIVE`), `PredicateType` (`IN_CATEGORY`, `NOT_IN_CATEGORY`, `IS_CHEAPER_THAN`, `IS_MORE_EXPENSIVE_THAN`), `CampaignPredicate` (`predicateType` non-null, `value` non-blank trimmed max 255 chars, UUID validation for category predicates, non-negative integer cents validation for price predicates, pure `matches(Product)`), `Campaign` (`id` non-null, `name` non-blank trimmed max 255, `description` optional trimmed max 4000, `status` non-null, `startDate`/`endDate` non-null with `!endDate.isBefore(startDate)`, `discountPercent` 0..100, immutable `predicates` list, `isActiveAt(Instant)`, logical-`AND` `matches(Product)`), `ActiveDiscount`, and `CampaignException` hierarchy (`InvalidCampaignDataException`, `CampaignNotFoundException`, `DuplicateCampaignIdentifierException`) in `src/main/java/es/nachobrito/sdd/example/domain/model/`
- [X] T023 [P] [US3] Create `CampaignRepositoryPort` outbound port (`save`, `update`, `findById`, `existsById`, `deleteById`, `findAll(name, description, status, startDate, endDate)`, `findActiveAt(Instant)`) in `src/main/java/es/nachobrito/sdd/example/application/port/out/CampaignRepositoryPort.java`
- [X] T024 [P] [US3] Implement `CampaignEntity`, `CampaignPredicateEntity`, `CampaignJdbcRepository` (using `LOCATE(LOWER(:filter), LOWER(col)) > 0`, overlap filter `(:startDate IS NULL OR c.end_date >= :startDate) AND (:endDate IS NULL OR c.start_date <= :endDate)`, and active query `c.status = 'ACTIVE' AND c.start_date <= :now AND c.end_date >= :now`), `CampaignPredicateJdbcRepository`, and `@DbQueryMetrics` `CampaignPersistenceAdapter` (with `@Transactional` child predicate loading and replacement) in `src/main/java/es/nachobrito/sdd/example/adapter/out/persistence/`
- [X] T025 [P] [US3] Create inbound ports `CreateCampaignUseCase`, `UpdateCampaignUseCase`, `ListCampaignsUseCase`, `DeleteCampaignUseCase`, `ResolveProductDiscountsUseCase`, and composite `CampaignUseCase` in `src/main/java/es/nachobrito/sdd/example/application/port/in/`
- [X] T026 [US3] Implement `CampaignService` (create with duplicate `id` check, update with `CampaignNotFoundException`, list with `startDate <= endDate` check, delete with existence check, and request-time `resolveActiveDiscounts(Product)` / `resolveActiveDiscounts(List<Product>)` using `Clock`) in `src/main/java/es/nachobrito/sdd/example/application/service/CampaignService.java`
- [X] T027 [P] [US3] Create REST DTOs `CampaignPredicateDto` (`@NotNull PredicateType`, `@NotBlank @Size(max = 255) String value`), `CreateCampaignRequest` (`@NotNull UUID id`, `@NotBlank @Size(max = 255) String name`, `@Nullable @Size(max = 4000) String description`, `@NotNull CampaignStatus status`, `@NotNull Instant startDate`, `@NotNull Instant endDate`, `@NotNull @Min(0) @Max(100) Integer discountPercent`, `@Valid @Nullable List<CampaignPredicateDto> predicates`), `UpdateCampaignRequest`, and `CampaignResponse` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/dto/`
- [X] T028 [P] [US3] Create `CampaignExceptionHandler` mapping `CampaignNotFoundException` (`404`), `DuplicateCampaignIdentifierException` (`409`), and `InvalidCampaignDataException` (`400`) to `ErrorResponse` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/CampaignExceptionHandler.java`
- [X] T029 [US3] Create `CampaignController` (`@Controller("/v1/campaigns")`, `@Secured({"ADMIN"})` on `POST`, `PUT /{id}`, `GET` with optional `name`, `description`, `status`, `startDate`, `endDate` filters, and `DELETE /{id}`, OpenAPI annotations) in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/CampaignController.java`
- [X] T030 [P] [US3] Add unit tests for `Campaign`, `CampaignPredicate`, `CampaignPersistenceAdapter`, `CampaignService`, `CampaignExceptionHandler`, and `CampaignController`, plus REST Assured integration test `CampaignLifecycleTestCase` in `src/test/java/es/nachobrito/sdd/example/`

**Checkpoint**: User Story 3 campaign CRUD, filtering, validation, and authorization are complete.

---

## Phase 6: User Story 4 - Expose Active Discounts on Product Reads (Priority: P2)

**Goal**: Resolve active matching campaigns at request time on `GET /v1/products`, `GET /v1/products/{id}`, `GET /v2/products`, and `GET /v2/products/{id}`, exposing `discounts` (`campaignId`, `campaignName`, `discountPercent`) in `ProductResponse` without storing campaign state on `Product`.

**Independent Test**: Create an `ACTIVE` in-window campaign and a `DRAFT` or expired campaign targeting products by category and price, then query `/v1/products`, `/v1/products/{id}`, `/v2/products`, and `/v2/products/{id}` to confirm only active matching discounts appear and `priceInCents` stays unchanged.

- [X] T031 [US4] Create `ProductDiscountResponse` (`UUID campaignId`, `String campaignName`, `int discountPercent`) and extend `ProductResponse` with immutable `List<ProductDiscountResponse> discounts` (defaulting to `List.of()` in `fromDomain(Product)` and mapping `List<ActiveDiscount>` in `fromDomain(Product, List<ActiveDiscount>)`) in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/dto/ProductDiscountResponse.java` and `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/dto/ProductResponse.java`
- [X] T032 [US4] Inject `ResolveProductDiscountsUseCase` into `ProductController` and `ProductV2Controller` and attach request-time active discounts on `getById` (`resolveActiveDiscounts(product)`) and `list` (`resolveActiveDiscounts(products)`) in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/ProductController.java` and `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/ProductV2Controller.java`
- [X] T033 [P] [US4] Add unit tests covering request-time discount resolution across single/multiple matching campaigns, `DRAFT`/`INACTIVE`/expired/future windows, empty predicates, uncategorized `NOT_IN_CATEGORY`, strict `<`/`>` price thresholds, and controller wiring in `src/test/java/es/nachobrito/sdd/example/domain/model/CampaignMatchingTest.java`, `src/test/java/es/nachobrito/sdd/example/application/service/CampaignDiscountResolutionTest.java`, and `src/test/java/es/nachobrito/sdd/example/adapter/in/rest/ProductControllerTest.java`
- [X] T034 [US4] Add end-to-end REST Assured integration test `ProductDiscountIntegrationTestCase` verifying `/v1/products`, `/v1/products/{id}`, `/v2/products`, and `/v2/products/{id}` resolve active discounts at request time for `CUSTOMER` and `CATALOG` callers while keeping `priceInCents` unmodified in `src/test/java/es/nachobrito/sdd/example/adapter/in/rest/ProductDiscountIntegrationTestCase.java`

**Checkpoint**: All user stories (US1, US2, US3, US4) are independently functional and verified.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Ensure full compliance with ASD-STE100 documentation, ArchUnit rules, static analysis gates, and Pitest mutation/coverage thresholds.

- [X] T035 [P] Audit all new and modified Javadocs, inline comments, and documentation for ASD-STE100 compliance and verify zero Checkstyle, PMD, SpotBugs, and ArchUnit (`HexagonalArchitectureTest`, `ExceptionHandlerArchitectureTest`) findings
- [ ] T036 Run full verification build via `./gradlew build` confirming `-Werror` compilation, unit and integration tests, ArchUnit, Checkstyle, PMD, SpotBugs, and Pitest (80% line coverage and 90% mutation score) pass, and verify all scenarios in `specs/012-categories-campaigns/quickstart.md` (blocked in sandbox: unit tests, static analysis, ArchUnit, and Pitest pass, while 13 `*TestCase` integration suites fail without `/var/run/docker.sock`)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — starts immediately.
- **Foundational (Phase 2)**: Depends on Phase 1 — blocks all user stories (`T002`–`T007`).
- **User Story 1 (Phase 3 - P1)**: Depends on Phase 2 (`T002`, `T003`, `T005`, `T006`).
- **User Story 2 (Phase 4 - P1)**: Depends on Phase 2 (`T002`, `T004`, `T005`, `T007`); integration test `T021` creates categories via US1 (`CategoryController`) or `CategoryRepositoryPort`.
- **User Story 3 (Phase 5 - P2)**: Depends on Phase 2 (`T002`, `T004`). Can proceed in parallel with US1 and US2 once Phase 2 is complete.
- **User Story 4 (Phase 6 - P2)**: Depends on Phase 4 (`ProductController`, `ProductV2Controller`, `ProductResponse`) and Phase 5 (`ResolveProductDiscountsUseCase`, `CampaignService`, `ActiveDiscount`).
- **Polish (Phase 7)**: Depends on Phases 3, 4, 5, and 6.

### Parallel Opportunities

- **Phase 2**: `T003`, `T004`, `T005`, `T006`, `T007` touch distinct files and can run in parallel after `T001`/`T002`.
- **Phase 3 (US1)**: `T008`, `T010`, `T011` can run in parallel before `T009`/`T012`.
- **Phase 4 (US2)**: `T015`, `T017`, `T018` can run in parallel before `T016`/`T019`/`T020`.
- **Phase 5 (US3)**: `T022`, `T023`, `T024`, `T025`, `T027`, `T028` can run in parallel before `T026`/`T029`.
- **Phase 6 (US4)**: `T033` unit tests for matching logic can run in parallel once `T031`–`T032` contracts settle.

---

## Parallel Example: User Story 1

```text
Parallel batch 1 (ports, DTOs, exception handler):
- T008 [P] [US1] CreateCategoryUseCase, ListCategoriesUseCase, DeleteCategoryUseCase, CategoryUseCase
- T010 [P] [US1] CreateCategoryRequest, CategoryResponse
- T011 [P] [US1] CategoryExceptionHandler

Sequential wiring after batch 1:
- T009 [US1] CategoryService
- T012 [US1] CategoryController

Verification:
- T013 [P] [US1] Unit tests for Category domain, adapter, service, handler, controller
- T014 [US1] CategoryLifecycleTestCase
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 (Setup) and Phase 2 (Foundational).
2. Complete Phase 3 (User Story 1 — Category create/list/delete + in-use protection).
3. **STOP and VALIDATE**: Verify category creation, listing with filters, authorization, and deletion protection independently.

### Incremental Delivery

1. Setup + Foundational → Database schema and core domain/persistence contracts ready.
2. Add US1 (Category management) → Validate MVP.
3. Add US2 (Product `categoryId` on `/v1` optional and `/v2` required + category validation) → Validate `/v1` backward compatibility and `/v2` strictness.
4. Add US3 (Campaign CRUD + filtering + validation + `ADMIN` security) → Validate campaign lifecycle independently.
5. Add US4 (Request-time active discount resolution on product reads) → Validate end-to-end discount matching without modifying `Product` domain state.
6. Run Phase 7 (`./gradlew build`) → Confirm zero static-analysis violations and Pitest thresholds pass.

