# Bug Fix: Order Product Validation

- **Slug**: order-product-validation
- **Fixed**: 2026-11-12
- **Assessment**: ./assessment.md
- **Status**: applied

## Summary

Injected `ProductRepositoryPort` into `OrderService` and added existence validation for product identifiers in order lines.
Requests with unexisting product identifiers now throw `InvalidOrderDataException` and return HTTP 400 Bad Request.

## Changes

| File | Change | Notes |
|------|--------|-------|
| `src/main/java/es/nachobrito/sdd/example/application/service/OrderService.java` | modified | Added `ProductRepositoryPort` dependency and validated product existence in `createOrder` and `updateOrder`. |
| `src/test/java/es/nachobrito/sdd/example/application/service/OrderServiceCommandTest.java` | modified | Injected mock `ProductRepositoryPort` and added unit tests for missing products. |
| `src/test/java/es/nachobrito/sdd/example/application/service/OrderServiceDeleteTest.java` | modified | Updated constructor invocation with mock `ProductRepositoryPort`. |
| `src/test/java/es/nachobrito/sdd/example/application/service/OrderServiceQueryTest.java` | modified | Updated constructor invocation with mock `ProductRepositoryPort`. |
| `src/test/java/es/nachobrito/sdd/example/adapter/in/rest/OrderFilterAndValidationTestCase.java` | modified | Added `rejectUnexistingProductReturnsBadRequest` test and updated test fixtures to create products. |
| `src/test/java/es/nachobrito/sdd/example/adapter/in/rest/OrderLifecycleTestCase.java` | modified | Updated test fixtures to create products before order placement. |
| `src/test/java/es/nachobrito/sdd/example/integration/NotificationFlowTestCase.java` | modified | Updated test fixture to create an active product before order placement. |

## Diff Highlights

```java
  private void validateProductExistence(List<OrderLine> orderLines) {
    for (OrderLine line : orderLines) {
      if (!productRepository.existsById(line.productId())) {
        throw new InvalidOrderDataException(
            "Product not found with identifier: " + line.productId());
      }
    }
  }
```

## Tests Added or Updated

- `es.nachobrito.sdd.example.application.service.OrderServiceCommandTest::createOrderThrowsInvalidOrderDataExceptionWhenProductNotFound` — Verifies that `createOrder` rejects non-existent product identifiers.
- `es.nachobrito.sdd.example.application.service.OrderServiceCommandTest::updateOrderThrowsInvalidOrderDataExceptionWhenProductNotFound` — Verifies that `updateOrder` rejects non-existent product identifiers.
- `es.nachobrito.sdd.example.adapter.in.rest.OrderFilterAndValidationTestCase::rejectUnexistingProductReturnsBadRequest` — Verifies that `POST /v1/orders` returns HTTP 400 Bad Request when an unexisting product is supplied.
- `es.nachobrito.sdd.example.adapter.in.rest.OrderLifecycleTestCase` — Updated all lifecycle tests to persist a product before creating an order.
- `es.nachobrito.sdd.example.integration.NotificationFlowTestCase::orderLifecycleEventsTriggerAsynchronousNotifications` — Updated integration flow to persist a product before creating an order.

## Local Verification

- Commands run:
  - `./gradlew test` → SUCCESS (350 tests completed, 0 failures)
  - `./gradlew pmdTest` → SUCCESS (PMD import limit verified)
  - `./gradlew build` → SUCCESS (All checks passed: Checkstyle, PMD, SpotBugs, ArchUnit, and Pitest mutation tests with 91% killed)

## Deviations from Assessment

- Updated `NotificationFlowTestCase.java` in addition to the files listed in the assessment. The notification flow test generated a random product identifier without persisting a product.

## Follow-ups

- None required. All tests and static analysis gates passed.

