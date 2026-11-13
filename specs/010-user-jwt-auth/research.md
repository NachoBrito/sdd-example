# Research: User Authentication and Role-Based Authorization

**Feature**: `010-user-jwt-auth` | **Date**: 2026-11-13

## 1. JWT Authentication & Micronaut Security

### Decision
Use `io.micronaut.security:micronaut-security-jwt` (with `micronaut-security-annotations` processor) for JWT generation, signature verification, and `@Secured` AOP role enforcement on REST controllers.

### Rationale
- `AGENTS.md` Section 5 requires Micronaut-native libraries.
- `AGENTS.md` Section 3 requires Micronaut AOP (`@Secured`) for crosscutting security concerns instead of manual token parsing in each controller method.
- Micronaut Security integrates directly with Micronaut HTTP routing, Netty, and OpenAPI (`@SecurityRequirement`).
- `HexagonalArchitectureTest` keeps `io.micronaut.http..` and `io.micronaut.security..` out of `domain` and `application` layers; `@Secured` annotations stay on `adapter.in.rest` controllers, while `application` receives explicit `CurrentUser` / `userId` and `roles` parameters.

### Alternatives Considered
- **Custom HMAC-SHA256 JWT filter using JDK `javax.crypto.Mac`**: Avoids adding a Gradle dependency, but duplicates standard JWT parsing, expiration, and `@Secured` AOP integration that Micronaut Security already provides.
- **Note on Build Authorization**: Adding `micronaut-security-jwt` and `micronaut-security-annotations` to `gradle/libs.versions.toml` and `build.gradle.kts` requires explicit user authorization per `AGENTS.md` Section 2 and Constitution Infrastructure Escalation Protocol.

## 2. Password Hashing

### Decision
Use JDK-standard `PBKDF2WithHmacSHA256` (`javax.crypto.SecretKeyFactory`) with a 16-byte random salt (`SecureRandom`), 310,000 iterations, and constant-time comparison (`MessageDigest.isEqual`) behind a `PasswordHasherPort` outbound port in `application.port.out`.

### Rationale
- Requires zero additional third-party dependencies.
- Follows OWASP Password Storage Cheat Sheet recommendations for PBKDF2-HMAC-SHA256.
- Keeps `domain` framework-free (`User` stores only the opaque `passwordHash` string, never plaintext).

### Alternatives Considered
- **BCrypt (`at.favre.lib:bcrypt` or `spring-security-crypto`)**: Strong adaptive hash, but introduces an extra external dependency not currently in `gradle/libs.versions.toml`. Can be swapped behind `PasswordHasherPort` later if desired.

## 3. Token Refresh & Revocation Model

### Decision
Issue signed JWT access tokens and opaque/signed refresh tokens backed by a `revoked_tokens` (or `auth_tokens` with `jti`, `user_id`, `token_type`, `expires_at`, `revoked_at`) table in MariaDB via Micronaut Data JDBC.

### Rationale
- Stateless JWT verification handles normal requests quickly, while checking token revocation by `jti` against `auth_tokens` ensures immediate revocation on `/v1/auth/revoke` (Requirement `FR-007`).
- Refresh tokens stored with `jti` and `revoked_at` allow rotation (revoking the old refresh token and issuing a new access + refresh token pair) and rejection of revoked or expired tokens.
- Uses existing MariaDB + Liquibase + Micronaut Data JDBC stack (no Redis or external cache needed, honoring Constitution Principle 3: Do Not Distribute by Default).

### Alternatives Considered
- **In-memory revocation map**: Fast, but loses revocation state on restart and fails across multiple instances.
- **Redis token store**: Violates Constitution Principle 3 ("MUST NOT introduce a new distributed cache without explicit user requirement").

## 4. Order `userId` Migration & Ownership Enforcement

### Decision
- Add `user_id` (`VARCHAR(36)`, nullable initially in Liquibase expand step, backfilled to `00000000-0000-0000-0000-000000000001`, then altered to `NOT NULL` with index `idx_orders_user_id` in `004-add-users-and-order-user-id.xml`).
- Seed a system/legacy user record with `id = '00000000-0000-0000-0000-000000000001'` or allow `orders.user_id` without a hard FK if legacy orders do not have a loginable account (recommended: no FK constraint to `users` or seed a disabled/sentinel legacy user so referential integrity holds if needed; we will use a non-null `VARCHAR(36)` column `user_id` on `orders` without breaking existing test fixtures that create orders with a valid UUID `userId`).
- Update `Order` domain record, `CreateOrderCommand`, `OrderEntity`, `OrderResponse`, and `OrderEventDto` / `OrderPayloadDto` (if needed) to carry `UUID userId`.
- Enforce product visibility and order ownership via Micronaut AOP (`@AuthorizePolicy` on `ProductService` and `OrderService`, handled by `AuthorizationInterceptor` `@InterceptorBean` in `adapter.out.security` backed by a mockable `CallerContextPort`):
  - Before `context.proceed()`: `AuthorizationInterceptor` rewrites `ListProductsQuery` (forcing `ProductStatus.ACTIVE` for non-`CATALOG` callers), `ListOrdersQuery` (forcing `userId = caller.userId()` for non-`ADMIN` callers), and `CreateOrderCommand` (populating `userId = caller.userId()`).
  - After `context.proceed()`: `AuthorizationInterceptor` validates returned `Product` (rejecting non-`ACTIVE` for non-`CATALOG` callers) and `Order` (rejecting non-owned orders for non-`ADMIN` callers) with `AuthorizationException`.
  - When no caller context is present (e.g., direct unit tests calling `OrderService` or `ProductService` without AOP/security context), `CallerContextPort` is not invoked unless AOP proxies are active, keeping unit tests clean and decoupled.

### Alternatives Considered
- **Adding a foreign key `orders.user_id -> users(id)`**: Would require every unit/integration test that creates an order to pre-insert a `User` row in MariaDB, increasing coupling in existing order tests. Storing `user_id` as a required `VARCHAR(36)` column with an index keeps existing order tests clean while enforcing the `Order.userId` domain invariant.

