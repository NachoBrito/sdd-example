# Data Model: Load Testing and Performance Validation

**Feature Branch**: `009-load-testing`  
**Date**: 2026-11-12  
**Spec**: [spec.md](spec.md)

This document describes the configuration models, data structures, and synthetic entity payloads for the load testing suite.

---

## 1. Load Testing Entities

```
+------------------------------------+
|       WorkloadConfiguration        |
+------------------------------------+
| targetUrl: String                  |
| scenario: String                   |
| virtualUsers: Integer              |
| duration: String                   |
| rampUpDuration: String             |
+-----------------+------------------+
                  |
                  | configures
                  v
+-----------------+------------------+
|          LoadScenario              |
+------------------------------------+
| name: String                       |
| description: String                |
| targetEndpoints: List<String>      |
| thresholds: Map<String, List>      |
+-----------------+------------------+
                  |
                  | executes with
                  v
+-----------------+------------------+
|           VirtualUser              |
+------------------------------------+
| vuId: Integer                      |
| iteration: Integer                 |
| state: UserContext                 |
+-----------------+------------------+
                  |
                  | generates
                  v
+-----------------+------------------+
|        PerformanceReport           |
+------------------------------------+
| totalRequests: Integer             |
| requestsPerSecond: Float           |
| failedRequests: Integer            |
| errorRate: Float                   |
| durationMin: Float                 |
| durationMed: Float                 |
| durationP95: Float                 |
| durationP99: Float                 |
| durationMax: Float                 |
| status: ExecutionStatus            |
+------------------------------------+
```

---

## 2. Configuration Attributes

### WorkloadConfiguration
Defines the parameters that control scenario execution.

| Attribute | Type | Description | Default | Validation Rule |
|:---|:---|:---|:---|:---|
| `targetUrl` | String | Target service base URL | `http://localhost:8080` | Valid HTTP/HTTPS URI |
| `scenario` | String | Target workload scenario | `mixed` | One of: `products`, `orders`, `mixed`, `stability` |
| `virtualUsers` | Integer | Concurrent simulated users | `10` | Positive integer (1 to 500) |
| `duration` | String | Active traffic duration | `30s` | Valid duration string (e.g., `30s`, `5m`) |
| `rampUpDuration`| String | Ramp-up ramp time | `5s` | Valid duration string (e.g., `5s`, `30s`) |

---

## 3. Synthetic Payload Schemas

### Product Payload
Represents the request body sent to `POST /v1/products`.

```json
{
  "id": "UUID (UUIDv4)",
  "sku": "String (e.g., PROD-LOAD-xxxx)",
  "name": "String (e.g., Ergonomic Keyboard Model xxxx)",
  "description": "String (Product description)",
  "priceInCents": "Integer (> 0, e.g., 4999)",
  "currency": "String (ISO 4217, e.g., EUR)",
  "status": "String (ACTIVE)"
}
```

**Validation Invariants**:
- `id` must be a valid UUIDv4 string.
- `sku` must be non-blank and unique per product.
- `name` must be non-blank.
- `priceInCents` must be greater than zero.
- `currency` must be `EUR`.
- `status` must be `ACTIVE`.

### Order Payload
Represents the request body sent to `POST /v1/orders`.

```json
{
  "id": "UUID (UUIDv4)",
  "customerName": "String (e.g., Performance Test User)",
  "customerEmail": "String (e.g., loadtest-xxxx@example.com)",
  "currency": "String (EUR)",
  "deliveryAddress": {
    "name": "String",
    "telephone": "String",
    "street": "String",
    "complement": "String",
    "city": "String",
    "state": "String",
    "zipCode": "String",
    "country": "String"
  },
  "invoiceAddress": {
    "name": "String",
    "vatNumber": "String",
    "telephone": "String",
    "street": "String",
    "complement": "String",
    "city": "String",
    "state": "String",
    "zipCode": "String",
    "country": "String"
  },
  "orderLines": [
    {
      "productId": "UUID (Must match an existing active product)",
      "quantity": "Integer (>= 1)",
      "unitPriceInCents": "Integer (> 0)"
    }
  ]
}
```

**Validation Invariants**:
- `id` must be a valid UUIDv4 string.
- `customerEmail` must follow valid email format.
- `currency` must match product currency (`EUR`).
- `orderLines` must contain at least one item.
- `orderLines[].productId` MUST reference an existing product record in the system.
- `orderLines[].quantity` must be greater than or equal to 1.

---

## 4. Performance Metric Schema

Represents the summary results evaluated against success criteria.

| Metric | Source | Unit | Pass Criteria |
|:---|:---|:---|:---|
| `http_reqs` | k6 Counter | Requests | Count > 0 |
| `http_req_duration{p(95)}` | k6 Trend | Milliseconds | Value < 500ms |
| `http_req_duration{p(99)}` | k6 Trend | Milliseconds | Value < 1000ms |
| `http_req_failed` | k6 Rate | Percentage | Rate < 0.01 (< 1%) |
| `vus` | k6 Gauge | Virtual Users | Matches configured user count |

