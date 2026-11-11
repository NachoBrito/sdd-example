# Implementation Plan: Order Management

**Branch**: `004-order-management` | **Date**: 2026-11-10 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/004-order-management/spec.md`

## Summary

Implement support for the `Order` aggregate and a complete REST API using Hexagonal Architecture and Domain-Driven Design (DDD). Persistence uses Micronaut Data JDBC, MariaDB, and Liquibase.

Key capabilities:
- Order creation with client-provided UUID identifiers (`POST /v1/orders`).
- Server calculation and enforcement of line totals and order totals.
- Order retrieval by identifier (`GET /v1/orders/{id}`).
- Order attribute and line updates (`PUT /v1/orders/{id}`).
- Conditional hard delete (`DELETE /v1/orders/{id}`) permitted only for `PENDING` and `CANCELLED` orders.
- Order querying with status, customer email, date range filtering, and dynamic sorting (`GET /v1/orders`).
- Strict multi-layer validation across controller, application, domain, and persistence layers.

---

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Micronaut 5.2.5 (Micronaut Data JDBC, Micronaut Validation, Micronaut Serde Jackson, Micronaut OpenAPI, HikariCP)

**Storage**: MariaDB with Liquibase migrations (`src/main/resources/db/changelog/`)

**Testing**: JUnit 5, REST Assured, Mockito, ArchUnit, Pitest

**Target Platform**: Linux server / Docker container (Eclipse Temurin JRE 25)

**Project Type**: REST web service / backend application

**Performance Goals**: < 100ms p95 for single order operations, < 250ms for filtered queries

**Constraints**:
- Hexagonal Architecture and ArchUnit rule compliance.
- No JPA, Hibernate, Jackson Databind, or Lombok dependencies.
- Zero compiler warnings (`-Werror`) and zero static analysis warnings (Checkstyle, PMD, SpotBugs).
- Minimum 80% line coverage and 90% mutation score threshold via Pitest.
- Client-side ID generation (required UUID parameter).
- Server-enforced total calculations (`lineTotalInCents = quantity * unitPriceInCents`, `totalAmountInCents = sum(lineTotals)`).
- Deletion allowed only for `PENDING` or `CANCELLED` orders; rejection with 409 for other statuses.

**Scale/Scope**: 1 aggregate root (`Order`), 3 value objects (`DeliveryAddress`, `InvoiceAddress`, `OrderLine`), 5 REST endpoints, 2 database tables (`orders`, `order_lines`).

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Evaluation | Justification |
|-----------|------------|---------------|
| **1. Minimum Viable Diff** | PASS | Diff strictly contains Order domain models, use cases, service, controller, persistence adapter, Liquibase migration, and tests. |
| **2. Ambiguity Halts Execution** | PASS | All critical forks resolved via clarification: client-provided UUID, server-enforced total calculations, and conditional hard delete for non-fulfilled orders. |
| **3. Do Not Distribute by Default** | PASS | Single-process web service using existing MariaDB datasource. No background queues or external caches introduced. |
| **4. Optimize for Deletion, Not Extension** | PASS | Self-contained hexagonal components without generic abstractions or premature inheritance hierarchies. |
| **5. Make Dependencies Explicit** | PASS | Explicit constructor injection throughout controllers, services, and adapters. No global state or hidden singletons. |
| **6. Contract at the Boundary** | PASS | Explicit OpenAPI 3 contract in `contracts/orders-api.yaml`. Boundary request validation via Micronaut Validation. |
| **7. Test the Transformation** | PASS | Pure unit tests for `Order` invariants and calculations; integration tests (`OrderControllerIT`) with REST Assured verifying boundary contracts against real MariaDB test container. |
| **8. Emit Structured Events** | PASS | Structured JSON logging via existing Logback primitives. No unstructured `System.out` calls. |
| **9. Recovery Over Prevention** | PASS | Versioned Liquibase migration (`003-create-orders-tables.xml`) with rollback support. |
| **10. Attention Is Finite** | PASS | Actionable error responses with HTTP problem details for client errors (400, 404, 409). |
| **11. Value Is Realized at the User** | PASS | Automated integration tests and runnable validation scenarios in `quickstart.md`. |
| **12. Commands Are Discoverable** | PASS | Standard Gradle tasks: `./gradlew test`, `./gradlew check`, `./gradlew build`. |

---

## Project Structure

### Documentation (this feature)

```text
specs/004-order-management/
├── plan.md              # This plan
├── research.md          # Technical decisions and trade-offs
├── data-model.md        # Entities, validation rules, schema
├── quickstart.md        # Runnable verification guide
├── contracts/           # API contracts
│   └── orders-api.yaml
└── checklists/
    └── requirements.md  # Requirements quality checklist
```

### Source Code Layout

```text
src/main/java/es/nachobrito/sdd/example/
├── domain/model/
│   ├── Order.java
│   ├── OrderStatus.java
│   ├── DeliveryAddress.java
│   ├── InvoiceAddress.java
│   ├── OrderLine.java
│   ├── OrderNotFoundException.java
│   ├── InvalidOrderDataException.java
│   └── OrderDeletionConflictException.java
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── CreateOrderUseCase.java
│   │   │   ├── GetOrderUseCase.java
│   │   │   ├── UpdateOrderUseCase.java
│   │   │   ├── DeleteOrderUseCase.java
│   │   │   ├── ListOrdersUseCase.java
│   │   │   └── OrderUseCase.java
│   │   └── out/
│   │       └── OrderRepositoryPort.java
│   └── service/
│       └── OrderService.java
└── adapter/
    ├── in/rest/
    │   ├── OrderController.java
    │   ├── OrderExceptionHandler.java
    │   └── dto/
    │       ├── CreateOrderRequest.java
    │       ├── UpdateOrderRequest.java
    │       ├── OrderResponse.java
    │       ├── DeliveryAddressDto.java
    │       ├── InvoiceAddressDto.java
    │       ├── OrderLineRequest.java
    │       └── OrderLineResponse.java
    └── out/persistence/
        ├── OrderEntity.java
        ├── OrderLineEntity.java
        ├── OrderJdbcRepository.java
        ├── OrderLineJdbcRepository.java
        └── OrderPersistenceAdapter.java

src/main/resources/
└── db/changelog/
    ├── db.changelog-master.xml
    └── 003-create-orders-tables.xml

src/test/java/es/nachobrito/sdd/example/
├── domain/model/
│   └── OrderTest.java
├── application/service/
│   └── OrderServiceTest.java
└── adapter/in/rest/
    └── OrderControllerIT.java
```

**Structure Decision**: Adheres strictly to the hexagonal architecture layers specified in `docs/project-setup.md` and enforced by `HexagonalArchitectureTest.java`.

---

## Complexity Tracking

*No constitution violations. No additional complexity introduced.*

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
