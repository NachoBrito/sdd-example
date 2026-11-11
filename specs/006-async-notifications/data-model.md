# Data Model: Asynchronous Notifications

**Feature Branch**: `006-async-notifications`  
**Date**: 2026-11-11  
**Spec**: [spec.md](spec.md)

This document describes the models, boundary event schemas, value objects, and state transitions for the asynchronous notification system.

---

## 1. Domain & Application Models

### NotificationChannel (Value Object / Enum)
Identifies the communication medium for notification delivery.

| Field | Type | Description |
|-------|------|-------------|
| `SMTP` | Enum constant | Email delivery using Simple Mail Transfer Protocol |

### NotificationMessage (Value Object)
Represents a transport-independent notification message ready for delivery.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `recipient` | `String` | Non-blank, valid email syntax | Customer destination address |
| `subject` | `String` | Non-blank, max 200 chars | Subject line of the notification |
| `body` | `String` | Non-blank | Rendered message body (HTML for SMTP) |
| `channel` | `NotificationChannel` | Non-null | Target delivery channel |

---

## 2. Boundary Event Contracts (Messaging DTOs)

### OrderEventType (Enum)
Classifies the order lifecycle occurrence.

- `CREATED`: New order placed in the system.
- `UPDATED`: Existing order attributes, lines, or non-terminal statuses modified.
- `CANCELLED`: Order status changed to `CANCELLED` or order deleted.

### OrderLifecycleEvent (Message Envelope)
The message payload published to RabbitMQ exchange `sdd.orders`.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `eventId` | `UUID` | Non-null, version 4 UUID | Unique event identifier |
| `eventType` | `OrderEventType` | Non-null | Lifecycle event classification |
| `timestamp` | `Instant` | Non-null, UTC | Timestamp when event occurred |
| `order` | `OrderPayload` | Non-null | Complete snapshot of the order |

### OrderPayload
Snapshot of order attributes at the time the event occurred.

| Field | Type | Description |
|-------|------|-------------|
| `id` | `UUID` | Order unique identifier |
| `customerName` | `String` | Full name of the customer |
| `customerEmail` | `String` | Email address of the customer |
| `date` | `Instant` | Order creation timestamp |
| `status` | `String` | Order status (`PENDING`, `PAID`, `SHIPPED`, `DELIVERED`, `CANCELLED`) |
| `totalAmountInCents`| `long` | Order monetary total in cents |
| `currency` | `String` | 3-letter ISO-4217 currency code |
| `deliveryAddress` | `AddressPayload` | Delivery address details |
| `invoiceAddress` | `AddressPayload` | Invoice address details |
| `orderLines` | `List<OrderLinePayload>`| List of order line snapshots |

### AddressPayload
Address snapshot for delivery or invoice.

| Field | Type | Description |
|-------|------|-------------|
| `name` | `String` | Recipient or billing contact name |
| `telephone` | `String` | Contact phone number |
| `street` | `String` | Primary street address |
| `complement` | `String` (nullable) | Optional apartment, unit, or suite |
| `city` | `String` | City name |
| `state` | `String` | State, region, or province |
| `zipCode` | `String` | Postal code |
| `country` | `String` | Country name or code |

### OrderLinePayload
Product line item snapshot.

| Field | Type | Description |
|-------|------|-------------|
| `productId` | `UUID` | Product identifier |
| `quantity` | `int` | Ordered quantity (>= 1) |
| `unitPriceInCents` | `long` | Unit price in cents (>= 0) |
| `lineTotalInCents` | `long` | Calculated line total (`quantity * unitPriceInCents`) |

---

## 3. State Transitions & Event Flow

```
                      +-------------------+
                      |   Order Placed    |
                      +---------+---------+
                                |
                                v
               [Publish: order.created to sdd.orders]
                                |
                                v
                [Consume: sdd.orders.notifications]
                                |
                                v
                   [Render: HTML Created Template]
                                |
                                v
                   [Send: SMTP to customerEmail]

                      +-------------------+
                      |   Order Updated   |
                      +---------+---------+
                                |
            +-------------------+-------------------+
            | (status == CANCELLED)                 | (status != CANCELLED)
            v                                       v
[Publish: order.cancelled]              [Publish: order.updated]
            |                                       |
            v                                       v
[Consume: sdd.orders.notifications]     [Consume: sdd.orders.notifications]
            |                                       |
            v                                       v
[Render: HTML Cancelled Template]       [Render: HTML Updated Template]
            |                                       |
            v                                       v
[Send: SMTP to customerEmail]           [Send: SMTP to customerEmail]

                      +-------------------+
                      |   Order Deleted   |
                      +---------+---------+
                                |
                                v
               [Publish: order.cancelled to sdd.orders]
                                |
                                v
                [Consume: sdd.orders.notifications]
                                |
                                v
                  [Render: HTML Cancelled Template]
                                |
                                v
                   [Send: SMTP to customerEmail]
```

---

## 4. Invariants & Data Integrity

1. **Email Validity**: `customerEmail` must follow standard email syntax (`local-part@domain`). If invalid, delivery is skipped and logged as an error without consumer failure.
2. **Monetary Consistency**: `totalAmountInCents` must equal the sum of all `lineTotalInCents` values in `orderLines`.
3. **Immutability**: All payload records (`OrderLifecycleEvent`, `OrderPayload`, `AddressPayload`, `OrderLinePayload`, `NotificationMessage`) are immutable Java records.
4. **Idempotency**: Consumers tolerate duplicate events by safely re-rendering and re-sending notifications without corrupting system state.

