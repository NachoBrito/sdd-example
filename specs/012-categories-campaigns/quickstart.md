# Quickstart Validation Guide: Product Categories and Discount Campaigns

**Feature**: `012-categories-campaigns` | **Date**: 2026-11-14

## Prerequisites

- Docker is running so Micronaut Test Resources can launch MariaDB and RabbitMQ.
- Full verification build passes:
  ```bash
  ./gradlew build
  ```
- Contracts and data rules are defined in [categories-api.yaml](contracts/categories-api.yaml), [campaigns-api.yaml](contracts/campaigns-api.yaml), [products-api.yaml](contracts/products-api.yaml), and [data-model.md](data-model.md).

## End-to-End Validation Scenarios

### 1. Category Lifecycle and Role Enforcement
1. Call `POST /v1/categories` with an `ADMIN` bearer token to create a category (`201 Created`).
2. Call `POST /v1/categories` and `DELETE /v1/categories/{id}` with `CUSTOMER` or `CATALOG` tokens to verify access is denied (`403 Forbidden`).
3. Call `GET /v1/categories` with `CUSTOMER` or `CATALOG` tokens, including `name` and `description` query filters, and verify matching categories return (`200 OK`).
4. Call `GET /v1/categories` without authentication and verify rejection (`401 Unauthorized`).

### 2. Product `categoryId` Versioning and Validation
1. Call `POST /v1/products` without `categoryId` as a `CATALOG` user and confirm backward-compatible creation (`201 Created` with `categoryId: null`).
2. Call `POST /v2/products` without `categoryId` as a `CATALOG` user and confirm validation failure (`400 Bad Request`).
3. Call `GET /v1/categories` to obtain a valid category identifier, then call `POST /v2/products` and `PUT /v2/products/{id}` with that `categoryId` (`201 Created` / `200 OK`).
4. Call `POST /v1/products` or `POST /v2/products` with an unknown `categoryId` and confirm rejection (`400 Bad Request`).
5. Call `DELETE /v1/categories/{id}` while a product references that category and confirm conflict (`409 Conflict`); delete or reassign the product and retry `DELETE /v1/categories/{id}` (`204 No Content`).

### 3. Campaign Lifecycle and Validation
1. Call `POST /v1/campaigns` with an `ADMIN` token to create an `ACTIVE` campaign with valid date window, `discountPercent`, and predicates (`201 Created`).
2. Call `PUT /v1/campaigns/{id}` with an `ADMIN` token to update attributes and predicates (`200 OK`).
3. Call `GET /v1/campaigns` with an `ADMIN` token using optional `name`, `description`, `status`, `startDate`, and `endDate` filters (`200 OK`).
4. Verify non-`ADMIN` callers (`CUSTOMER`, `CATALOG`) receive `403 Forbidden` on all `/v1/campaigns` endpoints.
5. Submit invalid `discountPercent` (`< 0` or `> 100`), `endDate < startDate`, invalid category UUID, or non-integer price threshold and confirm `400 Bad Request`.
6. Call `DELETE /v1/campaigns/{id}` with an `ADMIN` token (`204 No Content`).

### 4. Request-Time Active Discount Resolution on Product Reads
1. Create an `ACTIVE` campaign whose `[startDate, endDate]` window includes the current UTC timestamp and whose `AND` predicates match a target product.
2. Create a second `DRAFT` or expired campaign matching the same product.
3. Call `GET /v1/products/{id}`, `GET /v1/products`, `GET /v2/products/{id}`, and `GET /v2/products` as a `CUSTOMER` or `CATALOG` user.
4. Confirm each product response lists only the active matching campaign in `discounts` (`campaignId`, `campaignName`, `discountPercent`), leaves `priceInCents` unmodified, and returns an empty `discounts` list for non-matching products.

