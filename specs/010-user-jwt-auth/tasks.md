# Tasks: User Authentication and Role-Based Authorization

**Input**: Design documents from `/specs/010-user-jwt-auth/`
**Prerequisites**: [plan.md](file:///home/nacho/github/sdd-example/specs/010-user-jwt-auth/plan.md), [spec.md](file:///home/nacho/github/sdd-example/specs/010-user-jwt-auth/spec.md), [research.md](file:///home/nacho/github/sdd-example/specs/010-user-jwt-auth/research.md), [data-model.md](file:///home/nacho/github/sdd-example/specs/010-user-jwt-auth/data-model.md), [auth-api.yaml](file:///home/nacho/github/sdd-example/specs/010-user-jwt-auth/contracts/auth-api.yaml)

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., `US1`, `US2`, `US3`, `US4`)
- Exact file paths are included in every task description.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Authorize and configure Micronaut Security JWT dependencies and application security settings.

- [X] T001 Obtain explicit user authorization and add `micronaut-security-jwt` and `micronaut-security-annotations` to `gradle/libs.versions.toml` and `build.gradle.kts`
- [X] T002 Configure Micronaut Security (`micronaut.security.enabled: true`, `authentication: bearer`, JWT HMAC secret, access/refresh token TTLs) in `src/main/resources/application.yml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core database schema, domain entities, persistence adapters, and password hashing required by all user stories.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T003 Create Liquibase expand-backfill-contract migration creating `users` (`id` VARCHAR(36) PK NOT NULL, `username` VARCHAR(100) UNIQUE NOT NULL, `password_hash` VARCHAR(255) NOT NULL, `created_at` TIMESTAMP(6) NOT NULL, `updated_at` TIMESTAMP(6) NOT NULL), `user_roles` (`user_id` VARCHAR(36) NOT NULL, `role` VARCHAR(20) NOT NULL, composite PK), `auth_tokens` (`token_id` VARCHAR(36) PK NOT NULL, `user_id` VARCHAR(36) NOT NULL, `token_type` VARCHAR(20) NOT NULL, `issued_at` TIMESTAMP(6) NOT NULL, `expires_at` TIMESTAMP(6) NOT NULL, `revoked_at` TIMESTAMP(6) NULL), adding `orders.user_id` (backfilled to `00000000-0000-0000-0000-000000000001`, then NOT NULL with `idx_orders_user_id`), and seeding initial `ADMIN` and `CATALOG` accounts in `src/main/resources/db/changelog/004-add-users-and-order-user-id.xml` and include it in `src/main/resources/db/changelog/db.changelog-master.xml`
- [X] T004 [P] Create `Role` enum (`ADMIN`, `CUSTOMER`, `CATALOG`) and `TokenType` enum (`ACCESS`, `REFRESH`) in `src/main/java/es/nachobrito/sdd/example/domain/model/Role.java` and `src/main/java/es/nachobrito/sdd/example/domain/model/TokenType.java`
- [X] T005 [P] Create domain exceptions (`InvalidUserDataException`, `DuplicateUsernameException`, `AuthenticationException`, `AuthorizationException`) in `src/main/java/es/nachobrito/sdd/example/domain/model/`
- [X] T006 [P] Create `User` domain record (`UUID id` non-null, `String username` non-blank trimmed max 100 chars, `String passwordHash` non-blank, `List<Role> roles` non-empty immutable, `Instant createdAt` non-null, `Instant updatedAt` non-null) in `src/main/java/es/nachobrito/sdd/example/domain/model/User.java`
- [X] T007 [P] Create `AuthToken` domain record (`UUID tokenId` non-null, `UUID userId` non-null, `TokenType tokenType` non-null, `Instant issuedAt` non-null, `Instant expiresAt` after `issuedAt`, `@Nullable Instant revokedAt`) in `src/main/java/es/nachobrito/sdd/example/domain/model/AuthToken.java`
- [X] T008 [P] Create outbound ports `UserRepositoryPort`, `AuthTokenRepositoryPort`, `PasswordHasherPort`, and `TokenIssuerPort` in `src/main/java/es/nachobrito/sdd/example/application/port/out/`
- [X] T009 [P] Implement `Pbkdf2PasswordHasher` (`PBKDF2WithHmacSHA256`, 16-byte `SecureRandom` salt, 310000 iterations, constant-time `MessageDigest.isEqual`) in `src/main/java/es/nachobrito/sdd/example/adapter/out/security/Pbkdf2PasswordHasher.java`
- [X] T010 [P] Implement `UserEntity`, `UserRoleEntity`, `UserJdbcRepository`, `UserRoleJdbcRepository`, and `UserPersistenceAdapter` in `src/main/java/es/nachobrito/sdd/example/adapter/out/persistence/`
- [X] T011 [P] Implement `AuthTokenEntity`, `AuthTokenJdbcRepository`, and `AuthTokenPersistenceAdapter` in `src/main/java/es/nachobrito/sdd/example/adapter/out/persistence/`
- [X] T012 [P] Add unit tests for `User`, `AuthToken`, and `Pbkdf2PasswordHasher` in `src/test/java/es/nachobrito/sdd/example/domain/model/UserTest.java` and `src/test/java/es/nachobrito/sdd/example/adapter/out/security/Pbkdf2PasswordHasherTest.java`

**Checkpoint**: Foundation ready — user story implementation can now begin.

---

## Phase 3: User Story 1 - Register and Authenticate a User (Priority: P1) 🎯 MVP

**Goal**: Allow public `CUSTOMER` registration with client-generated UUID and public credential login returning signed JWT tokens.

**Independent Test**: Register a new user via `POST /v1/auth/register` and sign in via `POST /v1/auth/login` to receive a valid JWT token.

- [X] T013 [P] [US1] Create inbound use case ports `RegisterUserUseCase`, `LoginUseCase`, and `AuthUseCase` in `src/main/java/es/nachobrito/sdd/example/application/port/in/`
- [X] T014 [P] [US1] Implement `JwtTokenIssuerAdapter` (using Micronaut Security JWT generator, embedding `userId` subject, `username`, `roles`, and `jti`) in `src/main/java/es/nachobrito/sdd/example/adapter/out/security/JwtTokenIssuerAdapter.java`
- [X] T015 [US1] Implement registration (assigning only `Role.CUSTOMER`, hashing password, checking duplicate `userId`/`username`) and login (verifying credentials, issuing and persisting access + refresh `AuthToken`) in `src/main/java/es/nachobrito/sdd/example/application/service/AuthService.java`
- [X] T016 [P] [US1] Create REST DTOs `RegisterRequest` (`userId` UUID, `username` 1..100, `password` 8..128), `UserResponse` (no password), `LoginRequest`, and `TokenResponse` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/dto/`
- [X] T017 [US1] Create `AuthController` (`@Controller("/v1/auth")`, `@Secured(SecurityRule.IS_ANONYMOUS)` on `/register` and `/login`) and `AuthExceptionHandler` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/AuthController.java` and `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/AuthExceptionHandler.java`
- [X] T018 [P] [US1] Add unit and integration tests for US1 registration and login flows in `src/test/java/es/nachobrito/sdd/example/application/service/AuthServiceTest.java` and `src/test/java/es/nachobrito/sdd/example/adapter/in/rest/AuthRegisterLoginTestCase.java`

**Checkpoint**: User Story 1 is independently functional and testable (MVP).

---

## Phase 4: User Story 2 - Validate, Refresh, and Revoke Tokens (Priority: P1)

**Goal**: Support JWT validation, refresh token rotation, and immediate server-side token revocation.

**Independent Test**: Obtain a token via login, validate it (`POST /v1/auth/validate`), refresh it (`POST /v1/auth/refresh`), revoke it (`POST /v1/auth/revoke`), and verify subsequent validation/refresh/protected calls fail.

- [X] T019 [P] [US2] Create inbound use case ports `ValidateTokenUseCase`, `RefreshTokenUseCase`, and `RevokeTokenUseCase` (and extend `AuthUseCase`) in `src/main/java/es/nachobrito/sdd/example/application/port/in/`
- [X] T020 [US2] Implement token validation, refresh rotation (revoking old refresh token and issuing new pair), and revocation in `src/main/java/es/nachobrito/sdd/example/application/service/AuthService.java`
- [X] T021 [P] [US2] Implement `TokenRevocationValidator` (`TokenValidator` bean checking `AuthTokenRepositoryPort` revocation status on every bearer request) in `src/main/java/es/nachobrito/sdd/example/adapter/out/security/TokenRevocationValidator.java`
- [X] T022 [P] [US2] Create DTOs `TokenValidationRequest`, `TokenValidationResponse`, `RefreshTokenRequest`, and `RevokeTokenRequest` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/dto/`
- [X] T023 [US2] Add `/validate`, `/refresh`, and `/revoke` endpoints to `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/AuthController.java`
- [X] T024 [P] [US2] Add integration tests for token validation, refresh, and revocation in `src/test/java/es/nachobrito/sdd/example/adapter/in/rest/AuthTokenLifecycleTestCase.java`

**Checkpoint**: User Stories 1 and 2 are both independently functional.

---

## Phase 5: User Story 3 - Role-Based Product Catalog Access and Management (Priority: P2)

**Goal**: Restrict product listing and reading of `DRAFT`/`ARCHIVED` products and all product mutations to `CATALOG` users, while allowing `CUSTOMER` users to list and read `ACTIVE` products.

**Independent Test**: Authenticate as `CUSTOMER` and `CATALOG` users and verify product listing, single-product read, creation, and update permissions across `ACTIVE`, `DRAFT`, and `ARCHIVED` products.

- [X] T025 [US3] Update `GetProductUseCase` and `ListProductsUseCase` (passing caller visibility flag `includeNonActive`) and enforce active-only filtering/access in `src/main/java/es/nachobrito/sdd/example/application/service/ProductService.java`
- [X] T026 [US3] Add `@Secured` annotations (`CUSTOMER`/`CATALOG` on reads, `CATALOG` only on create/update/delete) and pass caller `Authentication` roles in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/ProductController.java`
- [X] T027 [P] [US3] Update existing product controller/service tests and add role-based product authorization integration tests in `src/test/java/es/nachobrito/sdd/example/adapter/in/rest/ProductAuthorizationTestCase.java`

**Checkpoint**: User Story 3 is independently functional and tested.

---

## Phase 6: User Story 4 - Role-Based Order Ownership and Administration (Priority: P2)

**Goal**: Require `userId` on all orders, scope `CUSTOMER` order reads/lists to their own `userId`, allow `CUSTOMER` order creation, and restrict order modifications and cross-user reads/lists to `ADMIN`.

**Independent Test**: Authenticate as two different `CUSTOMER` users and an `ADMIN` user, create orders, and verify ownership scoping for reads, lists, and modifications.

- [X] T028 [US4] Add required `UUID userId` (`userId == null` -> `InvalidOrderDataException`) to `Order` in `src/main/java/es/nachobrito/sdd/example/domain/model/Order.java`
- [X] T029 [P] [US4] Add `userId` to `OrderEntity`, `OrderJdbcRepository` (filter by `userId`), and `OrderPersistenceAdapter` in `src/main/java/es/nachobrito/sdd/example/adapter/out/persistence/`
- [X] T030 [US4] Update `CreateOrderUseCase`, `GetOrderUseCase`, `ListOrdersUseCase`, and `OrderService` to record `userId` on creation and enforce `ADMIN` vs `CUSTOMER` ownership on get and list in `src/main/java/es/nachobrito/sdd/example/application/service/OrderService.java`
- [X] T031 [US4] Add `@Secured` annotations (`CUSTOMER` on create, `CUSTOMER`/`ADMIN` on get/list, `ADMIN` on update/delete), extract caller `userId` and roles from `Authentication`, and include `userId` in `OrderResponse` in `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/OrderController.java` and `src/main/java/es/nachobrito/sdd/example/adapter/in/rest/dto/OrderResponse.java`
- [X] T032 [P] [US4] Update existing order tests for required `userId` + JWT auth and add `OrderAuthorizationTestCase` in `src/test/java/es/nachobrito/sdd/example/adapter/in/rest/OrderAuthorizationTestCase.java`

**Checkpoint**: All user stories (US1–US4) are independently functional.

---

## Phase 7: Running Scripts, Load Testing, Documentation & Verification

**Purpose**: Update request scripts, k6 load-testing scenarios, documentation, and run full verification gates.

- [X] T033 [P] Create `docs/running/requests/auth.http` (register, login, validate, refresh, revoke) and update `docs/running/requests/products.http` and `docs/running/requests/orders.http` with Bearer auth headers and `userId` on orders
- [X] T034 [P] Update `load-tests/helpers/payloads.js`, `load-tests/run.sh`, and existing k6 scenarios (`load-tests/scenarios/products.js`, `orders.js`, `mixed.js`, `stability.js`) to authenticate in `setup()` and send `Authorization: Bearer` headers
- [X] T035 [P] Create dedicated k6 `auth` scenario in `load-tests/scenarios/auth.js` exercising register, login, validate, refresh, and revoke
- [X] T036 [P] Update `README.md`, `docs/running/README.md`, and `docs/running/load-testing.md` in ASD-STE100 to document JWT auth, seeded accounts, `.http` usage, and the `auth` load-test scenario
- [X] T037 [P] Verify all new classes, Javadocs, and comments follow ASD-STE100 and emit structured logs for auth events (`auth_register_succeeded`, `auth_login_succeeded`, `auth_login_failed`, `auth_token_revoked`)
- [X] T038 Run full build and static analysis (`./gradlew build` including `-Werror`, Checkstyle, PMD, SpotBugs, ArchUnit `HexagonalArchitectureTest`, Pitest 90%/80%, and integration tests) and validate `specs/010-user-jwt-auth/quickstart.md`

---

## Phase 8: Micronaut AOP Authorization Refactoring & Interface Separation

**Purpose**: Move product visibility and order ownership authorization out of application services into a Micronaut AOP `MethodInterceptor` backed by a mockable `CallerContextPort`.

- [X] T039 [P] Create `CallerContextPort` (`CallerContext(UUID userId, Set<Role> roles)` and `Optional<CallerContext> currentCaller()`) in `src/main/java/es/nachobrito/sdd/example/application/port/out/CallerContextPort.java`
- [X] T040 [P] Create `@AuthorizePolicy` (`@Around` binding annotation) and `AuthorizationPolicy` enum (`PRODUCT_READ`, `PRODUCT_LIST`, `ORDER_READ`, `ORDER_LIST`, `ORDER_CREATE`) in `src/main/java/es/nachobrito/sdd/example/application/security/`
- [X] T041 [P] Implement `MicronautSecurityCallerContext` (implementing `CallerContextPort` via Micronaut `SecurityService`) and `AuthorizationInterceptor` (`@InterceptorBean(AuthorizePolicy.class)` `MethodInterceptor`) in `src/main/java/es/nachobrito/sdd/example/adapter/out/security/`
- [X] T042 [US3] Remove `includeNonActive` authorization logic from `GetProductUseCase`, `ListProductsUseCase`, `ProductService`, and `ProductController`, and annotate `ProductService.getProduct` / `listProducts` with `@AuthorizePolicy`
- [X] T043 [US4] Remove `callerUserId` / `isAdmin` authorization logic from `GetOrderUseCase`, `ListOrdersUseCase`, `OrderService`, and `OrderController`, and annotate `OrderService` (`createOrder`, `getOrder`, `listOrders`) with `@AuthorizePolicy`
- [X] T044 [P] Add unit tests for `AuthorizationInterceptor` and `MicronautSecurityCallerContext` in `src/test/java/es/nachobrito/sdd/example/adapter/out/security/`
- [X] T045 [P] Update `ProductServiceQueryTest`, `OrderServiceQueryTest`, `OrderServiceCommandTest`, and `OrderControllerTest` to match the clean authorization-free service signatures
- [X] T046 Update `docs/journal/010-user-jwt-auth.md` in ASD-STE100 to document the Micronaut AOP authorization refactoring
- [X] T047 Run full build (`./gradlew build --no-daemon`) and verify compilation (`-Werror`), unit/integration tests, ArchUnit, Checkstyle, PMD, SpotBugs, and Pitest thresholds pass

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: Must complete first (requires user authorization for build dependencies).
- **Phase 2 (Foundational)**: Depends on Phase 1; BLOCKS all user stories (US1–US4).
- **Phase 3 (US1)**: Depends on Phase 2. Delivers MVP (register + login).
- **Phase 4 (US2)**: Depends on US1 (`AuthService`, `AuthController`, `JwtTokenIssuerAdapter`).
- **Phase 5 (US3)** & **Phase 6 (US4)**: Depend on Phase 2 and JWT security setup from Phase 1/US1; US3 and US4 are independent of each other and can proceed in parallel.
- **Phase 7 (Polish)**: Depends on all desired user stories being complete.

### Parallel Opportunities

- Within Phase 2: `T004`–`T012` can run in parallel once `T003` (or alongside `T003`) is defined.
- Within Phase 3: `T013`, `T014`, `T016` can run in parallel before `T015` and `T017`.
- Phase 5 (US3) and Phase 6 (US4) can run in parallel after US1/US2.

## Parallel Example: Foundational Phase

```bash
# Run domain & port creation in parallel:
Task: T004 Role & TokenType enums
Task: T005 Auth domain exceptions
Task: T006 User domain record
Task: T007 AuthToken domain record
Task: T008 Outbound ports
Task: T009 Pbkdf2PasswordHasher
```

## Implementation Strategy

1. **MVP First**: Complete Phase 1 -> Phase 2 -> Phase 3 (US1) and validate register + login.
2. **Token Lifecycle**: Complete Phase 4 (US2) for validate, refresh, and revocation.
3. **Authorization Rollout**: Complete Phase 5 (US3: Products) and Phase 6 (US4: Orders) in parallel or sequentially.
4. **Full Gate**: Run `./gradlew build` in Phase 7 to verify all tests, ArchUnit, Checkstyle, PMD, SpotBugs, and Pitest thresholds.

