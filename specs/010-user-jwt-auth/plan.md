# Implementation Plan: User Authentication and Role-Based Authorization

**Branch**: `010-user-jwt-auth` | **Date**: 2026-11-13 | **Spec**: [spec.md](file:///home/nacho/github/sdd-example/specs/010-user-jwt-auth/spec.md)

**Input**: Feature specification from `/specs/010-user-jwt-auth/spec.md`

## Summary

Introduce the `User` domain entity (`userId` UUID, `username`, `passwordHash`, `roles`), add required `userId` to `Order` (with a Liquibase backfill of `00000000-0000-0000-0000-000000000001` for existing rows), seed initial `ADMIN` and `CATALOG` accounts via Liquibase, implement JWT-based authentication (`/v1/auth/register`, `/v1/auth/login`, `/v1/auth/validate`, `/v1/auth/refresh`, `/v1/auth/revoke`), enforce role-based and ownership-based authorization across products (`CUSTOMER`, `CATALOG`) and orders (`CUSTOMER`, `ADMIN`) using Micronaut Security and Micronaut AOP, and update running/request scripts (`docs/running/requests/*.http`), k6 load-testing scenarios (`load-tests/`), and related documentation.

## Technical Context

**Language/Version**: Java 25  
**Primary Dependencies**: Micronaut 5.2.5 (`micronaut-data-jdbc`, `micronaut-validation`, `micronaut-serde-jackson`, `micronaut-liquibase`, `micronaut-openapi`, `micronaut-micrometer-core`); requires adding `micronaut-security-jwt` and `micronaut-security-annotations` (see Constitution Check / Escalation Note)  
**Storage**: MariaDB via Micronaut Data JDBC and Liquibase  
**Testing**: JUnit 5, REST Assured (`micronaut-test-rest-assured`), Mockito, ArchUnit (`HexagonalArchitectureTest`), Pitest (90% mutation, 80% coverage), Checkstyle, PMD, SpotBugs  
**Target Platform**: Linux server (Netty HTTP runtime)  
**Project Type**: Single-module hexagonal web service (`es.nachobrito.sdd.example`)  
**Performance Goals**: Auth operations (register, login, validate, refresh, revoke) complete in under 1 second under normal load  
**Constraints**: Strict hexagonal layer isolation (`HexagonalArchitectureTest`), ASD-STE100 documentation, `-Werror` compilation, zero static-analysis suppressions  
**Scale/Scope**: Single-process service; 3 initial roles (`ADMIN`, `CUSTOMER`, `CATALOG`), 5 auth endpoints, updated product & order authorization

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Principle 1 (Minimum Viable Diff)**: Scope is strictly limited to `User`, `Order.userId` + migration, `/v1/auth/*` endpoints, and the stated role/ownership rules on `ProductController` and `OrderController`.
- **Principle 2 (Ambiguity Halts Execution)**: Both critical ambiguities (`FR-003` public `CUSTOMER` registration; `FR-009` UUID backfill `00000000-0000-0000-0000-000000000001`) were resolved with the user prior to planning.
- **Principle 3 (Do Not Distribute by Default)**: Token revocation and user persistence use the existing MariaDB database; no Redis or external auth service is introduced.
- **Principle 4 (Optimize for Deletion)**: Auth domain, ports, service, and adapters are self-contained without generic base classes or speculative abstractions.
- **Principle 5 (Make Dependencies Explicit)**: Constructor injection only (enforced by ArchUnit `noFieldInjection`).
- **Principle 6 (Contract at the Boundary)**: Versioned `/v1/auth/*` DTOs with `@Valid` bean validation; domain models remain framework-free.
- **Principle 9 (Recovery Over Prevention)**: Liquibase migration `004-add-users-and-order-user-id.xml` uses expand-backfill-contract with explicit `<rollback>`.
- **Infrastructure Escalation Note (`AGENTS.md` Section 2)**: Adding `micronaut-security-jwt` and `micronaut-security-annotations` to `gradle/libs.versions.toml` and `build.gradle.kts` requires explicit user authorization before editing build files during `/speckit-implement`.

## Project Structure

### Documentation (this feature)

```text
specs/010-user-jwt-auth/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── auth-api.yaml    # OpenAPI contract for /v1/auth/*
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
src/main/java/es/nachobrito/sdd/example/
├── domain/model/
│   ├── User.java, Role.java, AuthToken.java, TokenType.java
│   ├── InvalidUserDataException.java, DuplicateUsernameException.java, AuthenticationException.java
│   └── Order.java (updated with UUID userId)
├── application/
│   ├── port/in/  (RegisterUserUseCase, LoginUseCase, ValidateTokenUseCase, RefreshTokenUseCase, RevokeTokenUseCase, AuthUseCase; GetProductUseCase, ListProductsUseCase, GetOrderUseCase, ListOrdersUseCase without caller/role params)
│   ├── port/out/ (UserRepositoryPort, AuthTokenRepositoryPort, PasswordHasherPort, TokenIssuerPort, CallerContextPort)
│   ├── security/ (AuthorizePolicy, AuthorizationPolicy)
│   └── service/  (AuthService, OrderService, ProductService — pure business logic with declarative @AuthorizePolicy)
└── adapter/
    ├── in/rest/  (AuthController, AuthExceptionHandler, dto/*, OrderController & ProductController updated with @Secured)
    └── out/
        ├── persistence/ (UserEntity, UserRoleEntity, AuthTokenEntity, *JdbcRepository, *PersistenceAdapter, OrderEntity updated)
        └── security/    (Pbkdf2PasswordHasher, JwtTokenIssuerAdapter, RevocationAwareJwtClaimsValidator, MicronautSecurityCallerContext, AuthorizationInterceptor)
src/main/resources/db/changelog/
└── 004-add-users-and-order-user-id.xml

docs/
├── README.md (updated auth & role overview)
└── running/
    ├── README.md (updated with auth setup & seeded credentials)
    ├── load-testing.md (updated with auth scenario & token setup)
    └── requests/
        ├── auth.http (new: register, login, validate, refresh, revoke)
        ├── products.http (updated with Bearer token)
        └── orders.http (updated with Bearer token & userId)

load-tests/
├── run.sh (updated to support auth scenario)
├── helpers/payloads.js (updated with auth/user payloads)
└── scenarios/
    ├── auth.js (new: register, login, validate, refresh, revoke)
    ├── products.js (updated with setup() auth & Bearer token)
    ├── orders.js (updated with setup() auth & Bearer token)
    ├── mixed.js (updated with setup() auth & Bearer token)
    └── stability.js (updated with setup() auth & Bearer token)
```

**Structure Decision**: Follow the existing single-module hexagonal package layout under `es.nachobrito.sdd.example` so all `HexagonalArchitectureTest` rules remain satisfied.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Adding `micronaut-security-jwt` & `micronaut-security-annotations` (build config change) | Provides Micronaut-native JWT signing/verification and `@Secured` AOP required by `AGENTS.md` Sections 3 & 5 | Manual HTTP filter + custom JWT parsing duplicates framework security and lacks `@Secured` AOP support |
