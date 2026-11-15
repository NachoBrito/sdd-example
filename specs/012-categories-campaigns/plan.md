# Implementation Plan: Product Categories and Discount Campaigns

**Branch**: `012-categories-campaigns` | **Date**: 2026-11-14 | **Spec**: [spec.md](file:///home/nacho/github/sdd-example/specs/012-categories-campaigns/spec.md)

**Input**: Feature specification from `/specs/012-categories-campaigns/spec.md`

## Summary

Introduce `Category` and `Campaign` (with `CampaignPredicate`) domain aggregates, Liquibase migration `005-add-categories-and-campaigns.xml` (adding `categories`, `campaigns`, `campaign_predicates`, and nullable `products.category_id`), `/v1/categories` (`POST`, `GET`, `DELETE`) and `/v1/campaigns` (`POST`, `PUT`, `GET`, `DELETE`) REST endpoints, backward-compatible optional `categoryId` on `/v1/products` alongside mandatory `categoryId` on `/v2/products`, category-deletion protection when products exist in that category, and request-time active campaign evaluation attached exclusively to product HTTP read/list responses (`ProductResponse.discounts`) without modifying core backend `Product` domain entities.

## Technical Context

**Language/Version**: Java 25  
**Primary Dependencies**: Micronaut 5.2.5 (`micronaut-data-jdbc`, `micronaut-validation`, `micronaut-serde-jackson`, `micronaut-liquibase`, `micronaut-openapi`, `micronaut-security-jwt`, `micronaut-security-annotations`) — zero new build dependencies required  
**Storage**: MariaDB via Micronaut Data JDBC and Liquibase (`005-add-categories-and-campaigns.xml`)  
**Testing**: JUnit 5, REST Assured (`micronaut-test-rest-assured`), Mockito, ArchUnit (`HexagonalArchitectureTest`, `ExceptionHandlerArchitectureTest`), Pitest (80% line coverage, 90% mutation score), Checkstyle, PMD, SpotBugs  
**Target Platform**: Linux server (Netty HTTP runtime)  
**Project Type**: Single-module hexagonal web service (`es.nachobrito.sdd.example`)  
**Performance Goals**: Single batch active-campaign lookup per product list/get request (avoiding N+1 database queries); indexed `products.category_id` and `campaigns(status, start_date, end_date)` lookups  
**Constraints**: Strict hexagonal layer isolation (`HexagonalArchitectureTest`), zero build/static-analysis configuration edits, zero suppression annotations, ASD-STE100 documentation, `-Werror` compilation  
**Scale/Scope**: Single-process service; 2 new domain aggregates (`Category`, `Campaign`), 2 new controllers (`CategoryController`, `CampaignController`), 1 versioned product controller (`ProductV2Controller`), updated `ProductController` and `ProductResponse`

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Principle 1 (Minimum Viable Diff)**: Implements only the requested Category endpoints, Campaign endpoints, `Product.categoryId` (`/v1` optional, `/v2` required) with category-existence check, category in-use delete block, and request-time discounts on product get/list responses. No unrequested single-item GET endpoints for Category/Campaign and no unrequested category filter on product list.
- **Principle 2 (Ambiguity Halts Execution)**: All 3 critical decision forks (`Q1: C`, `Q2: A`, `Q3: A`) were clarified with the user and resolved in `spec.md` prior to planning.
- **Principle 3 (Do Not Distribute by Default)**: Uses the existing MariaDB database and in-process request-time evaluation; no external caches or services added.
- **Principle 4 (Optimize for Deletion)**: Reuses `ProductUseCase`/`ProductService` across `/v1/products` and `/v2/products`; avoids generic base repositories or speculative abstractions.
- **Principle 5 (Make Dependencies Explicit)**: Constructor injection only across all services, repositories, and controllers (enforced by ArchUnit `noFieldInjection`).
- **Principle 6 (Contract at the Boundary)**: Strict `@Valid` DTO validation at `/v1/categories`, `/v1/campaigns`, `/v1/products`, and `/v2/products`, including `@NotNull categoryId` on `/v2/products`; core `Product` domain record stays free of campaign/discount fields.
- **Principle 7 (Test the Transformation, Not the Plumbing)**: Pure unit tests cover `Category`, `Campaign`, `CampaignPredicate` matching, and `Product` validation without mocks; service tests mock ports; REST Assured integration tests verify full HTTP + MariaDB + JWT role behavior.
- **Principle 8 (Emit Structured Events, Derive Everything Else)**: Uses existing SLF4J/Logback structured logging and `DbQueryMetrics` interceptor on new persistence adapters without introducing new telemetry dependencies.
- **Principle 9 (Recovery Over Prevention)**: Liquibase `005-add-categories-and-campaigns.xml` uses an expand-only nullable `products.category_id` column and explicit `<rollback>` blocks for every change set.
- **Principle 12 (Commands Are Discoverable)**: Verified strictly through existing `./gradlew check` and `./gradlew build`.

## Project Structure

### Documentation (this feature)

```text
specs/012-categories-campaigns/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   ├── categories-api.yaml
│   ├── campaigns-api.yaml
│   └── products-api.yaml
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
src/main/java/es/nachobrito/sdd/example/
├── domain/model/
│   ├── Category.java
│   ├── CategoryException.java
│   ├── InvalidCategoryDataException.java
│   ├── CategoryNotFoundException.java
│   ├── DuplicateCategoryIdentifierException.java
│   ├── CategoryInUseException.java
│   ├── Campaign.java
│   ├── CampaignStatus.java
│   ├── CampaignPredicate.java
│   ├── PredicateType.java
│   ├── ActiveDiscount.java
│   ├── CampaignException.java
│   ├── InvalidCampaignDataException.java
│   ├── CampaignNotFoundException.java
│   ├── DuplicateCampaignIdentifierException.java
│   └── Product.java (extended with @Nullable UUID categoryId; no campaign/discount fields)
├── application/
│   ├── port/in/
│   │   ├── CreateCategoryUseCase.java
│   │   ├── ListCategoriesUseCase.java
│   │   ├── DeleteCategoryUseCase.java
│   │   ├── CategoryUseCase.java
│   │   ├── CreateCampaignUseCase.java
│   │   ├── UpdateCampaignUseCase.java
│   │   ├── ListCampaignsUseCase.java
│   │   ├── DeleteCampaignUseCase.java
│   │   ├── ResolveProductDiscountsUseCase.java
│   │   ├── CampaignUseCase.java
│   │   ├── CreateProductUseCase.java (updated with @Nullable UUID categoryId)
│   │   └── UpdateProductUseCase.java (updated with @Nullable UUID categoryId)
│   ├── port/out/
│   │   ├── CategoryRepositoryPort.java
│   │   ├── CampaignRepositoryPort.java
│   │   └── ProductRepositoryPort.java (extended with existsByCategoryId)
│   └── service/
│       ├── CategoryService.java
│       ├── CampaignService.java
│       └── ProductService.java (extended to validate non-null categoryId via CategoryRepositoryPort)
└── adapter/
    ├── in/rest/
    │   ├── CategoryController.java (/v1/categories)
    │   ├── CategoryExceptionHandler.java
    │   ├── CampaignController.java (/v1/campaigns)
    │   ├── CampaignExceptionHandler.java
    │   ├── ProductController.java (/v1/products — optional categoryId, discounts on get/list)
    │   ├── ProductV2Controller.java (/v2/products — required categoryId, discounts on get/list)
    │   └── dto/
    │       ├── CreateCategoryRequest.java
    │       ├── CategoryResponse.java
    │       ├── CampaignPredicateDto.java
    │       ├── CreateCampaignRequest.java
    │       ├── UpdateCampaignRequest.java
    │       ├── CampaignResponse.java
    │       ├── ProductDiscountResponse.java
    │       ├── CreateProductRequest.java (updated with optional categoryId)
    │       ├── UpdateProductRequest.java (updated with optional categoryId)
    │       ├── CreateProductV2Request.java (required @NotNull categoryId)
    │       ├── UpdateProductV2Request.java (required @NotNull categoryId)
    │       └── ProductResponse.java (updated with categoryId and discounts)
    └── out/persistence/
        ├── CategoryEntity.java
        ├── CategoryJdbcRepository.java
        ├── CategoryPersistenceAdapter.java
        ├── CampaignEntity.java
        ├── CampaignPredicateEntity.java
        ├── CampaignJdbcRepository.java
        ├── CampaignPredicateJdbcRepository.java
        ├── CampaignPersistenceAdapter.java
        ├── ProductEntity.java (updated with @Nullable categoryId)
        ├── ProductJdbcRepository.java (extended with existsByCategoryId)
        └── ProductPersistenceAdapter.java (extended with existsByCategoryId)

src/main/resources/db/changelog/
├── db.changelog-master.xml (include 005-add-categories-and-campaigns.xml)
└── 005-add-categories-and-campaigns.xml

src/test/java/es/nachobrito/sdd/example/
├── domain/model/        (CategoryTest, CampaignTest, CampaignPredicateTest, ProductTest updates)
├── application/service/ (CategoryServiceTest, CampaignServiceTest, ProductServiceCommandTest updates)
└── adapter/
    ├── in/rest/         (CategoryLifecycleTestCase, CampaignLifecycleTestCase, ProductV2LifecycleTestCase, ProductDiscountIntegrationTestCase, controller/exception/dto unit tests)
    └── out/persistence/ (CategoryPersistenceAdapterTest, CampaignPersistenceAdapterTest, ProductPersistenceAdapter updates)
```

**Structure Decision**: Follow the existing single-module hexagonal layout under `es.nachobrito.sdd.example` so `HexagonalArchitectureTest` and `ExceptionHandlerArchitectureTest` pass with zero configuration modifications.

## Post-Design Constitution Check

- **All Phase 1 artifacts** (`data-model.md`, `contracts/categories-api.yaml`, `contracts/campaigns-api.yaml`, `contracts/products-api.yaml`, `quickstart.md`) satisfy Principles 1–12.
- **No build or tool configuration changes required**: Zero Gradle, Checkstyle, PMD, SpotBugs, ArchUnit, or Pitest rules need modification.
- **Ready for `/speckit-tasks`**.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No constitution violations. All design choices reuse existing Micronaut libraries, hexagonal ports/adapters, and single-process MariaDB persistence.
