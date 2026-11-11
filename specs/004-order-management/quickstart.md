# Quickstart: Order Management

**Feature**: `004-order-management`

**Date**: 2026-11-10

This document describes how to validate the order management feature end-to-end.

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

### Scenario 1: Create an Order (Client-Side UUID)

Send a `POST` request to create a new order:

```bash
curl -i -X POST http://localhost:8080/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "customerName": "Alice Johnson",
    "customerEmail": "alice.johnson@example.com",
    "currency": "USD",
    "deliveryAddress": {
      "name": "Alice Johnson",
      "telephone": "+1-555-123-4567",
      "street": "123 Main Street",
      "complement": "Apt 4B",
      "city": "Springfield",
      "state": "IL",
      "zipCode": "62701",
      "country": "USA"
    },
    "invoiceAddress": {
      "name": "Alice Johnson",
      "vatNumber": "US987654321",
      "telephone": "+1-555-123-4567",
      "street": "123 Main Street",
      "complement": "Apt 4B",
      "city": "Springfield",
      "state": "IL",
      "zipCode": "62701",
      "country": "USA"
    },
    "orderLines": [
      {
        "productId": "e7b0a708-41d3-4f9e-a6c9-3a32f04e8d35",
        "quantity": 2,
        "unitPriceInCents": 4999
      }
    ]
  }'
```

**Expected Result**:
- HTTP Status: `201 Created`
- Header: `Location: /v1/orders/f47ac10b-58cc-4372-a567-0e02b2c3d479`
- The response body contains the order with calculated `totalAmountInCents: 9998` and initial status `PENDING`.

---

### Scenario 2: Retrieve the Order by Identifier

Send a `GET` request using the order identifier:

```bash
curl -i -X GET http://localhost:8080/v1/orders/f47ac10b-58cc-4372-a567-0e02b2c3d479
```

**Expected Result**:
- HTTP Status: `200 OK`
- The response body matches the created order data.

---

### Scenario 3: Update the Order

Send a `PUT` request to update order details:

```bash
curl -i -X PUT http://localhost:8080/v1/orders/f47ac10b-58cc-4372-a567-0e02b2c3d479 \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Alice Johnson",
    "customerEmail": "alice.johnson@example.com",
    "date": "2026-11-10T12:00:00Z",
    "status": "PAID",
    "currency": "USD",
    "deliveryAddress": {
      "name": "Alice Johnson",
      "telephone": "+1-555-123-4567",
      "street": "123 Main Street",
      "complement": "Apt 4B",
      "city": "Springfield",
      "state": "IL",
      "zipCode": "62701",
      "country": "USA"
    },
    "invoiceAddress": {
      "name": "Alice Johnson",
      "vatNumber": "US987654321",
      "telephone": "+1-555-123-4567",
      "street": "123 Main Street",
      "complement": "Apt 4B",
      "city": "Springfield",
      "state": "IL",
      "zipCode": "62701",
      "country": "USA"
    },
    "orderLines": [
      {
        "productId": "e7b0a708-41d3-4f9e-a6c9-3a32f04e8d35",
        "quantity": 3,
        "unitPriceInCents": 4999
      }
    ]
  }'
```

**Expected Result**:
- HTTP Status: `200 OK`
- `status` is now `PAID`.
- `totalAmountInCents` is recalculated to `14997`.

---

### Scenario 4: Attempt Deletion of Paid Order (Conflict)

Attempt to delete the order while its status is `PAID`:

```bash
curl -i -X DELETE http://localhost:8080/v1/orders/f47ac10b-58cc-4372-a567-0e02b2c3d479
```

**Expected Result**:
- HTTP Status: `409 Conflict`
- The response message explains that paid orders cannot be deleted.

---

### Scenario 5: Filter and Search Orders

Query orders by status, email, and date:

```bash
curl -i -X GET "http://localhost:8080/v1/orders?status=PAID&customerEmail=alice.johnson@example.com&sort=date&direction=desc"
```

**Expected Result**:
- HTTP Status: `200 OK`
- The response body contains an array with matching orders.

---

### Scenario 6: Delete a Cancelled Order (Hard Delete)

Update the order status to `CANCELLED`:

```bash
curl -i -X PUT http://localhost:8080/v1/orders/f47ac10b-58cc-4372-a567-0e02b2c3d479 \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Alice Johnson",
    "customerEmail": "alice.johnson@example.com",
    "date": "2026-11-10T12:00:00Z",
    "status": "CANCELLED",
    "currency": "USD",
    "deliveryAddress": {
      "name": "Alice Johnson",
      "telephone": "+1-555-123-4567",
      "street": "123 Main Street",
      "complement": "Apt 4B",
      "city": "Springfield",
      "state": "IL",
      "zipCode": "62701",
      "country": "USA"
    },
    "invoiceAddress": {
      "name": "Alice Johnson",
      "vatNumber": "US987654321",
      "telephone": "+1-555-123-4567",
      "street": "123 Main Street",
      "complement": "Apt 4B",
      "city": "Springfield",
      "state": "IL",
      "zipCode": "62701",
      "country": "USA"
    },
    "orderLines": [
      {
        "productId": "e7b0a708-41d3-4f9e-a6c9-3a32f04e8d35",
        "quantity": 1,
        "unitPriceInCents": 4999
      }
    ]
  }'
```

Now delete the cancelled order:

```bash
curl -i -X DELETE http://localhost:8080/v1/orders/f47ac10b-58cc-4372-a567-0e02b2c3d479
```

**Expected Result**:
- HTTP Status: `204 No Content`

Confirm the order is gone:

```bash
curl -i -X GET http://localhost:8080/v1/orders/f47ac10b-58cc-4372-a567-0e02b2c3d479
```

**Expected Result**:
- HTTP Status: `404 Not Found`

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

