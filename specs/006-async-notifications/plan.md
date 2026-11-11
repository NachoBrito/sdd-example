# Implementation Plan: Asynchronous Notifications

**Branch**: `006-async-notifications` | **Date**: 2026-11-11 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/006-async-notifications/spec.md`

---

## Summary

Implement asynchronous notification delivery triggered by order lifecycle events using RabbitMQ, Hexagonal Architecture, and Domain-Driven Design (DDD). The notification system accepts multiple transports, beginning with an SMTP transport that uses dedicated HTML templates for order created, updated, and cancelled events.

Key capabilities:
- Asynchronous event publication when orders are created, updated, or cancelled (`OrderEventPublisherPort` -> `RabbitMqOrderEventPublisher` -> `@RabbitClient`).
- Topic exchange (`sdd.orders`) and queue (`sdd.orders.notifications`) initialized with topic binding `order.*` via `ChannelInitializer`.
- Asynchronous message consumption within the service via `@RabbitListener` (`OrderNotificationConsumer`).
- Decoupled multi-transport architecture via `NotificationTransportPort` and `NotificationService`.
- Native SMTP transport (`SmtpNotificationTransport`) using standard JDK 25 networking (`java.net.Socket`) with RFC 5321 commands, without adding unauthorized third-party libraries.
- Dedicated HTML templates (`SmtpOrderHtmlTemplate`) rendering rich order details, lines, totals, and addresses with HTML escaping.
- Best-effort delivery error handling with structured logging.
- Comprehensive unit tests and integration tests using Micronaut Test Resources with a real RabbitMQ container.

---

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**: Micronaut 5.2.5 (Micronaut RabbitMQ, Micronaut Serde Jackson, Micronaut Validation, Micronaut Data JDBC)

**Storage**: MariaDB with Liquibase migrations (existing)

**Messaging**: RabbitMQ topic exchange `sdd.orders`, queue `sdd.orders.notifications`, routing keys `order.created`, `order.updated`, `order.cancelled`

**Testing**: JUnit 5, Mockito, Micronaut Test Resources (RabbitMQ, MariaDB), ArchUnit, Pitest

**Target Platform**: Linux server / Docker container (Eclipse Temurin JRE 25)

**Project Type**: REST web service with asynchronous messaging

**Performance Goals**: < 100ms p95 for order API operations (decoupled from email delivery); < 5s for notification delivery

**Constraints**:
- Hexagonal Architecture and ArchUnit compliance:
  - `@RabbitClient` strictly in `..adapter.out.messaging..`
  - `@RabbitListener` strictly in `..adapter.in.messaging..`
  - SMTP transport strictly in `..adapter.out.smtp..` (or `..adapter.out.notification..`)
  - Domain models free of framework imports
- No external template engine or email client dependencies (no unauthorized build changes in `build.gradle.kts`).
- Zero compiler warnings (`-Werror`) and zero static analysis warnings (Checkstyle, PMD, SpotBugs).
- Minimum 80% line coverage and 90% mutation score threshold via Pitest.
- Best-effort delivery error policy with structured logging.

**Scale/Scope**:
- 1 inbound message listener (`OrderNotificationConsumer`)
- 1 outbound messaging client (`OrderEventClient`) and publisher (`RabbitMqOrderEventPublisher`)
- 1 channel initializer (`RabbitMqChannelInitializer`)
- 1 outbound transport port (`NotificationTransportPort`) and adapter (`SmtpNotificationTransport`)
- 1 HTML template renderer (`SmtpOrderHtmlTemplate`)
- 1 application service (`NotificationService`)
- 1 outbound event publisher port (`OrderEventPublisherPort`)

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Evaluation | Justification |
|-----------|------------|---------------|
| **1. Minimum Viable Diff** | PASS | Implements strictly what is requested: event publishing, RabbitMQ consumer, multi-transport interface, SMTP transport with HTML template, and tests. No extra frameworks or build dependencies. |
| **2. Ambiguity Halts Execution** | PASS | All three critical questions resolved: notifications for created/updated/cancelled; cancellation on status change and deletion; best-effort delivery failure handling. |
| **3. Do Not Distribute by Default** | PASS | Justified by explicit user requirement: user explicitly mandated asynchronous RabbitMQ event bus. Monolithic process maintained; consumer runs in the same application. |
| **4. Optimize for Deletion, Not Extension** | PASS | Clean, self-contained transport and template classes without open-ended plugin frameworks or generic hierarchies. |
| **5. Make Dependencies Explicit** | PASS | Explicit constructor injection for all services, ports, and listeners. No global state or hidden singletons. |
| **6. Contract at the Boundary** | PASS | Versioned JSON schema in `contracts/order-events.json` and explicit RabbitMQ topology in `contracts/rabbitmq-topology.yaml`. |
| **7. Test the Transformation** | PASS | Pure unit tests for HTML templating and event transformations; integration test for RabbitMQ producer-consumer flow using Micronaut Test Resources. |
| **8. Emit Structured Events** | PASS | Structured logging via Logback for message processing and delivery failures. |
| **9. Recovery Over Prevention** | PASS | Best-effort failure policy prevents poison-pill queue blockages or repeated duplicate email loops. |
| **10. Attention Is Finite** | PASS | Structured error events include event ID, recipient, and cause without noisy unhandled stack traces. |
| **11. Value Is Realized at the User** | PASS | End-to-end integration tests prove end-to-end event flow with real RabbitMQ container. |
| **12. Commands Are Discoverable** | PASS | Standard Gradle tasks: `./gradlew test`, `./gradlew build`. |

---

## Project Structure

### Documentation (this feature)

```text
specs/006-async-notifications/
├── plan.md              # This plan
├── research.md          # Technical decisions and trade-offs
├── data-model.md        # Entities, event contracts, and flows
├── quickstart.md        # Runnable verification guide
├── contracts/           # Boundary contracts
│   ├── order-events.json
│   └── rabbitmq-topology.yaml
└── checklists/
    └── requirements.md  # Requirements quality checklist
```

### Source Code Layout

```text
src/main/java/es/nachobrito/sdd/example/
├── adapter/
│   ├── in/
│   │   └── messaging/
│   │       └── OrderNotificationConsumer.java
│   └── out/
│       ├── messaging/
│       │   ├── OrderEventClient.java
│       │   ├── RabbitMqChannelInitializer.java
│       │   └── RabbitMqOrderEventPublisher.java
│       └── smtp/
│           ├── HtmlEscaper.java
│           ├── SmtpClient.java
│           ├── SmtpNotificationTransport.java
│           └── SmtpOrderHtmlTemplate.java
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   └── ProcessNotificationUseCase.java
│   │   └── out/
│   │       ├── NotificationTransportPort.java
│   │       └── OrderEventPublisherPort.java
│   └── service/
│       ├── NotificationService.java
│       └── OrderService.java (updated to publish events)
└── domain/model/
    ├── NotificationChannel.java
    ├── NotificationMessage.java
    ├── OrderEventType.java
    └── OrderLifecycleEvent.java

src/test/java/es/nachobrito/sdd/example/
├── adapter/
│   ├── in/messaging/
│   │   └── OrderNotificationConsumerTest.java
│   └── out/
│       ├── messaging/
│       │   └── RabbitMqOrderEventPublisherTest.java
│       └── smtp/
│           ├── HtmlEscaperTest.java
│           ├── SmtpNotificationTransportTest.java
│           └── SmtpOrderHtmlTemplateTest.java
├── application/service/
│   └── NotificationServiceTest.java
└── integration/
    └── NotificationFlowIT.java
```

---

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| RabbitMQ event broker | Explicitly required by user prompt (item 4) | In-process synchronous calls would block order transactions and violate user requirement |
