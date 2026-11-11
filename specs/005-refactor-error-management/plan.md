# Implementation Plan: Refactor Controller Error Management

**Branch**: `005-refactor-error-management` | **Date**: 2026-11-11 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/005-refactor-error-management/spec.md`

## Summary

Refactor REST controller error handling to replace error handling controllers (`ProductExceptionHandler`, `OrderExceptionHandler`) with Micronaut `ExceptionHandler` classes.

Key capabilities:
- Remove `@Controller("/v1/errors")` and `@Controller("/v1/order-errors")` routes from the API.
- Introduce domain base exceptions (`ProductException`, `OrderException`) to group exceptions by bounded context.
- Implement `ExceptionHandler<ProductException, HttpResponse<ErrorResponse>>` in `ProductExceptionHandler`.
- Implement `ExceptionHandler<OrderException, HttpResponse<ErrorResponse>>` in `OrderExceptionHandler`.
- Enforce exactly one exception handler per controller for current and future features.
- Preserve 100 percent backward compatibility for all existing HTTP status codes (400, 404, 409) and error payloads.

---

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Micronaut 5.2.5 (`micronaut-http`, `micronaut-http-server`, `micronaut-validation`, `micronaut-serde-jackson`)

**Storage**: MariaDB with Liquibase migrations (No database changes required for this refactoring)

**Testing**: JUnit 5, REST Assured, ArchUnit, Pitest

**Target Platform**: Linux server / Docker container (Eclipse Temurin JRE 25)

**Project Type**: REST web service / backend application

**Performance Goals**: < 10ms for exception handling translation

**Constraints**:
- Hexagonal Architecture and ArchUnit rule compliance.
- No JPA, Hibernate, Jackson Databind, or Lombok dependencies.
- Zero compiler warnings (`-Werror`) and zero static analysis warnings (Checkstyle, PMD, SpotBugs).
- Minimum 80% line coverage and 90% mutation score threshold via Pitest.
- One exception handler class per controller.
- All existing error response payload structures and HTTP status codes must remain unchanged.

**Scale/Scope**: 2 controller exception handlers (`ProductExceptionHandler`, `OrderExceptionHandler`), 2 domain base exceptions (`ProductException`, `OrderException`), 8 domain exception subclasses.

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Evaluation | Justification |
|-----------|------------|---------------|
| **1. Minimum Viable Diff** | PASS | Diff strictly modifies error handler classes, domain exception hierarchies, and their tests. |
| **2. Ambiguity Halts Execution** | PASS | All requirements and architectural boundaries are fully specified. |
| **3. Do Not Distribute by Default** | PASS | In-process exception handling inside the existing web service. |
| **4. Optimize for Deletion, Not Extension** | PASS | Handlers are self-contained classes without speculative abstractions. |
| **5. Make Dependencies Explicit** | PASS | Handlers are standard `@Singleton` beans with explicit dependencies. |
| **6. Contract at the Boundary** | PASS | External HTTP contracts remain identical; artificial controller routes removed. |
| **7. Test the Transformation** | PASS | Isolated unit tests verify exception mapping; integration tests verify HTTP response codes. |
| **8. Emit Structured Events** | PASS | Existing structured logging primitives retained; no unstructured stdout. |
| **9. Recovery Over Prevention** | PASS | No database schema modifications; changes are easily revertible in minutes. |
| **10. Attention Is Finite** | PASS | Clean error responses with actionable problem messages for API clients. |
| **11. Value Is Realized at the User** | PASS | Verified through automated unit tests, integration tests, and OpenAPI route cleanliness. |
| **12. Commands Are Discoverable** | PASS | Standard Gradle build and test commands: `./gradlew test` and `./gradlew build`. |

---

## Project Structure

### Documentation (this feature)

```text
specs/005-refactor-error-management/
├── plan.md              # This plan
├── research.md          # Technical decisions and trade-offs
├── data-model.md        # Entities, exception models, handler mappings
├── quickstart.md        # Runnable verification guide
├── contracts/           # API error contract definitions
│   └── error-api.yaml
└── checklists/
    └── requirements.md  # Requirements quality checklist
```

### Source Code Layout

```text
src/main/java/es/nachobrito/sdd/example/
├── domain/model/
│   ├── ProductException.java                  # Abstract base exception for product domain
│   ├── ProductNotFoundException.java          # Extends ProductException
│   ├── DuplicateIdentifierException.java      # Extends ProductException
│   ├── DuplicateSkuException.java             # Extends ProductException
│   ├── InvalidProductDataException.java       # Extends ProductException
│   ├── OrderException.java                    # Abstract base exception for order domain
│   ├── OrderNotFoundException.java            # Extends OrderException
│   ├── DuplicateOrderIdentifierException.java # Extends OrderException
│   ├── OrderDeletionConflictException.java    # Extends OrderException
│   └── InvalidOrderDataException.java         # Extends OrderException
└── adapter/in/rest/
    ├── ProductExceptionHandler.java           # Implements ExceptionHandler<ProductException, ...>
    ├── OrderExceptionHandler.java             # Implements ExceptionHandler<OrderException, ...>
    └── dto/
        └── ErrorResponse.java                 # Standard error response DTO

src/test/java/es/nachobrito/sdd/example/
└── adapter/in/rest/
    ├── ProductExceptionHandlerTest.java       # Unit tests for ProductExceptionHandler
    └── OrderExceptionHandlerTest.java         # Unit tests for OrderExceptionHandler
```

**Structure Decision**: The refactoring modifies files in `domain/model` and `adapter/in/rest`. Domain exceptions reside in `domain/model` without framework imports. Handlers reside in `adapter/in/rest` as inbound REST adapters.

---

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

*No constitution violations. All checks passed.*
