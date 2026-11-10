# Data Model: Product Management

**Feature**: `003-product-management`

**Date**: 2026-11-10

## 1. Domain Entities and Value Objects

### 1.1 Product Aggregate Root

The `Product` class is an immutable domain model in `es.nachobrito.sdd.example.domain.model`.

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `id` | `UUID` | Yes | Non-null, unique | Client-generated unique identifier |
| `sku` | `String` | Yes | Non-blank, max 100 chars, unique | Stock Keeping Unit |
| `name` | `String` | Yes | Non-blank, max 255 chars | Human-readable product name |
| `description` | `String` | No | Nullable, max 4000 chars | Detailed product description |
| `priceInCents` | `int` | Yes | Integer >= 0 | Monetary price in integer cents |
| `currency` | `String` | Yes | 3-letter uppercase ISO-4217 code | Currency denomination |
| `status` | `ProductStatus` | Yes | Non-null enum | Lifecycle status |
| `createdAt` | `Instant` | Yes | Non-null, UTC | Timestamp when record was created |
| `updatedAt` | `Instant` | Yes | Non-null, UTC | Timestamp when record was last updated |

### 1.2 ProductStatus Enum

Represents the lifecycle state of a product:

| Value | Description |
|-------|-------------|
| `DRAFT` | Initial state. Product is being defined and is not visible to public customers. |
| `ACTIVE` | Product is active and available in catalog. |
| `ARCHIVED` | Product is discontinued or archived. |

### 1.3 State Transitions

```
 [Client Create] ──> DRAFT ──> ACTIVE ──> ARCHIVED
                       │                   │
                       └───────────────────┴──> [Hard Delete (DELETE)]
```

- Any product in any status (`DRAFT`, `ACTIVE`, `ARCHIVED`) can be permanently removed via `DELETE /v1/products/{id}`.
- Any status can be updated to another valid status via `PUT /v1/products/{id}`.

---

## 2. Invariants and Business Rules

1. **Client Identifier Invariant**:
   - The product identifier must be a valid UUID provided by the client.
   - The system never assigns or overwrites the identifier during creation.
2. **SKU Uniqueness Invariant**:
   - Each SKU must be unique across the entire product catalog.
   - Duplicate SKUs must be rejected with a conflict error.
3. **Price Non-Negativity Invariant**:
   - `priceInCents` must be an integer greater than or equal to 0.
   - Negative values must be rejected at every layer (controller, service, repository).
4. **Currency Validation Invariant**:
   - `currency` must be verified against `java.util.Currency.getInstance(code)`.
   - Invalid or unrecognized codes must be rejected.
5. **Timestamp Invariant**:
   - `createdAt` is immutable after product creation.
   - `updatedAt` is updated to current UTC time whenever product attributes change.

---

## 3. Database Schema (MariaDB)

### Table: `products`

```sql
CREATE TABLE products (
    id VARCHAR(36) NOT NULL,
    sku VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    price_in_cents INT NOT NULL,
    currency CHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT uk_products_sku UNIQUE (sku),
    CONSTRAINT chk_products_price CHECK (price_in_cents >= 0)
);

CREATE INDEX idx_products_status ON products (status);
CREATE INDEX idx_products_created_at ON products (created_at);
```

---

## 4. Layer Mapping

| Domain Model | DTO (REST Layer) | Entity (Persistence Layer) |
|--------------|------------------|---------------------------|
| `Product.id` (`UUID`) | `ProductRequest.id` (`UUID`), `ProductResponse.id` (`UUID`) | `ProductEntity.id` (`UUID`) |
| `Product.sku` (`String`) | `ProductRequest.sku`, `ProductResponse.sku` | `ProductEntity.sku` |
| `Product.name` (`String`) | `ProductRequest.name`, `ProductResponse.name` | `ProductEntity.name` |
| `Product.description` (`String`) | `ProductRequest.description`, `ProductResponse.description` | `ProductEntity.description` |
| `Product.priceInCents` (`int`) | `ProductRequest.priceInCents`, `ProductResponse.priceInCents` | `ProductEntity.priceInCents` |
| `Product.currency` (`String`) | `ProductRequest.currency`, `ProductResponse.currency` | `ProductEntity.currency` |
| `Product.status` (`ProductStatus`) | `ProductRequest.status`, `ProductResponse.status` | `ProductEntity.status` (`String`) |
| `Product.createdAt` (`Instant`) | `ProductResponse.createdAt` (`Instant`) | `ProductEntity.createdAt` (`Instant`) |
| `Product.updatedAt` (`Instant`) | `ProductResponse.updatedAt` (`Instant`) | `ProductEntity.updatedAt` (`Instant`) |

