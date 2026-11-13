# Feature Specification: User Authentication and Role-Based Authorization

**Feature Branch**: `[010-user-jwt-auth]`

**Created**: 2026-11-13

**Status**: Draft

**Input**: User description: "Define the User domain entity with userId (UUID, client generated), username (required, unique), password (required, hashed), roles (required, list of strings). Add userId to orders as a required field (not backwards compatible; update existing orders with userId=1). Introduce JWT based authentication (login, validate, refresh, revoke, register). Add authorization rules for ADMIN, CUSTOMER, and CATALOG roles across products and orders."

## Clarifications

### Session 2026-11-13

- Q: How should initial `ADMIN` and `CATALOG` user accounts be provisioned in a fresh environment so local request scripts and load tests can authenticate with those roles? → A: Seed initial `ADMIN` and `CATALOG` accounts via Liquibase migration only (no `ADMIN` user-creation endpoint).
- Q: How should the load-testing suite (`load-tests/`) be updated to handle authentication? → A: Update existing scenarios to authenticate in `setup()` and add a dedicated `auth` scenario covering register, login, validate, refresh, and revoke.
- Q: How should authorization be structured in the application layer? → A: Implement authorization as a cross-cutting concern using Micronaut AOP (`@AuthorizePolicy` on `ProductService` and `OrderService` handled by a `@InterceptorBean` in `adapter/security`), keeping application services and use-case ports free of authorization flags or caller-scoping logic.
- Q: How should the AOP `MethodInterceptor` enforce list/create scoping rules? → A: Rewrite query/command parameters before `context.proceed()` (e.g., forcing `ACTIVE` status on product lists for non-`CATALOG` callers, or scoping `userId` on order lists/creates for non-`ADMIN` callers) and validate returned entities after `context.proceed()`.
- Q: How should authentication and security services separate interfaces from implementations? → A: Keep `AuthUseCase` as the inbound interface implemented by `AuthService`, and introduce a mockable `CallerContextPort` interface (implemented by `MicronautSecurityCallerContext`) for the AOP authorization interceptors.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Register and Authenticate a User (Priority: P1)

A new or existing user registers an account and signs in with a username and password to receive a JWT token.

**Why this priority**: Authentication is the foundation for all protected product and order operations.

**Independent Test**: Register a user, sign in with valid credentials, and verify that the system returns an active JWT token.

**Acceptance Scenarios**:

1. **Given** no account exists for a username, **When** a user registers with a client-generated `userId` (UUID), a unique `username`, and a `password`, **Then** the system creates the user with the password stored in hashed form and assigns the default `CUSTOMER` role.
2. **Given** a registered user exists, **When** any user submits valid `username` and `password` credentials to the login operation, **Then** the system issues a JWT token containing the user identity and assigned roles.
3. **Given** a user submits an unknown `username` or an incorrect `password` to the login operation, **When** the system checks the credentials, **Then** the system rejects the login request without issuing a token.
4. **Given** a username already exists, **When** a registration request uses that same `username`, **Then** the system rejects the registration request as a duplicate.

---

### User Story 2 - Validate, Refresh, and Revoke Tokens (Priority: P1)

An authenticated user or client validates an active JWT token, refreshes it before or upon expiration, or revokes it to end the session.

**Why this priority**: Token lifecycle operations ensure that sessions remain valid when active and immediately stop working when revoked.

**Independent Test**: Issue a token at login, validate it, refresh it to obtain a new token, revoke the token, and confirm that subsequent validation and refresh attempts fail.

**Acceptance Scenarios**:

1. **Given** a user holds a valid, unrevoked JWT token, **When** a client invokes the token validation operation, **Then** the system confirms the token is valid and returns the associated user identity and roles.
2. **Given** a user holds a valid refreshable JWT token, **When** a client invokes the token refresh operation, **Then** the system issues a new JWT token for that user.
3. **Given** a user holds an active JWT token, **When** the user invokes the token revocation operation, **Then** the system marks the token as revoked so that subsequent validation, refresh, and protected operations reject it.
4. **Given** a token is expired, malformed, or revoked, **When** a client invokes the validation or refresh operation, **Then** the system rejects the token.

---

### User Story 3 - Role-Based Product Catalog Access and Management (Priority: P2)

A `CUSTOMER` user browses active products, while a `CATALOG` user views all product statuses and creates or updates products.

**Why this priority**: Product visibility and catalog management must be separated so customers only see active products and only authorized catalog staff modify the catalog.

**Independent Test**: Authenticate as a `CUSTOMER` user and a `CATALOG` user, then verify product listing, single-product retrieval, creation, and update permissions for active, draft, and archived products.

**Acceptance Scenarios**:

1. **Given** a user has the `CUSTOMER` role without the `CATALOG` role, **When** the user lists products, **Then** the system returns only active products and excludes draft and archived products.
2. **Given** a user has the `CUSTOMER` role without the `CATALOG` role, **When** the user requests a specific active product, **Then** the system returns the product details.
3. **Given** a user has the `CUSTOMER` role without the `CATALOG` role, **When** the user requests a draft or archived product, **Then** the system denies access.
4. **Given** a user has the `CATALOG` role, **When** the user lists or reads products, **Then** the system returns active, draft, and archived products.
5. **Given** a user has the `CATALOG` role, **When** the user creates or updates a product, **Then** the system completes the operation.
6. **Given** a user does not have the `CATALOG` role, **When** the user attempts to create or update a product, **Then** the system denies the request.

---

### User Story 4 - Role-Based Order Ownership and Administration (Priority: P2)

A `CUSTOMER` user creates orders associated with their `userId` and views only their own orders, while an `ADMIN` user lists, reads, and modifies any order.

**Why this priority**: Order data must be tied to the owning user so customers cannot access or alter other customers' orders.

**Independent Test**: Authenticate as two different `CUSTOMER` users and an `ADMIN` user, create orders, and verify ownership scoping for reads, lists, and modifications.

**Acceptance Scenarios**:

1. **Given** an authenticated `CUSTOMER` user, **When** the user creates an order, **Then** the system requires and records the user's `userId` on the order.
2. **Given** an authenticated `CUSTOMER` user, **When** the user lists orders, **Then** the system returns only orders whose `userId` matches that user.
3. **Given** an authenticated `CUSTOMER` user, **When** the user reads an order belonging to another user, **Then** the system denies access.
4. **Given** an authenticated `CUSTOMER` user without the `ADMIN` role, **When** the user attempts to modify an order, **Then** the system denies the request.
5. **Given** an authenticated `ADMIN` user, **When** the user lists orders, reads any order, or modifies any order, **Then** the system allows the operation across all users' orders.

---

### Edge Cases

- What happens when an unauthenticated caller accesses any product or order operation? The system rejects the request and requires authentication.
- What happens when a user submits an invalid or missing `userId` (non-UUID) during registration or order creation? The system rejects the request with a validation error.
- What happens when a user is created or updated with an empty `roles` list? The system rejects the request because every user must have at least one role.
- What happens when existing orders in storage lack a `userId` prior to this change? Existing orders are updated during migration with the designated legacy `userId`.
- What happens when a user holds multiple roles (for example, `CUSTOMER` and `CATALOG`)? The system grants the combined permissions of all assigned roles.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST define a `User` entity with a client-generated UUID `userId`, a required unique `username`, a required hashed `password`, and a required non-empty list of `roles`.
- **FR-002**: System MUST support the initial roles `ADMIN`, `CUSTOMER`, and `CATALOG`, and MUST assign `CUSTOMER` as the default role for new users.
- **FR-003**: System MUST provide a public registration operation for unauthenticated users that creates a user with only the default `CUSTOMER` role, stores passwords only in hashed form, and never exposes password hashes in responses.
- **FR-015**: System MUST seed initial `ADMIN` and `CATALOG` user accounts via database migration so elevated roles are available in fresh environments without an admin user-creation endpoint.
- **FR-016**: Project running/request scripts (`docs/running/requests/*.http`) and related documentation (`docs/running/README.md`, `docs/running/load-testing.md`, `README.md`) MUST be updated to include authentication workflows, JWT bearer headers, and `userId` on orders.
- **FR-017**: Load-testing scenarios (`load-tests/`) MUST be updated so existing scenarios (`products`, `orders`, `mixed`, `stability`) authenticate in `setup()` and send bearer tokens, and a dedicated `auth` scenario MUST exercise register, login, validate, refresh, and revoke.
- **FR-004**: System MUST allow any user (without prior authentication) to access the login operation to exchange valid `username` and `password` credentials for a JWT token.
- **FR-005**: System MUST provide an operation to validate a JWT token and report its validity and associated user identity and roles.
- **FR-006**: System MUST provide an operation to refresh a valid JWT token and issue a new JWT token.
- **FR-007**: System MUST provide an operation to revoke a JWT token so that the revoked token can no longer be validated, refreshed, or used for authenticated requests.
- **FR-008**: System MUST add `userId` as a required field on orders and reject order creation requests that lack a valid `userId` association.
- **FR-009**: System MUST update all existing orders to include the deterministic UUID `00000000-0000-0000-0000-000000000001` as `userId` in a non-backwards-compatible data migration.
- **FR-010**: System MUST allow `CUSTOMER` users to list and read active products, and MUST restrict draft and archived products in both list and single-product reads to users with the `CATALOG` role.
- **FR-011**: System MUST allow only users with the `CATALOG` role to create or update products.
- **FR-012**: System MUST allow `CUSTOMER` users to create orders, and MUST allow only `ADMIN` users to modify orders.
- **FR-013**: System MUST restrict `CUSTOMER` users to listing and reading only their own orders (matching `userId`), while allowing `ADMIN` users to list and read any order.
- **FR-014**: System MUST reject unauthenticated requests to all protected operations and deny authenticated requests that lack the required role or resource ownership.
- **FR-018**: System MUST implement product visibility and order ownership authorization as a cross-cutting concern via Micronaut AOP (`@AuthorizePolicy` on `ProductService` / `OrderService` and an `@InterceptorBean` `MethodInterceptor`), keeping business methods and use-case ports free of authorization logic.
- **FR-019**: System MUST separate interfaces from implementations for authentication (`AuthUseCase` -> `AuthService`) and caller security context (`CallerContextPort` -> `MicronautSecurityCallerContext`) so services and interceptors can be mocked or replaced independently.

### Key Entities *(include if feature involves data)*

- **User**: Represents an authenticated actor in the system. Attributes: `userId` (UUID, client-generated, unique identifier), `username` (required, unique text), `password` (required, stored as a cryptographic hash), `roles` (required, non-empty list of role names from `ADMIN`, `CUSTOMER`, `CATALOG`).
- **Order**: Existing order entity extended with `userId` (required UUID referencing the owning user). Existing orders receive the migration `userId` value.
- **AuthToken**: Represents an issued JWT access/refresh token or revocation state used to authenticate requests, refresh sessions, and enforce token revocation.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of unauthenticated requests to protected product, order, validation, refresh, and revocation operations are rejected.
- **SC-002**: Users can register, log in, validate, refresh, and revoke a token in under 1 second per operation under normal load.
- **SC-003**: 100% of role-restricted operations (draft/archived product access, product mutations, cross-user order reads, and order modifications) enforce role and ownership rules without unauthorized data exposure.
- **SC-004**: 100% of existing orders have a valid non-null `userId` after the data migration completes, and zero plaintext passwords are stored or returned.

## Assumptions

- Users may hold one or more roles simultaneously; permissions are additive across assigned roles.
- An `ADMIN` user who also needs to browse products or place orders will hold the `CUSTOMER` role in addition to `ADMIN`, unless product/order rules explicitly grant `ADMIN` access.
- Token revocation applies immediately on the server side so revoked tokens cannot pass validation or be refreshed, even before their original expiration time.
- Login and registration operations are public entry points; all product and order operations require an active, unrevoked JWT token.

