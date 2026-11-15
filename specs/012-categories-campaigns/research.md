# Research: Product Categories and Discount Campaigns

**Feature**: `012-categories-campaigns` | **Date**: 2026-11-14

## 1. Product Category Validation Across Hexagonal Boundaries

### Decision
- Extend `Product`, `CreateProductCommand`, `UpdateProductCommand`, and `ProductEntity` with `@Nullable UUID categoryId`.
- Enforce `categoryId` existence in `ProductService.createProduct` and `ProductService.updateProduct` via `CategoryRepositoryPort.existsById(categoryId)` when `categoryId != null`, throwing `InvalidProductDataException` (HTTP `400 Bad Request`) when absent.
- Expose only `POST /v1/categories`, `GET /v1/categories`, and `DELETE /v1/categories/{id}` (no `GET /v1/categories/{id}` endpoint); external callers, HTTP scripts, and tests discover and validate categories through `GET /v1/categories`.
- Enforce category deletion guard in `CategoryService.deleteCategory` via `ProductRepositoryPort.existsByCategoryId(id)`, throwing `CategoryInUseException` (HTTP `409 Conflict`) when any product references the category.

### Rationale
- `HexagonalArchitectureTest` forbids `application` services from calling HTTP endpoints or depending on `adapter` classes, and forbids package cycles.
- Having both `ProductService` and `CategoryService` depend only on `application.port.out` interfaces (`CategoryRepositoryPort`, `ProductRepositoryPort`) matches `OrderService -> ProductRepositoryPort` and creates zero dependency cycles.
- Omitting `GET /v1/categories/{id}` satisfies Principle 1 (Minimum Viable Diff) while allowing callers to query categories via `GET /v1/categories`.

### Alternatives Considered
- **Calling `ListCategoriesUseCase` from `ProductService`**: Loads all categories into memory on every product write and introduces coupling between application services (and AOP `@AuthorizePolicy` indirection). `CategoryRepositoryPort.existsById` executes an O(1) indexed lookup.
- **Database foreign-key failure only**: Produces an unhandled SQL integrity exception (HTTP `500`) instead of clean `400 Bad Request` or `409 Conflict` responses. Adding both application checks and a nullable DB foreign key (`ON DELETE RESTRICT`) provides clean error codes and strict DB integrity.

## 2. Backward-Compatible `/v1/products` and Mandatory-Category `/v2/products`

### Decision
- Keep `categoryId` optional (`@Nullable UUID categoryId`) on `CreateProductRequest` and `UpdateProductRequest` under `/v1/products` (`ProductController`).
- Add `ProductV2Controller` at `@Controller("/v2/products")` with `CreateProductV2Request` and `UpdateProductV2Request` declaring `@NotNull UUID categoryId` at the HTTP boundary.
- Share the same `ProductUseCase` inbound port, `ProductService`, and `ProductExceptionHandler` across v1 and v2 controllers.
- Return `ProductResponse` (containing `@Nullable UUID categoryId` and `List<ProductDiscountResponse> discounts`) from both `/v1/products` and `/v2/products`.

### Rationale
- Satisfies `AGENTS.md` Section 5 ("REST: use versioned paths (`/v1/...`, `/v2/...`). Always keep backward compatibility") and the clarified `Q1: C` decision.
- Preserves existing `/v1/products` integration tests, `.http` scripts, and k6 load-testing scripts without breaking changes, while giving v2 clients strict `categoryId` validation at the boundary (Constitution Principle 6).

### Alternatives Considered
- **Separate `ProductV2Service` or separate `ProductV2` domain entity**: Violates Constitution Principle 4 (Optimize for Deletion) by duplicating domain logic and persistence for identical underlying product behavior.

## 3. Request-Time Active Campaign Evaluation Without Mutating Domain Entities

### Decision
- Keep `Product` domain record completely free of campaign or discount fields.
- Model pure business matching logic in `domain.model`:
  - `Campaign.isActiveAt(Instant now)` checks `status == CampaignStatus.ACTIVE`, `!now.isBefore(startDate)`, and `!now.isAfter(endDate)`.
  - `Campaign.matches(Product product)` evaluates `predicates` with logical `AND` (`predicates.stream().allMatch(p -> p.matches(product))`), returning `true` when `predicates` is empty.
  - `CampaignPredicate.matches(Product product)` handles `IN_CATEGORY`, `NOT_IN_CATEGORY`, `IS_CHEAPER_THAN`, and `IS_MORE_EXPENSIVE_THAN`.
- Introduce an inbound query port `ResolveProductDiscountsUseCase` (in `application.port.in`) implemented by `CampaignService` (using `CampaignRepositoryPort.findActiveAt(Instant)` and `Clock.systemUTC()`).
- Call `ResolveProductDiscountsUseCase` from `ProductController` and `ProductV2Controller` on `getById` (single product) and `list` (batch resolution across returned products with one DB query for active campaigns), mapping results into `ProductResponse.discounts`.

### Rationale
- Honors the explicit requirement: "Note we only change the HTTP resource, not the backend domain entities. The active campaigns are resolved at request time."
- Satisfies `HexagonalArchitectureTest` (`adapter.in.rest` depends only on `application.port.in` and `domain.model`, never on `adapter.out.persistence`).
- Batching discount resolution in `resolveActiveDiscounts(List<Product>)` prevents N+1 queries when listing products.
- Pure matching methods in `Campaign` and `CampaignPredicate` allow fast, mock-free unit and Pitest mutation testing (Constitution Principle 7).

### Alternatives Considered
- **Returning an enriched `ProductWithDiscounts` wrapper from `ProductService`**: Couples `ProductService` to `CampaignRepositoryPort` and blurs the requirement that only the HTTP resource changes.
- **SQL-level predicate evaluation**: Complex and error-prone across arbitrary dynamic predicate lists; fetching active campaigns in-window via an indexed query and evaluating in domain Java keeps the rule engine simple, testable, and under 100 lines.

## 4. Category and Campaign Substring Filtering and Date-Range Queries

### Decision
- For category and campaign optional `name` and `description` filters, use MariaDB `LOCATE(LOWER(:filter), LOWER(col)) > 0` when the filter is non-null and non-blank.
- For campaign date range filters (`startDate`, `endDate`), validate `startDate <= endDate` in `CampaignService` (throwing `InvalidCampaignDataException`) and match campaigns whose `[start_date, end_date]` window overlaps `[filterStart, filterEnd]`:
  `(:startDate IS NULL OR c.end_date >= :startDate) AND (:endDate IS NULL OR c.start_date <= :endDate)`.
- Persist `campaign_predicates` in a normalized child table with foreign key `campaign_id -> campaigns(id) ON DELETE CASCADE`, mirroring `order_lines` and `user_roles`.

### Rationale
- `LOCATE(LOWER(...), LOWER(...)) > 0` performs safe case-insensitive substring matching without `LIKE` wildcard (`%`, `_`) escaping bugs.
- Interval overlap matching for campaign dates aligns with schedule querying when searching for campaigns active within a date window.
- Normalized `campaign_predicates` avoids JSON column parsing in MariaDB SQL and matches existing `OrderPersistenceAdapter` patterns.

## 5. Authorization and Exception Handling

### Decision
- Secure `CategoryController` endpoints with `@Secured({"ADMIN"})` on `POST` and `DELETE`, and `@Secured({"CUSTOMER", "CATALOG"})` on `GET /v1/categories`.
- Secure all `CampaignController` endpoints with `@Secured({"ADMIN"})`.
- Add `CategoryException` hierarchy + `CategoryExceptionHandler`, and `CampaignException` hierarchy + `CampaignExceptionHandler`, following `docs/project-setup.md` Section 4 and `ExceptionHandlerArchitectureTest`.

### Rationale
- Micronaut Security `@Secured` enforces role checks declaratively via AOP before controller execution, returning `401 Unauthorized` when unauthenticated and `403 Forbidden` when lacking the required role.
- Dedicated exception handlers per domain module keep error translation clean (`400 Bad Request`, `404 Not Found`, `409 Conflict`).

