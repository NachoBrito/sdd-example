# Quickstart: Refactor Controller Error Management

**Feature**: `005-refactor-error-management`

**Date**: 2026-11-11

This document describes how to validate the refactored controller error handling end-to-end.

## 1. Prerequisites

- A running Docker engine for Micronaut Test Resources.
- Java 25 configured for the workspace.
- Access to the repository root directory.

## 2. Automated Verification

Execute the complete verification suite using Gradle:

```bash
./gradlew build
```

This command runs compilation, unit tests, integration tests, ArchUnit, Checkstyle, PMD, SpotBugs, and Pitest.

To run only the unit and integration tests:

```bash
./gradlew test
```

---

## 3. Manual End-to-End Validation Scenarios

Start the local server:

```bash
./gradlew run
```

The server listens on port `8080`.

### Scenario 1: Missing Product Returns 404 Not Found

Send a request for a nonexistent product identifier:

```bash
curl -i -X GET http://localhost:8080/v1/products/00000000-0000-0000-0000-000000000000
```

Expected response:
- Status: `404 Not Found`
- Body: `{"message":"Product not found for identifier 00000000-0000-0000-0000-000000000000","details":[]}`

### Scenario 2: Duplicate Product Returns 409 Conflict

Create a product twice with the same identifier:

```bash
curl -i -X POST http://localhost:8080/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "id": "11111111-1111-1111-1111-111111111111",
    "sku": "PROD-ERR-TEST",
    "name": "Test Product",
    "priceInCents": 1999,
    "currency": "EUR",
    "status": "ACTIVE"
  }'
```

Send the exact same request a second time.

Expected response:
- Status: `409 Conflict`
- Body: `{"message":"Product with identifier 11111111-1111-1111-1111-111111111111 already exists","details":[]}`

### Scenario 3: Missing Order Returns 404 Not Found

Send a request for a nonexistent order identifier:

```bash
curl -i -X GET http://localhost:8080/v1/orders/00000000-0000-0000-0000-000000000000
```

Expected response:
- Status: `404 Not Found`
- Body: `{"message":"Order not found with identifier 00000000-0000-0000-0000-000000000000","details":[]}`

### Scenario 4: Error Controller Routes Do Not Exist

Verify that the artificial error controller routes are completely removed:

```bash
curl -i -X GET http://localhost:8080/v1/errors
```

```bash
curl -i -X GET http://localhost:8080/v1/order-errors
```

Expected response for both requests:
- Status: `404 Not Found` (No route found)

