# Data Model: Refactor Controller Error Management

**Feature**: `005-refactor-error-management`

**Date**: 2026-11-11

## 1. REST Error DTO

### 1.1 ErrorResponse Record

The `ErrorResponse` record is the standard error payload model in `es.nachobrito.sdd.example.adapter.in.rest.dto`.

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `message` | `String` | Yes | Non-blank | Primary human-readable error description |
| `details` | `List<String>` | No | Unmodifiable list | Optional specific violation or detail messages |

---

## 2. Domain Exception Models

### 2.1 Product Domain Exception Hierarchy

All product domain exceptions reside in `es.nachobrito.sdd.example.domain.model`.
They are framework-free Java classes that extend `ProductException`.

```text
RuntimeException
 └── ProductException (abstract)
      ├── ProductNotFoundException
      ├── DuplicateIdentifierException
      ├── DuplicateSkuException
      └── InvalidProductDataException
```

| Exception Class | Base Class | Constructor Parameters | Mapped HTTP Status |
|-----------------|------------|------------------------|--------------------|
| `ProductException` | `RuntimeException` | `String message`, `Throwable cause` | N/A (Abstract) |
| `ProductNotFoundException` | `ProductException` | `UUID id` | 404 Not Found |
| `DuplicateIdentifierException` | `ProductException` | `UUID id` | 409 Conflict |
| `DuplicateSkuException` | `ProductException` | `String sku` | 409 Conflict |
| `InvalidProductDataException` | `ProductException` | `String message`, `Throwable cause` | 400 Bad Request |

---

### 2.2 Order Domain Exception Hierarchy

All order domain exceptions reside in `es.nachobrito.sdd.example.domain.model`.
They are framework-free Java classes that extend `OrderException`.

```text
RuntimeException
 └── OrderException (abstract)
      ├── OrderNotFoundException
      ├── DuplicateOrderIdentifierException
      ├── OrderDeletionConflictException
      └── InvalidOrderDataException
```

| Exception Class | Base Class | Constructor Parameters | Mapped HTTP Status |
|-----------------|------------|------------------------|--------------------|
| `OrderException` | `RuntimeException` | `String message`, `Throwable cause` | N/A (Abstract) |
| `OrderNotFoundException` | `OrderException` | `UUID id` | 404 Not Found |
| `DuplicateOrderIdentifierException` | `OrderException` | `UUID id` | 409 Conflict |
| `OrderDeletionConflictException` | `OrderException` | `UUID id, OrderStatus status` | 409 Conflict |
| `InvalidOrderDataException` | `OrderException` | `String message`, `Throwable cause` | 400 Bad Request |

---

## 3. Exception Handler Component Model

The exception handlers reside in `es.nachobrito.sdd.example.adapter.in.rest`.
Each handler implements the Micronaut `ExceptionHandler` interface.

| Handler Class | Handled Exception Type | Handled Controller | Injected Dependencies |
|---------------|------------------------|--------------------|-----------------------|
| `ProductExceptionHandler` | `ProductException` | `ProductController` | None |
| `OrderExceptionHandler` | `OrderException` | `OrderController` | None |

### 3.1 Mapping Rules for ProductExceptionHandler

1. If the exception is `ProductNotFoundException`, return HTTP 404 with `new ErrorResponse(message)`.
2. If the exception is `DuplicateIdentifierException`, return HTTP 409 with `new ErrorResponse(message)`.
3. If the exception is `DuplicateSkuException`, return HTTP 409 with `new ErrorResponse(message)`.
4. If the exception is `InvalidProductDataException`, return HTTP 400 with `new ErrorResponse(message)`.
5. For any other `ProductException`, return HTTP 400 with `new ErrorResponse(message)`.

### 3.2 Mapping Rules for OrderExceptionHandler

1. If the exception is `OrderNotFoundException`, return HTTP 404 with `new ErrorResponse(message)`.
2. If the exception is `DuplicateOrderIdentifierException`, return HTTP 409 with `new ErrorResponse(message)`.
3. If the exception is `OrderDeletionConflictException`, return HTTP 409 with `new ErrorResponse(message)`.
4. If the exception is `InvalidOrderDataException`, return HTTP 400 with `new ErrorResponse(message)`.
5. For any other `OrderException`, return HTTP 400 with `new ErrorResponse(message)`.

