# Tasks: Asynchronous Notifications

**Input**: Design documents from `specs/006-async-notifications/`  
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/`

## Format: `[ID] [P?] [Story?] Description with file path`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (`[US1]`, `[US2]`, `[US3]`)
- Every task includes the exact file path and quoted constraints

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Messaging topology configuration and application settings

- [X] T001 Create RabbitMQ channel initializer in `src/main/java/es/nachobrito/sdd/example/adapter/out/messaging/RabbitMqChannelInitializer.java` declaring topic exchange `sdd.orders` (durable, non-auto-delete), durable queue `sdd.orders.notifications`, and binding `order.*`
- [X] T002 Update application configuration in `src/main/resources/application.yml` with default SMTP settings (`notification.smtp.host: localhost`, `notification.smtp.port: 2525`, `notification.smtp.from: no-reply@example.com`)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core domain models, boundary event schemas, and port interfaces

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T003 [P] Create `OrderEventType` enum in `src/main/java/es/nachobrito/sdd/example/domain/model/OrderEventType.java` with constants `CREATED`, `UPDATED`, `CANCELLED`
- [X] T004 [P] Create `NotificationChannel` enum in `src/main/java/es/nachobrito/sdd/example/domain/model/NotificationChannel.java` with constant `SMTP`
- [X] T005 [P] Create `NotificationMessage` value object in `src/main/java/es/nachobrito/sdd/example/domain/model/NotificationMessage.java` with constraints: `recipient` non-blank valid email, `subject` non-blank max 200 chars, `body` non-blank, `channel` non-null `NotificationChannel`
- [X] T006 [P] Create messaging boundary contract records `OrderLifecycleEvent`, `OrderPayload`, `AddressPayload`, and `OrderLinePayload` in `src/main/java/es/nachobrito/sdd/example/domain/model/OrderLifecycleEvent.java` matching schema in `specs/006-async-notifications/contracts/order-events.json`
- [X] T007 [P] Create outbound port `OrderEventPublisherPort` in `src/main/java/es/nachobrito/sdd/example/application/port/out/OrderEventPublisherPort.java` defining `publishOrderCreated(Order order)`, `publishOrderUpdated(Order order)`, and `publishOrderCancelled(Order order)`
- [X] T008 [P] Create outbound port `NotificationTransportPort` in `src/main/java/es/nachobrito/sdd/example/application/port/out/NotificationTransportPort.java` defining `NotificationChannel channel()` and `void sendNotification(OrderLifecycleEvent event)`
- [X] T009 [P] Create inbound use case port `ProcessNotificationUseCase` in `src/main/java/es/nachobrito/sdd/example/application/port/in/ProcessNotificationUseCase.java` defining `void processOrderEvent(OrderLifecycleEvent event)`

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Order Lifecycle Notifications (Priority: P1) 🎯 MVP

**Goal**: Render and deliver HTML email notifications with order details when order events occur.

**Independent Test**: Calling `NotificationService.processOrderEvent` with an `OrderLifecycleEvent` renders the corresponding HTML template and transmits it via SMTP to the customer email address.

### Tests for User Story 1

- [X] T010 [P] [US1] Create unit tests in `src/test/java/es/nachobrito/sdd/example/adapter/out/smtp/HtmlEscaperTest.java` verifying escaping of HTML characters (`&`, `<`, `>`, `"`, `'`)
- [X] T011 [P] [US1] Create unit tests in `src/test/java/es/nachobrito/sdd/example/adapter/out/smtp/SmtpOrderHtmlTemplateTest.java` verifying HTML table layout and dynamic content for `CREATED`, `UPDATED`, and `CANCELLED` events
- [X] T012 [P] [US1] Create unit tests in `src/test/java/es/nachobrito/sdd/example/application/service/NotificationServiceTest.java` verifying that `NotificationService` delegates event processing to registered transports

### Implementation for User Story 1

- [X] T013 [P] [US1] Create `HtmlEscaper` in `src/main/java/es/nachobrito/sdd/example/adapter/out/smtp/HtmlEscaper.java` providing HTML entity sanitization
- [X] T014 [P] [US1] Create `SmtpOrderHtmlTemplate` in `src/main/java/es/nachobrito/sdd/example/adapter/out/smtp/SmtpOrderHtmlTemplate.java` rendering dedicated HTML templates for `CREATED` (order details confirmation), `UPDATED` (order update notice), and `CANCELLED` (cancellation notice)
- [X] T015 [P] [US1] Create `SmtpClient` in `src/main/java/es/nachobrito/sdd/example/adapter/out/smtp/SmtpClient.java` implementing RFC 5321 text exchange (`HELO`, `MAIL FROM`, `RCPT TO`, `DATA`, `QUIT`) over `java.net.Socket`
- [X] T016 [US1] Create `SmtpNotificationTransport` in `src/main/java/es/nachobrito/sdd/example/adapter/out/smtp/SmtpNotificationTransport.java` implementing `NotificationTransportPort` using `SmtpOrderHtmlTemplate` and `SmtpClient`
- [X] T017 [US1] Implement `NotificationService` in `src/main/java/es/nachobrito/sdd/example/application/service/NotificationService.java` implementing `ProcessNotificationUseCase` and delegating to registered `NotificationTransportPort` implementations

**Checkpoint**: User Story 1 is functional and testable independently (MVP ready)

---

## Phase 4: User Story 2 - Asynchronous Order Event Dispatching (Priority: P2)

**Goal**: Publish order lifecycle events to RabbitMQ asynchronously and consume them to trigger notifications without blocking API responses.

**Independent Test**: Creating, updating, or deleting orders via `OrderService` publishes events to `sdd.orders`; consumer receives the event on `sdd.orders.notifications` and invokes the notification use case.

### Tests for User Story 2

- [X] T018 [P] [US2] Create unit tests in `src/test/java/es/nachobrito/sdd/example/adapter/out/messaging/RabbitMqOrderEventPublisherTest.java` verifying domain-to-contract event mapping and client invocation
- [X] T019 [P] [US2] Update unit tests in `src/test/java/es/nachobrito/sdd/example/application/service/OrderServiceCommandTest.java`, `OrderServiceDeleteTest.java`, and `OrderServiceQueryTest.java` verifying `OrderEventPublisherPort` interactions
- [X] T020 [P] [US2] Create unit tests in `src/test/java/es/nachobrito/sdd/example/adapter/in/messaging/OrderNotificationConsumerTest.java` verifying consumer receipt and delegation to `ProcessNotificationUseCase`

### Implementation for User Story 2

- [X] T021 [P] [US2] Create declarative messaging client `OrderEventClient` in `src/main/java/es/nachobrito/sdd/example/adapter/out/messaging/OrderEventClient.java` annotated with `@RabbitClient("sdd.orders")` and routing methods for `order.created`, `order.updated`, and `order.cancelled`
- [X] T022 [US2] Implement outbound adapter `RabbitMqOrderEventPublisher` in `src/main/java/es/nachobrito/sdd/example/adapter/out/messaging/RabbitMqOrderEventPublisher.java` implementing `OrderEventPublisherPort` using `OrderEventClient`
- [X] T023 [US2] Update `OrderService` in `src/main/java/es/nachobrito/sdd/example/application/service/OrderService.java` to inject `OrderEventPublisherPort` and publish events on `createOrder`, `updateOrder` (cancellation vs update), and `deleteOrder` (cancellation)
- [X] T024 [US2] Create inbound consumer `OrderNotificationConsumer` in `src/main/java/es/nachobrito/sdd/example/adapter/in/messaging/OrderNotificationConsumer.java` annotated with `@RabbitListener` listening on `@Queue("sdd.orders.notifications")` and calling `ProcessNotificationUseCase`

**Checkpoint**: User Stories 1 and 2 are functional and testable independently

---

## Phase 5: User Story 3 - Multi-Transport and Template Customization (Priority: P3)

**Goal**: Support multiple notification delivery transports with isolated execution and best-effort delivery failure handling.

**Independent Test**: Dispatching an event across multiple transports executes each transport independently; delivery errors are recorded in structured logs without throwing unhandled exceptions.

### Tests for User Story 3

- [X] T025 [P] [US3] Add unit tests in `src/test/java/es/nachobrito/sdd/example/application/service/NotificationServiceTest.java` verifying multi-transport iteration and failure isolation
- [X] T026 [P] [US3] Create unit tests in `src/test/java/es/nachobrito/sdd/example/adapter/out/smtp/SmtpNotificationTransportTest.java` verifying delivery failure resilience (offline SMTP server, socket timeout, malformed email) and structured error logging

### Implementation for User Story 3

- [X] T027 [US3] Implement transport iteration isolation and structured error logging in `src/main/java/es/nachobrito/sdd/example/application/service/NotificationService.java`
- [X] T028 [US3] Add error handling and structured logging for connection timeouts and socket failures in `src/main/java/es/nachobrito/sdd/example/adapter/out/smtp/SmtpNotificationTransport.java`

**Checkpoint**: All user stories functional, isolated, and resilient

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: End-to-end integration validation and static analysis verification

- [X] T029 [P] Create end-to-end integration test `NotificationFlowIT` in `src/test/java/es/nachobrito/sdd/example/integration/NotificationFlowIT.java` using Micronaut Test Resources with a real RabbitMQ container and mock SMTP server socket
- [X] T030 Execute quickstart validation scenarios in `specs/006-async-notifications/quickstart.md` and verify code coverage and ArchUnit checks via `./gradlew build`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately.
- **Foundational (Phase 2)**: Depends on Setup (Phase 1) - BLOCKS all user stories.
- **User Story 1 (Phase 3)**: Depends on Foundational completion.
- **User Story 2 (Phase 4)**: Depends on Foundational completion and US1 interfaces.
- **User Story 3 (Phase 5)**: Depends on US1 and US2 completion.
- **Polish (Phase 6)**: Depends on all user story phases being complete.

### User Story Dependencies

- **User Story 1 (P1)**: Independent notification domain model, templates, and SMTP transport. MVP ready.
- **User Story 2 (P2)**: Connects RabbitMQ publishing and consumption to the notification domain.
- **User Story 3 (P3)**: Hardens multi-transport isolation and best-effort failure handling.

### Parallel Opportunities

- Within Phase 2: T003, T004, T005, T006, T007, T008, T009 can all be created in parallel.
- Within Phase 3: Tests (T010, T011, T012) and components (T013, T014, T015) can be created in parallel.
- Within Phase 4: Tests (T018, T019, T020) and client (T021) can be created in parallel.
- Within Phase 5: Tests (T025, T026) can be created in parallel.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001, T002)
2. Complete Phase 2: Foundational (T003 - T009)
3. Complete Phase 3: User Story 1 (T010 - T017)
4. **Validate MVP**: Test HTML template rendering and SMTP transport delivery in isolation.

### Incremental Delivery

1. Complete Setup + Foundational -> Foundation ready.
2. Complete User Story 1 -> MVP delivery with HTML email generation and SMTP transport.
3. Complete User Story 2 -> Wire asynchronous RabbitMQ producer and consumer to order operations.
4. Complete User Story 3 -> Multi-transport isolation and delivery error resilience.
5. Complete Polish -> End-to-end integration test with Test Resources and full `./gradlew build`.

