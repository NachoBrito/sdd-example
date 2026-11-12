# Quickstart & Validation Guide: Local Environment and Execution

**Feature Branch**: `007-local-run`  
**Date**: 2026-11-11  
**Spec**: [spec.md](spec.md)

This guide documents the procedures to start, verify, use, and stop the local environment.

---

## Prerequisites

Verify that the following software is installed on the host machine:
- Docker version 24.0 or newer.
- Docker Compose v2 version 2.20 or newer.

---

## 1. Start the Environment

### Complete Containerized Mode

Execute the following command in the repository root to build and start all containers:

```bash
docker compose up -d --build
```

### Hybrid Development Mode (Backing Services Only)

To run the application locally in an IDE or terminal against containerized dependencies, execute:

```bash
docker compose up -d mariadb rabbitmq mailpit otel-collector prometheus grafana
./gradlew run
```

---

## 2. Verify Container Health

Check that all containers report healthy status:

```bash
docker compose ps
```

Expected output:
- `sdd-mariadb`: `healthy` on port `3306`.
- `sdd-rabbitmq`: `healthy` on ports `5672` and `15672`.
- `sdd-mailpit`: `healthy` on ports `2525` and `8025`.
- `sdd-otel-collector`: `running` on ports `4318`, `4317`, and `8889`.
- `sdd-prometheus`: `running` on port `9090`.
- `sdd-grafana`: `running` on port `3000`.
- `sdd-app`: `running` on port `8080`.

---

## 3. Access Web User Interfaces

Open the following URLs in a web browser:

| Interface | URL | Credentials | Purpose |
|:---|:---|:---|:---|
| **API Health / Service** | `http://localhost:8080` | None | Verify REST service response |
| **RabbitMQ Management** | `http://localhost:15672` | `guest` / `guest` | Inspect queues and exchanges |
| **Mailpit Web UI** | `http://localhost:8025` | None | Inspect sent notification emails |
| **Prometheus UI** | `http://localhost:9090` | None | Inspect raw scraped metrics |
| **Grafana Dashboard** | `http://localhost:3000` | `admin` / `admin` | View HTTP service metrics dashboard |

---

## 4. Execute API Use Cases

### Using HTTP Files

Open the request files in an IDE (IntelliJ or VS Code with REST Client extension):
- [`docs/running/requests/products.http`](file:///home/nacho/github/sdd-example/docs/running/requests/products.http)
- [`docs/running/requests/orders.http`](file:///home/nacho/github/sdd-example/docs/running/requests/orders.http)

Execute requests in the following sequence:
1. Create a product in `products.http`.
2. Retrieve the product by identifier.
3. Create an order in `orders.http` using the created product identifier.
4. Verify email notification arrival in the Mailpit web interface (`http://localhost:8025`).
5. Open the Grafana dashboard (`http://localhost:3000`) and verify that HTTP request metrics increase.

### Using cURL Commands

Create a product:

```bash
curl -i -X POST http://localhost:8080/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "id": "11111111-1111-1111-1111-111111111111",
    "sku": "PROD-SAMPLE-001",
    "name": "Sample Product",
    "description": "Validation sample",
    "priceInCents": 1999,
    "currency": "EUR",
    "status": "ACTIVE"
  }'
```

Verify response: HTTP `201 Created` with `Location: /v1/products/11111111-1111-1111-1111-111111111111`.

---

## 5. Stop the Environment

Stop all running containers:

```bash
docker compose down
```

To stop containers and delete persisted volumes (clean slate):

```bash
docker compose down -v
```

