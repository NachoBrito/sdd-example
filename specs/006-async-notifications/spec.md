# Feature Specification: Asynchronous Notifications

**Feature Branch**: `006-async-notifications`

**Created**: 2026-11-11

**Status**: Draft

**Input**: User description: "Add asyncrhronous notifications: 1. Create a notification service that accepts multiple transports, SMTP initially. 2. When an order is created, the notification service sends the customer the order details. 3. Each transport uses its own template. The SMTP transport will have an html template. 4. We want this to be fully asyncrhronous using RabbitMQ: - The notification service should publish events to RabbitMQ when an order is created, updated or cancelled. - The events should be consumed by a new RabbitMQ consumer that will send the notifications. - The consumer is part of the same notification service. 5. Besides unit testsing, cover this with integration tests using Micronaut test resources."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Order Lifecycle Notifications (Priority: P1)

A customer places, updates, or cancels an order in the store. The system sends an email notification with the event details to the customer.

**Why this priority**:
Immediate confirmation of order changes is critical for customer trust. It informs the customer about their order state.

**Independent Test**:
A client triggers an order creation, update, or cancellation. The test verifies that the notification service delivers the corresponding HTML email to the customer.

**Acceptance Scenarios**:

1. **Given** a newly created order with valid details, **When** the creation event is processed, **Then** the customer receives an HTML email with order details.
2. **Given** an existing order is updated, **When** the update event is processed, **Then** the customer receives an HTML email with updated order details.
3. **Given** an existing order is cancelled, **When** the cancellation event is processed, **Then** the customer receives an HTML email confirming order cancellation.
4. **Given** an order event with an invalid customer email address, **When** the notification service attempts delivery, **Then** the service logs the delivery failure.

---

### User Story 2 - Asynchronous Order Event Dispatching (Priority: P2)

The system processes order lifecycle events asynchronously without blocking the primary business transactions.

**Why this priority**:
Asynchronous dispatching decouples database operations from notification delivery. This prevents external delivery delays from slowing down client requests.

**Independent Test**:
A client creates, updates, or cancels an order. The test verifies that the system emits the event and returns an immediate response to the client.

**Acceptance Scenarios**:

1. **Given** a new order is saved, **When** the creation transaction finishes, **Then** the system emits an order creation event for asynchronous consumption.
2. **Given** an existing order is updated, **When** the update transaction finishes, **Then** the system emits an order updated event for asynchronous consumption.
3. **Given** an order status updates to CANCELLED, **When** the update transaction finishes, **Then** the system emits an order cancelled event for asynchronous consumption.
4. **Given** an existing order is deleted, **When** the deletion finishes, **Then** the system emits an order cancelled event for asynchronous consumption.

---

### User Story 3 - Multi-Transport and Template Customization (Priority: P3)

The notification service supports multiple delivery transports. Each transport renders messages with its own dedicated template.

**Why this priority**:
Different communication channels require distinct message structures. For example, email uses rich HTML, while other channels use plain text.

**Independent Test**:
The test delivers a notification through the SMTP transport. It verifies that the rendered output matches the HTML template structure.

**Acceptance Scenarios**:

1. **Given** an order notification event, **When** dispatched through the SMTP transport, **Then** the message body is rendered using the configured HTML template.
2. **Given** an unsupported transport identifier, **When** delivery is requested, **Then** the notification service records a transport configuration error.

---

### Edge Cases

- What happens when the email transport is temporarily unavailable? The consumer records a structured error log and acknowledges the event without retries.
- What happens when the customer email address is malformed? The system records a delivery validation failure and does not stop the consumer.
- What happens when an order contains multiple lines and addresses? The HTML template formats all items and address lines clearly.
- What happens when a duplicate event is received? The system processes the event idempotently.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST publish an event whenever an order is created.
- **FR-002**: The system MUST publish an event whenever an order is updated.
- **FR-003**: The system MUST publish an event whenever an order is cancelled.
- **FR-004**: The notification service MUST consume order lifecycle events asynchronously.
- **FR-005**: The notification service MUST support multiple notification transports.
- **FR-006**: The notification service MUST provide an SMTP transport as the initial delivery mechanism.
- **FR-007**: Each notification transport MUST use a dedicated template format.
- **FR-008**: The SMTP transport MUST render notifications using an HTML template.
- **FR-009**: When an order creation event is consumed, the notification service MUST send the order details to the customer email address.
- **FR-010**: The order details in the notification MUST include order identifier, date, customer name, total amount, currency, order lines, and delivery address.
- **FR-011**: The system MUST send HTML email notifications to the customer when an order is created, updated, or cancelled, using dedicated templates for each event type.
- **FR-012**: The system MUST emit an order cancelled event when an order status changes to CANCELLED and when an order is deleted.
- **FR-013**: When a notification delivery transport encounters a failure, the consumer MUST record a structured error log and acknowledge the event without retries.
- **FR-014**: The notification service MUST not block order persistence operations during event publishing or notification delivery.
- **FR-015**: The notification service MUST validate recipient email format before attempting SMTP delivery.

### Key Entities *(include if feature involves data)*

- **OrderLifecycleEvent**:
  - `eventId`: Unique identifier of the event.
  - `eventType`: Type of the event (CREATED, UPDATED, CANCELLED).
  - `timestamp`: Time when the event occurred.
  - `order`: Complete snapshot of the order data.

- **NotificationMessage**:
  - `recipient`: Destination address of the customer.
  - `subject`: Header or subject line of the notification.
  - `content`: Rendered body content.
  - `format`: Format of the body (e.g., HTML, TEXT).

- **NotificationTransport**:
  - `transportType`: Channel type identifier (e.g., SMTP).
  - `template`: Template associated with this transport channel.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of order creation, update, and cancellation events trigger asynchronous processing.
- **SC-002**: The customer receives an HTML notification email within 5 seconds of an order event under standard conditions.
- **SC-003**: Order creation, update, and deletion API requests complete without waiting for notification transport delivery.
- **SC-004**: 100% of rendered SMTP notification emails contain accurate order lines, totals, currency, and recipient details.
- **SC-005**: Transport delivery errors do not roll back or compromise stored order transactions.

## Assumptions

- The order notification email is addressed to the `customerEmail` specified on the order.
- The SMTP sender address is provided through application configuration.
- The initial deployment environment provides an accessible message broker and SMTP gateway.
- The consumer runs as an asynchronous background component within the notification service.
- If an order has no items or an invalid recipient address, the notification attempt is marked as failed without retrying invalid data.
- The system sends dedicated HTML email notifications for all three lifecycle events (created, updated, cancelled).
- An order cancellation event is emitted when an order status changes to CANCELLED and when an order is deleted.
- Notification delivery uses best-effort delivery: failed transmissions are recorded in structured logs without retries or message re-queuing.
