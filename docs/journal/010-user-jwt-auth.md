# Feature Journal: User Authentication and Role-Based Authorization

This document records the development process for the user authentication and role-based authorization feature.
The feature adds JWT authentication, token lifecycle management, and role-based access control for products and orders.

---

## 1. Implementation Process

The implementation followed the Spec-Driven Development workflow.

First, the agent created the feature specification in `specs/010-user-jwt-auth/spec.md`.
The specification defines four prioritized user stories:
- User registration and credential login (P1).
- Token validation, refresh rotation, and server-side revocation (P1).
- Role-based product catalog access and management (P2).
- Role-based order ownership and administration (P2).

Next, the user clarified that the change includes running scripts, load-testing infrastructure, and documentation.
The agent updated the specification, implementation plan, contracts, and task list in `specs/010-user-jwt-auth/tasks.md`.
The agent also requested and received user authorization before adding `micronaut-security-jwt` and `micronaut-security-annotations` to the build.

Then, the agent implemented the solution across all architectural layers:
- Created Liquibase migration `004-add-users-and-order-user-id.xml` to add `users`, `user_roles`, `auth_tokens`, seeded accounts, and `orders.user_id`.
- Implemented domain models (`User`, `AuthToken`, `Role`, `TokenType`, `Order.OrderDraft`) and domain exceptions.
- Implemented `Pbkdf2PasswordHasher`, `JwtTokenIssuerAdapter`, `RevocationAwareJwtClaimsValidator`, and persistence adapters.
- Implemented `AuthService` and updated `ProductService` and `OrderService` for role and ownership rules.
- Implemented `AuthController`, `AuthExceptionHandler`, and `@Secured` rules on `ProductController` and `OrderController`.
- Updated `docs/running/requests/` HTTP files, `load-tests/` k6 scenarios, `README.md`, and `docs/running/` guides.

---

## 2. Bugs Found and Technical Challenges

The agent found and fixed four issues during implementation and testing.

### Refresh Token Blocked by HTTP Claims Validator
`RevocationAwareJwtClaimsValidator` initially rejected any token without `TokenType.ACCESS`.
`JwtTokenIssuerAdapter.parseToken()` invokes the Micronaut `JwtValidator` with a `null` HTTP request.
Because of this, `/v1/auth/refresh` failed when it validated a refresh token.
The agent updated `RevocationAwareJwtClaimsValidator` to allow non-ACCESS tokens when `request` is `null`, while HTTP bearer requests still require `TokenType.ACCESS`.

### Unknown Routes Returning HTTP 403 Instead of HTTP 404
With Micronaut Security enabled, requests to non-existent routes returned HTTP 403 Forbidden instead of HTTP 404 Not Found.
The existing `ErrorRouteRemovalTestCase` expected HTTP 404 on removed error routes.
The agent configured `micronaut.security.reject-not-found: false` in `src/main/resources/application.yml` to restore standard 404 behavior.

### Backward Compatibility for Null `userId` in Order Events
`OrderPayloadDto` initially rejected a `null` `userId` in RabbitMQ order event payloads.
The existing `OrderEventDtoTest` verified backward compatibility when legacy messages omit `userId`.
`OrderPayloadDto` now defaults a `null` `userId` to the legacy backfill identifier `00000000-0000-0000-0000-000000000001`.

### Duplicate Token Lookup in `AuthService`
`AuthService.refresh()` and `AuthService.revoke()` called `parseActiveToken()` and then queried `authTokenRepository.findByTokenId()` a second time.
The agent updated `parseActiveToken()` to return a private `ActiveToken` record containing both claims and the stored token.
This change removed the duplicate repository call and eliminated uncovered fallback branches.

---

## 3. Static Code Analysis

The agent ran Checkstyle, PMD, SpotBugs, and ArchUnit (`./gradlew build --no-daemon`) without changing thresholds or adding suppressions.
All static analysis issues were fixed directly in the source and test files:
- **Checkstyle**: Fixed line-length violations (`> 100` characters) and `VariableDeclarationUsageDistance` warnings in authorization integration tests.
- **PMD**:
  - Replaced the 10-parameter `Order.create` signature with `Order.create(OrderDraft)` (`ExcessiveParameterList`).
  - Removed the 10-parameter convenience constructor from `OrderPayloadDto` (`ExcessiveParameterList`).
  - Reduced imports in `OrderController`, `OrderControllerTest`, and `AuthServiceTest` (`ExcessiveImports`).
  - Preserved exception causes in `AuthenticationException` and `OrderController` (`PreserveStackTrace`).
  - Replaced `java.util.Date` usage in `JwtTokenIssuerAdapter` and `JwtTokenIssuerAdapterTest` with `java.time` and epoch claims (`ReplaceJavaUtilDate`).
  - Extracted helper methods in `JwtTokenIssuerAdapter` (`CyclomaticComplexity`) and guarded log calls in `AuthService` (`GuardLogStatement`).
  - Added explicit assertions in integration tests (`UnitTestShouldIncludeAssert`) and avoided returning or assigning `null` in `OrderDraft` (`NullAssignment`, `ReturnEmptyCollectionRatherThanNull`).
- **SpotBugs**:
  - Added defensive `List.copyOf` copies in compact constructors and accessors for `TokenValidationResponse`, `UserResponse`, `ValidateTokenUseCase.TokenValidationResult`, `TokenIssuerPort.ParsedTokenClaims`, and `Order.OrderDraft` (`EI_EXPOSE_REP`, `EI_EXPOSE_REP2`).
  - Fixed a nullable dereference check in `AuthController` (`NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE`) and a discarded return value check in `UserTest` (`RV_RETURN_VALUE_IGNORED_INFERRED`).

---

## 4. Test Coverage and Mutation Testing

The project enforces a minimum line coverage of 80 percent and a minimum Pitest mutation score of 90 percent.
The initial Pitest run after feature completion scored 74 percent because several new security and persistence components lacked dedicated unit tests.

The agent added unit tests across all new components:
- `UserTest`: Added validation, role immutability, and `AuthToken` lifecycle and null-field tests.
- `Pbkdf2PasswordHasherTest`: Added hashing, verification, and malformed-hash tests.
- `AuthServiceTest`: Added registration, login, validation, refresh, revocation, and failure-path tests.
- `JwtTokenIssuerAdapterTest` and `RevocationAwareJwtClaimsValidatorTest`: Added token issuance, claim parsing, and revocation validator tests.
- `AuthControllerTest` and `AuthExceptionHandlerTest`: Added endpoint delegation, bearer header parsing, and exception mapping tests.
- `UserPersistenceAdapterTest` and `AuthTokenPersistenceAdapterTest`: Added persistence mapping and repository delegation tests.
- `ProductServiceQueryTest` and `OrderServiceQueryTest`: Added role-based visibility and ownership assertions.
- `AuthRegisterLoginTestCase`, `AuthTokenLifecycleTestCase`, `ProductAuthorizationTestCase`, and `OrderAuthorizationTestCase`: Added end-to-end REST integration tests.

With these additions, the final `./gradlew build --no-daemon` run passed with 98 percent line coverage (`1265/1294`) and 90 percent mutation score (`575/639` mutations killed).

---

## 5. Design Refinement: Micronaut AOP Authorization and Interface Separation

After the initial implementation, a design review identified that authorization logic was mixed into the application layer.
Specifically, `ProductController` and `OrderController` extracted roles and `userId` from `Authentication` and passed them as explicit flags (`includeNonActive`, `callerUserId`, `isAdmin`) into `ProductService` and `OrderService`.

### Discussion and Architectural Tradeoffs

The user requested an alternate design using Micronaut AOP for cross-cutting authorization and clear interface separation:
- **Interceptor Placement**: The team evaluated placing the AOP interceptor on application services versus REST controllers.
  The team selected application services (`ProductService` and `OrderService`) with a declarative `@AuthorizePolicy` annotation and an `@InterceptorBean` in `adapter.out.security`.
  This keeps REST controllers thin and aligns with `AGENTS.md` Section 3.
- **Scoping Strategy**: The team evaluated rewriting query/command arguments versus rejecting unauthorized filters.
  The team selected rewriting parameters before `context.proceed()` and validating returned entities after `context.proceed()`.
  For example, `PRODUCT_LIST` forces `ProductStatus.ACTIVE` for non-`CATALOG` callers, and `ORDER_LIST` / `ORDER_CREATE` scopes `userId` for non-`ADMIN` callers.
  `PRODUCT_READ` and `ORDER_READ` validate returned entities after `context.proceed()` and throw `AuthorizationException` when access is denied.
- **Interface Separation**: `AuthUseCase` already served as the inbound interface implemented by `AuthService`.
  To decouple the AOP interceptor from Micronaut Security internals, the team introduced a mockable `CallerContextPort` interface in `application.port.out`, implemented by `MicronautSecurityCallerContext`.

### Implementation and Verification

The agent refactored the affected components across all layers:
- Added `CallerContextPort` in `application.port.out` and `@AuthorizePolicy` / `AuthorizationPolicy` in `application.security`.
- Implemented `MicronautSecurityCallerContext` and `AuthorizationInterceptor` in `adapter.out.security`.
- Removed `includeNonActive`, `callerUserId`, and `isAdmin` parameters from `GetProductUseCase`, `ListProductsUseCase`, `GetOrderUseCase`, `ProductService`, `OrderService`, `ProductController`, and `OrderController`.
- Added unit tests in `AuthorizationInterceptorTest` and `MicronautSecurityCallerContextTest`, and simplified `ProductServiceQueryTest`, `OrderServiceQueryTest`, `ProductControllerTest`, and `OrderControllerTest`.
- Fixed PMD `CyclomaticComplexity`, `UseVarargs`, `UseShortArrayInitializer`, and `LambdaCanBeMethodReference` findings in the new classes.

The final `./gradlew build --no-daemon` run passed all checks with 98 percent line coverage (`1325/1351`) and 90 percent mutation score (`597/660` mutations killed).


