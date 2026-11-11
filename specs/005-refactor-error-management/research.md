# Research: Refactor Controller Error Management

**Feature**: `005-refactor-error-management`

**Date**: 2026-11-11

## 1. Error Handling Mechanism

### Decision
Replace error handling controllers with Micronaut `ExceptionHandler` classes:
- Remove `@Controller` and `@Error` annotations from `ProductExceptionHandler` and `OrderExceptionHandler`.
- Annotate the handler classes with `@Singleton` and `@Produces`.
- Implement `io.micronaut.http.server.exceptions.ExceptionHandler<T, HttpResponse<ErrorResponse>>`.

### Rationale
- Micronaut `ExceptionHandler` classes handle exceptions without registering REST endpoints.
- This change removes `/v1/errors` and `/v1/order-errors` from the routing table and OpenAPI documentation.
- The change satisfies the explicit user requirement to replace error handling controllers.

### Alternatives Considered
- Keep `@Controller` with `@Error(global = true)`: Rejected because it exposes artificial routes to external clients.
- Add `@Error` methods directly to each controller: Rejected because it increases coupling and class complexity in the controllers.
- Create one `ExceptionHandler` per individual exception: Rejected because the user required one handler per controller.

---

## 2. One Exception Handler Per Controller Pattern

### Decision
Assign exactly one `ExceptionHandler` class to each resource controller:
- `ProductExceptionHandler` handles exceptions originating from `ProductController`.
- `OrderExceptionHandler` handles exceptions originating from `OrderController`.
- Maintain this 1:1 relationship for future controllers.

### Rationale
- Directly satisfies the user requirement: "Use one exception handler per controller, keep this behavior for future features."
- Prevents class explosion by avoiding a separate handler class for every domain exception.
- Keeps error handling organized by feature bounded context.

### Alternatives Considered
- Single global exception handler for the entire application: Rejected because the user explicitly mandated one handler per controller.
- Multiple handlers per controller: Rejected because it multiplies the number of classes and violates the design rule.

---

## 3. Domain Exception Hierarchy

### Decision
Introduce a domain base exception for each bounded context:
- `ProductException extends RuntimeException` in `domain.model` for the product context.
- `OrderException extends RuntimeException` in `domain.model` for the order context.
- Domain exceptions extend their respective base exception:
  - `ProductNotFoundException`, `DuplicateIdentifierException`, `DuplicateSkuException`, and `InvalidProductDataException` extend `ProductException`.
  - `OrderNotFoundException`, `DuplicateOrderIdentifierException`, `OrderDeletionConflictException`, and `InvalidOrderDataException` extend `OrderException`.
- `ProductExceptionHandler` implements `ExceptionHandler<ProductException, HttpResponse<ErrorResponse>>`.
- `OrderExceptionHandler` implements `ExceptionHandler<OrderException, HttpResponse<ErrorResponse>>`.

### Rationale
- Micronaut resolves `ExceptionHandler` beans using the closest type argument matching the thrown exception.
- A context-specific base exception guarantees that product exceptions route to `ProductExceptionHandler`.
- Similarly, order exceptions route to `OrderExceptionHandler`.
- This approach avoids bean resolution collisions in the dependency injection container.
- The base exception classes reside in the domain model and maintain zero framework dependencies.

### Alternatives Considered
- Single application-wide `DomainException`: Rejected because it couples independent bounded contexts and complicates per-controller dispatch.
- Catching `Throwable` or `RuntimeException` in both handlers: Rejected because duplicate beans for the same type cause `NonUniqueBeanException`.

---

## 4. Validation and Constraint Handling

### Decision
Retain bean validation constraint handling for incoming request DTOs:
- Handle `ConstraintViolationException` through a dedicated validation handler or delegate to Micronaut validation.
- Return the standardized `ErrorResponse` with HTTP 400 Bad Request and violation messages in `details`.

### Rationale
- Protects the boundary contracts for `POST /v1/products` and `POST /v1/orders`.
- Guarantees backward compatibility with existing integration tests.

### Alternatives Considered
- Default Micronaut error response processor: Rejected if the payload format differs from the existing `ErrorResponse` contract.

---

## 5. Static Analysis and Architecture Compliance

### Decision
Place all `ExceptionHandler` implementations in `adapter.in.rest`:
- Handlers depend only on domain exceptions and `ErrorResponse` DTOs.
- Keep the domain base exceptions framework-free under `domain.model`.
- Satisfy all ArchUnit rules, Checkstyle rules, PMD limits, and SpotBugs checks.

### Rationale
- Handlers are inbound REST adapters that translate exceptions into HTTP responses.
- Placing handlers in `adapter.in.rest` preserves hexagonal architecture boundaries.
- Preserving framework-free domain models complies with the project constitution.

### Alternatives Considered
- Placing handlers in a separate `adapter.in.rest.error` package: Rejected to avoid unnecessary package nesting.

