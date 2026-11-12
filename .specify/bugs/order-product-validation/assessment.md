# Bug Assessment: Order Product Validation

- **Slug**: order-product-validation
- **Created**: 2026-11-12
- **Source**: pasted text
- **Verdict**: valid
- **Severity**: high

## Report (verbatim or summarized)

When an order is created, product ids are not validated, making possible to create an order with unexisting products.

## Symptom

When a client creates an order with unexisting product identifiers, the system accepts the request and returns HTTP 201 Created.
The system must reject the request and return HTTP 400 Bad Request when an order line contains an unexisting product identifier.

## Reproduction

1. Send a POST request to `/v1/orders` with a valid payload.
2. In the `orderLines` array, specify a `productId` that does not exist in the product catalog (for example, `00000000-0000-0000-0000-000000000000`).
3. Observe that the server returns HTTP 201 Created and persists the order.
4. Send a GET request to `/v1/orders/{id}` and observe that the order contains the non-existent product identifier.

## Suspected Code Paths

- `src/main/java/es/nachobrito/sdd/example/application/service/OrderService.java:58-86` — `createOrder` calculates totals and persists the order, but does not verify product existence.
- `src/main/java/es/nachobrito/sdd/example/application/service/OrderService.java:95-118` — `updateOrder` updates order lines without verifying product existence.
- `src/main/java/es/nachobrito/sdd/example/domain/model/OrderLine.java:22-38` — The `OrderLine` value object constructor validates format and numeric values, but cannot verify external product existence.
- `src/main/java/es/nachobrito/sdd/example/adapter/out/persistence/OrderPersistenceAdapter.java:99-103` — `insertOrderLines` inserts records into the database without product existence checks.
- `src/main/resources/db/changelog/003-create-orders-tables.xml:113-115` — The `order_lines` table defines `product_id VARCHAR(36) NOT NULL` without a foreign key constraint to the `products` table.

## Root Cause Hypothesis

Confidence: high.
The `OrderService` application service does not depend on `ProductRepositoryPort`.
The service does not check whether product identifiers in `CreateOrderCommand` exist in the database.
In addition, the `order_lines` database table has no foreign key constraint that references the `products` table.
Because no layer validates product existence, the application creates orders with fictitious product identifiers.

## Proposed Remediation

**Preferred**:
Inject `ProductRepositoryPort` into `OrderService`.
In `OrderService.createOrder` and `OrderService.updateOrder`, iterate through the order lines.
Verify that each product identifier exists via `productRepository.existsById(productId)`.
If a product does not exist, throw an `InvalidOrderDataException` with a descriptive message.
The `OrderExceptionHandler` will translate this exception into HTTP 400 Bad Request.

**Alternatives**:
- Alternative 1: Throw `ProductNotFoundException`. The `ProductExceptionHandler` will return HTTP 404 Not Found. Trade-off: HTTP 404 on `POST /v1/orders` implies that the endpoint does not exist instead of signaling a payload validation failure.
- Alternative 2: Add a foreign key constraint in a Liquibase changeset from `order_lines.product_id` to `products.id`. Trade-off: This enforces integrity at the database level, but unhandled database exceptions will cause HTTP 500 responses instead of user-friendly validation errors.

**Files likely to change**:
- `src/main/java/es/nachobrito/sdd/example/application/service/OrderService.java`
- `src/test/java/es/nachobrito/sdd/example/application/service/OrderServiceCommandTest.java`
- `src/test/java/es/nachobrito/sdd/example/adapter/in/rest/OrderFilterAndValidationTestCase.java`
- `src/test/java/es/nachobrito/sdd/example/adapter/in/rest/OrderLifecycleTestCase.java`

**Tests to add or update**:
- Add a unit test to `OrderServiceCommandTest` to verify that `createOrder` throws `InvalidOrderDataException` when a product does not exist.
- Add a unit test to `OrderServiceCommandTest` to verify that `updateOrder` throws `InvalidOrderDataException` when an order line product does not exist.
- Add an integration test to `OrderFilterAndValidationTestCase` to verify that creating an order with an unexisting product returns HTTP 400 Bad Request.
- Update `OrderLifecycleTestCase` and `OrderFilterAndValidationTestCase` helper methods that generate random product identifiers so they create active products before order placement.

## Risks & Considerations

- Existing test suite impact: Several integration tests (`OrderLifecycleTestCase`, `OrderFilterAndValidationTestCase`) use `UUID.randomUUID()` for product identifiers without inserting products. Adding validation will cause these tests to fail until their test fixtures create real products.
- Performance: Validating product identifiers one by one executes multiple database queries. If orders contain many lines, consider a batch query method such as `existsAllById(Set<UUID> ids)`.
- Scope discipline: As mandated by Principle 1 of the constitution, limit the validation to product existence. Do not add unrequested checks for product catalog status (`ACTIVE`) or price consistency unless explicitly requested.

## Open Questions

- Should `updateOrder` also validate product existence for all updated order lines? (Recommended: yes, to ensure ongoing referential integrity).
- Should the validation verify only product existence, or also verify that the product has `ACTIVE` status?
- Should the response status code be HTTP 400 Bad Request or HTTP 404 Not Found? (Recommended: HTTP 400 Bad Request).

