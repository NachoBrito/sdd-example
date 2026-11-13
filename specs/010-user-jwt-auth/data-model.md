# Data Model: User Authentication and Role-Based Authorization

**Feature**: `010-user-jwt-auth` | **Date**: 2026-11-13

## Domain Entities & Value Objects

### 1. `User` (`es.nachobrito.sdd.example.domain.model.User`)
- **Type**: Java `record` (plain Java, no framework dependencies)
- **Fields**:
  - `UUID id` (`userId`, required, non-null, client-generated)
  - `String username` (required, non-blank, trimmed, max 100 chars, unique)
  - `String passwordHash` (required, non-blank; never plaintext)
  - `List<Role> roles` (required, non-empty immutable list of `Role` values)
  - `Instant createdAt` (required, UTC)
  - `Instant updatedAt` (required, UTC)
- **Validation Rules**:
  - `id == null` -> `InvalidUserDataException`
  - `username == null || username.isBlank()` or length > 100 -> `InvalidUserDataException`
  - `passwordHash == null || passwordHash.isBlank()` -> `InvalidUserDataException`
  - `roles == null || roles.isEmpty()` -> `InvalidUserDataException`

### 2. `Role` (`es.nachobrito.sdd.example.domain.model.Role`)
- **Type**: Java `enum`
- **Values**:
  - `ADMIN`
  - `CUSTOMER`
  - `CATALOG`
- **Default**: `CUSTOMER` for public registration.

### 3. `Order` (`es.nachobrito.sdd.example.domain.model.Order` — updated)
- **Added Field**:
  - `UUID userId` (required, non-null)
- **Validation Rule**:
  - `userId == null` -> `InvalidOrderDataException("Order userId cannot be null")`
- **Ownership Rules**:
  - `CUSTOMER` (without `ADMIN`): can create orders for their own `userId`; can list/read only orders where `order.userId().equals(callerUserId)`.
  - `ADMIN`: can list, read, and update any order regardless of `userId`.

### 4. `AuthToken` / Token Record (`es.nachobrito.sdd.example.domain.model.AuthToken`)
- **Type**: Java `record`
- **Fields**:
  - `UUID tokenId` (`jti`, required, non-null)
  - `UUID userId` (required, non-null)
  - `TokenType tokenType` (`ACCESS`, `REFRESH`)
  - `Instant issuedAt` (required)
  - `Instant expiresAt` (required, must be after `issuedAt`)
  - `@Nullable Instant revokedAt` (null when active; set on revocation)
- **State Transitions**:
  - `ACTIVE` (`revokedAt == null && now.isBefore(expiresAt)`) -> `REVOKED` (`revokedAt = now`)
  - `REVOKED` or `EXPIRED` -> cannot be validated or refreshed.

## Database Schema Changes (Liquibase)

### Migration `004-add-users-and-order-user-id.xml`
1. **`users` table**:
   - `id` `VARCHAR(36)` PK, `NOT NULL`
   - `username` `VARCHAR(100)` `UNIQUE`, `NOT NULL`
   - `password_hash` `VARCHAR(255)` `NOT NULL`
   - `created_at` `TIMESTAMP(6)` `NOT NULL`
   - `updated_at` `TIMESTAMP(6)` `NOT NULL`
2. **`user_roles` table**:
   - `user_id` `VARCHAR(36)` `NOT NULL`, FK -> `users(id)` ON DELETE CASCADE
   - `role` `VARCHAR(20)` `NOT NULL`
   - Composite PK `(user_id, role)`
3. **`auth_tokens` table**:
   - `token_id` `VARCHAR(36)` PK, `NOT NULL`
   - `user_id` `VARCHAR(36)` `NOT NULL`, FK -> `users(id)` ON DELETE CASCADE
   - `token_type` `VARCHAR(20)` `NOT NULL`
   - `issued_at` `TIMESTAMP(6)` `NOT NULL`
   - `expires_at` `TIMESTAMP(6)` `NOT NULL`
   - `revoked_at` `TIMESTAMP(6)` `NULL`
   - Index `idx_auth_tokens_user_id` on `(user_id)`
4. **`orders` table alteration** (expand-backfill-contract):
   - Add column `user_id` `VARCHAR(36)` nullable
   - Backfill existing rows: `UPDATE orders SET user_id = '00000000-0000-0000-0000-000000000001' WHERE user_id IS NULL`
   - Add `NOT NULL` constraint to `orders.user_id`
   - Create index `idx_orders_user_id` on `orders(user_id)`
5. **Initial `ADMIN` and `CATALOG` seed data** (`FR-015`):
   - Insert initial `admin` user (`ADMIN`, `CUSTOMER`, `CATALOG` or `ADMIN`) and `catalog` user (`CATALOG`) with pre-hashed `PBKDF2WithHmacSHA256` passwords into `users` and `user_roles` so local request scripts and k6 load tests can authenticate immediately.

