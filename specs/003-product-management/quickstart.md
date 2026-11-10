# Quickstart: Product Management

**Feature**: `003-product-management`

**Date**: 2026-11-10

This document describes how to validate the product management feature end-to-end.

## 1. Prerequisites

- A running Docker engine for Micronaut Test Resources.
- Java runtime configured for the project.
- Access to the repository root directory.

## 2. Start the Application

Start the application with Gradle:

```bash
./gradlew run
```

The application starts on port `8080` by default.

---

## 3. End-to-End Validation Scenarios

### Scenario 1: Create a Product (Client-Side UUID)

Send a `POST` request to create a new product:

```bash
curl -i -X POST http://localhost:8080/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "id": "e7b0a708-41d3-4f9e-a6c9-3a32f04e8d35",
    "sku": "KB-MECH-01",
    "name": "Mechanical Gaming Keyboard",
    "description": "RGB tactile mechanical keyboard",
    "priceInCents": 9999,
    "currency": "USD",
    "status": "DRAFT"
  }'
```

**Expected Result**:
- HTTP Status: `201 Created`
- Header: `Location: /v1/products/e7b0a708-41d3-4f9e-a6c9-3a32f04e8d35`
- Response body contains the product record with `createdAt` and `updatedAt` timestamps.

---

### Scenario 2: Retrieve the Product by Identifier

Send a `GET` request using the identifier:

```bash
curl -i -X GET http://localhost:8080/v1/products/e7b0a708-41d3-4f9e-a6c9-3a32f04e8d35
```

**Expected Result**:
- HTTP Status: `200 OK`
- Response body matches the created product data.

---

### Scenario 3: Update the Product

Send a `PUT` request to update product attributes:

```bash
curl -i -X PUT http://localhost:8080/v1/products/e7b0a708-41d3-4f9e-a6c9-3a32f04e8d35 \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "KB-MECH-01",
    "name": "Mechanical Gaming Keyboard Pro",
    "description": "Upgraded RGB tactile mechanical keyboard",
    "priceInCents": 10999,
    "currency": "USD",
    "status": "ACTIVE"
  }'
```

**Expected Result**:
- HTTP Status: `200 OK`
- `status` is now `ACTIVE`.
- `updatedAt` timestamp is more recent than `createdAt`.

---

### Scenario 4: Filter and Search Products

Query products by status and regular expression on name:

```bash
curl -i -X GET "http://localhost:8080/v1/products?status=ACTIVE&nameRegex=Gaming.*&sort=name&direction=asc"
```

**Expected Result**:
- HTTP Status: `200 OK`
- Response body contains an array with the matching product.

---

### Scenario 5: Delete the Product (Permanent Removal)

Send a `DELETE` request:

```bash
curl -i -X DELETE http://localhost:8080/v1/products/e7b0a708-41d3-4f9e-a6c9-3a32f04e8d35
```

**Expected Result**:
- HTTP Status: `204 No Content`

Confirm the product is gone:

```bash
curl -i -X GET http://localhost:8080/v1/products/e7b0a708-41d3-4f9e-a6c9-3a32f04e8d35
```

**Expected Result**:
- HTTP Status: `404 Not Found`

---

### Scenario 6: Boundary Validation (Negative Price)

Attempt to create a product with a negative price:

```bash
curl -i -X POST http://localhost:8080/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "id": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
    "sku": "INVALID-PRICE",
    "name": "Invalid Product",
    "priceInCents": -500,
    "currency": "USD",
    "status": "DRAFT"
  }'
```

**Expected Result**:
- HTTP Status: `400 Bad Request`
- Response body indicates that `priceInCents` must be greater than or equal to 0.

---

## 4. Automated Verification Commands

Run unit tests:

```bash
./gradlew test
```

Run static analysis and mutation tests:

```bash
./gradlew check
```

