# Feature Specification: Complete Telemetry Dashboard

**Feature Branch**: `011-telemetry-dashboard`

**Created**: 2026-11-14

**Status**: Draft

**Input**: User description: "Rename the grafana dashboard 'SDD Example - HTTP Service Metrics' to 'SDD Example - Telemetry'. Add database metrics to the telemetry dashboard (connection pool size, query duration, etc.). Add RabbitMQ metrics to the telemetry dashboard (queue size, message count, etc.). Add SMTP metrics to the telemetry dashboard (messages sent, failed, etc.)."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Rename and Retain Core Service Telemetry (Priority: P1)

An operator opens the monitoring portal to inspect overall system health. The operator finds the dashboard under the name "SDD Example - Telemetry" instead of "SDD Example - HTTP Service Metrics". All existing request rate, status code, response latency, and memory panels remain visible and functional.

**Why this priority**: The dashboard rename establishes the unified entry point for all service observability without breaking existing HTTP and runtime visibility.

**Independent Test**: Open the monitoring portal, locate the "SDD Example - Telemetry" dashboard, verify the old title no longer appears, and confirm all existing HTTP and memory panels display live data.

**Acceptance Scenarios**:

1. **Given** the monitoring portal is running, **When** an operator views the dashboard list, **Then** the dashboard appears as "SDD Example - Telemetry" and "SDD Example - HTTP Service Metrics" does not appear.
2. **Given** an operator opens "SDD Example - Telemetry", **When** the service handles requests, **Then** the existing HTTP request rate, status distribution, response latency, and heap memory panels display current data.

---

### User Story 2 - Monitor Database Pool and Query Performance (Priority: P1)

An operator investigates slow responses or database saturation during an incident. The operator views the database section of the "SDD Example - Telemetry" dashboard to inspect connection pool utilization, pending connection requests, and query execution duration.

**Why this priority**: Database bottlenecks directly cause request latency and service outages, so operators need immediate diagnostic visibility into pool capacity and query times.

**Independent Test**: Execute database-backed service requests, open the database section of the telemetry dashboard, and verify that connection pool counts and query duration metrics update to reflect the activity.

**Acceptance Scenarios**:

1. **Given** the service is connected to the database, **When** an operator views the database metrics section, **Then** the dashboard displays active, idle, and maximum connection pool sizes along with pending connection wait counts.
2. **Given** the service executes database queries, **When** an operator inspects query performance, **Then** the dashboard displays query execution duration percentiles or averages and connection acquisition wait times.

---

### User Story 3 - Monitor Message Broker Queue and Throughput (Priority: P2)

An operator checks asynchronous notification processing and message backlog health. The operator views the RabbitMQ section of the "SDD Example - Telemetry" dashboard to inspect queue depth, unacknowledged messages, and message publish and consume rates.

**Why this priority**: Asynchronous messaging decouples user actions from background delivery, so operators must detect growing backlogs or stalled consumers before notifications are delayed.

**Independent Test**: Trigger actions that publish and consume messages through the broker, open the RabbitMQ section of the telemetry dashboard, and verify that queue size and message rate panels reflect the traffic.

**Acceptance Scenarios**:

1. **Given** messages are queued in the broker, **When** an operator views the RabbitMQ metrics section, **Then** the dashboard displays current queue size, ready messages, and unacknowledged message counts.
2. **Given** the service publishes and consumes messages, **When** an operator inspects throughput, **Then** the dashboard displays message publish, delivery, and acknowledgment rates alongside active consumer counts.

---

### User Story 4 - Monitor Email Delivery Outcomes (Priority: P2)

An operator verifies that outbound email notifications are delivered reliably and detects delivery failures early. The operator views the SMTP section of the "SDD Example - Telemetry" dashboard to see sent message counts, failed delivery counts, and delivery duration.

**Why this priority**: Email delivery failures are silent to end users unless operators track sent versus failed counts and delivery latency.

**Independent Test**: Trigger email notifications (both successful and simulated failure cases), open the SMTP section of the telemetry dashboard, and verify that sent counts, failure counts, and send duration update accurately.

**Acceptance Scenarios**:

1. **Given** the service sends outbound emails, **When** an operator views the SMTP metrics section, **Then** the dashboard displays the count and rate of successfully sent messages.
2. **Given** an outbound email fails to send, **When** an operator views the SMTP metrics section, **Then** the dashboard displays the count and rate of failed deliveries and the send attempt duration.

---

### Edge Cases

- What happens when a subsystem (database, RabbitMQ, or SMTP) has zero recent traffic? The dashboard panels display zero or empty-state indicators without showing query errors.
- What happens when the SMTP server is unreachable or rejects messages? Failed delivery counters increment and appear on the dashboard without crashing the service.
- What happens when the database connection pool reaches maximum capacity? Pending connection requests and wait times become visible on the dashboard so operators can diagnose saturation.
- What happens when a RabbitMQ queue has messages that are not consumed? Queue size and ready message counts remain elevated and visible until consumers process them.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST rename the dashboard titled "SDD Example - HTTP Service Metrics" to "SDD Example - Telemetry".
- **FR-002**: The "SDD Example - Telemetry" dashboard MUST retain all existing HTTP request rate, HTTP status distribution, HTTP response latency, and JVM heap memory panels.
- **FR-003**: The system MUST collect and expose database connection pool metrics, including active connections, idle connections, maximum pool size, and pending threads waiting for a connection.
- **FR-004**: The system MUST collect and expose database query execution metrics, including query duration and connection acquisition time.
- **FR-005**: The "SDD Example - Telemetry" dashboard MUST display curated panels for database connection pool utilization and query performance.
- **FR-006**: The system MUST collect and expose RabbitMQ messaging metrics, including queue depth (ready and unacknowledged messages), message publish rate, message consume/delivery rate, and active consumer count.
- **FR-007**: The "SDD Example - Telemetry" dashboard MUST display curated panels for RabbitMQ queue size and message throughput.
- **FR-008**: The system MUST collect and expose SMTP email delivery metrics, including total messages sent, total messages failed, and send operation duration.
- **FR-009**: The "SDD Example - Telemetry" dashboard MUST display curated panels for SMTP sent messages, failed messages, and delivery latency.

### Key Entities

- **Telemetry Dashboard**: The unified operational view titled "SDD Example - Telemetry" organized into diagnostic sections for HTTP/Runtime, Database, RabbitMQ, and SMTP.
- **Database Telemetry**: Measurements of connection pool capacity, connection wait queues, and query execution times.
- **Message Broker Telemetry**: Measurements of RabbitMQ queue depth, consumer presence, and message publish/consume rates.
- **Email Delivery Telemetry**: Measurements of outbound SMTP message outcomes (sent, failed) and send duration.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Operators locate the unified dashboard under the exact name "SDD Example - Telemetry" within 5 seconds of opening the monitoring portal, with zero duplicate dashboards remaining under the old title.
- **SC-002**: All four telemetry domains (HTTP/Runtime, Database, RabbitMQ, and SMTP) render without panel errors on the "SDD Example - Telemetry" dashboard when the system runs normally.
- **SC-003**: After executing a workload that triggers database queries, message queue events, and email deliveries, 100% of the new panels reflect updated measurements within 30 seconds.
- **SC-004**: Operators can identify a simulated database pool saturation, stalled message queue, or SMTP delivery failure from the dashboard within 1 minute without inspecting raw application logs.

## Assumptions

- The project already has a working metrics collection and dashboard provisioning pipeline from earlier features.
- Existing dashboard layout conventions (such as refresh interval and section grouping) apply to the new panels.
- Standard operational defaults are used where specific metric lists were summarized as "etc.":
  - Database includes pool counts (active, idle, max, pending) and timing (query duration, connection acquisition time).
  - RabbitMQ includes queue size (ready, unacknowledged), publish/consume rates, and consumer count.
  - SMTP includes sent count, failed count, and send duration.
- Each panel answers a specific diagnostic question during incidents rather than displaying redundant raw counters.

