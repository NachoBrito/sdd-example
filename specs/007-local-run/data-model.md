# Data Model: Local Environment and Execution Configuration

**Feature Branch**: `007-local-run`  
**Date**: 2026-11-11  
**Spec**: [spec.md](spec.md)

This document describes the declarative configuration models, service topologies, and request contracts for the local environment.

---

## 1. Local Container Environment Model

The local environment is represented by a set of interconnected container definitions.

### Container Service Topology

| Service Name | Container Image | Host Ports | Internal Ports | Dependencies |
|:---|:---|:---|:---|:---|
| `app` | Built from `Dockerfile` | `8080` | `8080` | `mariadb`, `rabbitmq`, `mailpit`, `otel-collector` |
| `mariadb` | `mariadb:11.4` | `3306` | `3306` | None |
| `rabbitmq` | `rabbitmq:3.13-management` | `5672`, `15672` | `5672`, `15672` | None |
| `mailpit` | `axllent/mailpit:latest` | `2525`, `8025` | `1025`, `8025` | None |
| `otel-collector` | `otel/opentelemetry-collector-contrib:0.114.0` | `4318`, `4317`, `8889` | `4318`, `4317`, `8889` | None |
| `prometheus` | `prom/prometheus:latest` | `9090` | `9090` | `otel-collector` |
| `grafana` | `grafana/grafana:latest` | `3000` | `3000` | `prometheus` |

### Environment Variables Model

#### Application Container (`app`)
| Variable | Default Value | Description |
|:---|:---|:---|
| `DATASOURCES_DEFAULT_URL` | `jdbc:mariadb://mariadb:3306/sdd` | JDBC database connection string |
| `DATASOURCES_DEFAULT_USERNAME` | `sdd` | MariaDB application user |
| `DATASOURCES_DEFAULT_PASSWORD` | `sdd` | MariaDB application password |
| `RABBITMQ_URI` | `amqp://guest:guest@rabbitmq:5672` | RabbitMQ connection URI |
| `SMTP_HOST` | `mailpit` | Mock SMTP host address |
| `SMTP_PORT` | `1025` | Mock SMTP port |
| `OTEL_EXPORTER_OTLP_METRICS_ENDPOINT` | `http://otel-collector:4318/v1/metrics` | OTLP metrics endpoint |

#### MariaDB Container (`mariadb`)
| Variable | Default Value | Description |
|:---|:---|:---|
| `MARIADB_ROOT_PASSWORD` | `root` | Database administrator password |
| `MARIADB_DATABASE` | `sdd` | Default application database name |
| `MARIADB_USER` | `sdd` | Default application database user |
| `MARIADB_PASSWORD` | `sdd` | Default application user password |

---

## 2. Telemetry and Dashboard Configuration Model

### OpenTelemetry Collector Pipeline Model
- **Receiver**: OTLP HTTP receiver listening on `0.0.0.0:4318`.
- **Processor**: Batch processor with 200ms timeout.
- **Exporters**:
  - `prometheus`: Serves metrics on `0.0.0.0:8889`.
  - `debug`: Emits metrics summary to container log.

### Grafana Datasource Model
- **Name**: `Prometheus`
- **Type**: `prometheus`
- **Access**: `proxy`
- **URL**: `http://prometheus:9090`
- **IsDefault**: `true`

### Grafana Dashboard Panel Model
The pre-configured dashboard defines the following visual panels:

| Panel Title | Metric Query | Visualization Type | Purpose |
|:---|:---|:---|:---|
| **HTTP Request Rate** | `rate(http_server_requests_seconds_count[1m])` | Time series graph | Measures requests per second across endpoints |
| **Response Duration (p95)** | `histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[1m])) by (le))` | Time series graph | Monitors 95th percentile request latency |
| **HTTP Status Code Distribution** | `sum by (status) (rate(http_server_requests_seconds_count[1m]))` | Bar gauge / pie chart | Visualizes ratio of 2xx, 4xx, and 5xx responses |
| **JVM Memory Usage** | `jvm_memory_used_bytes{area="heap"}` | Stat / gauge | Tracks application heap consumption |

---

## 3. HTTP Request Suite Entity Model

### Product Use Case Request Payloads

#### Create Product Command
| Field | Type | Required | Description |
|:---|:---|:---|:---|
| `id` | `UUID` | Yes | Client-generated product identifier |
| `sku` | `String` | Yes | Unique product stock keeping unit |
| `name` | `String` | Yes | Name of the product |
| `description` | `String` | No | Optional product details |
| `priceInCents` | `Long` | Yes | Price expressed in smallest currency units |
| `currency` | `String` | Yes | Three-letter ISO currency code |
| `status` | `String` | Yes | Product status (`ACTIVE`, `INACTIVE`) |

#### Update Product Command
| Field | Type | Required | Description |
|:---|:---|:---|:---|
| `name` | `String` | Yes | Updated product name |
| `description` | `String` | No | Updated product details |
| `priceInCents` | `Long` | Yes | Updated price in cents |
| `status` | `String` | Yes | Updated product status |

### Order Use Case Request Payloads

#### Create Order Command
| Field | Type | Required | Description |
|:---|:---|:---|:---|
| `id` | `UUID` | Yes | Client-generated order identifier |
| `customerName` | `String` | Yes | Full name of the customer |
| `customerEmail` | `String` | Yes | Customer email address |
| `lines` | `List<OrderLine>` | Yes | List of ordered items |
| `deliveryAddress` | `Address` | Yes | Shipping destination |

#### OrderLine Sub-Entity
| Field | Type | Required | Description |
|:---|:---|:---|:---|
| `productId` | `UUID` | Yes | Reference product identifier |
| `quantity` | `Integer` | Yes | Purchased item quantity (minimum 1) |
| `unitPriceInCents` | `Long` | Yes | Unit price at time of purchase |

#### Address Sub-Entity
| Field | Type | Required | Description |
|:---|:---|:---|:---|
| `street` | `String` | Yes | Street address |
| `city` | `String` | Yes | City name |
| `postalCode` | `String` | Yes | Postal code |
| `country` | `String` | Yes | Country code or name |

