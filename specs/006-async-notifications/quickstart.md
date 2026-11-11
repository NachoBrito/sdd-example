# Quickstart Guide: Asynchronous Notifications

**Feature Branch**: `006-async-notifications`  
**Date**: 2026-11-11  
**Spec**: [spec.md](spec.md)

This guide documents the procedures to validate the asynchronous notification system end-to-end.

---

## 1. Prerequisites

- **Java**: JDK 25 installed (`java -version`).
- **Docker**: Docker daemon active and reachable. Micronaut Test Resources requires Docker to start the MariaDB and RabbitMQ test containers automatically.
- **Repository Setup**: Clean working directory on branch `006-async-notifications`.

---

## 2. Architecture & Contracts Reference

- **Messaging Topology**: [contracts/rabbitmq-topology.yaml](contracts/rabbitmq-topology.yaml)
- **Event Schemas**: [contracts/order-events.json](contracts/order-events.json)
- **Data Model**: [data-model.md](data-model.md)

---

## 3. Validation Scenarios

### Scenario 1: Order Creation Notification
1. **Trigger**: Submit `POST /v1/orders` with valid customer details, addresses, and order lines.
2. **Execution**:
   - The order is saved to the database.
   - An `OrderLifecycleEvent` with `eventType=CREATED` is published to topic exchange `sdd.orders` with routing key `order.created`.
   - The message listener on queue `sdd.orders.notifications` consumes the event.
   - The SMTP transport renders the order confirmation using the HTML created template.
   - The message is transmitted via SMTP to the recipient `customerEmail`.
3. **Verification**:
   - HTTP response returns `201 Created` without waiting for SMTP transport transmission.
   - Mock SMTP receiver records an email addressed to the customer containing order identifier, items, and total.

### Scenario 2: Order Update Notification
1. **Trigger**: Submit `PUT /v1/orders/{id}` with modified customer name or order lines.
2. **Execution**:
   - The order updates in the database.
   - An event with `eventType=UPDATED` is published to `sdd.orders` with routing key `order.updated`.
   - Consumer receives the event and renders the HTML update notification.
   - An email is transmitted to `customerEmail`.
3. **Verification**:
   - HTTP response returns `200 OK`.
   - Mock SMTP receiver records an update notification email.

### Scenario 3: Order Cancellation Notification
1. **Trigger**:
   - Case A: Submit `PUT /v1/orders/{id}` with `status=CANCELLED`.
   - Case B: Submit `DELETE /v1/orders/{id}` for an order in `PENDING` status.
2. **Execution**:
   - The status is updated or the order record is deleted from the database.
   - An event with `eventType=CANCELLED` is published to `sdd.orders` with routing key `order.cancelled`.
   - Consumer receives the event and renders the HTML cancellation notification.
   - An email is transmitted to `customerEmail`.
3. **Verification**:
   - Case A returns `200 OK`; Case B returns `204 No Content`.
   - Mock SMTP receiver records an order cancellation notification email.

### Scenario 4: Transport Delivery Failure Resilience
1. **Trigger**: Submit an order with a malformed customer email or with the SMTP server offline.
2. **Execution**:
   - The order is stored successfully.
   - Event is published to RabbitMQ.
   - The notification consumer encounters a delivery error.
   - The consumer emits a structured error log and acknowledges the message without throwing unhandled exceptions.
3. **Verification**:
   - HTTP response returns `201 Created`.
   - The RabbitMQ message queue remains clean without message blocking or infinite re-queuing.

---

## 4. Test Execution Commands

Run all unit tests covering template rendering, services, and transport logic:
```bash
./gradlew test --tests "*Notification*"
```

Run integration tests using Micronaut Test Resources with auto-managed RabbitMQ and MariaDB containers:
```bash
./gradlew test --tests "*NotificationFlowIT*"
```

Run the complete project build (compilation, tests, ArchUnit, Checkstyle, PMD, SpotBugs, Pitest):
```bash
./gradlew build
```

