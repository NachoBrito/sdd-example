# Research: Product Management

**Feature**: `003-product-management`

**Date**: 2026-11-10

## 1. Architecture and Layering

### Decision
Implement the feature according to Hexagonal Architecture (Ports and Adapters) under `es.nachobrito.sdd.example`:
- `domain.model`: `Product`, `ProductStatus`, and related value objects. Pure Java, zero framework dependencies.
- `application.port.in`: Inbound use case ports (`CreateProductUseCase`, `GetProductUseCase`, `UpdateProductUseCase`, `DeleteProductUseCase`, `ListProductsUseCase`).
- `application.port.out`: Outbound ports (`ProductRepositoryPort`).
- `application.service`: `ProductService` implementing the inbound ports.
- `adapter.in.rest`: `ProductController` annotated with `@Controller("/v1/products")` and OpenAPI annotations.
- `adapter.out.persistence`: `ProductPersistenceAdapter` implementing `ProductRepositoryPort`, backed by `ProductJdbcRepository`.

### Rationale
- Mandated by repository rules (`AGENTS.md`) and verified by ArchUnit tests (`HexagonalArchitectureTest`).
- Isolates business logic from external libraries, database drivers, and serialization frameworks.
- Aligns with the project constitution (Principle 4: Optimize for Deletion, Not Extension; Principle 6: Contract at the Boundary).

### Alternatives Considered
- Direct Controller-to-Repository architecture: Rejected because it violates ArchUnit layer rules and couples database concerns with HTTP handling.
- Rich domain service with JPA entity: Rejected because JPA/Hibernate is strictly prohibited by project conventions.

---

## 2. Persistence and Client-Side Identifier Handling

### Decision
Use Micronaut Data JDBC with MariaDB.
- Liquibase migration script `db/changelog/002-create-products-table.xml` included in `db.changelog-master.xml`.
- Entity table `products` with columns: `id` (VARCHAR(36) or UUID binary), `sku` (VARCHAR(100) UNIQUE), `name` (VARCHAR(255)), `description` (TEXT), `price_in_cents` (BIGINT/INT), `currency` (CHAR(3)), `status` (VARCHAR(20)), `created_at` (TIMESTAMP), `updated_at` (TIMESTAMP).
- Because identifiers are generated on the client, `@Id` on the persistence model does not use `@GeneratedValue`.
- The persistence adapter checks existence and executes explicit `insert` or `update` operations on the Micronaut Data repository.

### Rationale
- MariaDB and Liquibase are standard in this project.
- Client-side ID generation prevents database round-trips for ID retrieval and allows idempotent creation requests.
- Explicit database constraints (`UNIQUE (sku)`, `CHECK (price_in_cents >= 0)`) ensure data integrity at the database layer.

### Alternatives Considered
- Database-generated autoincrement ID or database UUID: Rejected because user specification explicitly mandates client-side ID generation.
- Soft-deletion column (`deleted_at`): Rejected because user clarification explicitly confirmed hard-delete semantics (permanent removal, 204 No Content).

---

## 3. Multi-Layer Validation Strategy

### Decision
Enforce validation at every layer as requested:
1. **Controller Layer**:
   - Jakarta / Micronaut Validation annotations (`@Valid`, `@NotNull`, `@Pattern`, `@PositiveOrZero`).
   - Validates incoming HTTP DTOs at the boundary. Returns 400 Bad Request with field error details.
2. **Application / Service Layer**:
   - Validates use case commands before invoking domain logic.
   - Verifies ID uniqueness and SKU uniqueness against the repository.
   - Throws domain exceptions (`DuplicateIdentifierException`, `DuplicateSkuException`, `ProductNotFoundException`) mapped to HTTP 409 and 404.
3. **Domain Layer**:
   - Invariants checked inside domain model constructors.
   - Enforces valid UUID format, non-empty SKU and name, non-negative price, valid ISO-4217 currency (`java.util.Currency.getInstance`), and non-null status.
4. **Repository / Database Layer**:
   - Table schema defines column constraints: `NOT NULL`, `UNIQUE(sku)`, `CHECK(price_in_cents >= 0)`.

### Rationale
- Satisfies explicit user requirement: "Implement proper validation in every layer: controller, service and repository".
- Upholds Constitution Principle 6: "Semantic reconciliation and data validation MUST happen strictly at the boundary."

### Alternatives Considered
- Single-point validation at controller only: Rejected because internal consumers or direct service calls could bypass validation.
- Database-only validation: Rejected because database errors result in generic 500 responses rather than clear 400 client messages.

---

## 4. Query Filtering and Regex Search

### Decision
Support filtering by `sku`, regular expression `name`, and `status`, plus sorting by `name`, `status`, and `createdAt`.
- Filter parameters are optional query parameters on `GET /v1/products`.
- In MariaDB, regular expression matching uses the `REGEXP` operator (`name REGEXP :nameRegex`).
- When `nameRegex` is null or blank, regular expression matching is skipped.
- Sort parameter accepts field name and direction (`name,asc`, `createdAt,desc`), validated against permitted sortable fields.

### Rationale
- Matches user specification for `GET /products (filter by sku, name regex, status, sort by name, status, createdAt)`.
- Server-side filtering avoids loading the entire catalog into application memory.

### Alternatives Considered
- In-memory regex filtering: Rejected because it performs poorly as catalog size grows.
- Full-text search engine: Rejected by Constitution Principle 3 (Do Not Distribute by Default) and Principle 1 (Minimum Viable Diff).

---

## 5. Price and Currency Format

### Decision
- Represent price as `Integer` (or `Long`) `priceInCents` representing integer cents (e.g., 1000 = $10.00).
- Represent currency as a 3-letter uppercase ISO-4217 code (e.g., `USD`, `EUR`).
- Validate currency code against standard `java.util.Currency`.

### Rationale
- Prevents floating-point rounding errors common in monetary calculations.
- Exactly adheres to user specification.

### Alternatives Considered
- `BigDecimal` with decimal fractions: Rejected because user explicitly specified `priceInCents` as an Integer.

