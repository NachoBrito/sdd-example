# Feature Specification: Refactor Controller Error Management

**Feature Branch**: `005-refactor-error-management`

**Created**: 2026-11-11

**Status**: Draft

**Input**: User description: "Refactor error management in controllers: - Replace error handling controllers (OrderExceptionHandler, ProductExceptionHandler) with Micronaut ExceptionHandler classes. - Use one exception handler per controller, keep this behavior for future features."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Standardized Error Responses for Product Controller (Priority: P1)

An API client sends invalid or conflicting product requests to the product endpoints. The system catches the domain exceptions and returns standardized error responses.

**Why this priority**: API clients require dependable error responses when product operations fail. The system must preserve the existing response contract.

**Independent Test**: Submit invalid or conflicting product requests. Verify that the response returns the expected HTTP status and JSON error structure.

**Acceptance Scenarios**:

1. **Given** a product identifier that does not exist in the catalog, **When** the client requests the product by identifier, **Then** the system returns HTTP 404 Not Found with the error message.
2. **Given** an existing product identifier, **When** the client creates a product with that identifier, **Then** the system returns HTTP 409 Conflict with the error message.
3. **Given** an existing product SKU, **When** the client creates a product with that SKU, **Then** the system returns HTTP 409 Conflict with the error message.
4. **Given** invalid product data, **When** the client submits the product request, **Then** the system returns HTTP 400 Bad Request with the error message.
5. **Given** a request payload that violates bean validation constraints, **When** the client submits the product request, **Then** the system returns HTTP 400 Bad Request with the validation details.

---

### User Story 2 - Standardized Error Responses for Order Controller (Priority: P2)

An API client sends invalid or conflicting order requests to the order endpoints. The system catches the domain exceptions and returns standardized error responses.

**Why this priority**: API clients require dependable error responses when order operations fail. The system must preserve the existing response contract.

**Independent Test**: Submit invalid or conflicting order requests. Verify that the response returns the expected HTTP status and JSON error structure.

**Acceptance Scenarios**:

1. **Given** an order identifier that does not exist, **When** the client requests the order by identifier, **Then** the system returns HTTP 404 Not Found with the error message.
2. **Given** an existing order identifier, **When** the client creates an order with that identifier, **Then** the system returns HTTP 409 Conflict with the error message.
3. **Given** an order with status PAID, SHIPPED, or DELIVERED, **When** the client submits a deletion request, **Then** the system returns HTTP 409 Conflict with the error message.
4. **Given** invalid order data, **When** the client submits the order request, **Then** the system returns HTTP 400 Bad Request with the error message.

---

### User Story 3 - Elimination of Error Controller Endpoints (Priority: P3)

An API consumer or automated tool inspects the public REST route inventory and OpenAPI documentation. The consumer verifies that no artificial error controller routes exist.

**Why this priority**: Exposing controller endpoints for internal error handling pollutes the API contract. The API documentation must display only business resource endpoints.

**Independent Test**: Query the HTTP route definitions and inspect the generated OpenAPI schema. Verify that the system registers zero routes for `/v1/errors` and `/v1/order-errors`.

**Acceptance Scenarios**:

1. **Given** the running application, **When** a client sends an HTTP request to `/v1/errors` or `/v1/order-errors`, **Then** the system returns HTTP 404 Not Found because no such endpoints exist.
2. **Given** the generated OpenAPI documentation, **When** an API consumer inspects the document, **Then** the document contains zero operations for error paths.

---

### User Story 4 - Consistent Handler Architecture for Future Features (Priority: P4)

A software engineer adds a new REST controller to the service. The engineer creates exactly one exception handler class for that controller.

**Why this priority**: A predictable 1:1 relationship between controllers and exception handlers keeps the architecture consistent and easy to maintain.

**Independent Test**: Verify that the codebase pairs each REST controller with exactly one dedicated exception handler class.

**Acceptance Scenarios**:

1. **Given** the product REST controller, **When** an exception occurs during request processing, **Then** the dedicated product exception handler processes the failure.
2. **Given** the order REST controller, **When** an exception occurs during request processing, **Then** the dedicated order exception handler processes the failure.
3. **Given** a new controller introduced in a future feature, **When** the engineer implements error handling, **Then** the architecture enforces exactly one exception handler for that controller.

---

### Edge Cases

- What happens when a request triggers a framework error before reaching a controller? The default framework error processor handles the failure.
- What happens when an unhandled exception type occurs in a controller? The system returns HTTP 500 Internal Server Error without leaking internal stack traces.
- What happens when an error response contains multiple validation violations? The response includes all violation messages in the details list.
- What happens when an exception message is empty? The system provides a default error message in the response payload.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST replace error handling controllers with Micronaut `ExceptionHandler` classes.
- **FR-002**: The system MUST assign exactly one dedicated exception handler class to each REST controller.
- **FR-003**: The system MUST remove the `/v1/errors` and `/v1/order-errors` paths from the REST routing table.
- **FR-004**: The system MUST exclude error handling components from the generated OpenAPI endpoint specifications.
- **FR-005**: The product exception handler MUST map `ProductNotFoundException` to HTTP 404 Not Found.
- **FR-006**: The product exception handler MUST map `DuplicateIdentifierException` and `DuplicateSkuException` to HTTP 409 Conflict.
- **FR-007**: The product exception handler MUST map `InvalidProductDataException` and `ConstraintViolationException` to HTTP 400 Bad Request.
- **FR-008**: The order exception handler MUST map `OrderNotFoundException` to HTTP 404 Not Found.
- **FR-009**: The order exception handler MUST map `DuplicateOrderIdentifierException` and `OrderDeletionConflictException` to HTTP 409 Conflict.
- **FR-010**: The order exception handler MUST map `InvalidOrderDataException` to HTTP 400 Bad Request.
- **FR-011**: All error responses MUST return the standardized `ErrorResponse` structure containing a message and optional details.
- **FR-012**: Future feature additions MUST maintain the pattern of one exception handler class per REST controller.

### Key Entities

- **Error Response**: The standardized DTO returned to clients upon failure. Contains a summary message and an optional list of detail strings.
- **Controller Exception Handler**: The component responsible for translating exceptions from a specific REST controller into HTTP error responses.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: All existing automated tests for product and order error conditions continue to pass with 100 percent success rate.
- **SC-002**: The API routing table contains zero routes for `/v1/errors` and `/v1/order-errors`.
- **SC-003**: The generated OpenAPI specification contains zero paths for error handlers.
- **SC-004**: Each REST controller in the codebase is paired with exactly one exception handler class.
- **SC-005**: All error response payloads and HTTP status codes remain 100 percent backward compatible.

## Assumptions

- All domain exceptions continue to originate within their respective bounded contexts (`Product` and `Order`).
- The existing `ErrorResponse` schema satisfies all current client error reporting needs.
- The build checks (Checkstyle, PMD, SpotBugs, ArchUnit, and Pitest) continue to enforce quality standards on the new handler classes.

