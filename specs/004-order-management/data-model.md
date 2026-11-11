# Data Model: Order Management

**Feature**: `004-order-management`

**Date**: 2026-11-10

## 1. Domain Entities and Value Objects

### 1.1 Order Aggregate Root

The `Order` class is an immutable domain model in `es.nachobrito.sdd.example.domain.model`.

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `id` | `UUID` | Yes | Non-null, unique | Client-generated unique identifier |
| `customerName` | `String` | Yes | Non-blank, max 255 chars | Customer full name |
| `customerEmail` | `String` | Yes | Non-blank, valid email syntax, max 255 chars | Customer contact email |
| `date` | `Instant` | Yes | Non-null, UTC | Timestamp of order placement |
| `status` | `OrderStatus` | Yes | Non-null enum | Order lifecycle status |
| `totalAmountInCents` | `long` | Yes | Integer >= 0, equals sum of line totals | Total order monetary amount in cents |
| `currency` | `String` | Yes | 3-letter uppercase ISO-4217 code | Currency code |
| `deliveryAddress` | `DeliveryAddress` | Yes | Non-null value object | Delivery address details |
| `invoiceAddress` | `InvoiceAddress` | Yes | Non-null value object | Invoicing and tax address details |
| `orderLines` | `List<OrderLine>` | Yes | Non-empty list, minimum 1 item | Line items included in the order |
| `createdAt` | `Instant` | Yes | Non-null, UTC | Timestamp when record was created |
| `updatedAt` | `Instant` | Yes | Non-null, UTC | Timestamp when record was last updated |

---

### 1.2 DeliveryAddress Value Object

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `name` | `String` | Yes | Non-blank, max 255 chars | Recipient name |
| `telephone` | `String` | Yes | Non-blank, max 50 chars | Contact phone number |
| `street` | `String` | Yes | Non-blank, max 255 chars | Street name and number |
| `complement` | `String` | No | Nullable, max 255 chars | Apartment, suite, or extra directions |
| `city` | `String` | Yes | Non-blank, max 100 chars | City name |
| `state` | `String` | Yes | Non-blank, max 100 chars | State or province |
| `zipCode` | `String` | Yes | Non-blank, max 20 chars | Postal code |
| `country` | `String` | Yes | Non-blank, max 100 chars | Country name |

---

### 1.3 InvoiceAddress Value Object

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `name` | `String` | Yes | Non-blank, max 255 chars | Invoiced person or entity name |
| `vatNumber` | `String` | Yes | Non-blank, max 50 chars | Tax or VAT registration identifier |
| `telephone` | `String` | Yes | Non-blank, max 50 chars | Contact phone number |
| `street` | `String` | Yes | Non-blank, max 255 chars | Street name and number |
| `complement` | `String` | No | Nullable, max 255 chars | Apartment, suite, or extra directions |
| `city` | `String` | Yes | Non-blank, max 100 chars | City name |
| `state` | `String` | Yes | Non-blank, max 100 chars | State or province |
| `zipCode` | `String` | Yes | Non-blank, max 20 chars | Postal code |
| `country` | `String` | Yes | Non-blank, max 100 chars | Country name |

---

### 1.4 OrderLine Value Object

| Field | Type | Required | Constraints | Description |
|-------|------|----------|-------------|-------------|
| `productId` | `UUID` | Yes | Non-null UUID | Referenced product identifier |
| `quantity` | `int` | Yes | Integer > 0 | Number of units ordered |
| `unitPriceInCents` | `int` | Yes | Integer >= 0 | Price per unit in integer cents |
| `lineTotalInCents` | `long` | Yes | Integer >= 0, equals `quantity * unitPriceInCents` | Total calculated line amount |

---

### 1.5 OrderStatus Enum

| Value | Description | Deletable via `DELETE`? |
|-------|-------------|-------------------------|
| `PENDING` | Order created and awaiting payment confirmation. | Yes |
| `PAID` | Payment received; order ready for fulfillment. | No (HTTP 409) |
| `SHIPPED` | Order in transit with delivery carrier. | No (HTTP 409) |
| `DELIVERED` | Order delivered to customer. | No (HTTP 409) |
| `CANCELLED` | Order cancelled before fulfillment. | Yes |

---

### 1.6 State Transitions

```text
               ┌──────────> PAID ──> SHIPPED ──> DELIVERED
               │             │          │
 [Create] ──> PENDING        │          │
               │             ▼          ▼
               └──────────> CANCELLED <─┘
                               │
                               ▼
                   [Hard Delete (DELETE)]
                   (Only PENDING or CANCELLED)
```

---

## 2. Invariants and Business Rules

1. **Client Identifier Invariant**:
   - The order identifier must be a valid UUID supplied by the client.
   - Creation requests with missing or invalid identifiers must be rejected with HTTP 400.
   - Creation requests with an existing identifier must be rejected with HTTP 409.
2. **Order Lines Invariant**:
   - An order must contain at least one order line.
   - Each order line must have `quantity > 0` and `unitPriceInCents >= 0`.
3. **Calculation Invariant**:
   - `lineTotalInCents` must equal `quantity * unitPriceInCents`.
   - `totalAmountInCents` must equal the sum of all `lineTotalInCents` values in the order.
   - The server enforces these calculations.
4. **Currency Invariant**:
   - Currency must be validated with `java.util.Currency.getInstance(code)`.
5. **Deletion Invariant**:
   - Only orders with status `PENDING` or `CANCELLED` can be removed.
   - Orders with status `PAID`, `SHIPPED`, or `DELIVERED` return HTTP 409 Conflict.

---

## 3. Database Schema (MariaDB)

### Table: `orders`

```sql
CREATE TABLE orders (
    id VARCHAR(36) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    order_date TIMESTAMP(6) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_amount_in_cents BIGINT NOT NULL,
    currency CHAR(3) NOT NULL,
    delivery_name VARCHAR(255) NOT NULL,
    delivery_telephone VARCHAR(50) NOT NULL,
    delivery_street VARCHAR(255) NOT NULL,
    delivery_complement VARCHAR(255) NULL,
    delivery_city VARCHAR(100) NOT NULL,
    delivery_state VARCHAR(100) NOT NULL,
    delivery_zip_code VARCHAR(20) NOT NULL,
    delivery_country VARCHAR(100) NOT NULL,
    invoice_name VARCHAR(255) NOT NULL,
    invoice_vat_number VARCHAR(50) NOT NULL,
    invoice_telephone VARCHAR(50) NOT NULL,
    invoice_street VARCHAR(255) NOT NULL,
    invoice_complement VARCHAR(255) NULL,
    invoice_city VARCHAR(100) NOT NULL,
    invoice_state VARCHAR(100) NOT NULL,
    invoice_zip_code VARCHAR(20) NOT NULL,
    invoice_country VARCHAR(100) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT chk_orders_total CHECK (total_amount_in_cents >= 0)
);

CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_customer_email ON orders (customer_email);
CREATE INDEX idx_orders_order_date ON orders (order_date);
```

### Table: `order_lines`

```sql
CREATE TABLE order_lines (
    id VARCHAR(36) NOT NULL,
    order_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    quantity INT NOT NULL,
    unit_price_in_cents INT NOT NULL,
    line_total_in_cents BIGINT NOT NULL,
    CONSTRAINT pk_order_lines PRIMARY KEY (id),
    CONSTRAINT fk_order_lines_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT chk_order_lines_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_lines_unit_price CHECK (unit_price_in_cents >= 0),
    CONSTRAINT chk_order_lines_total CHECK (line_total_in_cents >= 0)
);

CREATE INDEX idx_order_lines_order_id ON order_lines (order_id);
CREATE INDEX idx_order_lines_product_id ON order_lines (product_id);
```

---

## 4. Layer Mapping

| Domain Model | DTO (REST Layer) | Persistence Entities |
|--------------|------------------|----------------------|
| `Order.id` (`UUID`) | `id` (`UUID`) | `OrderEntity.id` (`UUID`) |
| `Order.customerName` (`String`) | `customerName` (`String`) | `OrderEntity.customerName` (`String`) |
| `Order.customerEmail` (`String`) | `customerEmail` (`String`) | `OrderEntity.customerEmail` (`String`) |
| `Order.date` (`Instant`) | `date` (`Instant`) | `OrderEntity.orderDate` (`Instant`) |
| `Order.status` (`OrderStatus`) | `status` (`OrderStatus`) | `OrderEntity.status` (`String`) |
| `Order.totalAmountInCents` (`long`) | `totalAmountInCents` (`long`) | `OrderEntity.totalAmountInCents` (`long`) |
| `Order.currency` (`String`) | `currency` (`String`) | `OrderEntity.currency` (`String`) |
| `Order.deliveryAddress` (`DeliveryAddress`) | `deliveryAddress` (`AddressDto`) | `OrderEntity.delivery*` fields |
| `Order.invoiceAddress` (`InvoiceAddress`) | `invoiceAddress` (`InvoiceAddressDto`) | `OrderEntity.invoice*` fields |
| `Order.orderLines` (`List<OrderLine>`) | `orderLines` (`List<OrderLineDto>`) | `OrderLineEntity` rows |
| `Order.createdAt` (`Instant`) | `createdAt` (`Instant`) | `OrderEntity.createdAt` (`Instant`) |
| `Order.updatedAt` (`Instant`) | `updatedAt` (`Instant`) | `OrderEntity.updatedAt` (`Instant`) |

