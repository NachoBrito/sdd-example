# Contract: HTTP Requests Specification

This contract defines the format and expected status codes for the example HTTP files.

---

## 1. Product Catalog Endpoints (`/v1/products`)

### 1.1 Create Product
- **Method**: `POST /v1/products`
- **Headers**: `Content-Type: application/json`
- **Payload**:
  ```json
  {
    "id": "{{productId}}",
    "sku": "PROD-SAMPLE-001",
    "name": "Sample Product",
    "description": "A high-quality sample product for local testing",
    "priceInCents": 1999,
    "currency": "EUR",
    "status": "ACTIVE"
  }
  ```
- **Expected Responses**:
  - `201 Created` with `Location: /v1/products/{{productId}}` and created entity body.
  - `400 Bad Request` when required fields are missing or invalid.
  - `409 Conflict` when product ID or SKU already exists.

### 1.2 Get Product by Identifier
- **Method**: `GET /v1/products/{{productId}}`
- **Headers**: `Accept: application/json`
- **Expected Responses**:
  - `200 OK` with product details body.
  - `404 Not Found` when product identifier does not exist.

### 1.3 Update Product
- **Method**: `PUT /v1/products/{{productId}}`
- **Headers**: `Content-Type: application/json`
- **Payload**:
  ```json
  {
    "name": "Updated Sample Product",
    "description": "Updated description with new features",
    "priceInCents": 2499,
    "status": "ACTIVE"
  }
  ```
- **Expected Responses**:
  - `200 OK` with updated product body.
  - `400 Bad Request` on invalid payload.
  - `404 Not Found` when product does not exist.

### 1.4 List Products
- **Method**: `GET /v1/products?limit=10&offset=0&status=ACTIVE`
- **Headers**: `Accept: application/json`
- **Expected Responses**:
  - `200 OK` with JSON array of products.

### 1.5 Delete Product
- **Method**: `DELETE /v1/products/{{productId}}`
- **Expected Responses**:
  - `204 No Content` on successful deletion.
  - `404 Not Found` when product does not exist.

---

## 2. Order Management Endpoints (`/v1/orders`)

### 2.1 Create Order
- **Method**: `POST /v1/orders`
- **Headers**: `Content-Type: application/json`
- **Payload**:
  ```json
  {
    "id": "{{orderId}}",
    "customerName": "Jane Doe",
    "customerEmail": "jane.doe@example.com",
    "lines": [
      {
        "productId": "{{productId}}",
        "quantity": 2,
        "unitPriceInCents": 1999
      }
    ],
    "deliveryAddress": {
      "street": "123 Main Street",
      "city": "Madrid",
      "postalCode": "28001",
      "country": "Spain"
    }
  }
  ```
- **Expected Responses**:
  - `201 Created` with `Location: /v1/orders/{{orderId}}` and created entity body.
  - `400 Bad Request` on invalid data.
  - `409 Conflict` when order identifier already exists.

### 2.2 Get Order by Identifier
- **Method**: `GET /v1/orders/{{orderId}}`
- **Headers**: `Accept: application/json`
- **Expected Responses**:
  - `200 OK` with order details body.
  - `404 Not Found` when order does not exist.

### 2.3 Update Order
- **Method**: `PUT /v1/orders/{{orderId}}`
- **Headers**: `Content-Type: application/json`
- **Payload**:
  ```json
  {
    "customerName": "Jane Doe Updated",
    "customerEmail": "jane.doe@example.com",
    "status": "PAID",
    "deliveryAddress": {
      "street": "456 New Avenue",
      "city": "Madrid",
      "postalCode": "28002",
      "country": "Spain"
    }
  }
  ```
- **Expected Responses**:
  - `200 OK` with updated order details body.
  - `400 Bad Request` on invalid data.
  - `404 Not Found` when order does not exist.

### 2.4 List Orders
- **Method**: `GET /v1/orders?limit=10&offset=0`
- **Headers**: `Accept: application/json`
- **Expected Responses**:
  - `200 OK` with JSON array of orders.

### 2.5 Delete Order
- **Method**: `DELETE /v1/orders/{{orderId}}`
- **Expected Responses**:
  - `204 No Content` on successful deletion.
  - `404 Not Found` when order does not exist.
  - `409 Conflict` when order is in non-cancellable status (PAID, SHIPPED, DELIVERED).

