# Data Model: Complete Telemetry Dashboard

**Feature**: `011-telemetry-dashboard` | **Date**: 2026-11-14

## Telemetry Entities

### 1. Telemetry Dashboard
- **Title**: `"SDD Example - Telemetry"`
- **UID**: `"sdd-http-service"` (preserved to maintain existing URLs)
- **Sections (Grafana Rows)**:
  - `HTTP & Runtime`: HTTP Request Rate, HTTP Status Code Distribution, HTTP Response Latency, JVM Heap Memory Usage
  - `Database`: Connection Pool Size (Active, Idle, Max, Pending), Connection Acquisition Time, Query Execution Duration
  - `RabbitMQ`: Queue Size (Total, Ready, Unacknowledged), Message Throughput (Published, Delivered, Acknowledged), Active Consumers
  - `SMTP`: Email Delivery Rate (Sent vs. Failed), Total Emails Sent / Failed, Email Send Duration

### 2. Database Metrics
- **Pool Gauges** (from HikariCP Micrometer binder):
  - `hikaricp_connections_active`: Current number of active connections.
  - `hikaricp_connections_idle`: Current number of idle connections.
  - `hikaricp_connections_max`: Configured maximum pool size.
  - `hikaricp_connections_pending`: Threads waiting for a connection.
- **Pool & Query Timers**:
  - `hikaricp_connections_acquire_milliseconds`: Time spent waiting to acquire a connection.
  - `db_query_duration_milliseconds`: Execution time of persistence adapter operations, tagged by `adapter`, `method`, and `outcome` (`success` | `error`).

### 3. RabbitMQ Metrics
- **Queue Gauges** (from `rabbitmq_prometheus`):
  - `rabbitmq_queue_messages`: Total messages in queue.
  - `rabbitmq_queue_messages_ready`: Messages ready for delivery.
  - `rabbitmq_queue_messages_unacked`: Messages delivered but not yet acknowledged.
  - `rabbitmq_queue_consumers`: Active consumers attached to the queue.
- **Message Counters**:
  - `rabbitmq_queue_messages_published_total`: Cumulative messages published.
  - `rabbitmq_queue_messages_delivered_total`: Cumulative messages delivered to consumers.
  - `rabbitmq_queue_messages_ack_total`: Cumulative messages acknowledged.

### 4. SMTP Metrics
- **Delivery Counters** (from Micronaut AOP interceptor on `SmtpClient`):
  - `smtp_messages_total`: Total outbound email attempts, tagged by `outcome` (`sent` | `failed`).
- **Delivery Timer**:
  - `smtp_send_duration_milliseconds`: Duration of each SMTP send attempt, tagged by `outcome` (`sent` | `failed`).

