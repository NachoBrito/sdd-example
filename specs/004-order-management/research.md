# Research: Order Management

**Feature**: `004-order-management`

**Date**: 2026-11-10

## 1. Architecture and Layering

### Decision
Implement the Order feature using Hexagonal Architecture (Ports and Adapters) under `es.nachobrito.sdd.example`:
- `domain.model`: `Order`, `OrderStatus`, `DeliveryAddress`, `InvoiceAddress`, `OrderLine`, and domain exceptions. Pure Java with zero framework dependencies.
- `application.port.in`: Inbound use case interfaces (`CreateOrderUseCase`, `GetOrderUseCase`, `UpdateOrderUseCase`, `DeleteOrderUseCase`, `ListOrdersUseCase`).
- `application.port.out`: Outbound repository interface (`OrderRepositoryPort`).
- `application.service`: `OrderService` implementing the inbound use case interfaces and coordinating transactions.
- `adapter.in.rest`: `OrderController` at `@Controller("/v1/orders")` with OpenAPI documentation and DTO mappers.
- `adapter.out.persistence`: `OrderPersistenceAdapter` implementing `OrderRepositoryPort`, backed by `OrderJdbcRepository` and `OrderLineJdbcRepository`.

### Rationale
- Mandated by repository rules in `AGENTS.md` and verified by ArchUnit tests (`HexagonalArchitectureTest`).
- Isolates business logic from external frameworks, databases, and network transports.
- Aligns with the project constitution (Principle 4: Optimize for Deletion, Not Extension; Principle 6: Contract at the Boundary).

### Alternatives Considered
- Direct controller to repository calls: Rejected because this approach violates ArchUnit layer rules and couples database logic with web controllers.
- Anemic data model with database-specific ORM mappings: Rejected because project rules strictly forbid Hibernate and JPA.

---

## 2. Persistence and Aggregate Relational Mapping

### Decision
Map the Order aggregate across two relational tables in MariaDB:
1. `orders`: Contains primary attributes (`id`, `customer_name`, `customer_email`, `order_date`, `status`, `total_amount_in_cents`, `currency`, `created_at`, `updated_at`) and embedded delivery and invoice address columns (`delivery_name`, `delivery_telephone`, `delivery_street`, `delivery_complement`, `delivery_city`, `delivery_state`, `delivery_zip_code`, `delivery_country`, `invoice_name`, `invoice_vat_number`, `invoice_telephone`, `invoice_street`, `invoice_complement`, `invoice_city`, `invoice_state`, `invoice_zip_code`, `invoice_country`).
2. `order_lines`: Child table containing `(id, order_id, product_id, quantity, unit_price_in_cents, line_total_in_cents)` with a foreign key referencing `orders(id) ON DELETE CASCADE`.

### Rationale
- Delivery address and invoice address have a strict 1-to-1 relationship with the order. Embedding columns eliminates joins and avoids separate identity management for address value objects.
- Order lines have a 1-to-N relationship with the order, requiring a dedicated child table.
- A cascading foreign key constraint ensures referential integrity and clean cascade deletion.

### Alternatives Considered
- Separate tables for `order_delivery_addresses` and `order_invoice_addresses`: Rejected because addresses have no lifecycle outside the order.
- Storing lines as a JSON column: Rejected because JSON columns reduce query performance and prevent standard relational foreign key checks on product identifiers.

---

## 3. Total Amount Calculation and Validation

### Decision
The server calculates and enforces monetary totals:
- For each order line: `lineTotalInCents = quantity * unitPriceInCents`.
- For the order total: `totalAmountInCents = sum(lineTotalInCents)`.
- If a client supplies values for `lineTotalInCents` or `totalAmountInCents`, the server recalculates and enforces the correct amounts.

### Rationale
- Selected by the user (Question 2: Option A).
- Protects the system against client calculation tampering and price manipulation.
- Guarantees mathematical consistency across all persisted orders.

### Alternatives Considered
- Trusting client-supplied totals without verification: Rejected because this approach allows incorrect or fraudulent order totals.
- Requiring client totals and failing if mismatched: Rejected because client implementations may round or calculate differently.

---

## 4. Deletion Persistence Semantics and State Guarding

### Decision
Implement conditional hard deletion:
- `DELETE /v1/orders/{id}` permanently removes the order and its child lines from the database if the status is `PENDING` or `CANCELLED`.
- If the status is `PAID`, `SHIPPED`, or `DELIVERED`, the system rejects the deletion and returns HTTP 409 Conflict with an explanatory message.
- If the order does not exist, the system returns HTTP 404 Not Found.

### Rationale
- Selected by the user (Question 3: Option B).
- Allows cleanup of cancelled or abandoned orders while protecting financial and fulfillment audit records.

### Alternatives Considered
- Unconditional hard delete: Rejected because deleting fulfilled orders destroys business history.
- Soft delete via status change: Rejected because the user selected hard deletion for eligible statuses.

---

## 5. Query Filtering and Dynamic Sorting

### Decision
Support the following optional query parameters on `GET /v1/orders`:
- `status`: Filter by `OrderStatus` enum value.
- `customerEmail`: Filter by exact customer email address.
- `startDate`: Filter by order date greater than or equal to timestamp.
- `endDate`: Filter by order date less than or equal to timestamp.
- `sort`: Sort by `date` (mapped to `order_date`) or `status`. Default is `date`.
- `direction`: Sort direction `asc` or `desc`. Default is `asc`.

Implement dynamic querying in `OrderJdbcRepository` using SQL parameters.

### Rationale
- Directly satisfies the user specification: `GET /orders (filter by status, customerEmail, dateRange (startDate, endDate), sort by date, status)`.
- Database-level filtering avoids loading the complete table into memory.

### Alternatives Considered
- In-memory stream filtering: Rejected because in-memory filtering scales poorly as order volume grows.
- Elasticsearch or secondary search engine: Rejected by Constitution Principle 3 (Do Not Distribute by Default).

---

## 6. Multi-Layer Validation Strategy

### Decision
Enforce validation at every architectural boundary:
1. **Controller Layer**:
   - Jakarta / Micronaut Validation annotations (`@Valid`, `@NotNull`, `@Email`, `@Positive`, `@PositiveOrZero`, `@Size`).
   - Rejects malformed requests with HTTP 400 Bad Request.
2. **Application / Service Layer**:
   - Verifies identifier uniqueness on creation.
   - Enforces deletion status rules (rejects deletion of non-deletable orders with HTTP 409).
3. **Domain Layer**:
   - Constructor invariant checks in `Order`, `OrderLine`, `DeliveryAddress`, and `InvoiceAddress`.
   - Validates ISO-4217 currency codes via `java.util.Currency.getInstance(code)`.
4. **Database Layer**:
   - Schema constraints: `NOT NULL`, `CHECK (total_amount_in_cents >= 0)`, `CHECK (quantity > 0)`, `CHECK (unit_price_in_cents >= 0)`.

### Rationale
- Satisfies Constitution Principle 6 (Contract at the Boundary, Not in the Middle).
- Guarantees data integrity even when internal services invoke use cases directly.

