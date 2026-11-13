# Running the Service Locally

This document describes the procedures to start, verify, use, and stop the `sdd-example` service with all dependencies.

---

## 1. Prerequisites

Verify that the host system has the following tools installed:

- Docker version 24.0 or newer.
- Docker Compose v2 version 2.20 or newer.
- Java 25 (only required for hybrid execution).

---

## 2. Start the Service

### Option A: Complete Containerized Execution

Run the service and all backing containers together inside Docker:

```bash
docker compose up -d --build
```

This command builds the application container and starts all supporting services in the background.

### Option B: Hybrid Local Execution

Start only the backing services in containers and execute the application locally through Gradle:

```bash
docker compose up -d mariadb rabbitmq mailpit otel-collector prometheus grafana
./gradlew run
```

This mode allows rapid development and debugging in an IDE.

---

## 3. Verify Container Status

Check that all containers report a healthy status:

```bash
docker compose ps
```

All services must show `running` or `healthy` states before you send requests.

Inspect container logs if any container reports an error:

```bash
docker compose logs -f
```

---

## 4. Exposed Services and Web User Interfaces

The local environment exposes the following endpoints:

| Service | Host Port | Web URL | Credentials | Purpose |
|:---|:---|:---|:---|:---|
| **Application** | `8080` | `http://localhost:8080` | Seeded: `admin`/`admin1234`, `catalog`/`catalog1234` | REST API service with JWT bearer authentication |
| **MariaDB** | `3306` | None | `sdd` / `sdd` | Relational database storage |
| **RabbitMQ Management** | `15672` | `http://localhost:15672` | `guest` / `guest` | Message queue management console |
| **Mailpit Web UI** | `8025` | `http://localhost:8025` | None | Web viewer for email notifications |
| **Mailpit SMTP** | `2525` | None | None | SMTP listener port |
| **OpenTelemetry Collector** | `4318` | `http://localhost:4318` | None | OTLP metrics HTTP endpoint |
| **Prometheus** | `9090` | `http://localhost:9090` | None | Metric collection and query interface |
| **Grafana** | `3000` | `http://localhost:3000` | `admin` / `admin` | HTTP service metrics dashboard |

---

## 5. Send Example Requests

The repository includes executable HTTP files with sample requests for all use cases:

- `docs/running/requests/auth.http` — User registration, login, token validation, token refresh, and token revocation.
- `docs/running/requests/products.http` — Product catalog management (requires `CATALOG` or `CUSTOMER` JWT token).
- `docs/running/requests/orders.http` — Order lifecycle operations (requires `CUSTOMER` or `ADMIN` JWT token).

- Product catalog requests: [`requests/products.http`](requests/products.http)
- Order management requests: [`requests/orders.http`](requests/orders.http)

Open these files in IntelliJ IDEA or Visual Studio Code with the REST Client extension.
Execute the requests in sequence to test all CRUD workflows and observe error handling.

---

## 6. Inspect Telemetry Dashboards

1. Open `http://localhost:3000` in a web browser.
2. Log in with username `admin` and password `admin`.
3. Open the **SDD Example - HTTP Service Metrics** dashboard.
4. Observe HTTP request volume, latency, status distributions, and memory usage.

---

## 7. Run Load Testing and Performance Validation

Execute automated load scenarios to validate throughput, latency, and stability under concurrent traffic:

```bash
./load-tests/run.sh
```

Read the [Load Testing Guide](load-testing.md) for available scenarios, parameters, and threshold options.

---

## 8. Stop the Service

Stop all running containers:

```bash
docker compose down
```

To stop containers and delete all persisted volumes (database and queues):

```bash
docker compose down -v
```

