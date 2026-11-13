# Quickstart Validation Guide: User Authentication and Role-Based Authorization

**Feature**: `010-user-jwt-auth` | **Date**: 2026-11-13

## Prerequisites

- Docker is running (required by Micronaut Test Resources for MariaDB and RabbitMQ).
- Build and static analysis pass:
  ```bash
  ./gradlew build
  ```

## Validation Scenarios

### 1. Register and Login
1. `POST /v1/auth/register` with `userId` (UUID), `username`, and `password` returns `201 Created` with `roles: ["CUSTOMER"]` and no `password` field.
2. `POST /v1/auth/login` with valid `username` and `password` returns `200 OK` with `accessToken` and `refreshToken`.
3. `POST /v1/auth/login` with invalid credentials returns `401 Unauthorized`.

### 2. Token Validation, Refresh, and Revocation
1. `POST /v1/auth/validate` with `Authorization: Bearer <accessToken>` returns `200 OK` with `valid: true`, `userId`, and `roles`.
2. `POST /v1/auth/refresh` with `refreshToken` returns `200 OK` with a new `accessToken` and `refreshToken`.
3. `POST /v1/auth/revoke` with `Authorization: Bearer <accessToken>` returns `204 No Content`.
4. Subsequent `POST /v1/auth/validate` or protected endpoint calls with the revoked token return `401 Unauthorized`.

### 3. Product Authorization Rules
1. Unauthenticated `GET /v1/products` returns `401 Unauthorized`.
2. `CUSTOMER` calling `GET /v1/products` receives only `ACTIVE` products; calling `GET /v1/products/{draftId}` or `POST /v1/products` returns `403 Forbidden`.
3. `CATALOG` calling `GET /v1/products` sees `ACTIVE`, `DRAFT`, and `ARCHIVED` products, and can `POST` / `PUT` products.

### 4. Order Ownership and Administration
1. `CUSTOMER` calling `POST /v1/orders` creates an order bound to their `userId`.
2. `CUSTOMER` calling `GET /v1/orders` sees only their own orders; reading another user's order or calling `PUT /v1/orders/{id}` returns `403 Forbidden`.
3. `ADMIN` calling `GET /v1/orders`, `GET /v1/orders/{id}`, or `PUT /v1/orders/{id}` succeeds across all users' orders.

### 5. Running Scripts & Load Testing
1. Execute requests in `docs/running/requests/auth.http`, `products.http`, and `orders.http` against the running Docker Compose stack.
2. Run `./load-tests/run.sh -s auth` and `./load-tests/run.sh -s mixed` to confirm k6 scenarios authenticate in `setup()` and pass all thresholds.

