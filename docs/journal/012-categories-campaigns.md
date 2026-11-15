# Feature Journal: Product Categories and Promotional Campaigns

This document records the development process for the product categories and promotional campaigns feature.
The feature adds category management, product category assignment with a version 2 API, promotional campaigns, and request-time product discounts.

---

## 1. Implementation Process

The implementation followed the Spec-Driven Development workflow.

First, the specification in `specs/012-categories-campaigns/spec.md` defined four user stories:
- Manage product categories with admin writes and catalog or customer reads (P1).
- Assign optional categories in `/v1/products` and required categories in `/v2/products` (P2).
- Manage promotional discount campaigns and eligibility predicates with admin authorization (P3).
- Resolve active discounts on product read endpoints at request time without storing discounts on products (P4).

During clarification, the user answered three design questions recorded in Section 2.

Next, the agent created the implementation plan, OpenAPI contracts, data model, quickstart guide, and task list in `specs/012-categories-campaigns/tasks.md`.
Then the agent implemented the feature across all hexagonal layers:
- Created Liquibase migration `005-add-categories-and-campaigns.xml` and included it in `db.changelog-master.xml`.
- Added pure domain types (`Category`, `Campaign`, `CampaignPredicate`, `CampaignStatus`, `PredicateType`, `ActiveDiscount`) and updated `Product` with an optional `categoryId`.
- Added inbound ports, outbound ports, `CategoryService`, and `CampaignService`, and updated `ProductService` to verify category existence.
- Added JDBC entities, repositories, and persistence adapters for categories, campaigns, and predicates.
- Added `CategoryController`, `CategoryExceptionHandler`, `CampaignController`, `CampaignExceptionHandler`, and `ProductV2Controller`, and updated `ProductController`.
- Updated `docs/running/requests/products.http` and added `categories.http` and `campaigns.http`.

---

## 2. Specification Clarifications

Before planning, the agent asked three clarification questions with proposed options and recorded the user selections.

### Question 1: Product `categoryId` Requirement and Backward Compatibility
- **Question**: Is `categoryId` optional or required on `Product`, and how should the API preserve backward compatibility?
- **Proposed Options**:
  - **Option A** (Optional `nullable` on `/v1/products`): Keeps backward compatibility for `/v1/products` callers and existing products. Validates non-null values.
  - **Option B** (Required on `/v1/products` with backfill): Seeds a default category in Liquibase. Breaks callers that omit `categoryId`.
  - **Option C** (Optional on `/v1/products`, required on `/v2/products`): Keeps `/v1/products` backward compatible. Requires `categoryId` on the new `/v2/products` endpoints.
  - **Custom**: Specifies custom optionality, migration, and versioning rules.
- **Selected Option**: **Option C** (`Q1: C`).

### Question 2: Campaign Predicate Combination and Comparison Rules
- **Question**: How do multiple predicates combine, how does an empty list behave, and are price comparisons strict or inclusive?
- **Proposed Options**:
  - **Option A** (Logical `AND`, empty list matches all, strict `<` and `>`): Supports price ranges and combined rules. Matches uncategorized products on `NOT_IN_CATEGORY` only.
  - **Option B** (Logical `OR`, empty list matches none or fails): Targets multiple categories together. Prevents combined `AND` constraints across category and price.
  - **Option C** (Logical `AND`, non-empty list required, strict `<` and `>`): Matches Option A semantics. Rejects campaigns that supply an empty `predicates` list.
  - **Custom**: Specifies custom boolean combination, empty-list handling, and boundary comparisons.
- **Selected Option**: **Option A** (`Q2: A`).

### Question 3: Active Discounts Representation and Multiple Active Campaigns
- **Question**: Which fields belong in the active discount payload, and how does the response handle multiple active campaigns?
- **Proposed Options**:
  - **Option A** (List of matching active discounts only): Exposes `campaignId`, `campaignName`, and `discountPercent` (or an empty list). Keeps `priceInCents` unmodified.
  - **Option B** (Matching list plus `discountedPriceInCents` from highest discount): Exposes all matching active campaigns. Computes one effective price from the highest `discountPercent`.
  - **Option C** (Matching list plus stacked `discountedPriceInCents`): Exposes all matching active campaigns. Stacks discounts additively (capped at 100 percent) or multiplicatively.
  - **Custom**: Specifies custom response fields and multi-campaign calculation rules.
- **Selected Option**: **Option A** (`Q3: A`).

---

## 3. Bugs Found and Verification Blocks

The team encountered two issues during verification.

### Stub Case Mismatch in `CampaignPersistenceAdapterTest`
When expanding persistence unit tests, a mock stub for `CampaignJdbcRepository.search()` expected lowercase `"desc"` while passing filter `"Desc"`.
`CampaignPersistenceAdapter.normalizeFilter()` trims whitespace but preserves case because the SQL query handles case folding with `LOWER(...)`.
Mockito reported a `PotentialStubbingProblem` failure, and changing the stub argument to `"Desc"` resolved the failure.

### Terminal Sandbox Block on Docker Test Resources
All 13 `*TestCase` integration suites failed during `./gradlew build` at `initializationError` because Testcontainers could not access `/var/run/docker.sock`.
End-to-end database and RabbitMQ verification requires running `./gradlew build` outside the sandbox with Docker available.

---

## 4. Static Code Analysis

The agent ran Checkstyle, PMD, SpotBugs, and ArchUnit without modifying thresholds or adding suppressions.
The agent fixed all findings directly in the production and test code:
- **Checkstyle**:
  - Replaced the constructor `switch` statement in `CampaignPredicate` with an `if/else` check (`MissingSwitchDefault`).
  - Shortened the `list` operation description in `CampaignController` to stay within 100 characters (`LineLength`).
  - Moved local variable declarations next to their first usage in `ProductV2LifecycleTestCase`, `CampaignMatchingTest`, and `ProductDiscountIntegrationTestCase` (`VariableDeclarationUsageDistance`).
- **PMD**:
  - Added `Throwable` cause constructors to `CampaignException` and `InvalidCampaignDataException`, and passed caught causes in `CampaignPredicate` (`PreserveStackTrace`).
  - Replaced `LinkedHashMap` with `ConcurrentHashMap` in `CampaignService.resolveActiveDiscounts()` (`UseConcurrentHashMap`).
  - Qualified inner command and query types on `CampaignUseCase` and `ProductUseCase`, and removed extra local imports in `CampaignController`, `ProductController`, and `ProductV2Controller` (`ExcessiveImports`).
  - Added JUnit `assertNotNull` checks on login tokens in `CampaignLifecycleTestCase` and `ProductDiscountIntegrationTestCase` (`UnitTestShouldIncludeAssert`).
- **SpotBugs**:
  - Added compact constructors and `Collections.unmodifiableList` accessors to `CreateCampaignCommand` and `UpdateCampaignCommand` (`EI_EXPOSE_REP`, `EI_EXPOSE_REP2`).
  - Stored nullable return values in local variables before null checks in `CampaignPredicate.matches()` and `CampaignService.listCampaigns()` (`NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE`).
  - Wrapped `response.body()` with `Objects.requireNonNull` before assertions in `CampaignControllerTest` (`NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE`).

---

## 5. Test Coverage and Mutation Testing

The repository requires at least 80 percent line coverage and a 90 percent Pitest mutation score.
The agent wrote dedicated unit tests for every new domain type, persistence adapter, application service, exception handler, DTO, and controller.
The agent also added integration test suites (`CategoryLifecycleTestCase`, `ProductV2LifecycleTestCase`, `CampaignLifecycleTestCase`, and `ProductDiscountIntegrationTestCase`).

To strengthen coverage of defensive code paths added for static analysis, the agent added targeted unit assertions:
- Verified non-null root causes in `CampaignPredicateTest`.
- Verified null-list handling and immutability on `CreateCampaignCommand` and `UpdateCampaignCommand` in `CampaignServiceTest`.
- Verified predicate re-insertion counts on `update` and non-empty `findAll` mapping with a `null` status filter in `CampaignPersistenceAdapterTest`.

After the final edits, all non-Docker verification gates passed:
- `checkstyleMain`, `checkstyleTest`, `pmdMain`, `pmdTest`, `spotbugsMain`, and `spotbugsTest` passed with zero errors.
- Unit and ArchUnit tests (`./gradlew test --tests "*Test"`) passed with 395 tests across 77 suites and 0 failures.
- Pitest passed with 98 percent line coverage (`1802/1830`) and a 92 percent mutation score (`825/897` mutations killed).
