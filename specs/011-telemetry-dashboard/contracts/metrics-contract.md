# Metrics Contract: SDD Example - Telemetry

**Feature**: `011-telemetry-dashboard` | **Date**: 2026-11-14

## 1. Application-Emitted Metrics (OTLP -> Prometheus)

| Metric Name | Type | Tags / Labels | Source | Description |
|---|---|---|---|---|
| `hikaricp_connections_active` | Gauge | `pool` | HikariCP / Micrometer | Active database connections |
| `hikaricp_connections_idle` | Gauge | `pool` | HikariCP / Micrometer | Idle database connections |
| `hikaricp_connections_max` | Gauge | `pool` | HikariCP / Micrometer | Maximum database pool size |
| `hikaricp_connections_pending` | Gauge | `pool` | HikariCP / Micrometer | Pending connection requests |
| `hikaricp_connections_acquire_milliseconds` | Timer | `pool` | HikariCP / Micrometer | Connection acquisition latency |
| `db_query_duration_milliseconds` | Timer | `adapter`, `method`, `outcome` | Persistence AOP Interceptor | Persistence operation duration |
| `smtp_messages_total` | Counter | `outcome` (`sent`, `failed`) | SMTP AOP Interceptor | Outbound email delivery count |
| `smtp_send_duration_milliseconds` | Timer | `outcome` (`sent`, `failed`) | SMTP AOP Interceptor | Outbound email send duration |

## 2. Broker-Emitted Metrics (RabbitMQ Prometheus Plugin -> Prometheus)

| Metric Name | Type | Tags / Labels | Source | Description |
|---|---|---|---|---|
| `rabbitmq_queue_messages` | Gauge | `queue`, `vhost` | `rabbitmq_prometheus` | Total messages in queue |
| `rabbitmq_queue_messages_ready` | Gauge | `queue`, `vhost` | `rabbitmq_prometheus` | Ready messages in queue |
| `rabbitmq_queue_messages_unacked` | Gauge | `queue`, `vhost` | `rabbitmq_prometheus` | Unacknowledged messages in queue |
| `rabbitmq_queue_consumers` | Gauge | `queue`, `vhost` | `rabbitmq_prometheus` | Active queue consumers |
| `rabbitmq_queue_messages_published_total` | Counter | `queue`, `vhost` | `rabbitmq_prometheus` | Messages published |
| `rabbitmq_queue_messages_delivered_total` | Counter | `queue`, `vhost` | `rabbitmq_prometheus` | Messages delivered |
| `rabbitmq_queue_messages_ack_total` | Counter | `queue`, `vhost` | `rabbitmq_prometheus` | Messages acknowledged |

## 3. Grafana Dashboard Provisioning Contract

- **Provisioned File**: `config/grafana/dashboards/http-service.json` (or `telemetry.json`, replacing `http-service.json` so no duplicate exists)
- **Datasource UID**: `"prometheus"`
- **Dashboard Title**: `"SDD Example - Telemetry"`
- **Dashboard UID**: `"sdd-http-service"`

