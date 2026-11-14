# Research: Complete Telemetry Dashboard

**Feature**: `011-telemetry-dashboard` | **Date**: 2026-11-14

## 1. Dashboard Renaming and Provisioning

- **Decision**: Rename the dashboard title in `config/grafana/dashboards/http-service.json` from `"SDD Example - HTTP Service Metrics"` to `"SDD Example - Telemetry"` and keep the existing `uid` (`"sdd-http-service"`) or rename the file to `telemetry.json` while keeping the same `uid` so existing links stay valid.
- **Rationale**: Grafana provisions dashboards from `/var/lib/grafana/dashboards` (configured in `config/grafana/provisioning/dashboards/dashboards.yml`). Updating the JSON file in place (or replacing `http-service.json` with `telemetry.json` with the same `uid`) ensures no duplicate dashboard appears under the old title.
- **Alternatives considered**: Creating a second dashboard file `telemetry.json` alongside `http-service.json`. Rejected because it would leave the old `"SDD Example - HTTP Service Metrics"` dashboard in Grafana.

## 2. Database Metrics (Connection Pool and Query Duration)

- **Decision**: Use the built-in HikariCP Micrometer integration for connection pool metrics and add a Micronaut AOP `@Around` `MethodInterceptor` (or `@Timed`) on the JDBC persistence adapters to record query execution duration.
- **Rationale**:
  - The application already includes `micronaut-jdbc-hikari`, `micronaut-micrometer-core`, and `micronaut-micrometer-registry-otlp`. HikariCP automatically registers `hikaricp_connections_*` gauges and `hikaricp_connections_acquire_*` timers when a `MeterRegistry` bean exists.
  - Micronaut Data JDBC does not emit per-query duration timers by default. Adding a Micronaut AOP interceptor on the persistence adapter boundary follows `AGENTS.md` Rule 3 (AOP for crosscutting concerns) without adding new Gradle dependencies (`AGENTS.md` Rule 2).
- **Alternatives considered**: Adding `datasource-micrometer` as a new dependency in `gradle/libs.versions.toml` and `build.gradle.kts`. Rejected because `AGENTS.md` Rule 2 prohibits changing build configuration without explicit user authorization, and AOP instrumentation satisfies the requirement cleanly.

## 3. RabbitMQ Metrics (Queue Size, Message Count, Throughput)

- **Decision**: Enable the built-in `rabbitmq_prometheus` plugin on the `rabbitmq` container in `docker-compose.yml` and add a Prometheus scrape job for `rabbitmq:15692` in `config/prometheus/prometheus.yml`. Optionally complement with AOP counters/timers on the outbound publisher and inbound consumer.
- **Rationale**:
  - `docker-compose.yml` uses `rabbitmq:3.13-management`, which bundles `rabbitmq_prometheus`. Enabling it exposes authoritative broker-side metrics (`rabbitmq_queue_messages`, `rabbitmq_queue_messages_ready`, `rabbitmq_queue_messages_unacked`, `rabbitmq_queue_messages_published_total`, `rabbitmq_queue_messages_delivered_total`, `rabbitmq_queue_messages_ack_total`, `rabbitmq_queue_consumers`).
  - Client-side Micrometer instrumentation cannot observe broker queue depth (`ready` / `unacked` message counts) without polling the management API.
- **Alternatives considered**: Writing a scheduled poller in Java to query the RabbitMQ HTTP management API. Rejected because it violates Constitution Principle 1 (Minimum Viable Diff) and duplicates functionality built into the RabbitMQ container.

## 4. SMTP Metrics (Messages Sent, Failed, Duration)

- **Decision**: Instrument `SmtpClient.sendEmail(...)` using a Micronaut AOP `@Around` `MethodInterceptor` that records `smtp_messages_sent_total`, `smtp_messages_failed_total` (or `smtp_messages_total` with `outcome="sent"|"failed"`), and `smtp_send_duration` via Micrometer `MeterRegistry`.
- **Rationale**:
  - `SmtpClient` is a `@Singleton` bean whose `sendEmail(...)` method throws `IOException` on network or protocol failure and returns normally on success.
  - Wrapping `SmtpClient` with an AOP interceptor keeps `SmtpClient` and `SmtpNotificationTransport` free of telemetry boilerplate (`AGENTS.md` Rule 3) and ensures every send attempt is counted and timed.
- **Alternatives considered**: Injecting `MeterRegistry` directly into `SmtpNotificationTransport.sendNotification(...)`. Rejected because `AGENTS.md` Rule 3 mandates Micronaut AOP for crosscutting concerns such as metrics.

