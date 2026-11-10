# Implementation Plan: Product Management

**Branch**: `003-product-management` | **Date**: 2026-11-10 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/003-product-management/spec.md`

## Summary

Implement support for the `Product` aggregate and a complete REST API with client-side UUID generation and integer cent price validation. The solution follows Hexagonal Architecture and Domain-Driven Design (DDD) using Micronaut Data JDBC, MariaDB, and Liquibase.

Key capabilities:
- Product creation with client-provided UUID identifiers (`POST /v1/products`).
- Retrieval by identifier (`GET /v1/products/{id}`).
- Attribute updates with timestamp refresh (`PUT /v1/products/{id}`).
- Permanent deletion (`DELETE /v1/products/{id}`).
- Catalog querying with SKU, name regular expression, status filtering, and sorting (`GET /v1/products`).
- Strict validation across controller, application/service, domain, and persistence layers.

---

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Micronaut 5.2.5 (Micronaut Data JDBC, Micronaut Validation, Micronaut Serde Jackson, Micronaut OpenAPI, HikariCP)

**Storage**: MariaDB with Liquibase migrations (`src/main/resources/db/changelog/`)

**Testing**: JUnit 5, REST Assured, Mockito, ArchUnit, Pitest

**Target Platform**: Linux server / Docker container (Eclipse Temurin JRE 25)

**Project Type**: REST web service / backend application

**Performance Goals**: Fast response times (< 100ms p95 for single product operations, < 250ms for filtered queries)

**Constraints**:
- Hexagonal Architecture and ArchUnit rule compliance.
- No JPA, Hibernate, Jackson Databind, or Lombok dependencies.
- Zero compiler warnings (`-Werror`) and zero static analysis warnings (Checkstyle, PMD, SpotBugs).
- Minimum 80% line coverage and 90% mutation score threshold via Pitest.
- Client-side ID generation (required UUID parameter).
- Prices expressed in integer cents (`priceInCents >= 0`).

**Scale/Scope**: 1 aggregate root (`Product`), 5 REST endpoints, 1 database table (`products`).

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Evaluation | Justification |
|-----------|------------|---------------|
| **1. Minimum Viable Diff** | PASS | Diff strictly contains the Product domain model, ports, service, controller, persistence adapter, Liquibase migration, and tests. No speculative configuration. |
| **2. Ambiguity Halts Execution** | PASS | All requirements and boundary conditions clarified upfront: hard delete semantics, non-negative price validation (`>= 0`), and unique SKU scope. |
| **3. Do Not Distribute by Default** | PASS | Single-process web service utilizing existing MariaDB datasource. No external caches, background brokers, or extra services introduced. |
| **4. Optimize for Deletion, Not Extension** | PASS | Focused hexagonal modules without generic base classes, speculative factories, or premature inheritance hierarchies. |
| **5. Make Dependencies Explicit** | PASS | Explicit constructor injection throughout controllers, services, and adapters. No global state or hidden singletons. |
| **6. Contract at the Boundary** | PASS | Explicit OpenAPI 3 schema in `contracts/products-api.yaml`. Boundary request validation via Micronaut Validation before passing to use cases. |
| **7. Test the Transformation** | PASS | Pure unit tests for `Product` domain invariants; integration tests (`ProductControllerIT`) with REST Assured verifying boundary contracts against real MariaDB test container. |
| **8. Emit Structured Events** | PASS | Existing structured JSON logging via Logback. No unstructured `System.out` or string concatenation. |
| **9. Recovery Over Prevention** | PASS | Versioned Liquibase migration (`002-create-products-table.xml`) with rollback support. |
| **10. Attention Is Finite** | PASS | Clean error responses with actionable problem details for client errors (400, 404, 409). |
| **11. Value Is Realized at the User** | PASS | Automated end-to-end integration tests and runnable validation scenarios in `quickstart.md`. |
| **12. Commands Are Discoverable** | PASS | Standard Gradle tasks: `./gradlew test`, `./gradlew check`, `./gradlew build`. |

---

## Project Structure

### Documentation (this feature)

```text
specs/003-product-management/
├── plan.md              # This plan
├── research.md          # Technical decisions and trade-offs
├── data-model.md        # Entities, validation rules, schema
├── quickstart.md        # Runnable verification guide
├── contracts/           # API contracts
│   └── products-api.yaml
└── checklists/
    └── requirements.md  # Requirements quality checklist
```

### Source Code Layout

```text
src/main/java/es/nachobrito/sdd/example/
├── domain/model/
│   ├── Product.java
│   └── ProductStatus.java
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── CreateProductUseCase.java
│   │   │   ├── GetProductUseCase.java
│   │   │   ├── UpdateProductUseCase.java
│   │   │   ├── DeleteProductUseCase.java
│   │   │   └── ListProductsUseCase.java
│   │   └── out/
│   │       └── ProductRepositoryPort.java
│   └── service/
│       └── ProductService.java
└── adapter/
    ├── in/rest/
    │   ├── ProductController.java
    │   ├── dto/
    │   │   ├── CreateProductRequest.java
    │   │   ├── UpdateProductRequest.java
    │   │   ├── ProductResponse.java
    │   │   └── ErrorResponse.java
    │   └── exception/
    │       └── ProductExceptionHandler.java
    └── out/persistence/
        ├── ProductEntity.java
        ├── ProductJdbcRepository.java
        └── ProductPersistenceAdapter.java

src/main/resources/
└── db/changelog/
    ├── db.changelog-master.xml
    └── 002-create-products-table.xml

src/test/java/es/nachobrito/sdd/example/
├── domain/model/
│   └── ProductTest.java
├── application/service/
│   └── ProductServiceTest.java
└── adapter/in/rest/
    └── ProductControllerIT.java
```

**Structure Decision**: The source structure adheres strictly to the hexagonal architecture layers specified in `docs/project-setup.md` and enforced by `HexagonalArchitectureTest.java`.

---

## Complexity Tracking

*No constitution violations. No additional complexity introduced.*

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
