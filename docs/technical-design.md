# Technical Design

This document describes the technical design of the `sdd-example` service.
It covers the domain model, the system components, and the main use case flows.

---

## 1. System Overview

This repository provides `sdd-example`, an e-commerce REST service that runs on Java 25 and Micronaut 5.2.5.
The service manages user authentication, product categories, products, promotional campaigns, orders, and asynchronous email notifications.

The architecture follows Hexagonal Architecture and Domain-Driven Design (DDD).
Pure business rules live in the domain package with zero framework dependencies.
Application services coordinate use cases through inbound and outbound ports.
Inbound adapters handle HTTP requests and RabbitMQ messages.
Outbound adapters handle MariaDB persistence, security, RabbitMQ publishing, and SMTP delivery.

---

## 2. Domain Model and Entity-Relation Diagram

The domain model resides in `es.nachobrito.sdd.example.domain.model` and uses immutable Java records with constructor invariant validation.

### 2.1 Entity-Relation Diagram

```mermaid
erDiagram
    USER ||--|{ USER_ROLE : "has"
    USER ||--o{ AUTH_TOKEN : "owns"
    USER ||--o{ ORDER : "places"
    CATEGORY |o--o{ PRODUCT : "classifies"
    PRODUCT ||..o{ ORDER_LINE : "referenced by"
    ORDER ||--|{ ORDER_LINE : "contains"
    ORDER ||--|| DELIVERY_ADDRESS : "ships to"
    ORDER ||--|| INVOICE_ADDRESS : "bills to"
    CAMPAIGN ||--o{ CAMPAIGN_PREDICATE : "evaluates"
    CAMPAIGN }o..o{ PRODUCT : "discounts at read time"

    USER {
        uuid id PK
        string username UK
        string passwordHash
        instant createdAt
        instant updatedAt
    }

    USER_ROLE {
        uuid userId FK
        Role role
    }

    AUTH_TOKEN {
        uuid tokenId PK
        uuid userId FK
        TokenType tokenType
        instant issuedAt
        instant expiresAt
        instant revokedAt
    }

    CATEGORY {
        uuid id PK
        string name
        string description
    }

    PRODUCT {
        uuid id PK
        string sku UK
        string name
        string description
        int priceInCents
        string currency
        ProductStatus status
        uuid categoryId FK
        instant createdAt
        instant updatedAt
    }

    CAMPAIGN {
        uuid id PK
        string name
        string description
        CampaignStatus status
        instant startDate
        instant endDate
        int discountPercent
    }

    CAMPAIGN_PREDICATE {
        uuid id PK
        uuid campaignId FK
        PredicateType predicateType
        string value
    }

    ORDER {
        uuid id PK
        uuid userId
        string customerName
        string customerEmail
        instant date
        OrderStatus status
        long totalAmountInCents
        string currency
        instant createdAt
        instant updatedAt
    }

    ORDER_LINE {
        uuid id PK
        uuid orderId FK
        uuid productId
        int quantity
        int unitPriceInCents
        long lineTotalInCents
    }

    DELIVERY_ADDRESS {
        string name
        string telephone
        string street
        string complement
        string city
        string state
        string zipCode
        string country
    }

    INVOICE_ADDRESS {
        string name
        string vatNumber
        string telephone
        string street
        string complement
        string city
        string state
        string zipCode
        string country
    }
```

### 2.2 Domain Entities, Value Objects, and Enumerations

| Model Type | Kind | Key Invariants and Values |
|:---|:---|:---|
| `User` | Aggregate root | Non-blank unique `username` (max 100), non-blank `passwordHash`, and at least one `Role`. |
| `Role` | Enumeration | `ADMIN`, `CUSTOMER`, `CATALOG`. |
| `AuthToken` | Entity | Tracks `tokenId` (`jti`), `TokenType` (`ACCESS` or `REFRESH`), `expiresAt > issuedAt`, and `revokedAt`. |
| `Category` | Aggregate root | Non-blank `name` (max 255) and optional `description` (max 4000). |
| `Product` | Aggregate root | Unique `sku` (max 100), `priceInCents >= 0`, valid ISO-4217 `currency`, `ProductStatus`, and optional `categoryId`. |
| `ProductStatus` | Enumeration | `DRAFT`, `ACTIVE`, `ARCHIVED`. |
| `Campaign` | Aggregate root | `endDate >= startDate`, `discountPercent` from `0` to `100`, `CampaignStatus`, and `CampaignPredicate` list. |
| `CampaignStatus` | Enumeration | `DRAFT`, `ACTIVE`, `INACTIVE`. |
| `CampaignPredicate` | Value object | Evaluates `IN_CATEGORY`, `NOT_IN_CATEGORY` (UUID), `IS_CHEAPER_THAN`, or `IS_MORE_EXPENSIVE_THAN` (cents). |
| `ActiveDiscount` | Projection | Request-time record holding `campaignId`, `campaignName`, and `discountPercent` without modifying `Product`. |
| `Order` | Aggregate root | Owning `userId`, email format, `totalAmountInCents` equal to line sum, non-empty `OrderLine` list, and `OrderStatus`. |
| `OrderStatus` | Enumeration | `PENDING`, `PAID`, `SHIPPED`, `DELIVERED`, `CANCELLED`. |
| `OrderLine` | Value object | `quantity > 0`, `unitPriceInCents >= 0`, and `lineTotalInCents == quantity * unitPriceInCents`. |
| `DeliveryAddress`, `InvoiceAddress` | Value objects | Trimmed required address fields, optional `complement`, and `vatNumber` on `InvoiceAddress`. |
| `OrderLifecycleEvent` | Domain event | Carries `eventId`, `OrderEventType` (`CREATED`, `UPDATED`, `CANCELLED`), `timestamp`, and `Order`. |
| `NotificationMessage` | Value object | Validated `recipient` email, `subject` (max 200), `body`, and `NotificationChannel` (`SMTP`). |

### 2.3 Persistence Mapping Notes

- **Embedded addresses**: `OrderPersistenceAdapter` stores `DeliveryAddress` and `InvoiceAddress` fields directly in columns of the `orders` table.
- **Surrogate keys on child tables**: The `order_lines` and `campaign_predicates` tables include a UUID `id` primary key alongside their parent foreign keys.
- **Category deletion constraint**: The `products.category_id` column enforces a SQL foreign key to `categories.id` with `ON DELETE RESTRICT`.
- **Application-level references**: `orders.user_id` and `order_lines.product_id` use indexes without SQL foreign keys; `OrderService` validates `productId` existence at runtime.
- **Campaign matching**: `Campaign.matches` combines all `CampaignPredicate` entries with logical `AND` and matches all products when `predicates` is empty.
- **Role associations**: The `user_roles` table stores a composite primary key `(user_id, role)` with `ON DELETE CASCADE` on `users.id`.

---

## 3. Component Architecture

The codebase separates external interfaces from domain rules through explicit inbound and outbound port interfaces.
Cross-cutting security and observability concerns execute through Micronaut AOP method interceptors rather than inside business logic.

```mermaid
flowchart LR
    subgraph ExternalIn["External Clients and Triggers"]
        HttpClient["HTTP Clients (JWT Bearer)"]
        BrokerTrigger["RabbitMQ Queue (sdd.orders.notifications)"]
    end

    subgraph Inbound["Inbound Adapters (adapter.in)"]
        AuthCtrl["AuthController + AuthExceptionHandler (/v1/auth)"]
        CatCtrl["CategoryController + CategoryExceptionHandler (/v1/categories)"]
        CampCtrl["CampaignController + CampaignExceptionHandler (/v1/campaigns)"]
        ProdCtrl["ProductController (/v1) + ProductV2Controller (/v2) + ProductExceptionHandler"]
        OrderCtrl["OrderController + OrderExceptionHandler (/v1/orders)"]
        NotifConsumer["OrderNotificationConsumer (@RabbitListener)"]
    end

    subgraph Core["Application and Domain Core"]
        InPorts["Inbound Ports (application.port.in): UseCase Interfaces"]
        subgraph Services["Application Services (application.service)"]
            AuthSvc["AuthService"]
            CatSvc["CategoryService"]
            CampSvc["CampaignService"]
            ProdSvc["ProductService"]
            OrderSvc["OrderService"]
            NotifSvc["NotificationService"]
        end
        Domain["Domain Model (domain.model): Entities, Value Objects, Events, Exceptions"]
        OutPorts["Outbound Ports (application.port.out): Repository, Security, Messaging, Transport"]
    end

    subgraph Outbound["Outbound Adapters and AOP (adapter.out)"]
        SecurityOut["Security (adapter.out.security): AuthorizationInterceptor, CallerContext, JwtTokenIssuer, PasswordHasher, JwtClaimsValidator"]
        PersistenceOut["Persistence (adapter.out.persistence): JDBC Repositories, Adapters, DbQueryMetricsInterceptor"]
        MessagingOut["Messaging (adapter.out.messaging): RabbitMqOrderEventPublisher, OrderEventClient, ChannelInitializer"]
        SmtpOut["SMTP (adapter.out.smtp): SmtpNotificationTransport, SmtpOrderHtmlTemplate, SmtpClient, SmtpMetricsInterceptor"]
    end

    subgraph Infra["Backing Infrastructure"]
        MariaDB["MariaDB (Liquibase Migrations)"]
        RabbitMQ["RabbitMQ (Topic Exchange sdd.orders)"]
        SmtpServer["SMTP Server"]
        OTLP["OTLP Collector / Micrometer"]
    end

    HttpClient --> AuthCtrl
    HttpClient --> CatCtrl
    HttpClient --> CampCtrl
    HttpClient --> ProdCtrl
    HttpClient --> OrderCtrl
    BrokerTrigger --> NotifConsumer

    AuthCtrl --> InPorts
    CatCtrl --> InPorts
    CampCtrl --> InPorts
    ProdCtrl --> InPorts
    OrderCtrl --> InPorts
    NotifConsumer --> InPorts

    InPorts --> Services
    Services --> Domain
    Services --> OutPorts

    SecurityOut -.->|"@AuthorizePolicy"| ProdSvc
    SecurityOut -.->|"@AuthorizePolicy"| OrderSvc
    OutPorts --> SecurityOut
    OutPorts --> PersistenceOut
    OutPorts --> MessagingOut
    OutPorts --> SmtpOut

    PersistenceOut --> MariaDB
    MessagingOut --> RabbitMQ
    SmtpOut --> SmtpServer
    PersistenceOut -.->|"db.query.duration"| OTLP
    SmtpOut -.->|"smtp.messages, smtp.send.duration"| OTLP
```

### Component Responsibilities

| Component Group | Package | Responsibility |
|:---|:---|:---|
| REST controllers and handlers | `adapter.in.rest` | Validate HTTP requests, enforce `@Secured` roles, call inbound ports, and map domain exceptions to HTTP status codes. |
| Messaging consumer | `adapter.in.messaging` | Consume `OrderEventDto` messages from `sdd.orders.notifications` and invoke `ProcessNotificationUseCase`. |
| Inbound ports | `application.port.in` | Define use case contracts and immutable command or query records. |
| Application services | `application.service` | Orchestrate transactions, domain validation, repository calls, discount resolution, and event publishing. |
| Domain model | `domain.model` | Enforce business invariants inside pure Java entities, value objects, domain events, and exception hierarchies. |
| Outbound ports | `application.port.out` | Abstract persistence, caller context, password hashing, JWT operations, event publishing, and notifications. |
| Security adapter and AOP | `adapter.out.security` | Issue and validate JWT tokens with revocation checks, hash passwords with PBKDF2, and enforce `@AuthorizePolicy` scoping. |
| Persistence adapters | `adapter.out.persistence` | Map domain models to Micronaut Data JDBC entities and record `db.query.duration` via `@DbQueryMetrics`. |
| Messaging publisher | `adapter.out.messaging` | Declare exchange `sdd.orders`, bind `sdd.orders.notifications`, and publish `order.*` events asynchronously. |
| SMTP transport | `adapter.out.smtp` | Render HTML emails with escaped fields, transmit messages via `SmtpClient`, and record `@SmtpMetrics`. |

---

## 4. Main Use Cases and Flow Diagrams

### 4.1 Authentication and Revocation-Aware JWT Validation

`AuthController` exposes `/v1/auth/register`, `/v1/auth/login`, `/v1/auth/validate`, `/v1/auth/refresh`, and `/v1/auth/revoke`.
Registration assigns `Role.CUSTOMER` and hashes passwords through `Pbkdf2PasswordHasher`.
Login issues both `ACCESS` and `REFRESH` tokens and stores their `tokenId` values in `auth_tokens`.
On every secured HTTP request, `RevocationAwareJwtClaimsValidator` checks the database to verify an active and unrevoked `ACCESS` token.

```mermaid
flowchart TD
    Start["POST /v1/auth/login"] --> FindUser["AuthService loads User by username"]
    FindUser --> ValidCreds{"User exists and password matches?"}
    ValidCreds -->|No| Return401["Throw AuthenticationException: 401 Unauthorized"]
    ValidCreds -->|Yes| SaveTokens["Save ACCESS and REFRESH AuthToken rows in MariaDB"]
    SaveTokens --> IssueJwt["JwtTokenIssuerAdapter signs ACCESS and REFRESH JWTs"]
    IssueJwt --> ReturnTokens["Return 200 OK with IssuedTokens"]

    ProtectedReq["Protected HTTP Request with Bearer JWT"] --> VerifySig["Micronaut verifies JWT signature and expiration"]
    VerifySig --> Validator["RevocationAwareJwtClaimsValidator checks ACCESS token type"]
    Validator --> CheckDb{"Token exists and isActiveAt(now)?"}
    CheckDb -->|No| Reject401["Reject request: 401 Unauthorized"]
    CheckDb -->|Yes| CheckRole{"Caller holds @Secured role?"}
    CheckRole -->|No| Reject403["Reject request: 403 Forbidden"]
    CheckRole -->|Yes| InvokeEndpoint["Invoke REST controller"]
```

Token refresh (`POST /v1/auth/refresh`) validates an active `REFRESH` token, revokes that token in MariaDB, and issues a fresh token pair.
Token revocation (`POST /v1/auth/revoke`) marks the presented token as revoked immediately.

---

### 4.2 Category Management and Deletion Protection

`CategoryController` restricts `POST /v1/categories` and `DELETE /v1/categories/{id}` to `ADMIN` users, and restricts `GET /v1/categories` to `CUSTOMER` or `CATALOG` users.
The seeded `admin` account in Liquibase holds the `ADMIN`, `CUSTOMER`, and `CATALOG` roles.
`CategoryService` prevents deletion when any product references the target category.

```mermaid
flowchart TD
    CreateReq["POST /v1/categories (ADMIN)"] --> ValidateCat["Construct Category domain record"]
    ValidateCat --> DupCat{"categoryRepository.existsById?"}
    DupCat -->|Yes| ConflictCreate["Throw DuplicateCategoryIdentifierException: 409 Conflict"]
    DupCat -->|No| SaveCat["Save Category in MariaDB: 201 Created"]

    DeleteReq["DELETE /v1/categories/{id} (ADMIN)"] --> ExistsCat{"categoryRepository.existsById?"}
    ExistsCat -->|No| NotFoundCat["Throw CategoryNotFoundException: 404 Not Found"]
    ExistsCat -->|Yes| InUseCheck{"productRepository.existsByCategoryId?"}
    InUseCheck -->|Yes| ConflictInUse["Throw CategoryInUseException: 409 Conflict"]
    InUseCheck -->|No| RemoveCat["Delete Category from MariaDB: 204 No Content"]
```

---

### 4.3 Product Creation and Update (`/v1/products` and `/v2/products`)

Catalog managers (`CATALOG` role) create, update, and delete products through versioned endpoints.
`ProductController` (`/v1/products`) keeps `categoryId` optional for backward compatibility.
`ProductV2Controller` (`/v2/products`) requires `categoryId` on the request DTO.

```mermaid
flowchart TD
    WriteReq["POST or PUT /v1 or /v2 products (CATALOG)"] --> DtoVal["Bean validation on request DTO (/v2 requires categoryId)"]
    DtoVal --> ExistsCheck{"POST ID or SKU conflict, or PUT missing ID or SKU conflict?"}
    ExistsCheck -->|"ID missing on PUT"| Return404["Throw ProductNotFoundException: 404 Not Found"]
    ExistsCheck -->|"Duplicate ID or SKU"| Return409["Throw DuplicateIdentifierException or DuplicateSkuException: 409 Conflict"]
    ExistsCheck -->|OK| HasCategory{"categoryId present?"}
    HasCategory -->|Yes| CheckCat{"categoryRepository.existsById?"}
    CheckCat -->|No| BadCat["Throw InvalidProductDataException: 400 Bad Request"]
    CheckCat -->|Yes| DomainCheck["Validate Product invariants (non-negative price, ISO-4217 currency)"]
    HasCategory -->|No| DomainCheck
    DomainCheck --> SaveProd["ProductPersistenceAdapter saves Product (@DbQueryMetrics)"]
    SaveProd --> WriteDone["Return 201 Created or 200 OK with ProductResponse"]
```

---

### 4.4 Product Reads, AOP Visibility Scoping, and Request-Time Discounts

Both `/v1/products` and `/v2/products` expose `GET` and `GET /{id}` to callers holding `CUSTOMER` or `CATALOG` roles.
Before returning `ProductResponse`, the controller calls `ResolveProductDiscountsUseCase` to evaluate active campaigns at request time without storing discounts on `Product`.

```mermaid
flowchart TD
    ReadReq["GET /v1 or /v2 products or /{id} (CUSTOMER or CATALOG)"] --> CallSvc["ProductUseCase.listProducts or getProduct"]
    CallSvc --> AopCheck{"Caller holds CATALOG role?"}
    AopCheck -->|"No (list)"| ForceActive["AuthorizationInterceptor sets status filter to ACTIVE"]
    AopCheck -->|"No (getById) and not ACTIVE"| Throw403["Throw AuthorizationException: 403 Forbidden"]
    AopCheck -->|"Yes, or ACTIVE product"| LoadProd["Load Product entities from MariaDB"]
    ForceActive --> LoadProd

    LoadProd --> ResolveDiscounts["CampaignService.resolveActiveDiscounts at Clock instant now"]
    ResolveDiscounts --> LoadActive["CampaignRepositoryPort.findActiveAt(now) for ACTIVE window matches"]
    LoadActive --> EvalPred["Match each Product against CampaignPredicate rules (logical AND)"]
    EvalPred --> MapResp["Map matches to ActiveDiscount list in ProductResponse.discounts"]
    MapResp --> Return200["Return 200 OK with unmodified Product.priceInCents"]
```

`CampaignController` (`/v1/campaigns`) exposes `POST`, `PUT /{id}`, `GET`, and `DELETE /{id}` operations for `ADMIN` users so catalog reads reflect campaign changes immediately.

---

### 4.5 Order Management and AOP Ownership Scoping

Customers (`CUSTOMER` role) create orders through `POST /v1/orders` and read their own orders through `GET /v1/orders` and `GET /v1/orders/{id}`.
Administrators (`ADMIN` role) read all orders and perform updates (`PUT /v1/orders/{id}`) or deletions (`DELETE /v1/orders/{id}`).

```mermaid
flowchart TD
    CreateReq["POST /v1/orders (CUSTOMER)"] --> InjectUser["AuthorizationInterceptor (ORDER_CREATE) sets command.userId from CallerContext"]
    InjectUser --> CheckOrderId{"Order ID already exists?"}
    CheckOrderId -->|Yes| Err409["Throw DuplicateOrderIdentifierException: 409 Conflict"]
    CheckOrderId -->|No| CheckProds{"All line productId entries exist?"}
    CheckProds -->|No| Err400["Throw InvalidOrderDataException: 400 Bad Request"]
    CheckProds -->|Yes| BuildOrder["Order validates lines and computes totalAmountInCents"]
    BuildOrder --> SaveOrder["OrderPersistenceAdapter saves Order and OrderLine rows"]
    SaveOrder --> PubCreated["OrderEventPublisherPort publishes OrderEventType.CREATED"]
    PubCreated --> Resp201["Return 201 Created"]

    ReadOrders["GET /v1/orders or /{id} (CUSTOMER or ADMIN)"] --> ScopeRead{"Caller holds ADMIN role?"}
    ScopeRead -->|No| EnforceOwner["ORDER_LIST filters by caller userId; ORDER_READ throws 403 on mismatch"]
    ScopeRead -->|Yes| ReturnOrders["Return requested Order records"]
```

On `PUT /v1/orders/{id}`, `OrderService` validates product IDs, updates the order, and publishes `OrderEventType.CANCELLED` for cancelled orders or `OrderEventType.UPDATED` otherwise.
On `DELETE /v1/orders/{id}`, `OrderService` rejects statuses other than `PENDING` or `CANCELLED` with `409 Conflict`, deletes the order, and publishes `OrderEventType.CANCELLED`.

---

### 4.6 Asynchronous Order Notification Pipeline

Order write operations publish lifecycle events to RabbitMQ instead of sending emails synchronously during HTTP requests.
`RabbitMqChannelInitializer` declares a durable topic exchange `sdd.orders`, declares a durable queue `sdd.orders.notifications`, and binds them with key `order.*`.

```mermaid
flowchart LR
    OrderSvc["OrderService"] -->|"publishOrderCreated / Updated / Cancelled"| Publisher["RabbitMqOrderEventPublisher"]
    Publisher -->|"OrderEventDto and routing key order.*"| Client["OrderEventClient (@RabbitClient)"]
    Client -->|"Publish to sdd.orders"| Exchange["RabbitMQ Topic Exchange (sdd.orders)"]
    Exchange -->|"Binding order.*"| Queue["Queue (sdd.orders.notifications)"]
    Queue --> Consumer["OrderNotificationConsumer (@RabbitListener)"]
    Consumer -->|"OrderLifecycleEvent"| NotifSvc["NotificationService"]
    NotifSvc --> Transport["SmtpNotificationTransport"]
    Transport --> Template["SmtpOrderHtmlTemplate renders escaped HTML"]
    Template --> Smtp["SmtpClient (@SmtpMetrics) sends email to SMTP server"]
```

`SmtpNotificationTransport` constructs a validated `NotificationMessage` with `NotificationChannel.SMTP`.
`SmtpMetricsInterceptor` wraps `SmtpClient.sendEmail` to record `smtp.messages` counters and `smtp.send.duration` timers with the `sent` or `failed` outcome tag.
`NotificationService` logs transport failures without stopping the consumer loop so failed deliveries do not block subsequent messages.
