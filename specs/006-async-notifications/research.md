# Research & Technical Decisions: Asynchronous Notifications

**Feature Branch**: `006-async-notifications`  
**Date**: 2026-11-11  
**Spec**: [spec.md](spec.md)

This document captures the technical decisions, architectural patterns, and trade-offs for asynchronous notifications.

---

## 1. Asynchronous Messaging with Micronaut RabbitMQ

### Decision
Use `@RabbitClient` on an outbound messaging client (`OrderEventClient`) with a topic exchange (`sdd.orders`) and routing keys `order.created`, `order.updated`, and `order.cancelled`. Implement an inbound message listener (`OrderNotificationConsumer`) annotated with `@RabbitListener` bound to `@Queue("sdd.orders.notifications")`. Initialize exchange, queue, and topic binding via `io.micronaut.rabbitmq.connect.ChannelInitializer`.

### Rationale
- Micronaut RabbitMQ provides declarative, compile-time messaging clients and listeners.
- The project already includes `libs.micronaut.rabbitmq` and ArchUnit rules enforcing:
  - `@RabbitClient` in `adapter.out.messaging`
  - `@RabbitListener` in `adapter.in.messaging`
- `ChannelInitializer` ensures the topic exchange and queue bindings exist on startup without manual broker setup.
- Micronaut Test Resources starts a real RabbitMQ test container automatically when Docker is available.

### Alternatives Considered
- *In-memory application events*: Lacks cross-process decoupling and violates explicit requirement for RabbitMQ.
- *Fanout exchange*: Prevents selective routing or future consumers subscribing to specific order events.

---

## 2. Multi-Transport Notification Architecture

### Decision
Define an outbound port interface `NotificationTransportPort` in `es.nachobrito.sdd.example.application.port.out`. The `NotificationService` in `application.service` receives order events and dispatches them across all registered `NotificationTransportPort` implementations. The initial implementation is `SmtpNotificationTransport` located in `es.nachobrito.sdd.example.adapter.out.smtp`.

### Rationale
- Conforms to Hexagonal Architecture and DDD separation of concerns.
- Supports multiple transports (user requirement 1). Adding a new transport (e.g. SMS, Webhook) requires adding a new adapter implementing `NotificationTransportPort` without modifying the core notification logic.
- Transports declare their supported channels and formats.

### Alternatives Considered
- *Single hardcoded SMTP service*: Violates requirement 1 ("accepts multiple transports").
- *Dynamic plugin mechanism*: Adds unnecessary reflection and configuration complexity, violating Principle 4 (Optimize for Deletion, Not Extension).

---

## 3. Dedicated Templates and HTML Email Rendering

### Decision
Each transport encapsulates its own message templates. For SMTP, implement an HTML template renderer (`SmtpOrderHtmlTemplate`) that formats order created, updated, and cancelled messages. The renderer produces clean HTML tables displaying order ID, customer details, line items, totals, currency, and delivery address. All dynamic values are safely escaped against HTML injection.

### Rationale
- Satisfies user requirement 3 ("Each transport uses its own template. The SMTP transport will have an html template").
- Keeping templating logic within the adapter keeps the domain and application layers free from transport-specific presentation formats.
- Avoids adding heavy external template engines (e.g., FreeMarker, Thymeleaf), which would violate AGENTS.md rule 2 ("Do not change the build configuration without explicit authorization from the user").

### Alternatives Considered
- *External template engines (Thymeleaf, FreeMarker)*: Requires new dependencies in `build.gradle.kts` and increases attack surface.
- *Plain text email*: Violates user requirement 3.

---

## 4. SMTP Client Implementation

### Decision
Implement a lightweight, non-blocking SMTP transport client (`SmtpClient`) using standard JDK 25 networking (`java.net.Socket`). The client performs the standard RFC 5321 text exchange (`HELO`, `MAIL FROM`, `RCPT TO`, `DATA`, `QUIT`) with configurable host, port, timeout, and sender address (`notification.smtp.host`, `notification.smtp.port`, `notification.smtp.from`).

### Rationale
- Java 25 standard library networking delivers high performance with zero external dependencies.
- Adheres strictly to Principle 1 (Minimum Viable Diff) and AGENTS.md rule 2 (no unauthorized build file modifications).
- Enables exact end-to-end testing against mock sockets or test mail receivers.

### Alternatives Considered
- *Adding Jakarta Mail / JavaMail dependency*: Requires modifying `build.gradle.kts` without authorization.
- *Mock-only transport in production*: Unacceptable for production deployments.

---

## 5. Event Schema and Boundary Contracts

### Decision
Define an immutable record `OrderLifecycleEvent` representing the contract at the messaging boundary. The event includes:
- `eventId`: UUID
- `eventType`: `OrderEventType` (`CREATED`, `UPDATED`, `CANCELLED`)
- `timestamp`: Instant
- `order`: `OrderPayload` snapshot containing ID, customer details, status, totals, currency, lines, and addresses.
Serialize to JSON using Micronaut Serde Jackson.

### Rationale
- Principle 6 (Contract at the Boundary): Message queues must have versioned, explicit schemas.
- Self-contained event payloads prevent race conditions where the consumer queries a database row that is already deleted or in transit.

### Alternatives Considered
- *Sending internal domain model `Order` directly*: Leaks domain model into the serialization wire format.
- *Sending only `orderId`*: Forces the notification consumer to query the order database synchronously, creating tight coupling and failing if the order is already deleted.

---

## 6. Testing Strategy with Micronaut Test Resources

### Decision
1. **Unit Tests**:
   - `OrderServiceTest`: Verify that `OrderService` invokes `OrderEventPublisherPort` with correct events during creation, update, and deletion.
   - `NotificationServiceTest`: Verify that `NotificationService` formats and delegates messages to all active transports.
   - `SmtpOrderHtmlTemplateTest`: Verify HTML rendering, table formatting, and HTML escaping for all three lifecycle events.
   - `SmtpNotificationTransportTest`: Verify SMTP socket commands and header formatting.
2. **Integration Tests**:
   - `NotificationFlowIT`: Uses Micronaut Test Resources to run against a real RabbitMQ test container. Starts a lightweight mock SMTP server socket in the test. Creates an order via the REST API or `OrderUseCase`, verifies message publishing to RabbitMQ, verifies consumer execution, and asserts that the mock SMTP server receives the rendered HTML email.

### Rationale
- Fulfills user requirement 5 ("cover this with integration tests using Micronaut test resources").
- Verifies real RabbitMQ serialization, routing, queue binding, and consumer acknowledgment.

